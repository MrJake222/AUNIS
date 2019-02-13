package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.RendererState;

/**
 * Implemented by TileEntities that use TESR
 * 
 * Manages getting the Renderer(client side only),
 * getting the RendererState(server side, for Renderer update on block load)
 *
 */
public interface ITileEntityRendered {	
	
	/**
	 * Should return instance of specified renderer(StargateRenderer for StartgateBaseTile for ex.)
	 * or create it if it doesn't exists already. Doesn't set any of it's parameters.
	 * 
	 * Used by client-side TESR.
	 * 
	 * @return Renderer instance.
	 */
	@SuppressWarnings("rawtypes")
	public abstract ISpecialRenderer getRenderer();
	
	/**
	 * Should return an RendererState instance with all parameters that matter to client-side renderer.
	 * 
	 * @return Specific RendererState instance.
	 */
	public abstract RendererState getRendererState();
	
	/**
	 * 
	 * 
	 * @param rendererState
	 */
	public abstract void setRendererState(RendererState rendererState);
}
