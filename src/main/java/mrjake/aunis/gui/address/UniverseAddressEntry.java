package mrjake.aunis.gui.address;

import mrjake.aunis.gui.address.AbstractAddressEntry.RefreshListener;
import mrjake.aunis.packet.gui.address.AddressDataTypeEnum;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumHand;

public class UniverseAddressEntry extends AbstractAddressEntry {
	
	public UniverseAddressEntry(FontRenderer fontRenderer, int dx, int dy, int index, EnumHand hand, String name, SymbolTypeEnum symbolType, StargateAddress stargateAddress, int maxSymbols, RefreshListener reloadListener) {
		super(fontRenderer, dx, dy, index, hand, name, symbolType, stargateAddress, maxSymbols, reloadListener);
	}

	
	@Override
	public void render() {
		super.render();
		
		final int xSpace = 10;
		final int width = 15;
		final int height = width*2;

		int x = dx+(160-xSpace*(maxSymbols+1))/2;
		
		for (int i=0; i<maxSymbols; i++) {
			SymbolInterface symbol = stargateAddress.get(i);	
			
			renderSymbol(x, dy-6, width, height, symbol);
			x += xSpace;
		}
	}
	
	@Override
	protected int getHeight() {
		return 30;
	}
	
	@Override
	protected int getButtonOffset() {
		return 0;
	}
	
	@Override
	protected int getMaxNameLength() {
		return 16;
	}
	
	@Override
	protected AddressDataTypeEnum getAddressDataType() {
		return AddressDataTypeEnum.UNIVERSE;
	}
}
