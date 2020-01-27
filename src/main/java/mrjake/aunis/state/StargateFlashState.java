package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class StargateFlashState extends State {
	public StargateFlashState() {}
	
	public boolean flash;
	
	public StargateFlashState(boolean flash) {
		this.flash = flash;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(flash);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		flash = buf.readBoolean();
	}

}
