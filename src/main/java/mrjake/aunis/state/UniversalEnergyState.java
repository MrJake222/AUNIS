package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class UniversalEnergyState extends State {
	public UniversalEnergyState() {}

	public int energy;
	
	public UniversalEnergyState(int energy) {
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
