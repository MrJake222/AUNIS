package mrjake.aunis.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mrjake.aunis.item.AunisItems;
import net.minecraftforge.common.util.Constants;

@JEIPlugin
public final class JEIIntegration implements IModPlugin {

    public JEIIntegration(){}

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(AunisItems.PAGE_MYSTERIOUS_ITEM,
                stack -> stack.hasTagCompound() && stack.getTagCompound().hasKey("generator", Constants.NBT.TAG_STRING) ?
                        stack.getTagCompound().getString("generator") :
                        ISubtypeRegistry.ISubtypeInterpreter.NONE);
    }
}
