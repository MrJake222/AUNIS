package mrjake.aunis.tileentity;

import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.beamer.BeamerStatusEnum;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.gui.container.BeamerContainerGuiUpdate;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.state.BeamerRendererStateUpdate;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.util.ComparatorHelper;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.state.pattern.BlockMatcher;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BeamerTile extends TileEntity implements ITickable, StateProviderInterface {
	
	// -----------------------------------------------------------------------------
	// Ticking & loading
	
	private EnumFacing facing;
	private TargetPoint targetPoint;
	private AxisAlignedBB renderBox = TileEntity.INFINITE_EXTENT_AABB;
	
	public EnumFacing getFacing() {
		return facing;
	}
	
	public AunisAxisAlignedBB getRenderBoxForDisplay() {
		return new AunisAxisAlignedBB(-0.5, 0, -0.5, 0.5, 1, 9).rotate(facing).offset(0.5, 0, 0.5);
	}
	
	@Override
	public void onLoad() {
		updateFacing(world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL));
		
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_UPDATE));
		}
	}
	
	public void updateFacing(EnumFacing facing) {
		this.facing = facing;
		this.renderBox = new AunisAxisAlignedBB(-0.5, 0, -0.5, 0.5, 1, 9).rotate(facing).offset(0.5, 0, 0.5).offset(pos);
	}
	
	private static final BlockMatcher BEAMER_MATCHER = BlockMatcher.forBlock(AunisBlocks.BEAMER_BLOCK);
	
	private BeamerStatusEnum updateBeamerStatus() {
		if (beamerMode == BeamerModeEnum.NONE)
			return BeamerStatusEnum.NO_CRYSTAL;
		
		if (!isLinked())
			return BeamerStatusEnum.NOT_LINKED;
		
		StargateClassicBaseTile gateTile = getLinkedGateTile();
		
		if (!gateTile.getStargateState().engaged())
			return BeamerStatusEnum.CLOSED;
		
		if (isObstructed)
			return BeamerStatusEnum.OBSTRUCTED;
		
		if (targetBeamerWorld == null || targetBeamerPos == null || !BEAMER_MATCHER.apply(targetBeamerWorld.getBlockState(targetBeamerPos)))
			return BeamerStatusEnum.NO_BEAMER;
		
		BeamerTile targetBeamerTile = (BeamerTile) targetBeamerWorld.getTileEntity(targetBeamerPos);
		
		if (targetBeamerTile.isObstructed)
			return BeamerStatusEnum.OBSTRUCTED_TARGET;
		
		if (beamerRole == BeamerRoleEnum.DISABLED)
			return BeamerStatusEnum.BEAMER_DISABLED;
		
		if (targetBeamerTile.getRole() == BeamerRoleEnum.DISABLED)
			return BeamerStatusEnum.BEAMER_DISABLED_TARGET;
		
		if (beamerRole == targetBeamerTile.getRole()) {
			if (beamerRole == BeamerRoleEnum.TRANSMIT)
				return BeamerStatusEnum.TWO_TRANSMITTERS;
			else
				return BeamerStatusEnum.TWO_RECEIVERS;
		}
		
		if (beamerMode != targetBeamerTile.getMode())
			return BeamerStatusEnum.MODE_MISMATCH;
		
		if (beamerMode != BeamerModeEnum.POWER && ((gateTile.getStargateState().initiating() && beamerRole != BeamerRoleEnum.TRANSMIT) || (gateTile.getStargateState() == EnumStargateState.ENGAGED && beamerRole != BeamerRoleEnum.RECEIVE)))
			return BeamerStatusEnum.INCOMING;
		
		return BeamerStatusEnum.OK;
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			BeamerStatusEnum lastBeamerStatus = beamerStatus;
			
			if (world.getTotalWorldTime() % 20 == 0) {
				int lastComp = comparatorOutput;
				comparatorOutput = updateComparatorOutput();
				
				if (lastComp != comparatorOutput) {
					world.updateComparatorOutputLevel(pos, AunisBlocks.BEAMER_BLOCK);
				}
				
				if (beamerMode != BeamerModeEnum.NONE && isLinked()) {
					StargateClassicBaseTile gateTile = getLinkedGateTile();
					
					if (gateTile.getStargateState().engaged()) {
						updateObstructed();
					}
				}
			}
			
			beamerStatus = updateBeamerStatus();
			
			if (beamerStatus == BeamerStatusEnum.OK) {
				
				if (beamerRole == BeamerRoleEnum.TRANSMIT) {
					BeamerTile targetBeamerTile = (BeamerTile) targetBeamerWorld.getTileEntity(targetBeamerPos);
					
					switch (beamerMode) {
						case POWER:
							int tx = energyStorage.extractEnergy(AunisConfig.beamerConfig.energyTransfer, true);
							tx = targetBeamerTile.energyStorage.receiveEnergyInternal(tx, false);
							energyStorage.extractEnergy(tx, false);
							break;
							
						case FLUID:
							FluidStack fluid = fluidHandler.drainInternal(AunisConfig.beamerConfig.fluidTransfer, false);
							int filled = targetBeamerTile.fluidHandler.fillInternal(fluid, true);
							fluidHandler.drainInternal(filled, true);
							break;
							
						case ITEMS:
							int toTransfer = AunisConfig.beamerConfig.itemTransfer;
							
							for (int i=1; i<5; i++) {
								ItemStack stack = itemStackHandler.extractItem(i, toTransfer, true);
								
								if (stack.isEmpty())
									continue;
								
								for (int k=1; k<5; k++) {									
									int accepted = stack.getCount() - targetBeamerTile.itemStackHandler.insertItemInternal(k, stack, false).getCount();
									itemStackHandler.extractItem(i, accepted, false);
									toTransfer -= accepted;
									
									if (toTransfer == 0)
										break;
								}
								
								if (toTransfer == 0)
									break;
							}
							
							break;
						
						default:
							break;
					}
				}
			}
				
			if (beamerRole == BeamerRoleEnum.RECEIVE) {
				for (EnumFacing side : EnumFacing.values()) {
					TileEntity tileEntity = world.getTileEntity(pos.offset(side));
					
					if (tileEntity != null) {
						switch (beamerMode) {
							case POWER:
								if (tileEntity.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite())) {
									IEnergyStorage targetEnergyStorage = tileEntity.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
									int tx = energyStorage.extractEnergy(AunisConfig.beamerConfig.energyTransfer, true);
									tx = targetEnergyStorage.receiveEnergy(tx, false);
									energyStorage.extractEnergy(tx, false);
								}
								
								break;
								
							case FLUID:
									if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite())) {
										IFluidHandler targetFluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite());
										FluidStack drained = fluidHandler.drain(AunisConfig.beamerConfig.fluidTransfer, false);
										int filled = targetFluidHandler.fill(drained, true);
										fluidHandler.drain(filled, true);
									}
								
								break;
								
							case ITEMS:
									if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) {
										IItemHandler targetItemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
										
										int toTransfer = AunisConfig.beamerConfig.itemTransfer;
										
										for (int i=1; i<5; i++) {
											ItemStack stack = itemStackHandler.extractItem(i, toTransfer, true);
											
											if (stack.isEmpty())
												continue;
											
											for (int k=0; k<targetItemHandler.getSlots(); k++) {									
												int accepted = stack.getCount() - targetItemHandler.insertItem(k, stack, false).getCount();
												itemStackHandler.extractItem(i, accepted, false);
												toTransfer -= accepted;
												
												if (toTransfer == 0)
													break;
											}
											
											if (toTransfer == 0)
												break;
										}
									}
								
								break;
							
							default:
								break;
						}
					}
				}
			}
			
			
			if (beamerMode == BeamerModeEnum.POWER) {
				energyTransferredLastTick = energyStorage.getEnergyStored() - energyStoredLastTick;
				energyStoredLastTick = energyStorage.getEnergyStored();
			}
			
			if (lastBeamerStatus != beamerStatus) {
				syncToClient();
			}
		}
	}
	
	private void syncToClient() {
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, getState(StateTypeEnum.RENDERER_UPDATE)), targetPoint);
		}
	}
	
	public void gateEngaged(StargatePos targetGatePos) {
		targetBeamerWorld = targetGatePos.getWorld();
		EnumFacing targetFacing = targetBeamerWorld.getBlockState(targetGatePos.gatePos).getValue(AunisProps.FACING_HORIZONTAL);		
		Rotation rotation = FacingToRotation.get(targetFacing);
		
		targetBeamerPos = targetGatePos.gatePos.add(baseVect.rotate(rotation));
	}
	
	public void gateClosed() {
		
	}
	
	// -----------------------------------------------------------------------------
	// Beamer
	
	private BeamerModeEnum beamerMode = BeamerModeEnum.NONE;
	private BeamerRoleEnum beamerRole = BeamerRoleEnum.TRANSMIT;
	private BeamerStatusEnum beamerStatus = BeamerStatusEnum.OBSTRUCTED;
	private World targetBeamerWorld = null;
	private BlockPos targetBeamerPos = null;
	private int comparatorOutput;
	
	private boolean isObstructed;
	
	public BeamerModeEnum getMode() {
		return beamerMode;
	}
	
	public BeamerRoleEnum getRole() {
		return beamerRole;
	}
	
	public BeamerStatusEnum getStatus() {
		return beamerStatus;
	}
	
	public int getComparatorOutput() {
		return comparatorOutput;
	}
	
	public boolean isActive() {
		return beamerStatus == BeamerStatusEnum.OK;
	}
	
	public void setNextRole() {
		beamerRole = beamerRole.next();
		markDirty();
	}
	
	public void updateObstructed() {
		if (basePos == null)
			return;
		
		isObstructed = false;
		int diff = 0;
		
		switch (facing.getAxis()) {
			case X:
				diff = basePos.getX() - pos.getX();
				break;
				
			case Z:
				diff = basePos.getZ() - pos.getZ();
				break;
				
			default:
				break;
		}
		
		if (diff < 0) diff *= -1;
		
		for (BlockPos pos : BlockPos.getAllInBoxMutable(pos.offset(facing), pos.offset(facing, diff))) {
			if (!world.isAirBlock(pos) && !world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
				isObstructed = true;
				break;
			}
		}
	}
	
	public int updateComparatorOutput() {
		
		switch (beamerMode) {
			case POWER:
				return ComparatorHelper.getComparatorLevel(energyStorage);
				
			case FLUID:
				return ComparatorHelper.getComparatorLevel(fluidHandler);
				
			case ITEMS:
				return ComparatorHelper.getComparatorLevel(itemStackHandler, 1);
				
			default:
				return 0;
		}
	}
	
	// -----------------------------------------------------------------------------
	// Linking
	
	private BlockPos baseVect;
	private BlockPos basePos;
	
	public boolean isLinked() {
		return basePos != null;
	}
	
	public BlockPos getLinkedGate() {
		return basePos;
	}
	
	public StargateClassicBaseTile getLinkedGateTile() {
		return (StargateClassicBaseTile) world.getTileEntity(basePos);
	}
	
	/**
	 * @param baseVect South-rotated gate-to-beamer vector.
	 */
	public void setLinkedGate(BlockPos basePos, BlockPos baseVect) {
		Aunis.info("setting gate vec to " + baseVect);
		
		if (basePos == null || baseVect == null) {
			this.basePos = null;
			this.baseVect = null;
		}
		
		else {
			this.basePos = basePos.toImmutable();
			this.baseVect = baseVect.toImmutable();
		}
		
		markDirty();
	}
	
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	private class ItemStackHandlerBeamer extends ItemStackHandler {
		
		public ItemStackHandlerBeamer(int slots) {
			super(slots);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:
					return item == AunisItems.BEAMER_CRYSTAL_POWER || item == AunisItems.BEAMER_CRYSTAL_FLUID || item == AunisItems.BEAMER_CRYSTAL_ITEMS;
					
				default:
					return true;
			}
		}
		
		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			if (slot == 0)
				return 1;
			
			return super.getStackLimit(slot, stack);
		}
		
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			
			if (!world.isRemote && slot == 0)
				updateMode();
			
			markDirty();
		}
		
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack stack = super.extractItem(slot, amount, simulate);
			
			if (!world.isRemote && slot == 0 && !simulate)
				updateMode();
			
			return stack;
		}
		
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (beamerRole != BeamerRoleEnum.TRANSMIT)
				return stack;
			
			return super.insertItem(slot, stack, simulate);
		}
		
		public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {			
			return super.insertItem(slot, stack, simulate);
		}
	}
	
	private ItemStackHandlerBeamer itemStackHandler = new ItemStackHandlerBeamer(5);
	
	private void updateMode() {		
		beamerMode = getModeFromItem(itemStackHandler.getStackInSlot(0).getItem());
		markDirty();
		syncToClient();
	}
	
	public static BeamerModeEnum getModeFromItem(Item crystal) {
		if (crystal == AunisItems.BEAMER_CRYSTAL_POWER)
			return BeamerModeEnum.POWER;
			
		else if (crystal == AunisItems.BEAMER_CRYSTAL_FLUID)
			return BeamerModeEnum.FLUID;
		
		else if (crystal == AunisItems.BEAMER_CRYSTAL_ITEMS)
			return BeamerModeEnum.ITEMS;
		
		return BeamerModeEnum.NONE;
	}
	
	// -----------------------------------------------------------------------------
	// Fluid handler
	
	private FluidTank fluidHandler = new FluidTank(null, AunisConfig.beamerConfig.fluidCapacity) {
		
		protected void onContentsChanged() {
			markDirty();
		}
		
		public boolean canFill() {
			return beamerRole == BeamerRoleEnum.TRANSMIT;
		};
	};
	
	
	// -----------------------------------------------------------------------------
	// Power system
	
	private StargateAbstractEnergyStorage energyStorage = new StargateAbstractEnergyStorage(AunisConfig.beamerConfig.energyCapacity, AunisConfig.beamerConfig.energyTransfer) {
		
		@Override
		protected void onEnergyChanged() {
			markDirty();
		}
		
		@Override
		public boolean canReceive() {
			return beamerRole == BeamerRoleEnum.TRANSMIT;
		}
	};
	
	private int energyStoredLastTick = 0;
	private int energyTransferredLastTick = 0;
	
	public int getEnergyTransferredLastTick() {
		return energyTransferredLastTick;
	}
		
	
	// -----------------------------------------------------------------------------
	// Capabilities
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		
		// Not front
		if (facing != this.facing) {
			return (beamerMode == BeamerModeEnum.ITEMS && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					|| (beamerMode == BeamerModeEnum.POWER && capability == CapabilityEnergy.ENERGY)
					|| (beamerMode == BeamerModeEnum.FLUID && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
					|| super.hasCapability(capability, facing);
		}
				
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		
		// Not front
		if (facing != this.facing) {
			if ((beamerMode == BeamerModeEnum.ITEMS || facing == null) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
			
			else if ((beamerMode == BeamerModeEnum.POWER || facing == null) && capability == CapabilityEnergy.ENERGY)
				return CapabilityEnergy.ENERGY.cast(energyStorage);
			
			else if ((beamerMode == BeamerModeEnum.FLUID || facing == null) && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
				return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
		}
		
		return super.getCapability(capability, facing);	
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// States
	
	public int beamLengthClient;
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_UPDATE:
				int distance = 0;
				
				if (baseVect != null) {
					distance = baseVect.getZ();
					if (distance < 0) distance = -distance;
				}
								
				return new BeamerRendererStateUpdate(beamerMode, beamerStatus, isObstructed, distance);
		
			case GUI_UPDATE:
				return new BeamerContainerGuiUpdate(energyStorage.getEnergyStored(), energyTransferredLastTick, fluidHandler.getFluid(), beamerRole);
				
			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_UPDATE:
				return new BeamerRendererStateUpdate();
		
			case GUI_UPDATE:
				return new BeamerContainerGuiUpdate();
				
			default:
				return null;
		}
	}

	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_UPDATE:
				BeamerRendererStateUpdate update = (BeamerRendererStateUpdate) state;
				beamerMode = update.beamerMode;
				beamerStatus = update.beamerStatus;
				isObstructed = update.isObstructed;
				beamLengthClient = update.beamLength;
				world.markBlockRangeForRenderUpdate(pos, pos);
				Aunis.info("beamerStatus: " + beamerStatus);

				break;
		
			case GUI_UPDATE:
				BeamerContainerGuiUpdate guiUpdate = (BeamerContainerGuiUpdate) state;
				energyStorage.setEnergyStored(guiUpdate.energyStored);
				energyTransferredLastTick = guiUpdate.transferredLastTick;
				fluidHandler.setFluid(guiUpdate.fluidStack);
				beamerRole = guiUpdate.beamerRole;
				
				break;
				
			default:
				break;
		}
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (baseVect != null && basePos != null) {
			compound.setLong("baseVect", baseVect.toLong());
			compound.setLong("basePos", basePos.toLong());
		}
		
		compound.setTag("itemStackHandler", itemStackHandler.serializeNBT());
		compound.setTag("energyStorage", energyStorage.serializeNBT());
		
		NBTTagCompound fluidHandlerCompound = new NBTTagCompound();
		fluidHandler.writeToNBT(fluidHandlerCompound);
		compound.setTag("fluidHandler", fluidHandlerCompound);
		
		compound.setInteger("beamerMode", beamerMode.getKey());
		compound.setInteger("beamerRole", beamerRole.getKey());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (compound.hasKey("baseVect") && compound.hasKey("basePos")) {
			baseVect = BlockPos.fromLong(compound.getLong("baseVect"));
			basePos = BlockPos.fromLong(compound.getLong("basePos"));
		}
		
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemStackHandler"));
		energyStorage.deserializeNBT(compound.getCompoundTag("energyStorage"));
		fluidHandler.readFromNBT(compound.getCompoundTag("fluidHandler"));
		
		beamerMode = BeamerModeEnum.valueOf(compound.getInteger("beamerMode"));
		beamerRole = BeamerRoleEnum.valueOf(compound.getInteger("beamerRole"));
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	// Rendering distance
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return renderBox;
	}
	
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}
}
