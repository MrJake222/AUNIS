package mrjake.aunis.tileentity;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.gui.container.CapacitorContainerGuiUpdate;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.state.CapacitorPowerLevelUpdate;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class CapacitorTile extends TileEntity implements ITickable, ICapabilityProvider, StateProviderInterface {
	
	// ------------------------------------------------------------------------
	// Loading & ticking
	
	private TargetPoint targetPoint;
	private int powerLevel;
	private int lastPowerLevel;
	
	public int getPowerLevel() {
		return powerLevel;
	}
	
	@Override
	public void onLoad() {
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_UPDATE));
		}
	}
	
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
			
			powerLevel = Math.round(energyStorage.getEnergyStored() / (float)energyStorage.getMaxEnergyStored() * 10);
			if (powerLevel != lastPowerLevel) {
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, getState(StateTypeEnum.RENDERER_UPDATE)), targetPoint);
				
				lastPowerLevel = powerLevel;
			}
			
			energyTransferedLastTick = energyStorage.getEnergyStored() - energyStoredLastTick;
			energyStoredLastTick = energyStorage.getEnergyStored();
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
	
	private int energyStoredLastTick = 0;
	private int energyTransferedLastTick = 0;
	
	public int getEnergyTransferedLastTick() {
		return energyTransferedLastTick;
	}
	
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

	
	// -----------------------------------------------------------------------------
	// State
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_UPDATE:
				return new CapacitorPowerLevelUpdate(powerLevel);
			
			case GUI_UPDATE:
				return new CapacitorContainerGuiUpdate(energyStorage.getEnergyStored(), energyTransferedLastTick);
				
			default:
				return null;
		}
	}


	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_UPDATE:
				return new CapacitorPowerLevelUpdate();
				
			case GUI_UPDATE:
				return new CapacitorContainerGuiUpdate();
				
			default:
				return null;
		}
	}


	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_UPDATE:
				powerLevel = ((CapacitorPowerLevelUpdate) state).powerLevel;
				world.markBlockRangeForRenderUpdate(pos, pos);
				break;
				
			case GUI_UPDATE:
				CapacitorContainerGuiUpdate guiUpdate = (CapacitorContainerGuiUpdate) state;
				energyStorage.setEnergyStored(guiUpdate.energyStored);
				energyTransferedLastTick = guiUpdate.energyTransferedLastTick;
				break;
				
			default:
				break;
		}
	}
}
