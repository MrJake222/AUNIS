package mrjake.aunis.gui.address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.packet.gui.address.AddressActionEnum;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

/**
 * Class handles universal screen shown when editing Notebook or Universe Dialer
 * saved addresses.
 * 
 * @author MrJake222
 * 
 */
public abstract class AbstractAddressChangeGui extends GuiScreen {

	protected EnumHand hand; 
	protected NBTTagCompound mainCompound;
	protected List<AbstractAddressEntry> addressEntries = new ArrayList<>();
	
	protected int dispx;
	protected int dispy;
	protected int guiHeight;
	
	public AbstractAddressChangeGui(EnumHand hand, ItemStack stack) {
		this.hand = hand;
		this.mainCompound = stack.getTagCompound();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
		
		addressEntries.clear();
		generateAddressEntries();
		
		guiHeight = 20;
		for (AbstractAddressEntry entry : addressEntries) {
			guiHeight += entry.getHeight();
		}
		
		dispx = (width-350)/2;
		dispy = (height-guiHeight)/2;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawGradientRect(dispx-20, dispy-20, dispx+350+40, dispy+guiHeight+40, -0x3FEFEFF0, -0x2FEFEFF0);
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		int x = dispx;
		
		String nameStr = I18n.format("item.aunis.gui.name");
		String addrStr = I18n.format("item.aunis.gui.address");
		drawString(fontRenderer, addrStr, x+(160-fontRenderer.getStringWidth(addrStr))/2, dispy, 0x00FFFFFF); x += 170;
		drawString(fontRenderer, nameStr, x+(100-fontRenderer.getStringWidth(nameStr))/2, dispy, 0x00FFFFFF); x += 100;
		
		int y = 20;
		
		for (AbstractAddressEntry entry : addressEntries) {
			entry.renderAt(dispx, dispy+y, mouseX, mouseY, partialTicks);
			y += entry.getHeight();
		}
		
		// Debug UI layout
//		drawRect(dispx+0, 		dispy, dispx+160, 				dispy+y, 0x80FF0000);
//		drawRect(dispx+160, 	dispy, dispx+160+120, 			dispy+y, 0x8000FF00);
//		drawRect(dispx+160+120, dispy, dispx+160+120+25*2+20, 	dispy+y, 0x800000FF);
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		
		for (AbstractAddressEntry entry : addressEntries) {
			// This handles saving the name when exiting the gui
			entry.nameField.setFocused(false);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (AbstractAddressEntry entry : addressEntries) {
			if (entry.mouseClicked(mouseX, mouseY, mouseButton)) {
				// Click performed some action
				break;
			}
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		for (AbstractAddressEntry entry : addressEntries) {
			entry.keyTyped(typedChar, keyCode);
		}
		
		boolean shift = Keyboard.isKeyDown(42);
		
		// Tab
		if (keyCode == 15) {
			for (int i=0; i<addressEntries.size(); i++) {
				if (addressEntries.get(i).nameField.isFocused()) {
					
					if (shift) {
						// If not first
						// Focus on the previous field
						if (i != 0) {
							addressEntries.get(i).nameField.setFocused(false);
							addressEntries.get(i-1).nameField.setFocused(true);
						}
					}
					
					else if (i != addressEntries.size()-1) {
						// If not last
						// Focus on the next field
						addressEntries.get(i).nameField.setFocused(false);
						addressEntries.get(i+1).nameField.setFocused(true);
					}
					
					break;
				}
			}
		}
	}
	
	@Override
	public void updateScreen() {
		for (AbstractAddressEntry entry : addressEntries) {
			entry.updateScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	protected void actionPerformed(AddressActionEnum action, int index) {		
		switch (action) {
			case MOVE_UP:
				entriesSwitchPlaces(index, index-1);
				break;
		
			case MOVE_DOWN:
				entriesSwitchPlaces(index, index+1);
				break;
				
			case REMOVE:
				addressEntries.remove(index);
				break;
				
			case RENAME:
				break;
		}
	}
	
	private void entriesSwitchPlaces(int a, int b) {
		AbstractAddressEntry entry = addressEntries.get(a);
		addressEntries.set(a, addressEntries.get(b));
		addressEntries.set(b, entry);
		
		// Synchronize indexes
		addressEntries.get(a).index = a;
		addressEntries.get(b).index = b;
	}
	
	protected abstract void generateAddressEntries();
}
