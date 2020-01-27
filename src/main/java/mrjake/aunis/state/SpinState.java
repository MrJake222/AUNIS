package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSpinDirection;

public class SpinState extends State {
	
	/**
	 * Stores world's time when rotation started.
	 */
	public long tickStart = 0;
	
	/**
	 * Stores starting ring pos to correctly shift the next rotation
	 * 
	 * Set by requestStart()
	 */
	public double startingRotation = 0;

	/**
	 * Defines when ring is spinning ie. no stop animation was performed.
	 */
	public boolean isSpinning = false;
	
	/**
	 * Set by requestStop(). Set when ring needs to be stopped.
	 */
	public boolean stopRequested = false;
	
	/**
	 * Time when requestStop() was called
	 */
	public long tickStopRequested = 0;
	
	/**
	 * Spin direction
	 */
	public EnumSpinDirection direction = EnumSpinDirection.COUNTER_CLOCKWISE;
	

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeDouble(startingRotation);
		
		buf.writeLong(tickStart);
		buf.writeBoolean(isSpinning);
		buf.writeBoolean(stopRequested);
		buf.writeLong(tickStopRequested);
		buf.writeInt(direction.id);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		startingRotation = buf.readDouble();
		
		tickStart = buf.readLong();
		isSpinning = buf.readBoolean();
		stopRequested = buf.readBoolean();
		tickStopRequested = buf.readLong();
		direction = EnumSpinDirection.valueOf(buf.readInt());
	}
}
