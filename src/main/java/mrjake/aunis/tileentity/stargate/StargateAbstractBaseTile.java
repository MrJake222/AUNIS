package mrjake.aunis.tileentity.stargate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisDamageSources;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
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
import mrjake.aunis.stargate.DimensionPowerMap;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateAbstractEnergyStorage;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateEnergyRequired;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
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
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers")
public abstract class StargateAbstractBaseTile extends TileEntity implements StateProviderInterface, ITickable, ICapabilityProvider, ScheduledTaskExecutorInterface, Environment, PreparableInterface {
	
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
		
		AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.WORMHOLE_LOOP, true);
		
		markDirty();
	}
	
	protected void disconnectGate() {	
		stargateState = EnumStargateState.IDLE;
		getAutoCloseManager().reset();

		if (!(this instanceof StargateOrlinBaseTile))
			dialedAddress.clear();
		
		ForgeChunkManager.unforceChunk(chunkLoadingTicket, new ChunkPos(pos));
		
		markDirty();
	}
	
	protected void failGate() {
		stargateState = EnumStargateState.IDLE;

		if (!(this instanceof StargateOrlinBaseTile))
			dialedAddress.clear();
				
		markDirty();
	}
	
	public void onBlockBroken() {
//		updateTargetGate();
		world.setBlockToAir(getGateCenterPos());
		
		if (stargateState.initiating()) {
			attemptClose();
		}
		
		else if (stargateState.engaged()) {
			targetGatePos.getTileEntity().attemptClose();
		}
		
		updateMergeState(false, facing);
		for (StargateAddress address : gateAddressMap.values())
			network.removeStargate(address);
		
		ForgeChunkManager.unforceChunk(chunkLoadingTicket, new ChunkPos(pos));
		AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.WORMHOLE_LOOP, false);
	}
	
	public boolean canAcceptConnectionFrom(StargatePos targetGatePos) {
		return stargateState.idle();
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
		StargateOpenResult result = checkDialedAddress();
		
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
			targetTile.dialedAddress.addAll(gateAddressMap.get(targetGatePos.symbolType));
			targetTile.dialedAddress.addOrigin();
		}
			
		return result;
	}
	
	/**
	 * Wrapper for {@link this#checkDialedGate(StargatePos)}. Adds
	 * address validation.
	 * @return {@code True} if the {@link this#dialedAddress} is valid and the dialed gate can be reached, {@code false} otherwise.
	 */
	protected StargateOpenResult checkDialedAddress() {
		if (!dialedAddress.validate())
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		StargatePos targetGatePos = network.getStargate(dialedAddress);
		
		if (!canDial(targetGatePos))
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		StargateAbstractBaseTile targetTile = targetGatePos.getTileEntity();
			
		if (!targetTile.canAcceptConnectionFrom(gatePosMap.get(getSymbolType())))
			return StargateOpenResult.ADDRESS_MALFORMED;
		
		if (!hasEnergyToDial(targetGatePos))
			return StargateOpenResult.NOT_ENOUGH_POWER;
		
		return StargateOpenResult.OK;
	}
	
	/**
	 * Checks if {@link StargateAbstractBaseTile#dialedAddress} points to
	 * a valid target gate (and not to itself).
	 * @return {@code True} if the gate can be reached, {@code false} otherwise
	 */
	protected boolean canDial(StargatePos targetGatePos) {		
		if (targetGatePos == null)
			return false;
		
		if (targetGatePos.equals(gatePosMap.get(getSymbolType())))
			return false;
		
		boolean areDimensionsEqual = world.provider.getDimension() == targetGatePos.dimensionID;
		
		if ((world.provider.getDimensionType() != DimensionType.NETHER || targetGatePos.dimensionID != 0) && (world.provider.getDimensionType() != DimensionType.OVERWORLD || targetGatePos.dimensionID != -1))
			if (dialedAddress.size() < getSymbolType().getMinimalSymbolCountTo(targetGatePos.symbolType, areDimensionsEqual))
				return false;
		
		int additional = dialedAddress.size() - 7;
		
		if (additional > 0) {
			Aunis.info("dialed subList: " + dialedAddress.getAdditional().subList(0, additional));
			Aunis.info("target subList: " + targetGatePos.additionalSymbols.subList(0, additional));
			
			if (!dialedAddress.getAdditional().subList(0, additional).equals(targetGatePos.additionalSymbols.subList(0, additional)))
				return false;
		}
		
		return true;
	}
	
	public void attemptClose() {
		targetGatePos.getTileEntity().closeGate();
		closeGate();
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
		
		if (symbol.origin())
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
	 * @param symbol Symbol to be added
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
	 */
	protected void addSymbolToAddress(SymbolInterface symbol) {
		if (!canAddSymbol(symbol))
			throw new IllegalStateException("Cannot add that symbol");
		
		dialedAddress.addSymbol(symbol);
		
		if (stargateWillLock(symbol) && dialedAddress.validate()) {
			StargatePos targetGatePos = network.getStargate(dialedAddress);
			
			if (canDial(targetGatePos) && hasEnergyToDial(targetGatePos)) {
				targetGatePos.getTileEntity().incomingWormhole(dialedAddress.size());
			}
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
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_OPEN_SOUND));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_LIGHT_BLOCK, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 19 + getTicksPerHorizonSegment(true)));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 23 + getTicksPerHorizonSegment(true))); // 1.3s of the sound to the kill
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ENGAGE));
		
		if (isInitiating) {
			StargateEnergyRequired energyRequired = getEnergyRequiredToDial(targetGatePos);
			getEnergyStorage().extractEnergy(energyRequired.energyToOpen, false);
			keepAliveEnergyPerTick = energyRequired.keepAlive;
		}
		
		ForgeChunkManager.forceChunk(chunkLoadingTicket, new ChunkPos(pos));
		
		sendSignal(null, "stargate_open", new Object[] { isInitiating });
		
		markDirty();
	}
	
	/**
	 * Called either on pressing BRB on open gate or close command from a computer.
	 */
	public void closeGate() {
//		Aunis.info("closeGate init=" + isInitiating + ", targetGatePos: " + targetGatePos);
		
		stargateState = EnumStargateState.UNSTABLE;
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CLOSE, 62));
		sendSignal(null, "stargate_close", new Object[] {});
		
		playSoundEvent(StargateSoundEventEnum.CLOSE);
		sendRenderingUpdate(EnumGateAction.CLOSE_GATE, 0, false);
		AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.WORMHOLE_LOOP, false);
		
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
	public void dialingFailed() {
		sendSignal(null, "stargate_failed", new Object[] {});
		horizonFlashTask = null;
		
		addDialingFailedTask();
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
		
		stargateState = EnumStargateState.FAILING;
		
		markDirty();
	}
	
	protected void addDialingFailedTask() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 53));
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
			AunisSoundHelper.playSoundEventClientSide(world, pos, soundEvent);
		else
			AunisSoundHelper.playSoundEvent(world, pos, soundEvent);
	}
	
	// ------------------------------------------------------------------------
	// Ticking and loading
	
	public abstract BlockPos getGateCenterPos();	
	
	protected TargetPoint targetPoint;
	protected EnumFacing facing = EnumFacing.NORTH;
	protected StargateNetwork network;
	
	protected Ticket chunkLoadingTicket;
	protected LoadingCallback chunkLoadingCallback = new LoadingCallback() {
		public void ticketsLoaded(List<Ticket> tickets, World world) {}
	};
	
	public EnumFacing getFacing() {
		return facing;
	}
	
	@Override
	public void onLoad() {		
		if (!world.isRemote) {
			updateFacing(world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL), true);
			network = StargateNetwork.get(world);
			
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			Aunis.ocWrapper.joinOrCreateNetwork(this);
			
			ForgeChunkManager.setForcedChunkLoadingCallback(Aunis.instance, chunkLoadingCallback);
			chunkLoadingTicket = ForgeChunkManager.requestTicket(Aunis.instance, world, Type.NORMAL);
			
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
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_STATE));
		}
	}
	
	@Override
	public void update() {
		// Scheduled tasks
		ScheduledTask.iterate(scheduledTasks, world.getTotalWorldTime());		
		
		if (!world.isRemote) {
			// Event horizon teleportation			
			if (stargateState.initiating()) {
				eventHorizon.scheduleTeleportation(targetGatePos);
			}
//						
//			// Not initiating
			if (stargateState == EnumStargateState.ENGAGED && AunisConfig.autoCloseConfig.autocloseEnabled) {
				if (getAutoCloseManager().shouldClose(targetGatePos)) {
					targetGatePos.getTileEntity().attemptClose();
				}
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
//					int threshold = keepAliveEnergyPerTick*20 * AunisConfig.powerConfig.instabilitySeconds;

					/*
					 * If energy can sustain connection for less than AunisConfig.powerConfig.instabilitySeconds seconds
					 * Start flickering
					 */
					
					// Horizon becomes unstable
					if (horizonFlashTask == null && energySecondsToClose < AunisConfig.powerConfig.instabilitySeconds) {
						resetFlashingSequence();
						
						setHorizonFlashTask(new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int) (Math.random() * 40) + 5));
					}
					
					// Horizon becomes stable
					if (horizonFlashTask != null && energySecondsToClose > AunisConfig.powerConfig.instabilitySeconds) {
						horizonFlashTask = null;
						isCurrentlyUnstable = false;
						
						updateFlashState(false);
					}
					
					getEnergyStorage().extractEnergy(keepAliveEnergyPerTick, false);
					
					markDirty();
//					Aunis.info("Stargate energy: " + energyStorage.getEnergyStored() + " / " + energyStorage.getMaxEnergyStored() + "\t\tAlive for: " + (float)(energyStorage.getEnergyStored())/keepAliveCostPerTick/20);
				}
				
				else {
					attemptClose();
				}
			}
			
//			energyTransferedLastTick = energyStorage.getEnergyStored() - energyStoredLastTick;
//			energyStoredLastTick = energyStorage.getEnergyStored();
//			
//			if (stargateState.initiating())
//				energySecondsToClose = (float)(energyStorage.getEnergyStored()) / keepAliveCostPerTick / 20;
//			else
//				energySecondsToClose = 0;
		}
	}
	
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
		
		AunisSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.WORMHOLE_LOOP, rendererState.doEventHorizonRender);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_LIGHTING_UPDATE_CLIENT, 10));
	}
	
	public void updateFacing(EnumFacing facing, boolean server) {
		this.facing = facing;
		this.eventHorizon = new EventHorizon(world, pos, facing, getHorizonTeleportBox(server));
		
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
		return new AxisAlignedBB(getPos().add(-7, 0, -7), getPos().add(7, 12, 7));
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
	 * Function called when {@link this#updateMergeState(boolean, IBlockState)} is
	 * called with {@code true} parameter(multiblock valid).
	 */
	protected abstract void mergeGate();
	
	/**
	 * Function called when {@link this#updateMergeState(boolean, IBlockState)} is
	 * called with {@code false} parameter(multiblock not valid).
	 */
	protected abstract void unmergeGate();
	
	/**
	 * @return Appropriate merge helper
	 */
	protected abstract StargateAbstractMergeHelper getMergeHelper();
	
	/**
	 * Checks gate's merge state
	 * 
	 * @param shouldBeMerged - True if gate's multiblock structure is valid
	 * @param facing Facing of the base block.
	 */
	public final void updateMergeState(boolean shouldBeMerged, EnumFacing facing) {		
		this.isMerged = shouldBeMerged;
		
		if (!shouldBeMerged) {
			unmergeGate();
			
			if (stargateState.engaged()) {
				targetGatePos.getTileEntity().closeGate();
			}
		}
		
		else {
			mergeGate();
		}
		
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
		BlockPos tPos = targetGatePos.gatePos;
				
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), targetPoint);
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(tPos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), new TargetPoint(targetGatePos.dimensionID, tPos.getX(), tPos.getY(), tPos.getZ(), 512));
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
					
					if (flashIndex == 1) {
						AunisSoundHelper.playSoundEvent(world, pos, SoundEventEnum.WORMHOLE_FLICKER);
						AunisSoundHelper.playSoundEvent(targetGatePos.getWorld(), targetGatePos.gatePos, SoundEventEnum.WORMHOLE_FLICKER);
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
				throw new UnsupportedOperationException("EnumScheduledTask."+scheduledTask.name()+" not implemented on "+this.getClass().getName());
		}
	}
	
	
	// -----------------------------------------------------------------
	// Power system
	
//	private int openCost = 0;
	private int keepAliveEnergyPerTick = 0;
//	private int energyStoredLastTick = 0;
//	protected int energyTransferedLastTick = 0;
	protected float energySecondsToClose = 0;
	
	protected abstract StargateAbstractEnergyStorage getEnergyStorage();
	
//	protected int getMaxEnergyStorage() {
//		return AunisConfig.powerConfig.stargateEnergyStorage;
//	}
//	
//	protected boolean canReceiveEnergy() {
//		return true;
//	}
	
//	protected EnergyStorageUncapped energyStorage = new EnergyStorageUncapped(getMaxEnergyStorage(), AunisConfig.powerConfig.stargateMaxEnergyTransfer) {
//		
//		@Override
//		protected void onEnergyChanged() {			
//			markDirty();
//		};
//		
//		@Override
//		public boolean canReceive() {
//			return canReceiveEnergy();
//		};
//	};
	
	/**
	 * Unifies energy consumption methods
	 * The order is as follows:
	 *   - Gate internal buffer
	 *   - DHD Crystal
	 *   
	 * @param minEnergy - minimal energy 
	 * @return First IEnergyStorage that has enough energy, CAN BE NULL if no source can provide such energy
	 */
//	@Nullable
//	protected IEnergyStorage getEnergyStorage(int minEnergy) {
//		if (energyStorage.getEnergyStored() >= minEnergy)
//			return energyStorage;
//		
//		return null;
//	}
	
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
		energyRequired = energyRequired.mul(distance).add(DimensionPowerMap.getCost(world.provider.getDimensionType(), targetDim));
		
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
//		compound.setInteger("dialedAddressLength", dialedAddress.size());
//		
//		for (int i=0; i<dialedAddress.size(); i++) {
//			compound.setInteger("dialedSymbol"+i, dialedAddress.get(i).id);
//		}
			
		compound.setBoolean("isMerged", isMerged);
		compound.setTag("autoCloseManager", getAutoCloseManager().serializeNBT());
		
//		compound.setInteger("openCost", openCost);
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
		
//		if (compound.hasKey("symbol0")) {					
//			for (int i=0; i<7; i++) {
//				int id = compound.getInteger("symbol"+i);
//				gateAddress.add( EnumSymbol.valueOf(id) );
//			}
//		}
		
		dialedAddress.deserializeNBT(compound.getCompoundTag("dialedAddress"));
		
		if (compound.hasKey("targetGatePos"))
			targetGatePos = new StargatePos(getSymbolType(), compound.getCompoundTag("targetGatePos"));
		
//		dialedAddress.clear();
//		int dialedAddressLength = compound.getInteger("dialedAddressLength");
//		
//		for (int i=0; i<dialedAddressLength; i++) {
//			dialedAddress.add( EnumSymbol.valueOf(compound.getInteger("dialedSymbol"+i)) );
//		}
//		
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
		return new Object[] {isMerged ? gateAddressMap : null};
	}

	@Optional.Method(modid = "opencomputers")
	@Callback(getter = true)
	public Object[] dialedAddress(Context context, Arguments args) {
		return new Object[] {(isMerged && stargateState != EnumStargateState.ENGAGED) ? dialedAddress : null};
	}
}
