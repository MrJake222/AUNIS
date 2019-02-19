package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PositionedPacket implements IMessage {
	public PositionedPacket() {}
	
	protected BlockPos pos;
		
	public PositionedPacket(BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
	}
}
