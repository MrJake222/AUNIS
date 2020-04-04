package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.State;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.NotImplementedError;

public class StateUpdatePacketToClient extends PositionedPacket {
	public StateUpdatePacketToClient() {}
	
	private StateTypeEnum stateType;
	private State state;
	
	private ByteBuf stateBuf;
	
	public StateUpdatePacketToClient(BlockPos pos, StateTypeEnum stateType, State state) {
		super(pos);
		
		this.stateType = stateType;
		this.state = state;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {		
		super.toBytes(buf);
		
		buf.writeInt(stateType.id);
		
		state.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		stateType = StateTypeEnum.byId(buf.readInt());
		stateBuf = buf.copy();
	}
	
	public static class StateUpdateClientHandler implements IMessageHandler<StateUpdatePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(StateUpdatePacketToClient message, MessageContext ctx) {			
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
								
				StateProviderInterface te = (StateProviderInterface) world.getTileEntity(message.pos);
				
				try {
					if (te == null)
						return;
					
					State state = te.createState(message.stateType);
					
					if (state != null) {
						state.fromBytes(message.stateBuf);
						
						if (te != null)
							te.setState(message.stateType, state);
					}
					
					else {
						throw new NotImplementedError("State not implemented on " + te.toString());
					}
				}
				
				catch (UnsupportedOperationException e) {
					e.printStackTrace();
				}
			});
			
			return null;
		}
		
	}
}
