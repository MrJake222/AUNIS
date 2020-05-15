package mrjake.aunis.renderer.stargate;

import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.loader.texture.TextureLoader;
import mrjake.aunis.util.math.MathFunction;
import mrjake.aunis.util.math.MathFunctionImpl;
import mrjake.aunis.util.math.MathRange;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class StargateMilkyWayRenderer extends StargateClassicRenderer<StargateMilkyWayRendererState> {
	
	private static final Vec3d RING_LOC = new Vec3d(0.0, -0.122333, -0.000597);
	private static final float GATE_DIAMETER = 10.1815f;
	
	@Override
	protected void applyTransformations(StargateMilkyWayRendererState rendererState) {
		GlStateManager.translate(0.50, GATE_DIAMETER/2 + rendererState.stargateSize.renderTranslationY, 0.50);			
		GlStateManager.scale(rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale, rendererState.stargateSize.renderScale);
	}
	
	@Override
	protected void renderGate(StargateMilkyWayRendererState rendererState, double partialTicks) {
		renderRing(rendererState, partialTicks);
		GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
		renderChevrons(rendererState, partialTicks);
		
		ElementEnum.MILKYWAY_GATE.bindTextureAndRender();
	}
	
	// ----------------------------------------------------------------------------------------
	// Ring
	
	private void renderRing(StargateMilkyWayRendererState rendererState, double partialTicks) {
		GlStateManager.pushMatrix();
		float angularRotation = rendererState.spinHelper.currentSymbol.getAngle();
		
		if (rendererState.spinHelper.isSpinning)
			angularRotation += rendererState.spinHelper.apply(getWorld().getTotalWorldTime() + partialTicks);
		
		if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 0)
			angularRotation *= -1;

		
		if (rendererState.horizontalRotation == 90 || rendererState.horizontalRotation == 270) {
			GlStateManager.translate(RING_LOC.y, RING_LOC.z, RING_LOC.x);
			GlStateManager.rotate(angularRotation, 1, 0, 0);
			GlStateManager.translate(-RING_LOC.y, -RING_LOC.z, -RING_LOC.x);
		}
		
		else {
			GlStateManager.translate(RING_LOC.x, RING_LOC.z, RING_LOC.y);
			GlStateManager.rotate(angularRotation, 0, 0, 1);
			GlStateManager.translate(-RING_LOC.x, -RING_LOC.z, -RING_LOC.y);
		}
		
		GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
		
		ElementEnum.MILKYWAY_RING.bindTextureAndRender();
		
		GlStateManager.popMatrix();
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	private static MathRange chevronOpenRange = new MathRange(0, 1.57f);
	private static MathFunction chevronOpenFunction = new MathFunctionImpl(x -> x*x*x*x/80f);
	
	private static MathRange chevronCloseRange = new MathRange(0, 1.428f);
	private static MathFunction chevronCloseFunction = new MathFunctionImpl(x0 -> MathHelper.cos(x0*1.1f) / 12f);
	
	private float calculateTopChevronOffset(StargateMilkyWayRendererState rendererState, double partialTicks) {
		float tick = (float) (getWorld().getTotalWorldTime() - rendererState.chevronActionStart + partialTicks);
		float x = tick / 6.0f;
		
		if (rendererState.chevronOpening) {
			if (chevronOpenRange.test(x))
				return chevronOpenFunction.apply(x);
			else {
				rendererState.chevronOpen = true;
				rendererState.chevronOpening = false;
			}
		}
		
		else if (rendererState.chevronClosing) {
			if (chevronCloseRange.test(x))
				return chevronCloseFunction.apply(x);
			else {
				rendererState.chevronOpen = false;
				rendererState.chevronClosing = false;
			}
		}
		
		return rendererState.chevronOpen ? 0.08333f : 0;
	}
	
	@Override
	protected void renderChevron(StargateMilkyWayRendererState rendererState, double partialTicks, ChevronEnum chevron) {
		GlStateManager.pushMatrix();
			
		GlStateManager.rotate(chevron.rotation, 0, 0, 1);
		
		TextureLoader.getTexture(rendererState.chevronTextureList.get(chevron)).bindTexture();
					
		if (chevron.isFinal()) {
			float chevronOffset = calculateTopChevronOffset(rendererState, partialTicks);
			
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(0, chevronOffset, 0);
			ElementEnum.MILKYWAY_CHEVRON_LIGHT.render();
			
			GlStateManager.translate(0, -2*chevronOffset, 0);
			ElementEnum.MILKYWAY_CHEVRON_MOVING.render();

			GlStateManager.popMatrix();
		}
		
		else {
			ElementEnum.MILKYWAY_CHEVRON_MOVING.render();
			ElementEnum.MILKYWAY_CHEVRON_LIGHT.render();
		}			
		
		ElementEnum.MILKYWAY_CHEVRON_FRAME.bindTextureAndRender();
		ElementEnum.MILKYWAY_CHEVRON_BACK.render();

		
		GlStateManager.popMatrix();
	}
}
