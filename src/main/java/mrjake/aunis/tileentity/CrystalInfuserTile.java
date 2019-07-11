package mrjake.aunis.tileentity;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.capability.EnergyStorageSerializable;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.infuser.EnergyStoredToClient;
import mrjake.aunis.packet.infuser.ShouldRenderWavesToClient;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.crystalinfuser.CrystalInfuserRenderer;
import mrjake.aunis.renderer.state.CrystalInfuserRendererState;
import mrjake.aunis.renderer.state.RendererState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CrystalInfuserTile extends TileEntity implements ITileEntityRendered, ITickable {
	
	private boolean firstTick = true;
	
	private int ticksBufferEmpty = 0;
	
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
			
			// Client
			if (world.isRemote) {
//				Aunis.info("TileUpdateRequestToServer " + pos.toString());
				
				AunisPacketHandler.INSTANCE.sendToServer(new RendererUpdateRequestToServer(pos, Aunis.proxy.getPlayerInMessageHandler(null)));
			}
		}
		
		ItemStack crystalItemStack = inventory.getStackInSlot(0);
		
		if (!crystalItemStack.isEmpty()) {
			IEnergyStorage crystalEnergyStorage = crystalItemStack.getCapability(CapabilityEnergy.ENERGY, null);
			
			TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			
			int rx = energyStorage.extractEnergy(AunisConfig.powerConfig.dhdCrystalMaxEnergyTransfer, true);
			boolean stopWaveRender = false;
			
			if (rx > 0) {
				ticksBufferEmpty = 0;
				rx = crystalEnergyStorage.receiveEnergy(rx, false);
				
				if (rx > 0) {		
					energyStorage.extractEnergy(rx, false);
					
					getInfuserRendererState().energyStored = crystalEnergyStorage.getEnergyStored();
					AunisPacketHandler.INSTANCE.sendToAllTracking(new EnergyStoredToClient(pos, getInfuserRendererState().energyStored), point);
					
					if (!getInfuserRendererState().renderWaves) {
						getInfuserRendererState().renderWaves = true;
						AunisPacketHandler.INSTANCE.sendToAllTracking(new ShouldRenderWavesToClient(pos, getInfuserRendererState().renderWaves), point);
					}
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
				if (getInfuserRendererState().renderWaves) {
					getInfuserRendererState().renderWaves = false;
					AunisPacketHandler.INSTANCE.sendToAllTracking(new ShouldRenderWavesToClient(pos, getInfuserRendererState().renderWaves), point);
				}
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		
		getInfuserRendererState().toNBT(compound);
		
		compound.setTag("energy", energyStorage.serializeNBT());
		compound.setTag("inventory", inventory.serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		
		getRendererState().fromNBT(compound);
		
		if (compound.hasKey("energy"))
			energyStorage.deserializeNBT((NBTTagCompound) compound.getTag("energy"));
		
		if (compound.hasKey("inventory"))
			inventory.deserializeNBT((NBTTagCompound) compound.getTag("inventory"));
		
		super.readFromNBT(compound);
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
				getInfuserRendererState().energyStored = -1;
				getInfuserRendererState().renderWaves = false;
			}
			
			else			
				getInfuserRendererState().energyStored = stack.getCapability(CapabilityEnergy.ENERGY, null).getEnergyStored();
			
			TargetPoint point = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			AunisPacketHandler.INSTANCE.sendToAllTracking(new EnergyStoredToClient(pos, getInfuserRendererState().energyStored), point);
//			AunisPacketHandler.INSTANCE.sendToAllAround(new ShouldRenderWavesToClient(pos, getInfuserRendererState().renderWaves), point);
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
	
	// ------------------------------------------------------------------------
	// Renderer
	
	CrystalInfuserRenderer renderer;
	CrystalInfuserRendererState rendererState;
	
	@Override
	public ISpecialRenderer<CrystalInfuserRendererState> getRenderer() {
		if (renderer == null)
			renderer = new CrystalInfuserRenderer(this);
		
		return renderer;
	}

	@Override
	public RendererState getRendererState() {	
		if (rendererState == null)
			rendererState = new CrystalInfuserRendererState();
				
		return rendererState;
	}
	
	public CrystalInfuserRendererState getInfuserRendererState() {	
		return (CrystalInfuserRendererState) getRendererState();
	}
	
	public RendererState createRendererState(ByteBuf buf) {
		return new CrystalInfuserRendererState().fromBytes(buf);
	}
}
