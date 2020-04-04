package mrjake.aunis.event;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.raycaster.RaycasterDHD;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class AunisEventHandler {
	
	@SubscribeEvent
    public static void onConfigChangedEvent(OnConfigChangedEvent event) {		
        if (event.getModID().equals(Aunis.ModID)) {
            ConfigManager.sync(Aunis.ModID, Type.INSTANCE);
        }
	}
	
	@SubscribeEvent
	public static void onDrawHighlight(DrawBlockHighlightEvent event) {		
		RayTraceResult target = event.getTarget();
		
		if ( target.typeOfHit == RayTraceResult.Type.BLOCK ) {
			IBlockState blockState = event.getPlayer().world.getBlockState( target.getBlockPos() );
			Block block = blockState.getBlock();
			
			boolean cancelled = false;
			
			cancelled |= block == AunisBlocks.DHD_BLOCK;
			cancelled |= (block == (Block) AunisBlocks.STARGATE_MILKY_WAY_MEMBER_BLOCK || block == AunisBlocks.STARGATE_MILKY_WAY_BASE_BLOCK) && !blockState.getValue(AunisProps.RENDER_BLOCK);
			cancelled |= (block == (Block) AunisBlocks.STARGATE_UNIVERSE_MEMBER_BLOCK || block == AunisBlocks.STARGATE_UNIVERSE_BASE_BLOCK) && !blockState.getValue(AunisProps.RENDER_BLOCK);
			cancelled |= (block == AunisBlocks.STARGATE_ORLIN_MEMBER_BLOCK) && !blockState.getValue(AunisProps.RENDER_BLOCK);
			
			event.setCanceled(cancelled);
		}
    }
	
	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {	
		onRightClick(event);
	}
	
	@SubscribeEvent
	public static void onRightClickEmpty(RightClickEmpty event) {	
		onRightClick(event);
	}
	
	public static void onRightClick(PlayerInteractEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = player.getEntityWorld();
		
		if (!player.isSneaking()) {
			BlockPos pos = player.getPosition();
			EnumFacing playerFacing = EnumFacing.getDirectionFromEntityLiving(pos, player).getOpposite();
			
			if (playerFacing != EnumFacing.UP && playerFacing != EnumFacing.DOWN) { 
				EnumFacing left = playerFacing.rotateYCCW();
				EnumFacing right = playerFacing.rotateY();
				
//				Aunis.info(playerFacing + ": left="+left+", right="+right);
				
				Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos.offset(left).down(), pos.offset(right, 2).up().offset(playerFacing));
				
				for (BlockPos activatedBlock : blocks) {
					Block block = world.getBlockState(activatedBlock).getBlock();
	
					/*
					 * This only activates the DHD block, on both sides and
					 * cancels the event. A packet is sent to the server by onActivated
					 * only on main hand click.
					 */
					if (block == AunisBlocks.DHD_BLOCK && RaycasterDHD.INSTANCE.onActivated(world, activatedBlock, player, event.getHand())) {
						event.setCanceled(true);
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
