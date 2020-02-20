package mrjake.aunis.util.math;

public class MathFunctionImpl implements MathFunction {
	private MathFunction function;
	
	public MathFunctionImpl(MathFunction function) {
		this.function = function;
	}
	
	@Override
	public float apply(float x) {
		return function.apply(x);
	}
}
