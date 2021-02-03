package mrjake.aunis.packet.gui.address;

public enum AddressActionEnum {
	RENAME(false),
	MOVE_UP(true),
	MOVE_DOWN(true),
	REMOVE(true);
	
	public boolean shouldRefresh;

	private AddressActionEnum(boolean shouldRefresh) {
		this.shouldRefresh = shouldRefresh;
	}
}
