package mrjake.aunis.renderer.activation;

import java.util.List;

import mrjake.aunis.renderer.DHDRenderer;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRenderer;
import net.minecraft.world.World;

/**
 * Holds instance of activation request(fox ex. Stargate's chevron or DHD's button).
 * 
 * Previously done by a bunch of variables in {@link StargateMilkyWayRenderer} or {@link DHDRenderer} like "activation", "activationStateChange"
 * 
 * @author MrJake222
 */
public abstract class Activation<K> {
	
	/**
	 * Texture index on the list.
	 * 
	 * Previously "activation".
	 */
	protected K textureKey;
	
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
	public Activation(K textureKey, long stateChange, boolean dim) {
		this.textureKey = textureKey;
		this.stateChange = stateChange;
		this.dim = dim;
		
		state = new ActivationState(dim ? getMaxStage() : 0);
		active = true;
	}
	
	/**
	 * Get max activation stage inclusive.
	 * 
	 * @return Max activation stage.
	 */
	protected abstract float getMaxStage();
	
	/**
	 * Get tick multiplier for given {@link Activation#textureIndex}.
	 * 
	 * @param textureIndex Texture index.
	 * @return Tick multiplier.
	 */
	protected abstract float getTickMultiplier();
	
	/**
	 * Mark this {@link Activation} inactive.
	 * Prevents {@link Activation#activate(long, double)} from being called in the render loop.
	 * 
	 * @return This instance.
	 */
	public Activation<K> inactive() {
		this.active = false;
		
		return this;
	}
	
	/**
	 * Mark this {@link Activation} active.
	 * @see Activation#inactive().
	 * 
	 * @return This instance.
	 */
	public Activation<K> active() {
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
		double stage = (worldTicks - stateChange + partialTicks) * getTickMultiplier();
				
		if (stage >= 0) {
			
			if (stage <= getMaxStage()) {			
				if (dim)
					stage = getMaxStage() - stage;
								
				state.stage = (float) stage;
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
		public float stage;
		public boolean remove = false;
		
		public ActivationState(float stage) {
			this.stage = stage;
		}
	}
	
	/**
	 * SAM interface used by {@link Activation#iterate(List, long, double, IActivationCallback)}.
	 * 
	 * @author MrJake222
	 */
	public static interface IActivationCallback<K> {
		public void run(K textureKey, float stage);
	}
	
	/**
	 * Iterates through {@link List} of {@link Activation} and calls provided {@link IActivationCallback}.
	 * 
	 * @param activationList {@link List} of {@link Activation}.
	 * @param ticks Usually {@link World#getTotalWorldTime()}.
	 * @param partialTicks Partial ticks.
	 * @param callback Callback interface.
	 */
	public static <K> void iterate(List<Activation<K>> activationList, long ticks, double partialTicks, IActivationCallback<K> callback) {
		for (int i=0; i<activationList.size();) {			
			Activation<K> activation = activationList.get(i);
			
			if (activation.isActive()) {			
				ActivationState activationState = activation.activate(ticks, partialTicks);
				
				callback.run(activation.textureKey, activationState.stage);
				
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
		result = prime * result + textureKey.hashCode();
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
		@SuppressWarnings("unchecked")
		Activation<K> other = (Activation<K>) obj;
		if (!textureKey.equals(other.textureKey))
			return false;
		if (dim != other.dim)
			return false;
		if (stateChange != other.stateChange)
			return false;
		return true;
	}
}
