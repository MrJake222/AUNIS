package mrjake.aunis.renderer.crystalinfuser;

import mrjake.aunis.renderer.stargate.StargateRendererStatic.QuadStrip;
import net.minecraft.client.renderer.GlStateManager;

public class CrystalInfuserWave {

	private double created;
	
	public CrystalInfuserWave(double tickPartial) {
		this.created = tickPartial;
	}
	
	/**
	 * Renders the wave
	 * 
	 * @param tick - Ticks of the world
	 * @return True if the wave needs to be removed from the list
	 */
	public boolean render(double tick) {
		tick -= created;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();

		GlStateManager.rotate(-90, 1, 0, 0);
		GlStateManager.translate(0, 0, tick / 160 + 0.2);
		
		double alpha = 0.5 - (tick / 80);
		float radius = (float) (tick / 300) + 0.1f;
		new QuadStrip(0, radius, radius + 0.04f, null).renderBoth(0, 0f, 0f, true, (float) (alpha<0 ? 0 : alpha), 0, true);
				
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
				
		return alpha < 0.1;
	}

}
