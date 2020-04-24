package mrjake.aunis.packet.transportrings;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.TransportRingsTile;
import mrjake.aunis.transportrings.ParamsSetResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
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
	
	
	public static class SaveRingsParametersServerHandler implements IMessageHandler<SaveRingsParametersToServer, IMessage> {

		@Override
		public StateUpdatePacketToClient onMessage(SaveRingsParametersToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			WorldServer world = player.getServerWorld();
			
			world.addScheduledTask(() -> {
				TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(message.pos);
				if (ringsTile.setRingsParams(message.address, message.name) == ParamsSetResult.DUPLICATE_ADDRESS)
					player.sendStatusMessage(new TextComponentTranslation("tile.aunis.transportrings_block.duplicate_address"), true);
			
				AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(message.pos, StateTypeEnum.GUI_STATE, ringsTile.getState(StateTypeEnum.GUI_STATE)), player);
			});
			
			return null;
		}
		
	}
}
