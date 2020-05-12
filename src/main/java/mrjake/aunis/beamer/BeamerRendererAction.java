package mrjake.aunis.beamer;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum BeamerRendererAction implements EnumKeyInterface<Integer> {
	BEAM_ON(0),
	BEAM_OFF(1);
	
	public int id;

	BeamerRendererAction(int id) {
		this.id = id;
	}
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	private static final EnumKeyMap<Integer, BeamerRendererAction> KEY_MAP = new EnumKeyMap<>(values());
	
	public static BeamerRendererAction valueOf(int id) {
		return KEY_MAP.valueOf(id);
	}
}
