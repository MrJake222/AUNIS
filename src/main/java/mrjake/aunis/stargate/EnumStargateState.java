package mrjake.aunis.stargate;

public enum EnumStargateState {
	IDLE(0),
	COMPUTER_DIALING(1),
	DHD_DIALING(2),
	ENGAGED(3),
	ENGAGED_INITIATING(4),
	UNSTABLE(5),
	FAILING(6);
	
	public int id;
	
	private EnumStargateState(int id) {
		this.id = id;
	}
	
	public boolean idle() {
		return this == IDLE;
	}
	
	public boolean engaged() {
		return this == ENGAGED || this == ENGAGED_INITIATING;
	}
	
	public boolean initiating() {
		return this == ENGAGED_INITIATING;
	}
	
	public boolean dialingDhd() {
		return this == DHD_DIALING;
	}
	
	public static EnumStargateState valueOf(int id) {
		return EnumStargateState.values()[id];
	}
}
