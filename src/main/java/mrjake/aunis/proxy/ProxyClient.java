package mrjake.aunis.proxy;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.color.GrassBlockColor;
import mrjake.aunis.fluid.AunisBlockFluid;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.color.CrystalControlDHDItemColor;
import mrjake.aunis.item.color.PageMysteriousItemColor;
import mrjake.aunis.item.color.PageNotebookItemColor;
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
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyClient implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {
		registerRenderers();
		
		registerFluidRenderers();
	}

	public void init(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new CrystalControlDHDItemColor(), AunisItems.crystalControlDhd);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageMysteriousItemColor(), AunisItems.pageMysteriousItem);
    	Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageNotebookItemColor(), AunisItems.pageNotebookItem);
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
	

    private void registerFluidRenderers() {
		for (AunisBlockFluid blockFluid : AunisFluids.blockFluidMap.values()) {
			ModelLoader.setCustomStateMapper(blockFluid, new StateMap.Builder().ignore(AunisBlockFluid.LEVEL).build());
		}
	}

	@Override
	public EntityPlayer getPlayerClientSide() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void addScheduledTaskClientSide(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}
}
