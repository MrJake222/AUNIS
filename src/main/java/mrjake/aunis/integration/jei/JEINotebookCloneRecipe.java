package mrjake.aunis.integration.jei;

import java.util.Arrays;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class JEINotebookCloneRecipe implements IRecipeWrapper {
	
	private static ItemStack output;
	
	static {
		output = JEINotebookRecipe.PAGE1.copy();
		output.setCount(2);
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(JEINotebookRecipe.PAGE1, new ItemStack(Items.PAPER)));
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}
}
