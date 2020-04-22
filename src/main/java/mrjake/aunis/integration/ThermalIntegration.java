package mrjake.aunis.integration;

import cofh.api.util.ThermalExpansionHelper;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class ThermalIntegration {

	public static void registerRecipes() {
		// Silicons
		if (OreDictionary.getOres("itemSilicon").isEmpty() || !AunisConfig.recipeConfig.enableSiliconRecipes) {
			ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(Blocks.SAND), new FluidStack(AunisFluids.moltenSiliconBlack, 50));
		}
		
		else {
			for (ItemStack itemStack : OreDictionary.getOres("itemSilicon")) {
				ThermalExpansionHelper.addCrucibleRecipe(2500, itemStack, new FluidStack(AunisFluids.moltenSiliconBlack, 100));
			}
		}
		
		ThermalExpansionHelper.addBrewerRecipe(2000, new ItemStack(Items.REDSTONE), new FluidStack(AunisFluids.moltenSiliconBlack, 200), new FluidStack(AunisFluids.moltenSiliconRed, 250));
		ThermalExpansionHelper.addBrewerRecipe(2000, new ItemStack(Items.DYE, 1, 4), new FluidStack(AunisFluids.moltenSiliconBlack, 200), new FluidStack(AunisFluids.moltenSiliconBlue, 250));
		ThermalExpansionHelper.addBrewerRecipe(8000, new ItemStack(Items.ENDER_PEARL), new FluidStack(AunisFluids.moltenSiliconBlack, 800), new FluidStack(AunisFluids.moltenSiliconEnder, 1000));
		ThermalExpansionHelper.addBrewerRecipe(8000, new ItemStack(Items.GLOWSTONE_DUST), new FluidStack(AunisFluids.moltenSiliconBlack, 200), new FluidStack(AunisFluids.moltenSiliconYellow, 250));
		ThermalExpansionHelper.addRefineryRecipe(2500, new FluidStack(AunisFluids.moltenSiliconBlack, 250), new FluidStack(AunisFluids.moltenSiliconWhite, 100), ItemStack.EMPTY);

		
		// Crystals
		ThermalExpansionHelper.addCompactorRecipe(10000, new ItemStack(Items.QUARTZ, 3), new ItemStack(AunisItems.CRYSTAL_SEED));
		
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CRYSTAL_SEED), new ItemStack(AunisItems.CRYSTAL_RED), new FluidStack(AunisFluids.moltenSiliconRed, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CRYSTAL_SEED), new ItemStack(AunisItems.CRYSTAL_BLUE), new FluidStack(AunisFluids.moltenSiliconBlue, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CRYSTAL_SEED), new ItemStack(AunisItems.CRYSTAL_ENDER), new FluidStack(AunisFluids.moltenSiliconEnder, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CRYSTAL_SEED), new ItemStack(AunisItems.CRYSTAL_YELLOW), new FluidStack(AunisFluids.moltenSiliconYellow, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CRYSTAL_SEED), new ItemStack(AunisItems.CRYSTAL_WHITE), new FluidStack(AunisFluids.moltenSiliconWhite, 1000), false);
		
		
		// Naquadah
		ThermalExpansionHelper.addCrucibleRecipe(25000, new ItemStack(AunisBlocks.ORE_NAQUADAH_BLOCK), new FluidStack(AunisFluids.moltenNaquadahRaw, 6800));
		ThermalExpansionHelper.addCrucibleRecipe(25000, new ItemStack(AunisBlocks.ORE_NAQUADAH_BLOCK_STONE), new FluidStack(AunisFluids.moltenNaquadahRaw, 6800));
		ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(AunisItems.NAQUADAH_SHARD), new FluidStack(AunisFluids.moltenNaquadahRaw, 500));
		ThermalExpansionHelper.addRefineryRecipe(2500, new FluidStack(AunisFluids.moltenNaquadahRaw, 300), new FluidStack(AunisFluids.moltenNaquadahRefined, 200), ItemStack.EMPTY);
		ThermalExpansionHelper.addTransposerFill(2500, new ItemStack(Items.IRON_INGOT), new ItemStack(AunisItems.NAQUADAH_ALLOY_RAW, 2), new FluidStack(AunisFluids.moltenNaquadahRaw, 1000), true);
		ThermalExpansionHelper.addTransposerFill(2500, new ItemStack(Items.IRON_INGOT), new ItemStack(AunisItems.NAQUADAH_ALLOY, 2), new FluidStack(AunisFluids.moltenNaquadahRefined, 1000), true);
		
		ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(AunisItems.NAQUADAH_ALLOY), new FluidStack(AunisFluids.moltenNaquadahAlloy, 1000));

		
		// Circuits
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CIRCUIT_CONTROL_BASE), new ItemStack(AunisItems.CIRCUIT_CONTROL_CRYSTAL), new FluidStack(AunisFluids.moltenSiliconWhite, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.CIRCUIT_CONTROL_BASE), new ItemStack(AunisItems.CIRCUIT_CONTROL_NAQUADAH), new FluidStack(AunisFluids.moltenNaquadahAlloy, 1000), false);
	
		// Capacitor
		ThermalExpansionHelper.addTransposerFill(80000, new ItemStack(AunisBlocks.CAPACITOR_BLOCK_EMPTY), new ItemStack(AunisBlocks.CAPACITOR_BLOCK), new FluidStack(AunisFluids.moltenSiliconRed, 8000), false);
	}
}
