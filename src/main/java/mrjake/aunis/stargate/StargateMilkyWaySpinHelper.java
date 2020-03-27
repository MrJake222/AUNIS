package mrjake.aunis.stargate;

import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;

public class StargateMilkyWaySpinHelper extends StargateClassicSpinHelper<SymbolMilkyWayEnum> {
	public StargateMilkyWaySpinHelper() {}
	
	public StargateMilkyWaySpinHelper(SymbolMilkyWayEnum currentRingSymbol, EnumSpinDirection spinDirection, boolean isSpinning, SymbolMilkyWayEnum targetRingSymbol, long spinStartTime) {
		super(currentRingSymbol, spinDirection, isSpinning, targetRingSymbol, spinStartTime);
	}

	@Override
	protected SymbolMilkyWayEnum valueOf(int id) {
		return SymbolMilkyWayEnum.valueOf(id);
	}

}
