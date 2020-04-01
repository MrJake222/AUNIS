package mrjake.aunis.stargate;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.util.math.MathFunction;
import mrjake.aunis.util.math.MathFunctionLinear;
import mrjake.aunis.util.math.MathFunctionQuadratic;
import mrjake.aunis.util.math.MathRange;

/**
 * Client-side class helping with the ring's rotation.
 * 
 * @author MrJake222
 */
public class StargateClassicSpinHelper {
	public StargateClassicSpinHelper() {}
	
	public static final float A_ANGLE_PER_TICK = 2;
	public static final float U_SPEEDUP_TIME = 25;
	public static final float S_STOP_TIME = 35;
	
	public SymbolTypeEnum symbolType;
	
	public boolean isSpinning;
	public SymbolInterface currentSymbol;
	public EnumSpinDirection direction = EnumSpinDirection.CLOCKWISE;
	
	private long spinStartTime;
	private SymbolInterface targetSymbol;
	private float targetRotationOffset;
	
	/**
	 * First phase.
	 */
	private static final Map.Entry<MathRange, MathFunction> SPEEDUP_PHASE =
			new AbstractMap.SimpleEntry<MathRange, MathFunction>(new MathRange(0, U_SPEEDUP_TIME), new MathFunctionQuadratic(A_ANGLE_PER_TICK/(2*U_SPEEDUP_TIME), 0, 0));
	
	/**
	 * Second phase's function.
	 */
	private static final MathFunction LINEAR_SPIN_FUNCTION =
			new MathFunctionLinear(A_ANGLE_PER_TICK, -A_ANGLE_PER_TICK*U_SPEEDUP_TIME/2);
	
	private static float getx0(float targetAngle) {
		return targetAngle/A_ANGLE_PER_TICK + (U_SPEEDUP_TIME-S_STOP_TIME)/2;
	}
	
	private static float getTargetRotation(float x0) {
		return A_ANGLE_PER_TICK*x0 + A_ANGLE_PER_TICK*(S_STOP_TIME-U_SPEEDUP_TIME)/2;
	}
	
	public static float getMinimalDistance() {
		return getTargetRotation(U_SPEEDUP_TIME);
	}
	
	public static int getAnimationDuration(float distance) {
		return (int) (getx0(distance) + S_STOP_TIME);
	}
	
	/**
	 * {@link Map} containing the phases.
	 */
	private Map<MathRange, MathFunction> phases = new HashMap<MathRange, MathFunction>(3);
	
	public StargateClassicSpinHelper(SymbolTypeEnum symbolType, SymbolInterface currentSymbol, EnumSpinDirection spinDirection, boolean isSpinning, SymbolInterface targetRingSymbol, long spinStartTime) {
		this.symbolType = symbolType;
		this.currentSymbol = currentSymbol;
		this.direction = spinDirection;
		this.isSpinning = isSpinning;
		this.targetSymbol = targetRingSymbol;
		this.spinStartTime = spinStartTime;
	}	
	
	public void initRotation(long totalWorldTime, SymbolInterface targetSymbol, EnumSpinDirection direction) {
		float distance = direction.getDistance(currentSymbol, targetSymbol);
		
		phases.clear();
		phases.put(SPEEDUP_PHASE.getKey(), SPEEDUP_PHASE.getValue());
		
		float x0 = getx0(distance);
		phases.put(new MathRange(U_SPEEDUP_TIME, x0), LINEAR_SPIN_FUNCTION);
		phases.put(new MathRange(x0, x0+S_STOP_TIME), new MathFunctionQuadratic(-A_ANGLE_PER_TICK/(2*S_STOP_TIME), A_ANGLE_PER_TICK+(A_ANGLE_PER_TICK*x0/S_STOP_TIME), -(A_ANGLE_PER_TICK*U_SPEEDUP_TIME/2 + A_ANGLE_PER_TICK*x0*x0/(2*S_STOP_TIME))));
		
		this.targetSymbol = targetSymbol;
		this.targetRotationOffset = getTargetRotation(x0);
		this.direction = direction;
		this.spinStartTime = totalWorldTime;
		
		isSpinning = true;
	}
	
	private float calculate(float tick) {
		tick -= spinStartTime;
				
		if (tick < 0) {
			Aunis.info("Negative argument");
			return 0;
		}
		
		for (Map.Entry<MathRange, MathFunction> phase : phases.entrySet()) {			
			if (phase.getKey().test(tick)) {
				return phase.getValue().apply(tick);
			}
		}
		
		isSpinning = false;
		currentSymbol = targetSymbol;
		
		return targetRotationOffset;
	}
	
	public float apply(double tick) {
//		Aunis.info("("+tick+", "+calculate((float) tick) * direction.mul+")");
		return calculate((float) tick) * direction.mul;
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(symbolType.id);
		
		buf.writeBoolean(isSpinning);
		buf.writeInt(currentSymbol.getId());
		buf.writeInt(direction.id);
		
		buf.writeLong(spinStartTime);
		buf.writeInt(targetSymbol.getId());
	}

	public void fromBytes(ByteBuf buf) {
		symbolType = SymbolTypeEnum.valueOf(buf.readInt());
		
		isSpinning = buf.readBoolean();
		currentSymbol = symbolType.valueOfSymbol(buf.readInt());
		direction = EnumSpinDirection.valueOf(buf.readInt());
		
		spinStartTime = buf.readLong();
		targetSymbol = symbolType.valueOfSymbol(buf.readInt());
		
		if (isSpinning) {
			initRotation(spinStartTime, targetSymbol, direction);
		}		
	}
}
