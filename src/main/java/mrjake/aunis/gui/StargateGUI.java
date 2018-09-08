package mrjake.aunis.gui;

import java.util.List;

import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.addressUpdate.GateAddressRequestToServer;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class StargateGUI extends GuiScreen {
	
	private int color;
	
	private StargateBaseTile gateTile;
	private List<EnumSymbol> gateAddress;
	
	public StargateGUI(StargateBaseTile gateTile, int sections) {
		this.gateTile = gateTile;
		this.gateAddress = gateTile.gateAddress;
		
		if (gateAddress == null) {
			AunisPacketHandler.INSTANCE.sendToServer( new GateAddressRequestToServer(gateTile.getPos()) );
		}
		
		this.sections = sections;
		bgWidth = sectionSize*sections + (sections+1)*frameThickness;
		imageWidth = bgWidth + 2*frameThickness;
	}

	private int sections;
	
	private final int sectionSize = 90;
	private final int frameThickness = 8;
	
	// 6 symbols, 5 spaces, 2 margins
	// private final int bgWidth = sectionSize*6 + 7*frameThickness;
	private final int bgWidth;
	private final int bgHeight = sectionSize + 2*frameThickness;
		
	// private final int imageWidth = bgWidth + 2*frameThickness;
	private final int imageWidth;
	private final int imageHeight = bgHeight + 2*frameThickness;
	
	private final boolean drawSymbolBackground = false;
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
				
		if (width < imageWidth) {
			GlStateManager.translate( (width - imageWidth/2f)/2f, (height - imageHeight/2f)/2f, 0 );
			GlStateManager.scale( 0.5, 0.5, 0 );
		}
		else 
			GlStateManager.translate( (width - imageWidth)/2f, (height - imageHeight)/2f, 0 );
		
		setColor(24, 26, 31, 255);
		frame(imageWidth, imageHeight, frameThickness);
		
		setColor(39, 43, 51, 242);
		drawRect(frameThickness, frameThickness, bgWidth+frameThickness, bgHeight+frameThickness, color);
		
		if (gateAddress != null) {
			for (int i=0; i<sections; i++) {
				String name = gateAddress.get(i).name;
				Minecraft.getMinecraft().getTextureManager().bindTexture( new ResourceLocation("aunis:textures/gui/symbol/" + name.toLowerCase() + ".png") );
				
				int firstPos = frameThickness*2;
				int x = firstPos + (sectionSize+frameThickness)*i;
				
				if (drawSymbolBackground) {
					setColor(128, 128, 128, 75);
					drawRect(x, firstPos, sectionSize+x, sectionSize+firstPos, color);
					
					GlStateManager.color(1, 1, 1, 1);
				}
				
				GlStateManager.enableBlend();
				
				drawModalRectWithCustomSizedTexture(x, firstPos, 0, 0, sectionSize, sectionSize, sectionSize, sectionSize);
				
				setColor(78, 86, 102, 255);
				fontRenderer.drawString(name, x + (sectionSize - fontRenderer.getStringWidth(name))/2, firstPos + sectionSize - frameThickness, color);		
				
				GlStateManager.disableBlend();
			}
		}
		
		else {
			gateAddress = gateTile.gateAddress;
		}
		
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void frame(int w, int h, int thickness) {
		// Up
		drawRect(0, 0, w, thickness, color);
		
		// Down
		drawRect(0, h-thickness, w, h, color);
				
		// Left
		drawRect(0, thickness, thickness, h-thickness, color);
		
		// Right
		drawRect(w-thickness, thickness, w, h-thickness, color);
	}
	
	private void setColor(int red, int green, int blue, int alpha) {
		color = color( red, green, blue, alpha );
	}
	
	private int color(int red, int green, int blue, int alpha) {
		alpha = (alpha & 0xFF) << 24;
		red = (red & 0xFF) << 16;
		green = (green & 0xFF) << 8;
		blue = (blue & 0xFF);
		
		return alpha | red | green | blue;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
