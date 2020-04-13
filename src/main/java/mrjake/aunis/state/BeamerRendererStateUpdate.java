package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerStatusEnum;

public class BeamerRendererStateUpdate extends State {
	public BeamerRendererStateUpdate() {}
	
	public BeamerModeEnum beamerMode;
	public BeamerStatusEnum beamerStatus;
	public boolean isObstructed;
	public int beamLength;

	public BeamerRendererStateUpdate(BeamerModeEnum beamerMode, BeamerStatusEnum beamerStatus, boolean isObstructed, int beamLength) {
		this.beamerMode = beamerMode;
		this.beamerStatus = beamerStatus;
		this.isObstructed = isObstructed;
		this.beamLength = beamLength;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerMode.getKey());
		buf.writeInt(beamerStatus.getKey());
		buf.writeBoolean(isObstructed);
		buf.writeInt(beamLength);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerMode = BeamerModeEnum.valueOf(buf.readInt());
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
		isObstructed = buf.readBoolean();
		beamLength = buf.readInt();
	}

}
