package mrjake.aunis.sound;

public enum EnumAunisPositionedSound {
	RING_ROLL_START(0),
	RING_ROLL_LOOP(1), 
	WORMHOLE(2);
	
	public int id;
	
	EnumAunisPositionedSound(int id) {
		this.id = id;
	}
	
	public static EnumAunisPositionedSound valueOf(int id) {
		return EnumAunisPositionedSound.values()[id];
	}
}
