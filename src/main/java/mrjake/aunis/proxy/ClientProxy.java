package mrjake.aunis.proxy;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.color.GrassBlockColor;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.color.CrystalControlDHDItemColor;
import mrjake.aunis.tesr.CrystalInfuserTESR;
import mrjake.aunis.tesr.DHD_TESR;
import mrjake.aunis.tesr.StargateTESR;
import mrjake.aunis.tesr.TRControllerTESR;
import mrjake.aunis.tesr.TransportRingsTESR;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {
		registerRenderers();
	}
 
    public void init(FMLInitializationEvent event) {
    	Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new CrystalControlDHDItemColor(), AunisItems.crystalControlDhd);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new GrassBlockColor(), AunisBlocks.stargateMemberBlock);
    }
 
    public void postInit(FMLPostInitializationEvent event) {
 
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	private void registerRenderers() {
		OBJLoader.INSTANCE.addDomain("aunis");
		
		ClientRegistry.bindTileEntitySpecialRenderer(StargateBaseTile.class, new StargateTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(DHDTile.class, new DHD_TESR());
		
		ClientRegistry.bindTileEntitySpecialRenderer(CrystalInfuserTile.class, new CrystalInfuserTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TransportRingsTile.class, new TransportRingsTESR());
		ClientRegistry.bindTileEntitySpecialRenderer(TRControllerTile.class, new TRControllerTESR());
	}
}
