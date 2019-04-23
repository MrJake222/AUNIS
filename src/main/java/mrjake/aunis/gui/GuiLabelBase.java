package mrjake.aunis.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;

public class GuiLabelBase extends GuiLabel {

	public GuiLabelBase(FontRenderer fontRenderer, int id, int x, int y, int textColor, String text) {
		this(fontRenderer, id, x, y, fontRenderer.getStringWidth(text), 10, textColor);
		
		this.addLine(text);
	}
	
	public GuiLabelBase(FontRenderer fontRendererObj, int id, int x, int y, int width, int height, int textColor) {
		super(fontRendererObj, id, x, y, width, height, textColor);
	}
}
