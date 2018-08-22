package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.Renderer;
import mrjake.aunis.renderer.RendererState;
import net.minecraft.tileentity.TileEntity;

@SuppressWarnings("rawtypes")
public abstract class RenderedTileEntity extends TileEntity {
	
	protected Renderer renderer;
	protected RendererState rendererState;
	
	public abstract Renderer getRenderer();
	public abstract RendererState getRendererState();
	
	public void setRendererState(RendererState rendererState) {
		this.rendererState = rendererState;
		Aunis.info("rendererState synced: "+rendererState.toString());
		
		markDirty();
	}
}
