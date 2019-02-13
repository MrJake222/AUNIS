package mrjake.aunis.event;

import mrjake.aunis.block.BlockTESRMember;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.block.StargateMemberBlock;
import mrjake.aunis.dhd.DHDActivation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class AunisEventHandler {
	
	@SubscribeEvent
	public static void onDrawHighlight(DrawBlockHighlightEvent event) {		
		RayTraceResult target = event.getTarget();
		
		if ( target.typeOfHit == RayTraceResult.Type.BLOCK ) {
			IBlockState blockState = event.getPlayer().world.getBlockState( target.getBlockPos() );
			Block block = blockState.getBlock();
			
			boolean cancelled = false;
			
			cancelled |= block instanceof DHDBlock;
			cancelled |= (block instanceof StargateMemberBlock || block instanceof StargateBaseBlock) && !blockState.getValue(BlockTESRMember.RENDER);
			
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
