package mrjake.aunis.renderer;

import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.tileentity.BeamerTile;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class BeamerRenderer extends TileEntitySpecialRenderer<BeamerTile> {

	@Override
	public void render(BeamerTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		
		if (AunisConfig.debugConfig.renderBoundingBoxes) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			te.getRenderBoxForDisplay().render();
	        GlStateManager.popMatrix();
		}
		
		if (te.getMode() != BeamerModeEnum.NONE && te.beamRadiusClient > 0) { 
			GlStateManager.alphaFunc(516, 0.1F);
	        this.bindTexture(TileEntityBeaconRenderer.TEXTURE_BEACON_BEAM);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 15 * 16, 15 * 16);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x+0.5, y+0.5, z+0.5);
			GlStateManager.rotate(180-te.getFacing().getHorizontalAngle(), 0, 1, 0);
			GlStateManager.rotate(-90, 1, 0, 0);
			
			float mul = 1;
	        TileEntityBeaconRenderer.renderBeamSegment(-0.5, -1.07, -0.5, partialTicks*mul, (te.getRole() == BeamerRoleEnum.TRANSMIT ? 1 : -1), getWorld().getTotalWorldTime()*mul, 1, te.beamLengthClient, te.getMode().colors, te.beamRadiusClient, te.beamRadiusClient+0.05f);
	        GlStateManager.popMatrix();
	        GlStateManager.enableCull();
		}
	}
	
	@Override
	public boolean isGlobalRenderer(BeamerTile te) {
		return true;
	}
}
