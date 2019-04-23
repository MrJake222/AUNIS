package mrjake.aunis.tesr;

import mrjake.aunis.tileentity.TRControllerTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TRControllerTESR extends TileEntitySpecialRenderer<TRControllerTile> {

	
	@Override
	public void render(TRControllerTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		te.getRenderer().render(x, y, z, partialTicks);
	}
}
