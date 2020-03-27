package mrjake.aunis.renderer.activation;

import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;

public class DHDActivation extends Activation<SymbolMilkyWayEnum> {

	public DHDActivation(SymbolMilkyWayEnum textureKey, long stateChange, boolean dim) {
		super(textureKey, stateChange, dim);
	}

	@Override
	protected int getMaxStage() {
		return 5;
	}
	
	@Override
	protected int getTickMultiplier() {
		return (textureKey == SymbolMilkyWayEnum.BRB && !dim) ? 1 : 2;
	}
}
