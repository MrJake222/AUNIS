package mrjake.aunis.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.teleportation.TeleportHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
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
	
	private List<Long> orlinGatesOverworldAddresses = new ArrayList<Long>();
	
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
			compound.setInteger("netherGate7th", netherGateAddress.get(6).id);
		}
		
		compound.setInteger("orlinSize", orlinGatesOverworldAddresses.size());
		for (int j=0; j<orlinGatesOverworldAddresses.size(); j++) {
			compound.setLong("orlin"+j, orlinGatesOverworldAddresses.get(j));
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
			netherGateAddress = new ArrayList<EnumSymbol>(7);
			
			netherGateAddress = EnumSymbol.toSymbolList(EnumSymbol.fromLong(compound.getLong("netherGateAddress")));
			netherGateAddress.add(EnumSymbol.valueOf(compound.getInteger("netherGate7th")));
		}
		
		int orlinSize = compound.getInteger("orlinSize");
		orlinGatesOverworldAddresses.clear();
		
		for (int j=0; j<orlinSize; j++) {
			orlinGatesOverworldAddresses.add(compound.getLong("orlin"+j));
		}
		
		// Aunis.log("Read gates from NBT: " + stargateMap.toString());
	}
	
	public void setNetherGate(List<EnumSymbol> address) {
		netherGateAddress = new ArrayList<EnumSymbol>(7);
		netherGateAddress.addAll(address);
		
		markDirty();
	}
	
	@Nullable
	public List<EnumSymbol> getNetherAddress() {
		return netherGateAddress;
	}
	
	public boolean isNetherGateGenerated() {
		return netherGateAddress != null;
	}
	
	public void addOrlinAddress(List<EnumSymbol> address) {
		orlinGatesOverworldAddresses.add(EnumSymbol.toLong(address));
		
		markDirty();
	}
	
	public void removeOrlinAddress(List<EnumSymbol> address) {
		orlinGatesOverworldAddresses.remove(EnumSymbol.toLong(address));
		
		markDirty();
	}
	
	public List<Long> getOrlinAddressList() {
		return orlinGatesOverworldAddresses;
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
	
	public boolean stargateInWorld(World currentWorld, List<EnumSymbol> address) {
		if (address == null)
			return false;
		
		if (address.size() < 7 || address.get(address.size()-1) != EnumSymbol.ORIGIN)
			return false;
		
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
			}
			
			// Cross dimensional dial
			else {
				
				// No need to check dimension
				return true;
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
