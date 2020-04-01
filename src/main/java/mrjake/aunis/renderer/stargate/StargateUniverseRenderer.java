package mrjake.aunis.renderer.stargate;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.OBJLoader.ModelEnum;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
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
		float angularRotation = rendererState.spinHelper.currentSymbol.getAngle();
		
		if (rendererState.spinHelper.isSpinning)
			angularRotation += rendererState.spinHelper.apply(getWorld().getTotalWorldTime() + partialTicks);
		
		GlStateManager.rotate(rendererState.horizontalRotation - 90, 1, 0, 0);
		GlStateManager.rotate((float) angularRotation + 0.6f, 0, 1, 0);
		renderChevrons(rendererState, partialTicks);
					
		rendererDispatcher.renderEngine.bindTexture(ModelEnum.UNIVERSE_GATE_MODEL.textureResource);
		ModelLoader.getModel(ModelEnum.UNIVERSE_GATE_MODEL).render();
		
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		for (SymbolUniverseEnum symbol : SymbolUniverseEnum.values()) {
			if (symbol.model != null) {
				float color = rendererState.getSymbolColor(symbol) + 0.25f;
				
				GlStateManager.color(color, color, color);
				ModelLoader.getModel(symbol.model).render();
			}
		}
		GlStateManager.enableTexture2D();
		
		rendererState.iterate(getWorld(), partialTicks);
	}
	
	@Override
	protected void renderKawoosh(StargateAbstractRendererState rendererState, double partialTicks) {
		GlStateManager.translate(0, 0.04, 0);
		GlStateManager.rotate(90, 1, 0, 0);
		GlStateManager.scale(0.9, 0.9, 0.9);
		
		super.renderKawoosh(rendererState, partialTicks);
	}
	
	
	// ----------------------------------------------------------------------------------------
	// Chevrons
	
	@Override
	protected void renderChevron(StargateUniverseRendererState rendererState, double partialTicks, ChevronEnum chevron) {
		GlStateManager.pushMatrix();
		
		GlStateManager.rotate(-chevron.rotation, 0, 1, 0);
		rendererDispatcher.renderEngine.bindTexture(rendererState.chevronTextureList.get(chevron));
		ModelLoader.getModel(ModelEnum.UNIVERSE_CHEVRON_MODEL).render();
		
		GlStateManager.popMatrix();
	}
}
