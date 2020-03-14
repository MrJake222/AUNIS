package mrjake.aunis.tileentity.util;

import java.util.HashMap;
import java.util.Map;

public class EnumIdMap<T extends EnumIdInterface> {
	
	private Map<Integer, T> idMap = new HashMap<>();
	
	public EnumIdMap(T[] values) {
		for (T value : values) {
			idMap.put(value.getId(), value);
		}
	}
	
	public T valueOf(int id) {
		return idMap.get(id);
	}
}
