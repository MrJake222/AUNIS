package mrjake.aunis.event;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class AunisEventHandlerClient {
	
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
}
