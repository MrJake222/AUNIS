package mrjake.aunis.integration;

import cofh.api.util.ThermalExpansionHelper;
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
		if (OreDictionary.getOres("itemSilicon").isEmpty()) {
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
		ThermalExpansionHelper.addCompactorRecipe(10000, new ItemStack(Items.QUARTZ, 3), new ItemStack(AunisItems.crystalSeed));
		
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.crystalSeed), new ItemStack(AunisItems.crystalRed), new FluidStack(AunisFluids.moltenSiliconRed, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.crystalSeed), new ItemStack(AunisItems.crystalBlue), new FluidStack(AunisFluids.moltenSiliconBlue, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.crystalSeed), new ItemStack(AunisItems.crystalEnder), new FluidStack(AunisFluids.moltenSiliconEnder, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.crystalSeed), new ItemStack(AunisItems.crystalYellow), new FluidStack(AunisFluids.moltenSiliconYellow, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.crystalSeed), new ItemStack(AunisItems.crystalWhite), new FluidStack(AunisFluids.moltenSiliconWhite, 1000), false);
		
		
		// Naquadah
		ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(AunisItems.naquadahShard), new FluidStack(AunisFluids.moltenNaquadahRaw, 500));
		ThermalExpansionHelper.addRefineryRecipe(2500, new FluidStack(AunisFluids.moltenNaquadahRaw, 250), new FluidStack(AunisFluids.moltenNaquadahRefined, 200), ItemStack.EMPTY);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(Items.IRON_INGOT), new ItemStack(AunisItems.naquadahAlloy), new FluidStack(AunisFluids.moltenNaquadahRefined, 500), false);
		
		ThermalExpansionHelper.addCrucibleRecipe(10000, new ItemStack(AunisItems.naquadahAlloy), new FluidStack(AunisFluids.moltenNaquadahAlloy, 1000));

		
		// Circuits
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.circuitControlBase), new ItemStack(AunisItems.circuitControlCrystal), new FluidStack(AunisFluids.moltenSiliconWhite, 1000), false);
		ThermalExpansionHelper.addTransposerFill(10000, new ItemStack(AunisItems.circuitControlBase), new ItemStack(AunisItems.circuitControlNaquadah), new FluidStack(AunisFluids.moltenNaquadahAlloy, 1000), false);
	}
}
