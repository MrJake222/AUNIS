package mrjake.aunis.stargate.power;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.energy.IEnergyStorage;

public class StargateClassicEnergyStorage extends StargateAbstractEnergyStorage {
	
	private List<IEnergyStorage> storages = new ArrayList<IEnergyStorage>();
	
	public StargateClassicEnergyStorage(int capacity, int maxTransfer) {
		super(capacity, maxTransfer);
	}
	
	public StargateClassicEnergyStorage() {
		super();
	}

	public void clearStorages() {
		storages.clear();
	}
	
	public void addStorage(IEnergyStorage storage) {		
		storages.add(storage);
	}
	
	@Override
	public int getEnergyStored() {
		int energyStored = this.energy;
		
		for (IEnergyStorage storage : storages)
			energyStored += storage.getEnergyStored();
		
		return energyStored;
	}
	
	@Override
	public int getMaxEnergyStored() {		
		int maxEnergyStored = this.capacity;
		
		for (IEnergyStorage storage : storages)
			maxEnergyStored += storage.getMaxEnergyStored();
		
		return maxEnergyStored;
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int toReceive = maxReceive;
		toReceive -= super.receiveEnergy(maxReceive, simulate);
		
		for (IEnergyStorage storage : storages) {
			if (toReceive == 0)
				return maxReceive;
			
			toReceive -= storage.receiveEnergy(toReceive, simulate);
		}
		
		return maxReceive - toReceive;
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int toExtract = maxExtract;
		
		for (IEnergyStorage storage : storages) {
			if (toExtract == 0)
				return maxExtract;
			
			toExtract -= storage.extractEnergy(toExtract, simulate);
		}
		
		toExtract -= super.extractEnergy(toExtract, simulate);
		return maxExtract - toExtract;
	}
	
	public void setEnergyStoredInternally(int energy) {
		this.energy = energy;
	}
	
	public int getEnergyStoredInternally() {
		return this.energy;
	}

	public int getMaxEnergyStoredInternally() {
		return this.capacity;
	}
}
