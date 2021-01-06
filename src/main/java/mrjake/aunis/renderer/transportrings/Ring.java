package mrjake.aunis.renderer.transportrings;

import mrjake.aunis.loader.ElementEnum;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Contains single instance of a transport ring
 *
 */
public class Ring {
	
	private World world;
//	private int index;
	
	private boolean shouldRender;
	private boolean shouldAnimate;
	private boolean ringsUprising;
	private long animationStart;
		
	private double y;
	private double yMax;
	
	public Ring(World world, int index) {
		this.world = world;
//		this.index = index;
		
		this.shouldRender = false;
		
		this.y = 0;
		this.yMax = 4-index + 1.5;
	}

	public void render(double partialTicks) {		
		if (shouldRender) {
			GlStateManager.pushMatrix();				
			GlStateManager.translate(0, y, 0);
			
			ElementEnum.RINGS_BLACK.bindTextureAndRender();
			GlStateManager.popMatrix();

		}
		
		if (shouldAnimate) {
			double effTick = world.getTotalWorldTime() - animationStart + partialTicks;
			
			effTick /= TransportRingsRenderer.ANIMATION_SPEED_DIVISOR; // 2.7 = 1.8 * 1.5
//			effTick /= 1.8;
//			effTick /= 1.5;
			
			if (effTick > Math.PI) {
				effTick = Math.PI;
				shouldAnimate = false;
			}
			
			float cos = MathHelper.cos((float) effTick);
			
			if (ringsUprising)
				cos *= -1;
			
			y = ((cos + 1) / 2) * yMax;
			
			if (!ringsUprising && effTick == Math.PI)
				shouldRender = false;
			
//			Aunis.info("y = " + y);
		}
	}
	
	public void animate(boolean ringsUprising) {		
		this.ringsUprising = ringsUprising;
		shouldRender = true;
		
		animationStart = world.getTotalWorldTime();
		shouldAnimate = true;
	}

	public void setTop() {
		y = yMax;
		
		shouldAnimate = false;
		shouldRender = true;
	}

	public void setDown() {
		shouldAnimate = false;
		shouldRender = false;
	}
}
