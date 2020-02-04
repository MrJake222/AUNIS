package mrjake.aunis.state;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.EnumSymbol;

public class StargateMilkyWayGuiState extends State {
	public StargateMilkyWayGuiState() {}
	
	private List<EnumSymbol> gateAddress;
	public List<EnumSymbol> getGateAddress() { return gateAddress; }
	
	private boolean hasUpgrade;
	public boolean hasUpgrade() { return hasUpgrade; }
	
	public int maxEnergy;	
	public UniversalEnergyState energyState;
	
	public StargateMilkyWayGuiState(List<EnumSymbol> gateAddress, boolean hasUpgrade, int maxEnergy, UniversalEnergyState energyState) {
		this.gateAddress = gateAddress;
		this.hasUpgrade = hasUpgrade;
		
		this.maxEnergy =  maxEnergy;
		this.energyState = energyState;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(EnumSymbol.toLong(gateAddress));
		
		buf.writeBoolean(hasUpgrade);
		if (hasUpgrade)
			buf.writeInt(gateAddress.get(6).id);
		
		buf.writeInt(maxEnergy);
		
		energyState.toBytes(buf);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		gateAddress = new ArrayList<EnumSymbol>();
		
		for (int id : EnumSymbol.fromLong(buf.readLong())) {
			gateAddress.add(EnumSymbol.valueOf(id));
		}
		
		hasUpgrade = buf.readBoolean();
		
		if (hasUpgrade)
			gateAddress.add(EnumSymbol.valueOf(buf.readInt()));
		
		maxEnergy = buf.readInt();
		
		energyState = new UniversalEnergyState();
		energyState.fromBytes(buf);
	}
}
