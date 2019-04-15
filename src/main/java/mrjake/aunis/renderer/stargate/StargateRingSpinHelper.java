package mrjake.aunis.renderer.stargate;

import mrjake.aunis.renderer.SpinHelper;
import mrjake.aunis.renderer.state.SpinState;
import mrjake.aunis.sound.AunisSoundHelper;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class handling ring spinning. Does the calculations to smoothly start
 * and stop the ring spinning.
 *
 */
public class StargateRingSpinHelper extends SpinHelper {
	
	private BlockPos pos;
	private StargateRenderer renderer;
	
	/**
	 * Indicates if final chevron lock sound has been played
	 */
	private boolean lockSoundPlayed;
	
	public StargateRingSpinHelper(World world, BlockPos pos, StargateRenderer renderer, SpinState state) {
		super(world, state);
		
		this.pos = pos;
		this.renderer = renderer;
	}
	
	@Override
	public void requestStart(double startingRotation) {
		setSpeedUpTimeTick(25);
		
		super.requestStart(startingRotation);
	}
	
	@Override
	public void requestStop() {
		setSpeedUpTimeTick(35);		
		
		super.requestStop();
	}
	
	@Override
	protected double getStopTickShift() {
		return 5;
	}
	
	protected void stopRequestedAction(double effectiveTick) {
		if (world.isRemote && !lockSoundPlayed && (effectiveTick*1.25f >= (state.tickStopRequested + speedUpTimeTick))) {
			lockSoundPlayed = true;
									
			// Play final chevron lock sound
			if (renderer.state.dialingComplete) {
				renderer.moveFinalChevron();
				AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.chevronLockDHD);
			}
		}
	}
		
	@Override
	protected void onStopReached() {
		lockSoundPlayed = false;
	}
}