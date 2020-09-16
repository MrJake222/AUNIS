package mrjake.aunis.proxy;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.stargate.StargateClassicMemberBlockColor;
import mrjake.aunis.event.InputHandlerClient;
import mrjake.aunis.fluid.AunisBlockFluid;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.color.PageMysteriousItemColor;
import mrjake.aunis.item.color.PageNotebookItemColor;
import mrjake.aunis.item.renderer.CustomModelItemInterface;
import mrjake.aunis.loader.ReloadListener;
import mrjake.aunis.renderer.BeamerRenderer;
import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.SpecialRenderer;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRenderer;
import mrjake.aunis.renderer.stargate.StargateOrlinRenderer;
import mrjake.aunis.renderer.stargate.StargateUniverseRenderer;
import mrjake.aunis.sound.AunisSoundHelperClient;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.tileentity.BeamerTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.TRControllerTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.aunis.tileentity.stargate.StargateUniverseBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
		
		InputHandlerClient.registerKeybindings();
	}

	public void init(FMLInitializationEvent event) {		
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageMysteriousItemColor(), AunisItems.PAGE_MYSTERIOUS_ITEM);
    	Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new PageNotebookItemColor(), AunisItems.PAGE_NOTEBOOK_ITEM);
    	
    	Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new StargateClassicMemberBlockColor(),
    			AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK,
    			AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK);
    }
 
    public void postInit(FMLPostInitializationEvent event) {
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ReloadListener());
    }

	public String localize(String unlocalized, Object... args) {
		return I18n.format(unlocalized, args);
	}
	
	private void registerRenderers() {
		OBJLoader.INSTANCE.addDomain("aunis");
		
		SpecialRenderer specialRenderer = new SpecialRenderer();
		
		ClientRegistry.bindTileEntitySpecialRenderer(StargateMilkyWayBaseTile.class, new StargateMilkyWayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(StargateUniverseBaseTile.class, new StargateUniverseRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(StargateOrlinBaseTile.class, new StargateOrlinRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(DHDTile.class, new DHDRenderer());
				
		ClientRegistry.bindTileEntitySpecialRenderer(TransportRingsTile.class, specialRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TRControllerTile.class, specialRenderer);
		
		ClientRegistry.bindTileEntitySpecialRenderer(BeamerTile.class, new BeamerRenderer());
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
		item.setTileEntityItemStackRenderer(((CustomModelItemInterface) item).createTEISR());
	}

	@Override
	public EntityPlayer getPlayerClientSide() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void addScheduledTaskClientSide(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}
	
	@Override
	public void orlinRendererSpawnParticles(World world, StargateAbstractRendererState rendererState) {
		StargateOrlinRenderer.spawnParticles(world, rendererState);
	}
	
	@Override
	public void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		AunisSoundHelperClient.playPositionedSoundClientSide(pos, soundEnum, play);
	}
	
	@Override
	public void openGui(GuiScreen gui) {
		Minecraft.getMinecraft().displayGuiScreen(gui);
	}
}
