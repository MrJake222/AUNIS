package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraft.tileentity.TileEntity;

@SuppressWarnings("rawtypes")
public abstract class RenderedTileEntity extends TileEntity {
	
	protected Renderer renderer;
	protected RendererState rendererState = null;
	
	public abstract Renderer getRenderer();
	public abstract RendererState getRendererState();
	public abstract void setRendererState(RendererState rendererState);
	
	public abstract void setUpgrade(boolean hasUpgrade);
	public abstract void setInsertAnimation(boolean insertAnimation);
}
