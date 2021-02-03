package mrjake.aunis.gui.address;

import mrjake.aunis.gui.address.AbstractAddressEntry.RefreshListener;
import mrjake.aunis.packet.gui.address.AddressDataTypeEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumHand;

public class NotebookAddressEntry extends AbstractAddressEntry {
	
	public NotebookAddressEntry(FontRenderer fontRenderer, int dx, int dy, int index, EnumHand hand, String name, SymbolTypeEnum symbolType, StargateAddress stargateAddress, int maxSymbols, RefreshListener reloadListener) {
		super(fontRenderer, dx, dy, index, hand, name, symbolType, stargateAddress, maxSymbols, reloadListener);
	}


	@Override
	public void render() {
		super.render();
		
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
