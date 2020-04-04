package mrjake.aunis.util;

import java.util.HashMap;
import java.util.Map;

public class EnumKeyMap<K, T extends EnumKeyInterface<K>> {
	
	private Map<K, T> idMap = new HashMap<>();
	
	public EnumKeyMap(T[] values) {
		for (T value : values) {
			idMap.put(value.getKey(), value);
		}
	}
	
	public T valueOf(K key) {
		return idMap.get(key);
	}

	public boolean contains(K key) {
		return idMap.containsKey(key);
	}
}
