package mrjake.aunis.gui.container;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.StargateAbstractGuiState;

public class StargateContainerGuiState extends StargateAbstractGuiState {
	public StargateContainerGuiState() {}
	
	private List<EnumSymbol> gateAddress;
	public List<EnumSymbol> getGateAddress() { return gateAddress; }
	
	private boolean hasUpgrade;
	public boolean hasUpgrade() { return hasUpgrade; }
	
	public StargateContainerGuiState(List<EnumSymbol> gateAddress, boolean hasUpgrade, int energy, int maxEnergy, int transferedLastTick, float secondsToClose) {
		super(energy, maxEnergy, transferedLastTick, secondsToClose);
		
		this.gateAddress = gateAddress;
		this.hasUpgrade = hasUpgrade;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeLong(EnumSymbol.toLong(gateAddress));
		
		buf.writeBoolean(hasUpgrade);
		buf.writeInt(gateAddress.get(6).id);
		
		buf.writeInt(maxEnergy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		gateAddress = new ArrayList<EnumSymbol>();
		
		for (int id : EnumSymbol.fromLong(buf.readLong())) {
			gateAddress.add(EnumSymbol.valueOf(id));
		}
		
		gateAddress.add(EnumSymbol.valueOf(buf.readInt()));
		
		maxEnergy = buf.readInt();
	}
}
