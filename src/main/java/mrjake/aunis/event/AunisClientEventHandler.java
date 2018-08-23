package mrjake.aunis.event;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.chunk.AunisChunkLoader;
import mrjake.aunis.dhd.DHDActivation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class AunisClientEventHandler {
	
	@SubscribeEvent
	public static void onDrawHighlight(DrawBlockHighlightEvent event) {		
		RayTraceResult target = event.getTarget();
		
		if ( target.typeOfHit == RayTraceResult.Type.BLOCK ) {
			
			if ( event.getPlayer().world.getBlockState( target.getBlockPos() ).getBlock() == AunisBlocks.DHDBlock ) {
				event.setCanceled(true);
			}
		}
    }
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		AunisChunkLoader.genTicketForWorld( event.getWorld() );
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		AunisChunkLoader.removeTicketForWorld( event.getWorld() );
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
		
		if ( world.isRemote && event.getHand() == EnumHand.MAIN_HAND) {
			BlockPos pos = player.getPosition();
						
			Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos.add(-1,-1,-1), pos.add(1,1,1));
			
			for (BlockPos activatedBlock : blocks) {
				if (world.getBlockState(activatedBlock).getBlock() instanceof DHDBlock) {
					DHDActivation.onActivated(world, activatedBlock, player);
				}
			}
		}
    }
}
