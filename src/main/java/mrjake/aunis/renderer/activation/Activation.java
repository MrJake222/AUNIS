package mrjake.aunis.renderer.activation;

import java.util.List;

import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.stargate.StargateRendererSG1;
import net.minecraft.world.World;

/**
 * Holds instance of activation request(fox ex. Stargate's chevron or DHD's button).
 * 
 * Previously done by a bunch of variables in {@link StargateRendererSG1} or {@link DHDRenderer} like "activation", "activationStateChange"
 * 
 * @author MrJake222
 */
public abstract class Activation {
	
	/**
	 * Texture index on the list.
	 * 
	 * Previously "activation".
	 */
	protected int textureIndex;
	
	public int getTextureIndex() {
		return textureIndex;
	}
	
	/**
	 * When the {@link Activation} was created.
	 * 
	 * Previously "activationStateChange".
	 */
	private long stateChange;
	
	/**
	 * Are we dimming?
	 */
	protected boolean dim;
	
	/**
	 * {@link ActivationState} containing texture of the {@link Activation#textureIndex} and removal state.
	 */
	private ActivationState state;
	
	/**
	 * Is this {@link Activation} actively called from the render loop?
	 */
	private boolean active;
	
	/**
	 * Main constructor
	 * 
	 * @param textureIndex Index on the texture list.
	 * @param stateChange When the {@link Activation} was created.
	 * @param dim Are we dimming?
	 */
	public Activation(int textureIndex, long stateChange, boolean dim) {
		this.textureIndex = textureIndex;
		this.stateChange = stateChange;
		this.dim = dim;
		
		state = new ActivationState(dim ? getMaxStage() : 0);
		active = true;
	}
	
	/**
	 * Secondary constructor
	 * 
	 * @param textureIndex Index on the texture list.
	 * @param stateChange When the {@link Activation} was created.
	 */
	public Activation(int textureIndex, long stateChange) {
		this(textureIndex, stateChange, false);
	}
	
	/**
	 * Get max activation stage inclusive.
	 * 
	 * @return Max activation stage.
	 */
	protected abstract int getMaxStage();
	
	/**
	 * Get tick multiplier for given {@link Activation#textureIndex}.
	 * 
	 * @param textureIndex Texture index.
	 * @return Tick multiplier.
	 */
	protected abstract int getTickMultiplier();
	
	/**
	 * Mark this {@link Activation} inactive.
	 * Prevents {@link Activation#activate(long, double)} from being called in the render loop.
	 * 
	 * @return This instance.
	 */
	public Activation inactive() {
		this.active = false;
		
		return this;
	}
	
	/**
	 * Mark this {@link Activation} active.
	 * @see Activation#inactive().
	 * 
	 * @return This instance.
	 */
	public Activation active() {
		this.active = true;
		
		return this;
	}
	
	/**
	 * Getter for active
	 * 
	 * @return active state.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Main calculations function. Call this in render loop.
	 * 
	 * @param worldTicks Usually {@link World#getTotalWorldTime()}.
	 * @param partialTicks Partial ticks.
	 * 
	 * {@link ActivationState} containing texture of the {@link Activation#textureIndex} and removal state.
	 */
	public ActivationState activate(long worldTicks, double partialTicks) {
		int stage = (int) ((worldTicks - stateChange + partialTicks) * getTickMultiplier());
				
		if (stage >= 0) {
			
			if (stage <= getMaxStage()) {			
				if (dim)
					stage = getMaxStage() - stage;
								
				state.stage = stage;
			}
				
			else {			
				onActivated();
				
				state.stage = (dim ? 0 : getMaxStage());
				state.remove = true;
			}
		}
		
		return state;
	}
	
	/**
	 * Called on stage exceeding {@link Activation#getMaxStage()}
	 */
	protected void onActivated() {}

	public static class ActivationState {
		public int stage;
		public boolean remove = false;
		
		public ActivationState(int stage) {
			this.stage = stage;
		}
	}
	
	/**
	 * SAM interface used by {@link Activation#iterate(List, long, double, IActivationCallback)}.
	 * 
	 * @author MrJake222
	 */
	public static interface IActivationCallback {
		public void run(int textureIndex, int stage);
	}
	
	/**
	 * Iterates through {@link List} of {@link Activation} and calls provided {@link IActivationCallback}.
	 * 
	 * @param activationList {@link List} of {@link Activation}.
	 * @param ticks Usually {@link World#getTotalWorldTime()}.
	 * @param partialTicks Partial ticks.
	 * @param callback Callback interface.
	 */
	public static void iterate(List<Activation> activationList, long ticks, double partialTicks, IActivationCallback callback) {
		for (int i=0; i<activationList.size();) {			
			Activation activation = activationList.get(i);
			
			if (activation.isActive()) {			
				ActivationState activationState = activation.activate(ticks, partialTicks);
				
				callback.run(activation.getTextureIndex(), activationState.stage);
				
				if (activationState.remove) {				
					activationList.remove(activation);
				}
				
				else i++;
			}
			
			else i++;
		}
	}
	
	// Eclipse generated methods
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + textureIndex;
		result = prime * result + (dim ? 1231 : 1237);
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
		if (textureIndex != other.textureIndex)
			return false;
		if (dim != other.dim)
			return false;
		if (stateChange != other.stateChange)
			return false;
		return true;
	}
}
