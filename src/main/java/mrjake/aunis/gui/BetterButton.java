package mrjake.aunis.gui;

import net.minecraft.client.gui.GuiButton;

public class BetterButton extends GuiButton {

	private ActionCallback actionCallback;
	
	public void performAction() {
		actionCallback.performAction();
	}
	
	public BetterButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}

	
	public BetterButton setFgColor(int fgColor) {
		packedFGColour = fgColor;
		return this;
	}
	
	public BetterButton setActionCallback(ActionCallback callback) {
		actionCallback = callback;
		return this;
	}
	
	public BetterButton setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	public static interface ActionCallback {
		public void performAction();
	}
}