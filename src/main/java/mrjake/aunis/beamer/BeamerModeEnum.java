package mrjake.aunis.beamer;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum BeamerModeEnum implements EnumKeyInterface<Integer> {
	POWER(0, 0.68f, 0.25f, 0.26f),
	FLUID(1, 0.25f, 0.49f, 0.68f),
	ITEMS(2, 0.23f, 0.62f, 0.29f),
	NONE(3, null);
	
	public int id;
	public float[] colors;
	
	private BeamerModeEnum(int id, float... colors) {
		this.id = id;
		this.colors = colors;
	}
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	private static final EnumKeyMap<Integer, BeamerModeEnum> KEY_MAP = new EnumKeyMap<>(values());
	
	public static BeamerModeEnum valueOf(int id) {
		return KEY_MAP.valueOf(id);
	}
}
