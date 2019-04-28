package mrjake.aunis.packet.gate.teleportPlayer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RetrieveMotionToClient implements IMessage {
	public RetrieveMotionToClient() {}

	private BlockPos gatePos;
	
	public RetrieveMotionToClient(BlockPos gatePos) {
		this.gatePos = gatePos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( gatePos.toLong() );
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		gatePos = BlockPos.fromLong( buf.readLong() );
	}
	
	
	public static class RetrieveMotionClientHandler implements IMessageHandler<RetrieveMotionToClient, IMessage> {

		@Override
		public IMessage onMessage(RetrieveMotionToClient message, MessageContext ctx) {
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = Aunis.proxy.getPlayerInMessageHandler(ctx);
								
				AunisPacketHandler.INSTANCE.sendToServer( new MotionToServer(player.getEntityId(), message.gatePos, (float)player.motionX, (float)player.motionZ) );
			});
			
			return null;
		}
		
	}

	
	
}
