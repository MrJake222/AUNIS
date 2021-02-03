package mrjake.aunis.gui.address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.gui.GuiScreenTextFields;
import mrjake.aunis.packet.gui.address.AddressActionEnum;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
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
public abstract class AbstractAddressChangeGui extends GuiScreenTextFields {

	protected EnumHand hand; 
	protected NBTTagCompound mainCompound;
	protected List<AbstractAddressEntry> addressEntries = new ArrayList<>();
	
	protected final int dispx = 0;
	protected final int dispy = 0;
	
	public AbstractAddressChangeGui(EnumHand hand, ItemStack stack) {
		this.hand = hand;
		this.mainCompound = stack.getTagCompound();
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		super.initGui();
		
		generateAddressEntries();
		reloadAddressEntries(null, 0);
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		
		for (GuiTextField tf : textFields) {
			// This handles saving the name when exiting the gui
			tf.setFocused(false);
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		boolean ctrl = Keyboard.isKeyDown(29);
		
		// Tab
		if (keyCode == 15) {
			for (int i=0; i<textFields.size(); i++) {
				if (textFields.get(i).isFocused()) {
					
					if (ctrl) {
						// If not first
						// Focus on the previous field
						if (i != 0) {
							textFields.get(i).setFocused(false);
							textFields.get(i-1).setFocused(true);
						}
					}
					
					else if (i != textFields.size()-1) {
						// If not last
						// Focus on the next field
						textFields.get(i).setFocused(false);
						textFields.get(i+1).setFocused(true);
					}
					
					break;
				}
			}
		}
	}
	
	protected void reloadAddressEntries(@Nullable AddressActionEnum action, int index) {
		Aunis.info("Reloading action="+action+", index:"+index);
		
		if (action != null) {
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
					
				default:
					// Rename
					break;
			}
		}
		
		textFields.clear();
		buttonList.clear();
		
		for (int i=0; i<addressEntries.size(); i++) {
			AbstractAddressEntry entry = addressEntries.get(i);
			boolean first = (i==0);
			boolean last = (i==addressEntries.size()-1);
			buttonList.addAll(entry.getButtons(buttonList.size(), first, last));
			textFields.addAll(entry.getTextFields(textFields.size()));
		}
	}
	
	private void entriesSwitchPlaces(int a, int b) {
		AbstractAddressEntry entry = addressEntries.get(a);
		addressEntries.set(a, addressEntries.get(b));
		addressEntries.set(b, entry);
	}
	
	protected abstract void generateAddressEntries();
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		int x = dispx;
		int y = dispy;
		
		String nameStr = I18n.format("item.aunis.gui.name");
		String addrStr = I18n.format("item.aunis.gui.address");
		drawString(fontRenderer, addrStr, x+(160-fontRenderer.getStringWidth(addrStr))/2, y, 0x00FFFFFF); x += 170;
		drawString(fontRenderer, nameStr, x+(100-fontRenderer.getStringWidth(nameStr))/2, y, 0x00FFFFFF); x += 100;
		
		for (AbstractAddressEntry entry : addressEntries) {
			entry.render();
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		Aunis.info("actionPerformed: " + button);
		((BetterButton) button).performAction();
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
