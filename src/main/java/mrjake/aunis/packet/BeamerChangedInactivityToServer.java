package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.BeamerTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BeamerChangedInactivityToServer extends PositionedPacket {
	public BeamerChangedInactivityToServer() {}
	
	public int inactivity;
	
	public BeamerChangedInactivityToServer(BlockPos pos, int inactivity) {
		super(pos);
		this.inactivity = inactivity;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(inactivity);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		inactivity = buf.readInt();
	}
	
	
	public static class BeamerChangedInactivityServerHandler implements IMessageHandler<BeamerChangedInactivityToServer, IMessage> {

		@Override
		public IMessage onMessage(BeamerChangedInactivityToServer message, MessageContext ctx) {
			WorldServer world = (WorldServer) ctx.getServerHandler().player.getEntityWorld();
			
			world.addScheduledTask(() -> {
				BeamerTile beamerTile = (BeamerTile) world.getTileEntity(message.pos);
				beamerTile.setInactivity(message.inactivity);
			});
			
			return null;
		}
		
	}
}
