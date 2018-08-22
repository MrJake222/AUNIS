package mrjake.aunis.renderer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class RendererState {
	
	public BlockPos pos;
	
	public RendererState(BlockPos pos) {
		this.pos = pos;
	}
	
	public RendererState(ByteBuf buf) {
		fromBytes(buf);
	}
	
	// Defined by inherent class
	public abstract void toBytes(ByteBuf buf);
	public abstract void fromBytes(ByteBuf buf);
	
	public void toNBT(NBTTagCompound compound) {
		ByteBuf buf = Unpooled.buffer();
		toBytes(buf);
		
		byte[] dst = new byte[buf.readableBytes()];
		buf.readBytes(dst);
		
		compound.setByteArray("dhdRendererState", dst);
	}
	
	public RendererState(NBTTagCompound compound) {
		byte[] dst = compound.getByteArray("dhdRendererState");
		
		if (dst != null && dst.length > 0) {
			ByteBuf buf = Unpooled.copiedBuffer(dst);
			fromBytes(buf);
		}
	}
}
