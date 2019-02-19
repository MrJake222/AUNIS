package mrjake.aunis.tileentity;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.renderer.CrystalInfuserRenderer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.CrystalInfuserRendererState;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraft.tileentity.TileEntity;

public class CrystalInfuserTile extends TileEntity implements ITileEntityRendered {

	CrystalInfuserRenderer renderer;
	CrystalInfuserRendererState rendererState;
	
	@Override
	public ISpecialRenderer<CrystalInfuserRendererState> getRenderer() {
		if (renderer == null)
			renderer = new CrystalInfuserRenderer(this);
		
		return renderer;
	}

	@Override
	public RendererState getRendererState() {	
		if (rendererState == null)
			rendererState = new CrystalInfuserRendererState();
				
		return rendererState;
	}
	
	public RendererState createRendererState(ByteBuf buf) {
		return new CrystalInfuserRendererState(buf);
	}
}
