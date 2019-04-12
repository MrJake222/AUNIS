package mrjake.aunis.event.registry;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.IBlockBase;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.ItemBase;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber
public class RegistryHandler {
	
	@SubscribeEvent
	public static void onRegisterBlocks(Register<Block> event) {
		event.getRegistry().registerAll(AunisBlocks.blocks);
		
		GameRegistry.registerTileEntity(StargateBaseTile.class, AunisBlocks.stargateBaseBlock.getRegistryName());
		GameRegistry.registerTileEntity(DHDTile.class, AunisBlocks.dhdBlock.getRegistryName());
		GameRegistry.registerTileEntity(CrystalInfuserTile.class, AunisBlocks.crystalInfuserBlock.getRegistryName());
	}
	
	@SubscribeEvent
	public static void onRegisterItems(Register<Item> event) {		
		event.getRegistry().registerAll(AunisItems.items);
		event.getRegistry().registerAll(AunisBlocks.getItems());
	}
	
	@SubscribeEvent
	public static void onModelRegistry(ModelRegistryEvent event) {
		for (Block block : AunisBlocks.blocks)
			((IBlockBase) block).registerItemRenderer();
		
		
		for (Item item : AunisItems.items)
			((ItemBase) item).registerItemRenderer();
	}
}