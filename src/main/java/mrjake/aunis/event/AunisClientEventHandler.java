package mrjake.aunis.event;

import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.dhd.DHDActivation;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
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
	
	/*@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		EntityPlayer player = event.player;
		BlockPos pos = player.getPosition();
		World world = player.getEntityWorld();
		
		Vec3i range = new Vec3i(32, 32, 32);
		BlockPos from = pos.subtract(range);
		BlockPos to = pos.add(range);
		
		AxisAlignedBB box = new AxisAlignedBB( from, to );
		
		List<Entity> entities = world.getEntitiesInAABBexcluding(player, box, null);
		EntityPlayer targetPlayer = null;
		
		for (Entity entity : entities) {
			if (entity instanceof EntityPlayer) {
				targetPlayer = (EntityPlayer) entity;
				break;
			}
		}
		
		if (targetPlayer != null) {
			Iterable<BlockPos> blocks = BlockPos.getAllInBox(from, to);
			List<BlockPos> stargatesToUpdate = new ArrayList<BlockPos>();
			List<BlockPos> dhdsToUpdate = new ArrayList<BlockPos>();
			
			for (BlockPos blockPos : blocks) {				
				Block block = world.getBlockState(blockPos).getBlock();
				
				if ( block instanceof StargateBaseBlock )
					stargatesToUpdate.add(blockPos);
				else if ( block instanceof DHDBlock )
					dhdsToUpdate.add(blockPos);
			}
						
			AunisPacketHandler.INSTANCE.sendTo(new OnLoadUpdateRequest(player.getEntityId(), true, stargatesToUpdate, dhdsToUpdate), (EntityPlayerMP) targetPlayer);			
		}
	}*/
	
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
