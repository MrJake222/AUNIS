package mrjake.aunis.proxy;

import mrjake.aunis.tesr.DHD_TESR;
import mrjake.aunis.tesr.StargateTESR;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {

	}
 
    public void init(FMLInitializationEvent event) {
 
    }
 
    public void postInit(FMLPostInitializationEvent event) {
 
    }
    
    public void registerItemRenderer(Item item, int meta, ResourceLocation registryName) {
    	ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(registryName, "inventory"));
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	@Override
	public void registerRenderers() {
		OBJLoader.INSTANCE.addDomain("aunis");
		
		ClientRegistry.bindTileEntitySpecialRenderer(StargateBaseTile.class, new StargateTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(DHDTile.class, new DHD_TESR());
	}
}
