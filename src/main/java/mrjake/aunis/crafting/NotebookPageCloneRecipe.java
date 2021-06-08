package mrjake.aunis.crafting;

import mrjake.aunis.item.AunisItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class NotebookPageCloneRecipe extends Impl<IRecipe> implements IRecipe {

	public NotebookPageCloneRecipe() {
		setRegistryName("page_notebook_cloning");
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		
		int matchCount = 0;
		int pageCount = 0;
		
		for (int i=0; i<inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			Item item = stack.getItem();
			
			if (item == AunisItems.PAGE_NOTEBOOK_ITEM && stack.getMetadata() == 1) {
				matchCount++;
				pageCount++;
			}
			
			else if (item == Items.PAPER) {
				matchCount++;
			}
			
			else if (!stack.isEmpty()) {
				return false;
			}
		}
		
		return pageCount == 1 && matchCount >= 2;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack pageStack = null;
		int outputCount = 0;
		
		for (int i=0; i<inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			Item item = stack.getItem();
			
			if (item == AunisItems.PAGE_NOTEBOOK_ITEM) {
				pageStack = stack;
				outputCount++;
			}
			
			else if (item == Items.PAPER) {
				outputCount++;
			}
		}
		
		ItemStack output = pageStack.copy();
		output.setCount(outputCount);		
		return output;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width*height >= 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
}
