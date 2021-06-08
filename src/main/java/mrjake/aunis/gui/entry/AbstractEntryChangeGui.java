package mrjake.aunis.gui.entry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.packet.gui.entry.EntryActionEnum;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

/**
 * Class handles universal screen shown when editing Notebook or Universe Dialer
 * saved addresses.
 * 
 * @author MrJake222
 * 
 */
public abstract class AbstractEntryChangeGui extends GuiScreen {

	public static final int PADDING = 25;
	
	protected EnumHand hand; 
	protected NBTTagCompound mainCompound;
	protected List<AbstractEntry> entries = new ArrayList<>();
	protected List<Section> sections = new ArrayList<>();
	
	protected int dispx;
	protected int dispy;
	protected int guiWidth;
	protected int guiHeight;
	
	protected boolean firstRun = true;
	
	public AbstractEntryChangeGui(EnumHand hand, NBTTagCompound compound) {
		this.hand = hand;
		this.mainCompound = compound;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
		
		if (firstRun) {
			firstRun = false;
			
			entries.clear();
			generateEntries();
			
			sections.clear();
			generateSections();
			
			guiWidth = 0;
			for (Section section : sections)
				guiWidth += section.getWidth() + 10; // Margin
			
			guiWidth -= 10; // Last margin unnecessary
			
			calculateGuiHeight();
		}
				
		dispx = PADDING + (width-guiWidth)/2;
		dispy = PADDING + (height-guiHeight)/2;
	}
	
	protected void calculateGuiHeight() {
		guiHeight = 14;
		for (AbstractEntry entry : entries)
			guiHeight += entry.getHeight() + getEntryBottomMargin(); // Margin
		
		guiHeight -= getEntryBottomMargin(); // Last margin unnecessary
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawGradientRect(dispx-PADDING, dispy-PADDING, dispx+guiWidth+PADDING, dispy+guiHeight+PADDING, -0x3FEFEFF0, -0x2FEFEFF0);
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		int x = dispx;
		for (Section section : sections) {
			section.render(fontRenderer, x, dispy);
			x += section.getWidth() + 10;
		}
		
		int y = dispy+14;
		for (AbstractEntry entry : entries) {
			entry.renderAt(dispx, y, mouseX, mouseY, partialTicks);
			y += entry.getHeight() + getEntryBottomMargin(); // 6;
		}
		
		// Debug UI layout
		// Sections
//		int colorId = 0;
//		x = dispx;
//		for (Section section : sections) {
//			drawRect(x, dispy, x+section.getWidth(), dispy+guiHeight, 0x80000000 | (0xFF << 8*(colorId%3)));
//			x += section.getWidth()+10;
//			colorId++;
//		}
		
		// Entries
//		int colorId = 0;
//		y = dispy+14;
//		for (AbstractEntry entry : entries) {
//			drawRect(dispx, y, dispx+guiWidth, y+entry.getHeight(), 0x80000000 | (0xFF << 8*(colorId%3)));
//			y += entry.getHeight() + getEntryBottomMargin();
//			colorId += 1;
//		}
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		
		for (AbstractEntry entry : entries) {
			// This handles saving the name when exiting the gui
			for (GuiTextField tf : entry.textFields) {
				tf.setFocused(false);
			}
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		for (AbstractEntry entry : entries) {
			if (entry.mouseClicked(mouseX, mouseY, mouseButton)) {
				// Click performed some action
				break;
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		for (AbstractEntry entry : entries) {
			entry.keyTyped(typedChar, keyCode);
		}
		
		boolean shift = Keyboard.isKeyDown(42);
		
		// Tab
		if (keyCode == 15) {
			for (int i=0; i<entries.size(); i++) {
				if (entries.get(i).nameField.isFocused()) {
					
					if (shift) {
						// If not first
						// Focus on the previous field
						if (i != 0) {
							entries.get(i).nameField.setFocused(false);
							entries.get(i-1).nameField.setFocused(true);
						}
					}
					
					else if (i != entries.size()-1) {
						// If not last
						// Focus on the next field
						entries.get(i).nameField.setFocused(false);
						entries.get(i+1).nameField.setFocused(true);
					}
					
					break;
				}
			}
		}
	}
	
	@Override
	public void updateScreen() {
		for (AbstractEntry entry : entries) {
			entry.updateScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	protected void performAction(EntryActionEnum action, int index) {		
		switch (action) {
			case MOVE_UP:
				entriesSwitchPlaces(index, index-1);
				break;
		
			case MOVE_DOWN:
				entriesSwitchPlaces(index, index+1);
				break;
				
			case REMOVE:
				entries.remove(index);
				
				if (entries.size() == 0) {
					// Close gui
					this.mc.displayGuiScreen((GuiScreen)null);
					if (this.mc.currentScreen == null) {
		                this.mc.setIngameFocus();
		            }
				}
				
				// Synchronize indexes
				for (int i=index; i<entries.size(); i++) {
					entries.get(i).index = i;
				}
				
				calculateGuiHeight();
				break;
				
			case RENAME:
				break;
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		((BetterButton) button).performAction();
	}
	
	private void entriesSwitchPlaces(int a, int b) {
		AbstractEntry entry = entries.get(a);
		entries.set(a, entries.get(b));
		entries.set(b, entry);
		
		// Synchronize indexes
		entries.get(a).index = a;
		entries.get(b).index = b;
	}
	
	protected abstract void generateEntries();
	protected abstract void generateSections();
	protected abstract int getEntryBottomMargin();
}
