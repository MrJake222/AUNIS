package mrjake.aunis.transportrings;

import mrjake.aunis.Aunis;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class RingsNetwork extends WorldSavedData {
	public static final String DATA_NAME = Aunis.ModID + "_RingsNetworkData";
	
	public RingsNetwork() {
		super(DATA_NAME);
	}
	
	public static RingsNetwork get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		RingsNetwork instance = (RingsNetwork) storage.getOrLoadData(RingsNetwork.class, DATA_NAME);
				
		if (instance == null) {
			instance = new RingsNetwork();
			storage.setData(DATA_NAME, instance);
		}
		
		return instance;
	}
	
	// ---------------------------------------------------------------------------------

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		
		return compound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
	}
	
	// ---------------------------------------------------------------------------------
//	public void add
}
