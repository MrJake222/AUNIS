package mrjake.aunis.gui.address;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import mrjake.aunis.Aunis;
import mrjake.aunis.gui.BetterButton;
import mrjake.aunis.gui.BetterTextField;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gui.address.AddressDataTypeEnum;
import mrjake.aunis.packet.gui.address.AddressActionEnum;
import mrjake.aunis.packet.gui.address.AddressActionToServer;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class AbstractAddressEntry {

	protected FontRenderer fontRenderer;
	protected int dx;
	protected int dy;
	protected int index;
	protected EnumHand hand;
	protected String name;
	protected SymbolTypeEnum symbolType;
	protected StargateAddress stargateAddress;
	protected int maxSymbols;
	private RefreshListener refreshListener;
	
	protected GuiTextField nameField;
	
	public AbstractAddressEntry(FontRenderer fontRenderer, int dx, int dy, int index, EnumHand hand, String name, SymbolTypeEnum symbolType, StargateAddress stargateAddress, int maxSymbols, RefreshListener reloadListener) {
		this.fontRenderer = fontRenderer;
		this.dx = dx;
		this.dy = dy;
		this.index = index;
		this.hand = hand;
		this.name = name;
		this.symbolType = symbolType;
		this.stargateAddress = stargateAddress;
		this.maxSymbols = maxSymbols;
		this.refreshListener = reloadListener;
	}
	
	public void render() {
//		fontRenderer.drawString(TextFormatting.GRAY + name, dx+160, dy, 0x00FFFFFF);
	}
	
	protected List<GuiButton> getButtons(int id, boolean first, boolean last) {
		List<GuiButton> list = new ArrayList<>();
		int x = dx+100+160+20;
		int y = dy+getButtonOffset();
		
//		list.add(new BetterButton(id++, x, y, 60, 20, I18n.format("item.aunis.gui.rename")).setActionCallback(() -> rename())); x += 65;
		list.add(new BetterButton(id++, x, y, 20, 20, "▲").setEnabled(!first).setFgColor(GuiUtils.getColorCode('a', true)).setActionCallback(() -> action(AddressActionEnum.MOVE_UP))); x += 25;
		list.add(new BetterButton(id++, x, y, 20, 20, "▼").setEnabled(!last).setFgColor(GuiUtils.getColorCode('c', true)).setActionCallback(() -> action(AddressActionEnum.MOVE_DOWN))); x += 25;
		list.add(new BetterButton(id++, x, y, 20, 20, "x").setFgColor(GuiUtils.getColorCode('c', true)).setActionCallback(() -> action(AddressActionEnum.REMOVE))); x += 25;
		
		return list;
	}
	
	protected List<GuiTextField> getTextFields(int id) {
		List<GuiTextField> list = new ArrayList<>();
		int y = dy+getButtonOffset();
		
		nameField = new GuiTextField(id++, fontRenderer, dx+160+10, y, 100, 20) {
			@Override
			public void setFocused(boolean focused) {				
				if (isFocused() && !focused && !name.equals(getText())) {
					name = getText();
					action(AddressActionEnum.RENAME);
				}
				
				super.setFocused(focused);
			}
		};
		
		nameField.setText(name);
		nameField.setMaxStringLength(getMaxNameLength());
		list.add(nameField);
		
		return list;
	}
	
	protected void action(AddressActionEnum action) {
		Aunis.info("reload");
		AunisPacketHandler.INSTANCE.sendToServer(new AddressActionToServer(hand, getAddressDataType(), action, index, nameField.getText()));
		
		if (action.shouldRefresh) {
			refreshListener.refresh(action, index);
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
	
	static interface RefreshListener {
		public void refresh(AddressActionEnum action, int index);
	}
}
