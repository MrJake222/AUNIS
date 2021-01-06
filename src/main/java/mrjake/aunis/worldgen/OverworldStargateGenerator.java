package mrjake.aunis.worldgen;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.stargate.merging.StargateMilkyWayMergeHelper;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public final class OverworldStargateGenerator extends AbstractStargateGenerator<StargateMilkyWayBaseTile> {
    public static OverworldStargateGenerator INSTANCE = new OverworldStargateGenerator();

    @Override
    protected OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos startPos) {
//		boolean nether = rand.nextFloat() < AunisConfig.mysteriousConfig.netherChance;
        BlockPos pos;
        int tries = 0;

        WorldServer worldToSpawn = world.getMinecraftServer().getWorld(0);

        do {
            int x = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (world.rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (world.rand.nextBoolean() ? -1 : 1);
            int z = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (world.rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (world.rand.nextBoolean() ? -1 : 1);

            pos = checkForPlace(worldToSpawn, x/16, z/16);
            tries++;
        } while (pos == null && tries < 100);

        if (tries == 100) {
            Aunis.logger.debug("StargateGenerator: Failed to find place");

            return null;
        }

        return new OptimalStargatePlace(pos, findOptimalRotation(worldToSpawn, pos));
    }

    private static final int SG_SIZE_X = 12;
    private static final int SG_SIZE_Z = 13;

    private static final int SG_SIZE_X_PLAINS = 11;
    private static final int SG_SIZE_Z_PLAINS = 11;

    private static BlockPos checkForPlace(World world, int chunkX, int chunkZ) {
        if (world.isChunkGeneratedAt(chunkX, chunkZ))
            return null;

        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);

        int y = chunk.getHeightValue(8, 8);

        if (y > 240)
            return null;

        BlockPos pos = new BlockPos(chunkX*16, y, chunkZ*16);
        String biomeName = chunk.getBiome(pos, world.getBiomeProvider()).getRegistryName().getResourcePath();

        boolean desert = biomeName.contains("desert");

        if (!biomeName.contains("ocean") && !biomeName.contains("river") && !biomeName.contains("beach")) {
//		if (biomeName.contains("Ocean")) {
            int x = desert ? SG_SIZE_X : SG_SIZE_X_PLAINS;
            int z = desert ? SG_SIZE_Z : SG_SIZE_Z_PLAINS;

            int y1 = chunk.getHeightValue(0, 0);
            int y2 = chunk.getHeightValue(x, z);

            int y3 = chunk.getHeightValue(x, 0);
            int y4 = chunk.getHeightValue(0, z);

            // No steep hill
            if (Math.abs(y1 - y2) <= 1 && Math.abs(y3 - y4) <= 1) {
                return pos.subtract(new BlockPos(0, 1, 0));
            }

            else {
                Aunis.logger.debug("StargateGenerator: too steep");
            }
        }

        else {
            Aunis.logger.debug("StargateGenerator: failed, " + biomeName);
        }

        return null;
    }

    private static final int MAX_CHECK = 100;

    private static Rotation findOptimalRotation(World world, BlockPos pos) {
        BlockPos start = pos.add(0, 5, 5);
        int max = -1;
        EnumFacing maxFacing = EnumFacing.EAST;

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            RayTraceResult rayTraceResult = world.rayTraceBlocks(new Vec3d(start), new Vec3d(start.offset(facing, MAX_CHECK)));

            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                int distance = (int) rayTraceResult.getBlockPos().distanceSq(start);
//				Aunis.info(facing.getName().toUpperCase() + ": distance: " + distance);

                if (distance > max) {
                    max = distance;
                    maxFacing = facing;
                }
            }

            else {
//				Aunis.info(facing.getName().toUpperCase() + ": null");

                max = 100000;
                maxFacing = facing;
            }
        }

//		Aunis.info("maxFacing: " + maxFacing.getName().toUpperCase());
        switch (maxFacing) {
            case SOUTH: return Rotation.CLOCKWISE_90;
            case WEST:  return Rotation.CLOCKWISE_180;
            case NORTH: return Rotation.COUNTERCLOCKWISE_90;
            case EAST:  return Rotation.NONE;
            default:    return Rotation.NONE;
        }
    }

    @Override
    protected Template getTemplate(WorldServer world, TemplateManager templateManager, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        boolean desert = biome.getRegistryName().getResourcePath().contains("desert");

        String templateName = "sg_";
        templateName += desert ? "desert" : "plains";
        templateName += AunisConfig.stargateSize == StargateSizeEnum.LARGE ? "_large" : "_small";

        return templateManager.getTemplate(world.getMinecraftServer(), new ResourceLocation(Aunis.ModID, templateName));
    }

    @Override
    protected BlockPos placeStargateBase(WorldServer world, BlockPos dataPos) {
        final BlockPos gatePos = dataPos.add(0, -3, 0);

        StargateMilkyWayBaseTile tile = (StargateMilkyWayBaseTile) world.getTileEntity(gatePos);

        tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).insertItem(4, new ItemStack(AunisBlocks.CAPACITOR_BLOCK), false);

        StargateMilkyWayMergeHelper.INSTANCE.updateMembersBasePos(world, gatePos, tile.getFacing());

        world.setBlockToAir(dataPos);
        world.setBlockToAir(dataPos.down()); // save block
        return gatePos;
    }

    @Override
    protected BlockPos placeDHD(WorldServer world, BlockPos dataPos) {
        world.setBlockToAir(dataPos);
        final BlockPos dhdPos = dataPos.down();

        if (world.rand.nextFloat() < AunisConfig.mysteriousConfig.despawnDhdChance) {
            world.setBlockToAir(dhdPos);
            return null;
        }

        else {
            int fluid = AunisConfig.powerConfig.stargateEnergyStorage / AunisConfig.dhdConfig.energyPerNaquadah;

            world.getTileEntity(dhdPos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).insertItem(0, new ItemStack(AunisItems.CRYSTAL_CONTROL_DHD), false);
            ((FluidTank) world.getTileEntity(dhdPos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)).fillInternal(new FluidStack(AunisFluids.moltenNaquadahRefined, fluid), true);
        }
        return dhdPos;
    }
}
