package mrjake.aunis.capability.endpoint;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class ItemEndpointStorage implements IStorage<ItemEndpointInterface> {

	@Override
	public NBTBase writeNBT(Capability<ItemEndpointInterface> capability, ItemEndpointInterface instance, EnumFacing side) {
		return new NBTTagCompound();
	}

	@Override
	public void readNBT(Capability<ItemEndpointInterface> capability, ItemEndpointInterface instance, EnumFacing side, NBTBase nbt) {
		
	}

}
