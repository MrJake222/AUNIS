package mrjake.aunis.renderer.activation;

import mrjake.aunis.stargate.EnumSymbol;

public class DHDActivation extends Activation {

	public DHDActivation(int textureIndex, long stateChange, boolean dim) {
		super(textureIndex, stateChange, dim);
	}
	
	public DHDActivation(int textureIndex, long stateChange) {
		super(textureIndex, stateChange);
	}

	@Override
	protected int getMaxStage() {
		return 5;
	}
	
	@Override
	protected int getTickMultiplier() {
		return (textureIndex == EnumSymbol.BRB.id && !dim) ? 1 : 2;
	}
}
