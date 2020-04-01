package mrjake.aunis.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.item.dialer.ChangeEvent;
import mrjake.aunis.item.dialer.UniverseDialerOCProgramToServer;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class OCMessageGui extends GuiScreen {

	private int guiLeft;
	private int guiTop;
		
	private List<LabeledTextBox> textBoxes = new ArrayList<>(4);
	private GuiButton saveButton;
	private boolean isPortInvalid = false;
	
	@Override
	public void initGui() {		
		guiLeft = (this.width - 242) / 2;
		guiTop = (this.height - 184) / 4;
		
		String[] labels = {
				"item.aunis.universe_dialer.oc_name",
				"item.aunis.universe_dialer.oc_address",
				"item.aunis.universe_dialer.oc_port",
				"item.aunis.universe_dialer.oc_data"
		};
		
		String[] values = new String[4];
		
		for (int i=0; i<textBoxes.size(); i++)
			values[i] = textBoxes.get(i).textField.getText();
		
		textBoxes.clear();
		
		for (int i=0; i<labels.length; i++) {
			LabeledTextBox textBox = new LabeledTextBox(i, 0, 42*i, labels[i]);
			textBox.textField.setText(values[i] != null ? values[i] : "");
			textBoxes.add(textBox);
		}
		
		textBoxes.get(0).textField.setMaxStringLength(10);
		textBoxes.get(1).textField.setMaxStringLength(36);
		
		saveButton = new GuiButton(0, guiLeft+20, guiTop+170, I18n.format("item.aunis.universe_dialer.oc_save"));
		buttonList.add(saveButton);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		for (LabeledTextBox textBox : textBoxes)
			textBox.draw();
		
		if (isPortInvalid)
			drawString(fontRenderer, TextFormatting.DARK_RED + I18n.format("item.aunis.universe_dialer.oc_port_invalid"), guiLeft+230, guiTop+103, 0x00FFFFFF);
	}
	
	private class LabeledTextBox {
		
		private int x;
		private int y;
		private String text;
		private GuiTextField textField;
		
		public LabeledTextBox(int id, int x, int y, String translationKey) {
			this.x = x;
			this.y = y;
			this.text = I18n.format(translationKey);
			this.textField = new GuiTextField(id, fontRenderer, guiLeft+x, guiTop+y+13, 240, 18);
		}
		
		public void draw() {
			drawString(fontRenderer, text, guiLeft+x, guiTop+y, 0x00FFFFFF);
			textField.drawTextBox();
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		for (LabeledTextBox tf : textBoxes)
			tf.textField.textboxKeyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		for (LabeledTextBox tf : textBoxes)
			tf.textField.updateCursorCounter();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (LabeledTextBox tf : textBoxes)
			tf.textField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		String name = textBoxes.get(0).textField.getText();
		String address = textBoxes.get(1).textField.getText();
		String portStr = textBoxes.get(2).textField.getText();
		String data = textBoxes.get(3).textField.getText();
		int port = 0;
		
		try {
			port = Integer.valueOf(portStr);
			if (port < 0 || port > 65535)
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			isPortInvalid = true;
			return;
		}
		
		isPortInvalid = false;
		
		AunisPacketHandler.INSTANCE.sendToServer(new UniverseDialerOCProgramToServer(ChangeEvent.getHand(), name, address, (short) port, data));
		keyTyped(' ', 1); // close GUI
	}
}
