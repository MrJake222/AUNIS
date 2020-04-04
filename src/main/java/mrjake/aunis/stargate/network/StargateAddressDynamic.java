package mrjake.aunis.stargate.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import net.minecraft.nbt.NBTTagCompound;

public class StargateAddressDynamic extends StargateAddress {

	public StargateAddressDynamic(SymbolTypeEnum symbolType) {
		super(symbolType);
	}
	
	public StargateAddressDynamic(NBTTagCompound compound) {
		super(compound);
	}
	
	@Override
	protected int getSavedSymbols() {
		return Math.min(addressSize, 9);
	}
	
	// ---------------------------------------------------------------------------------
	// Address
		
	public void addSymbol(SymbolInterface symbol) {		
		if (address.size() == 9) {
			Aunis.logger.error("Tried to add symbol to already full address");
			return;
		}
		
		address.add(symbol);
		addressSize += 1;
	}
	
	public void addAll(StargateAddress stargateAddress) {
		if (address.size()+stargateAddress.address.size() > 9) {
			Aunis.logger.error("Tried to add symbols to already populated address");
			return;
		}
		
		address.addAll(stargateAddress.address);
		addressSize += stargateAddress.address.size();
	}
	
	public void addAll(List<SymbolInterface> stargateAddress) {
		if (address.size()+stargateAddress.size() > 9) {
			Aunis.logger.error("Tried to add symbols to already populated address");
			return;
		}
		
		address.addAll(stargateAddress);
		addressSize += stargateAddress.size();
	}	

	public void addOrigin() {
		if (symbolType.hasOrigin()) {
			addSymbol(symbolType.getOrigin());
		}
	}
	
	public void clear() {
		address.clear();
		addressSize = 0;
	}

	public int size() {
		return address.size();
	}

	public boolean contains(SymbolInterface symbol) {
		return address.contains(symbol);
	}
	
	public boolean validate() {
		return symbolType.validateDialedAddress(this);
	}

	public StargateAddress toImmutable() {
		StargateAddress stargateAddress = new StargateAddress(symbolType);
		stargateAddress.address.addAll(address);
		return stargateAddress;
	}
	
	// ---------------------------------------------------------------------------------
	// Serialization
	
	private int addressSize;
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = super.serializeNBT();
		
		compound.setInteger("size", address.size());
		
		return compound;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		addressSize = compound.getInteger("size");
		
		super.deserializeNBT(compound);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(address.size());

		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		addressSize = buf.readInt();
		
		super.fromBytes(buf);
	}
}
