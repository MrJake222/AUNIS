package mrjake.aunis.stargate.power;

import mrjake.aunis.config.AunisConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import net.minecraftforge.energy.EnergyStorage;

public class StargateAbstractEnergyStorage extends EnergyStorage implements INBTSerializable<NBTTagCompound> {

	public StargateAbstractEnergyStorage() {
		super(AunisConfig.powerConfig.stargateEnergyStorage/4, AunisConfig.powerConfig.stargateMaxEnergyTransfer, 0);
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		
		tagCompound.setInteger("energy", this.energy);
		
		return tagCompound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		if (nbt != null) {
			if (nbt.hasKey("energy")) {
				this.energy = nbt.getInteger("energy");
			}
		}
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int rx = super.receiveEnergy(maxReceive, simulate);
		
		if (rx > 0)
			onEnergyChanged();
		
		return rx;
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int energyExtracted = Math.min(energy, maxExtract);
		
		if (!simulate) {
			energy -= energyExtracted;
			onEnergyChanged();
		}
		
		return energyExtracted;
	}
	
	public void setEnergyStored(int energyStored) {
		this.energy = Math.min(energyStored, capacity);
		
		onEnergyChanged();
	}
	
	protected void onEnergyChanged() {}
}
