package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class CrystalInfuserRendererState extends State {
	
	public int energyStored;
	public boolean renderWaves;
	public boolean doCrystalRender;

	public CrystalInfuserRendererState() {
		this(0, false, false);
	}
	
	public CrystalInfuserRendererState(int energyStored, boolean renderWaves, boolean doCrystalRender) {
		this.energyStored = energyStored;
		this.renderWaves = renderWaves;
		this.doCrystalRender = doCrystalRender;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energyStored);
		buf.writeBoolean(renderWaves);
		buf.writeBoolean(doCrystalRender);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.energyStored = buf.readInt();
		this.renderWaves = buf.readBoolean();
		this.doCrystalRender = buf.readBoolean();
	}
}
