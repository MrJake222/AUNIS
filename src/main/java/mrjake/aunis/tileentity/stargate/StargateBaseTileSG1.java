package mrjake.aunis.tileentity.stargate;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.gui.StargateGUI;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.renderer.stargate.StargateRendererSG1;
import mrjake.aunis.renderer.stargate.StargateRingSpinHelper;
import mrjake.aunis.renderer.state.RendererGateActionState;
import mrjake.aunis.renderer.state.UpgradeRendererState;
import mrjake.aunis.renderer.state.RendererGateActionState.EnumGateAction;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateSG1;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumGateState;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.MergeHelper;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.state.EnergyState;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.SpinStateRequest;
import mrjake.aunis.state.StargateGuiState;
import mrjake.aunis.state.State;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.ScheduledTask;
import mrjake.aunis.upgrade.StargateUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StargateBaseTileSG1 extends StargateBaseTile implements ITileEntityUpgradeable, ILinkable {
	public StargateBaseTileSG1() {}
	
	private StargateUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedDHD = null;
	private StargateRingSpinHelper serverRingSpinHelper;
	
	protected StargateRingSpinHelper getServerRingSpinHelper() {
		if (serverRingSpinHelper == null)
			serverRingSpinHelper = new StargateRingSpinHelper(world, pos, null, rendererState.spinState);
		
		return serverRingSpinHelper;
	}
	
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	private static final List<EnumGateAction> ACTIONS_SUPPORTED = Arrays.asList(
			EnumGateAction.ACTIVATE_NEXT,
			EnumGateAction.ACTIVATE_FINAL,
			EnumGateAction.GATE_DIAL_FAILED,
			EnumGateAction.LIGHT_UP_CHEVRONS);
	
	
	@Override
	protected boolean isActionSupported(EnumGateAction action) {
		return ACTIONS_SUPPORTED.contains(action) || super.isActionSupported(action);
	}
	
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	@Override
	protected EventHorizon getEventHorizon()  {
		if (eventHorizon == null)
			eventHorizon = new EventHorizon(world, pos, -3, 4, 1.5f, 8.7f);
		
		return eventHorizon;
	}
	
	@Override
	protected int getMaxChevrons(boolean computer, DHDTile dhdTile) {		
		if (computer)
			return 8;
		else
			return (dhdTile.hasUpgrade() ? 8 : 7);
	}
	
	@Override
	protected void firstGlyphDialed(boolean computer) {
		 if (!computer) {
			getServerRingSpinHelper().requestStart(rendererState.ringCurrentSymbol.angle);
			ringRollLoopPlayed = false;
			
			stargateState = EnumStargateState.DHD_DIALING;
		}
	}
	
	@Override
	protected void lastGlyphDialed(boolean computer) {
		if (!computer) {
			getServerRingSpinHelper().requestStop();
			
			stargateState = EnumStargateState.IDLE;
		}
	}	
	
	@Override
	protected void dialingFailed(boolean stopRing) {
		if (stopRing) {
			getServerRingSpinHelper().requestStop();
		}
	}
	
	protected void clearLinkedDHDButtons(boolean dialingFailed) { // 29 : 65
		DHDTile dhdTile = getLinkedDHD(world);
				
		if (dhdTile != null) {		
			clearDelay = dialingFailed ? 39 : 62; // 39 : 65;
			
			waitForClear = world.getTotalWorldTime();
			clearingButtons = true;
		}		
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
			
			if (stargateState.engaged()) {
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
	public UpgradeRenderer getUpgradeRenderer() {
		if (upgradeRenderer == null)
			upgradeRenderer = new StargateUpgradeRenderer(world, renderer.getHorizontalRotation());
		
		return upgradeRenderer;
	}
	
	@Override
	public UpgradeRendererState getUpgradeRendererState() {
		if (upgradeRendererState == null)
			upgradeRendererState = new UpgradeRendererState();
		
		return upgradeRendererState;
	}
	
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
		
		compound.setBoolean("isMerged", isMerged);
		compound.setBoolean("hasUpgrade", hasUpgrade);
		
		compound.setBoolean("clearingButtons", clearingButtons);
		compound.setLong("waitForClear", waitForClear);
		compound.setBoolean("ringRollLoopPlayed", ringRollLoopPlayed);
		
		getUpgradeRendererState().toNBT(compound);
		
		compound.setBoolean("targetSymbolDialing", targetSymbolDialing);
		if (targetSymbol != null)
			compound.setInteger("targetSymbol", targetSymbol.id);
		
		if (spinDirection != null)
			compound.setInteger("lastSpinDirection", spinDirection.id);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedDHD"))
			this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );
		
		isMerged = compound.getBoolean("isMerged");
		hasUpgrade = compound.getBoolean("hasUpgrade");
		
		clearingButtons = compound.getBoolean("clearingButtons");
		waitForClear = compound.getLong("waitForClear");
		ringRollLoopPlayed = compound.getBoolean("ringRollLoopPlayed");
				
		getUpgradeRendererState().fromNBT(compound);
		
		targetSymbolDialing = compound.getBoolean("targetSymbolDialing");

		if (compound.hasKey("targetSymbol"))
			targetSymbol = EnumSymbol.valueOf(compound.getInteger("targetSymbol"));
		else
			targetSymbol = null;
		
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("lastSpinDirection"));
		
		super.readFromNBT(compound);
	}
	
	protected boolean ringRollLoopPlayed = true;
	
	public void setRollPlayed() {
		ringRollLoopPlayed = true;
	}
	
	/**
	 * Set by {@link StargateRingSpinHelper#stopRequestedAction(effectiveTick)} to stop all ring sounds
	 */
	protected boolean ringRollStopFlag = false;
	
	public void setRingRollStopFlag(boolean ringRollStopFlag) {
		this.ringRollLoopPlayed = true;
		this.ringRollStopFlag = ringRollStopFlag;
	}
		
	private boolean clearingButtons;
	private long waitForClear;
	private int clearDelay;
		
	@Override
	public void onLoad() {		
		super.onLoad();
		
		renderer = new StargateRendererSG1(this);
		
//		if (world.isRemote) {
//			AunisPacketHandler.INSTANCE.sendToServer( new RendererUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide()) );
//		}
//		
//		else {
//			
//		}
	}
	
	@Override
	public void update() {
		super.update();
		
		if (!world.isRemote) {
			if (clearingButtons) {
				if (world.getTotalWorldTime()-waitForClear >= clearDelay) { 				
					if (isLinked())
						getLinkedDHD(world).clearSymbols();
//						AunisPacketHandler.INSTANCE.sendToAllTracking(new ClearLinkedDHDButtonsToClient(linkedDHD), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
					
					clearingButtons = false;
				}
			}
			
			if (!ringRollLoopPlayed && (world.getTotalWorldTime() - rendererState.spinState.tickStart) > 98) {
				ringRollLoopPlayed = true;
				
				if (!ringRollStopFlag) {
					AunisSoundHelper.playPositionedSound(world,  pos, EnumAunisPositionedSound.RING_ROLL_LOOP, true);
				}
			}
			
			if (ringRollStopFlag) {
				ringRollStopFlag = false;
				
				if (ringRollLoopPlayed)
					AunisSoundHelper.playPositionedSound(world, pos, EnumAunisPositionedSound.RING_ROLL_LOOP, false);
				else
					AunisSoundHelper.playPositionedSound(world, pos, EnumAunisPositionedSound.RING_ROLL_START, false);
			}
		
			if (rendererState.spinState.isSpinning) {
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
	}
	
	// ------------------------------------------------------------------------
	// Rendering
	
	StargateRendererSG1 renderer;
	StargateRendererStateSG1 rendererState = new StargateRendererStateSG1();
	
	@Override
	protected StargateRendererStateBase getRendererState() {
		return rendererState;
	}
	
	private StargateRendererStateSG1 getRendererStateSG1() {
		return (StargateRendererStateSG1) rendererState;
	}
	
	@Override
	public StargateRendererBase getRenderer() {
		return renderer;
	}
	
	private static float GATE_DIAMETER = 10.1815f;
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		getEventHorizon().render(x, y, z);
		
		x += 0.50;
		y += GATE_DIAMETER/2;
		z += 0.50;
		
		super.render(x, y, z, partialTicks);
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
				
			default:
				return super.getState(stateType);
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
				
			default:
				return super.createState(stateType);
		}
	}
	
	private StargateGUI openGui;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {		
		switch (stateType) {		
			case RENDERER_UPDATE:
				RendererGateActionState gateActionState = (RendererGateActionState) state;
				
				switch (gateActionState.action) {
					case ACTIVATE_NEXT:
						renderer.activateNextChevron(!gateActionState.computer);
						break;
						
					case ACTIVATE_FINAL:
						renderer.activateFinalChevron(!gateActionState.computer);
						break;
						
					case GATE_DIAL_FAILED:
						renderer.setRingSpin(false, false);
						renderer.clearChevrons();
						break;
						
					case LIGHT_UP_CHEVRONS:
						renderer.lightUpChevrons(gateActionState.chevronCount);
						break;
						
					default:
						super.setState(stateType, gateActionState);
						break;
				}
				
				break;
		
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
					renderer.requestFinalMove(world.getTotalWorldTime(), spinStateRequest.lock);
				else
					renderer.setRingSpin(true, true, spinStateRequest);
				
				break;
				
			default:
				super.setState(stateType, state);
		}
	}
	
	// -----------------------------------------------------------------
	// Scheduled tasks
	@Override
	public void executeTask(EnumScheduledTask scheduledTask) {
		switch (scheduledTask) {				
			case STARGATE_CHEVRON_SHUT_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1.0f);
				break;
				
			case STARGATE_CHEVRON_OPEN_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_OPEN, 1.0f);
				break;
				
			case STARGATE_CHEVRON_LOCK_DHD_SOUND:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCK_DHD, 0.5f);
				break;
				
			default:
				super.executeTask(scheduledTask);;
		}
	}
	
	// -----------------------------------------------------------------
	// Ring rotation	
	protected EnumSymbol targetSymbol = EnumSymbol.ORIGIN;
	protected boolean targetSymbolDialing = false;
	protected EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	
	public void markStargateIdle() {
		stargateState = EnumStargateState.IDLE;
	}

	public void setEndingSymbol(EnumSymbol symbol) {
		rendererState.ringCurrentSymbol = symbol;
		
		markDirty();
	}
	
	// -----------------------------------------------------------------
	// OpenComputers methods
	
	@Callback(doc = "function(symbolName:string) -- Spins the ring to the given symbol and engages/locks it")
	public Object[] engageSymbol(Context context, Arguments args) {
		if (!stargateState.idle()) {
			return new Object[] {null, "stargate_busy", stargateState.toString()};
		}
		
		if (gateAddress.size() == 8) {
			return new Object[] {null, "stargate_failure_full", "Already dialed 8 chevrons"};
		}
		
		String name = args.checkString(0);
		
		EnumSymbol symbol = EnumSymbol.forName(name);
		
		if (symbol == null)
			throw new IllegalArgumentException("bad argument #1 (symbol name invalid)");
		
		boolean moveOnly = symbol == targetSymbol;
		
		if (dialedAddress.contains(symbol)) {
			return new Object[] {null, "stargate_failure_contains", "Dialed address contains this symbol already"};
		}
		
		this.targetSymbol = symbol;	
		this.targetSymbolDialing = true;
		
		spinDirection = spinDirection.opposite();
		
		double distance = spinDirection.getDistance(getRendererStateSG1().ringCurrentSymbol.angle, symbol.angle);
//			Aunis.info("position: " + getStargateRendererState().ringCurrentSymbol.angle + ", target: " + targetSymbol + ", direction: " + spinDirection + ", distance: " + distance + ", moveOnly: " + moveOnly);
		
		if (distance < (StargateRingSpinHelper.getStopAngleTraveled() + 5))
			spinDirection = spinDirection.opposite();
		
		int symbolCount = getEnteredSymbolsCount() + 1;
		boolean lock = symbolCount == 8 || (symbolCount == 7 && symbol == EnumSymbol.ORIGIN);
		
		if (moveOnly) {
			if (lock) {
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1f);
				
				addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CHEVRON_SHUT_SOUND));
				addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_CHEVRON_OPEN_SOUND));
			}
			
			else
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCKING, 0.2f);
		}
		
		stargateState = EnumStargateState.COMPUTER_DIALING;
		getServerRingSpinHelper().requestStart(getRendererStateSG1().ringCurrentSymbol.angle, spinDirection, symbol, lock, context, moveOnly);
		ringRollLoopPlayed = false || moveOnly;
		
		sendSignal(context, "stargate_spin_start", new Object[] { symbolCount, lock, targetSymbol.name });
		
		markDirty();
		
		return new Object[] {"stargate_spin"};
	}

	@Callback(doc = "function() -- Tries to open the gate")
	public Object[] engageGate(Context context, Arguments args) {
		EnumGateState gateState = GateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);

		if (gateState == EnumGateState.OK) {
			
			if (isLinked()) {
				getLinkedDHD(world).activateSymbol(EnumSymbol.BRB.id);
			}
		}
		
		else {
			targetSymbol = null;
			targetSymbolDialing = false;
			
			markStargateIdle();
			markDirty();
			
			sendSignal(null, "stargate_failed", new Object[] {});
		}
		
		return new Object[] {gateState.toString()};
	}
	
	@Callback(doc = "function() -- Tries to close the gate")
	public Object[] disengageGate(Context context, Arguments args) {
		if (stargateState.engaged()) {
			if (getStargateState().initiating()) {
				GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
				return new Object[] {};
			}
			
			else
				return new Object[] {null, "stargate_failure_wrong_end", "Unable to close the gate on this end"};
		}
		
		else {
			return new Object[] {null, "stargate_failure_not_open", "The gate is closed", stargateState.toString()};
		}
	}
	
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
}
