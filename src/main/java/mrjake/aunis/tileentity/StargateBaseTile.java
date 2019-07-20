package mrjake.aunis.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.vecmath.Vector2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.capability.EnergyStorageUncapped;
import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.gate.teleportPlayer.RetrieveMotionToClient;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.packet.update.renderer.RendererUpdatePacketToClient;
import mrjake.aunis.packet.update.renderer.RendererUpdateRequestToServer;
import mrjake.aunis.renderer.ISpecialRenderer;
import mrjake.aunis.renderer.stargate.StargateRenderer;
import mrjake.aunis.renderer.stargate.StargateRenderer.EnumVortexState;
import mrjake.aunis.renderer.stargate.StargateRingSpinHelper;
import mrjake.aunis.renderer.state.RendererState;
import mrjake.aunis.renderer.state.StargateRendererState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.stargate.StargateNetwork;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.state.EnergyState;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.FlashState;
import mrjake.aunis.state.ITileEntityStateProvider;
import mrjake.aunis.state.SpinStateRequest;
import mrjake.aunis.state.StargateGuiState;
import mrjake.aunis.state.State;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.tasks.IScheduledTaskExecutor;
import mrjake.aunis.upgrade.StargateUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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

public class StargateBaseTile extends TileEntity implements ITileEntityRendered, ITileEntityUpgradeable, ITickable, ICapabilityProvider, ITileEntityStateProvider, IScheduledTaskExecutor, ILinkable {
	public StargateBaseTile() {}
	
	private ISpecialRenderer<StargateRendererState> renderer;
	private RendererState rendererState;
	
	private StargateUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedDHD = null;
	
	private boolean isEngaged;
	private boolean isInitiating;
	
	private boolean unstableVortex;
	private boolean isClosing;
		
	private int playersPassed;
		
	public List<EnumSymbol> gateAddress = null;
	public List<EnumSymbol> dialedAddress = new ArrayList<EnumSymbol>();
	
	private StargateRingSpinHelper serverRingSpinHelper;
	
	protected StargateRingSpinHelper getServerRingSpinHelper() {
		if (serverRingSpinHelper == null)
			serverRingSpinHelper = new StargateRingSpinHelper(world, pos, null, getStargateRendererState().spinState);
		
		return serverRingSpinHelper;
	}
	
	/**
	 * List of scheduled tasks to be performed on {@link ITickable#update()}.
	 */
	private List<ScheduledTask> scheduledTasks = new ArrayList<>();
	
	/**
	 * Adds symbol to address. Called from GateRenderingUpdatePacketToServer. Handles all server-side consequences:
	 * 	- server ring movement cache
	 * 	- renderer's state
	 * 
	 * @param symbol - Currently added symbol
	 * @param dhdTile - Clicked DHD's Tile instance
	 * @return true if symbol was added
	 */
	public boolean addSymbolToAddress(EnumSymbol symbol, DHDTile dhdTile, boolean computer) {		
		if (dialedAddress.contains(symbol)) 
			return false;
		
		int maxChevrons;
		
		if (computer)
			maxChevrons = 8;
		else
			maxChevrons = (dhdTile.hasUpgrade() ? 8 : 7);
		
		if (dialedAddress.size() == maxChevrons)
			return false;
		
		// First glyph is pressed
		// Ring starts to spin
		if (dialedAddress.size() == 0 && !computer) {
			getServerRingSpinHelper().requestStart(getStargateRendererState().ringCurrentSymbol.angle);
			ringRollLoopPlayed = false;
			
			stargateState = EnumStargateState.DHD_DIALING;
		}
		
		dialedAddress.add(symbol);
		if (dhdTile != null)
			dhdTile.getDHDRendererState().activeButtons.add(symbol.id);
		
		if (dialedAddress.size() == maxChevrons || (dialedAddress.size() == 7 && symbol == EnumSymbol.ORIGIN)) {
			getStargateRendererState().setFinalActive(world, pos, true);
			
			if (!computer) {
				getServerRingSpinHelper().requestStop();
				
				stargateState = EnumStargateState.IDLE;
			}
		}
		
		else {
			getStargateRendererState().setActiveChevrons(world, pos, getStargateRendererState().getActiveChevrons() + 1);
		}
		
		markDirty();
		
		return true;
	}
	
	/**
	 * Called on receiving gate. Sets renderer's state
	 * 
	 * @param incomingAddress - Initializing gate's address
	 * @param dialedAddressSize - How many symbols are there pressed on the DHD
	 */
	public void incomingWormhole(List<EnumSymbol> incomingAddress, int dialedAddressSize) {
		getStargateRendererState().setActiveChevrons(world, pos, dialedAddressSize - 1);
		getStargateRendererState().setFinalActive(world, pos, true);
		
		DHDTile dhdTile = getLinkedDHD(world);
		
		if (dhdTile != null) {			
			dhdTile.getDHDRendererState().activeButtons = EnumSymbol.toIntegerList(incomingAddress.subList(0, dialedAddressSize - 1), EnumSymbol.ORIGIN);
		}
		
		sendSignal(null, "stargate_incoming_wormhole", new Object[] { dialedAddressSize });
	}
	
	/**
	 * Called on BRB press. Initializes renderer's state
	 * 
	 * @param initiating - true if gate is initializing the connection
	 * @param dialedAddressSize - Glyph count on initiating DHD
	 * @param incomingAddress - Source gate address
	 */
	public void openGate(boolean initiating, int dialedAddressSize, List<EnumSymbol> incomingAddress) {
		isInitiating = initiating;
		
		if (!isInitiating) {
			dialedAddress.clear();
			dialedAddress.addAll(incomingAddress);
		}
		
		unstableVortex = true;
		playersPassed = 0;
//		waitForEngage = world.getTotalWorldTime();
		
		if (isInitiating)
			getStargateRendererState().setActiveChevrons(world, pos, dialedAddress.size() - 1);
		else
			getStargateRendererState().setActiveChevrons(world, pos, dialedAddressSize - 1);
		
		getStargateRendererState().setFinalActive(world, pos, true);
		
		getStargateRendererState().doEventHorizonRender = true;
		getStargateRendererState().vortexState = EnumVortexState.STILL;
		getStargateRendererState().dialingComplete = true;
		
		addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_OPEN_SOUND));
		addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_ENGAGE));
		
		DHDTile dhdTile = getLinkedDHD(world);
		if (dhdTile != null) {
			dhdTile.getDHDRendererState().activeButtons.add(EnumSymbol.BRB.id);
		}
		
		if (isInitiating) {
//			Aunis.info("Opening gate, drawing " + openCost);
			((EnergyStorageUncapped) getEnergyStorage(openCost)).extractEnergyUncapped(openCost);
		}
		
		stargateState = EnumStargateState.ENGAGED;
		sendSignal(null, "stargate_open", new Object[] { isInitiating });
		
		markDirty();
	}
	
	public boolean fastDialer;
	
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
	
	public void updateEnergyStatus() {
		long ticks = world.getTotalWorldTime() - gateOpenTime;
		int energy = (int) (ticks * keepAliveCostPerTick);
				
		energy -= energyConsumed;
		
		energyStorage.extractEnergyUncapped(energy);
	}
	
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
	public IEnergyStorage getEnergyStorage(int minEnergy) {
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
	 * Called when vortex becomes stable. From now players are being teleported
	 * 
	 */
	private void engageGate() {	
		gateOpenTime = world.getTotalWorldTime();
		energyConsumed = 0;
		
		unstableVortex = false;
		isEngaged = true;
		gateCloseTimeout = 5;
		
		if (fastDialer) {
			AunisPacketHandler.INSTANCE.sendToAllTracking(new RendererUpdatePacketToClient(pos, getRendererState()), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32768));
			fastDialer = false;
		}
		
		markDirty();
	}
	
	
	/**
	 * Called either on pressing BRB on open gate or by pressing BRB on malformed address
	 * 
	 * @param dialingFailed - True if second case above
	 * @param stopRing - If using DHD, then true
	 */
	public void closeGate(boolean dialingFailed, boolean stopRing) {		
		if (!dialingFailed) {		
			isClosing = true;
			isEngaged = false;
			gateCloseTimeout = 5;
			
			addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CLOSE));
			
//			stargateState = EnumStargateState.CLOSING;
			// TODO: Move all the logic of isClosing to StargateState
			
			getStargateRendererState().doEventHorizonRender = false;
			getStargateRendererState().dialingComplete = false;
			
			sendSignal(null, "stargate_close", new Object[] {});
		}
		
		else {
			if (stopRing)
				getServerRingSpinHelper().requestStop();
			
			stargateState = EnumStargateState.FAILING;
			
			addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_FAIL));
			
			sendSignal(null, "stargate_failed", new Object[] {});
		}
			
		dialedAddress.clear();
		horizonFlashTask = null;
		
		getStargateRendererState().setActiveChevrons(world, pos, 0);
		getStargateRendererState().setFinalActive(world, pos, false);
		clearLinkedDHDButtons(dialingFailed);

		markDirty();
	}
	
	/**
	 * Called on event horizon fully closed
	 * 
	 */
	private void disconnectGate() {	
		isClosing = false;
		stargateState = EnumStargateState.IDLE;
				
		markDirty();
	}
	
	public boolean isEngaged() {
		return isEngaged;
	}
	
	public boolean isInitiating() {
		return isInitiating;
	}
	
	public boolean isOpening() {
		return unstableVortex;
	}
	
	public boolean isClosing() {
		return isClosing;
	}
	
	private boolean isMerged;
	
	public boolean isMerged() {
		return isMerged;
	}
	
	/**
	 * Checks gate's merge state
	 * 
	 * @param isMerged - True if gate's multiblock structure is valid
	 * @param state State of base block(when destroyed we can't get it from world). If null, get it from the world
	 */
	public void updateMergeState(boolean isMerged, @Nullable IBlockState state) {		
		this.isMerged = isMerged;
		
		if (!isMerged) {
			if (isLinked()) {
				getLinkedDHD(world).setLinkedGate(null);
				
				linkedDHD = null;
			}
			
			if (isEngaged()) {
				GateRenderingUpdatePacketToServer.closeGatePacket(this, true);
			}
		}
		
		else {
			BlockPos closestDhd = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.dhdBlock);
			
			if (closestDhd != null) {
				DHDTile dhdTile = (DHDTile) world.getTileEntity(closestDhd);
				
				dhdTile.setLinkedGate(pos);
				setLinkedDHD(closestDhd);
			}
		}
		
		IBlockState actualState = world.getBlockState(pos);
		
		// When the block is destroyed, there will be air in this place and we cannot set it's block state
		if (actualState.getBlock() == AunisBlocks.stargateBaseBlock)
			world.setBlockState(pos, actualState.withProperty(AunisProps.RENDER_BLOCK, !isMerged), 2);
		
		MergeHelper.updateChevRingMergeState(world, pos, (state != null) ? state : actualState, isMerged);
		
		markDirty();
	}
	
	@Override
	public ISpecialRenderer<StargateRendererState> getRenderer() {
		if (renderer == null)
			renderer = new StargateRenderer(this);
		
		return renderer;
	}
	
	public StargateRenderer getStargateRenderer() {
		return (StargateRenderer) getRenderer();
	}
	
	public StargateRendererState getStargateRendererState() {
		return (StargateRendererState) getRendererState();
	}
	
	@Override
	public RendererState getRendererState() {
		if (rendererState == null)
			rendererState = new StargateRendererState();
				
		return rendererState;
	}
	
	@Override
	public RendererState createRendererState(ByteBuf buf) {
		return new StargateRendererState().fromBytes(buf);
	}
	
	@Override
	public UpgradeRenderer getUpgradeRenderer() {
		if (upgradeRenderer == null)
			upgradeRenderer = new StargateUpgradeRenderer(world, getStargateRenderer().getHorizontalRotation());
		
		return upgradeRenderer;
	}
	
	@Override
	public UpgradeRendererState getUpgradeRendererState() {
		if (upgradeRendererState == null)
			upgradeRendererState = new UpgradeRendererState();
		
		return upgradeRendererState;
	}
	
	public int getEnteredSymbolsCount() {
		return dialedAddress.size();
	}
	
//	public BlockPos getLinkedDHD() {
//		return linkedDHD;
//	}
	
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
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {		
		if (isLinked())
			compound.setLong("linkedDHD", linkedDHD.toLong());
				
		if (gateAddress != null) {
			for (int i=0; i<7; i++) {
				compound.setInteger("symbol"+i, gateAddress.get(i).id);
			}
		}
		
		compound.setBoolean("isMerged", isMerged);
		compound.setBoolean("isEngaged", isEngaged);
		compound.setBoolean("isInitiating", isInitiating);
		compound.setBoolean("unstableVortex", unstableVortex);

		compound.setBoolean("hasUpgrade", hasUpgrade);
		
		compound.setInteger("dialedAddressLength", dialedAddress.size());
		
		for (int i=0; i<dialedAddress.size(); i++) {
			compound.setInteger("dialedSymbol"+i, dialedAddress.get(i).id);
		}
		
		compound.setBoolean("unstableVortex", unstableVortex);
		compound.setBoolean("isClosing", isClosing);
		compound.setBoolean("clearingButtons", clearingButtons);
		compound.setLong("waitForClear", waitForClear);
		compound.setBoolean("ringRollLoopPlayed", ringRollLoopPlayed);
				
		compound.setInteger("playersPassed", playersPassed);
		
		getStargateRendererState().toNBT(compound);
		getUpgradeRendererState().toNBT(compound);
		
		compound.setTag("energyStorage", energyStorage.serializeNBT());
		compound.setInteger("openCost", openCost);
		compound.setInteger("keepAliveCostPerTick", keepAliveCostPerTick);
		
		compound.setLong("gateOpenTime", gateOpenTime);
		compound.setInteger("energyConsumed", energyConsumed);
		
		compound.setBoolean("targetSymbolDialing", targetSymbolDialing);
		if (targetSymbol != null)
			compound.setInteger("targetSymbol", targetSymbol.id);
		
		if (spinDirection != null)
			compound.setInteger("lastSpinDirection", spinDirection.id);
		
		if (stargateState != null)
			compound.setInteger("stargateState", stargateState.id);
		
		for (int i=0; i<scheduledTasks.size(); i++)
			compound.setTag("scheduledTask"+i, scheduledTasks.get(i).serializeNBT());
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedDHD"))
			this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );
		
		boolean compoundHasAddress = compound.hasKey("symbol0");
		
		if (compoundHasAddress) {		
			gateAddress = new ArrayList<EnumSymbol>();
			
			for (int i=0; i<7; i++) {
				int id = compound.getInteger("symbol"+i);
				gateAddress.add( EnumSymbol.valueOf(id) );
			}
		}
		
		isMerged = compound.getBoolean("isMerged");
		isEngaged = compound.getBoolean("isEngaged");
		isInitiating = compound.getBoolean("isInitiating");
		unstableVortex = compound.getBoolean("unstableVortex");

		hasUpgrade = compound.getBoolean("hasUpgrade");
		
		dialedAddress.clear();
		int dialedAddressLength = compound.getInteger("dialedAddressLength");
		
		for (int i=0; i<dialedAddressLength; i++) {
			dialedAddress.add( EnumSymbol.valueOf(compound.getInteger("dialedSymbol"+i)) );
		}
		
		isClosing = compound.getBoolean("isClosing");
		clearingButtons = compound.getBoolean("clearingButtons");
		waitForClear = compound.getLong("waitForClear");
		ringRollLoopPlayed = compound.getBoolean("ringRollLoopPlayed");
				
		playersPassed = compound.getInteger("playersPassed");
		
		getRendererState().fromNBT(compound);
		getUpgradeRendererState().fromNBT(compound);
		
		energyStorage.deserializeNBT((NBTTagCompound) compound.getTag("energyStorage"));
		
		this.openCost = compound.getInteger("openCost");
		this.keepAliveCostPerTick = compound.getInteger("keepAliveCostPerTick");
		
		this.gateOpenTime = compound.getLong("gateOpenTime");
		this.energyConsumed = compound.getInteger("energyConsumed");
		
		targetSymbolDialing = compound.getBoolean("targetSymbolDialing");

		if (compound.hasKey("targetSymbol"))
			targetSymbol = EnumSymbol.valueOf(compound.getInteger("targetSymbol"));
		else
			targetSymbol = null;
		
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("lastSpinDirection"));
		stargateState = EnumStargateState.valueOf(compound.getInteger("stargateState"));
		
		for (int i=0; i<scheduledTasks.size(); i++)
			scheduledTasks.add(new ScheduledTask(this, (NBTTagCompound) compound.getTag("scheduledTask"+i)));
		
		super.readFromNBT(compound);
	}
	
	private boolean firstTick = true;
	
	// Where to place event horizon
	private final float placement = 0.5f;
	
	// How wide it should be
	private final float delta = 0.2f;
	
	// Other dimensions
	private final float left = -2;
	private final float right = 3;
	private final float up = 8;
	
	// Calculated box
	public AxisAlignedBB horizonBoundingBox;
		
	public static class TeleportPacket {
		private BlockPos sourceGatePos;
		private StargatePos targetGatePos;
		
		private float rotation;
		
		private Vector2f motionVector;
		
		public TeleportPacket(BlockPos source, StargatePos target, float rotation) {
			sourceGatePos = source;
			targetGatePos = target;
			
			this.rotation = rotation;
		}
		
		public void teleport(Entity entity) {
			TeleportHelper.teleportEntity(entity, sourceGatePos, targetGatePos, rotation, motionVector);
			
			AunisSoundHelper.playSoundEvent(targetGatePos.getWorld(), targetGatePos.getPos(), EnumAunisSoundEvent.WORMHOLE_GO, 1.0f);
		}

		public TeleportPacket setMotion(Vector2f motion) {
			this.motionVector = motion;
			
			return this;
		}
	}
	
	public Map<Integer, TeleportPacket> scheduledTeleportMap = new HashMap<Integer, TeleportPacket>();
	
	public void teleportEntity(int entityId) {
		Entity entity = world.getEntityByID(entityId);
		scheduledTeleportMap.get(entityId).teleport(entity);
		
		AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.WORMHOLE_GO, 1.0f);
		
		entityPassing(entity, false);
		
		removeEntityFromTeleportList(entityId);
	}
	
	public void removeEntityFromTeleportList(int entityId) {		
		scheduledTeleportMap.remove(entityId);
	}
	
	protected boolean ringRollLoopPlayed = true;
	
	public void setRollPlayed() {
		ringRollLoopPlayed = true;
	}
	
	public boolean getRollPlayed() {
		return ringRollLoopPlayed;
	}
		
	private boolean clearingButtons;
	private long waitForClear;
	private int clearDelay;
	
	private void clearLinkedDHDButtons(boolean dialingFailed) { // 29 : 65
		DHDTile dhdTile = getLinkedDHD(world);
				
		if (dhdTile != null) {
//			dhdTile.clearRendererButtons();
		
			clearDelay = dialingFailed ? 39 : 62; // 39 : 65;
			
			waitForClear = world.getTotalWorldTime();
			clearingButtons = true;
		}		
	}
	
	private int gateCloseTimeout = 5;
		
	@Override
	public void update() {
		if (firstTick) {
			firstTick = false;
						
			// Client loaded region, need to update
			// Send rendererState to client's renderer
			if (world.isRemote)
				AunisPacketHandler.INSTANCE.sendToServer( new RendererUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide()) );
			
			// Can't do this in onLoad(), because in that method, world isn't fully loaded
			generateAddress();
			
			if (!world.isRemote) {				
				String[] namesArray = new String[gateAddress.size()];
				
				for (int i=0; i<gateAddress.size(); i++)
					namesArray[i] = gateAddress.get(i).name;
				
				sendSignal(null, "stargate_added", (Object[]) namesArray);
				
				EnumFacing sourceGateFacing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
				
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				
				if (sourceGateFacing.getAxis().getName() == "z") {
					// North - South
					// Stargate divided on X axis
					
					float z1 = placement - delta;
					float z2 = placement + delta;
					
					horizonBoundingBox = new AxisAlignedBB(x+left, y+1, z+z1,  x+right, y+up, z+z2);
				}
					
				else {
					// West - East
					//  Stargate divided on Z axis
						
					float x1 = placement - delta;
					float x2 = placement + delta;
					
					horizonBoundingBox = new AxisAlignedBB(x+x1, y+1, z+left,  x+x2, y+up, z+right);
				}
			}
		}
		
		
		// Scan for entities passing through event horizon
		if (!world.isRemote && horizonBoundingBox != null && isEngaged && isInitiating) {
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, horizonBoundingBox);
//			StargateNetwork.get(world).getStargate(dialedAddress);
			for (Entity entity : entities) {
				int entId = entity.getEntityId();
				
				// If entity not added to scheduled teleport list
				if ( !scheduledTeleportMap.containsKey(entId) ) {
					StargatePos targetGate = StargateNetwork.get(world).getStargate( dialedAddress );
					if (targetGate != null) {	
						try {
							World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
							
							BlockPos targetPos = targetGate.getPos();
							// StargateBaseTile targetTile = (StargateBaseTile) targetWorld.getTileEntity(targetPos);
							
							EnumFacing sourceFacing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
							EnumFacing targetFacing = targetWorld.getBlockState(targetPos).getValue(AunisProps.FACING_HORIZONTAL);
							
							float rotation = (float) Math.toRadians( EnumFacing.fromAngle(targetFacing.getHorizontalAngle() - sourceFacing.getHorizontalAngle()).getOpposite().getHorizontalAngle() );
		
							TeleportPacket packet = new TeleportPacket(pos, targetGate, rotation);
							
							if (entity instanceof EntityPlayerMP) {
								scheduledTeleportMap.put(entId, packet);
								AunisPacketHandler.INSTANCE.sendTo(new RetrieveMotionToClient(pos), (EntityPlayerMP) entity);
							}
							
							else {
								Vector2f motion = new Vector2f( (float)entity.motionX, (float)entity.motionZ );
								
								if (TeleportHelper.frontSide(sourceFacing, motion)) {
									scheduledTeleportMap.put(entId, packet.setMotion(motion) );
									teleportEntity(entId);
								}
								
								/*else {
									// TODO Make custom message appear
									// entity.onKillCommand();
								}*/
							}
						}
						
						catch (Exception e) {
							e.printStackTrace();
							
							scheduledTeleportMap.remove(entId);
						}
					}
				}
			}
		}
		
		// AutoClose (on server) (engaged) (target gate)
		// Scan for load status of the source gate
		// Every X ticks
		//   Unloaded -- then no player is in the area
		//   Loaded   -- then scan for players
		if (!world.isRemote && isEngaged && !isInitiating) {
			if (world.getTotalWorldTime() % 20 == 0) {
				if (dialedAddress.size() > 0) {
					StargatePos sourceStargatePos = StargateNetwork.get(world).getStargate(dialedAddress);
					
					World sourceWorld = sourceStargatePos.getWorld();
					BlockPos sourcePos = sourceStargatePos.getPos();
					
					boolean sourceLoaded = sourceWorld.isBlockLoaded(sourcePos);
					
					if (getEntitiesPassed() > 0) {
						if (sourceLoaded) {
							
							AxisAlignedBB scanBox = new AxisAlignedBB(sourcePos.add(new Vec3i(-10, -5, -10)), sourcePos.add(new Vec3i(10, 5, 10)));
							int entityCount = sourceWorld.getEntitiesWithinAABB(EntityPlayerMP.class, scanBox, player -> !player.isDead).size();
							
							if (entityCount == 0)
								gateCloseTimeout--;
							else
								gateCloseTimeout = 5;
						}
						
						else {
							gateCloseTimeout--;
						}
					}
										
					if (gateCloseTimeout == 0) {
						GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
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
		if (isEngaged) {
			if (isInitiating) {

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
						StargateBaseTile sourceTile = (StargateBaseTile) sourcePos.getWorld().getTileEntity(sourcePos.getPos());
						
						sourceTile.updateEnergyStatus();
					}
				}
			}
		}
		
		if (!world.isRemote) {
			if (clearingButtons) {
				if (world.getTotalWorldTime()-waitForClear >= clearDelay) { 				
					if (isLinked())
						getLinkedDHD(world).clearSymbols();
//						AunisPacketHandler.INSTANCE.sendToAllTracking(new ClearLinkedDHDButtonsToClient(linkedDHD), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
					
					clearingButtons = false;
				}
			}
			
			if (!ringRollLoopPlayed && (world.getTotalWorldTime() - getStargateRendererState().spinState.tickStart) > 98) {
				ringRollLoopPlayed = true;
				
				AunisSoundHelper.playPositionedSound(world,  pos, EnumAunisPositionedSound.RING_ROLL_LOOP, true);
			}
			
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
		}
		
		if (!world.isRemote && getStargateRendererState().spinState.isSpinning) {
			float ringAngularRotation = (float) (getServerRingSpinHelper().spin(0) % 360);
						
			if (targetSymbolDialing) {				
				if (spinDirection.getDistance(ringAngularRotation, (float) this.targetSymbol.angle) <= StargateRingSpinHelper.getStopAngleTraveled()) {
					getServerRingSpinHelper().requestStopByComputer(world.getTotalWorldTime(), false);
				
					targetSymbolDialing = false;
				}
			}
			
			markDirty();
		}
	}
	
	public List<EnumSymbol> generateAddress() {
		
		// Server
		if ( !world.isRemote ) {			
			if ( gateAddress == null ) {
				Random rand = new Random(pos.hashCode() * 31 + world.provider.getDimension());
				List<EnumSymbol> address = new ArrayList<EnumSymbol>(); 
					
				while (true) {
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
				
				// Possibly TODO: Add region, so if we break the stargate and place it nearby, it keeps the address
			}			
		}
		
		return gateAddress;
	}
	
	
	// -----------------------------------------------------------------
	// Power system
	
	private int openCost = 0;
	private int keepAliveCostPerTick = 0;
	
	private EnergyStorageUncapped energyStorage = new EnergyStorageUncapped(AunisConfig.powerConfig.stargateEnergyStorage, AunisConfig.powerConfig.stargateMaxEnergyTransfer) {
		protected void onEnergyChanged() {			
			markDirty();
		};
	};
	

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
	
	// -----------------------------------------------------------------
	// States
	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new StargateGuiState(gateAddress, hasUpgrade, energyStorage.getMaxEnergyStored(), new EnergyState(energyStorage.getEnergyStored()));
				
			case ENERGY_STATE:
				return new EnergyState(energyStorage.getEnergyStored());
				
			case SPIN_STATE:
				return null;
				// It shouldn't be done this way. This is only sent from the server(new instance each time). See StargateRingSpinHelper@syncStartToClient()
				
			case FLASH_STATE:
				return null;
				
			default:
				return null;
		}
	}
	
	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case GUI_STATE:
				return new StargateGuiState();
				
			case ENERGY_STATE:
				return new EnergyState();
				
			case SPIN_STATE:
				return new SpinStateRequest();
				
			case FLASH_STATE:
				return new FlashState();
				
			default:
				return null;
		}
	}
	
	private StargateGUI openGui;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {		
		switch (stateType) {
			case GUI_STATE:
				if (openGui == null || !openGui.isOpen) {
					openGui = new StargateGUI(pos, (StargateGuiState) state);
					Minecraft.getMinecraft().displayGuiScreen(openGui);
				}
				
				else {
					openGui.state = (StargateGuiState) state;
				}
				
				break;
				
			case ENERGY_STATE:
				if (openGui != null && openGui.isOpen) {
					openGui.state.energyState = (EnergyState) state;
				}
				
				break;
				
			case SPIN_STATE:			
				SpinStateRequest spinStateRequest = (SpinStateRequest) state;
				
				if (spinStateRequest.moveOnly)
					getStargateRenderer().requestFinalMove(world.getTotalWorldTime(), spinStateRequest.lock);
				else
					getStargateRenderer().setRingSpin(true, true, spinStateRequest);
				
				break;
				
			case FLASH_STATE:
				getStargateRenderer().setHorizonUnstable(((FlashState) state).flash);
				
				break;
				
			default:
				break;
		}
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
				
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.FLASH_STATE, new FlashState(isCurrentlyUnstable)), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(tPos, EnumStateType.FLASH_STATE, new FlashState(isCurrentlyUnstable)), new TargetPoint(targetPos.getDimension(), tPos.getX(), tPos.getY(), tPos.getZ(), 512));
	}
	
	// -----------------------------------------------------------------
	// Scheduled tasks
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
				
			case STARGATE_CHEVRON_SHUT_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1.0f);
				break;
				
			case STARGATE_CHEVRON_OPEN_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_OPEN, 1.0f);
				break;
				
			case STARGATE_CHEVRON_LOCK_DHD_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCK_DHD, 0.5f);
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
	
	public void addTask(ScheduledTask scheduledTask) {
		scheduledTasks.add(scheduledTask);
		
		markDirty();
	}
	
	// -----------------------------------------------------------------
	// Ring rotation	
	protected EnumSymbol targetSymbol = EnumSymbol.ORIGIN;
	protected boolean targetSymbolDialing = false;
	protected EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	protected EnumStargateState stargateState = EnumStargateState.IDLE;
	
	public void markStargateIdle() {
		stargateState = EnumStargateState.IDLE;
	}
	
	public EnumStargateState getStargateState() {
		return stargateState;
	}
	
	public void setEndingSymbol(EnumSymbol symbol) {
		getStargateRendererState().ringCurrentSymbol = symbol;
		
		markDirty();
	}
	
	// -----------------------------------------------------------------
	// OpenComputers
	public void sendSignal(Object context, String name, Object... params) {}
	
	// -----------------------------------------------------------------
	// Upgrade
	private boolean hasUpgrade = false;
	
	@Override
	public boolean hasUpgrade() {
		return hasUpgrade;
	}
	
	@Override
	public void setUpgrade(boolean hasUpgrade) {
		this.hasUpgrade = hasUpgrade;
		
		markDirty();
	}

	@Override
	public Item getAcceptedUpgradeItem() {
		return AunisItems.crystalGlyphStargate;
	}
	
	public int getEntitiesPassed() {
		return playersPassed;
	}
	
	public void entityPassing(Entity entity, boolean inbound) {
		boolean player = entity instanceof EntityPlayerMP;
				
		if (player) {
			playersPassed++;
			markDirty();
		}
		
		sendSignal(null, "stargate_traveler", new Object[] {inbound, player});
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
}
