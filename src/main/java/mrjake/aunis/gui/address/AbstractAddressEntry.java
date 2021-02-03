package mrjake.aunis.gui.address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gui.address.AddressDataTypeEnum;
import mrjake.aunis.packet.gui.address.AddressActionEnum;
import mrjake.aunis.packet.gui.address.AddressActionToServer;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class AbstractAddressEntry {
	
	protected Minecraft mc;
	protected int index;
	protected int maxIndex;
	protected EnumHand hand;
	protected String name;
	protected SymbolTypeEnum symbolType;
	protected StargateAddress stargateAddress;
	protected int maxSymbols;
	
	private ActionListener actionListener;
	
	protected GuiTextField nameField;
	protected BetterButton upButton;
	protected BetterButton downButton;
	protected BetterButton removeButton;
	
	protected List<BetterButton> buttons = new ArrayList<>();
	protected List<GuiTextField> textFields = new ArrayList<>();
	
	public AbstractAddressEntry(Minecraft mc, int index, int maxIndex, EnumHand hand, String name2, SymbolTypeEnum type, StargateAddress addr, int maxSymbols, ActionListener actionListener) {
		this.mc = mc;
		this.index = index;
		this.maxIndex = maxIndex;
		this.hand = hand;
		this.name = name2;
		this.symbolType = type;
		this.stargateAddress = addr;
		this.maxSymbols = maxSymbols;
		this.actionListener = actionListener;
		
		// ----------------------------------------------------------------------------------------------------
		// Text fields
		
		int tId = 0;
		nameField = new GuiTextField(tId++, mc.fontRenderer, 0, 0, 100, 20) {
			@Override
			public void setFocused(boolean focused) {
				if (isFocused() && !focused && !name.equals(getText())) {
					// Unfocused and changed name
					name = getText();
					action(AddressActionEnum.RENAME);
				}
				
				super.setFocused(focused);
			}
		};
		
		nameField.setText(name);
		nameField.setMaxStringLength(getMaxNameLength());
		textFields.add(nameField);
		
		
		// ----------------------------------------------------------------------------------------------------
		// Buttons
		
		int bId = 0;
		upButton = new BetterButton(bId++, 0, 0, 20, 20, "▲")
				.setFgColor(GuiUtils.getColorCode('a', true))
				.setActionCallback(() -> action(AddressActionEnum.MOVE_UP));
		
		downButton = new BetterButton(bId++, 0, 0, 20, 20, "▼")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> action(AddressActionEnum.MOVE_DOWN));
		
		removeButton = new BetterButton(bId++, 0, 0, 20, 20, "x")
				.setFgColor(GuiUtils.getColorCode('c', true))
				.setActionCallback(() -> action(AddressActionEnum.REMOVE));
		
		buttons.add(upButton);
		buttons.add(downButton);
		buttons.add(removeButton);
	}
	
	public void renderAt(int dx, int dy, int mouseX, int mouseY, float partialTicks) {
		dy += getButtonOffset();
		
		// Fields
		nameField.x = dx+160+10;
		nameField.y = dy;
		
		for (GuiTextField tf : textFields) {
			tf.drawTextBox();
		}
		
		
		// Buttons
		boolean first = (index == 0);
		boolean last = (index == maxIndex-1);
		upButton.enabled = !first;
		downButton.enabled = !last;
		
		int x = dx+280;
		
		for (GuiButton btn : buttons) {
			btn.x = x;
			btn.y = dy;
			btn.drawButton(mc, mouseX, mouseY, partialTicks);
			
			x += 25;
		}
	}
	
	
	// ----------------------------------------------------------------------------------------------------
	// Actions
	
	protected void action(AddressActionEnum action) {
		AunisPacketHandler.INSTANCE.sendToServer(new AddressActionToServer(hand, getAddressDataType(), action, index, nameField.getText()));
		actionListener.action(action, index);
	}
	
	
	// ----------------------------------------------------------------------------------------------------
	// Interactions
	
	/**
	 * Called on mouse clicked on every instance of {@link AbstractAddressEntry}
	 * @return {@code true} when an action was performed, {@code false} if no element was activated.
	 */
	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0)
			return false;

		for (BetterButton btn : buttons) {
			if (btn.mousePressed(mc, mouseX, mouseY)) {
				// Mouse pressed inside of this button
				btn.playPressSound(this.mc.getSoundHandler());
				btn.performAction();
				
				return true;
			}
		}
		
		for (GuiTextField tf : textFields) {
			if (tf.mouseClicked(mouseX, mouseY, mouseButton)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {		
		for (GuiTextField tf : textFields) {
			tf.textboxKeyTyped(typedChar, keyCode);
		}
	}
	
	public void updateScreen() {		
		for (GuiTextField tf : textFields) {
			tf.updateCursorCounter();
		}
	}
	
	protected abstract int getHeight();	
	protected abstract int getButtonOffset();
	protected abstract int getMaxNameLength();
	protected abstract AddressDataTypeEnum getAddressDataType();
	
	protected static void renderSymbol(int x, int y, int sizeX, int sizeY, SymbolInterface symbol) {
		GlStateManager.enableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
		GlStateManager.color(0.77f, 0.77f, 0.77f, 1);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(symbol.getIconResource());		

		Gui.drawScaledCustomSizeModalRect(x, y, 0, 0, 256, 256, sizeX, sizeY, 256, 256);
		
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
	}
	
	static interface ActionListener {
		public void action(AddressActionEnum action, int index);
	}
}
