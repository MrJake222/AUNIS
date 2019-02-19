package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CrystalInfuserRendererState extends RendererState {
	
	public CrystalInfuserRendererState() {
		
	}
	
	public CrystalInfuserRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public CrystalInfuserRendererState(NBTTagCompound compound) {
		super(compound);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getKeyName() {
		return "rendererState";
	}

}
