package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Base class for all renderer states.
 * 
 * Defines methods to write NBT tags from abstract byte methods(used by TileUpdatePacketToClient)
 *
 */
public abstract class RendererState {
	
	public BlockPos pos;
	
	public RendererState(BlockPos pos) {
		this.pos = pos;
	}
	
	public RendererState(ByteBuf buf) {
		fromBytes(buf);
	}
	
	/**
	 * Should return all parameters that matter to client-side renderer(ex. vortexState in StargateRenderer)
	 * in form of a ByteBuf.
	 * 
	 * Data should be put and read in the same order!
	 * 
	 * @param buf - Buffer object you write into.
	 */
	public abstract void toBytes(ByteBuf buf);
	
	/**
	 * Should set all parameters that matter to client-side renderer(ex. vortexState in StargateRenderer)
	 * 
	 * @param buf - Buffer object you read from.
	 */
	public abstract void fromBytes(ByteBuf buf);
	
	protected abstract String getKeyName();
	
	public void toNBT(NBTTagCompound compound) {
		ByteBuf buf = Unpooled.buffer();
		toBytes(buf);
		
		byte[] dst = new byte[buf.readableBytes()];
		buf.readBytes(dst);
		
		compound.setByteArray(getKeyName(), dst);
	}
	
	public RendererState(NBTTagCompound compound) {
		byte[] dst = compound.getByteArray(getKeyName());
		
		if (dst != null && dst.length > 0) {
			ByteBuf buf = Unpooled.copiedBuffer(dst);
			fromBytes(buf);
		}
	}
}
