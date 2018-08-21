package mrjake.aunis.stargate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.Aunis;
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
	
	private Map<Long, BlockPos> stargateMap = new HashMap<Long, BlockPos>();
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger( "size", stargateMap.size() );
		
		int i = 0;
		for ( Map.Entry<Long, BlockPos> entry : stargateMap.entrySet() ) {
			
			compound.setLong( "key"+i, entry.getKey() );
			compound.setLong( "value"+i, entry.getValue().toLong() );
			
			i++;
		}
		
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		int size = compound.getInteger("size");
		
		for (int i=0; i<size; i++) {
			long key = compound.getLong("key"+i);
			BlockPos value = BlockPos.fromLong( compound.getLong("value"+i) );
			
			stargateMap.put(key, value);
		}
		
		Aunis.log("Read gates from NBT: " + stargateMap.toString());
	}
		
	public void addStargate(List<EnumSymbol> address, BlockPos pos) {	
		if ( !checkForStargate(address) ) {
			stargateMap.put(EnumSymbol.toLong(address), pos);
			
			markDirty();
		}
	}
	
	public void removeStargate(List<EnumSymbol> address) {
		if ( checkForStargate(address) ) {
			stargateMap.remove( EnumSymbol.toLong(address) );
			
			markDirty();
		}
	}
	
	public void clear() {
		stargateMap.clear();
		
		markDirty();
	}
	
	@Override
	public String toString() {
		return stargateMap.toString();
	}
	
	public BlockPos getStargate(List<EnumSymbol> address) {
		return stargateMap.get( EnumSymbol.toLong(address) );
	}
	
	public boolean checkForStargate(List<EnumSymbol> address) {
		return stargateMap.containsKey( EnumSymbol.toLong(address) );
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

	

	
	
}
