package mrjake.aunis.sound;

public enum EnumAunisSoundEvent {
	DHD_PRESS(0),
	DHD_PRESS_BRB(1),
	GATE_OPEN(2),
	GATE_CLOSE(3),
	GATE_DIAL_FAILED(4),
	
	CHEVRON_LOCK_DHD(5),
	CHEVRON_INCOMING(6),
	WORMHOLE_GO(7),
	
	WORMHOLE_FLICKER(8),
	RINGS_TRANSPORT(9),
	RINGS_CONTROLLER_BUTTON(10),
	
	CHEVRON_SHUT(11),
	CHEVRON_OPEN(12),
	CHEVRON_LOCKING(13),
	GATE_ORLIN_DIAL(14);
	
	public int id;
	
	EnumAunisSoundEvent(int id) {
		this.id = id;
	}
	
	public static EnumAunisSoundEvent valueOf(int id) {
		return EnumAunisSoundEvent.values()[id];
	}
}
