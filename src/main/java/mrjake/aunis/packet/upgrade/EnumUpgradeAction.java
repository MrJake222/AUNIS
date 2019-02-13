package mrjake.aunis.packet.upgrade;

public enum EnumUpgradeAction {
	PUT_UPGRADE(0),
	INSERT_UPGRADE(1),
	REMOVE_UPGRADE(2),
	POP_UPGRADE(3);
	
	public int id;
	
	private EnumUpgradeAction(int id) {
		this.id = id;
	}
}
