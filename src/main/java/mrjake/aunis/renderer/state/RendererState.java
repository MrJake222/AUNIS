package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Base class for all states(ex. RendererStates or GuiStates)
 * 
 * Defines methods to write NBT tags from abstract byte methods(used by TileUpdatePacketToClient)
 *
 */
public abstract class RendererState {
	
	public RendererState() {}
	
	/**
	 * Should write all parameters that matter to client-side renderer(ex. vortexState in StargateRenderer)
	 * to a ByteBuf.
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
	public abstract RendererState fromBytes(ByteBuf buf);
	
	/**
	 * Key name under which the parameters will be stored in NBT as ByteBuf
	 * 
	 * @return key name
	 */
	protected String getKeyName() {
		return "rendererState";
	}
	
	/**
	 * Writes ByteBuf with parameters to NBT with getKeyName() key
	 * 
	 * @param compound - NBT compound
	 */
	public void toNBT(NBTTagCompound compound) {
		ByteBuf buf = Unpooled.buffer();
		toBytes(buf);
		
		byte[] dst = new byte[buf.readableBytes()];
		buf.readBytes(dst);
		
		compound.setByteArray(getKeyName(), dst);
	}
	
	/**
	 * Constructor that reads bytes from NBT with getKeyName() key
	 * 
	 * @param compound - NBT compound
	 */
	public void fromNBT(NBTTagCompound compound) {
		byte[] dst = compound.getByteArray(getKeyName());
		
		if (dst != null && dst.length > 0) {
			ByteBuf buf = Unpooled.copiedBuffer(dst);
			fromBytes(buf);
		}
	}
}
