package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.BeamerTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BeamerChangedLevelsToServer extends PositionedPacket {
	public BeamerChangedLevelsToServer() {}
	
	public int start;
	public int stop;
	
	public BeamerChangedLevelsToServer(BlockPos pos, int start, int stop) {
		super(pos);
		this.start = start;
		this.stop = stop;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(start);
		buf.writeInt(stop);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		start = buf.readInt();
		stop = buf.readInt();
	}
	
	
	public static class BeamerChangedLevelsServerHandler implements IMessageHandler<BeamerChangedLevelsToServer, IMessage> {

		@Override
		public IMessage onMessage(BeamerChangedLevelsToServer message, MessageContext ctx) {
			WorldServer world = (WorldServer) ctx.getServerHandler().player.getEntityWorld();
			
			world.addScheduledTask(() -> {
				BeamerTile beamerTile = (BeamerTile) world.getTileEntity(message.pos);
				beamerTile.setStartStop(message.start, message.stop);
			});
			
			return null;
		}
		
	}
}
