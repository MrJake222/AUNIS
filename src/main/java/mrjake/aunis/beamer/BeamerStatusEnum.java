package mrjake.aunis.beamer;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum BeamerStatusEnum implements EnumKeyInterface<Integer> {
	OBSTRUCTED(0),
	INCOMING(1),

	CLOSED(3),
	NO_BEAMER(4),
	MODE_MISMATCH(5),
	OK(6),
	OBSTRUCTED_TARGET(7),
	NOT_LINKED(8),
	NO_CRYSTAL(9),
	BEAMER_DISABLED(10),
	BEAMER_DISABLED_TARGET(11),
	TWO_TRANSMITTERS(12),
	TWO_RECEIVERS(13),
	BEAMER_DISABLED_BY_LOGIC(14),
	BEAMER_DISABLED_BY_LOGIC_TARGET(15);
	
	
	public int id;
	public float[] colors;
	
	private BeamerStatusEnum(int id, float... colors) {
		this.id = id;
		this.colors = colors;
	}
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	private static final EnumKeyMap<Integer, BeamerStatusEnum> KEY_MAP = new EnumKeyMap<>(values());
	
	public static BeamerStatusEnum valueOf(int id) {
		return KEY_MAP.valueOf(id);
	}
}
