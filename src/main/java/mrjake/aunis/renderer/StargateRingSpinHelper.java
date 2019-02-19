package mrjake.aunis.renderer;

import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class handling ring spinning. Does the calculations to smoothly start
 * and stop the ring spinning.
 *
 */
public class StargateRingSpinHelper {	
	/**
	 * Defines how much angle is added to the ring's rotation
	 * in one game's tick.
	 */
	public static final double anglePerTick = 2.0;
	
	/**
	 * Stores how much time it takes to speed up or stop the ring.
	 */
	private int speedUpTimeTick;
	
	/**
	 * Setter for speedUpTimeTick. Recalculates needed variables.
	 * See: https://www.desmos.com/calculator/v1q9dsvggr
	 * 
	 * @param speedUpTimeTick
	 */
	public void setSpeedUpTimeTick(int speedUpTimeTick) {
		this.speedUpTimeTick = speedUpTimeTick;
				
		a2 = this.speedUpTimeTick * anglePerTick;
		b2 = this.speedUpTimeTick / anglePerTick;
	}
	
	private double a2;
	private double b2;
	
	private World world;
	private BlockPos pos;
	private StargateRenderer renderer;
	
	/**
	 * Indicates if final chevron lock sound has been played
	 */
	private boolean lockSoundPlayed;
	
	/**
	 * Holds StargateRendererState reference.
	 */
	StargateRendererState state;
	
	/**
	 * Creates this helper with specified parameters 
	 */
	public StargateRingSpinHelper(World world, BlockPos pos, StargateRenderer renderer, StargateRendererState state) {
		this.world = world;
		this.pos = pos;
		this.renderer = renderer;
		
		this.state = state;
	}
	
	public void getState(StargateRendererState state) {
		
	}
	
	/**
	 * Starts ring rotation from given angle. First smoothly accelerates it
	 * and keeps it's speed till requestStop() is called.
	 * 
	 * @param ringStartingRotation - enter rotation value
	 */
	public void requestStart(double ringStartingRotation) {
		this.state.tickStart = world.getTotalWorldTime();
				
		this.state.ringStartingRotation = ringStartingRotation;
		this.state.isSpinning = true;
		this.state.stopRequested = false;
	}
	
	/**
	 * Requests ring stop. Slowly deccelerates it and then stops it by
	 * turning off all the logic.  
	 */
	public void requestStop() {
		state.tickStopRequested = world.getTotalWorldTime() - state.tickStart;
		setSpeedUpTimeTick(35);
		
		state.stopRequested = true;
	}
	
	/**
	 * Square function that produces smooth acceleration.
	 * Used on the very beginning of the logic function
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinUpFormula(double tick) {
		return tick*tick / b2;
	}
	
	/**
	 * Linear function that produces fixed speed rotation.
	 * Used between spinUpFormula and spinDownFormula
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinFormula(double tick) {
//		Aunis.info("spin = "+((2 * anglePerTick * tick) - a2));
//		Aunis.info("horRot: " + renderer.getHorizontalRotation());
		
		return (2 * anglePerTick * tick) - a2;
	}
	
	/**
	 * Square function adjusted to linear. Produces smooth decceleration of
	 * the Stargate's ring 
	 * 
	 * @param tick - Ticks since animation started
	 * @return Ring's rotation
	 */
	public double spinDownFormula(double tick) {
		double effectiveTick = tick - (state.tickStopRequested + speedUpTimeTick);// + 5;
		
		return -((effectiveTick*effectiveTick) / b2) + (2 * anglePerTick * (state.tickStopRequested + speedUpTimeTick)) - (2*a2) + 5.012;
	}
	
	/**
	 * Main logic function. Handles switching between above functions.
	 * 
	 * @param partialTicks
	 * @return Ring's rotation
	 */
	public double spin(double partialTicks) {
		double effectiveTick = world.getTotalWorldTime() - state.tickStart + partialTicks;
		
		double angle = this.state.ringStartingRotation;
		
		if (state.isSpinning) {	
			
			// If user requested ring to stop
			// Run decceleration function
			if (state.stopRequested) {
				if (world.isRemote && !lockSoundPlayed && (effectiveTick*1.25f >= (state.tickStopRequested + speedUpTimeTick))) {
					lockSoundPlayed = true;
											
					// Play final chevron lock sound
					if (renderer.state.dialingComplete) {
						renderer.moveFinalChevron();
						AunisSoundHelper.playSound(world, pos, AunisSoundHelper.chevronLockDHD);
					}
				}
				
				if (effectiveTick >= (state.tickStopRequested + speedUpTimeTick)) {
					state.stopRequested = false;
					state.isSpinning = false;
					lockSoundPlayed = false;
											
					// Aunis.info("Angle: " + state.ringAngularRotation);
				}
				
				angle += spinDownFormula(effectiveTick);
			}
			
			else {			
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
}

