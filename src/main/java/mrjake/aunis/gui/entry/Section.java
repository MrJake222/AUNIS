package mrjake.aunis.gui.entry;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;

public class Section {
	
	private int width;
	private String unlocalized;

	public Section(int width, String unlocalized) {
		this.width = width;
		this.unlocalized = unlocalized;
	}
	
	public void render(FontRenderer fontRenderer, int x, int y) {
		String str = I18n.format(unlocalized);
		fontRenderer.drawStringWithShadow(str, x+(width-fontRenderer.getStringWidth(str))/2, y, 0x00FFFFFF);
	}
	
	public int getWidth() {
		return width;
	}
}
