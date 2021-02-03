package mrjake.aunis.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiScreenTextFields extends GuiScreen {
	protected List<GuiTextField> textFields = new ArrayList<>();
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		for (GuiTextField tf : textFields) {
			tf.drawTextBox();
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		for (GuiTextField tf : textFields) {
			tf.textboxKeyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		for (GuiTextField tf : textFields) {
			tf.updateCursorCounter();
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (GuiTextField tf : textFields) {
			tf.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
}
