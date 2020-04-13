package mrjake.aunis.gui.element;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidTank;

public class FluidTankElement {
		
	private GuiContainer screen;
	private int xCoord;
	private int yCoord;
	private int maxWidth;
	private int maxHeight;
	private FluidTank fluidTank;
		
	public FluidTankElement(GuiContainer screen, int xCoord, int yCoord, int maxWidth, int maxHeight, FluidTank fluidTank) {
		this.screen = screen;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.fluidTank = fluidTank;
	}
	
	public void renderTank() {
		if (fluidTank != null && fluidTank.getFluid() != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
	
			int yBottomCoord = yCoord + maxHeight;
			float fittingBlocks = (maxHeight / 16.0f) * fluidTank.getFluidAmount() / fluidTank.getCapacity();
			
			int maxFullBlocks = (int) fittingBlocks;
			float leftoverHeightScale = fittingBlocks - maxFullBlocks;
			
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidTank.getFluid().getFluid().getStill().toString());
			
			for (int block=0; block<maxFullBlocks; block++) {
				GuiHelper.drawTexturedRect(xCoord, yBottomCoord - block*16 - 16, sprite, sprite.getIconWidth(), sprite.getIconHeight(), 1.0f);
			}
			
			GuiHelper.drawTexturedRectScaled(xCoord, yBottomCoord - maxFullBlocks*16, sprite, sprite.getIconWidth(), sprite.getIconHeight(), leftoverHeightScale);
		}
	}
	
	public void renderTooltip(int mouseX, int mouseY) {
		mouseX -= screen.getGuiLeft();
		mouseY -= screen.getGuiTop();
		
		if (GuiHelper.isPointInRegion(xCoord, yCoord, maxWidth, maxHeight, mouseX, mouseY) && fluidTank != null && fluidTank.getFluid() != null) {
			String amount = String.format("%,d", fluidTank.getFluidAmount());
			String maxAmount = String.format("%,d", fluidTank.getCapacity());
			
			List<String> lines = Arrays.asList(
					fluidTank.getFluid().getFluid().getLocalizedName(null),
					TextFormatting.GRAY + amount + " / " + maxAmount + " mB");
			
			screen.drawHoveringText(lines, mouseX, mouseY);
		}
	}
}
