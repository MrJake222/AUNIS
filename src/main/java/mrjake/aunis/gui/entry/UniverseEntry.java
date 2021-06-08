package mrjake.aunis.gui.entry;

import mrjake.aunis.packet.gui.entry.EntryDataTypeEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;

public class UniverseEntry extends AbstractAddressEntry {
	
	public static final int ADDRESS_WIDTH = 80;
	public static final int BUTTON_COUNT = 3;
	
	public UniverseEntry(Minecraft mc, int index, int maxIndex, EnumHand hand, String name, ActionListener reloadListener, SymbolTypeEnum type, StargateAddress addr, int maxSymbols) {
		super(mc, index, maxIndex, hand, name, reloadListener, type, addr, maxSymbols);
	}

	
	@Override
	public void renderAt(int dx, int dy, int mouseX, int mouseY, float partialTicks) {
		final int width = 15;
		final int height = width*2;
		final int xSpacing = 10;

		final int addressWidth = xSpacing*maxSymbols + 5;

		int x = dx+(ADDRESS_WIDTH-addressWidth)/2;
		for (int i=0; i<maxSymbols; i++) {
			SymbolInterface symbol = stargateAddress.get(i);	
			
			renderSymbol(x, dy, width, height, symbol);
			x += xSpacing;
		}
		
		// Debug symbols
//		x = dx+(maxAddressWidth-addressWidth)/2;
//		Gui.drawRect(x, dy, x+addressWidth, dy+height, 0x80000000 | (0xFF << 8*(colorId%3))); colorId++;
		
		super.renderAt(dx+ADDRESS_WIDTH+10, dy+5, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected int getHeight() {
		return 30;
	}
	
	@Override
	protected int getMaxNameLength() {
		return 16;
	}
	
	@Override
	protected EntryDataTypeEnum getEntryDataType() {
		return EntryDataTypeEnum.UNIVERSE;
	}
}
