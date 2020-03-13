package mrjake.aunis.gui;

import org.lwjgl.opengl.GL11;

import mrjake.aunis.Aunis;
import mrjake.aunis.fluid.AunisFluids;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class DHDContainerGui extends GuiContainer {

	private DHDContainer container;
	
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Aunis.ModID, "textures/gui/dhd_container.png");
	
	public DHDContainerGui(DHDContainer container) {
		super(container);
		
		this.container = container;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
		if (container.slotCrystal.getHasStack())
			drawTexturedModalRect(guiLeft+76, guiTop+16, 176, 0, 24, 32);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("Upgrades"), 8, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("Cold fusion"), 168-fontRenderer.getStringWidth("Cold fusion")+1, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(new FluidStack(AunisFluids.moltenNaquadahRefined, 1000).getFluid().getStill().toString());

		GlStateManager.color(1, 1, 1, 1);
		drawTexturedModalRect(151, 56, sprite, sprite.getIconWidth(), sprite.getIconHeight());
		drawTexturedModalRectMy(151, 72-16, sprite, sprite.getIconWidth(), sprite.getIconHeight(), 0.8f);
		
		GlStateManager.enableBlend();
		mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
		drawTexturedModalRect(151, 18, 176, 32, 16, 54);
	}
	
	public void drawTexturedModalRectMy(int xLeftCoord, int yBottomCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn, float scaleHeight) {
		heightIn *= scaleHeight;
		
		yBottomCoord -= heightIn;
		
		double v = textureSprite.getMaxV() - textureSprite.getMinV();
		v *= (1-scaleHeight);
				
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(xLeftCoord + 0), (double)(yBottomCoord + heightIn), (double)this.zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xLeftCoord + widthIn), (double)(yBottomCoord + heightIn), (double)this.zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xLeftCoord + widthIn), (double)(yBottomCoord + 0), (double)this.zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV()+v).endVertex();
        bufferbuilder.pos((double)(xLeftCoord + 0), (double)(yBottomCoord + 0), (double)this.zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV()+v).endVertex();
        tessellator.draw();
    }
}
