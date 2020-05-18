package mrjake.aunis.event;

import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.raycaster.RaycasterDHD;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class AunisEventHandler {

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {	
		onRightClick(event);
	}
	
	@SubscribeEvent
	public static void onRightClickItem(RightClickItem event) {	
		onRightClick(event);
	}
	
	@SubscribeEvent
	public static void onRightClickEmpty(RightClickEmpty event) {	
		onRightClick(event);
	}
	
	private static void onRightClick(PlayerInteractEvent event) {		
		EntityPlayer player = event.getEntityPlayer();
		World world = player.getEntityWorld();
		
		if (!player.isSneaking() && !player.isSpectator()) {
			BlockPos pos = player.getPosition();
			EnumFacing playerFacing = EnumFacing.getDirectionFromEntityLiving(pos, player).getOpposite();
			
			if (playerFacing != EnumFacing.UP && playerFacing != EnumFacing.DOWN) { 
				EnumFacing left = playerFacing.rotateYCCW();
				EnumFacing right = playerFacing.rotateY();
								
				Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos.offset(left).down(), pos.offset(right, 2).up().offset(playerFacing));
				
				for (BlockPos activatedBlock : blocks) {
					Block block = world.getBlockState(activatedBlock).getBlock();
	
					/*
					 * This only activates the DHD block, on both sides and
					 * cancels the event. A packet is sent to the server by onActivated
					 * only on main hand click.
					 */
					if (block == AunisBlocks.DHD_BLOCK && RaycasterDHD.INSTANCE.onActivated(world, activatedBlock, player, event.getHand())) {
						
						if (event.isCancelable()) {
							event.setCanceled(true);
						}
					}
				}
			}
			
			else
				Aunis.logger.warn("Facing down when activating DHD");
		}
    }

	@SubscribeEvent
	public static void onLootTableLoad(LootTableLoadEvent event) {
	    if (event.getName().toString().equals("minecraft:chests/end_city_treasure")) {
	    	LootEntry entry = new LootEntryTable(new ResourceLocation(Aunis.ModID, "end_city_treasure"), 1, 0, new LootCondition[] {}, "universe_dialer");  
	    	LootPool pool = new LootPool(new LootEntry[] {entry}, new LootCondition[] {}, new RandomValueRange(1), new RandomValueRange(0), "univese_dialer_pool");
	    	
	    	event.getTable().addPool(pool);
	    }
	}
}
