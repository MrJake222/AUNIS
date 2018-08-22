package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.renderer.RendererState;
import mrjake.aunis.renderer.StargateRendererState;
import net.minecraft.tileentity.TileEntity;

public abstract class RenderedTileEntity extends TileEntity {
	
	private RendererState rendererState;
	
	public void setRendererState(RendererState rendererState) {
		this.rendererState = rendererState;
		Aunis.info("StargateRendererState synced: "+rendererState.toString());
		
		markDirty();
	}
	
	public RendererState getRendererState() {
		if (rendererState == null)
			rendererState = new StargateRendererState(pos);
		
		return rendererState;
	}
}
