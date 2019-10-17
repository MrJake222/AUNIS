package mrjake.aunis.stargate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.teleportation.TeleportHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

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
		
		// Aunis.log("Read gates from NBT: " + stargateMap.toString());
	}
		
	public void addStargate(List<EnumSymbol> address, int dimension, BlockPos pos) {	
		if ( !checkForStargate(address) ) {
			stargateMap.put(EnumSymbol.toLong(address), new StargatePos(pos, address.get(address.size()-1), dimension));
			
			markDirty();
		}
	}
	
	public void removeStargate(List<EnumSymbol> address) {
		if ( checkForStargate(address) ) {
			stargateMap.remove(EnumSymbol.toLong(address));
			
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
	
	public boolean checkForStargate(List<EnumSymbol> address) {
		return stargateMap.containsKey(EnumSymbol.toLong(address));
	}
	
	public boolean stargateInWorld(World currentWorld, List<EnumSymbol> address) {
		if (address == null)
			return false;
		
		if (address.size() < 7)
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

	
	public static class StargatePos {
		private BlockPos gatePos;
		private int dimension;
		private EnumSymbol lastSymbol;
		
		public StargatePos(BlockPos pos, EnumSymbol lastSymbol, int dimension) {
			this.gatePos = pos;
			this.lastSymbol = lastSymbol;
			this.dimension = dimension;
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
	}
}
