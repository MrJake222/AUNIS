package mrjake.aunis.packet.gate.stateUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.packet.gate.tileUpdate.EnumTile;
import mrjake.aunis.renderer.DHDRendererState;
import mrjake.aunis.renderer.RendererState;
import mrjake.aunis.renderer.StargateRendererState;
import mrjake.aunis.tileentity.RenderedTileEntity;
import net.minecraft.block.Block;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StateUpdateToServer implements IMessage {
	public StateUpdateToServer() {}
	
	protected RendererState rendererState;
	
	public StateUpdateToServer(RendererState rendererState) {
		this.rendererState = rendererState;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt( EnumTile.fromObject(rendererState) );
		rendererState.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		EnumTile tile = EnumTile.fromInt(buf.readInt());
		
		if (tile == EnumTile.GATE_TILE)
			rendererState = new StargateRendererState(buf);
		else
			rendererState = new DHDRendererState(buf);
	}
	
	public static class StateUpdateServerHandler implements IMessageHandler<StateUpdateToServer, IMessage> {

		@Override
		public IMessage onMessage(StateUpdateToServer message, MessageContext ctx) {	
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			Block block = world.getBlockState(message.rendererState.pos).getBlock();
			
			// Security check
			if ( block instanceof StargateBaseBlock || block instanceof DHDBlock ) {
				world.addScheduledTask(() -> {
					
					RenderedTileEntity te = (RenderedTileEntity) world.getTileEntity(message.rendererState.pos);
					te.setRendererState(message.rendererState);
						
				});
			}
			
			return null;
		}
		
	}
}
