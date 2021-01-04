package mrjake.aunis.util.math;

/**
 * Wrapper for {@link MathRange}, {@link MathFunction} pair.
 * 
 * @author MrJake222
 */
public class MathRangedFunction {
	
	public final MathRange range;
	public final MathFunction function;
	
	public MathRangedFunction(MathRange range, MathFunction function) {
		this.range = range;
		this.function = function;
	}
}
