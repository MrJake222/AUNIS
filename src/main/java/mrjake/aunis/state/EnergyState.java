package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class EnergyState extends State {
	public EnergyState() {}

	public int energy;
	
	public EnergyState(int energy) {
		this.energy = energy;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energy = buf.readInt();
	}
}
