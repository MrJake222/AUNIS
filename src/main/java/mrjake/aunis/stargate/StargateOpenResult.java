package mrjake.aunis.stargate;

public enum StargateOpenResult {
	OK,
	NOT_ENOUGH_POWER,
	ADDRESS_MALFORMED;
	
	public boolean ok() {
		return this == OK;
	}
}
