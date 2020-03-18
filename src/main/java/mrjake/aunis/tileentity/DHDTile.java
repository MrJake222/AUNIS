package mrjake.aunis.tileentity;

import java.util.Arrays;
import java.util.List;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.fluid.AunisFluids;
import mrjake.aunis.gui.container.DHDContainerGuiUpdate;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.renderer.DHDRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.DHDActivateButtonState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.util.ReactorStateEnum;
import mrjake.aunis.util.ILinkable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DHDTile extends TileEntity implements ILinkable, StateProviderInterface, ITickable {
	
	// ---------------------------------------------------------------------------------------------------
	// Gate linking
	
	private BlockPos linkedGate = null;
	
	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = world.getBlockState(pos);
		
		int rotationOrig = state.getValue(AunisProps.ROTATION_HORIZONTAL);
		world.setBlockState(pos, state.withProperty(AunisProps.ROTATION_HORIZONTAL, rotation.rotate(rotationOrig, 16)));
	}
	
	public void setLinkedGate(BlockPos gate) {		
		this.linkedGate = gate;
		
		markDirty();
	}
	
	@Override
	public boolean isLinked() {
		return this.linkedGate != null;
	}
	
	public StargateAbstractBaseTile getLinkedGate(World world) {
		if (linkedGate == null)
			return null;
		
		return (StargateAbstractBaseTile) world.getTileEntity(linkedGate);
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// Renderer state
	
	private DHDRendererState rendererStateClient;
	
	public DHDRendererState getRendererStateClient() {
		return rendererStateClient;
	}
	
	// ---------------------------------------------------------------------------------------------------
	// Loading and ticking
	
	private TargetPoint targetPoint;
	private ReactorStateEnum reactorState = ReactorStateEnum.STANDBY;
	
	public ReactorStateEnum getReactorState() {
		return reactorState;
	}
	
	@Override
	public void onLoad() {
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_STATE));
		}
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			
			// Has crystal
			if (!itemStackHandler.getStackInSlot(0).isEmpty()) {
				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(world);
					IEnergyStorage energyStorage = (IEnergyStorage) gateTile.getCapability(CapabilityEnergy.ENERGY, null);
					
					if (energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored() >= AunisConfig.dhdConfig.energyPerNaquadah) {
						
						// Has fuel
						if (fluidHandler.drainInternal(1 * AunisConfig.dhdConfig.powerGenerationMultiplier, false) != null) {
							fluidHandler.drainInternal(1 * AunisConfig.dhdConfig.powerGenerationMultiplier, true);
							energyStorage.receiveEnergy(AunisConfig.dhdConfig.energyPerNaquadah * AunisConfig.dhdConfig.powerGenerationMultiplier, false);
							
							reactorState = ReactorStateEnum.ONLINE;
						}
						
						// No fuel
						else {
							reactorState = ReactorStateEnum.NO_FUEL;
						}
					}
					
					// Buffer full
					else {
						reactorState = ReactorStateEnum.STANDBY;
					}
				}
				
				// Not linked
				else {
					reactorState = ReactorStateEnum.NOT_LINKED;
				}
			}
			
			// No crystal
			else {
				reactorState = ReactorStateEnum.NO_FUEL;
			}
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Symbol activation
	
	public void activateSymbol(EnumSymbol symbol) {	
		if (symbol == EnumSymbol.BRB)
			AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.DHD_MILKYWAY_PRESS_BRB, 0.5f);
		else
			AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.DHD_MILKYWAY_PRESS, 0.5f);
		
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(symbol)), targetPoint);
	}
	
	public void clearSymbols() {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.DHD_ACTIVATE_BUTTON, new DHDActivateButtonState(true)), targetPoint);
	}
	
	
	// -----------------------------------------------------------------------------
	// States
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				if (isLinked()) {
					StargateAbstractBaseTile gateTile = getLinkedGate(world);
					List<Integer> symbols = EnumSymbol.toIntegerList(gateTile.dialedAddress);
					
					if (gateTile.getStargateState().initiating())
						symbols.add(EnumSymbol.BRB.id);
					else if (gateTile.getStargateState().engaged())
						symbols = Arrays.asList(EnumSymbol.BRB.id);
					
					return new DHDRendererState(symbols);
				}
				
				return new DHDRendererState();
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return new DHDRendererState();
		
			case DHD_ACTIVATE_BUTTON:
				return new DHDActivateButtonState();
				
			case GUI_UPDATE:
				return new DHDContainerGuiUpdate();
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}

	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				float horizontalRotation = world.getBlockState(pos).getValue(AunisProps.ROTATION_HORIZONTAL) * -22.5f;
				rendererStateClient = ((DHDRendererState) state).initClient(pos, horizontalRotation);
				
				break;
		
			case DHD_ACTIVATE_BUTTON:
				DHDActivateButtonState activateState = (DHDActivateButtonState) state;
				
				if (activateState.clearAll)
					getRendererStateClient().clearSymbols(world.getTotalWorldTime());
				else
					getRendererStateClient().activateSymbol(world.getTotalWorldTime(), activateState.symbol);
				
				break;
				
			case GUI_UPDATE:
				DHDContainerGuiUpdate guiState = (DHDContainerGuiUpdate) state;
				
				fluidHandler.setFluid(new FluidStack(AunisFluids.moltenNaquadahRefined, guiState.fluidAmount));
				fluidHandler.setCapacity(guiState.tankCapacity);
				reactorState = guiState.reactorState;
				
				break;
				
			default:
				throw new UnsupportedOperationException("EnumStateType."+stateType.name()+" not implemented on "+this.getClass().getName());
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	public static final List<Item> SUPPORTED_UPGRADES = Arrays.asList(
			AunisItems.crystalGlyphDhd);
	
	private ItemStackHandler itemStackHandler = new ItemStackHandler(5) {
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:
					return item == AunisItems.crystalControlDhd;
				
				case 1:
				case 2:
				case 3:
				case 4:
					return SUPPORTED_UPGRADES.contains(item);
					
				default:
					return true;
			}
		}
		
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			return 1;
		}
		
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			
			markDirty();
		}
	};
	
	public static enum DHDUpgradeEnum {
		CHEVRON_UPGRADE(AunisItems.crystalGlyphDhd);
		
		public Item item;

		private DHDUpgradeEnum(Item item) {
			this.item = item;
			
		}
	}
	
	public boolean isUpgradeInstalled(DHDUpgradeEnum upgrade) {
		for (int slot=1; slot<5; slot++) {
			if (itemStackHandler.getStackInSlot(slot).getItem() == upgrade.item)
				return true;
		}
		
		return false;
	}
	
	// -----------------------------------------------------------------------------
	// Fluid handler
	
	private FluidTank fluidHandler = new FluidTank(new FluidStack(AunisFluids.moltenNaquadahRefined, 0), AunisConfig.dhdConfig.fluidCapacity) {
		
		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return fluid.getFluid() == AunisFluids.moltenNaquadahRefined;
		}
		
		protected void onContentsChanged() {
			markDirty();
		}
	};
	
	
	// -----------------------------------------------------------------------------
	// Capabilities
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.DOWN))
				|| super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
		else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing == EnumFacing.DOWN))
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);

		
		return super.getCapability(capability, facing);	
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (linkedGate != null)
			compound.setLong("linkedGate", linkedGate.toLong());
		
		compound.setTag("inventory", itemStackHandler.serializeNBT());
		
		NBTTagCompound fluidHandlerCompound = new NBTTagCompound();
		fluidHandler.writeToNBT(fluidHandlerCompound);
		compound.setTag("fluidHandler", fluidHandlerCompound);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedGate"))
			linkedGate = BlockPos.fromLong(compound.getLong("linkedGate"));
		
		if (compound.getBoolean("hasUpgrade") || compound.getBoolean("insertAnimation")) {
			itemStackHandler.setStackInSlot(1, new ItemStack(AunisItems.crystalGlyphDhd));
		}
		
		itemStackHandler.deserializeNBT(compound.getCompoundTag("inventory"));
		fluidHandler.readFromNBT(compound.getCompoundTag("fluidHandler"));

		super.readFromNBT(compound);
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// Rendering distance
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(1, 2, 1));
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
