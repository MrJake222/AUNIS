package mrjake.aunis.gui;

import java.util.HashMap;
import java.util.Map;

public enum GuiIdEnum {
	GUI_DHD(0),
	GUI_STARGATE(1),
	GUI_CAPACITOR(2),
	GUI_BEAMER(3);
	
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
