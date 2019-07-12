package mrjake.aunis.renderer.stargate;

import org.lwjgl.input.Mouse;

import mrjake.aunis.Aunis;
import net.minecraft.world.World;

/**
 * Holds instance of Stargate's chevron activation request
 * 
 * Previously done by a bunch of variables in {@link StargateRenderer} like "activation", "activationStateChange"
 * 
 * @author MrJake222
 */
public class Activation {
	
	/**
	 * Chevron index on the {@link StargateRenderer#chevronTextureList}.
	 * 
	 * Previously "activation".
	 */
	private int chevronIndex;
	
	public int getChevronIndex() {
		return chevronIndex;
	}
	
	/**
	 * When the {@link Activation} was created.
	 * 
	 * Previously "activationStateChange".
	 */
	private long stateChange;
	
	/**
	 * Are we dimming the chevron?
	 */
	private boolean dimChevron;
	
	/**
	 * {@link ActivationState} containing texture of the {@link Activation#chevronIndex} and removal state.
	 */
	private ActivationState state;
	
	/**
	 * Main constructor
	 * 
	 * @param chevronIndex Chevron index on the chevronTextureList.
	 * @param stateChange When the {@link Activation} was created.
	 * @param dimChevron Are we dimming the chevron?
	 */
	public Activation(int chevronIndex, long stateChange, boolean dimChevron) {
		this.chevronIndex = chevronIndex;
		this.stateChange = stateChange;
		this.dimChevron = dimChevron;
		
		state = new ActivationState(dimChevron ? 10 : 0);
		
//		Mouse.setGrabbed(false);
		Aunis.info("Creating new Activation("+chevronIndex+", "+stateChange+", "+dimChevron+")");
	}
	
	/**
	 * Secondary constructor
	 * 
	 * @param chevronIndex Chevron index on the chevronTextureList.
	 * @param stateChange When the {@link Activation} was created.
	 */
	public Activation(int chevronIndex, long stateChange) {
		this(chevronIndex, stateChange, false);
	}
	
	/**
	 * Main calculations function. Call this in render loop.
	 * 
	 * @param worldTicks Usually {@link World#getTotalWorldTime()}.
	 * @param partialTicks Partial ticks.
	 * 
	 * {@link ActivationState} containing texture of the {@link Activation#chevronIndex} and removal state.
	 */
	public ActivationState activate(long worldTicks, double partialTicks) {
		int stage = (int) ((worldTicks - stateChange + partialTicks) * 3);

//		if (stage < 0) return new ActivationState(dimChevron ? 10 : 0);
				
		if (stage >= 0) {
			
			if (stage <= 10) {			
				if (dimChevron)
					stage = 10 - stage;
				
//				if (chevronIndex == 0) Aunis.info("stage: " + stage + ", dim: " + dimChevron);
				
				state.stage = stage;
			}
				
			else {			
				onActivated();
				
				state.stage = (dimChevron ? 0 : 10);
				state.remove = true;
				
				Aunis.info("Removing Activation("+chevronIndex+", "+stateChange+", "+dimChevron+")");
			}
		}
		
		return state;
	}
	
	/**
	 * Called on stage exceeding 10
	 */
	protected void onActivated() {}

	public static class ActivationState {
		public int stage;
		public boolean remove = false;
		
		public ActivationState(int stage) {
			this.stage = stage;
		}
	}
	
	// Eclipse generated methods
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chevronIndex;
		result = prime * result + (dimChevron ? 1231 : 1237);
		result = prime * result + (int) (stateChange ^ (stateChange >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Activation other = (Activation) obj;
		if (chevronIndex != other.chevronIndex)
			return false;
		if (dimChevron != other.dimChevron)
			return false;
		if (stateChange != other.stateChange)
			return false;
		return true;
	}
}
