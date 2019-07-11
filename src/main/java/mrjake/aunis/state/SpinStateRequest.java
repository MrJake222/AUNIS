package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSpinDirection;

public class SpinStateRequest extends State {
	public SpinStateRequest() {}
	
	public EnumSpinDirection direction = EnumSpinDirection.COUNTER_CLOCKWISE;
	public double targetAngle = -1;
	public boolean lock = false;
	public boolean moveOnly;
	
	public SpinStateRequest(EnumSpinDirection direction, double stopAngle, boolean lock, boolean moveOnly) {
		this.direction = direction;
		this.targetAngle = stopAngle;
		this.lock = lock;
		this.moveOnly = moveOnly;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(direction.id);
		buf.writeDouble(targetAngle);
		buf.writeBoolean(lock);
		buf.writeBoolean(moveOnly);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		direction = EnumSpinDirection.valueOf(buf.readInt());
		targetAngle = buf.readDouble();
		lock = buf.readBoolean();
		moveOnly = buf.readBoolean();
	}

}
