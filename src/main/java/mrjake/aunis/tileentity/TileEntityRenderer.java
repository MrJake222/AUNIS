package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.state.RendererState;

@SuppressWarnings("rawtypes")
public interface TileEntityRenderer {	
	public abstract Renderer getRenderer();
	public abstract RendererState getRendererState();
	public abstract void setRendererState(RendererState rendererState);
	
	public abstract void setUpgrade(boolean hasUpgrade);
	public abstract void setInsertAnimation(boolean insertAnimation);
}
