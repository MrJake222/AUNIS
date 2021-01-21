package mrjake.aunis.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
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
    }
}
