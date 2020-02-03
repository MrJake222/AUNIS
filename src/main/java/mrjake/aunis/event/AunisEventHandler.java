package mrjake.aunis.event;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.raycaster.RaycasterDHD;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
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
			
			cancelled |= block == AunisBlocks.dhdBlock;
			cancelled |= (block == AunisBlocks.stargateMilkyWayMemberBlock || block == AunisBlocks.stargateMilkyWayBaseBlock) && !blockState.getValue(AunisProps.RENDER_BLOCK);
			cancelled |= (block == AunisBlocks.stargateOrlinMemberBlock) && !blockState.getValue(AunisProps.RENDER_BLOCK);
//			cancelled |= block instanceof CrystalInfuserBlock;
			
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
		
		if (player.isSneaking()) {
			if (event instanceof RightClickBlock && world.getBlockState(event.getPos()).getBlock() == AunisBlocks.dhdBlock) {
				((RightClickBlock) event).setUseBlock(Result.ALLOW);
			}
		}
		
		else {
			EnumHand hand = event.getHand();
			ItemStack heldItemStack = player.getHeldItem(hand);
			
			if (world.isRemote && hand == EnumHand.MAIN_HAND && heldItemStack.getItem() != AunisItems.analyzerAncient) {
				BlockPos pos = player.getPosition();
							
				Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos.add(-1,-1,-1), pos.add(1,1,1));
				
				for (BlockPos activatedBlock : blocks) {
					Block block = world.getBlockState(activatedBlock).getBlock();

					if (block == AunisBlocks.dhdBlock) {
						RaycasterDHD.INSTANCE.onActivated(world, activatedBlock, player);
					}
				}
			}
		}
    }	
}
