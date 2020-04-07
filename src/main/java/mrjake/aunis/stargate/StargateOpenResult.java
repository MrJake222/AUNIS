package mrjake.aunis.stargate;

public enum StargateOpenResult {
	OK,
	NOT_ENOUGH_POWER,
	ADDRESS_MALFORMED,
	ABORTED;
	
	public boolean ok() {
		return this == OK;
	}
}
