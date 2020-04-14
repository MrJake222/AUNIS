package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.BeamerTile;
import mrjake.aunis.tileentity.util.RedstoneModeEnum;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChangeRedstoneModeToServer extends PositionedPacket {
	public ChangeRedstoneModeToServer() {}
	
	public RedstoneModeEnum mode;
	
	public ChangeRedstoneModeToServer(BlockPos pos, RedstoneModeEnum mode) {
		super(pos);
		this.mode = mode;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(mode.getKey());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		mode = RedstoneModeEnum.valueOf(buf.readInt());
	}
	
	
	public static class ChangeRedstoneModeServerHandler implements IMessageHandler<ChangeRedstoneModeToServer, IMessage> {

		@Override
		public IMessage onMessage(ChangeRedstoneModeToServer message, MessageContext ctx) {
			WorldServer world = (WorldServer) ctx.getServerHandler().player.getEntityWorld();
			
			world.addScheduledTask(() -> {
				BeamerTile beamerTile = (BeamerTile) world.getTileEntity(message.pos);
				beamerTile.setRedstoneMode(message.mode);
			});
			
			return null;
		}
		
	}
}
