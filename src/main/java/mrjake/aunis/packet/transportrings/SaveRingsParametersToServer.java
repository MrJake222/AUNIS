package mrjake.aunis.packet.transportrings;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPlayerPacket;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SaveRingsParametersToServer extends PositionedPlayerPacket {
	public SaveRingsParametersToServer() {}
	
	int address;
	String name;
	
	public SaveRingsParametersToServer(BlockPos pos, EntityPlayer player, int address, String name) {
		super(pos, player);
		
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
	
	
	public static class SaveRingsParametersServerHandler implements IMessageHandler<SaveRingsParametersToServer, IMessage> {

		@Override
		public StateUpdatePacketToClient onMessage(SaveRingsParametersToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			
			world.addScheduledTask(() -> {
				TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(message.pos);
				ringsTile.setRingsParams(player, message.address, message.name);
			
				message.respond(world, new StateUpdatePacketToClient(message.pos, StateTypeEnum.GUI_STATE, ringsTile.getState(StateTypeEnum.GUI_STATE)));
			});
			
			return null;
		}
		
	}
}
