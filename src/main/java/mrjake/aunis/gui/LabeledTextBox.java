package mrjake.aunis.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class LabeledTextBox {
	
	private FontRenderer fontRenderer;
	private int guiLeft;
	private int guiTop;
	
	private int x;
	private int y;
	private String text;
	public GuiTextField textField;
	
	public LabeledTextBox(FontRenderer fontRenderer, int guiLeft, int guiTop, int id, int x, int y, String translationKey) {
		this.fontRenderer = fontRenderer;
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		
		this.x = x;
		this.y = y;
		this.text = I18n.format(translationKey);
		this.textField = new GuiTextField(id, fontRenderer, guiLeft+x, guiTop+y+13, 240, 18);
	}
	
	public void draw() {
		fontRenderer.drawStringWithShadow(text, guiLeft+x, guiTop+y, 0x00FFFFFF);
		textField.drawTextBox();
	}
}
