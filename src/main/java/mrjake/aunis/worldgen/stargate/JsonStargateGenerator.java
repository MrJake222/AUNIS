package mrjake.aunis.worldgen.stargate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import mrjake.aunis.api.worldgen.stargate.OptimalPlaceFinderAbstract;
import mrjake.aunis.api.worldgen.stargate.OptimalStargatePlace;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateGeneratorConfigEntry;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.util.LinkingHelper;
import mrjake.aunis.worldgen.stargate.finders.OptimalPlaceFinderNether;
import mrjake.aunis.worldgen.stargate.finders.OptimalPlaceFinderOverworld;
import mrjake.aunis.worldgen.stargate.finders.OptimalPlaceFinderSurface;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class JsonStargateGenerator {
    private static final Map<String, Function<JsonObject, OptimalPlaceFinderAbstract>> PLACE_FINDER_MAP = new HashMap<>();

    private final String name;
    private final StargateGeneratorConfigEntry config;
    private final OptimalPlaceFinderAbstract optimalPlaceFinder;

    public JsonStargateGenerator(String name, StargateGeneratorConfigEntry config) {
        this.name = name;
        this.config = config;
        this.optimalPlaceFinder = getPlaceFinder(config.generatorType, config.generatorSettings);
    }

    public String getName() {
        return name;
    }

    public StargateGeneratorConfigEntry getConfig() {
        return config;
    }

    public GeneratedStargate generateStargate(Random rand, BlockPos startPos) {
        WorldServer world = config.getWorldForGeneration(rand);
        return placeStargate(world, optimalPlaceFinder.findOptimalPlace(world, startPos));
    }

    private Template getTemplate(WorldServer world, TemplateManager templateManager, BlockPos pos) {
        String templateName = config.template;
        if(templateName.contains("%biome%"))
            templateName = templateName.replaceAll("%biome%", world.getBiome(pos).getRegistryName().getResourcePath());
        if(templateName.contains("%size%"))
            templateName = templateName.replaceAll("%size%", AunisConfig.stargateSize == StargateSizeEnum.LARGE ? "large" : "small");

        Template template = templateManager.get(world.getMinecraftServer(), new ResourceLocation(templateName));

        if(template == null)
            throw new StargateGenerationException("Template %s not found!", templateName);

        return template;
    }

    private BlockPos placeStargateBase(WorldServer world, BlockPos dataPos) {
        BlockPos gatePos = dataPos.add(config.stargate.datablockOffsetX, config.stargate.datablockOffsetY, config.stargate.datablockOffsetZ);
        world.setBlockToAir(dataPos);

        StargateAbstractBaseTile tile = (StargateAbstractBaseTile) world.getTileEntity(gatePos);

        IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null); //0-3 upgrades, 4-6 capacitors
        config.stargate.upgrades.apply((slot, stack) -> itemHandler.insertItem(slot, stack, false));

        tile.getMergeHelper().updateMembersMergeStatus(world, gatePos, tile.getFacing(), true);

        return gatePos;
    }

    private BlockPos placeDHD(WorldServer world, BlockPos dataPos) {
        BlockPos dhdPos = dataPos.add(config.dhd.datablockOffsetX, config.dhd.datablockOffsetY, config.dhd.datablockOffsetZ);
        world.setBlockToAir(dataPos);

        if(config.dhd.despawnChance > 0) {
            if (world.rand.nextFloat() < config.dhd.despawnChance) {
                world.setBlockToAir(dhdPos);
                return null;
            }
        }

        else {
            DHDTile tile = (DHDTile) world.getTileEntity(dhdPos);

            if(config.dhd.fuel > 0) {
                FluidTank tank = (FluidTank) tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                tank.fillInternal(new FluidStack(AunisFluids.moltenNaquadahRefined, Math.min(config.dhd.fuel, tank.getCapacity())), true);
            }

            IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null); //0 control crystal, 1-3 upgrades
            config.dhd.upgrades.apply((slot, stack) -> itemHandler.insertItem(slot, stack, false));
        }

        return dhdPos;
    }

    private GeneratedStargate placeStargate(WorldServer world, OptimalStargatePlace place){
        Template template = getTemplate(world, world.getStructureTemplateManager(), place.pos);

        if(template == null)
            throw new StargateGenerationException("Structure template not found in placeStargate");

        PlacementSettings settings = new PlacementSettings()
                .setIgnoreStructureBlock(false)
                .setRotation(place.rotation);

        template.addBlocksToWorld(world, place.pos, optimalPlaceFinder.getTemplateProcessor(), settings, 3);

        BlockPos stargatePos = null;
        BlockPos dhdPos = null;

        for(Map.Entry<BlockPos, String> en : template.getDataBlocks(place.pos, settings).entrySet()) {
            if(en.getValue().equals(config.stargate.datablock)) {
                stargatePos = placeStargateBase(world, en.getKey());
            } else if(en.getValue().equals(config.dhd.datablock)) {
                dhdPos = placeDHD(world, en.getKey());
            }
        }

        if(stargatePos == null)
            throw new StargateGenerationException("StargatePos is null in placeStargate method of JsonStargateGenerator");

        if(dhdPos != null && config.stargateType.equals(SymbolTypeEnum.MILKYWAY))
            LinkingHelper.updateLinkedGate(world, stargatePos, dhdPos);

        return new GeneratedStargate((StargateAbstractBaseTile) world.getTileEntity(stargatePos));
    }


    // ------------------------------------------------------------------------
    // Place finders registry

    @Nonnull
    private static OptimalPlaceFinderAbstract getPlaceFinder(String generatorType, JsonObject settings) {
        return PLACE_FINDER_MAP.containsKey(generatorType) ? PLACE_FINDER_MAP.get(generatorType).apply(settings) : new OptimalPlaceFinderSurface(settings);
    }

    /**
     * @param generatorType generator type for JSON
     * @param creator object constructor or any other function to get place finder from options
     */
    public static void registerPlaceFinder(String generatorType, Function<JsonObject, OptimalPlaceFinderAbstract> creator){
        PLACE_FINDER_MAP.put(generatorType, creator);
    }

    static {
        registerPlaceFinder("overworld", OptimalPlaceFinderOverworld::new);
        registerPlaceFinder("nether", OptimalPlaceFinderNether::new);
    }
}
