package mrjake.aunis.tileentity.util;

public enum ReactorStateEnum implements EnumIdInterface {
	ONLINE(0),
	NOT_LINKED(1),
	NO_FUEL(2),
	STANDBY(3);
	
	private int id;

	private ReactorStateEnum(int id) {
		this.id = id;
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	private static EnumIdMap<ReactorStateEnum> enumIdMap = new EnumIdMap<ReactorStateEnum>(values());
	
	public static ReactorStateEnum valueOf(int id) {
		return enumIdMap.valueOf(id);
	}
}
