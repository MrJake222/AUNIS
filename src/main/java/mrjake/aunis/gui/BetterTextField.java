package mrjake.aunis.gui;

import mrjake.aunis.gui.BetterButton.ActionCallback;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

/**
 * Adds setText that returns this inststance.
 * 
 * @author MrJake222
 *
 */
public class BetterTextField extends GuiTextField {

	private ActionCallback actionCallback;
	private String originalContent;
	private boolean numbersOnly;
	
	public BetterTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height, String originalContent) {
		super(componentId, fontrendererObj, x, y, width, height);
		this.originalContent = originalContent;
		setText(originalContent);
	}

	public BetterTextField setTextBetter(String textIn) {
		super.setText(textIn);
		return this;
	}

	public BetterTextField setMaxStringLengthBetter(int maxNameLength) {
		super.setMaxStringLength(maxNameLength);
		return this;
	}
	
	public BetterTextField setActionCallback(ActionCallback callback) {
		actionCallback = callback;
		return this;
	}
	
	public BetterTextField setNumbersOnly() {
		this.numbersOnly = true;
		return this;
	}
	
	@Override
	public void writeText(String textToWrite) {
		if (numbersOnly) {
			textToWrite = textToWrite.replaceAll("\\D+","");
		}
		
		super.writeText(textToWrite);
	}
	
	@Override
	public void setFocused(boolean focused) {
		if (isFocused() && !focused && !originalContent.equals(getText())) {
			// Unfocused and changed name
			originalContent = getText();
			actionCallback.performAction();
		}
		
		super.setFocused(focused);
	}
}
