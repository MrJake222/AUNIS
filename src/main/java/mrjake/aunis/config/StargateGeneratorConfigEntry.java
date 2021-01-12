package mrjake.aunis.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.worldgen.stargate.StargateGenerationException;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/**
 * StargateGenerator JSON object
 */
public final class StargateGeneratorConfigEntry {
    public String template;
    public SymbolTypeEnum stargateType = SymbolTypeEnum.MILKYWAY;
    public boolean enableMysteriousPage = true;
    public String generatorType = "surface";
    public JsonObject generatorSettings = new JsonObject();

    public StargateSettings stargate = new StargateSettings();

    public static final class StargateSettings {
        public String datablock = "base";
        public int datablockOffsetX = 0;
        public int datablockOffsetY = -1;
        public int datablockOffsetZ = 0;
        public StargateUpgrades upgrades = new StargateUpgrades();
    }

    public static final class StargateUpgrades {
        public int capacitors = 1;
        public boolean glyphMilkyWay = false;
        public boolean glyphPegasus = false;
        public boolean glyphUniverse = false;
        public boolean chevronUpgrade = false;

        /**
         * Fill stargate inventory with upgrades
         * @param func fill callback. Parameters: slot id, itemstack
         */
        public void apply(BiConsumer<Integer, ItemStack> func) { //0-3 upgrades, 4-6 capacitors
            for (int i = 0; i < Math.min(capacitors, 3); i++) {
                func.accept(i + 4, new ItemStack(AunisBlocks.CAPACITOR_BLOCK));
            }
            int lastUpgradeSlot = 0;

            if(glyphMilkyWay && lastUpgradeSlot < 4)
                func.accept(lastUpgradeSlot++, new ItemStack(AunisItems.CRYSTAL_GLYPH_MILKYWAY));
            if(glyphPegasus && lastUpgradeSlot < 4)
                func.accept(lastUpgradeSlot++, new ItemStack(AunisItems.CRYSTAL_GLYPH_PEGASUS));
            if(glyphUniverse && lastUpgradeSlot < 4)
                func.accept(lastUpgradeSlot++, new ItemStack(AunisItems.CRYSTAL_GLYPH_UNIVERSE));
            if(chevronUpgrade && lastUpgradeSlot < 4)
                func.accept(lastUpgradeSlot++, new ItemStack(AunisItems.CRYSTAL_GLYPH_STARGATE));
        }
    }

    public DHDSettings dhd = new DHDSettings();

    public static final class DHDSettings {
        public String datablock = "dhd";
        public int datablockOffsetX = 0;
        public int datablockOffsetY = -1;
        public int datablockOffsetZ = 0;
        public double despawnChance = AunisConfig.mysteriousConfig.despawnDhdChance;
        public int minFuel = 6000;
        public int maxFuel = 9000;
        public DHDUpgrades upgrades = new DHDUpgrades();
    }

    public static final class DHDUpgrades {
        public boolean controlCrystal = true;
        public boolean chevronUpgrade = false;

        /**
         * Fill dhd inventory with upgrades
         * @param func fill callback. Parameters: slot id, itemstack
         */
        public void apply(BiConsumer<Integer, ItemStack> func) { //0 control crystal, 1-3 upgrades
            if(controlCrystal)
                func.accept(0, new ItemStack(AunisItems.CRYSTAL_CONTROL_DHD));

            // Use lastUpgradeSlot if more upgrades will be added
            if(chevronUpgrade)
                func.accept(1, new ItemStack(AunisItems.CRYSTAL_GLYPH_DHD));
        }
    }

    public List<Integer> dimWhitelist = new ArrayList<>();
    public List<Integer> dimBlacklist = new ArrayList<>();

    public WorldServer getWorldForGeneration(Random rand){
        int dimId = 0;
        if(!dimWhitelist.isEmpty()) {
            dimId = dimWhitelist.get(rand.nextInt(dimWhitelist.size()));
        } else {
            List<Integer> possibleDims = Arrays.stream(DimensionManager.getIDs())
                    .filter(id -> !dimBlacklist.contains(id))
                    .collect(Collectors.toList());
            dimId = possibleDims.get(rand.nextInt(possibleDims.size()));
        }

        if(!DimensionManager.isDimensionRegistered(dimId))
            throw new StargateGenerationException("Dimension with id %s not found!", dimId);

        WorldServer world = DimensionManager.getWorld(dimId);
        if (world == null) {
            DimensionManager.initDimension(dimId);
            world = DimensionManager.getWorld(dimId);
        }
        return world;
    }
}
