package mrjake.aunis.renderer.state;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.state.State;

public class SparkState extends State {
	public SparkState() {}
	
	public int sparkIndex;
	public long spartStart;
	
	public SparkState(int sparkIndex, long spartStart) {
		this.sparkIndex = sparkIndex;
		this.spartStart = spartStart;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(sparkIndex);
		buf.writeLong(spartStart);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		sparkIndex = buf.readInt();
		spartStart = buf.readLong();
	}

}
