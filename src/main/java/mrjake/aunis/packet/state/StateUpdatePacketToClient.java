package mrjake.aunis.packet.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.State;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.NotImplementedError;

public class StateUpdatePacketToClient extends PositionedPacket {
	public StateUpdatePacketToClient() {}
		
	
	private EnumStateType stateType;
	private State state;
	
	public StateUpdatePacketToClient(BlockPos pos, EnumStateType stateType, State state) {
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

		stateType = EnumStateType.byId(buf.readInt());
		
		ITileEntityStateProvider te = (ITileEntityStateProvider) Minecraft.getMinecraft().world.getTileEntity(pos);
		state = te.createState(stateType);
		
		if (state != null)
			state.fromBytes(buf);
		
		else
			throw new NotImplementedError("State not implemented on " + te.toString());
	}
	
	public static class StateUpdateClientHandler implements IMessageHandler<StateUpdatePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(StateUpdatePacketToClient message, MessageContext ctx) {			
			EntityPlayer player = Aunis.proxy.getPlayerInMessageHandler(ctx);
			World world = player.getEntityWorld();
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
								
				ITileEntityStateProvider te = (ITileEntityStateProvider) world.getTileEntity(message.pos);
				
				te.setState(message.stateType, message.state);				
			});
			
			return null;
		}
		
	}
}
