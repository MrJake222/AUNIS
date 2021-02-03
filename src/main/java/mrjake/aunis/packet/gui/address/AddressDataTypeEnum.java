package mrjake.aunis.packet.gui.address;

public enum AddressDataTypeEnum {
	PAGE,
	UNIVERSE;
	
	boolean page() {
		return this == PAGE;
	}

	boolean universe() {
		return this == UNIVERSE;
	}
}
