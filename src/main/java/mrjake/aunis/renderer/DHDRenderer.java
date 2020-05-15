package mrjake.aunis.renderer;

import mrjake.aunis.loader.ElementEnum;
import mrjake.aunis.loader.model.ModelLoader;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class DHDRenderer extends TileEntitySpecialRenderer<DHDTile> {

	@Override
	public void render(DHDTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		DHDRendererState rendererState = te.getRendererStateClient();
		
		if (rendererState != null) {	
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x+0.5, y, z+0.5);
			GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
			
			ElementEnum.MILKYWAY_DHD.bindTextureAndRender();
			
			for (SymbolMilkyWayEnum symbol : SymbolMilkyWayEnum.values()) {
				rendererDispatcher.renderEngine.bindTexture(rendererState.getButtonTexture(symbol));
				ModelLoader.getModel(symbol.modelResource).render();
			}
			
			GlStateManager.popMatrix();
			
			rendererState.iterate(getWorld(), partialTicks);
		}
	}
}
