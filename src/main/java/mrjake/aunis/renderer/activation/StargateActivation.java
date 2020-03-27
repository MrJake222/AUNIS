package mrjake.aunis.renderer.activation;

import mrjake.aunis.renderer.stargate.ChevronEnum;

public class StargateActivation extends Activation<ChevronEnum> {

	public StargateActivation(ChevronEnum textureKey, long stateChange, boolean dim) {
		super(textureKey, stateChange, dim);
	}

	@Override
	protected int getMaxStage() {
		return 10;
	}
	
	@Override
	protected int getTickMultiplier() {
		return 3;
	}
}
