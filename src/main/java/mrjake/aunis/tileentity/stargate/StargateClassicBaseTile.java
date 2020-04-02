package mrjake.aunis.tileentity.stargate;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.gui.container.StargateContainerGuiState;
import mrjake.aunis.gui.container.StargateContainerGuiUpdate;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.item.PageNotebookItem;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.renderer.stargate.StargateClassicRendererState;
import mrjake.aunis.renderer.stargate.StargateClassicRendererState.StargateClassicRendererStateBuilder;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateAbstractEnergyStorage;
import mrjake.aunis.stargate.StargateClassicEnergyStorage;
import mrjake.aunis.stargate.StargateClassicSpinHelper;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateSpinState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;
import mrjake.aunis.util.FacingToRotation;
import mrjake.aunis.util.ItemHandlerHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * This class wraps common behavior for the fully-functional Stargates i.e.
 * all of them (right now) except Orlin's.
 * 
 * @author MrJake222
 *
 */
public abstract class StargateClassicBaseTile extends StargateAbstractBaseTile {
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	protected boolean isFinalActive;
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		isFinalActive = false;
		
		updateChevronLight(0);
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}
	
	@Override
	protected void failGate() {
		super.failGate();
		
		isFinalActive = false;
		
		updateChevronLight(0);
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
	}
	
	@Override
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		super.openGate(targetGatePos, isInitiating);
		
		this.isFinalActive = true;
	}
	
	@Override
	public void incomingWormhole(int dialedAddressSize) {
		super.incomingWormhole(dialedAddressSize);
		
		isFinalActive = true;
		
		playSoundEvent(StargateSoundEventEnum.INCOMING);
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, dialedAddressSize, true);
	}
	
	@Override
	public void onBlockBroken() {
		super.onBlockBroken();
		
		playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
		ItemHandlerHelper.dropInventoryItems(world, pos, itemStackHandler);
	}
	
	
	// ------------------------------------------------------------------------
	// Loading and ticking
	
	@Override
	public void update() {
		super.update();
		
		if (!world.isRemote) {
			if (givePageTask != null) {
				if (givePageTask.update(world.getTotalWorldTime())) {
					givePageTask = null;
				}
			}
			
			if (doPageProgress) {
				if (world.getTotalWorldTime() % 2 == 0) {
					pageProgress++;
				
					if (pageProgress > 18) {
						pageProgress = 0;
						doPageProgress = false;
					}
				}
				
				if (itemStackHandler.getStackInSlot(pageSlotId).isEmpty()) {
					lockPage = false;
					doPageProgress = false;
					pageProgress = 0;
					givePageTask = null;
				}
			}
			
			else {
//				Aunis.info("lock: " + lockPage);
					
				if (lockPage && itemStackHandler.getStackInSlot(pageSlotId).isEmpty()) {
					lockPage = false;
				}
				
				if (!lockPage) {
					for (int i=7; i<10; i++) {
						if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
							doPageProgress = true;
							lockPage = true;
							pageSlotId = i;
							givePageTask = new ScheduledTask(EnumScheduledTask.STARGATE_GIVE_PAGE, 36);
							givePageTask.setTaskCreated(world.getTotalWorldTime());
							givePageTask.setExecutor(this);
							
							break;
						}
					}
				}
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
	
	@Override
	protected void setWorldCreate(World world) {
		setWorld(world);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("itemHandler", itemStackHandler.serializeNBT());
		compound.setBoolean("isFinalActive", isFinalActive);
		
		compound.setBoolean("isSpinning", isSpinning);
		compound.setBoolean("locking", locking);
		compound.setLong("spinStartTime", spinStartTime);
		compound.setInteger("currentRingSymbol", currentRingSymbol.getId());
		compound.setInteger("targetRingSymbol", targetRingSymbol.getId());
		compound.setInteger("spinDirection", spinDirection.id);

		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemHandler"));
		
		if (compound.getBoolean("hasUpgrade")) {
			itemStackHandler.setStackInSlot(0, new ItemStack(AunisItems.crystalGlyphStargate));
		}
				
		updatePowerTier();
		isFinalActive = compound.getBoolean("isFinalActive");
		
		isSpinning = compound.getBoolean("isSpinning");
		locking = compound.getBoolean("locking");
		spinStartTime = compound.getLong("spinStartTime");
		currentRingSymbol = getSymbolType().valueOfSymbol(compound.getInteger("currentRingSymbol"));
		targetRingSymbol = getSymbolType().valueOfSymbol(compound.getInteger("targetRingSymbol"));
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("spinDirection"));
		
		super.readFromNBT(compound);
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	protected void updateChevronLight(int lightUp) {
		for (int i=0; i<9; i++) {
			BlockPos chevPos = getMergeHelper().getChevronBlocks().get(i);
			
			if (getMergeHelper().matchMember(world.getBlockState(chevPos))) {
				StargateClassicMemberTile memberTile = (StargateClassicMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(facing)).add(pos));
				memberTile.setLitUp(lightUp > i);
			}
		}
	}
	
	@Override
	protected StargateClassicRendererStateBuilder getRendererStateServer() {
		return new StargateClassicRendererStateBuilder(super.getRendererStateServer())
				.setSymbolType(getSymbolType())
				.setActiveChevrons(dialedAddress.size())
				.setFinalActive(isFinalActive)
				.setCurrentRingSymbol(currentRingSymbol)
				.setSpinDirection(spinDirection)
				.setSpinning(isSpinning)
				.setTargetRingSymbol(targetRingSymbol)
				.setSpinStartTime(spinStartTime);
	}
	
	@Override
	public StargateClassicRendererState getRendererStateClient() {
		return (StargateClassicRendererState) super.getRendererStateClient();
	}
	
	
	// -----------------------------------------------------------------
	// States
	
	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new StargateContainerGuiState(gateAddressMap);
				
			case GUI_UPDATE:
				return new StargateContainerGuiUpdate(energyStorage.getEnergyStoredInternally(), energyTransferedLastTick, energySecondsToClose);
				
			default:
				return super.getState(stateType);
		}
	}
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new StargateContainerGuiState();
				
			case GUI_UPDATE:
				return new StargateContainerGuiUpdate();
				
			case SPIN_STATE:
				return new StargateSpinState();
				
			default:
				return super.createState(stateType);
		}
	}
	
	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {		
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;
				
				switch (gateActionState.action) {
					case CHEVRON_ACTIVATE:
						if (gateActionState.modifyFinal)
							getRendererStateClient().chevronTextureList.activateFinalChevron(world.getTotalWorldTime());
						else
							getRendererStateClient().chevronTextureList.activateNextChevron(world.getTotalWorldTime());
						
						break;
					
					case CLEAR_CHEVRONS:
						getRendererStateClient().chevronTextureList.clearChevrons(world.getTotalWorldTime());
						break;
						
					case LIGHT_UP_CHEVRONS:
						getRendererStateClient().chevronTextureList.lightUpChevrons(world.getTotalWorldTime(), gateActionState.chevronCount);
						break;
						
					case CHEVRON_ACTIVATE_BOTH:
						getRendererStateClient().chevronTextureList.activateNextChevron(world.getTotalWorldTime());
						getRendererStateClient().chevronTextureList.activateFinalChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_DIM:
						getRendererStateClient().chevronTextureList.deactivateFinalChevron(world.getTotalWorldTime());
						break;
						
					default:
						break;
				}
				
				break;
		
			case GUI_STATE:
				StargateContainerGuiState guiState = (StargateContainerGuiState) state;
				gateAddressMap = guiState.gateAdddressMap;
				
				break;
				
			case GUI_UPDATE:
				StargateContainerGuiUpdate guiUpdate = (StargateContainerGuiUpdate) state;
				energyStorage.setEnergyStoredInternally(guiUpdate.energyStored);
				energyTransferedLastTick = guiUpdate.transferedLastTick;
				energySecondsToClose = guiUpdate.secondsToClose;
				
				break;
				
			case SPIN_STATE:
				StargateSpinState spinState = (StargateSpinState) state;
				getRendererStateClient().spinHelper.initRotation(world.getTotalWorldTime(), spinState.targetSymbol, spinState.direction);
				
				break;
				
			default:
				break;
		}
		
		super.setState(stateType, state);
	}

	
	// -----------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_ACTIVATE_CHEVRON:
				stargateState = EnumStargateState.IDLE;
				markDirty();
				
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"));
//				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"))), targetPoint);
				break;
				
			case STARGATE_SPIN_FINISHED:
				isSpinning = false;
				currentRingSymbol = targetRingSymbol;
				
				playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, false);
				playSoundEvent(StargateSoundEventEnum.CHEVRON_SHUT);
				
				markDirty();
				break;
				
			case STARGATE_GIVE_PAGE:
				SymbolTypeEnum symbolType = SymbolTypeEnum.valueOf(pageSlotId - 7);
				ItemStack stack = itemStackHandler.getStackInSlot(pageSlotId);
				
				if (stack.getItem() == AunisItems.UNIVERSE_DIALER) {
					NBTTagList saved = stack.getTagCompound().getTagList("saved", NBT.TAG_COMPOUND);
					NBTTagCompound compound = gateAddressMap.get(symbolType).serializeNBT();
					compound.setBoolean("hasUpgrade", hasUpgradeInstalled(StargateUpgradeEnum.CHEVRON_UPGRADE));
					saved.appendTag(compound);
				}
				
				else {
					Aunis.info("Giving Notebook page of address " + symbolType);
	
					NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(
							gateAddressMap.get(symbolType),
							hasUpgradeInstalled(StargateUpgradeEnum.CHEVRON_UPGRADE),
							PageNotebookItem.getRegistryPathFromWorld(world, pos));
	
					stack = new ItemStack(AunisItems.pageNotebookItem, 1, 1);
					stack.setTagCompound(compound);
					itemStackHandler.setStackInSlot(pageSlotId, stack);
				}
				
				break;
				
			default:
				super.executeTask(scheduledTask, customData);
		}
	}
	

	// ------------------------------------------------------------------------
	// Ring spinning
	
	protected boolean isSpinning;
	protected boolean locking;
	protected long spinStartTime;
	protected SymbolInterface currentRingSymbol = getSymbolType().getTopSymbol();
	protected SymbolInterface targetRingSymbol = getSymbolType().getTopSymbol();
	protected EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	protected Object ringSpinContext;
	
	public void addSymbolToAddressManual(SymbolInterface targetSymbol, @Nullable Object context) {
		targetRingSymbol = targetSymbol;
		
		boolean moveOnly = targetRingSymbol == currentRingSymbol;
		locking = (dialedAddress.size() == 7) || (dialedAddress.size() == 6 && targetRingSymbol.origin());
		
		if (moveOnly) {
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, 0));
		}
		
		else {
			float distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			
			if (distance < StargateClassicSpinHelper.getMinimalDistance()) {
				spinDirection = spinDirection.opposite();
				distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			}
			
			else if (distance > 180 && (360-distance) > StargateClassicSpinHelper.getMinimalDistance()) {
				spinDirection = spinDirection.opposite();
				distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			}
			
			// Aunis.info("position: " + currentRingSymbol + ", target: " + targetSymbol + ", direction: " + spinDirection + ", distance: " + distance + ", animEnd: " + StargateSpinHelper.getAnimationDuration(distance) + ", moveOnly: " + moveOnly + ", locking: " + locking);
			
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.SPIN_STATE, new StargateSpinState(targetRingSymbol, spinDirection)), targetPoint);
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, StargateClassicSpinHelper.getAnimationDuration(distance) - 5));
			playPositionedSound(StargateSoundPositionedEnum.GATE_RING_ROLL, true);
			
			isSpinning = true;
			spinStartTime = world.getTotalWorldTime();
			
			ringSpinContext = context;
			if (context != null)
				sendSignal(context, "stargate_spin_start", new Object[] { dialedAddress.size(), locking, targetSymbol.getEnglishName() });
		}
			
		markDirty();
	}
	
	
	// -----------------------------------------------------------------------------
	// Page conversion
	
	private short pageProgress = 0;
	private int pageSlotId;
	private boolean doPageProgress;
	private ScheduledTask givePageTask;
	private boolean lockPage;
	
	public short getPageProgress() {
		return pageProgress;
	}
	
	public void setPageProgress(int pageProgress) {
		this.pageProgress = (short) pageProgress;
	}
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	private ItemStackHandler itemStackHandler = new ItemStackHandler(10) {
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:				
				case 1:
				case 2:
				case 3:
					return StargateUpgradeEnum.contains(item);
					
				case 4:
				case 5:
				case 6:
					return item == Item.getItemFromBlock(AunisBlocks.CAPACITOR_BLOCK);
					
				case 7:
				case 8:
					return item == AunisItems.pageNotebookItem;
					
				case 9:
					return item == AunisItems.pageNotebookItem || item == AunisItems.UNIVERSE_DIALER;
					
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
			
			switch (slot) {
				case 4:
				case 5:
				case 6:			
					updatePowerTier();
					break;
					
				default:
					break;
			}
			
			markDirty();
		}
	};
	
	public static enum StargateUpgradeEnum implements EnumKeyInterface<Item> {
		MILKYWAY_GLYPHS(AunisItems.crystalGlyphMilkyWay),
		PEGASUS_GLYPHS(AunisItems.crystalGlyphPegasus),
		UNIVERSE_GLYPHS(AunisItems.crystalGlyphUniverse),
		CHEVRON_UPGRADE(AunisItems.crystalGlyphStargate);
		
		public Item item;

		private StargateUpgradeEnum(Item item) {
			this.item = item;
		}
		
		@Override
		public Item getKey() {
			return item;
		}
		
		private static EnumKeyMap<Item, StargateUpgradeEnum> idMap = new EnumKeyMap<Item, StargateClassicBaseTile.StargateUpgradeEnum>(values());
		
		public static StargateUpgradeEnum valueOf(Item item) {
			return idMap.valueOf(item);
		}
		
		public static boolean contains(Item item) {
			return idMap.contains(item);
		}
	}
	
	public boolean hasUpgradeInstalled(StargateUpgradeEnum upgrade) {
		for (int slot=0; slot<4; slot++) {
			if (itemStackHandler.getStackInSlot(slot).getItem() == upgrade.item)
				return true;
		}
		
		return false;
	}
	
	
	// -----------------------------------------------------------------------------
	// Power system
	
	private StargateClassicEnergyStorage energyStorage = new StargateClassicEnergyStorage() {
		
		@Override
		protected void onEnergyChanged() {
			markDirty();
		}
	};
	
	@Override
	protected StargateAbstractEnergyStorage getEnergyStorage() {
		return energyStorage;
	}
	
	private int currentPowerTier;
	
	public int getPowerTier() {
		return currentPowerTier;
	}
	
	private void updatePowerTier() {
		if (!world.isRemote)
			Aunis.info("updatePowerTier called");
		
		int powerTier = 1;
				
		for (int i=4; i<7; i++) {
			if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
				powerTier++;
			}
		}
		
		if (powerTier != currentPowerTier) {
			currentPowerTier = powerTier;
			
			energyStorage.clearStorages();
			
			for (int i=4; i<7; i++) {
				ItemStack stack = itemStackHandler.getStackInSlot(i);
				
				if (!stack.isEmpty()) {
					energyStorage.addStorage(stack.getCapability(CapabilityEnergy.ENERGY, null));
				}
			}
				
			Aunis.info("Updated to power tier: " + powerTier);
			markDirty();
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Capabilities
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
		
		return super.getCapability(capability, facing);	
	}
	
	
	// -----------------------------------------------------------------
	// OpenComputers methods
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function(symbolName:string) -- Spins the ring to the given symbol and engages/locks it")
	public Object[] engageSymbol(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null, "stargate_failure_not_merged", "Stargate is not merged" };
		
		if (!stargateState.idle()) {
			return new Object[] {null, "stargate_failure_busy", "Stargate is busy, state: " + stargateState.toString()};
		}
		
		if (dialedAddress.size() == 9) {
			return new Object[] {null, "stargate_failure_full", "Already dialed 9 chevrons"};
		}
		
		SymbolInterface targetSymbol = null;
		
		if (args.isInteger(0))
			targetSymbol = getSymbolType().valueOfSymbol(args.checkInteger(0));
		else if (args.isString(0))
			targetSymbol = getSymbolType().fromEnglishName(args.checkString(0));
		
		if (targetSymbol == null)
			throw new IllegalArgumentException("bad argument #1 (symbol name/index invalid)");
		
//		if (canAddSymbol(targetSymbol)) {
//			return new Object[] {null, "stargate_failure_add", "Dialed address contains this symbol already"};
//		}
		
		addSymbolToAddressManual(targetSymbol, context);
		
		markDirty();
		
		return new Object[] {"stargate_spin"};
	}

	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function() -- Tries to open the gate")
	public Object[] engageGate(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null, "stargate_failure_not_merged", "Stargate is not merged" };
		
		if (stargateState.idle()) {
			StargateOpenResult gateState = attemptOpenDialed();
	
			if (gateState.ok()) {
				return new Object[] {"stargate_engage"};
			}
			
			else {
				stargateState = EnumStargateState.IDLE;
				markDirty();
				dialingFailed();
				
				sendSignal(null, "stargate_failed", new Object[] {});
				return new Object[] {null, "stargate_failure_opening", "Stargate failed to open", gateState.toString()};
			}
		}
		
		else {
			return new Object[] {null, "stargate_failure_busy", "Stargate is busy", stargateState.toString()};
		}
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function() -- Tries to close the gate")
	public Object[] disengageGate(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null, "stargate_failure_not_merged", "Stargate is not merged" };
		
		if (stargateState.engaged()) {
			if (getStargateState().initiating()) {
				attemptClose();
				return new Object[] { "stargate_disengage" };
			}
			
			else
				return new Object[] {null, "stargate_failure_wrong_end", "Unable to close the gate on this end"};
		}
		
		else {
			return new Object[] {null, "stargate_failure_not_open", "The gate is closed"};
		}
	}
}
