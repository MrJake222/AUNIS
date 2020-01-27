package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class TransportRingsStartAnimationRequest extends State {
	public TransportRingsStartAnimationRequest() {}
	
	public long animationStart;
	
	public TransportRingsStartAnimationRequest(long animationStart) {
		this.animationStart = animationStart;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(animationStart);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		animationStart = buf.readLong();
	}

}
