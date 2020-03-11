package mrjake.aunis.stargate;

public enum EnumSpinDirection {
	COUNTER_CLOCKWISE(0, -1),
	CLOCKWISE(1, 1);
	
	public int id;
	public int mul;
	
	private EnumSpinDirection(int id, int mul) {
		this.id = id;
		this.mul = mul;
	}
	
	public EnumSpinDirection opposite() {
		if (this == CLOCKWISE)
			return COUNTER_CLOCKWISE;
		
		else
			return CLOCKWISE;
	}

	public float getDistance(EnumSymbol currentSymbol, EnumSymbol targetSymbol) {
		int indexDiff;
		
		if (this == CLOCKWISE)
			indexDiff = currentSymbol.angleIndex - targetSymbol.angleIndex;
		else
			indexDiff = targetSymbol.angleIndex - currentSymbol.angleIndex;
		
		if (indexDiff < 0)
			indexDiff += 39;
		
		return indexDiff * EnumSymbol.ANGLE_PER_GLYPH;
	}
	
	public static EnumSpinDirection valueOf(int id) {
		return id == 0 ? COUNTER_CLOCKWISE : CLOCKWISE;
	}
}
