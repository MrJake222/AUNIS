package mrjake.aunis.integration.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class JEINotebookRecipe implements IRecipeWrapper {

	static ItemStack getRandomPageWithNameColor(String name, String biome) {
		ItemStack pageStack = new ItemStack(AunisItems.PAGE_NOTEBOOK_ITEM, 1, 1);
		NBTTagCompound compound = new NBTTagCompound();
		pageStack.setTagCompound(compound);

		compound.setInteger("color", PageNotebookItem.getColorForBiome(biome));
		PageNotebookItem.setName(compound, name);
		
		StargateAddress address = new StargateAddress(SymbolTypeEnum.MILKYWAY).generate(new Random());
		compound.setInteger("symbolType", address.getSymbolType().id);
		compound.setTag("address", address.serializeNBT());
		compound.setBoolean("hasUpgrade", false);
		
		return pageStack;
	}
	
	static ItemStack getNotebookWithPages(int quantity, ItemStack ...pages) {
		ItemStack notebook = new ItemStack(AunisItems.NOTEBOOK_ITEM, quantity);
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		
		compound.setTag("addressList", list);
		notebook.setTagCompound(compound);
		
		for (ItemStack stack : pages) {
			list.appendTag(stack.getTagCompound());
		}
		
		return notebook;
	}
	
	static final ItemStack PAGE1 = getRandomPageWithNameColor("Plains", "plains");
	static final ItemStack PAGE2 = getRandomPageWithNameColor("Tundra", "ice");
	
	static final ItemStack NOTEBOOK = getNotebookWithPages(1, PAGE1, PAGE2);
	static final ItemStack NOTEBOOK2 = getNotebookWithPages(2, PAGE1, PAGE2);
	
	private List<ItemStack> inputs = new ArrayList<>();
	private  List<ItemStack> outputs = new ArrayList<>();
	
	JEINotebookRecipe(NotebookRecipeVariantEnum variant) {
		switch (variant) {
			case TWO_PAGES:
				inputs.add(PAGE1);
				inputs.add(PAGE2);
				outputs.add(NOTEBOOK);
				break;
				
			case NOTEBOOK_PAGE:
				inputs.add(PAGE1);
				inputs.add(getNotebookWithPages(1, PAGE2));
				outputs.add(NOTEBOOK);
				break;
				
			case NOTEBOOK_BOOK:
				inputs.add(NOTEBOOK);
				inputs.add(new ItemStack(Items.BOOK));
				outputs.add(NOTEBOOK2);
				break;
				
			case TWO_NOTEBOOKS:
				inputs.add(getNotebookWithPages(1, PAGE1));
				inputs.add(getNotebookWithPages(1, PAGE2));
				outputs.add(NOTEBOOK2);
				break;
		}
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, inputs);
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	static enum NotebookRecipeVariantEnum {
		TWO_PAGES,
		NOTEBOOK_PAGE,
		NOTEBOOK_BOOK,
		TWO_NOTEBOOKS;
	}

	public static List<JEINotebookRecipe> genAll() {
		List<JEINotebookRecipe> list = new ArrayList<>();
		
		for (NotebookRecipeVariantEnum variant : NotebookRecipeVariantEnum.values()) {
			list.add(new JEINotebookRecipe(variant));
		}
		
		return list;
	}
}
