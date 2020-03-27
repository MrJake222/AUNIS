package mrjake.aunis.renderer.stargate;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;

public enum ChevronEnum implements EnumKeyInterface<Integer> {
	C1(0, 1),
	C2(1, 2),
	C3(2, 3),
	
	C4(3, 6),
	C5(4, 7),
	C6(5, 8),
	
	C7(6, 4),
	C8(7, 5),
	
	C9(8, 0);
	
	public int index;
	public int rotation;
			
	ChevronEnum(int index, int rotationIndex) {
		this.index = index;
		this.rotation = -40*rotationIndex;
	}
	
	public boolean isFinal() {
		return this == C9;
	}
	
	public static ChevronEnum getFinal() {
		return C9;
	}
	
	public ChevronEnum getNext() {
		if (isFinal())
			throw new IllegalStateException("Requested next chevron, while chevron was already final.");
		
		return valueOf(index+1);
	}
	
	private static final EnumKeyMap<Integer, ChevronEnum> ID_MAP = new EnumKeyMap<Integer, ChevronEnum>(values());
	
	@Override
	public Integer getKey() {
		return index;
	}
	
	public static final ChevronEnum valueOf(int index) {
		return ID_MAP.valueOf(index);
	}
}
