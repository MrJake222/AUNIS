package mrjake.aunis.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.item.AunisItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;
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
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        // Show mysterious pages with all possible generators
        subtypeRegistry.registerSubtypeInterpreter(AunisItems.PAGE_MYSTERIOUS_ITEM,
                stack -> stack.hasTagCompound() && stack.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING) ?
                        stack.getTagCompound().getString("generator") :
                        ISubtypeRegistry.ISubtypeInterpreter.NONE);
    }
}
