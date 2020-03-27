package mrjake.aunis.tileentity.stargate;

import java.util.List;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState.StargateMilkyWayRendererStateBuilder;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateClassicSpinHelper;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.StargateSpinState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.DHDTile.DHDUpgradeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StargateMilkyWayBaseTile extends StargateClassicBaseTile implements ILinkable {
		
	// ------------------------------------------------------------------------
	// Stargate state
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		if (isLinked())
			getLinkedDHD(world).clearSymbols();
	}
	
	@Override
	protected void failGate() {
		super.failGate();
		
		if (isLinked())
			getLinkedDHD(world).clearSymbols();
	}
	
	@Override
	protected void addDialingFailedTask() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, stargateState.dialingComputer() ? 83 : 53));
	}
	
	@Override
	public void onBlockBroken() {
		super.onBlockBroken();
		
		if (isLinked()) {
			getLinkedDHD(world).clearSymbols();
			getLinkedDHD(world).setLinkedGate(null);
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Stargate connection
	
	@Override
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		super.openGate(targetGatePos, isInitiating);
		
		if (isLinked()) {
			getLinkedDHD(world).activateSymbol(SymbolMilkyWayEnum.BRB);
		}
	}	
	
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.MILKYWAY;
	}
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return getStargateSizeConfig(server).teleportBox;
	}
	
	public void addSymbolToAddressDHD(SymbolMilkyWayEnum symbol) {		
		addSymbolToAddress(symbol);
		stargateState = EnumStargateState.DIALING;
		
		NBTTagCompound taskData = new NBTTagCompound();
		
		if (stargateWillLock(symbol)) {
			isFinalActive = true;
			taskData.setBoolean("final", true);
		}
		
		sendSignal(null, "stargate_dhd_chevron_engaged", new Object[] { dialedAddress.size(), isFinalActive, symbol.englishName });
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ACTIVATE_CHEVRON, 10, taskData));
				
		markDirty();
	}
	
	@Override
	protected int getMaxChevrons() {
		if (isLinked()) {
			switch (getLinkedDHD(world).upgradeInstalledCount(DHDUpgradeEnum.CHEVRON_UPGRADE)) {
				case 0: return 7;
				case 1: return 8;
				case 2: return 9;
			}
		}
		
		return 9;
	}
	
	@Override
	public void addSymbolToAddress(SymbolInterface symbol) {
		if (symbol.origin() && dialedAddress.equals(StargateNetwork.EARTH_ADDRESS) && !network.isStargateInNetwork(StargateNetwork.EARTH_ADDRESS)) {
			dialedAddress.clear();
			
			for (int i=0; i<6; i++)
				dialedAddress.addSymbol(network.getLastActivatedOrlins().get(i));
		}
		
		super.addSymbolToAddress(symbol);
		Aunis.info("dialed: " + dialedAddress);

		if (isLinked()) {
			getLinkedDHD(world).activateSymbol((SymbolMilkyWayEnum) symbol);
		}
	}
	
	@Override
	public void incomingWormhole(int dialedAddressSize) {
		super.incomingWormhole(dialedAddressSize);
						
		if (isLinked()) {
			getLinkedDHD(world).clearSymbols();
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Merging
	
	@Override
	protected void unmergeGate() {
		if (isLinked()) {
			getLinkedDHD(world).setLinkedGate(null);
			setLinkedDHD(null);
		}
	}
	
	@Override
	protected void mergeGate() {
		BlockPos closestDhd = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.DHD_BLOCK);
		
		if (closestDhd != null) {
			DHDTile dhdTile = (DHDTile) world.getTileEntity(closestDhd);
			
			dhdTile.setLinkedGate(pos);
			setLinkedDHD(closestDhd);
		}
	}
	
	@Override
	protected StargateAbstractMergeHelper getMergeHelper() {
		return StargateMilkyWayMergeHelper.INSTANCE;
	}

	
	// ------------------------------------------------------------------------
	// Linking
	
	private BlockPos linkedDHD = null;
	
	@Nullable
	public DHDTile getLinkedDHD(World world) {
		if (linkedDHD == null)
			return null;
		
		return (DHDTile) world.getTileEntity(linkedDHD);
	}
	
	@Override
	public boolean isLinked() {
		return linkedDHD != null;
	}
	
	public void setLinkedDHD(BlockPos dhdPos) {		
		this.linkedDHD = dhdPos;
		
		markDirty();
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
		
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (isLinked())
			compound.setLong("linkedDHD", linkedDHD.toLong());
						
		compound.setInteger("stargateSize", stargateSize.id);

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
		if (compound.hasKey("linkedDHD"))
			this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );
				
		if (compound.hasKey("patternVersion"))
			stargateSize = StargateSizeEnum.SMALL;
		else {
			if (compound.hasKey("stargateSize"))
				stargateSize = StargateSizeEnum.fromId(compound.getInteger("stargateSize"));
			else
				stargateSize = StargateSizeEnum.LARGE;
		}
		
		isSpinning = compound.getBoolean("isSpinning");
		locking = compound.getBoolean("locking");
		spinStartTime = compound.getLong("spinStartTime");
		currentRingSymbol = SymbolMilkyWayEnum.valueOf(compound.getInteger("currentRingSymbol"));
		targetRingSymbol = SymbolMilkyWayEnum.valueOf(compound.getInteger("targetRingSymbol"));
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("spinDirection"));
		
		super.readFromNBT(compound);
	}
	
	@Override
	public void prepare() {
		super.prepare();
		setLinkedDHD(null);
	}
	
	// ------------------------------------------------------------------------
	// Ring spinning
	
	private boolean isSpinning;
	private boolean locking;
	private long spinStartTime;
	private SymbolInterface currentRingSymbol = SymbolMilkyWayEnum.ORIGIN;
	private SymbolInterface targetRingSymbol = SymbolMilkyWayEnum.ORIGIN;
	private EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	private Object ringSpinContext;
	
	public void addSymbolToAddressManual(SymbolInterface targetSymbol, @Nullable Object context) {
		targetRingSymbol = targetSymbol;
		
		boolean moveOnly = targetRingSymbol == currentRingSymbol;
		locking = (dialedAddress.size() == 7) || (dialedAddress.size() == 6 && targetRingSymbol.origin());
		
		if (moveOnly) {
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, 0));
		}
		
		else {
			float distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			
//			if (distance < StargateClassicSpinHelper.getMinimalDistance() || distance > (360 - StargateClassicSpinHelper.getMinimalDistance())) {
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
			stargateState = EnumStargateState.DIALING_COMPUTER;
			
			ringSpinContext = context;
			if (context != null)
				sendSignal(context, "stargate_spin_start", new Object[] { dialedAddress.size(), locking, targetSymbol.getEnglishName() });
		}
			
		markDirty();
	}
	
	
	// ------------------------------------------------------------------------
	// Sounds
	
	@Override
	protected SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum) {
		switch (soundEnum) {
			case GATE_RING_ROLL: return SoundPositionedEnum.MILKYWAY_RING_ROLL;
		}
		
		return null;
	}

	@Override
	protected SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum) {
		switch (soundEnum) {
			case OPEN: return SoundEventEnum.GATE_MILKYWAY_OPEN;
			case CLOSE: return SoundEventEnum.GATE_MILKYWAY_CLOSE;
			case DIAL_FAILED: return stargateState.dialingComputer() ? SoundEventEnum.GATE_MILKYWAY_DIAL_FAILED_COMPUTER : SoundEventEnum.GATE_MILKYWAY_DIAL_FAILED;
			case INCOMING: return SoundEventEnum.GATE_MILKYWAY_INCOMING;
			case CHEVRON_OPEN: return SoundEventEnum.GATE_MILKYWAY_CHEVRON_OPEN;
			case CHEVRON_SHUT: return SoundEventEnum.GATE_MILKYWAY_CHEVRON_SHUT;
		}
		
		return null;
	}
	
	
	// ------------------------------------------------------------------------
	// Ticking and loading
	
	@Override
	public BlockPos getGateCenterPos() {
		return pos.offset(EnumFacing.UP, 4);
	}
	
	private boolean firstTick = true;
	
	@Override
	public void update() {
		super.update();
		
		if (!world.isRemote) {
			if (firstTick) {
				firstTick = false;
				
				// Doing this in onLoad causes ConcurrentModificationException
				if (stargateSize != AunisConfig.stargateSize && isMerged()) {
					StargateMilkyWayMergeHelper.INSTANCE.convertToPattern(world, pos, facing, stargateSize, AunisConfig.stargateSize);
					updateMergeState(StargateMilkyWayMergeHelper.INSTANCE.checkBlocks(world, pos, facing), facing);
					
					stargateSize = AunisConfig.stargateSize;
					markDirty();
				}
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Killing and block vaporizing                                                                          
		
	@Override
	protected AunisAxisAlignedBB getHorizonKillingBox(boolean server) {
		return getStargateSizeConfig(server).killingBox;
	}
	
	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return getStargateSizeConfig(server).horizonSegmentCount;
	}
	
	@Override
	protected  List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {		
		return getStargateSizeConfig(server).gateVaporizingBoxes;
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	private StargateSizeEnum stargateSize = AunisConfig.stargateSize;
	
	/**
	 * Returns stargate state either from config or from client's state.
	 * THIS IS NOT A GETTER OF stargateSize.
	 * 
	 * @param server Is the code running on server
	 * @return Stargate's size
	 */
	private StargateSizeEnum getStargateSizeConfig(boolean server) {
		return server ? AunisConfig.stargateSize : getRendererStateClient().stargateSize;
	}
		
	@Override
	protected StargateMilkyWayRendererStateBuilder getRendererStateServer() {		
		return new StargateMilkyWayRendererStateBuilder(super.getRendererStateServer())
				.setStargateSize(stargateSize)
				.setCurrentRingSymbol((SymbolMilkyWayEnum) currentRingSymbol)
				.setSpinDirection(spinDirection)
				.setSpinning(isSpinning)
				.setTargetRingSymbol((SymbolMilkyWayEnum) targetRingSymbol)
				.setSpinStartTime(spinStartTime);
	}
	
	@Override
	protected StargateAbstractRendererState createRendererStateClient() {
		return new StargateMilkyWayRendererState();
	}
	
	@Override
	public StargateMilkyWayRendererState getRendererStateClient() {
		return (StargateMilkyWayRendererState) super.getRendererStateClient();
	}
	
	
	// -----------------------------------------------------------------
	// States
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {				
			case SPIN_STATE:
				return new StargateSpinState();
				
			default:
				return super.createState(stateType);
		}
	}
		
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {		
		switch (stateType) {		
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;
				
				switch (gateActionState.action) {					
					case CHEVRON_OPEN:
						getRendererStateClient().openChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_CLOSE:
						getRendererStateClient().closeChevron(world.getTotalWorldTime());
						break;
						
					default:
						super.setState(stateType, gateActionState);
						break;
				}
				
				break;
				
			case SPIN_STATE:
				StargateSpinState spinState = (StargateSpinState) state;
				getRendererStateClient().spinHelper.initRotation(world.getTotalWorldTime(), (SymbolMilkyWayEnum) spinState.targetSymbol, spinState.direction);
				
				break;
				
			default:
				super.setState(stateType, state);
		}
	}
	
	
	// -----------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_SPIN_FINISHED:
				isSpinning = false;
				currentRingSymbol = targetRingSymbol;
				
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN, 11));
				super.executeTask(scheduledTask, customData);
				
				break;
				
			case STARGATE_CHEVRON_OPEN:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				sendRenderingUpdate(EnumGateAction.CHEVRON_OPEN, 0, false);
				
				if (canAddSymbol(targetRingSymbol)) {
					addSymbolToAddress(targetRingSymbol);
					
					if (locking) {
						if (checkDialedAddress().ok()) {
							addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SECOND, 8));
						}
						
						else
							addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_FAIL, 60));
					}
					
					else
						addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SECOND, 8));
				}
				
				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_FAIL, 60));
				
				break;
				
			case STARGATE_CHEVRON_OPEN_SECOND:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_OPEN);
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_LIGHT_UP, 4));
				
				break;
				
			case STARGATE_CHEVRON_LIGHT_UP:
				if (locking)
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, 0, true);
				else
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE_BOTH, 0, false);
				
				updateChevronLight();
				
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_CLOSE, 14));

				break;
				
			case STARGATE_CHEVRON_CLOSE:
				playSoundEvent(StargateSoundEventEnum.CHEVRON_SHUT);
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);
				
				if (locking) {
					stargateState = EnumStargateState.IDLE;
					sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), locking, targetRingSymbol.getEnglishName() });
				} else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_DIM, 6));
				
				break;
				
			case STARGATE_MANUAL_OPEN:
				attemptOpenDialed();
				
				break;
				
			case STARGATE_CHEVRON_DIM:
				sendRenderingUpdate(EnumGateAction.CHEVRON_DIM, 0, false);
				stargateState = EnumStargateState.IDLE;
				
				sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), locking, targetRingSymbol.getEnglishName() });
				
				break;
				
			case STARGATE_CHEVRON_FAIL:
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);
				dialingFailed();
								
				break;
				
			default:
				super.executeTask(scheduledTask, customData);
		}
	}
	
	// -----------------------------------------------------------------
	// Power system
	
//	@Override
//	protected IEnergyStorage getEnergyStorage(int minEnergy) {
//		if (isLinked()) {
//			ItemStackHandler dhdItemStackHandler = (ItemStackHandler) getLinkedDHD(world).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
//			ItemStack crystalItemStack = dhdItemStackHandler.getStackInSlot(0);
//					
//			if (!crystalItemStack.isEmpty()) {
//				IEnergyStorage crystalEnergyStorage = crystalItemStack.getCapability(CapabilityEnergy.ENERGY, null);
//			
//				if (crystalEnergyStorage.getEnergyStored() >= minEnergy)
//					return crystalEnergyStorage;
//			}
//		}
//		
//		return super.getEnergyStorage(minEnergy);
//	}
	
	
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
		
		SymbolMilkyWayEnum targetSymbol = null;
		
		if (args.isInteger(0))
			targetSymbol = SymbolMilkyWayEnum.valueOf(args.checkInteger(0));
		else if (args.isString(0))
			targetSymbol = SymbolMilkyWayEnum.fromEnglishName(args.checkString(0));
		
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
