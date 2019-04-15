package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;

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

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeBoolean(renderWaves);
	}

	@Override
	public RendererState fromBytes(ByteBuf buf) {
		this.energyStored = buf.readInt();
		this.renderWaves = buf.readBoolean();
		
		return this;
	}
}
