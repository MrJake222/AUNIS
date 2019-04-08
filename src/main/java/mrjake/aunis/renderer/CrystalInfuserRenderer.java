package mrjake.aunis.renderer;

import mrjake.aunis.OBJLoader.Model;
import mrjake.aunis.OBJLoader.ModelLoader;
import mrjake.aunis.OBJLoader.ModelLoader.EnumModel;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.renderer.state.CrystalInfuserRendererState;
import mrjake.aunis.tileentity.CrystalInfuserTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CrystalInfuserRenderer implements ISpecialRenderer<CrystalInfuserRendererState>{

	private World world;
	
	private ItemRenderer itemRenderer;
	
	private long creationTime;
	
	public CrystalInfuserRenderer(CrystalInfuserTile te) {
		this.world = te.getWorld();
		creationTime = world.getTotalWorldTime();
		
		itemRenderer = new ItemRenderer(AunisItems.crystalControlDhd, world);
	}
	
	@Override
	public void render(double x, double y, double z, double partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
		
		double tick = world.getTotalWorldTime() - creationTime + partialTicks;
		
		GlStateManager.color(0.4f, 0.4f, 0.4f, 0.3f);
		
		renderBaseModel();
		renderCrystal(tick);
		
		GlStateManager.popMatrix();
	}
		
	private void renderCrystal(double tick) {		
		GlStateManager.rotate((float) (tick * 4.0), 0, 1, 0);
		
		GlStateManager.scale(0.45, 0.45, 0.45);
		GlStateManager.translate(0, 1 + (MathHelper.sin( (float)(tick/10.0f) ) / 3.0f), 0);
		
//		GlStateManager.color(0, 1, 2, 0.3f);
		GlStateManager.enableBlend();
//		GL11.glColor4d(1.0, 0.0, 0.0, 0.3);
		
		itemRenderer.render();
		
		GlStateManager.disableBlend();
	}
	
	private void renderBaseModel() {
//		Aunis.modelLoader.loadModel(EnumModel.CrystalInfuser);
		
		Model infuserModel = ModelLoader.getModel( EnumModel.CrystalInfuser );
				
		if(infuserModel != null) {
			
			EnumModel.CrystalInfuser.bindTexture();

			for (int i=0; i<=3; i++) {
				GlStateManager.pushMatrix();
				
				GlStateManager.rotate(90 * i, 0, 1, 0);
				infuserModel.render();
				
				GlStateManager.popMatrix();
			}
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
