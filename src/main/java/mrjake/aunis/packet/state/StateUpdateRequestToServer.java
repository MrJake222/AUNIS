package mrjake.aunis.packet.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.State;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.NotImplementedError;

public class StateUpdateRequestToServer extends PositionedPacket {
	public StateUpdateRequestToServer() {}	
	
	
	EnumStateType stateType;
	
	public StateUpdateRequestToServer(BlockPos pos, EnumStateType stateType) {
		super(pos);
		
		this.stateType = stateType;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(stateType.id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		stateType = EnumStateType.byId(buf.readInt());
	}
	
	
	public static class StateUpdateServerHandler implements IMessageHandler<StateUpdateRequestToServer, StateUpdatePacketToClient> {

		@Override
		public StateUpdatePacketToClient onMessage(StateUpdateRequestToServer message, MessageContext ctx) {
						
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				ITileEntityStateProvider te = (ITileEntityStateProvider) world.getTileEntity(message.pos);
				
				if (te != null) {
					State state = te.getState(message.stateType);
					
					if (state != null)
						return new StateUpdatePacketToClient(message.pos, message.stateType, state);
					
					else
						throw new NotImplementedError("State not implemented on " + te.toString());
				}
				
			}
			
			return null;
		}
		
	}
}