package mrjake.aunis.item.renderer;

import mrjake.aunis.Aunis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

/**
 * Font Renderer that properly handles in-world lighting
 * 
 * @author MrJake222
 *
 */
public class AunisFontRenderer extends FontRenderer {

	private static FontRenderer fontRenderer;

	public static FontRenderer getFontRenderer() {
		if (fontRenderer == null) {
			Minecraft mc = Minecraft.getMinecraft();
			fontRenderer = new AunisFontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
			((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(fontRenderer);
			
			Aunis.logger.debug("Created custom FontRenderer");
		}
		
		return fontRenderer;
	}
	
	public AunisFontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
		super(gameSettingsIn, location, textureManagerIn, unicode);
	}

	@Override
	protected float renderDefaultChar(int ch, boolean italic) {		
		int i = ch % 16 * 8;
		int j = ch / 16 * 8;
		int k = italic ? 1 : 0;
		bindTexture(this.locationFontTexture);
		int l = this.charWidth[ch];
//		float f = (float)l - 0.01F;
		float f = 8;
		
		GlStateManager.glBegin(5);
		
		GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, (float)j / 128.0F); // 3
		GlStateManager.glVertex3f(this.posX + (float)k, this.posY, 0.0F);  // 1
		
		GlStateManager.glTexCoord2f((float)i / 128.0F, (float)j / 128.0F); // 1
		GlStateManager.glVertex3f(this.posX + f - 1.0F + (float)k, this.posY, 0.0F); // 3
		
		GlStateManager.glTexCoord2f(((float)i + f - 1.0F) / 128.0F, ((float)j + 7.99F) / 128.0F); // 4
		GlStateManager.glVertex3f(this.posX - (float)k, this.posY + 7.99F, 0.0F); // 2
		
		GlStateManager.glTexCoord2f((float)i / 128.0F, ((float)j + 7.99F) / 128.0F); // 2
		GlStateManager.glVertex3f(this.posX + f - 1.0F - (float)k, this.posY + 7.99F, 0.0F); // 4
		
		GlStateManager.glEnd();
		
		return (float)l;
    }
	
	@Override
	protected void doDraw(float f) {
		super.doDraw(f);
		
		posX -= 2*f;
	}
}
