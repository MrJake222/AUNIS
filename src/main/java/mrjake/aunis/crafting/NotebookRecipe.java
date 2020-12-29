package mrjake.aunis.crafting;

import mrjake.aunis.item.AunisItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class NotebookRecipe extends Impl<IRecipe> implements IRecipe {

	public NotebookRecipe() {
		setRegistryName(AunisItems.NOTEBOOK_ITEM.getRegistryName());
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		
		int matchCount = 0;
		
		for (int i=0; i<inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			Item item = stack.getItem();
			
			if ((item == AunisItems.PAGE_NOTEBOOK_ITEM && stack.getMetadata() == 1) || item == AunisItems.NOTEBOOK_ITEM)
				matchCount++;
		}
		
		return matchCount >= 2;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		int outputCount = 0;
		NBTTagList tagList = new NBTTagList();
		
		for (int i=0; i<inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack.hasTagCompound()) {
				NBTTagCompound compound = stack.getTagCompound();

				if (stack.getItem() == AunisItems.NOTEBOOK_ITEM && compound.hasKey("addressList", NBT.TAG_COMPOUND)) {
					NBTTagList notebookTags = compound.getTagList("addressList", NBT.TAG_COMPOUND);

					for (NBTBase tag : notebookTags) {
						if (!tagListContains(tagList, (NBTTagCompound) tag)) {
							tagList.appendTag(tag);
						}
					}

					outputCount++;
				} else if (stack.getItem() == AunisItems.PAGE_NOTEBOOK_ITEM) {
					// TODO change NBT to capability + filter NBT values
					if (!tagListContains(tagList, compound)) {
						tagList.appendTag(compound);
					}
				}
			}
		}
		
		if (outputCount == 0)
			outputCount = 1;
		
		ItemStack output = new ItemStack(AunisItems.NOTEBOOK_ITEM, outputCount);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("addressList", tagList);
		compound.setInteger("selected", 0);
		output.setTagCompound(compound);
		
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

	public static boolean tagListContains(NBTTagList tagList, NBTTagCompound compound) {
		if (compound == null)
			return false;
		
		for (NBTBase tag : tagList) {
			if (tag.equals(compound)) {
				return true;
			}
		}
		
		return false;
	}
}
