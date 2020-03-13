package mrjake.aunis.renderer;

import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class DHDRenderer extends TileEntitySpecialRenderer<DHDTile> {

	@Override
	public void render(DHDTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		DHDRendererState rendererState = te.getRendererStateClient();
		
		Model dhdModel = ModelLoader.getModel(EnumModel.DHD_MODEL);
		Model brbModel = ModelLoader.getModel(EnumModel.BRB);
		
		if (dhdModel != null && brbModel != null && rendererState != null) {	
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(x+0.5, y, z+0.5);
			GlStateManager.rotate(rendererState.horizontalRotation, 0, 1, 0);
						
			EnumModel.DHD_MODEL.bindTexture();
			dhdModel.render();
			
			ModelLoader.bindTexture(rendererState.buttonTextureList.get(EnumSymbol.BRB.id));
			brbModel.render();
			
			for (EnumSymbol symbol : EnumSymbol.values()) {
				Model buttonModel = ModelLoader.getModel(EnumModel.getModelForSymbol(symbol));
				
				if (buttonModel != null) {
					ModelLoader.bindTexture(rendererState.buttonTextureList.get(symbol.id));
					buttonModel.render();
				}
			}
			
			GlStateManager.popMatrix();
			
			rendererState.iterate(getWorld(), partialTicks);
		}
	}
}
