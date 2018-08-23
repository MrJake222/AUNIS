package mrjake.aunis.tesr;

import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class DHD_TESR extends TileEntitySpecialRenderer<DHDTile> {
		
	@Override
	public void render(DHDTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {		
		te.getRenderer().render(x, y, z, partialTicks);
	}
}
