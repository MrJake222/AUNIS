package mrjake.aunis.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyStorageSerializable extends EnergyStorage implements INBTSerializable<NBTTagCompound> {
	
	public EnergyStorageSerializable(int capacity, int maxTransfer) {
		super(capacity, maxTransfer);
	}
	
	public EnergyStorageSerializable(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
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
		int tx = super.extractEnergy(maxExtract, simulate);
		
		if (tx > 0)
			onEnergyChanged();
		
		return tx;
	}
	
	public void setEnergyStored(int energyStored) {
		this.energy = energyStored;
		
		onEnergyChanged();
	}
	
	protected void onEnergyChanged() {}
}
