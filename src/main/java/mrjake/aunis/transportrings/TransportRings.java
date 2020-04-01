package mrjake.aunis.transportrings;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Defines one, specific ring platform
 * Contains their address, name, BlockPos etc.
 * 
 * Is NBT serializable and should be used to save ring data to NBT
 * 
 * @author MrJake
 *
 */
public class TransportRings {
	
	/**
	 * Rings address
	 */
	private int address;
	public int getAddress() { return address; }
	public void setAddress(int address) { this.address = address; }
	
	/**
	 * Rings display name
	 */
	private String name;
	
	public String getName() {
		if (name == null)
			return "[empty]";
		
		return name;
	}
	
	public void setName(String name) { this.name = name; }
	
	/**
	 * BlockPos of the main block
	 */
	private BlockPos pos;
	public BlockPos getPos() { return pos; }
	
	/**
	 * Distance to rings, set by cloning.
	 * 
	 * It is only saved to NBT in clones, as main object has no distance
	 */
	private double distance;
	public double getDistance() { return distance; }
	
	/**
	 * Defines if the object is a clone
	 */
	private boolean isClone;
	
	/**
	 * Called when new tile entity is created by world(first block placement),
	 * by reading NBT data(main tile or clones)
	 * 
	 * @param pos - mandatory, points to rings base block
	 */
	public TransportRings(BlockPos pos) {		
		this(-1, null, pos, false);
	}
	
	/**
	 * Used only for menu client-side
	 * 
	 * @param address
	 * @param name
	 */
	public TransportRings(int address, String name) {		
		this(address, name, new BlockPos(0,0,0), false);
	}
	
	/**
	 * NBT version of the constructor.
	 * @param compound {@link NBTTagCompound} read from NBT.
	 */
	public TransportRings(NBTTagCompound compound) {
		deserializeNBT(compound);
	}
	
	/**
	 * Called internally
	 */
	private TransportRings(int address, String name, BlockPos pos, boolean isClone) {
		this.address = address;
		this.name = name;
		this.pos = pos;
		
		this.isClone = isClone;
	}
	
	/**
	 * Returns new instance of this object with specified distance to the
	 * rings requiring the clone
	 */
	public TransportRings cloneWithNewDistance(BlockPos callerPos) {
		return new TransportRings(address, name, pos, true).setDistanceTo(callerPos);
	}
	
	/**
	 * Checks if address has been set(not equal to -1)
	 * 
	 * @return should put rings on map
	 */
	public boolean isInGrid() {
		return address != -1;
	}
	
	/**
	 * Sets this rings distance to caller position
	 * 
	 * @param pos - caller position
	 * @return this instance
	 */
	private TransportRings setDistanceTo(BlockPos pos) {
		distance = this.pos.getDistance(pos.getX(), pos.getY(), pos.getZ());
		
		return this;
	}
	
	/**
	 * Saves data of this ring
	 * 
	 * @return new tag compound
	 */
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		if (address != -1)
			compound.setInteger("address", address);
		
		if (name != null)
			compound.setString("name", name);
		
		compound.setLong("pos", pos.toLong());
		
		if (isClone)
			compound.setDouble("distance", distance);
		
		return compound;
	}
	
	public TransportRings deserializeNBT(NBTTagCompound compound) {
		if (compound.hasKey("address"))
			address = compound.getInteger("address");
		else
			address = -1;
		
		if (compound.hasKey("name"))
			name = compound.getString("name");
		
		pos = BlockPos.fromLong(compound.getLong("pos"));
		
		if (compound.hasKey("distance")) {
			isClone = true;
			
			distance = compound.getDouble("distance");
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		return "[pos="+pos.toString()+", address="+address+", name="+name+"]";
	}
}
