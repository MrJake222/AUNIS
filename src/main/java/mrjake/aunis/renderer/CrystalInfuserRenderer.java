package mrjake.aunis.renderer;

import mrjake.aunis.Aunis;
import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.renderer.state.CrystalInfuserRendererState;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

public class CrystalInfuserRenderer implements ISpecialRenderer<CrystalInfuserRendererState>{

	public CrystalInfuserRenderer(CrystalInfuserTile te) {
		
	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		renderBaseModel();
		
		GlStateManager.popMatrix();
	}
	
	private void renderBaseModel() {
		Model infuserModel = Aunis.modelLoader.getModel( EnumModel.CrystalInfuser );
				
		if(infuserModel != null) {			
			ModelLoader.bindTexture( EnumModel.CrystalInfuser );
			infuserModel.render();		
		}
	}

	@Override
	public void setState(CrystalInfuserRendererState rendererState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getHorizontalRotation() {
		return 0;
	}

}
