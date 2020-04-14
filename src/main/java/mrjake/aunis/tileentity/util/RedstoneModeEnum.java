package mrjake.aunis.tileentity.util;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum RedstoneModeEnum implements EnumKeyInterface<Integer> {
	AUTO(0, "gui.beamer.auto"),
	ON_HIGH(1, "gui.beamer.on_high"),
	ON_LOW(2, "gui.beamer.on_low"),
	IGNORED(3, "gui.beamer.ignored");
	
	public int id;
	public String translationKey;
	
	private RedstoneModeEnum(int id, String translationKey) {
		this.id = id;
		this.translationKey = translationKey;
	}
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	private static final EnumKeyMap<Integer, RedstoneModeEnum> KEY_MAP = new EnumKeyMap<>(values());
	
	public static RedstoneModeEnum valueOf(int id) {
		return KEY_MAP.valueOf(id);
	}
}
