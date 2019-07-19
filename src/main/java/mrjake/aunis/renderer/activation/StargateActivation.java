package mrjake.aunis.renderer.activation;

public class StargateActivation extends Activation {

	public StargateActivation(int textureIndex, long stateChange, boolean dim) {
		super(textureIndex, stateChange, dim);
	}
	
	public StargateActivation(int textureIndex, long stateChange) {
		super(textureIndex, stateChange);
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
