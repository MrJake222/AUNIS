package mrjake.aunis.proxy;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("deprecation")
public class ServerProxy implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {

	}
 
    public void init(FMLInitializationEvent event) {
 
    }
 
    public void postInit(FMLPostInitializationEvent event) {
 
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.translateToLocalFormatted(unlocalized, args);
	}
}
