package mrjake.aunis.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * Adds setText that returns this inststance.
 * 
 * @author MrJake222
 *
 */
public class BetterTextField extends GuiTextField {

	public BetterTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {
		super(componentId, fontrendererObj, x, y, width, height);
	}

	public BetterTextField setTextBetter(String textIn) {
		super.setText(textIn);
		return this;
	}

	public GuiTextField setMaxStringLengthBetter(int maxNameLength) {
		super.setMaxStringLength(maxNameLength);
		return this;
	}	
}
