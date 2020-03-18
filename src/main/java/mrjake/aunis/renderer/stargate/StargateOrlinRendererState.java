package mrjake.aunis.renderer.stargate;

public class StargateOrlinRendererState extends StargateAbstractRendererState {
	public StargateOrlinRendererState() {}
	
	public StargateOrlinRendererState(StargateAbstractRendererStateBuilder builder) {
		super(builder);
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
