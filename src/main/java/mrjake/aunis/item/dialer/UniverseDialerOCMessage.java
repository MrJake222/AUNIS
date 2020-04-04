package mrjake.aunis.item.dialer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public class UniverseDialerOCMessage implements INBTSerializable<NBTTagCompound> {

	public String name;
	public String address;
	public short port;
	public String dataStr;
	
	public UniverseDialerOCMessage(String name, String address, short port,	String dataStr) {
		this.name = name;
		this.address = address;
		this.port = port;
		this.dataStr = dataStr;
	}
	
	public UniverseDialerOCMessage(NBTTagCompound compound) {
		deserializeNBT(compound);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setString("name", name);
		compound.setString("address", address);
		compound.setShort("port", port);
		compound.setString("data", dataStr);
		
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound compound) {
		name = compound.getString("name");
		address = compound.getString("address");
		port = compound.getShort("port");
		dataStr = compound.getString("data");
	}
	
	public Object[] getData() {
		return dataStr.split(",");
	}
}
