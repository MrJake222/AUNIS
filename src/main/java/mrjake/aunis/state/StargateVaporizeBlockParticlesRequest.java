package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;

public class StargateVaporizeBlockParticlesRequest extends State {
	public StargateVaporizeBlockParticlesRequest() {}
	
	public BlockPos block;
	
	public StargateVaporizeBlockParticlesRequest(BlockPos block) {
		this.block = block;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(block.toLong());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		block = BlockPos.fromLong(buf.readLong());
	}
}
