package mrjake.aunis.item.notebook;

import org.lwjgl.opengl.GL11;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.item.renderer.AunisFontRenderer;
import mrjake.aunis.item.renderer.ItemRenderHelper;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

public class PageRenderer {
	
	public static void renderSymbol(float x, float y, float w, float h, SymbolInterface symbol) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.color(0, 0, 0, (float) AunisConfig.avConfig.glyphTransparency);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(symbol.getIconResource());		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(0.04f + x, 0.79f - y, 0.011f);
		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(0.04f + x + w, 0.79f - y, 0.011f);
		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(0.04f + x + w, 0.79f - y + h, 0.011f); // 0.2
		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.04f + x, 0.79f - y + h, 0.011f);
		
	    GL11.glEnd();
	    
	    // Render shadow
// 		GlStateManager.color(0, 0, 0, 0.2f);
//	    GL11.glBegin(GL11.GL_QUADS);
//		
//		x += symbol.getSymbolType() == SymbolTypeEnum.PEGASUS ?  0.008f : 0.01f;
//		y += symbol.getSymbolType() == SymbolTypeEnum.PEGASUS ?  0.008f : 0.01f;
//		
//		GL11.glTexCoord2f(0, 1); GL11.glVertex3f(0.04f + x, 0.79f - y, 0.01f);
//		GL11.glTexCoord2f(1, 1); GL11.glVertex3f(0.04f + x + w, 0.79f - y, 0.01f);
//		GL11.glTexCoord2f(1, 0); GL11.glVertex3f(0.04f + x + w, 0.79f - y + h, 0.01f); // 0.2
//		GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.04f + x, 0.79f - y + h, 0.01f);
//		
//	    GL11.glEnd();
	}
	
	public static void renderByCompound(TransformType lastTransform, NBTTagCompound compound) {	
		float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
		GlStateManager.pushMatrix();
		
		if (lastTransform == TransformType.FIXED) {
			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.translate(-0.91, -0.07, -0.5);
			float scale = 1.15f;
			GlStateManager.scale(scale, scale, scale);
		}
		
		else {
			boolean mainhand = lastTransform == TransformType.FIRST_PERSON_RIGHT_HAND;
//			Aunis.info("mainhand: " + AunisItems.PAGE_NOTEBOOK_ITEM.getLastTransform());
			EnumHandSide handSide = mainhand ? EnumHandSide.RIGHT : EnumHandSide.LEFT;
			
			GlStateManager.pushMatrix();
			GlStateManager.scale(20,20,20);		
			ItemRenderHelper.applyBobbing(partialTicks);
			
			ItemRenderHelper.renderArmFirstPersonSide(0, handSide, 0, null);
		    GlStateManager.popMatrix();
		    
		    float narrow = AunisConfig.avConfig.pageNarrowing;
		    GlStateManager.translate(mainhand ? 0.5f-narrow : -0.25f+narrow, 0.2f, 0);
		}
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("aunis:textures/gui/notebook_background.png"));		
	    GL11.glBegin(GL11.GL_QUADS);
		
	    GL11.glTexCoord2f(0, 0); GL11.glVertex3f(0.0f, 0.0f, 0.0f);
	    GL11.glTexCoord2f(0.5f, 0); GL11.glVertex3f(0.7f, 0.0f, 0.0f);
	    GL11.glTexCoord2f(0.5f, 0.71875f); GL11.glVertex3f(0.7f, 1.0f, 0.0f);
	    GL11.glTexCoord2f(0, 0.71875f); GL11.glVertex3f(0.0f, 1.0f, 0.0f);
		
	    GL11.glEnd();
			
		SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(compound.getInteger("symbolType"));
		StargateAddress stargateAddress = new StargateAddress(compound.getCompoundTag("address"));
		int maxSymbols = symbolType.getMaxSymbolsDisplay(compound.getBoolean("hasUpgrade"));
		
		for (int i=0; i<maxSymbols; i++) {
			if (symbolType == SymbolTypeEnum.UNIVERSE) {
				float x = 0.10f*(i%6);
				float y = 0.20f*(i/6) + 0.18f;
				
				renderSymbol(x, y, 0.095f, 0.2f, stargateAddress.get(i));
			}
			
			else {
				float x = 0.21f*(i%3);
				float y = 0.20f*(i/3) + 0.14f;
				
				renderSymbol(x, y, 0.2f, 0.2f, stargateAddress.get(i));
			}
		}
		
//		if (symbolType == SymbolTypeEnum.UNIVERSE)
//			renderSymbol(0.26f, 0.74f, 0.095f, 0.2f, symbolType.getOrigin());
//		else
//			renderSymbol(0.21f, 0.74f, 0.2f, 0.2f, symbolType.getOrigin());
		
		String name = PageNotebookItem.getNameFromCompound(compound);
		
		float scale = 0.009f;
		float width = Minecraft.getMinecraft().fontRenderer.getStringWidth(name) * scale;
		// 0.10 - 0.71 = 0.61 text space
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.10f + (0.61f - width)/2, 0.935f, 0.011f);
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.scale(scale, scale, scale);
		AunisFontRenderer.getFontRenderer().drawString(name, 0, 0, 0x383228, false);
		GlStateManager.popMatrix();
		
	    GlStateManager.popMatrix();
	}
}
