package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class UniversalEnergyState extends State {
	public UniversalEnergyState() {}

	public int energy;
	public int transferedLastTick;
	
	public UniversalEnergyState(int energy, int transferedLastTick) {
		this.energy = energy;
		this.transferedLastTick = transferedLastTick;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energy);
		buf.writeInt(transferedLastTick);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energy = buf.readInt();
		transferedLastTick = buf.readInt();
	}
}
