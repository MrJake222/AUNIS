package mrjake.aunis.gui.container;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.StargateAbstractGuiState;

public class StargateContainerGuiState extends StargateAbstractGuiState {
	public StargateContainerGuiState() {}
	
	public Map<SymbolTypeEnum, StargateAddress> gateAdddressMap;
	public boolean hasUpgrade;
	
	public StargateContainerGuiState(Map<SymbolTypeEnum, StargateAddress> gateAdddressMap, boolean hasUpgrade, int energy, int maxEnergy, int transferedLastTick, float secondsToClose) {
		super(energy, maxEnergy, transferedLastTick, secondsToClose);
		
		this.gateAdddressMap = gateAdddressMap;
		this.hasUpgrade = hasUpgrade;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			gateAdddressMap.get(symbolType).toBytes(buf);
		}
		
		buf.writeInt(maxEnergy);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		Mouse.setGrabbed(false);
		gateAdddressMap = new HashMap<>(3);
		
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			StargateAddress address = new StargateAddress(symbolType);
			address.fromBytes(buf);
			gateAdddressMap.put(symbolType, address);
		}
		
		maxEnergy = buf.readInt();
	}
}
