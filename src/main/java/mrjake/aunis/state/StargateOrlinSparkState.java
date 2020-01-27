package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class StargateOrlinSparkState extends State {
	public StargateOrlinSparkState() {}
	
	public int sparkIndex;
	public long spartStart;
	
	public StargateOrlinSparkState(int sparkIndex, long spartStart) {
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
