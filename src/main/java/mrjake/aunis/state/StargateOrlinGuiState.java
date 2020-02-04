package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class StargateOrlinGuiState extends State {
	public StargateOrlinGuiState() {}
	
	public int energy;	
	public int maxEnergy;	
	
	public StargateOrlinGuiState(int energy, int maxEnergy) {
		this.energy = energy;
		this.maxEnergy = maxEnergy;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energy);
		buf.writeInt(maxEnergy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energy = buf.readInt();
		maxEnergy = buf.readInt();
	}
}
