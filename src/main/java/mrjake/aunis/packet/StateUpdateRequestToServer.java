package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.NotImplementedError;

public class StateUpdateRequestToServer extends PositionedPacket {
	public StateUpdateRequestToServer() {}	
	
	StateTypeEnum stateType;
	
	public StateUpdateRequestToServer(BlockPos pos, StateTypeEnum stateType) {
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
		
		stateType = StateTypeEnum.byId(buf.readInt());
	}
	
	
	public static class StateUpdateServerHandler implements IMessageHandler<StateUpdateRequestToServer, IMessage> {

		@Override
		public StateUpdatePacketToClient onMessage(StateUpdateRequestToServer message, MessageContext ctx) {	
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				
				world.addScheduledTask(() -> {
					StateProviderInterface te = (StateProviderInterface) world.getTileEntity(message.pos);
				
					if (te != null) {
						try {
							State state = te.getState(message.stateType);
						
							if (state != null)
								AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(message.pos, message.stateType, state), player);							
							else
								throw new NotImplementedError("State not implemented on " + te.toString());
						}
						
						catch (UnsupportedOperationException e) {
							e.printStackTrace();
						}
					}
				});
			}
			
			return null;
		}
		
	}
}
