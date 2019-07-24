package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Base class for all states(ex. RendererStates or GuiStates)
 * 
 * Defines methods to write NBT tags from abstract byte methods(used by TileUpdatePacketToClient)
 *
 */
public abstract class State implements INBTSerializable<NBTTagCompound> {	
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
	public abstract void fromBytes(ByteBuf buf);
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		ByteBuf buf = Unpooled.buffer();
		toBytes(buf);
		
		byte[] dst = new byte[buf.readableBytes()];
		buf.readBytes(dst);
		
		compound.setByteArray("byteArray", dst);
		
		return compound;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		if (compound == null)
			return;
		
		byte[] dst = compound.getByteArray("byteArray");
		
		if (dst != null && dst.length > 0) {
			ByteBuf buf = Unpooled.copiedBuffer(dst);
			fromBytes(buf);
		}
	}
}
