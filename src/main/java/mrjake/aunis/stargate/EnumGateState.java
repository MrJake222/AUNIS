package mrjake.aunis.stargate;

public enum EnumGateState {
	OK,
	NOT_ENOUGH_POWER,
	ADDRESS_MALFORMED;
	
	public boolean ok() {
		return this == OK;
	}
}
