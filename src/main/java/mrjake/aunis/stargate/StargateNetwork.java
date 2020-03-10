package mrjake.aunis.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.teleportation.TeleportHelper;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.INBTSerializable;

public class StargateNetwork extends WorldSavedData {	
	public static final String DATA_NAME = Aunis.ModID + "_StargateNetworkData";
	
	public StargateNetwork() {
		super(DATA_NAME);
	}
	
	public StargateNetwork(String name) {
		super(name);
	}
	
	// Long - Base address - 6 symbols serialized
	// StargatePos - BlockPos/int Object
	private Map<Long, StargatePos> stargateMap = new HashMap<Long, StargatePos>();
	
	@Nullable
	private List<EnumSymbol> netherGateAddress = null;
	
	@Nullable
	private List<EnumSymbol> lastActivatedOrlin = null;
		
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger( "size", stargateMap.size() );
		
		int i = 0;
		for ( Map.Entry<Long, StargatePos> entry : stargateMap.entrySet() ) {
			
			compound.setLong(    "addr"+i, entry.getKey() );
			compound.setLong(    "pos"+i, entry.getValue().getPos().toLong() );
			compound.setInteger(    "last"+i, entry.getValue().get7thSymbol().id );
			compound.setInteger( "dim"+i, entry.getValue().getDimension() );
			
			i++;
		}
		
		if (netherGateAddress != null) {
			compound.setLong("netherGateAddress", EnumSymbol.toLong(netherGateAddress));
//			compound.setInteger("netherGate7th", netherGateAddress.get(6).id);
		}
		
		if (lastActivatedOrlin != null) {
			compound.setLong("lastActivatedOrlin", EnumSymbol.toLong(lastActivatedOrlin));
		}
		
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		int size = compound.getInteger("size");
		stargateMap.clear();
		
		for (int i=0; i<size; i++) {
			long addr = compound.getLong("addr"+i);
			BlockPos pos = BlockPos.fromLong( compound.getLong("pos"+i) );
			EnumSymbol last = EnumSymbol.valueOf( compound.getInteger("last"+i) );
			int dim = compound.getInteger("dim"+i);
			
			stargateMap.put(addr, new StargatePos(pos, last, dim));
		}
		
		if (compound.hasKey("netherGateAddress")) {			
			netherGateAddress = EnumSymbol.toSymbolList(EnumSymbol.fromLong(compound.getLong("netherGateAddress")));
//			netherGateAddress.add(EnumSymbol.valueOf(compound.getInteger("netherGate7th")));
		}
		
		if (compound.hasKey("lastActivatedOrlin")) {			
			lastActivatedOrlin = EnumSymbol.toSymbolList(EnumSymbol.fromLong(compound.getLong("lastActivatedOrlin")));
		}
		
		// Aunis.log("Read gates from NBT: " + stargateMap.toString());
	}
	
	public void setNetherGate(List<EnumSymbol> address) {
		netherGateAddress = new ArrayList<EnumSymbol>(6);
		netherGateAddress.addAll(address.subList(0, 6));
				
		markDirty();
	}
	
	@Nullable
	public List<EnumSymbol> getNetherAddress() {
		return netherGateAddress;
	}
	
	public boolean isNetherGateGenerated() {
		return netherGateAddress != null;
	}
	
//	public void addOrlinAddress(List<EnumSymbol> address) {
//		orlinGatesOverworldAddresses.add(EnumSymbol.toLong(address));
//		
//		markDirty();
//	}
//	
//	public void removeOrlinAddress(List<EnumSymbol> address) {
//		orlinGatesOverworldAddresses.remove(EnumSymbol.toLong(address));
//		
//		markDirty();
//	}
//	
//	public List<Long> getOrlinAddressList() {
//		return orlinGatesOverworldAddresses;
//	}
	
	public void setLastActivatedOrlinAddress(List<EnumSymbol> address) {
		lastActivatedOrlin = new ArrayList<EnumSymbol>(6);
		lastActivatedOrlin.addAll(address.subList(0, 6));
		
		markDirty();
	}
	
	@Nullable
	public List<EnumSymbol> getLastActivatedOrlinAddress() {
		return lastActivatedOrlin;
	}
	
	public boolean hasLastActivatedOrlinAddress() {
		return lastActivatedOrlin != null;
	}
	
	public void addStargate(List<EnumSymbol> address, int dimension, BlockPos pos) {
		if ( !checkForStargate(address) ) {
			stargateMap.put(EnumSymbol.toLong(address), new StargatePos(pos, address.get(address.size()-1), dimension));
			
			markDirty();
		}
	}
		
	public void removeStargate(List<EnumSymbol> address) {
		removeStargate(EnumSymbol.toLong(address));
	}
	
	public void removeStargate(long address) {
		if (checkForStargate(address)) {
			stargateMap.remove(address);
			
			markDirty();
		}
	}
	
	public Map<Long, StargatePos> queryStargates() {
		return stargateMap;
	}
	
	@Override
	public String toString() {
		return stargateMap.toString();
	}
	
	public StargatePos getStargate(List<EnumSymbol> address) {
		return stargateMap.get( EnumSymbol.toLong(address) );
	}
	
	public boolean checkForStargate(long address) {
		return stargateMap.containsKey(address);
	}
	
	public boolean checkForStargate(List<EnumSymbol> address) {
		return checkForStargate(EnumSymbol.toLong(address));
	}
	
	public boolean isAddressReserved(List<EnumSymbol> address) {
		long serialized = EnumSymbol.toLong(address);
		
		return serialized == EARTH_ADDRESS_SERIALIZED;
	}
	
	public static final List<EnumSymbol> EARTH_ADDRESS = Arrays.asList(
			EnumSymbol.AURIGA,
			EnumSymbol.CETUS,
			EnumSymbol.CENTAURUS,
			EnumSymbol.CANCER,
			EnumSymbol.SCUTUM,
			EnumSymbol.ERIDANUS,
			EnumSymbol.ORIGIN);
	
	private static final long EARTH_ADDRESS_SERIALIZED = EnumSymbol.toLong(EARTH_ADDRESS);
	
	public boolean stargateInWorld(World currentWorld, List<EnumSymbol> address) {		
		if (address == null)
			return false;
		
		if (address.size() < 7 || address.get(address.size()-1) != EnumSymbol.ORIGIN)
			return false;
		
		if (address.equals(EARTH_ADDRESS) && currentWorld.provider.getDimensionType() == DimensionType.NETHER) {
			return lastActivatedOrlin != null;
		}
		
		StargatePos stargatePos = stargateMap.get(EnumSymbol.toLong(address));
		
		// Gate exists
		// NOT checking 7th symbol or dimension
		if (stargatePos != null) {
			
			// Local dial
			if (address.size() == 7) {
				
				// Same dimension
				if (currentWorld.provider.getDimension() == stargatePos.getDimension()) {
					return true;
				}
				
				// Travelling from Overworld to Nether or vice-versa
				if (currentWorld.provider.getDimensionType() == DimensionType.NETHER || stargatePos.getDimension() == DimensionType.OVERWORLD.getId() ||
					currentWorld.provider.getDimensionType() == DimensionType.OVERWORLD || stargatePos.getDimension() == DimensionType.NETHER.getId())
					return true;
				
			}
			
			// Cross dimensional dial
			else {
				
				// No need to check dimension, only check last symbol
				return address.get(6) == getStargate(address).lastSymbol;
			}
		}
		
		return false;
	}
	
	public static StargateNetwork get(World world) {
		MapStorage storage = world.getMapStorage();
		StargateNetwork instance = (StargateNetwork) storage.getOrLoadData(StargateNetwork.class, DATA_NAME);
				
		if (instance == null) {
			instance = new StargateNetwork();
			storage.setData(DATA_NAME, instance);
		}
		
		return instance;
	}

	
	public static class StargatePos implements INBTSerializable<NBTTagCompound> {		
		private BlockPos gatePos;
		private int dimension;
		private EnumSymbol lastSymbol;
		
		public StargatePos(BlockPos pos, EnumSymbol lastSymbol, int dimension) {
			this.gatePos = pos;
			this.lastSymbol = lastSymbol;
			this.dimension = dimension;
		}
		
		public StargatePos(NBTTagCompound compound) {
			deserializeNBT(compound);
		}
				
		public BlockPos getPos() {
			return gatePos;
		}
		
		public int getDimension() {
			return dimension;
		}
		
		public World getWorld() {
			return TeleportHelper.getWorld(dimension);
		}
		
		public StargateAbstractBaseTile getTileEntity() {
			return (StargateAbstractBaseTile) getWorld().getTileEntity(gatePos);
		}
		
		public EnumSymbol get7thSymbol() {
			return lastSymbol;
		}
		
		@Override
		public String toString() {
			return gatePos+" in dim:"+dimension+", last: "+lastSymbol;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			
			compound.setLong("pos", gatePos.toLong());
			compound.setInteger("last", lastSymbol.id);
			compound.setInteger("dim", dimension);
			
			return compound;
		}

		@Override
		public void deserializeNBT(NBTTagCompound compound) {
			gatePos = BlockPos.fromLong(compound.getLong("pos"));
			lastSymbol = EnumSymbol.valueOf(compound.getInteger("last"));
			dimension = compound.getInteger("dim");
		}
	}
}
