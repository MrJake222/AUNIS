package mrjake.aunis.tileentity;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.beamer.BeamerModeEnum;
import mrjake.aunis.beamer.BeamerRendererAction;
import mrjake.aunis.beamer.BeamerRoleEnum;
import mrjake.aunis.beamer.BeamerStatusEnum;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.gui.container.BeamerContainerGui;
import mrjake.aunis.gui.container.BeamerContainerGuiUpdate;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.AunisSoundHelperClient;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.state.BeamerRendererActionState;
import mrjake.aunis.state.BeamerRendererState;
import mrjake.aunis.state.BeamerRendererUpdate;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.stargate.StargateClassicBaseTile;
import mrjake.aunis.tileentity.util.ComparatorHelper;
import mrjake.aunis.tileentity.util.RedstoneModeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.tileentity.util.ScheduledTaskExecutorInterface;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
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
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers")
public class BeamerTile extends TileEntity implements ITickable, StateProviderInterface, ScheduledTaskExecutorInterface, Environment {
	
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
			Aunis.ocWrapper.joinOrCreateNetwork(this);
			
//			if (loopSoundPlaying) {
//				AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.BEAMER_LOOP, true);
//			}
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_STATE));
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_UPDATE));
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
		
		if (targetBeamerTile.getStatus() == BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC)
			return BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC_TARGET;
		
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
		
		switch (redstoneMode) {
			case AUTO:
				if (beamerRole == BeamerRoleEnum.RECEIVE && (beamerMode == BeamerModeEnum.POWER || beamerMode == BeamerModeEnum.FLUID)) {
					float level = 0;
					
					if (beamerMode == BeamerModeEnum.POWER)
						level = energyStorage.getEnergyStored() / (float)energyStorage.getMaxEnergyStored();
					else
						level = fluidHandler.getFluidAmount() / (float)fluidHandler.getCapacity();
						
					level *= 100;
					
					if (beamerStatus == BeamerStatusEnum.OK) {
						if (level > stop)
							return BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC;
						
						return BeamerStatusEnum.OK;
					}
					
					if (beamerStatus == BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC) {
						if (level < start)
							return BeamerStatusEnum.OK;
						
						return BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC;
					}
				}
					
				if (beamerMode == BeamerModeEnum.ITEMS) {
					if (beamerStatus == BeamerStatusEnum.OK) {
						if (timeWithoutItemTransfer > inactivity)
							return BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC;
						
						return BeamerStatusEnum.OK;
					}
					
					if (beamerStatus == BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC) {
						for (int i=1; i<5; i++) {
							if ((beamerRole == BeamerRoleEnum.RECEIVE && !targetBeamerTile.itemStackHandler.getStackInSlot(i).isEmpty()) || (beamerRole == BeamerRoleEnum.TRANSMIT && !itemStackHandler.getStackInSlot(i).isEmpty())) {
								return BeamerStatusEnum.OK;
							}
						}
						
						return BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC;
					}
				}
				
				break;
		
			case ON_HIGH:
				return world.isBlockPowered(pos) ? BeamerStatusEnum.OK : BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC;
		
			case ON_LOW:
				return world.isBlockPowered(pos) ? BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC : BeamerStatusEnum.OK;
			
			case IGNORED:
				return (ocLocked ? BeamerStatusEnum.BEAMER_DISABLED_BY_LOGIC : BeamerStatusEnum.OK);
		}
		
		return BeamerStatusEnum.OK;
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			ScheduledTask.iterate(scheduledTasks, world.getTotalWorldTime());
			
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
									
									timeWithoutItemTransfer = 0;
									
									if (toTransfer == 0)
										break;
								}
								
								if (toTransfer == 0)
									break;
							}
							
							if (toTransfer == AunisConfig.beamerConfig.itemTransfer && world.getTotalWorldTime()%20 == 0) {
								timeWithoutItemTransfer++;
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
									if (beamerStatus == BeamerStatusEnum.OK && world.getTotalWorldTime()%20 == 0) {
										timeWithoutItemTransfer++;
									}
								
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
				
				if (beamerStatus == BeamerStatusEnum.OK) {
					sendSignal(null, "beamer_started");
					AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.BEAMER_START); // 0.611s delay = 12 ticks
					addTask(new ScheduledTask(EnumScheduledTask.BEAMER_TOGGLE_SOUND, 12));
					sendRenderingAction(BeamerRendererAction.BEAM_ON);
				}
				
				else if (lastBeamerStatus == BeamerStatusEnum.OK) {
					sendSignal(null, "beamer_stopped");
					AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.BEAMER_STOP); // 0.634s delay = 12 ticks
					addTask(new ScheduledTask(EnumScheduledTask.BEAMER_TOGGLE_SOUND, 12));
					sendRenderingAction(BeamerRendererAction.BEAM_OFF);
				}
			}
		}
		
		// Client update
		else {
			float speed = 0.005f;
			
			if (beamRadiusShrink) {
				if (beamRadiusClient > 0)
					beamRadiusClient -= speed;
				else {
					beamRadiusShrink = false;
					beamRadiusClient = 0;
				}
			}
			
			else if (beamRadiusWiden) {
				if (beamRadiusClient < 0.1375f)
					beamRadiusClient += speed;
				else {
					beamRadiusWiden = false;
					beamRadiusClient = 0.1375f;
				}
			}
		}
	}
	
	private void syncToClient() {
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_STATE, getState(StateTypeEnum.RENDERER_STATE)), targetPoint);
		}
	}
	
	private void sendRenderingAction(BeamerRendererAction action) {
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_ACTION, new BeamerRendererActionState(action)), targetPoint);
		}
	}
	
	public void gateEngaged(StargatePos targetGatePos, StargatePos sourceGatePos) {
		updateTargetBeamerPos(targetGatePos, sourceGatePos);
	}
	
	public void gateClosed() {
		clearTargetBeamerPos();
	}
	
	private void updateTargetBeamerPos(StargatePos targetGatePos, StargatePos sourceGatePos) {		
		if (targetBeamerPos != null)
			return;
		
		targetBeamerWorld = targetGatePos.getWorld();
		EnumFacing targetFacing = targetBeamerWorld.getBlockState(targetGatePos.gatePos).getValue(AunisProps.FACING_HORIZONTAL);		
		Rotation rotation = FacingToRotation.get(targetFacing);
		
		BlockPos origVec = baseVect.rotate(rotation);
		BlockPos startingVec = null;
		
		switch (targetFacing.getAxis()) {
			case X:
				startingVec = new BlockPos(0, origVec.getY(), -origVec.getZ());
				break;
				
			case Z:
				startingVec = new BlockPos(-origVec.getX(), origVec.getY(), 0);
				break;
				
			default:
				break;
		}
		
		BlockPos beamerPos = targetGatePos.gatePos.add(startingVec);
		
		for (int i=1; i<=9; i++) {
			beamerPos = beamerPos.offset(targetFacing);
//			 Aunis.info("checking " + beamerPos);
			
			if (BEAMER_MATCHER.apply(targetBeamerWorld.getBlockState(beamerPos))) {
				targetBeamerPos = beamerPos.toImmutable();
				BeamerTile targetBeamerTile = (BeamerTile) targetBeamerWorld.getTileEntity(targetBeamerPos);
				
				if (targetBeamerTile.targetBeamerPos == null) {
					targetBeamerTile.updateTargetBeamerPos(sourceGatePos, targetGatePos); // Intentionally swaped
				}
				
				break;
			}
		}
	}
	
	public void clearTargetBeamerPos() {
		if (targetBeamerPos != null) {
			BeamerTile targetBeamerTile = (BeamerTile) targetBeamerWorld.getTileEntity(targetBeamerPos);
			targetBeamerTile.targetBeamerPos = null;
		}
	}
	
	// -----------------------------------------------------------------------------
	// Beamer
	
	private BeamerModeEnum beamerMode = BeamerModeEnum.NONE;
	private BeamerRoleEnum beamerRole = BeamerRoleEnum.TRANSMIT;
	private BeamerStatusEnum beamerStatus = BeamerStatusEnum.OBSTRUCTED;
	private World targetBeamerWorld = null;
	private BlockPos targetBeamerPos = null;
	private int comparatorOutput;
	private RedstoneModeEnum redstoneMode = RedstoneModeEnum.AUTO;
	private int start = 10;
	private int stop = 90;
	private int inactivity = 5;
	private int timeWithoutItemTransfer;
	private boolean ocLocked;
	
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
	
	public RedstoneModeEnum getRedstoneMode() {
		return redstoneMode;
	}
	
	public void setRedstoneMode(RedstoneModeEnum redstoneMode) {
		this.redstoneMode = redstoneMode;
		this.ocLocked = false;
		
		markDirty();
	}
	
	public void setStartStop(int start, int stop) {
		this.start = start;
		this.stop = stop;
		
		markDirty();
	}
	
	public int getStart() {
		return start;
	}
	
	public int getStop() {
		return stop;
	}
	
	public void setInactivity(int inactivity) {
		this.inactivity = inactivity;
		markDirty();
	}
	
	public int getInactivity() {
		return inactivity;
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
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			
			if (!block.isAir(state, world, pos) && !block.isReplaceable(world, pos) && state.isOpaqueCube()) {
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
			if (beamerRole != BeamerRoleEnum.TRANSMIT && slot != 0)
				return stack;
			
			return super.insertItem(slot, stack, simulate);
		}
		
		public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
			timeWithoutItemTransfer = 0;
			
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
	// Tasks
	
	private boolean loopSoundPlaying;
	
	List<ScheduledTask> scheduledTasks = new ArrayList<>();
	
	@Override
	public void addTask(ScheduledTask scheduledTask) {
		scheduledTask.setExecutor(this);
		scheduledTask.setTaskCreated(world.getTotalWorldTime());
		
		scheduledTasks.add(scheduledTask);
		markDirty();
	}
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case BEAMER_TOGGLE_SOUND:
				if (loopSoundPlaying)
					AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.BEAMER_LOOP, false);
				else
					AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.BEAMER_LOOP, true);
				
				loopSoundPlaying ^= true;
//				syncToClient();
				markDirty();
				
				break;
				
			default:
				break;
		}
	}
	
	// ---------------------------------------------------------------------------------------------------
	// States
	
	public int beamLengthClient;
	public float beamRadiusClient;
	private boolean beamRadiusWiden;
	private boolean beamRadiusShrink;
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_UPDATE:
				return new BeamerRendererUpdate(beamerStatus);
				
			case RENDERER_STATE:
				int distance = 0;
				
				if (baseVect != null) {
					distance = baseVect.getZ();
					if (distance < 0) distance = -distance;
				}
								
				return new BeamerRendererState(beamerMode, beamerRole, beamerStatus, isObstructed, distance);
		
			case GUI_UPDATE:
				return new BeamerContainerGuiUpdate(energyStorage.getEnergyStored(), energyTransferredLastTick, fluidHandler.getFluid(), beamerRole, redstoneMode, start, stop, inactivity);
				
			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_ACTION:
				return new BeamerRendererActionState();
				
			case RENDERER_STATE:
				return new BeamerRendererState();
				
			case RENDERER_UPDATE:
				return new BeamerRendererUpdate();
		
			case GUI_UPDATE:
				return new BeamerContainerGuiUpdate();
				
			default:
				return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				BeamerRendererState rendererState = (BeamerRendererState) state;
				
				beamerMode = rendererState.beamerMode;
				beamerRole = rendererState.beamerRole;
				beamerStatus = rendererState.beamerStatus;
				isObstructed = rendererState.isObstructed;
				beamLengthClient = rendererState.beamLength;
				world.markBlockRangeForRenderUpdate(pos, pos);
				break;
				
			case RENDERER_UPDATE:
				BeamerRendererUpdate update = (BeamerRendererUpdate) state;
				
				beamRadiusClient = update.beamerStatus == BeamerStatusEnum.OK ? 0.1375f : 0;
				AunisSoundHelperClient.playPositionedSoundClientSide(pos, SoundPositionedEnum.BEAMER_LOOP, update.beamerStatus == BeamerStatusEnum.OK);
				
				break;
				
			case RENDERER_ACTION:
				BeamerRendererActionState rendererAction = (BeamerRendererActionState) state;
				
				switch (rendererAction.action) {
					case BEAM_ON:
						beamRadiusClient = 0;
						beamRadiusWiden = true;
						beamRadiusShrink = false;
						break;
				
					case BEAM_OFF:
						beamRadiusClient = 0.1375f;
						beamRadiusWiden = false;
						beamRadiusShrink = true;
						break;
				}

				break;
		
			case GUI_UPDATE:
				BeamerContainerGuiUpdate guiUpdate = (BeamerContainerGuiUpdate) state;
				energyStorage.setEnergyStored(guiUpdate.energyStored);
				energyTransferredLastTick = guiUpdate.transferredLastTick;
				fluidHandler.setFluid(guiUpdate.fluidStack);
				beamerRole = guiUpdate.beamerRole;
				redstoneMode = guiUpdate.mode;
				start = guiUpdate.start;
				stop = guiUpdate.stop;
				inactivity = guiUpdate.inactivity;
				
				GuiScreen screen = Minecraft.getMinecraft().currentScreen;
				if (screen instanceof BeamerContainerGui ) {
					((BeamerContainerGui) screen).updateStartStopInactivity();
				}
				
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
		compound.setInteger("redstoneMode", redstoneMode.getKey());
		compound.setInteger("start", start);
		compound.setInteger("stop", stop);
		compound.setInteger("inactivity", inactivity);
		compound.setBoolean("ocLocked", ocLocked);
		compound.setBoolean("loopSoundPlaying", loopSoundPlaying);
		
		compound.setTag("scheduledTasks", ScheduledTask.serializeList(scheduledTasks));
		
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
		redstoneMode = RedstoneModeEnum.valueOf(compound.getInteger("redstoneMode"));
		start = compound.getInteger("start");
		stop = compound.getInteger("stop");
		inactivity = compound.getInteger("inactivity");
		ocLocked = compound.getBoolean("ocLocked");
		loopSoundPlaying = compound.getBoolean("loopSoundPlaying");
		
		ScheduledTask.deserializeList(compound.getCompoundTag("scheduledTasks"), scheduledTasks, this);
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
	
	// ------------------------------------------------------------------------
	// OpenComputers
	
	@Override
	public void onChunkUnload() {
		if (node != null)
			node.remove();
	}

	@Override
	public void invalidate() {
		if (node != null)
			node.remove();
				
		super.invalidate();
	}
	
	// ------------------------------------------------------------
	// Node-related work
	private Node node = Aunis.ocWrapper.createNode(this, "beamer");
	
	@Override
	@Optional.Method(modid = "opencomputers")
	public Node node() {
		return node;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public void onConnect(Node node) {}

	@Override
	@Optional.Method(modid = "opencomputers")
	public void onDisconnect(Node node) {}

	@Override
	@Optional.Method(modid = "opencomputers")
	public void onMessage(Message message) {}
	
	public void sendSignal(Object context, String name, Object... params) {
		Aunis.ocWrapper.sendSignalToReachable(node, (Context) context, name, params);
	}
	
	// ------------------------------------------------------------
	// Methods
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] isActive(Context context, Arguments args) {
		return new Object[] { isActive() };
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] setActive(Context context, Arguments args) {
		ocLocked = !args.checkBoolean(0);
		markDirty();
		
		return new Object[] {};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] start(Context context, Arguments args) {
		ocLocked = false;
		markDirty();
		
		return new Object[] {};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] stop(Context context, Arguments args) {
		ocLocked = true;
		markDirty();
		
		return new Object[] {};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getBeamerMode(Context context, Arguments args) {		
		return new Object[] { beamerMode.toString().toLowerCase() };
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getBeamerRole(Context context, Arguments args) {		
		return new Object[] { beamerRole.toString().toLowerCase() };
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] setBeamerRole(Context context, Arguments args) {
		try {
			beamerRole = BeamerRoleEnum.valueOf(args.checkString(0).toUpperCase());
		}
		
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Wrong Role name");
		}
		
		return new Object[] {};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] toggleBeamerRole(Context context, Arguments args) {	
		switch (beamerRole) {
			case RECEIVE:
				beamerRole = BeamerRoleEnum.TRANSMIT;
				return new Object[] { beamerRole.toString().toLowerCase() };
				
			case TRANSMIT:
				beamerRole = BeamerRoleEnum.RECEIVE;
				return new Object[] { beamerRole.toString().toLowerCase() };
				
			case DISABLED:
				return new Object[] { "err_beamer_disabled" };
				
			default:
				return null;
		}
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getBeamerStatus(Context context, Arguments args) {		
		return new Object[] { beamerStatus.toString().toLowerCase() };
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getBufferStored(Context context, Arguments args) {
		switch (beamerMode) {
		case POWER: return new Object[] { energyStorage.getEnergyStored() };
		case FLUID: return new Object[] { fluidHandler.getFluidAmount(), (fluidHandler.getFluid() != null ? Aunis.proxy.localize(fluidHandler.getFluid().getFluid().getUnlocalizedName()) : null) };
		case ITEMS:
			List<Map.Entry<String, Integer>> stackList = new ArrayList<>(4);
			
			for (int i=1; i<5; i++) {
				ItemStack stack = itemStackHandler.getStackInSlot(i);
				stackList.add(new AbstractMap.SimpleEntry<String, Integer>(stack.getDisplayName(), stack.getCount()));
			}
			
			return new Object[] { stackList };
			
		default:
			return new Object[] { "no_mode_set" };
		}
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getBufferCapacity(Context context, Arguments args) {
		switch (beamerMode) {
			case POWER: return new Object[] { energyStorage.getMaxEnergyStored() };
			case FLUID: return new Object[] { fluidHandler.getCapacity() };
			case ITEMS:
				List<Map.Entry<String, Integer>> stackList = new ArrayList<>(4);
				
				for (int i=1; i<5; i++) {
					ItemStack stack = itemStackHandler.getStackInSlot(i);
					stackList.add(new AbstractMap.SimpleEntry<String, Integer>(stack.getDisplayName(), stack.getMaxStackSize()));
				}
				
				return new Object[] { stackList };
				
			default:
				return new Object[] { "no_mode_set" };
		}
	}
}
