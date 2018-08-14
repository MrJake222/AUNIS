package mrjake.aunis.event.registry;

import mrjake.aunis.init.AunisBlocks;
import mrjake.aunis.init.AunisItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@EventBusSubscriber
public class RegistryHandler {
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
    public static void registerBlocks(Register<Block> event) {
    	final Block[] blocks = {
    			AunisBlocks.stargateBaseBlock,
    			AunisBlocks.DHDBlock,
    	};
    	
    	event.getRegistry().registerAll(blocks);
    	GameRegistry.registerTileEntity(AunisBlocks.stargateBaseBlock.getTileEntityClass(), AunisBlocks.stargateBaseBlock.getRegistryName().toString());
    	GameRegistry.registerTileEntity(AunisBlocks.DHDBlock.getTileEntityClass(), AunisBlocks.DHDBlock.getRegistryName().toString());
    	
    }
	
	
    @SubscribeEvent
    public static void registerItems(Register<Item> event) {
        final Item[] items = {
        		AunisItems.naquadahItem
        };
        
        final Item[] ItemBlocks = {
        		AunisBlocks.stargateBaseBlock.getItemBlock(),
        		AunisBlocks.DHDBlock.getItemBlock()
        };
        
        event.getRegistry().registerAll(items);
        event.getRegistry().registerAll(ItemBlocks);
    }
}