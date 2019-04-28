package mrjake.aunis.packet.transportrings;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SaveRingsParametersToServer extends PositionedPacket {
	public SaveRingsParametersToServer() {}
	
	int address;
	String name;
	
	public SaveRingsParametersToServer(BlockPos pos, int address, String name) {
		super(pos);
		
		this.address = address;
		this.name = name;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(address);
		buf.writeInt(name.length());
		buf.writeCharSequence(name, StandardCharsets.UTF_8);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		address = buf.readInt();
		int len = buf.readInt();
		name = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
	}
	
	
	public static class SaveRingsParametersServerHandler implements IMessageHandler<SaveRingsParametersToServer, StateUpdatePacketToClient> {

		@Override
		public StateUpdatePacketToClient onMessage(SaveRingsParametersToServer message, MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().player;
			World world = player.getEntityWorld();
			
			TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(message.pos);
			ringsTile.setRingsParams(player, message.address, message.name);
			
			return new StateUpdatePacketToClient(message.pos, EnumStateType.GUI_STATE, ringsTile.getState(EnumStateType.GUI_STATE));
		}
		
	}
}
