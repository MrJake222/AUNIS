package mrjake.aunis.tesr;

import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class CrystalInfuserTESR extends TileEntitySpecialRenderer<CrystalInfuserTile> {
	@Override
	public void render(CrystalInfuserTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		x += 0.50;
		z += 0.50;
		
		te.getRenderer().render(x, y, z, partialTicks);
	}
}
