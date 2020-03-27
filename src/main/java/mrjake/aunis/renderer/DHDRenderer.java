package mrjake.aunis.renderer;

import mrjake.aunis.OBJLoader.ModelEnum;
import mrjake.aunis.OBJLoader.ModelLoader;
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
						
			rendererDispatcher.renderEngine.bindTexture(ModelEnum.MILKYWAY_DHD_MODEL.textureResource);
			ModelLoader.getModel(ModelEnum.MILKYWAY_DHD_MODEL).render();
			
			for (SymbolMilkyWayEnum symbol : SymbolMilkyWayEnum.values()) {
				rendererDispatcher.renderEngine.bindTexture(rendererState.getButtonTexture(symbol));
				ModelLoader.getModel(symbol.model).render();
			}
			
			GlStateManager.popMatrix();
			
			rendererState.iterate(getWorld(), partialTicks);
		}
	}
}
