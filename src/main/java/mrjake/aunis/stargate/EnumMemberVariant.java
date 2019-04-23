package mrjake.aunis.stargate;

import java.util.HashMap;
import java.util.Map;

public enum EnumMemberVariant {
	RING(0, "ring"),
	CHEVRON(1, "chevron");
	
	public int id;
	String name;
	
	private EnumMemberVariant(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	private static Map<String, EnumMemberVariant> nameMap = new HashMap<>();
	
	static {
		for (EnumMemberVariant variant : EnumMemberVariant.values()) {
			nameMap.put(variant.toString(), variant);
		}
	}
	
	public static EnumMemberVariant byName(String name) {
		return nameMap.get(name);
	}
	
	public static EnumMemberVariant byId(int id) {
		return EnumMemberVariant.values()[id];
	}
}
