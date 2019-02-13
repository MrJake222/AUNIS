package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class LimitedStargateRendererState extends RendererState {
	
	// Ring
	public float ringAngularRotation;
	
	@Override
	public String toString() {
		return pos+":  ringAngularRotation: "+ringAngularRotation;
	}
	
	// Default state
	public LimitedStargateRendererState(BlockPos pos) {
		this(pos, 0);
	}
	
	public LimitedStargateRendererState(BlockPos pos, float ringAngularRotation) {
		super(pos);
		
		this.ringAngularRotation = ringAngularRotation;
	}
	
	public LimitedStargateRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public LimitedStargateRendererState(NBTTagCompound compound) {
		super(compound);
	}
	
	@Override
	protected String getKeyName() {
		return "rendererState";
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
		
		buf.writeFloat(ringAngularRotation);
	}
	
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong( buf.readLong() );

		ringAngularRotation = buf.readFloat();
	}
}