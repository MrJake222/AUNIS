package mrjake.aunis.state;

import io.netty.buffer.ByteBuf;

public class StargateAbstractGuiState extends State {
	public StargateAbstractGuiState() {}
	
	public int energy;	
	public int maxEnergy;
	public int transferedLastTick;
	public float secondsToClose;	
	
	public StargateAbstractGuiState(int energy, int maxEnergy, int transferedLastTick, float secondsToClose) {
		this.energy = energy;
		this.maxEnergy = maxEnergy;
		this.transferedLastTick = transferedLastTick;
		this.secondsToClose = secondsToClose;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(energy);
		buf.writeInt(maxEnergy);
		buf.writeInt(transferedLastTick);
		buf.writeFloat(secondsToClose);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		energy = buf.readInt();
		maxEnergy = buf.readInt();
		transferedLastTick = buf.readInt();
		secondsToClose = buf.readFloat();
	}
}