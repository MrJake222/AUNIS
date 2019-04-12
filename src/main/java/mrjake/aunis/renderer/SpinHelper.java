package mrjake.aunis.renderer;

import mrjake.aunis.renderer.state.SpinState;
import net.minecraft.world.World;

/**
 * Class handling spinning. Does the calculations to smoothly start
 * and stop spinning.
 *
 */
public class SpinHelper {	
	/**
	 * Defines how much angle is added to the ring's rotation
	 * in one game's tick.
	 */
	public static final double anglePerTick = 2.0;
	
	/**
	 * Stores how much time it takes to speed up or stop the ring.
	 */
	protected int speedUpTimeTick;
	
	/**
	 * Setter for speedUpTimeTick. Recalculates needed variables.
	 * See: https://www.desmos.com/calculator/v1q9dsvggr
	 * 
	 * @param speedUpTimeTick
	 */
	protected void setSpeedUpTimeTick(int speedUpTimeTick) {
		this.speedUpTimeTick = speedUpTimeTick;
				
		a2 = this.speedUpTimeTick * anglePerTick;
		b2 = this.speedUpTimeTick / anglePerTick;
		
		a = Math.sqrt(a2);
		b = Math.sqrt(b2);
	}
	
	private double a2;
	private double b2;
	
	private double a;
	private double b;
	
	protected World world;	
	protected SpinState state;
	
	/**
	 * Creates this helper with specified parameters 
	 */
	public SpinHelper(World world, SpinState state, int defaultSpeedUpTimeTick) {		
		this.world = world;		
		this.state = state;
		
		setSpeedUpTimeTick(defaultSpeedUpTimeTick);
	}
	
	public SpinHelper(World world, SpinState state) {
		this(world, state, 25);
	}
	
	/**
	 * Starts ring rotation from given angle. First smoothly accelerates it
	 * and keeps it's speed till requestStop() is called.
	 * 
	 * @param startingRotation - enter rotation value
	 */
	public void requestStart(double startingRotation) {
		this.state.tickStart = world.getTotalWorldTime();

		this.state.startingRotation = startingRotation;
		this.state.isSpinning = true;
		this.state.stopRequested = false;
	}
	
	/**
	 * Requests ring stop. Slowly deccelerates it and then stops it by
	 * turning off all the logic.  
	 */
	public void requestStop() {
		state.tickStopRequested = world.getTotalWorldTime() - state.tickStart;
		
		state.stopRequested = true;
	}
	
	/**
	 * Square function that produces smooth acceleration.
	 * Used on the very beginning of the logic function
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinUpFormula(double effectiveTick) {
		return effectiveTick*effectiveTick / b2;
	}
	
	/**
	 * Linear function that produces fixed speed rotation.
	 * Used between spinUpFormula and spinDownFormula
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinFormula(double effectiveTick) {
//		Aunis.info("spin = "+((2 * anglePerTick * tick) - a2));
//		Aunis.info("horRot: " + renderer.getHorizontalRotation());
		
//		Aunis.info("spinFormula-("+effectiveTick+")");
		
		return (2 * anglePerTick * effectiveTick) - a2;
	}
	
	/**
	 * Square function adjusted to linear. Produces smooth decceleration of
	 * the Stargate's ring 
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinDownFormula(double effectiveTick) {
		effectiveTick += getStopTickShift();
		
		// Stop point
		double c = state.tickStopRequested + speedUpTimeTick;		
		double first = (effectiveTick - c) / b;
		
		return (first*first * -1) + (((2*a*c)/b) - (2*a2));// + 180;
	}

	/**
	 * Main logic function. Handles switching between above functions.
	 * 
	 * @param partialTicks
	 * @return Ring's rotation
	 */
	public double spin(double partialTicks) {
		double effectiveTick = world.getTotalWorldTime() - state.tickStart + partialTicks;
		
		double angle = this.state.startingRotation;
		
		if (state.isSpinning) {	
			
			// If user requested ring to stop
			// Run decceleration function
			if (state.stopRequested) {
				stopRequestedAction(effectiveTick);
				
				if (effectiveTick >= (state.tickStopRequested + speedUpTimeTick)) {
					state.stopRequested = false;
					state.isSpinning = false;
					
					onStopReached();
				}
				
				angle += spinDownFormula(effectiveTick);
			}
			
			else {			
//				Aunis.info("effectiveTick < speedUpTimeTick: "+effectiveTick+" < "+speedUpTimeTick);
				
				// If still below speed up time
				// Do the square function
				if (effectiveTick < speedUpTimeTick) {
					angle += spinUpFormula(effectiveTick);
				}
				
				else {
					// Just add ring's rotation linearly
					angle += spinFormula(effectiveTick);
				}
			}
		}
		
		return angle;
	}
	
	/**
	 * StargateRingSpinHelper uses this to play ring stop sound
	 * It is run when requestStop() has run, but spin has not come to stop
	 * 
	 * @param effectiveTick
	 */
	protected void stopRequestedAction(double effectiveTick) {}
	
	/**
	 * Called when spin came to full stop
	 * StargateRingSpinHelper puts it to use to clear flags
	 */
	protected void onStopReached() {}
	
	protected double getStopTickShift() {
		return 0;
	}
}

