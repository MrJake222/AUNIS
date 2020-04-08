package mrjake.aunis.gui.element;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiHelper {
	public static void drawTexturedRectScaled(int xLeftCoord, int yBottomCoord, TextureAtlasSprite textureSprite, int maxWidth, int maxHeight, float scaleHeight) {
		maxHeight *= scaleHeight;
		yBottomCoord -= maxHeight;
		
		drawTexturedRect(xLeftCoord, yBottomCoord, textureSprite, maxWidth, maxHeight, scaleHeight);
	}
	
	public static void drawTexturedRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int maxWidth, int maxHeight, float scaleHeight) {
		double v = textureSprite.getMaxV() - textureSprite.getMinV();
		v *= (1-scaleHeight);
		
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(xCoord + 0), (double)(yCoord + maxHeight), 0).tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xCoord + maxWidth), (double)(yCoord + maxHeight), 0).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xCoord + maxWidth), (double)(yCoord + 0), 0).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV()+v).endVertex();
        bufferbuilder.pos((double)(xCoord + 0), (double)(yCoord + 0), 0).tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV()+v).endVertex();
        tessellator.draw();
    }
	
	public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x + 0), (double)(y + height), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), 0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + 0), 0).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 0), (double)(y + 0), 0).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();
    }
	
	public static boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }
	
	public static void drawTexturedRectWithShadow(int x, int y, int xOffset, int yOffset, int xSize, int ySize, float color) {		
		GlStateManager.enableBlend();
		GlStateManager.color(color, color, color, 1);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, xSize, ySize, xSize, ySize);
		
		GlStateManager.color(color, color, color, 0.2f);
		Gui.drawModalRectWithCustomSizedTexture(x+xOffset, y+yOffset, 0, 0, xSize, ySize, xSize, ySize);
		GlStateManager.disableBlend();
		
		GlStateManager.color(1, 1, 1, 1);
	}
}
