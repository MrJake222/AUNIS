package mrjake.aunis.packet.gui.entry;

public enum EntryDataTypeEnum {
	PAGE,
	UNIVERSE,
	OC;
	
	boolean page() {
		return this == PAGE;
	}
	
	boolean universe() {
		return this == UNIVERSE;
	}

	boolean oc() {
		return this == OC;
	}
}
