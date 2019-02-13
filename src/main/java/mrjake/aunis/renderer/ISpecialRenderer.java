package mrjake.aunis.renderer;

import mrjake.aunis.renderer.state.RendererState;

/**
 * This interface should be implemented by renderer class(ex .StargateRenderer)
 *
 * @param <STATE>
 * Gives class that defines renderer state(all the rendering-changing variables)
 * 
 */
public interface ISpecialRenderer<STATE extends RendererState> {
	
	/**
	 * Main render function.
	 * Should be called from the TESR class itself.
	 * Should contain correct(+ 0.5) coordinates.
	 * 
	 */
	public void render(double x, double y, double z, double partialTicks);
	
	/**
	 * Sets state of the renderer.
	 * Used mainly when TileEntity is loaded by World.
	 * 
	 * @param rendererState - defines renderer state.
	 */
	public void setState(STATE rendererState);	
	
	/**
	 * Gets horizontal rotation of the model.
	 * 
	 * Required in UpgradeRenderer for proper rotation.
	 * 
	 * @return calculated horizontal rotation
	 */
	public float getHorizontalRotation();
}
