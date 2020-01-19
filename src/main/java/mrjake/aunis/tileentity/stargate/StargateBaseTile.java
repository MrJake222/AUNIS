package mrjake.aunis.tileentity.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.capability.EnergyStorageUncapped;
import mrjake.aunis.integration.opencomputers.OCHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.packet.state.StateUpdateRequestToServer;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.renderer.state.RendererGateActionState;
import mrjake.aunis.renderer.state.RendererGateActionState.EnumGateAction;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.AutoCloseManager;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.FlashState;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.State;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tesr.SpecialRendererProviderInterface;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.ScheduledTask;
import mrjake.aunis.tileentity.tasks.IScheduledTaskExecutor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

//@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public abstract class StargateBaseTile extends TileEntity implements SpecialRendererProviderInterface, ITileEntityStateProvider, ITickable, ICapabilityProvider, IScheduledTaskExecutor, Environment {
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	protected EnumStargateState stargateState = EnumStargateState.IDLE;
	
	public final EnumStargateState getStargateState() {
		return stargateState;
	}
	
	private boolean isInitiating;
		
	/**
	 * Set on engageGate(). Indicates when the gate has been opened.
	 * 
	 * Used to calculate energy use while not loaded 
	 */
	public long gateOpenTime;
	
	/**
	 * How much energy gate already consumed to keep the connection alive
	 * 
	 * Subtracted from calculated energy use at the closeGate()
	 */
	public int energyConsumed;
	
	public final void updateEnergyStatus() {
		long ticks = world.getTotalWorldTime() - gateOpenTime;
		int energy = (int) (ticks * keepAliveCostPerTick);
				
		energy -= energyConsumed;
		
		energyStorage.extractEnergyUncapped(energy);
	}
	
	private void engageGate() {	
		gateOpenTime = world.getTotalWorldTime();
		energyConsumed = 0;
		
		stargateState = isInitiating ? EnumStargateState.ENGAGED_INITIATING : EnumStargateState.ENGAGED;
		getEventHorizon().reset();
		getAutoCloseManager().reset();
		
		markDirty();
	}
	
	private void disconnectGate() {	
		stargateState = EnumStargateState.IDLE;
				
		markDirty();
	}
	
	private static final List<EnumGateAction> ACTIONS_SUPPORTED = Arrays.asList(
			EnumGateAction.OPEN_GATE,
			EnumGateAction.CLOSE_GATE);
	
	
	protected boolean isActionSupported(EnumGateAction action) {
		return ACTIONS_SUPPORTED.contains(action);
	}
	
	private void sendRenderingUpdate(EnumGateAction gateAction, boolean computer, int chevronCount) {
		if (isActionSupported(gateAction)) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.RENDERER_UPDATE, new RendererGateActionState(gateAction, computer, chevronCount)), targetPoint);
		}
	}
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	protected EventHorizon eventHorizon;
	private AutoCloseManager autoCloseManager;
	
	protected abstract EventHorizon getEventHorizon();
	
	private AutoCloseManager getAutoCloseManager() {
		if (autoCloseManager == null)
			autoCloseManager = new AutoCloseManager(this);
		
		return autoCloseManager;
	}
	
//	private EventHorizon getEventHorizon() {
//		if (eventHorizon == null)
//			eventHorizon = new EventHorizon(world, pos, widthLeft, widthRight, height)
//		
//		return eventHorizon;
//	}
	
	public void setMotionOfPassingEntity(int entityId, Vector2f motionVector) {
		getEventHorizon().setMotion(entityId, motionVector);
	}
	
	public void teleportEntity(int entityId) {
		getEventHorizon().teleportEntity(entityId);
	}
	
	public void removeEntity(int entityId) {
		getEventHorizon().removeEntity(entityId);
	}
	
	public List<EnumSymbol> gateAddress = null;
	public List<EnumSymbol> dialedAddress = new ArrayList<EnumSymbol>();
	
	public int getEnteredSymbolsCount() {
		return dialedAddress.size();
	}
	
	public List<EnumSymbol> generateAddress() {			
		if ( gateAddress == null ) {
			Random rand = new Random(pos.hashCode() * 31 + world.provider.getDimension());
			List<EnumSymbol> address = new ArrayList<EnumSymbol>(); 
				
			while (true) {
				address.clear();
				
				while (address.size() < 7) {
					EnumSymbol symbol = EnumSymbol.valueOf( rand.nextInt(38) );
						
					if ( !address.contains(symbol) && symbol != EnumSymbol.ORIGIN ) {
						address.add(symbol);
					}
				}
				
				// Check if SOMEHOW Stargate with the same address doesn't exists
				if ( StargateNetwork.get(world).checkForStargate(address) )
					rand = new Random();
				else
					break;
			}
						
			gateAddress = address;
			markDirty();
							
			// Add Stargate to the "network" - WorldSavedData
			StargateNetwork.get(world).addStargate(gateAddress, world.provider.getDimension(), pos);
		}
		
		return gateAddress;
	}
	
	protected abstract int getMaxChevrons(boolean computer, DHDTile dhdTile);
	protected abstract void firstGlyphDialed(boolean computer);
	protected abstract void lastGlyphDialed(boolean computer);
	
	protected abstract void dialingFailed(boolean stopRing);
	
	/**
	 * Adds symbol to address. Called from GateRenderingUpdatePacketToServer. Handles all server-side consequences:
	 * 	- server ring movement cache
	 * 	- renderer's state
	 * 
	 * @param symbol - Currently added symbol
	 * @param dhdTile - Clicked DHD's Tile instance
	 * @return true if symbol was added
	 */
	public final boolean addSymbolToAddress(EnumSymbol symbol, DHDTile dhdTile, boolean computer) {		
		if (dialedAddress.contains(symbol)) 
			return false;
		
		int maxChevrons = getMaxChevrons(computer, dhdTile);
		
		if (dialedAddress.size() == maxChevrons)
			return false;
		
		// First glyph is pressed
		// Ring starts to spin
		if (dialedAddress.size() == 0) {
			firstGlyphDialed(computer);
		}
		
		dialedAddress.add(symbol);
		if (dhdTile != null)
			dhdTile.getDHDRendererState().activeButtons.add(symbol.id);
		
		if (dialedAddress.size() == maxChevrons || (dialedAddress.size() == 7 && symbol == EnumSymbol.ORIGIN)) {
			getRendererState().setFinalActive(world, pos, true);
			
			if (!computer)
				sendRenderingUpdate(EnumGateAction.ACTIVATE_FINAL, computer, 0);
			
			lastGlyphDialed(computer);
		}
		
		else {			
			if (!computer) {
				sendRenderingUpdate(EnumGateAction.ACTIVATE_NEXT, computer, 0);
			}
		}
		
		getRendererState().setActiveChevrons(world, pos, getRendererState().getActiveChevrons() + 1);
		
		markDirty();
		
		return true;
	}
	
	/**
	 * Called on receiving gate. Sets renderer's state
	 * 
	 * @param incomingAddress - Initializing gate's address
	 * @param dialedAddressSize - How many symbols are there pressed on the DHD
	 */
	public final void incomingWormhole(List<EnumSymbol> incomingAddress, int dialedAddressSize) {
		Aunis.info("incoming size: " + dialedAddressSize);
		
		getRendererState().setActiveChevrons(world, pos, dialedAddressSize - 1);
		getRendererState().setFinalActive(world, pos, true);
		
		DHDTile dhdTile = getLinkedDHD(world);
		
		if (dhdTile != null) {			
			dhdTile.getDHDRendererState().activeButtons = EnumSymbol.toIntegerList(incomingAddress.subList(0, dialedAddressSize - 1), EnumSymbol.ORIGIN);
		}
		
		sendSignal(null, "stargate_incoming_wormhole", new Object[] { dialedAddressSize });
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, false, dialedAddressSize);
	}
	
	/**
	 * Called on BRB press. Initializes renderer's state
	 * 
	 * @param initiating - true if gate is initializing the connection
	 * @param dialedAddressSize - Glyph count on initiating DHD
	 * @param incomingAddress - Source gate address
	 */
	public final void openGate(boolean initiating, int dialedAddressSize, List<EnumSymbol> incomingAddress) {
		stargateState = EnumStargateState.UNSTABLE;
		isInitiating = initiating;
		
		if (!isInitiating) {
			dialedAddress.clear();
			dialedAddress.addAll(incomingAddress);
		}
		
		else {
			dialedAddressSize = dialedAddress.size();
		}
		
		sendRenderingUpdate(EnumGateAction.OPEN_GATE, false, 0);
		getRendererState().setStargateOpen(world, pos, dialedAddressSize, isInitiating);		
		
		addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_OPEN_SOUND));
		addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_ENGAGE));
		
		DHDTile dhdTile = getLinkedDHD(world);
		if (dhdTile != null) {
			dhdTile.getDHDRendererState().activeButtons.add(EnumSymbol.BRB.id);
		}
		
		if (isInitiating) {
			((EnergyStorageUncapped) getEnergyStorage(openCost)).extractEnergyUncapped(openCost);
		}
		
//		stargateState = EnumStargateState.ENGAGED;
		sendSignal(null, "stargate_open", new Object[] { isInitiating });
		
		markDirty();
	}
	
	/**
	 * Called either on pressing BRB on open gate or by pressing BRB on malformed address
	 * 
	 * @param dialingFailed - True if second case above
	 * @param stopRing - If using DHD, then true
	 */
	public final void closeGate(boolean dialingFailed, boolean stopRing) {		
		if (!dialingFailed) {		
			stargateState = EnumStargateState.UNSTABLE;
			
			addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CLOSE));
			sendSignal(null, "stargate_close", new Object[] {});
			
			sendRenderingUpdate(EnumGateAction.CLOSE_GATE, !stopRing, 0);
		}
		
		else {
			dialingFailed(stopRing);
			
			stargateState = EnumStargateState.FAILING;
			
			AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_DIAL_FAILED, 0.3f);
			
			addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_FAIL));
			sendSignal(null, "stargate_failed", new Object[] {});
			
			sendRenderingUpdate(EnumGateAction.GATE_DIAL_FAILED, !stopRing, 0);
		}
			
		if (!(this instanceof StargateBaseTileOrlin))
			dialedAddress.clear();
		
		horizonFlashTask = null;
		
		getRendererState().setStargateClosed(world, pos);
		clearLinkedDHDButtons(dialingFailed);

		markDirty();
	}	
	
	// ------------------------------------------------------------------------
	// Ticking and loading
	
	protected TargetPoint targetPoint;
	
	@Override
	public void onLoad() {		
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			
			generateAddress();
			Network.joinOrCreateNetwork(this);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), EnumStateType.RENDERER_STATE));
		}
	}
	
	@Override
	public void update() {
		
		if (!world.isRemote) {
			
			// Event horizon teleportation			
			if (stargateState == EnumStargateState.ENGAGED_INITIATING) {
				getEventHorizon().scheduleTeleportation(StargateNetwork.get(world).getStargate(dialedAddress));
			}
			
			// Not initiating
			if (stargateState == EnumStargateState.ENGAGED) {
				getAutoCloseManager().update(StargateNetwork.get(world).getStargate(dialedAddress));
			}
						
			// Scheduled tasks
			for (int i=0; i<scheduledTasks.size();) {			
				ScheduledTask scheduledTask = scheduledTasks.get(i);
				
				if (scheduledTask.isActive()) {								
					if (scheduledTask.update(world.getTotalWorldTime()))
						scheduledTasks.remove(scheduledTask);
					
					else i++;
				}
				
				else i++;
			}
			
			if (horizonFlashTask != null && horizonFlashTask.isActive()) {
				horizonFlashTask.update(world.getTotalWorldTime());
			}
			
			/*
			 * Draw power (engaged)
			 * 
			 * If initiating
			 * 	True: Extract energy each tick
			 * 	False: Update the source gate about consumed energy each second
			 */
			if (stargateState.engaged()) {
				if (stargateState.initiating()) {

					// If we have enough energy(minimal DHD operable)
					if (getEnergyStorage(AunisConfig.powerConfig.dhdMinimalEnergy * 2) != null) {
						IEnergyStorage energyStorage = getEnergyStorage(keepAliveCostPerTick);
						
						if (energyStorage != null) {
							int sec = keepAliveCostPerTick*20;
	
							/*
							 * If energy can sustain connection for less than AunisConfig.powerConfig.instabilitySeconds seconds
							 * Start flickering
							 */
							
							// Horizon becomes unstable
							if (horizonFlashTask == null && energyStorage.getEnergyStored() < sec*AunisConfig.powerConfig.instabilitySeconds) {
								resetFlashingSequence();
								
								horizonFlashTask = new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.HORIZON_FLASH, (int) (Math.random() * 40) + 5);
							}
							
							// Horizon becomes stable
							if (horizonFlashTask != null && energyStorage.getEnergyStored() > sec*AunisConfig.powerConfig.instabilitySeconds) {
								horizonFlashTask = null;
								isCurrentlyUnstable = false;
								
								updateFlashState(false);
							}
							
							energyConsumed += ((EnergyStorageUncapped) energyStorage).extractEnergyUncapped(keepAliveCostPerTick);
							
							markDirty();
	//						Aunis.info("Stargate energy: " + energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored() + "\t\tAlive for: " + (float)(energyStorage.getEnergyStored())/keepAliveCostPerTick/20);
						}
						
						else
							GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
					}
					
					else {
						GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
					}
				}
				
				else {
					if (world.getTotalWorldTime() % 20 == 0 && dialedAddress.size() > 0) {
						StargatePos sourcePos = StargateNetwork.get(world).getStargate(dialedAddress);
						
						// Only if not loaded
						if (!sourcePos.getWorld().isBlockLoaded(sourcePos.getPos())) {					
							StargateBaseTileSG1 sourceTile = (StargateBaseTileSG1) sourcePos.getWorld().getTileEntity(sourcePos.getPos());
							
							sourceTile.updateEnergyStatus();
						}
					}
				}
			}
		}
	}
	

	// ------------------------------------------------------------------------
	// Rendering
	
	// TODO Make getRenderer() private
	public abstract StargateRendererBase getRenderer();
	protected abstract StargateRendererStateBase getRendererState();
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		getRenderer().render(x, y, z, partialTicks);
				
		if (this instanceof ITileEntityUpgradeable) {
			((ITileEntityUpgradeable) this).getUpgradeRenderer().render(x, y, z, partialTicks);
		}
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		if (oldState.getBlock() == newSate.getBlock())
			return oldState.withProperty(AunisProps.RENDER_BLOCK, false) != newSate.withProperty(AunisProps.RENDER_BLOCK, false);
		
		return true;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos().add(-7, 0, -7), getPos().add(7, 12, 7));
	}
		
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}	
	
	// ------------------------------------------------------------------------
	// DHD
	
	public abstract DHDTile getLinkedDHD(World world);
	protected abstract void clearLinkedDHDButtons(boolean dialingFailed);
	
//	private BlockPos linkedDHD;
//	
//	@Nullable
//	public DHDTile getLinkedDHD(World world) {
//		if (linkedDHD == null)
//			return null;
//		
//		return (DHDTile) world.getTileEntity(linkedDHD);
//	}
//	
//	public void setLinkedDHD(BlockPos dhdPos) {		
//		this.linkedDHD = dhdPos;
//		
//		markDirty();
//	}
//	
//	@Override
//	public boolean isLinked() {
//		return linkedDHD != null;
//	}
	
	
	// ------------------------------------------------------------------------
	// AutoClose
	
	public final void entityPassing(boolean isPlayer, boolean inbound) {
		if (isPlayer) {
			getAutoCloseManager().playerPassing();
			markDirty();
		}
		
		sendSignal(null, "stargate_traveler", new Object[] {inbound, isPlayer});
	}

	
	// -----------------------------------------------------------------
	// Horizon flashing
	private ScheduledTask horizonFlashTask;

	private int flashIndex = 0;
	private boolean isCurrentlyUnstable = false;
	
	private void resetFlashingSequence() {
		flashIndex = 0;
		isCurrentlyUnstable = false;
	}
	
	private void updateFlashState(boolean flash) {
		StargatePos targetPos = StargateNetwork.get(world).getStargate(dialedAddress);
		BlockPos tPos = targetPos.getPos();
				
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.FLASH_STATE, new FlashState(isCurrentlyUnstable)), targetPoint);
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(tPos, EnumStateType.FLASH_STATE, new FlashState(isCurrentlyUnstable)), new TargetPoint(targetPos.getDimension(), tPos.getX(), tPos.getY(), tPos.getZ(), 512));
	}
	
	
	// ------------------------------------------------------------------------
	// States

	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererState();
				
			default:
				return null;
		}
	}

	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererState();
		
			case RENDERER_UPDATE:
				return new RendererGateActionState();
				
			case FLASH_STATE:
				return new FlashState();
				
			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				getRenderer().setRendererState((StargateRendererStateBase) state);
				break;
				
			case RENDERER_UPDATE:
				switch (((RendererGateActionState) state).action) {
					case OPEN_GATE:
						getRenderer().openGate();
						break;
						
					case CLOSE_GATE:
						getRenderer().closeGate();
						break;
						
					default:
						break;					
				}
				
				break;
		
			case FLASH_STATE:
				getRenderer().setHorizonUnstable(((FlashState) state).flash);
				break;
				
			default:
				break;
		}
	}
	
	// ------------------------------------------------------------------------
	// Scheduled tasks
	
	/**
	 * List of scheduled tasks to be performed on {@link ITickable#update()}.
	 */
	private List<ScheduledTask> scheduledTasks = new ArrayList<>();
	
	public void addTask(ScheduledTask scheduledTask) {
		scheduledTasks.add(scheduledTask);
		
		Aunis.info("scheduledTasks add: " + scheduledTasks);
		
		markDirty();
	}	
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask) {
		switch (scheduledTask) {
			case STARGATE_OPEN_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_OPEN, 0.3f);
				break;
				
			case STARGATE_CLOSE:
				disconnectGate();
				break;
				
			case STARGATE_ENGAGE:
				engageGate();
				break;
				
			case STARGATE_FAIL:
				stargateState = EnumStargateState.IDLE;
				markDirty();
				break;
				
			case HORIZON_FLASH:
				isCurrentlyUnstable ^= true;
				
				if (isCurrentlyUnstable) {
					flashIndex++;
					
					if (flashIndex == 1)
						AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.WORMHOLE_FLICKER, 0.5f);
					
					// Schedule change into stable state
					horizonFlashTask = new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 3) + 3);
				}
				
				else {
					if (flashIndex == 1)
						// Schedule second flash
						horizonFlashTask = new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 4) + 1);
					
					else {
						// Schedule next flash sequence
						horizonFlashTask = new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 40) + 5);
						
						resetFlashingSequence();
					}
				}
				
				updateFlashState(isCurrentlyUnstable);
				
				markDirty();				
				break;
		
			default:
				throw new UnsupportedOperationException("EnumScheduledTask."+scheduledTask.name()+" not implemented on "+this.getClass().getName());
		}
	}
	
	
	// -----------------------------------------------------------------
	// Power system
	
	private int openCost = 0;
	private int keepAliveCostPerTick = 0;
	
	protected EnergyStorageUncapped energyStorage = new EnergyStorageUncapped(AunisConfig.powerConfig.stargateEnergyStorage, AunisConfig.powerConfig.stargateMaxEnergyTransfer) {
		protected void onEnergyChanged() {			
			markDirty();
		};
	};
	
	/**
	 * Unifies energy consumption methods
	 * The order is as follows:
	 *   - Gate internal buffer
	 *   - DHD Crystal
	 *   
	 * @param minEnergy - minimal energy 
	 * @return First IEnergyStorage that has enough energy, CAN BE NULL if no source can provide such energy
	 */
	@Nullable
	private IEnergyStorage getEnergyStorage(int minEnergy) {
		if (energyStorage.getEnergyStored() >= minEnergy)
			return energyStorage;
		
		DHDTile dhdTile = getLinkedDHD(world);
		if (dhdTile != null) {
			ItemStackHandler dhdItemStackHandler = (ItemStackHandler) dhdTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			ItemStack crystalItemStack = dhdItemStackHandler.getStackInSlot(0);
					
			if (!crystalItemStack.isEmpty()) {
				IEnergyStorage crystalEnergyStorage = crystalItemStack.getCapability(CapabilityEnergy.ENERGY, null);
			
				if (crystalEnergyStorage.getEnergyStored() >= minEnergy)
					return crystalEnergyStorage;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if this {@link StargateBaseTile} can provide enough energy.
	 * 
	 * @param energy Energy required.
	 * @return Will it suffice?
	 */
	public boolean hasEnergy(int energy) {
		return getEnergyStorage(energy) != null;
	}
	
	/**
	 * Checks is gate has sufficient power to dial across specified distance and dimension
	 * It also sets energy draw for (possibly) outgoing wormhole
	 * 
	 * @param distance - distance in blocks to target gate
	 * @param targetWorld - target world, used for multiplier
	 */
	public boolean hasEnergyToDial(int distance, double multiplier) {		
		/* double mul;
		
		switch (targetWorld.provider.getDimensionType()) {
			case NETHER: mul = AunisConfig.netherMultiplier; break;
			case THE_END: mul = AunisConfig.theEndMultiplier; break;
		
			// No multiplier
			default: mul = 1; break;
		}*/
		
		int energy = (int) (distance * AunisConfig.powerConfig.openingBlockToEnergyRatio * multiplier);
		int keepAlive = (int) Math.ceil(distance * AunisConfig.powerConfig.keepAliveBlockToEnergyRatioPerTick * multiplier);
		
		Aunis.info("Energy required to dial [distance="+distance+", mul="+multiplier+"] = " + energy + " / keepAlive: "+(keepAlive*20)+"/s, stored: " + (getEnergyStorage(1) != null ? getEnergyStorage(1).getEnergyStored() : 0));
				
		if (getEnergyStorage(energy) != null) {
			this.openCost = energy;
			this.keepAliveCostPerTick = keepAlive;
		
			markDirty();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY) || super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return (T)energyStorage;
		}
		
		return super.getCapability(capability, facing);
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {				
		if (gateAddress != null) {
			for (int i=0; i<7; i++) {
				compound.setInteger("symbol"+i, gateAddress.get(i).id);
			}
		}
		
		compound.setInteger("dialedAddressLength", dialedAddress.size());
		
		for (int i=0; i<dialedAddress.size(); i++) {
			compound.setInteger("dialedSymbol"+i, dialedAddress.get(i).id);
		}
				
		compound.setTag("autoCloseManager", getAutoCloseManager().serializeNBT());
		compound.setTag("rendererState", getRendererState().serializeNBT());
		compound.setTag("energyStorage", energyStorage.serializeNBT());
		
		compound.setInteger("openCost", openCost);
		compound.setInteger("keepAliveCostPerTick", keepAliveCostPerTick);
		
		compound.setLong("gateOpenTime", gateOpenTime);
		compound.setInteger("energyConsumed", energyConsumed);
		
		if (stargateState != null)
			compound.setInteger("stargateState", stargateState.id);
		
		for (int i=0; i<scheduledTasks.size(); i++)
			compound.setTag("scheduledTask"+i, scheduledTasks.get(i).serializeNBT());
		
		if (node != null) {
			NBTTagCompound nodeCompound = new NBTTagCompound();
			node.save(nodeCompound);
			
			compound.setTag("node", nodeCompound);
		}
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {		
		if (compound.hasKey("symbol0")) {		
			gateAddress = new ArrayList<EnumSymbol>();
			
			for (int i=0; i<7; i++) {
				int id = compound.getInteger("symbol"+i);
				gateAddress.add( EnumSymbol.valueOf(id) );
			}
		}
		
		dialedAddress.clear();
		int dialedAddressLength = compound.getInteger("dialedAddressLength");
		
		for (int i=0; i<dialedAddressLength; i++) {
			dialedAddress.add( EnumSymbol.valueOf(compound.getInteger("dialedSymbol"+i)) );
		}
				
		getAutoCloseManager().deserializeNBT((NBTTagCompound) compound.getTag("autoCloseManager"));
		
		try {
			getRendererState().deserializeNBT((NBTTagCompound) compound.getTag("rendererState"));		
		}
		
		catch (NullPointerException | IndexOutOfBoundsException e) {
			Aunis.info("Exception at reading RendererState");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}
		
		energyStorage.deserializeNBT((NBTTagCompound) compound.getTag("energyStorage"));
		
		this.openCost = compound.getInteger("openCost");
		this.keepAliveCostPerTick = compound.getInteger("keepAliveCostPerTick");
		
		this.gateOpenTime = compound.getLong("gateOpenTime");
		this.energyConsumed = compound.getInteger("energyConsumed");
		
		stargateState = EnumStargateState.valueOf(compound.getInteger("stargateState"));
		
		for (int i=0; i<scheduledTasks.size(); i++)
			scheduledTasks.add(new ScheduledTask(this, (NBTTagCompound) compound.getTag("scheduledTask"+i)));
		
		if (node != null && compound.hasKey("node"))
			node.load((NBTTagCompound) compound.getTag("node"));
		
		super.readFromNBT(compound);
	}
	
	
	// ------------------------------------------------------------------------
	// OpenComputers
	
	// ------------------------------------------------------------
	// Node-related work
	private Node node = Network.newNode(this, Visibility.Network).withComponent("stargate", Visibility.Network).create();
	
	@Override
	public Node node() {
		return node;
	}

	@Override
	public void onConnect(Node node) {}

	@Override
	public void onDisconnect(Node node) {}

	@Override
	public void onMessage(Message message) {}
	
	@Override
	public void onChunkUnload() {
		if (node != null)
			node.remove();
		
		super.onChunkUnload();
	}

	@Override
	public void invalidate() {
		if (node != null)
			node.remove();
		
		super.invalidate();
	}
	
	public void sendSignal(Object context, String name, Object... params) {
		OCHelper.sendSignalToReachable(node, (Context) context, name, params);
	}
	
	// ------------------------------------------------------------
	// Methods
	// function(arg:type[, optionArg:type]):resultType; Description.
	@Callback(getter = true)
	public Object[] stargateAddress(Context context, Arguments args) {
		return new Object[] {gateAddress};
	}

	@Callback(getter = true)
	public Object[] dialedAddress(Context context, Arguments args) {
		return new Object[] {dialedAddress};
	}
}
