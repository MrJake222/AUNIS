package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.OBJLoader.ModelEnum;
import mrjake.aunis.OBJLoader.ModelLoader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class StargateUniverseRenderer extends StargateClassicRenderer<StargateUniverseRendererState> {

	private static final float GATE_DIAMETER = 8.67415f;
	
	@Override
	protected Map<BlockPos, IBlockState> getMemberBlockStates(EnumFacing facing) {
		return new HashMap<BlockPos, IBlockState>();
	}

	@Override
	protected void applyLightMap(StargateUniverseRendererState rendererState, double partialTicks) {
		
	}

	@Override
	protected void applyTransformations(StargateUniverseRendererState rendererState) {
		float scale = 0.90f;
		GlStateManager.scale(scale, scale, scale);
		
		GlStateManager.translate(0.5, GATE_DIAMETER/2 + 0.20, 0.5);
		GlStateManager.rotate(90, 0, 0, 1);
	}

	@Override
	protected void renderGate(StargateUniverseRendererState rendererState, double partialTicks) {
		GlStateManager.rotate(rendererState.horizontalRotation - 90, 1, 0, 0);
		renderChevrons(rendererState, partialTicks);
		
//		ModelLoader.loadModel(EnumModel.UNIVERSE_GATE_MODEL);
//		OBJModel gateModel = ModelLoader.getModel(EnumModel.UNIVERSE_GATE_MODEL);
//		OBJModel glyphModel = ModelLoader.getModel(EnumModel.UGLYPH1);
		
//		ModelLoader.loadModel(EnumModel.UGLYPH1);
		
//		if (gateModel != null && glyphModel != null) {			
			rendererDispatcher.renderEngine.bindTexture(ModelEnum.UNIVERSE_GATE_MODEL.textureResource);
			ModelLoader.getModel(ModelEnum.UNIVERSE_GATE_MODEL).render();
						
//			for (int i=0; i<45; i++) {
//				GlStateManager.pushMatrix();
////				GlStateManager.rotate((360/45f)*i, 0, 1, 0);
////				ModelLoader.bindTexture(i%10 == 0 ? "stargate/chevron/universe_chevron10.png" : "stargate/chevron/universe_chevron0.png");
//				GlStateManager.disableTexture2D();
//				float color = 0.4f;
//				GlStateManager.color(color, color, color, 1);
//				glyphModel.render();
//				GlStateManager.enableTexture2D();
//				GlStateManager.popMatrix();
//			}
//		}
	}
	
	@Override
	protected void renderEventHorizon(double partialTicks, boolean white, Float alpha, boolean backOnly, float mul) {
		GlStateManager.translate(0, -0.05, 0);
		GlStateManager.rotate(90, 1, 0, 0);
		GlStateManager.scale(0.9, 0.9, 0.9);
		
		super.renderEventHorizon(partialTicks, white, alpha, backOnly, mul);
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	@Override
	protected void renderChevron(StargateUniverseRendererState rendererState, double partialTicks, ChevronEnum chevron) {
//		OBJModel chevronModel = ModelLoader.getModel(EnumModel.UNIVERSE_CHEVRON_MODEL);
		
//		ModelLoader.loadModel(EnumModel.UNIVERSE_CHEVRON_MODEL);
		
//		if (chevronModel != null) {
			GlStateManager.pushMatrix();
			
//			int angularPosition = ;
//			int angularPosition = 0;
			GlStateManager.rotate(-chevron.rotation, 0, 1, 0);
			
//			ModelLoader.bindTexture(rendererState.chevronTextureList.get(index));
//			ModelLoader.bindTexture(index%2 == 0 ? "stargate/chevron/universe_chevron10.png" : "stargate/chevron/universe_chevron0.png");
//			ModelLoader.bindTexture("stargate/chevron/universe_chevron10.png");
//			chevronModel.render();
//			Mouse.setGrabbed(false);
			rendererDispatcher.renderEngine.bindTexture(rendererState.chevronTextureList.get(chevron));
			ModelLoader.getModel(ModelEnum.UNIVERSE_CHEVRON_MODEL).render();
			
			GlStateManager.popMatrix();
//		}
	}
}
