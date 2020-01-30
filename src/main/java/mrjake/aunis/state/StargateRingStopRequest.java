package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class StargateRingStopRequest extends State {
	public StargateRingStopRequest() {}
	
	public long worldTicks;
	public boolean moveOnly;

	public StargateRingStopRequest(long worldTicks, boolean moveOnly) {
		this.worldTicks = worldTicks;
		this.moveOnly = moveOnly;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(worldTicks);
		buf.writeBoolean(moveOnly);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		worldTicks = buf.readLong();
		moveOnly = buf.readBoolean();
	}

}
