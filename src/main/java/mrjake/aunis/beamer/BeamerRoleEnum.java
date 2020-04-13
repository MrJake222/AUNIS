package mrjake.aunis.beamer;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum BeamerRoleEnum implements EnumKeyInterface<Integer> {
	TRANSMIT(0, "gui.beamer.transmit"),
	RECEIVE(1, "gui.beamer.receive"),
	DISABLED(2, "gui.beamer.disabled");
	
	public int id;
	public String translationKey;
	
	private BeamerRoleEnum(int id, String translationKey) {
		this.id = id;
		this.translationKey = translationKey;
	}
	
	@Override
	public Integer getKey() {
		return id;
	}

	public BeamerRoleEnum next() {
		switch (this) {
			case TRANSMIT: return RECEIVE;
			case RECEIVE: return DISABLED;
			case DISABLED: return TRANSMIT;
			default: return null;
		}
	}
	
	private static final EnumKeyMap<Integer, BeamerRoleEnum> KEY_MAP = new EnumKeyMap<>(values());
	
	public static BeamerRoleEnum valueOf(int id) {
		return KEY_MAP.valueOf(id);
	}
}
