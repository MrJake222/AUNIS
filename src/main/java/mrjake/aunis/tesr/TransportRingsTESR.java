package mrjake.aunis.tesr;

import mrjake.aunis.tileentity.ITileEntityRendered;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TransportRingsTESR extends TileEntitySpecialRenderer<TransportRingsTile> {
	
	@Override
	public void render(TransportRingsTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		x += 0.50;
		y += 0.63271 / 2;
		z += 0.50;
		
		
		((ITileEntityRendered)te).getRenderer().render(x, y, z, partialTicks);
	}
}
