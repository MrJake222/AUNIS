package mrjake.aunis.util.math;

public class MathFunctionQuadratic implements MathFunction {
	
	/**
	 * x^2 scalar
	 */
	private float a;
	
	/**
	 * x scalar
	 */
	private float b;

	/**
	 * Offset
	 */
	private float c;	
	
	public MathFunctionQuadratic(float a, float b, float c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public float apply(float x) {
		return a*x*x + b*x + c;
	}
}
