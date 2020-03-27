package mrjake.aunis.tileentity;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.stargate.StargateAbstractEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class CapacitorTile extends TileEntity implements ITickable {
	
	// ------------------------------------------------------------------------
	// Loading & ticking
	
	@Override
	public void update() {
		if (!world.isRemote) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				TileEntity tile = world.getTileEntity(pos.offset(facing));
				
				if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
					int extracted = energyStorage.extractEnergy(AunisConfig.powerConfig.stargateMaxEnergyTransfer, true);
					extracted = tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).receiveEnergy(extracted, false);
					
					energyStorage.extractEnergy(extracted, false);
				}
			}
		}
	}
	
	
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
