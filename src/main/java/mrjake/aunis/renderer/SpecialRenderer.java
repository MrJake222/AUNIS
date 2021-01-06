package mrjake.aunis.renderer;

import mrjake.aunis.Aunis;
import mrjake.aunis.tesr.RendererProviderInterface;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class SpecialRenderer extends TileEntitySpecialRenderer<TileEntity> {

	@Override
	public void render(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		try {
			((RendererProviderInterface) te).getRenderer().render(x, y, z, partialTicks);
		}
		
		catch (ClassCastException e) {
			Aunis.info("RendererProviderInterface is not implemented on " + te.getClass().getName());
		}
	}
}
