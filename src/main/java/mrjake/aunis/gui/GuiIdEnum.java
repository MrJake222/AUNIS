package mrjake.aunis.gui;

import java.util.HashMap;
import java.util.Map;

public enum GuiIdEnum {
	DHD_GUI(0);
	
	public int id;
	
	GuiIdEnum(int id) {
		this.id = id;
	}
	
	private static Map<Integer, GuiIdEnum> idMap = new HashMap<>();
	static {
		for (GuiIdEnum guidEnum : values())
			idMap.put(guidEnum.id, guidEnum);
	}
	
	public static GuiIdEnum valueOf(int id) {
		return idMap.get(id);
	}
}
