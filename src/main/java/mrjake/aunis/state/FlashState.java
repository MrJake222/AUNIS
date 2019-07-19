package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class FlashState extends State {
	public FlashState() {}
	
	public boolean flash;
	
	public FlashState(boolean flash) {
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
