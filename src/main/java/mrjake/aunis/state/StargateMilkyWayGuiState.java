package mrjake.aunis.state;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;

public class StargateMilkyWayGuiState extends StargateAbstractGuiState {
	public StargateMilkyWayGuiState() {}
	
	private List<EnumSymbol> gateAddress;
	public List<EnumSymbol> getGateAddress() { return gateAddress; }
	
	private boolean hasUpgrade;
	public boolean hasUpgrade() { return hasUpgrade; }
	
	public StargateMilkyWayGuiState(List<EnumSymbol> gateAddress, boolean hasUpgrade, int energy, int maxEnergy, int transferedLastTick, float secondsToClose) {
		super(energy, maxEnergy, transferedLastTick, secondsToClose);
		
		this.gateAddress = gateAddress;
		this.hasUpgrade = hasUpgrade;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeLong(EnumSymbol.toLong(gateAddress));
		
		buf.writeBoolean(hasUpgrade);
		if (hasUpgrade)
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
		
		hasUpgrade = buf.readBoolean();
		
		if (hasUpgrade)
			gateAddress.add(EnumSymbol.valueOf(buf.readInt()));
		
		maxEnergy = buf.readInt();
	}
}
