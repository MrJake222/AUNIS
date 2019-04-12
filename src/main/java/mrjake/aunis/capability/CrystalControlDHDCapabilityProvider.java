package mrjake.aunis.capability;

import mrjake.aunis.AunisConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;

public class CrystalControlDHDCapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {

	private EnergyStorageSerializable energyStorage;
	
	public CrystalControlDHDCapabilityProvider() {		
		energyStorage = new EnergyStorageSerializable(AunisConfig.dhdCrystalEnergyStorage, AunisConfig.dhdCrystalMaxEnergyTransfer * 10);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY ? (T)energyStorage : null);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound compound = energyStorage.serializeNBT();
				
		return compound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound tagCompound) {
		energyStorage.deserializeNBT(tagCompound);
	}

}
