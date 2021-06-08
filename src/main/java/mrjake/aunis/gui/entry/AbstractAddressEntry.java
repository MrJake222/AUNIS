package mrjake.aunis.gui.entry;

import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;

public abstract class AbstractAddressEntry extends AbstractEntry {
	
	protected SymbolTypeEnum symbolType;
	protected StargateAddress stargateAddress;
	protected int maxSymbols;
	
	public AbstractAddressEntry(Minecraft mc, int index, int maxIndex, EnumHand hand, String name, ActionListener actionListener, SymbolTypeEnum type, StargateAddress addr, int maxSymbols) {
		super(mc, index, maxIndex, hand, name, actionListener);
		
		this.symbolType = type;
		this.stargateAddress = addr;
		this.maxSymbols = maxSymbols;
	}
}
