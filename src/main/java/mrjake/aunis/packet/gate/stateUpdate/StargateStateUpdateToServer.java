package mrjake.aunis.packet.gate.stateUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.renderer.RendererState;
import mrjake.aunis.renderer.StargateRendererState;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StargateStateUpdateToServer implements IMessage {
	public StargateStateUpdateToServer() {}
	
	StargateRendererState rendererState;
	
	public StargateStateUpdateToServer(StargateRendererState rendererState) {
		this.rendererState = rendererState;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		rendererState.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		rendererState = new StargateRendererState(buf);
	}
	
	
	public static class StagateStateServerHandler implements IMessageHandler<StargateStateUpdateToServer, IMessage> {

		@Override
		public IMessage onMessage(StargateStateUpdateToServer message, MessageContext ctx) {	
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if ( world.getBlockState(message.rendererState.pos).getBlock() instanceof StargateBaseBlock ) {
				world.addScheduledTask(() -> {
					
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.rendererState.pos); 
					gateTile.setRendererState(message.rendererState);
					
				});
			}
			
			return null;
		}
		
	}
}
