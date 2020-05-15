package mrjake.aunis.tileentity.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import li.cil.oc.api.network.Packet;
import li.cil.oc.api.network.WirelessEndpoint;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisDamageSources;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.chunkloader.ChunkManager;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateDimensionConfig;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.particle.ParticleWhiteSmoke;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState.StargateAbstractRendererStateBuilder;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.AutoCloseManager;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateClosedReasonEnum;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.stargate.power.StargateEnergyRequired;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.state.StargateFlashState;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.StargateVaporizeBlockParticlesRequest;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.util.PreparableInterface;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.tileentity.util.ScheduledTaskExecutorInterface;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.InterfaceList({
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers"),
	@Optional.Interface(iface = "li.cil.oc.api.network.WirelessEndpoint", modid = "opencomputers")})
public abstract class StargateAbstractBaseTile extends TileEntity implements StateProviderInterface, ITickable, ICapabilityProvider, ScheduledTaskExecutorInterface, Environment, WirelessEndpoint, PreparableInterface {
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	protected EnumStargateState stargateState = EnumStargateState.IDLE;
	
	public final EnumStargateState getStargateState() {
		return stargateState;
	}
	
	private boolean isInitiating;

	protected void engageGate() {	
		stargateState = isInitiating ? EnumStargateState.ENGAGED_INITIATING : EnumStargateState.ENGAGED;
		eventHorizon.reset();
		
		AunisSoundHelper.playPositionedSound(world, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, true);
		sendSignal(null, "stargate_wormhole_stabilized", new Object[] { isInitiating });
		
		markDirty();
	}
	
	protected void disconnectGate() {	
		stargateState = EnumStargateState.IDLE;
		getAutoCloseManager().reset();

		if (!(this instanceof StargateOrlinBaseTile))
			dialedAddress.clear();
		
		ChunkManager.unforceChunk(world, new ChunkPos(pos));
		sendSignal(null, "stargate_wormhole_closed_fully", new Object[] { isInitiating });
		
		markDirty();
	}
	
	protected void failGate() {
		stargateState = EnumStargateState.IDLE;

		if (!(this instanceof StargateOrlinBaseTile))
			dialedAddress.clear();
				
		markDirty();
	}
	
	public void onBlockBroken() {
		for (StargateAddress address : gateAddressMap.values())
			network.removeStargate(address);
	}
	
	protected void onGateBroken() {
		world.setBlockToAir(getGateCenterPos());
		
		if (stargateState.initiating()) {
			attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
		}
		
		else if (stargateState.engaged()) {
			targetGatePos.getTileEntity().attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
		}
		
		dialedAddress.clear();
		targetGatePos = null;
		scheduledTasks.clear();
		stargateState = EnumStargateState.IDLE;
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, 0, false);
		
		ChunkManager.unforceChunk(world, new ChunkPos(pos));
		AunisSoundHelper.playPositionedSound(world, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, false);
		
		markDirty();
	}
	
	protected void onGateMerged() {}
	
	public boolean canAcceptConnectionFrom(StargatePos targetGatePos) {
		return isMerged && stargateState.idle();
	}
		
	protected void sendRenderingUpdate(EnumGateAction gateAction, int chevronCount, boolean modifyFinal) {
		if (targetPoint != null) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(gateAction, chevronCount, modifyFinal)), targetPoint);
		}
	}
	
	/**
	 * Instance of the {@link EventHorizon} for teleporting entities.
	 */
	protected EventHorizon eventHorizon;
	
	public AunisAxisAlignedBB getEventHorizonLocalBox() {
		return eventHorizon.getLocalBox();
	}
	
	/**
	 * Get the bounding box of the horizon.
	 * @param server Calling side.
	 * @return Horizon bounding box.
	 */
	protected abstract AunisAxisAlignedBB getHorizonTeleportBox(boolean server);

	private AutoCloseManager autoCloseManager;
	
	private AutoCloseManager getAutoCloseManager() {
		if (autoCloseManager == null)
			autoCloseManager = new AutoCloseManager(this);
		
		return autoCloseManager;
	}
	
	public void setMotionOfPassingEntity(int entityId, Vector2f motionVector) {
		eventHorizon.setMotion(entityId, motionVector);
	}
	
	public void teleportEntity(int entityId) {
		eventHorizon.teleportEntity(entityId);
	}
	
	public void removeEntity(int entityId) {
		eventHorizon.removeEntity(entityId);
	}
	
	
	// ------------------------------------------------------------------------
	// Stargate connection
	
	/**
	 * Attempts to open the connection to gatepointed by {@link StargateAbstractBaseTile#dialedAddress}.
	 * @return
	 */
	public StargateOpenResult attemptOpenDialed() {
		StargateOpenResult result = checkAddressAndEnergy(dialedAddress);
		
		if (result.ok()) {		
			StargatePos targetGatePos = network.getStargate(dialedAddress);
			StargateAbstractBaseTile targetTile = targetGatePos.getTileEntity();
				
			if (!targetTile.canAcceptConnectionFrom(gatePosMap.get(getSymbolType())))
				return StargateOpenResult.ADDRESS_MALFORMED;
			
			if (!hasEnergyToDial(targetGatePos))
				return StargateOpenResult.NOT_ENOUGH_POWER;
			
			openGate(targetGatePos, true);
			targetTile.openGate(gatePosMap.get(targetGatePos.symbolType), false);
			targetTile.dialedAddress.clear();
			targetTile.dialedAddress.addAll(gateAddressMap.get(targetGatePos.symbolType).subList(0, dialedAddress.size()-1));
			targetTile.dialedAddress.addOrigin();
		}
			
		return result;
	}
	
	/**
	 * Checks if the address can be dialed. 
	 * @param address Address to be checked.
	 * @return {@code True} if the address parameter is valid and the dialed gate can be reached, {@code false} otherwise.
	 */
	protected StargateOpenResult checkAddress(StargateAddressDynamic address) {
		if (!address.validate())
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		if (!canDialAddress(address))
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		StargateAbstractBaseTile targetTile = network.getStargate(address).getTileEntity();
			
		if (!targetTile.canAcceptConnectionFrom(gatePosMap.get(getSymbolType())))
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		return StargateOpenResult.OK;
	}
	
	/**
	 * Checks if the address can be dialed and if the gate has power to do so.
	 * @param address Address to be checked.
	 * @return {@code True} if the address parameter is valid and the dialed gate can be reached, {@code false} otherwise.
	 */
	protected StargateOpenResult checkAddressAndEnergy(StargateAddressDynamic address) {
		StargateOpenResult result = checkAddress(address);
		
		if (!result.ok())
			return result;
		
		StargatePos targetGatePos = network.getStargate(address);
		
		if (!hasEnergyToDial(targetGatePos))
			return StargateOpenResult.NOT_ENOUGH_POWER;
		
		return StargateOpenResult.OK;
	}
	
	/**
	 * Checks if given address points to
	 * a valid target gate (and not to itself).
	 * @param address Address to check,
	 * @return {@code True} if the gate can be reached, {@code false} otherwise.
	 */
	protected boolean canDialAddress(StargateAddressDynamic address) {	
		StargatePos targetGatePos = network.getStargate(address);
		
		if (targetGatePos == null)
			return false;
		
		if (targetGatePos.equals(gatePosMap.get(getSymbolType())))
			return false;
			
		boolean localDial = world.provider.getDimension() == targetGatePos.dimensionID ||
				StargateDimensionConfig.isGroupEqual(world.provider.getDimensionType(), DimensionManager.getProviderType(targetGatePos.dimensionID));
		
		// TODO Optimize this, prevent dimension from loading only to check the SymbolType...
		if (address.size() < getSymbolType().getMinimalSymbolCountTo(targetGatePos.getTileEntity().getSymbolType(), localDial))
			return false;
		
		int additional = address.size() - 7;
		
		if (additional > 0) {
			if (!address.getAdditional().subList(0, additional).equals(targetGatePos.additionalSymbols.subList(0, additional)))
				return false;
		}
		
		return true;
	}
	
	public void attemptClose(StargateClosedReasonEnum reason) {
		if (targetGatePos != null)
			targetGatePos.getTileEntity().closeGate(reason);
		
		closeGate(reason);
	}
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	public abstract SymbolTypeEnum getSymbolType();
	
	/**
	 * Contains instance of {@link StargateAddress} which holds address of this gate.
	 */
	protected Map<SymbolTypeEnum, StargateAddress> gateAddressMap = new HashMap<>(3);
	protected Map<SymbolTypeEnum, StargatePos> gatePosMap = new HashMap<>(3);
	protected StargateAddressDynamic dialedAddress = new StargateAddressDynamic(getSymbolType());
	protected StargatePos targetGatePos;
	
	@Nullable
	public StargateAddress getStargateAddress(SymbolTypeEnum symbolType) {
		if (gateAddressMap == null)
			return null;
		
		return gateAddressMap.get(symbolType);
	}
	
	public void setGateAddress(SymbolTypeEnum symbolType, StargateAddress stargateAddress) {
		network.removeStargate(gateAddressMap.get(symbolType));
		
		StargatePos gatePos = new StargatePos(world.provider.getDimension(), pos, stargateAddress);
		gateAddressMap.put(symbolType, stargateAddress);
		gatePosMap.put(symbolType, gatePos);
		network.addStargate(stargateAddress, gatePos);
		
		markDirty();
	}
	
	public StargateAddressDynamic getDialedAddress() {
		return dialedAddress;
	}
	
	protected int getMaxChevrons() {
		return 7;
	}
	
	protected boolean stargateWillLock(SymbolInterface symbol) {
		if (dialedAddress.size() == getMaxChevrons())
			return true;
		
		if (dialedAddress.size() >= 7 && symbol.origin())
			return true;
		
		return false;
	}
	
//	public void setGateAddress(StargateAddress gateAddress) {
//		if (network.isStargateInNetwork(gateAddress))
//			Aunis.logger.error("Stargate with given address already exists");
//		
//		if (network.isStargateInNetwork(gateAddress))
//			network.removeStargate(this.gateAddress);
//				
//		StargateNetwork.get(world).addStargate(gateAddress, new StargatePos(world.provider.getDimension(), pos, gateAddress));
//		
//		this.gateAddress = gateAddress;
//		markDirty();
//	}
		
	/**
	 * Checks whether the symbol can be added to the address.
	 * 
	 * @param symbol Symbol to be added.
	 * @param manual True if dialing from computer.
	 * @return
	 */
	public boolean canAddSymbol(SymbolInterface symbol) {
		if (dialedAddress.contains(symbol)) 
			return false;
				
		if (dialedAddress.size() == getMaxChevrons())
			return false;
		
		return true;
	}
	
	/**
	 * Adds symbol to address. Called from GateRenderingUpdatePacketToServer.
	 * 
	 * @param symbol Currently added symbol.
	 * @param manual True if dialing from computer.
	 */
	protected void addSymbolToAddress(SymbolInterface symbol) {
		if (!canAddSymbol(symbol))
			throw new IllegalStateException("Cannot add that symbol");
		
		dialedAddress.addSymbol(symbol);
		
		if (stargateWillLock(symbol) && checkAddressAndEnergy(dialedAddress).ok()) {
			int size = dialedAddress.size();
			if (size == 6) size++;
			
			network.getStargate(dialedAddress).getTileEntity().incomingWormhole(size);
		}
	}
	
	/**
	 * Called on receiving gate. Sets renderer's state
	 * 
	 * @param incomingAddress - Initializing gate's address
	 * @param dialedAddressSize - How many symbols are there pressed on the DHD
	 */
	public void incomingWormhole(int dialedAddressSize) {
		dialedAddress.clear();		
		
		sendSignal(null, "stargate_incoming_wormhole", new Object[] { dialedAddressSize });
	}
	
	protected int getOpenSoundDelay() {
		return EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks;
	}
	
	/**
	 * Called on BRB press. Initializes renderer's state
	 * 
	 * @param targetGatePos {@link StargatePos} pointing to the other Gate.
	 * @param isInitiating True if gate is initializing the connection, false otherwise.
	 */
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		this.isInitiating = isInitiating;
		this.targetGatePos = targetGatePos;
		this.stargateState = EnumStargateState.UNSTABLE;
		
		sendRenderingUpdate(EnumGateAction.OPEN_GATE, 0, false);
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_OPEN_SOUND, getOpenSoundDelay()));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_LIGHT_BLOCK, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 19 + getTicksPerHorizonSegment(true)));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 23 + getTicksPerHorizonSegment(true))); // 1.3s of the sound to the kill
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ENGAGE));
		
		if (isInitiating) {
			StargateEnergyRequired energyRequired = getEnergyRequiredToDial(targetGatePos);
			getEnergyStorage().extractEnergy(energyRequired.energyToOpen, false);
			keepAliveEnergyPerTick = energyRequired.keepAlive;
		}
		
		ChunkManager.forceChunk(world, new ChunkPos(pos));
		
		sendSignal(null, "stargate_open", new Object[] { isInitiating });
		
		markDirty();
	}
	
	/**
	 * Called either on pressing BRB on open gate or close command from a computer.
	 */
	public void closeGate(StargateClosedReasonEnum reason) {
//		Aunis.info("closeGate init=" + isInitiating + ", targetGatePos: " + targetGatePos);
		
		stargateState = EnumStargateState.UNSTABLE;
		energySecondsToClose = 0;
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CLOSE, 62));
		
		playSoundEvent(StargateSoundEventEnum.CLOSE);
		sendRenderingUpdate(EnumGateAction.CLOSE_GATE, 0, false);
		sendSignal(null, "stargate_close", new Object[] { reason.toString().toLowerCase() });
		AunisSoundHelper.playPositionedSound(world, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, false);
		
		if (isInitiating) {
			horizonFlashTask = null;
			isCurrentlyUnstable = false;
			updateFlashState(false);
		}
		
		targetGatePos = null;
		
		markDirty();
	}
	
	/**
	 * Called on the failed dialing.
	 */
	public void dialingFailed(StargateOpenResult reason) {
		sendSignal(null, "stargate_failed", new Object[] { reason.toString().toLowerCase() });
		horizonFlashTask = null;
		
		addFailedTaskAndPlaySound();		
		stargateState = EnumStargateState.FAILING;
		
		markDirty();
	}
	
	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 53));
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
	}
	
	
	// ------------------------------------------------------------------------
	// Sounds
	
	@Nullable
	protected abstract SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum);
	
	@Nullable
	protected abstract SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum);
		
	public void playPositionedSound(StargateSoundPositionedEnum soundEnum, boolean play) {
		SoundPositionedEnum positionedSound = getPositionedSound(soundEnum);
		
		if (positionedSound == null)
			throw new IllegalArgumentException("Tried to play " + soundEnum + " on " + getClass().getCanonicalName() + " which apparently doesn't support it.");
		
		if (world.isRemote)
			Aunis.proxy.playPositionedSoundClientSide(getGateCenterPos(), positionedSound, play);
		else
			AunisSoundHelper.playPositionedSound(world, getGateCenterPos(), positionedSound, play);
	}
	
	public void playSoundEvent(StargateSoundEventEnum soundEnum) {
		SoundEventEnum soundEvent = getSoundEvent(soundEnum);
		
		if (soundEvent == null)
			throw new IllegalArgumentException("Tried to play " + soundEnum + " on " + getClass().getCanonicalName() + " which apparently doesn't support it.");
		
		if (world.isRemote)
			AunisSoundHelper.playSoundEventClientSide(world, getGateCenterPos(), soundEvent);
		else
			AunisSoundHelper.playSoundEvent(world, getGateCenterPos(), soundEvent);
	}
	
	// ------------------------------------------------------------------------
	// Ticking and loading
	
	public abstract BlockPos getGateCenterPos();	
	
	protected TargetPoint targetPoint;
	protected EnumFacing facing = EnumFacing.NORTH;
	protected StargateNetwork network;
		
	public EnumFacing getFacing() {
		return facing;
	}
	
	@Override
	public void onLoad() {		
		if (!world.isRemote) {
			updateFacing(world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL), true);
			network = StargateNetwork.get(world);
			
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);			
			Random random = new Random(pos.hashCode() * 31 + world.provider.getDimension());
			
			for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
				
				StargatePos stargatePos;
				
				if (gateAddressMap.get(symbolType) == null) {
					StargateAddress address = new StargateAddress(symbolType);
					address.generate(random);
					
					stargatePos = new StargatePos(world.provider.getDimension(), pos, address);
					network.addStargate(address, stargatePos);
					gateAddressMap.put(symbolType, address);
//					Aunis.info(address.toString());
				}
				
				else {
					stargatePos = new StargatePos(world.provider.getDimension(), pos, gateAddressMap.get(symbolType));
				}
				
				gatePosMap.put(symbolType, stargatePos);
			}
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, StateTypeEnum.RENDERER_STATE));
		}
	}
	
	private boolean addedToNetwork;
	
	@Override
	public void update() {
		// Scheduled tasks
		ScheduledTask.iterate(scheduledTasks, world.getTotalWorldTime());		
		
		if (!world.isRemote) {
			
			// This cannot be done in onLoad because it makes
			// Stargates invisible to the network sometimes
			if (!addedToNetwork) {
				addedToNetwork = true;
				Aunis.ocWrapper.joinWirelessNetwork(this);
				Aunis.ocWrapper.joinOrCreateNetwork(this);
				// Aunis.info(pos + ": Stargate joined OC network");
			}
			
			if (stargateState.engaged() && targetGatePos == null) {
				Aunis.logger.error("A stargateState indicates the Gate should be open, but targetGatePos is null. This is a bug. Closing gate...");
				attemptClose(StargateClosedReasonEnum.CONNECTION_LOST);
			}
			
			// Event horizon teleportation			
			if (stargateState.initiating()) {
				eventHorizon.scheduleTeleportation(targetGatePos);
			}
			
			// Autoclose
			if (world.getTotalWorldTime() % 20 == 0 && stargateState == EnumStargateState.ENGAGED && AunisConfig.autoCloseConfig.autocloseEnabled && shouldAutoclose()) {
				targetGatePos.getTileEntity().attemptClose(StargateClosedReasonEnum.REQUESTED);
			}
			
			if (horizonFlashTask != null && horizonFlashTask.isActive()) {
				horizonFlashTask.update(world.getTotalWorldTime());
			}
			
			// Event horizon killing
			if (horizonKilling) {	
				List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
				List<BlockPos> blocks = new ArrayList<BlockPos>();
				
				// Get all blocks and entities inside the kawoosh
				for (int i=0; i<horizonSegments; i++) {
					AunisAxisAlignedBB gBox = localKillingBoxes.get(i).offset(pos);
					
					entities.addAll(world.getEntitiesWithinAABB(EntityLivingBase.class, gBox));
					
//					Aunis.info(new AxisAlignedBB((int)Math.floor(gBox.minX), (int)Math.floor(gBox.minY+1), (int)Math.floor(gBox.minZ), (int)Math.ceil(gBox.maxX-1), (int)Math.ceil(gBox.maxY-1), (int)Math.ceil(gBox.maxZ-1)).toString());
					for (BlockPos bPos : BlockPos.getAllInBox((int)Math.floor(gBox.minX), (int)Math.floor(gBox.minY), (int)Math.floor(gBox.minZ), (int)Math.ceil(gBox.maxX)-1, (int)Math.ceil(gBox.maxY)-1, (int)Math.ceil(gBox.maxZ)-1))
						blocks.add(bPos);					
				}
				
				// Get all entities inside the gate
				for (AunisAxisAlignedBB lBox : localInnerEntityBoxes)
					entities.addAll(world.getEntitiesWithinAABB(EntityLivingBase.class, lBox.offset(pos)));
				
				// Get all blocks inside the gate
				for (AunisAxisAlignedBB lBox : localInnerBlockBoxes) {
					AunisAxisAlignedBB gBox = lBox.offset(pos);
					
					for (BlockPos bPos : BlockPos.getAllInBox((int)gBox.minX, (int)gBox.minY, (int)gBox.minZ, (int)gBox.maxX-1, (int)gBox.maxY-1, (int)gBox.maxZ-1))
						blocks.add(bPos);
				}
				
				// Kill them
				for (EntityLivingBase entity : entities) {
					entity.attackEntityFrom(AunisDamageSources.DAMAGE_EVENT_HORIZON, 20);
					AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_VAPORIZE_BLOCK_PARTICLES, new StargateVaporizeBlockParticlesRequest(entity.getPosition())), targetPoint);
				}
				
				// Vaporize them
				for (BlockPos dPos : blocks) {
					if (!dPos.equals(getGateCenterPos())) {
						if (!world.isAirBlock(dPos)) {
							world.setBlockToAir(dPos);
							AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_VAPORIZE_BLOCK_PARTICLES, new StargateVaporizeBlockParticlesRequest(dPos)), targetPoint);
						}
					}
				}
			}
			
			
			/*
			 * Draw power (engaged)
			 * 
			 * If initiating
			 * 	True: Extract energy each tick
			 * 	False: Update the source gate about consumed energy each second
			 */
			if (stargateState.initiating()) {
				int energyStored = getEnergyStorage().getEnergyStored();
				energySecondsToClose = energyStored/(float)keepAliveEnergyPerTick / 20f;
				
				if (energySecondsToClose >= 1) {
					
					/*
					 * If energy can sustain connection for less than AunisConfig.powerConfig.instabilitySeconds seconds
					 * Start flickering
					 * 
					 * 2020-04-25: changed the below to check if the gate is being sufficiently externally powered and, if so,
					 * do not start flickering even if the internal power isn't enough.
					 */
					
					// Horizon becomes unstable
					if (horizonFlashTask == null && energySecondsToClose < AunisConfig.powerConfig.instabilitySeconds && energyTransferedLastTick < 0) {
						resetFlashingSequence();
						
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int) (Math.random() * 40) + 5));
					}
					
					// Horizon becomes stable
					if (horizonFlashTask != null && (energySecondsToClose > AunisConfig.powerConfig.instabilitySeconds || energyTransferedLastTick >= 0)) {
						horizonFlashTask = null;
						isCurrentlyUnstable = false;
						
						updateFlashState(false);
					}
					
					getEnergyStorage().extractEnergy(keepAliveEnergyPerTick, false);
					
					markDirty();
//					Aunis.info("Stargate energy: " + energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored() + "\t\tAlive for: " + (float)(energyStorage.getEnergyStored())/keepAliveCostPerTick/20);
				}
				
				else {
					attemptClose(StargateClosedReasonEnum.OUT_OF_POWER);
				}
			}
			
			energyTransferedLastTick = getEnergyStorage().getEnergyStored() - energyStoredLastTick;
			energyStoredLastTick = getEnergyStorage().getEnergyStored();
		}
	}
	
	/**
	 * Method for closing the gate using Autoclose mechanism.
	 * @return {@code True} if the gate should be closed, false otherwise.
	 */
	protected boolean shouldAutoclose() {
		return getAutoCloseManager().shouldClose(targetGatePos);
	}
	
	@Override
	public void onChunkUnload() {
		if (node != null)
			node.remove();
		
		Aunis.ocWrapper.leaveWirelessNetwork(this);
	}

	@Override
	public void invalidate() {
		if (node != null)
			node.remove();
		
		Aunis.ocWrapper.leaveWirelessNetwork(this);
		
		super.invalidate();
	}
	
	@Override
	public void rotate(Rotation rotation) {
		IBlockState state = world.getBlockState(pos);
		
		EnumFacing facing = state.getValue(AunisProps.FACING_HORIZONTAL);
		world.setBlockState(pos, state.withProperty(AunisProps.FACING_HORIZONTAL, rotation.rotate(facing)));
	}
	
	// ------------------------------------------------------------------------
	// Killing and block vaporizing
	
	/**
	 * Gets full {@link AxisAlignedBB} of the killing area.
	 * @param server Calling side.
	 * @return Approximate kawoosh size.
	 */
	protected abstract AunisAxisAlignedBB getHorizonKillingBox(boolean server);
	
	/**
	 * How many segments should the exclusion zone have.
	 * @param server Calling side.
	 * @return Count of subsegments of the killing box.
	 */
	protected abstract int getHorizonSegmentCount(boolean server);
	
	/**
	 * The event horizon in the gate also should kill
	 * and vaporize everything
	 * @param server Calling side.
	 * @return List of {@link AxisAlignedBB} for the inner gate area.
	 */
	protected abstract List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server);
	
	/**
	 * How many ticks should the {@link StargateAbstractBaseTile} wait to perform
	 * next update to the size of the killing box.
	 * @param server Calling side
	 */
	protected int getTicksPerHorizonSegment(boolean server) {
		return 12 / getHorizonSegmentCount(server);
	}
	
	/**
	 * Contains all the subboxes to be activated with the kawoosh.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localKillingBoxes;
	
	public List<AunisAxisAlignedBB> getLocalKillingBoxes() {
		return localKillingBoxes;
	}
	
	/**
	 * Contains all boxes of the inner part of the gate.
	 * Full blocks. Used for destroying blocks.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerBlockBoxes;
	
	public List<AunisAxisAlignedBB> getLocalInnerBlockBoxes() {
		return localInnerBlockBoxes;
	}
	
	/**
	 * Contains all boxes of the inner part of the gate.
	 * Not full blocks. Used for entity killing.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerEntityBoxes;
	
	public List<AunisAxisAlignedBB> getLocalInnerEntityBoxes() {
		return localInnerEntityBoxes;
	}
	
	private boolean horizonKilling = false;
	private int horizonSegments = 0;

	// ------------------------------------------------------------------------
	// Rendering
	
	private AxisAlignedBB renderBoundingBox = TileEntity.INFINITE_EXTENT_AABB;
	
	public AunisAxisAlignedBB getRenderBoundingBoxForDisplay() {
		return getRenderBoundingBoxRaw().rotate((int) facing.getHorizontalAngle()).offset(0.5, 0, 0.5);
	}
	
	protected StargateAbstractRendererStateBuilder getRendererStateServer() {
		return StargateAbstractRendererState.builder()
				.setStargateState(stargateState);
	}
	
	StargateAbstractRendererState rendererStateClient;
	protected abstract StargateAbstractRendererState createRendererStateClient();

	public StargateAbstractRendererState getRendererStateClient() {
		return rendererStateClient;
	}
	
	protected void setRendererStateClient(StargateAbstractRendererState rendererState) {
		this.rendererStateClient = rendererState;
		
		AunisSoundHelper.playPositionedSound(world, getGateCenterPos(), SoundPositionedEnum.WORMHOLE_LOOP, rendererState.doEventHorizonRender);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_LIGHTING_UPDATE_CLIENT, 10));
	}
	
	protected abstract AunisAxisAlignedBB getRenderBoundingBoxRaw();
	
	public void updateFacing(EnumFacing facing, boolean server) {
		this.facing = facing;
		this.eventHorizon = new EventHorizon(world, pos, getGateCenterPos(), facing, getHorizonTeleportBox(server));
		this.renderBoundingBox = getRenderBoundingBoxRaw().rotate((int) facing.getHorizontalAngle()).offset(0.5, 0, 0.5).offset(pos);
		
		AunisAxisAlignedBB kBox = getHorizonKillingBox(server);
		double width = kBox.maxZ - kBox.minZ;
		width /= getHorizonSegmentCount(server);
		
		localKillingBoxes = new ArrayList<AunisAxisAlignedBB>(getHorizonSegmentCount(server));
		for (int i=0; i<getHorizonSegmentCount(server); i++) {
			AunisAxisAlignedBB box = new AunisAxisAlignedBB(kBox.minX, kBox.minY, kBox.minZ + width*i, kBox.maxX, kBox.maxY, kBox.minZ + width*(i+1));
			box = box.rotate(facing).offset(0.5, 0, 0.5);
			
			localKillingBoxes.add(box);
		}
				
		localInnerBlockBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		localInnerEntityBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		for (AunisAxisAlignedBB lBox : getGateVaporizingBoxes(server)) {
			localInnerBlockBoxes.add(lBox.rotate(facing).offset(0.5, 0, 0.5));
			localInnerEntityBoxes.add(lBox.grow(0, 0, -0.25).rotate(facing).offset(0.5, 0, 0.5));
		}
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return renderBoundingBox;
	}
		
	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536;
	}	
	
	
	// ------------------------------------------------------------------------
	// Merging
	
	private boolean isMerged;
	
	public final boolean isMerged() {
		return isMerged;
	}
	
	/**
	 * @return Appropriate merge helper
	 */
	public abstract StargateAbstractMergeHelper getMergeHelper();
	
	/**
	 * Checks gate's merge state
	 * 
	 * @param shouldBeMerged - True if gate's multiblock structure is valid
	 * @param facing Facing of the base block.
	 */
	public final void updateMergeState(boolean shouldBeMerged, EnumFacing facing) {
		if (!shouldBeMerged) {
			if (isMerged)
				onGateBroken();
			
			if (stargateState.engaged()) {
				targetGatePos.getTileEntity().closeGate(StargateClosedReasonEnum.CONNECTION_LOST);
			}
		}
		
		else {
			onGateMerged();
		}
		
		this.isMerged = shouldBeMerged;
		IBlockState actualState = world.getBlockState(pos);
		
		// When the block is destroyed, there will be air in this place and we cannot set it's block state
		if (getMergeHelper().matchBase(actualState)) {
			world.setBlockState(pos, actualState.withProperty(AunisProps.RENDER_BLOCK, !shouldBeMerged), 2);
		}
		
		getMergeHelper().updateMembersMergeStatus(world, pos, facing, shouldBeMerged);
		
		markDirty();
	}
	
	
	// ------------------------------------------------------------------------
	// AutoClose
	
	public final void entityPassing(Entity entity, boolean inbound) {
		boolean isPlayer = entity instanceof EntityPlayerMP;
		
		if (isPlayer) {
			getAutoCloseManager().playerPassing();
			markDirty();
		}
		
		sendSignal(null, "stargate_traveler", new Object[] {inbound, isPlayer, entity.getClass().getSimpleName()});
	}

	
	// -----------------------------------------------------------------
	// Horizon flashing
	private ScheduledTask horizonFlashTask;
	
	private void setHorizonFlashTask(ScheduledTask horizonFlashTask) {
		horizonFlashTask.setExecutor(this);
		horizonFlashTask.setTaskCreated(world.getTotalWorldTime());
		
		this.horizonFlashTask = horizonFlashTask;
		markDirty();
	}

	private int flashIndex = 0;
	private boolean isCurrentlyUnstable = false;
	
	private void resetFlashingSequence() {
		flashIndex = 0;
		isCurrentlyUnstable = false;
	}
	
	private void updateFlashState(boolean flash) {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), targetPoint);
		
		if (targetGatePos != null) {
			BlockPos tPos = targetGatePos.gatePos;
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(tPos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), new TargetPoint(targetGatePos.dimensionID, tPos.getX(), tPos.getY(), tPos.getZ(), 512));
		}
	}
	
	
	// ------------------------------------------------------------------------
	// States

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererStateServer().build();
				
			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return createRendererStateClient();
		
			case RENDERER_UPDATE:
				return new StargateRendererActionState();
				
			case STARGATE_VAPORIZE_BLOCK_PARTICLES:
				return new StargateVaporizeBlockParticlesRequest();
				
			case FLASH_STATE:
				return new StargateFlashState();
				
			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case RENDERER_STATE:
				EnumFacing facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);

				setRendererStateClient(((StargateAbstractRendererState) state).initClient(pos, facing));
				updateFacing(facing, false);
				
				break;
				
			case RENDERER_UPDATE:
				switch (((StargateRendererActionState) state).action) {
					case OPEN_GATE:
						getRendererStateClient().horizonSegments = 0;
						getRendererStateClient().openGate(world.getTotalWorldTime());
						break;
						
					case CLOSE_GATE:
						getRendererStateClient().closeGate(world.getTotalWorldTime());						
						break;
						
					case STARGATE_HORIZON_WIDEN:
						getRendererStateClient().horizonSegments++;
						break;
						
					case STARGATE_HORIZON_SHRINK:
						getRendererStateClient().horizonSegments--;
						break;
						
					default:
						break;					
				}
				
				break;
		
			case STARGATE_VAPORIZE_BLOCK_PARTICLES:
				BlockPos b = ((StargateVaporizeBlockParticlesRequest) state).block;
				
				for (int i=0; i<20; i++) {
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleWhiteSmoke(world, b.getX() + (Math.random()-0.5), b.getY(), b.getZ() + (Math.random()-0.5), 0, 0, false));
				}
				
				break;
				
			case FLASH_STATE:
				if (getRendererStateClient() != null)
					getRendererStateClient().horizonUnstable = ((StargateFlashState) state).flash;
				
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
			case STARGATE_OPEN_SOUND:
				playSoundEvent(StargateSoundEventEnum.OPEN);
				break;
				
			case STARGATE_HORIZON_LIGHT_BLOCK:
				world.setBlockState(getGateCenterPos(), AunisBlocks.INVISIBLE_BLOCK.getDefaultState().withProperty(AunisProps.HAS_COLLISIONS, false));
				
				break;
				
			case STARGATE_HORIZON_WIDEN:
				if (!horizonKilling)
					horizonKilling = true;
				
				horizonSegments++;
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_WIDEN_ACTION), targetPoint);
				
				if (horizonSegments < getHorizonSegmentCount(true))
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, getTicksPerHorizonSegment(true)));
				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, getTicksPerHorizonSegment(true) + 12));
				
				break;
				
			case STARGATE_HORIZON_SHRINK:
				horizonSegments--;
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_SHRINK_ACTION), targetPoint);
				
				if (horizonSegments > 0)
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, getTicksPerHorizonSegment(true) + 1));
				else
					horizonKilling = false;
				
				markDirty();
					
				break;
				
			case STARGATE_CLOSE:
				world.setBlockToAir(getGateCenterPos());
				disconnectGate();
				break;
				
			case STARGATE_FAIL:
				failGate();
				break;
				
			case STARGATE_ENGAGE:
				engageGate();
				
				break;
				
			case STARGATE_LIGHTING_UPDATE_CLIENT:
				world.notifyLightSet(getGateCenterPos());
				world.checkLightFor(EnumSkyBlock.BLOCK, getGateCenterPos());
								
				break;
				
			case HORIZON_FLASH:				
				isCurrentlyUnstable ^= true;
				
				if (isCurrentlyUnstable) {
					flashIndex++;
					
					if (flashIndex == 1 && targetGatePos != null) {
						AunisSoundHelper.playSoundEvent(world, getGateCenterPos(), SoundEventEnum.WORMHOLE_FLICKER);
						AunisSoundHelper.playSoundEvent(targetGatePos.getWorld(), targetGatePos.getTileEntity().getGateCenterPos(), SoundEventEnum.WORMHOLE_FLICKER);
					}
					
					// Schedule change into stable state
					setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 3) + 3));
				}
				
				else {
					if (flashIndex == 1)
						// Schedule second flash
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 4) + 1));
					
					else {
						// Schedule next flash sequence
						float mul = energySecondsToClose / (float)AunisConfig.powerConfig.instabilitySeconds;
						int min = (int) (15 * mul);
						int off = (int) (20 * mul);
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, min + (int)(Math.random() * off)));
						
						resetFlashingSequence();
					}
				}
				
				updateFlashState(isCurrentlyUnstable);
				
				markDirty();				
				break;
		
			default:
				break;
		}
	}
	
	
	// -----------------------------------------------------------------
	// Power system
	
	private int keepAliveEnergyPerTick = 0;
	private int energyStoredLastTick = 0;
	protected int energyTransferedLastTick = 0;
	protected float energySecondsToClose = 0;
	
	public int getEnergyTransferedLastTick() {
		return energyTransferedLastTick;
	}
	
	public float getEnergySecondsToClose() {
		return energySecondsToClose;
	}
	
	protected abstract StargateAbstractEnergyStorage getEnergyStorage();
	
	protected StargateEnergyRequired getEnergyRequiredToDial(StargatePos targetGatePos) {
		BlockPos sPos = pos;
		BlockPos tPos = targetGatePos.gatePos;
		
		DimensionType sourceDim = world.provider.getDimensionType();
		DimensionType targetDim = targetGatePos.getWorld().provider.getDimensionType();
		
		if (sourceDim == DimensionType.OVERWORLD && targetDim == DimensionType.NETHER)
			tPos = new BlockPos(tPos.getX()*8, tPos.getY(), tPos.getZ()*8);
		else if (sourceDim == DimensionType.NETHER && targetDim == DimensionType.OVERWORLD)
			sPos = new BlockPos(sPos.getX()*8, sPos.getY(), sPos.getZ()*8);
		
		double distance = (int) sPos.getDistance(tPos.getX(), tPos.getY(), tPos.getZ());		
		
		if (distance < 5000)
			distance *= 0.8;
		else
			distance = 5000 * Math.log10(distance) / Math.log10(5000);	
		
		StargateEnergyRequired energyRequired = new StargateEnergyRequired(AunisConfig.powerConfig.openingBlockToEnergyRatio, AunisConfig.powerConfig.keepAliveBlockToEnergyRatioPerTick);
		energyRequired = energyRequired.mul(distance).add(StargateDimensionConfig.getCost(world.provider.getDimensionType(), targetDim));
		
		Aunis.info(String.format("Energy required to dial [distance=%,d, from=%s, to=%s] = %,d / keepAlive: %,d/t, stored=%,d", 
				Math.round(distance),
				sourceDim,
				targetDim,
				energyRequired.energyToOpen,
				energyRequired.keepAlive,
				getEnergyStorage().getEnergyStored()));
		
		return energyRequired;
	}
	
	/**
	 * Checks is gate has sufficient power to dial across specified distance and dimension
	 * It also sets energy draw for (possibly) outgoing wormhole
	 * 
	 * @param distance - distance in blocks to target gate
	 * @param targetWorld - target world, used for multiplier
	 */
	public boolean hasEnergyToDial(StargatePos targetGatePos) {
		StargateEnergyRequired energyRequired = getEnergyRequiredToDial(targetGatePos);
		
		if (getEnergyStorage().getEnergyStored() >= energyRequired.energyToOpen) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY) || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(getEnergyStorage());
		}
		
		return super.getCapability(capability, facing);
	}
	
	
	// ------------------------------------------------------------------------
	// NBT
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {				
		for (StargateAddress stargateAddress : gateAddressMap.values()) {
			compound.setTag("address_" + stargateAddress.getSymbolType(), stargateAddress.serializeNBT());
		}
		
		compound.setTag("dialedAddress", dialedAddress.serializeNBT());
		
		if (targetGatePos != null)
			compound.setTag("targetGatePos", targetGatePos.serializeNBT());
			
		compound.setBoolean("isMerged", isMerged);
		compound.setTag("autoCloseManager", getAutoCloseManager().serializeNBT());
		
		compound.setInteger("keepAliveCostPerTick", keepAliveEnergyPerTick);
		
		if (stargateState != null)
			compound.setInteger("stargateState", stargateState.id);
		
		compound.setTag("scheduledTasks", ScheduledTask.serializeList(scheduledTasks));
		
		compound.setTag("energyStorage", getEnergyStorage().serializeNBT());
		
		if (node != null) {
			NBTTagCompound nodeCompound = new NBTTagCompound();
			node.save(nodeCompound);
			
			compound.setTag("node", nodeCompound);
		}
		
		compound.setBoolean("horizonKilling", horizonKilling);
		compound.setInteger("horizonSegments", horizonSegments);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			if (compound.hasKey("address_" + symbolType))
				gateAddressMap.put(symbolType, new StargateAddress(compound.getCompoundTag("address_" + symbolType)));
		}
		
		dialedAddress.deserializeNBT(compound.getCompoundTag("dialedAddress"));
		
		if (compound.hasKey("targetGatePos"))
			targetGatePos = new StargatePos(getSymbolType(), compound.getCompoundTag("targetGatePos"));
		
		isMerged = compound.getBoolean("isMerged");
		getAutoCloseManager().deserializeNBT(compound.getCompoundTag("autoCloseManager"));
		
		try {
			ScheduledTask.deserializeList(compound.getCompoundTag("scheduledTasks"), scheduledTasks, this);
		}
		
		catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.info("Exception at reading NBT");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}
		
		getEnergyStorage().deserializeNBT(compound.getCompoundTag("energyStorage"));
		this.keepAliveEnergyPerTick = compound.getInteger("keepAliveCostPerTick");
		
		stargateState = EnumStargateState.valueOf(compound.getInteger("stargateState"));
		if (stargateState == null)
			stargateState = EnumStargateState.IDLE;
				
		if (node != null && compound.hasKey("node"))
			node.load(compound.getCompoundTag("node"));
		
		horizonKilling = compound.getBoolean("horizonKilling");
		horizonSegments = compound.getInteger("horizonSegments");
		
		super.readFromNBT(compound);
	}
	
	@Override
	public void prepare() {
		gateAddressMap = null;
	}
	
	// ------------------------------------------------------------------------
	// OpenComputers
	
	/**
	 * Tries to find a {@link SymbolInterface} instance from
	 * Integer index or String name of the symbol.
	 * @param nameIndex Name or index.
	 * @return Symbol.
	 * @throws IllegalArgumentException When symbol/index is invalid.
	 */
	public SymbolInterface getSymbolFromNameIndex(Object nameIndex) throws IllegalArgumentException {
		SymbolInterface symbol = null;
		
		if (nameIndex instanceof Integer)
			symbol = getSymbolType().valueOfSymbol((Integer) nameIndex);
		
		else if (nameIndex instanceof byte[])
			symbol = getSymbolType().fromEnglishName(new String((byte[]) nameIndex));
		
		else if (nameIndex instanceof String)
			symbol = getSymbolType().fromEnglishName((String) nameIndex);

		if (symbol == null)
			throw new IllegalArgumentException("bad argument (symbol name/index invalid)");
		
		return symbol;
	}
	
	// ------------------------------------------------------------
	// Wireless Network
	@Override
	public int x() {
		return pos.getX();
	}
	
	@Override
	public int y() {
		return pos.getY();
	}
	
	@Override
	public int z() {
		return pos.getZ();
	}
	
	@Override
	public World world() {
		return world;
	}
	
	@Override
	@Optional.Method(modid = "opencomputers")
	public void receivePacket(Packet packet, WirelessEndpoint sender) {
//		Aunis.info("received packet: ttl="+packet.ttl());
		
		if (stargateState.engaged() && packet.ttl() > 0) {
			Network.sendWirelessPacket(targetGatePos.getTileEntity(), 20, packet.hop());
		}
	}
	
	
	// ------------------------------------------------------------
	// Node-related work
	private Node node = Aunis.ocWrapper.createNode(this, "stargate");
	
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
	// function(arg:type[, optionArg:type]):resultType; Description.
	@Optional.Method(modid = "opencomputers")
	@Callback(getter = true)
	public Object[] stargateAddress(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null };
		
		Map<SymbolTypeEnum, List<String>> map = new HashMap<>(3);
		
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values()) {
			map.put(symbolType, gateAddressMap.get(symbolType).getNameList());
		}
		
		return new Object[] { map };
	}

	@Optional.Method(modid = "opencomputers")
	@Callback(getter = true)
	public Object[] dialedAddress(Context context, Arguments args) {
		return new Object[] {(isMerged && stargateState != EnumStargateState.ENGAGED) ? dialedAddress : null};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getEnergyStored(Context context, Arguments args) {
		
		return new Object[] {isMerged ? getEnergyStorage().getEnergyStored() : null};
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getMaxEnergyStored(Context context, Arguments args) {
		return new Object[] {isMerged ? getEnergyStorage().getMaxEnergyStored() : null};
	}
}
