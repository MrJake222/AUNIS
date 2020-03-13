package mrjake.aunis.renderer.stargate;

import mrjake.aunis.stargate.EnumStargateState;

public class StargateOrlinRendererState extends StargateAbstractRendererState {
	public StargateOrlinRendererState() {}
	
	public StargateOrlinRendererState(EnumStargateState stargateState) {
		super(stargateState);
	}
	
	// Sparks
	// Not saved
	public long sparkStart;
	public int sparkIndex;
	
	public void sparkFrom(int chevronIndex, long worldTime) {		
		sparkIndex = chevronIndex;
		sparkStart = worldTime;
	}
}
