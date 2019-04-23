package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class LightState extends State {
	public LightState() {}
	
	
	private boolean isLitUp;
	
	public LightState(boolean isLitUp) {
		this.isLitUp = isLitUp;
	}
	
	public boolean isLitUp() {
		return isLitUp;
	}
	
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(isLitUp);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		isLitUp = buf.readBoolean();
	}
}
