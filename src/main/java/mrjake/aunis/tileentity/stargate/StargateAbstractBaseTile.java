package mrjake.aunis.tileentity.stargate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisDamageSources;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.capability.EnergyStorageUncapped;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.particle.ParticleWhiteSmoke;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.AutoCloseManager;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateEnergyRequired;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.state.StargateFlashState;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.StargateRendererStateBase;
import mrjake.aunis.state.StargateVaporizeBlockParticlesRequest;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateProviderInterface;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tesr.SpecialRendererProviderInterface;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.tileentity.util.ScheduledTaskExecutorInterface;
import mrjake.aunis.upgrade.ITileEntityUpgradeable;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "opencomputers")
public abstract class StargateAbstractBaseTile extends TileEntity implements SpecialRendererProviderInterface, StateProviderInterface, ITickable, ICapabilityProvider, ScheduledTaskExecutorInterface, Environment {
	
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
		eventHorizon.reset();
		
		markDirty();
	}
	
	private void disconnectGate() {	
		stargateState = EnumStargateState.IDLE;
		getAutoCloseManager().reset();
				
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
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(gateAction, computer, chevronCount)), targetPoint);
		}
	}
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	/**
	 * Instance of the {@link EventHorizon} for teleporting entities.
	 */
	protected EventHorizon eventHorizon;
	
	/**
	 * Get the bounding box of the horizon.
	 * @return Horizon bounding box.
	 */
	protected abstract AunisAxisAlignedBB getHorizonTeleportBox();

	private AutoCloseManager autoCloseManager;
	
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
		eventHorizon.setMotion(entityId, motionVector);
	}
	
	public void teleportEntity(int entityId) {
		eventHorizon.teleportEntity(entityId);
	}
	
	public void removeEntity(int entityId) {
		eventHorizon.removeEntity(entityId);
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
//			Aunis.info("add stargate " + gateAddress);
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
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_OPEN_SOUND));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_LIGHT_BLOCK, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 19 + TICKS_PER_HORIZON_SEGMENT));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, EnumScheduledTask.STARGATE_OPEN_SOUND.waitTicks + 23 + TICKS_PER_HORIZON_SEGMENT)); // 1.3s of the sound to the kill
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ENGAGE));
		
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
			
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CLOSE));
			sendSignal(null, "stargate_close", new Object[] {});
			
			sendRenderingUpdate(EnumGateAction.CLOSE_GATE, !stopRing, 0);
		}
		
		else {
			dialingFailed(stopRing);
			
			stargateState = EnumStargateState.FAILING;
			
			AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_DIAL_FAILED, 0.3f);
			
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL));
			sendSignal(null, "stargate_failed", new Object[] {});
			
			sendRenderingUpdate(EnumGateAction.GATE_DIAL_FAILED, !stopRing, 0);
		}
			
		if (!(this instanceof StargateOrlinBaseTile))
			dialedAddress.clear();
		
		horizonFlashTask = null;
		
		getRendererState().setStargateClosed(world, pos);
		clearLinkedDHDButtons(dialingFailed);

		markDirty();
	}	
	
	// ------------------------------------------------------------------------
	// Ticking and loading

	protected abstract BlockPos getLightBlockPos();
	
	protected TargetPoint targetPoint;
	protected EnumFacing facing = EnumFacing.NORTH;
	
	@Override
	public void onLoad() {
		updateFacing(world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL));
		
		if (!world.isRemote) {
			targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
			
			generateAddress();
			Aunis.ocWrapper.joinOrCreateNetwork(this);
		}
		
		else {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.RENDERER_STATE));
		}
	}
	
	@Override
	public void update() {
		
		if (!world.isRemote) {
			// Event horizon teleportation			
			if (stargateState == EnumStargateState.ENGAGED_INITIATING) {
				eventHorizon.scheduleTeleportation(StargateNetwork.get(world).getStargate(dialedAddress));
			}
						
			// Not initiating
			if (stargateState == EnumStargateState.ENGAGED) {
				getAutoCloseManager().update(StargateNetwork.get(world).getStargate(dialedAddress));
//				Aunis.info(scheduledTasks.toString());
			}
						
			// Scheduled tasks
			ScheduledTask.iterate(scheduledTasks, world.getTotalWorldTime());
			
			if (horizonFlashTask != null && horizonFlashTask.isActive()) {
				horizonFlashTask.update(world.getTotalWorldTime());
			}
			
			// Event horizon killing
			if (horizonKilling) {	
				List<EntityLivingBase> entities = new ArrayList<EntityLivingBase>();
				List<BlockPos> blocks = new ArrayList<BlockPos>();
				
				// Get all blocks and entities inside the kawoosh
				for (int i=0; i<getRendererState().horizonSegments; i++) {
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
					if (!dPos.equals(getLightBlockPos())) {
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
								
								horizonFlashTask = new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int) (Math.random() * 40) + 5);
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
							StargateRenderingUpdatePacketToServer.closeGatePacket(this, false);
					}
					
					else {
						StargateRenderingUpdatePacketToServer.closeGatePacket(this, false);
					}
				}
				
				else {
					if (world.getTotalWorldTime() % 20 == 0 && dialedAddress.size() > 0) {
						StargatePos sourcePos = StargateNetwork.get(world).getStargate(dialedAddress);
						
						// Only if not loaded
						if (!sourcePos.getWorld().isBlockLoaded(sourcePos.getPos())) {					
							StargateMilkyWayBaseTile sourceTile = (StargateMilkyWayBaseTile) sourcePos.getWorld().getTileEntity(sourcePos.getPos());
							
							sourceTile.updateEnergyStatus();
						}
					}
				}
			}
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
	
	// ------------------------------------------------------------------------
	// Killing and block vaporizing
	
	/**
	 * Gets full {@link AxisAlignedBB} of the killing area.
	 * @return Approximate kawoosh size.
	 */
	protected abstract AunisAxisAlignedBB getHorizonKillingBox();
	
	/**
	 * How many segments should the exclusion zone have.
	 * @return Count of subsegments of the killing box.
	 */
	protected abstract int getHorizonSegmentCount();
	
	/**
	 * The event horizon in the gate also should kill
	 * and vaporize everything
	 * @return List of {@link AxisAlignedBB} for the inner gate area.
	 */
	protected abstract List<AunisAxisAlignedBB> getGateVaporizingBoxes();
	
	/**
	 * How many ticks should the {@link StargateAbstractBaseTile} wait to perform
	 * next update to the size of the killing box.
	 */
	private int TICKS_PER_HORIZON_SEGMENT = 12 / getHorizonSegmentCount();
	
	/**
	 * Contains all the subboxes to be activated with the kawoosh.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localKillingBoxes;
	
	/**
	 * Contains all boxes of the inner part of the gate.
	 * Full blocks. Used for destroying blocks.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerBlockBoxes;
	
	/**
	 * Contains all boxes of the inner part of the gate.
	 * Not full blocks. Used for entity killing.
	 * On the server needs to be offsetted by the {@link TileEntity#getPos()}
	 */
	protected List<AunisAxisAlignedBB> localInnerEntityBoxes;
	
	private boolean horizonKilling;

	// ------------------------------------------------------------------------
	// Rendering
	
	protected abstract StargateRendererBase getRenderer();
	protected abstract StargateRendererStateBase getRendererState();
	protected abstract Vec3d getRenderTranslaton();
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {		
		if (AunisConfig.debugConfig.renderBoundingBoxes || AunisConfig.debugConfig.renderWholeKawooshBoundingBox) {
			eventHorizon.render(x, y, z);
			
			int segments = AunisConfig.debugConfig.renderWholeKawooshBoundingBox ? getHorizonSegmentCount() : getRendererState().horizonSegments;

			for (int i=0; i<segments; i++) {
				localKillingBoxes.get(i).render(x, y, z);
			}
						
			for (AunisAxisAlignedBB b : localInnerBlockBoxes)
				b.render(x, y, z);
		}
				
		Vec3d vec = getRenderTranslaton();
		
		x += vec.x;
		y += vec.y;
		z += vec.z;
		
		getRenderer().render(x, y, z, partialTicks);
				
		if (this instanceof ITileEntityUpgradeable) {
			((ITileEntityUpgradeable) this).getUpgradeRenderer().render(x, y, z, partialTicks);
		}
	}
	
	public void updateFacing(EnumFacing facing) {
		this.facing = facing;
		
		this.eventHorizon = new EventHorizon(world, pos, facing, getHorizonTeleportBox());
		
		AunisAxisAlignedBB kBox = getHorizonKillingBox();
		double width = kBox.maxZ - kBox.minZ;
		width /= getHorizonSegmentCount();
		
		localKillingBoxes = new ArrayList<AunisAxisAlignedBB>(getHorizonSegmentCount());
		for (int i=0; i<getHorizonSegmentCount(); i++) {
			AunisAxisAlignedBB box = new AunisAxisAlignedBB(kBox.minX, kBox.minY, kBox.minZ + width*i, kBox.maxX, kBox.maxY, kBox.minZ + width*(i+1));
			box = box.rotate(facing).offset(0.5, 0, 0.5);
			
			localKillingBoxes.add(box);
		}
		
		localInnerBlockBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		localInnerEntityBoxes = new ArrayList<AunisAxisAlignedBB>(3);
		for (AunisAxisAlignedBB lBox : getGateVaporizingBoxes()) {
			localInnerBlockBoxes.add(lBox.rotate(facing).offset(0.5, 0, 0.5));
			localInnerEntityBoxes.add(lBox.grow(0, 0, -0.25).rotate(facing).offset(0.5, 0, 0.5));
		}
		
		if (world.isRemote) {
			getRenderer().updateFacing(facing);
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
				
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), targetPoint);
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(tPos, StateTypeEnum.FLASH_STATE, new StargateFlashState(isCurrentlyUnstable)), new TargetPoint(targetPos.getDimension(), tPos.getX(), tPos.getY(), tPos.getZ(), 512));
	}
	
	
	// ------------------------------------------------------------------------
	// States

	@Override
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererState();
				
			default:
				return null;
		}
	}

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case RENDERER_STATE:
				return getRendererState();
		
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
				getRenderer().setRendererState((StargateRendererStateBase) state);
				break;
				
			case RENDERER_UPDATE:
				switch (((StargateRendererActionState) state).action) {
					case OPEN_GATE:
						getRendererState().horizonSegments = 0;
						getRenderer().openGate();
						break;
						
					case CLOSE_GATE:
						getRenderer().closeGate();
						break;
						
					case STARGATE_HORIZON_WIDEN:
						getRendererState().horizonSegments++;
						break;
						
					case STARGATE_HORIZON_SHRINK:
						getRendererState().horizonSegments--;
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
				getRenderer().setHorizonUnstable(((StargateFlashState) state).flash);
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
	public void executeTask(EnumScheduledTask scheduledTask) {		
		switch (scheduledTask) {
			case STARGATE_OPEN_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_OPEN, 0.3f);
				break;
				
			case STARGATE_HORIZON_LIGHT_BLOCK:
				world.setBlockState(getLightBlockPos(), AunisBlocks.invisibleBlock.getDefaultState().withProperty(AunisProps.HAS_COLLISIONS, false));
				break;
				
			case STARGATE_HORIZON_WIDEN:
				if (!horizonKilling)
					horizonKilling = true;
				
				getRendererState().horizonSegments++;
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_WIDEN_ACTION), targetPoint);
				
				if (getRendererState().horizonSegments < getHorizonSegmentCount())
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_WIDEN, TICKS_PER_HORIZON_SEGMENT));
				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, TICKS_PER_HORIZON_SEGMENT + 12));
				
				break;
				
			case STARGATE_HORIZON_SHRINK:
				getRendererState().horizonSegments--;
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, StargateRendererActionState.STARGATE_HORIZON_SHRINK_ACTION), targetPoint);
				
				if (getRendererState().horizonSegments > 0)
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_HORIZON_SHRINK, TICKS_PER_HORIZON_SEGMENT + 1));
				else
					horizonKilling = false;
				
				markDirty();
					
				break;
				
			case STARGATE_CLOSE:
				world.setBlockToAir(getLightBlockPos());
				disconnectGate();
				break;
				
			case STARGATE_ENGAGE:
				engageGate();
				
				if (!stargateState.initiating()) {
					world.notifyLightSet(getLightBlockPos());
					world.checkLightFor(EnumSkyBlock.BLOCK, getLightBlockPos());
				}
				
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
					horizonFlashTask = new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 3) + 3);
				}
				
				else {
					if (flashIndex == 1)
						// Schedule second flash
						horizonFlashTask = new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 4) + 1);
					
					else {
						// Schedule next flash sequence
						horizonFlashTask = new ScheduledTask(EnumScheduledTask.HORIZON_FLASH, (int)(Math.random() * 40) + 5);
						
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
	 * Checks if this {@link StargateAbstractBaseTile} can provide enough energy.
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
	public boolean hasEnergyToDial(StargateEnergyRequired energyRequired) {		
//		Aunis.info("Energy required to dial [distance="+distance+", mul="+multiplier+"] = " + energy + " / keepAlive: "+(keepAlive*20)+"/s, stored: " + (getEnergyStorage(1) != null ? getEnergyStorage(1).getEnergyStored() : 0));
				
		if (getEnergyStorage(energyRequired.energyToOpen) != null) {
			this.openCost = energyRequired.energyToOpen;
			this.keepAliveCostPerTick = energyRequired.keepAlive;
		
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
		
		compound.setTag("scheduledTasks", ScheduledTask.serializeList(scheduledTasks));
		
		if (node != null) {
			NBTTagCompound nodeCompound = new NBTTagCompound();
			node.save(nodeCompound);
			
			compound.setTag("node", nodeCompound);
		}
		
		compound.setBoolean("horizonKilling", horizonKilling);
		
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
				
		getAutoCloseManager().deserializeNBT(compound.getCompoundTag("autoCloseManager"));
		
		try {
			getRendererState().deserializeNBT(compound.getCompoundTag("rendererState"));
			ScheduledTask.deserializeList(compound.getCompoundTag("scheduledTasks"), scheduledTasks, this);
		}
		
		catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.info("Exception at reading NBT");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}
		
		energyStorage.deserializeNBT(compound.getCompoundTag("energyStorage"));
		
		this.openCost = compound.getInteger("openCost");
		this.keepAliveCostPerTick = compound.getInteger("keepAliveCostPerTick");
		
		this.gateOpenTime = compound.getLong("gateOpenTime");
		this.energyConsumed = compound.getInteger("energyConsumed");
		
		stargateState = EnumStargateState.valueOf(compound.getInteger("stargateState"));
				
		if (node != null && compound.hasKey("node"))
			node.load(compound.getCompoundTag("node"));
		
		horizonKilling = compound.getBoolean("horizonKilling");
		
		super.readFromNBT(compound);
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
		return new Object[] {gateAddress};
	}

	@Optional.Method(modid = "opencomputers")
	@Callback(getter = true)
	public Object[] dialedAddress(Context context, Arguments args) {
		return new Object[] {dialedAddress};
	}
}
