package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.capability.EnergyStorageSerializable;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.renderer.crystalinfuser.CrystalInfuserRenderer;
import mrjake.aunis.state.CrystalInfuserRendererState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tesr.SpecialRendererProviderInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CrystalInfuserTile extends TileEntity implements ITickable, SpecialRendererProviderInterface, StateProviderInterface {
		
	private int ticksBufferEmpty = 0;
	
	@Override
	public void update() {		
		ItemStack crystalItemStack = inventory.getStackInSlot(0);
		
		if (!crystalItemStack.isEmpty()) {
			IEnergyStorage crystalEnergyStorage = crystalItemStack.getCapability(CapabilityEnergy.ENERGY, null);
			
			int rx = energyStorage.extractEnergy(AunisConfig.powerConfig.dhdCrystalMaxEnergyTransfer, true);
			boolean stopWaveRender = false;
			
			if (rx > 0) {
				ticksBufferEmpty = 0;
				rx = crystalEnergyStorage.receiveEnergy(rx, false);
				
				if (rx > 0) {		
					energyStorage.extractEnergy(rx, false);
					
					rendererState.renderWaves = true;
					rendererState.energyStored = crystalEnergyStorage.getEnergyStored();
					AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_STATE, rendererState), targetPoint);
				}
				
				// Cannot insert energy, crystal full
				else
					stopWaveRender = true;
			}
			
			// Cannot extract, buffer empty
			else
//				stopWaveRender = true;
				ticksBufferEmpty++;
			
			
//			Aunis.info("ticksBufferEmpty: " + ticksBufferEmpty);
			
			if (stopWaveRender || ticksBufferEmpty >= 20) {
				if (rendererState.renderWaves) {
					rendererState.renderWaves = false;
					AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_STATE, rendererState), targetPoint);
				}
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		compound.setTag("rendererState", rendererState.serializeNBT());
		
		compound.setTag("energy", energyStorage.serializeNBT());
		compound.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		try {
			rendererState.deserializeNBT(compound.getCompoundTag("rendererState"));
		}
		
		catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.info("Exception at reading RendererState");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}
		
		if (compound.hasKey("energy"))
			energyStorage.deserializeNBT(compound.getCompoundTag("energy"));
		
		if (compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		
		super.readFromNBT(compound);
	}
	
	private TargetPoint targetPoint;
	
	@Override
	public void onLoad() {		
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		}
		
		else {
			renderer = new CrystalInfuserRenderer(this);
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_STATE));
		}
	}
	
	// ------------------------------------------------------------------------
	// Renderer
	
	private CrystalInfuserRenderer renderer;
	private CrystalInfuserRendererState rendererState = new CrystalInfuserRendererState();
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		x += 0.50;
		z += 0.50;
		
		renderer.render(x, y, z, partialTicks);
	}
	
	
	// ------------------------------------------------------------------------
	// Power buffer
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return rendererState;
			
			default:
				return null;
		}
	}
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new CrystalInfuserRendererState();
				
			default:
				return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				renderer.setRendererState((CrystalInfuserRendererState) state);
				break;
		
			default:
				break;
		}
	}
	
	// ------------------------------------------------------------------------
	// Power buffer
	EnergyStorageSerializable energyStorage = new EnergyStorageSerializable(AunisConfig.powerConfig.dhdCrystalMaxEnergyTransfer * 4, AunisConfig.powerConfig.dhdCrystalMaxEnergyTransfer * 4) {
		protected void onEnergyChanged() {			
			markDirty();
		};
	};
	
	// ------------------------------------------------------------------------
	// Item storage
	private ItemStackHandler inventory = new ItemStackHandler(1) {
		
		protected void onContentsChanged(int slot) {			
			markDirty();
			
			ItemStack stack = this.getStackInSlot(slot);
			
			if (stack.isEmpty()) {
				rendererState.doCrystalRender = false;
			} else {
				rendererState.doCrystalRender = true;
				rendererState.renderWaves = false;
				rendererState.energyStored = stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
			}
			
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_STATE, rendererState), targetPoint);
		};
	};
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return	capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
				capability == CapabilityEnergy.ENERGY ||
				super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T)inventory;
		
		else if (capability == CapabilityEnergy.ENERGY && facing == EnumFacing.DOWN)
			return (T) energyStorage;
		
		else
			return super.getCapability(capability, facing);	
	}
}
