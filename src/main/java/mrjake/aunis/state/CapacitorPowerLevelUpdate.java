package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class CapacitorPowerLevelUpdate extends State {
	public CapacitorPowerLevelUpdate() {}
	
	public int powerLevel;
	
	public CapacitorPowerLevelUpdate(int powerLevel) {
		this.powerLevel = powerLevel;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(powerLevel);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		powerLevel = buf.readInt();
	}

}
