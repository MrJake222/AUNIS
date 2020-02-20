package mrjake.aunis.util.math;

import java.util.function.Predicate;

public class MathRange implements Predicate<Float> {
	
	private float start;
	private float end;

	public MathRange(float start, float end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public boolean test(Float x) {
		return x >= start && x <= end;
	}
	
}
