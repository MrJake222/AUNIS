package mrjake.aunis.tesr;

import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class StargateTESR extends TileEntitySpecialRenderer<StargateBaseTile> {
	
	private float gateDiameter = 10.1815f;
	
	@Override
	public void render(StargateBaseTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {	
		x += 0.50;
		y += gateDiameter/2;
		z += 0.50;
		
		te.getRenderer().render(x, y, z, partialTicks);
	}
}

