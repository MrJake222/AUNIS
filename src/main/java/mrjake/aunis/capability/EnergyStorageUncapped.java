package mrjake.aunis.capability;

public class EnergyStorageUncapped extends EnergyStorageSerializable {

	public EnergyStorageUncapped(int capacity, int maxTransfer) {
		super(capacity, maxTransfer);
	}
	
	public int extractEnergyUncapped(int maxExtract) {
		int extract = Math.min(maxExtract, energy);
		
		energy -= extract;
		
		return extract;
	}
}
