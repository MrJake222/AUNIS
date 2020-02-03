package mrjake.aunis.proxy;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.GrassBlockColor;
import mrjake.aunis.fluid.AunisBlockFluid;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.PageNotebookItem;
import mrjake.aunis.item.color.CrystalControlDHDItemColor;
import mrjake.aunis.item.color.PageMysteriousItemColor;
import mrjake.aunis.item.color.PageNotebookItemColor;
import mrjake.aunis.item.renderer.PageNotebookTEISR;
import mrjake.aunis.renderer.SpecialRenderer;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ProxyClient implements IProxy {
	public void preInit(FMLPreInitializationEvent event) {
		registerRenderers();
		
		registerFluidRenderers();
	}

	public void init(FMLInitializationEvent event) {
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new CrystalControlDHDItemColor(), AunisItems.crystalControlDhd);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageMysteriousItemColor(), AunisItems.pageMysteriousItem);
    	Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageNotebookItemColor(), AunisItems.pageNotebookItem);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new GrassBlockColor(), AunisBlocks.stargateMilkyWayMemberBlock);
    }
 
    public void postInit(FMLPostInitializationEvent event) {
 
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	private void registerRenderers() {
		OBJLoader.INSTANCE.addDomain("aunis");
		
		SpecialRenderer specialRenderer = new SpecialRenderer();
		
		ClientRegistry.bindTileEntitySpecialRenderer(StargateMilkyWayBaseTile.class, specialRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(StargateOrlinBaseTile.class, specialRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(DHDTile.class, specialRenderer);
		
		ClientRegistry.bindTileEntitySpecialRenderer(CrystalInfuserTile.class, specialRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TransportRingsTile.class, specialRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TRControllerTile.class, specialRenderer);
	}
	

    private void registerFluidRenderers() {
		for (AunisBlockFluid blockFluid : AunisFluids.blockFluidMap.values()) {
			ModelLoader.setCustomStateMapper(blockFluid, new StateMap.Builder().ignore(AunisBlockFluid.LEVEL).build());
		}
	}

	@Override
	public EntityPlayer getPlayerInMessageHandler(MessageContext ctx) {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void setTileEntityItemStackRenderer(Item item) {
		if (item.getRegistryName().equals(new ResourceLocation(Aunis.ModID, PageNotebookItem.ITEM_NAME)))
			item.setTileEntityItemStackRenderer(new PageNotebookTEISR());
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
