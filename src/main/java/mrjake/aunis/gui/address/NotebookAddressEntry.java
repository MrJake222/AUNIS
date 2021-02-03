package mrjake.aunis.gui.address;

import mrjake.aunis.packet.gui.address.AddressDataTypeEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;

public class NotebookAddressEntry extends AbstractAddressEntry {
	
	public NotebookAddressEntry(Minecraft mc, int index, int maxIndex, EnumHand hand, String name2, SymbolTypeEnum type, StargateAddress addr, int maxSymbols, ActionListener reloadListener) {
		super(mc, index, maxIndex, hand, name2, type, addr, maxSymbols, reloadListener);
	}


	@Override
	public void renderAt(int dx, int dy, int mouseX, int mouseY, float partialTicks) {
		super.renderAt(dx, dy, mouseX, mouseY, partialTicks);
		
		final int size = 20;
		int x = dx+(160-size*(maxSymbols))/2;
		
		for (int i=0; i<maxSymbols; i++) {
			SymbolInterface symbol = stargateAddress.get(i);	
			
			renderSymbol(x, dy-6, size, size, symbol);
			x += size;
		}
	}
	
	@Override
	protected int getHeight() {
		return 25;
	}
	
	@Override
	protected int getButtonOffset() {
		return -7;
	}
	
	@Override
	protected int getMaxNameLength() {
		return 11;
	}
	
	@Override
	protected AddressDataTypeEnum getAddressDataType() {
		return AddressDataTypeEnum.PAGE;
	}
}
