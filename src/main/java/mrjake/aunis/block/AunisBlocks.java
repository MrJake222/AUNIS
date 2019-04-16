package mrjake.aunis.block;

import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber
public class AunisBlocks {
	public static NaquadahOreBlock naquadahOreBlock = new NaquadahOreBlock();
	
	public static StargateBaseBlock stargateBaseBlock = new StargateBaseBlock();
	public static StargateMemberBlock ringBlock = new StargateMemberBlock("stargate_ring_block");
	public static StargateMemberBlock chevronBlock = new StargateMemberBlock("stargate_chevron_block");
	
	public static DHDBlock dhdBlock = new DHDBlock();
	public static CrystalInfuserBlock crystalInfuserBlock = new CrystalInfuserBlock();
	
	public static TransportRingsBlock transportRingsBlock = new TransportRingsBlock();
	public static InvisibleBlock invisibleBlock = new InvisibleBlock();
	
	private static Block[] blocks = {
		naquadahOreBlock,
		
		stargateBaseBlock,
		ringBlock,
		chevronBlock,
		
		dhdBlock,
		crystalInfuserBlock,
		
		transportRingsBlock,
		invisibleBlock
	};
	
	@SubscribeEvent
	public static void onRegisterBlocks(Register<Block> event) {
		event.getRegistry().registerAll(blocks);
		
		GameRegistry.registerTileEntity(StargateBaseTile.class, AunisBlocks.stargateBaseBlock.getRegistryName());
		GameRegistry.registerTileEntity(DHDTile.class, AunisBlocks.dhdBlock.getRegistryName());
		GameRegistry.registerTileEntity(CrystalInfuserTile.class, AunisBlocks.crystalInfuserBlock.getRegistryName());
		GameRegistry.registerTileEntity(TransportRingsTile.class, AunisBlocks.transportRingsBlock.getRegistryName());
	}
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {	
		for (Block block : blocks) {
			event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		}
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
		for (Block block : blocks) {
			ModelLoader.setCustomModelResourceLocation(ItemBlock.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
	}
}

