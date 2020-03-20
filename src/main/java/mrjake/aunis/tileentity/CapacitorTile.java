package mrjake.aunis.tileentity;

import mrjake.aunis.stargate.StargateAbstractEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class CapacitorTile extends TileEntity {
	
	// ------------------------------------------------------------------------
	// NBT

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("energyStorage", energyStorage.serializeNBT());
				
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		energyStorage.deserializeNBT(compound.getCompoundTag("energyStorage"));
		
		super.readFromNBT(compound);
	}
	
	
	// -----------------------------------------------------------------------------
	// Power system
		
	private StargateAbstractEnergyStorage energyStorage = new StargateAbstractEnergyStorage() {
		
		@Override
		protected void onEnergyChanged() {
			markDirty();
		}
	};
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY) || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(energyStorage);
		}
		
		return super.getCapability(capability, facing);
	}
}
