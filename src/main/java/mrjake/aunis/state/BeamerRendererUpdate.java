package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.beamer.BeamerStatusEnum;

public class BeamerRendererUpdate extends State {
	public BeamerRendererUpdate() {}
	
	public BeamerStatusEnum beamerStatus;


	public BeamerRendererUpdate(BeamerStatusEnum beamerStatus) {
		this.beamerStatus = beamerStatus;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(beamerStatus.getKey());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		beamerStatus = BeamerStatusEnum.valueOf(buf.readInt());
	}

}
