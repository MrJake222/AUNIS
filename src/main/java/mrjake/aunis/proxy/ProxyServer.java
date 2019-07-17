package mrjake.aunis.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("deprecation")
public class ProxyServer implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {

	}
 
    public void init(FMLInitializationEvent event) {
 
    }
 
    public void postInit(FMLPostInitializationEvent event) {
 
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.translateToLocalFormatted(unlocalized, args);
	}

	@Override
	public EntityPlayer getPlayerClientSide() {
		return null;
	}

	@Override
	public void addScheduledTaskClientSide(Runnable runnable) {}
}
