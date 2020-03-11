package mrjake.aunis.util.math;

public class MathFunctionLinear implements MathFunction {
	
	/**
	 * Direction
	 */
	private float a;
	
	/**
	 * Offset
	 */
	private float b;
	
	public MathFunctionLinear(float a, float b) {
		this.a = a;
		this.b = b;
	}
	
	@Override
	public float apply(float x) {
		return a*x + b;
	}
}
