package mrjake.aunis.tesr;

import mrjake.aunis.renderer.stargate.StargateAbstractRenderer;

/**
 * Defines basic function to be implemented on a renderer
 * such as {@link StargateAbstractRenderer}.
 * 
 * @author MrJake222
 */
public interface RendererInterface {
	
	/**
	 * Main render function.
	 * 
	 * @param x X-coord.
	 * @param y Y-coord.
	 * @param z Z-coord.
	 * @param partialTicks Partial ticks.
	 */
	public abstract void render(double x, double y, double z, float partialTicks);
}
