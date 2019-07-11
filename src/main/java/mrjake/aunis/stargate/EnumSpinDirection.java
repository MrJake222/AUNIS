package mrjake.aunis.stargate;

public enum EnumSpinDirection {
	COUNTER_CLOCKWISE(0, 1),
	CLOCKWISE(1, -1);
	
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

	public double getDistance(double ringAngularRotation, float angle) {
		double distance = ringAngularRotation - angle;
		
		if (distance < 0) {
			if (this == CLOCKWISE) distance += 360;
			else distance *= -1;
		}
		
		else {
			if (this == COUNTER_CLOCKWISE) distance += 360;
		}
		
		return distance;
	}
	
	public static EnumSpinDirection valueOf(int id) {
		return id == 0 ? COUNTER_CLOCKWISE : CLOCKWISE;
	}
}
