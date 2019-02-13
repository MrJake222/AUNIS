package mrjake.aunis.tileentity;

import mrjake.aunis.renderer.CrystalInfuserRenderer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.state.CrystalInfuserRendererState;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraft.tileentity.TileEntity;

public class CrystalInfuserTile extends TileEntity implements ITileEntityRendered {

	CrystalInfuserRenderer renderer;
	
	@Override
	public ISpecialRenderer<CrystalInfuserRendererState> getRenderer() {
		if (renderer == null)
			renderer = new CrystalInfuserRenderer(this);
		
		return renderer;
	}

	@Override
	public RendererState getRendererState() {
		return new CrystalInfuserRendererState(pos);
	}

	@Override
	public void setRendererState(RendererState rendererState) {
		// TODO Auto-generated method stub
		
	}
	
}
