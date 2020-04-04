package mrjake.aunis.capability;

import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;

public class CapacitorCapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {

	private StargateAbstractEnergyStorage energyStorage;
	
	public CapacitorCapabilityProvider() {		
		energyStorage = new StargateAbstractEnergyStorage();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(energyStorage) : null);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {				
		return energyStorage.serializeNBT();
	}

	@Override
	public void deserializeNBT(NBTTagCompound tagCompound) {
		energyStorage.deserializeNBT(tagCompound);
	}

}
