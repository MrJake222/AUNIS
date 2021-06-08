package mrjake.aunis.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.item.notebook.PageNotebookItem;
import mrjake.aunis.item.notebook.PageNotebookSetNameToServer;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;

public class PageRenameGui extends GuiScreen {

	private int guiLeft;
	private int guiTop;
		
	private List<LabeledTextBox> textBoxes = new ArrayList<>();
	private GuiButton saveButton;
	
	private EnumHand hand;
	private String originalName;
	
	public PageRenameGui(EnumHand hand, ItemStack stack) {
		this.hand = hand;
		originalName = PageNotebookItem.getNameFromCompound(stack.getTagCompound());
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		guiLeft = (this.width - 242) / 2;
		guiTop = (this.height - 64) / 2;
		
		String[] labels = {
				"item.aunis.notebook.rename_gui",
		};
		
		String[] values = new String[labels.length];
		values[0] = originalName;		
		
		for (int i=0; i<textBoxes.size(); i++)
			values[i] = textBoxes.get(i).textField.getText();
		
		textBoxes.clear();
		
		for (int i=0; i<labels.length; i++) {
			LabeledTextBox textBox = new LabeledTextBox(fontRenderer, guiLeft, guiTop, i, 0, 42*i, labels[i]);
			textBox.textField.setText(values[i] != null ? values[i] : "");
			textBoxes.add(textBox);
		}
		
		saveButton = new GuiButton(0, guiLeft+20, guiTop+13+18+12, I18n.format("item.aunis.universe_dialer.oc_save"));
		buttonList.add(saveButton);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		for (LabeledTextBox textBox : textBoxes)
			textBox.draw();
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
		
		AunisPacketHandler.INSTANCE.sendToServer(new PageNotebookSetNameToServer(hand, name));
		keyTyped(' ', 1); // close GUI
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
}
