package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.gui.container.StargateContainer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetOpenTabToServer implements IMessage {
	public SetOpenTabToServer() {}
	
	public int openTabId;
	
	public SetOpenTabToServer(int openTabId) {
		this.openTabId = openTabId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(openTabId);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		openTabId = buf.readInt();
	}

	
	public static class SetOpenTabServerHandler implements IMessageHandler<SetOpenTabToServer, IMessage> {

		@Override
		public IMessage onMessage(SetOpenTabToServer message, MessageContext ctx) {
			Container container = ctx.getServerHandler().player.openContainer;
			
			if (container instanceof StargateContainer) {
				((StargateContainer) container).openTabId = message.openTabId;
			}
			
			return null;
		}
		
	}
}
