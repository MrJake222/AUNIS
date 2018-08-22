package mrjake.aunis.packet.gate.stateUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.renderer.RendererState;
import mrjake.aunis.renderer.StargateRendererState;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class StateUpdateToServer implements IMessage {
	public StateUpdateToServer() {}
	
	private RendererState rendererState;
	
	public StateUpdateToServer(RendererState rendererState) {
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
	
	
	public static class StateUpdateServerHandler implements IMessageHandler<StateUpdateToServer, IMessage> {

		@Override
		public IMessage onMessage(StateUpdateToServer message, MessageContext ctx) {	
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			
			world.addScheduledTask(() -> {
				
				TileEntity te = null;
				
				if ( world.getBlockState(message.rendererState.pos).getBlock() instanceof StargateBaseBlock )
				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.rendererState.pos); 
				gateTile.setRendererState(message.rendererState);
					
			});
			
			return null;
		}
		
	}
}
