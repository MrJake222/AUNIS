package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class CrystalInfuserRendererState extends RendererState {
	
	public int energyStored;
	public boolean renderWaves;

	public CrystalInfuserRendererState() {
		this(-1, false);
	}
	
	public CrystalInfuserRendererState(int energyStored, boolean renderWaves) {
		this.energyStored = energyStored;
		this.renderWaves = renderWaves;
	}
	
	public CrystalInfuserRendererState(ByteBuf buf) {
		super(buf);
	}
	
	public CrystalInfuserRendererState(NBTTagCompound compound) {
		super(compound);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeBoolean(renderWaves);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.energyStored = buf.readInt();
		this.renderWaves = buf.readBoolean();
	}

	@Override
	protected String getKeyName() {
		return "rendererState";
	}
}
