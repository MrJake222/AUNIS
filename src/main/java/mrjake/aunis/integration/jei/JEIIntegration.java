package mrjake.aunis.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mrjake.aunis.block.AunisBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

@JEIPlugin
public final class JEIIntegration implements IModPlugin {

    public JEIIntegration(){}

    @Override
    public void register(IModRegistry registry) {
        // Hide invisible block in JEI
        registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(AunisBlocks.INVISIBLE_BLOCK, 1, OreDictionary.WILDCARD_VALUE));
        
        // Tab handling
        registry.addAdvancedGuiHandlers(new JEIAdvancedGuiHandler());
        
        List<IRecipeWrapper> recipes = new ArrayList<>();
        recipes.addAll(JEINotebookRecipe.genAll());
        recipes.add(new JEIUniverseDialerCloneRecipe());
		registry.addRecipes(recipes, VanillaRecipeCategoryUid.CRAFTING);
    }

//    @Override
//    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
//        // Show mysterious pages with all possible generators
//        subtypeRegistry.registerSubtypeInterpreter(AunisItems.PAGE_MYSTERIOUS_ITEM,
//                stack -> stack.hasTagCompound() && stack.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING) ?
//                        stack.getTagCompound().getString("generator") :
//                        ISubtypeRegistry.ISubtypeInterpreter.NONE);
//    }
}
