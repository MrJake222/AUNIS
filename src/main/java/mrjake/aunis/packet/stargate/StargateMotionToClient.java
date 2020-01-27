package mrjake.aunis.packet.stargate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StargateMotionToClient implements IMessage {
	public StargateMotionToClient() {}

	private BlockPos gatePos;
	
	public StargateMotionToClient(BlockPos gatePos) {
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
	
	
	public static class RetrieveMotionClientHandler implements IMessageHandler<StargateMotionToClient, IMessage> {

		@Override
		public IMessage onMessage(StargateMotionToClient message, MessageContext ctx) {
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				EntityPlayer player = Aunis.proxy.getPlayerClientSide();
								
				AunisPacketHandler.INSTANCE.sendToServer( new StargateMotionToServer(player.getEntityId(), message.gatePos, (float)player.motionX, (float)player.motionZ) );
			});
			
			return null;
		}
		
	}

	
	
}
