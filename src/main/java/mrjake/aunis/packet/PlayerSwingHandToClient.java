package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerSwingHandToClient implements IMessage {
	public PlayerSwingHandToClient() {}
	
	public EnumHand hand;
	
	public PlayerSwingHandToClient(EnumHand hand) {
		this.hand = hand;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hand == EnumHand.MAIN_HAND ? 0 : 1);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readInt() == 0 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}

	
	public static class PlayerSwingHandClientHandler implements IMessageHandler<PlayerSwingHandToClient, IMessage> {

		@Override
		public IMessage onMessage(PlayerSwingHandToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				player.swingArm(message.hand);
			});
			
			return null;
		}
		
	}
}
