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
import mrjake.aunis.gui.StargateMilkyWayGui;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumGateState;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.state.StargateAbstractRendererState;
import mrjake.aunis.state.StargateMilkyWayGuiState;
import mrjake.aunis.state.StargateMilkyWayRendererState;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.state.UniversalEnergyState;
import mrjake.aunis.state.UpgradeRendererState;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.upgrade.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.StargateUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StargateMilkyWayBaseTile extends StargateAbstractBaseTile implements ITileEntityUpgradeable, ILinkable {
		
	private StargateUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedDHD = null;	
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
		
		if (isLinked())
			getLinkedDHD(world).clearSymbols();
	}
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return getStargateSize(server).teleportBox;
	}
	
	@Override
	protected int getMaxChevrons(boolean computer, DHDTile dhdTile) {		
		if (computer)
			return 8;
		else
			return (dhdTile.hasUpgrade() ? 8 : 7);
	}
	
	@Override
	public void incomingWormhole(List<EnumSymbol> incomingAddress, int dialedAddressSize) {
		super.incomingWormhole(incomingAddress, dialedAddressSize);
		
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, dialedAddressSize, true);
	}
	
	@Override
	public void openGate(boolean initiating, List<EnumSymbol> incomingAddress, boolean eightChevronDial) {
		super.openGate(initiating, incomingAddress, eightChevronDial);
		
		if (isLinked()) {
			getLinkedDHD(world).getDHDRendererState().activeButtons.add(EnumSymbol.BRB.id);
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
		BlockPos closestDhd = LinkingHelper.findClosestUnlinked(world, pos, LinkingHelper.getDhdRange(), AunisBlocks.dhdBlock);
		
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
	
	@Override
	public UpgradeRenderer getUpgradeRenderer() {
		if (upgradeRenderer == null)
			upgradeRenderer = new StargateUpgradeRenderer(world, 0);
		
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
		
		compound.setBoolean("hasUpgrade", hasUpgrade);
		
		compound.setTag("upgradeRendererState", getUpgradeRendererState().serializeNBT());
		
		compound.setBoolean("targetSymbolDialing", targetSymbolDialing);
		if (targetSymbol != null)
			compound.setInteger("targetSymbol", targetSymbol.id);
		
		if (spinDirection != null)
			compound.setInteger("lastSpinDirection", spinDirection.id);
		
		compound.setInteger("stargateSize", stargateSize.id);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("linkedDHD"))
			this.linkedDHD = BlockPos.fromLong( compound.getLong("linkedDHD") );
		
		hasUpgrade = compound.getBoolean("hasUpgrade");
			
		try {
			getUpgradeRendererState().deserializeNBT(compound.getCompoundTag("upgradeRendererState"));
		}
		
		catch (NullPointerException | IndexOutOfBoundsException | ClassCastException e) {
			Aunis.info("Exception at reading RendererState");
			Aunis.info("If loading world used with previous version and nothing game-breaking doesn't happen, please ignore it");

			e.printStackTrace();
		}
		
		targetSymbolDialing = compound.getBoolean("targetSymbolDialing");

		if (compound.hasKey("targetSymbol"))
			targetSymbol = EnumSymbol.valueOf(compound.getInteger("targetSymbol"));
		else
			targetSymbol = null;
		
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("lastSpinDirection"));
		
		if (compound.hasKey("patternVersion"))
			stargateSize = StargateSizeEnum.SMALL;
		else {
			if (compound.hasKey("stargateSize"))
				stargateSize = StargateSizeEnum.fromId(compound.getInteger("stargateSize"));
			else
				stargateSize = StargateSizeEnum.LARGE;
		}
		
		super.readFromNBT(compound);
	}
	
	@Override
	public void prepare() {
		super.prepare();
		setLinkedDHD(null);
	}
	
	// ------------------------------------------------------------------------
	// Ticking and loading
		
	@Override
	public void onLoad() {		
		if (world.isRemote) {
			AunisPacketHandler.INSTANCE.sendToServer(new StateUpdateRequestToServer(pos, Aunis.proxy.getPlayerClientSide(), StateTypeEnum.UPGRADE_RENDERER_STATE));
		}
		
		super.onLoad();
	}
	
	@Override
	protected BlockPos getLightBlockPos() {
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
					updateMergeState(StargateMilkyWayMergeHelper.INSTANCE.checkBlocks(world, pos, facing), null);
					
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
		return getStargateSize(server).killingBox;
	}
	
	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return getStargateSize(server).horizonSegmentCount;
	}
	
	@Override
	protected  List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {
		return getStargateSize(server).gateVaporizingBoxes;
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	@Override
	protected void updateChevronLight() {
		for (int i=0; i<9; i++) {
			BlockPos chevPos = StargateMilkyWayMergeHelper.INSTANCE.getChevronBlocks().get(i);
			
			if (StargateMilkyWayMergeHelper.MEMBER_MATCHER.apply(world.getBlockState(chevPos))) {
				StargateMilkyWayMemberTile memberTile = (StargateMilkyWayMemberTile) world.getTileEntity(chevPos.rotate(FacingToRotation.get(facing)).add(pos));
				
				memberTile.setLitUp(dialedAddress.size() > i);
			}
		}
	}
	
	private StargateSizeEnum stargateSize = AunisConfig.stargateSize;
	
	private StargateSizeEnum getStargateSize(boolean server) {
		return server ? stargateSize : getRendererStateClient().stargateSize;
	}
		
	@Override
	protected StargateAbstractRendererState getRendererStateServer() {
		return new StargateMilkyWayRendererState(stargateSize, stargateState, dialedAddress.size(), isFinalActive, EnumSymbol.ORIGIN);
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
	public State getState(StateTypeEnum stateType) {
		switch (stateType) {		
			case UPGRADE_RENDERER_STATE:
				return getUpgradeRendererState();
				
			case GUI_STATE:
				return new StargateMilkyWayGuiState(gateAddress, hasUpgrade, energyStorage.getMaxEnergyStored(), new UniversalEnergyState(energyStorage.getEnergyStored()));
				
			case ENERGY_STATE:
				return new UniversalEnergyState(energyStorage.getEnergyStored());
				
			default:
				return super.getState(stateType);
		}
	}
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case UPGRADE_RENDERER_STATE:
				return new UpgradeRendererState();
		
			case GUI_STATE:
				return new StargateMilkyWayGuiState();
				
			case ENERGY_STATE:
				return new UniversalEnergyState();
				
			default:
				return super.createState(stateType);
		}
	}
	
	private StargateMilkyWayGui stargateGui;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {		
		switch (stateType) {		
			case UPGRADE_RENDERER_STATE:
				getUpgradeRenderer().setState((UpgradeRendererState) state);
				
				break;
		
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;
				
				switch (gateActionState.action) {
					case ACTIVATE_CHEVRON:
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
						
					default:
						super.setState(stateType, gateActionState);
						break;
				}
				
				break;
		
			case GUI_STATE:
				if (stargateGui == null || !stargateGui.isOpen) {
					stargateGui = new StargateMilkyWayGui(pos, (StargateMilkyWayGuiState) state);
					Minecraft.getMinecraft().displayGuiScreen(stargateGui);
				}
				
				else {
					stargateGui.state = (StargateMilkyWayGuiState) state;
				}
				
				break;
				
			case ENERGY_STATE:
				if (stargateGui != null && stargateGui.isOpen) {
					stargateGui.state.energyState = (UniversalEnergyState) state;
				}
				
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
			case STARGATE_ACTIVATE_CHEVRON:
				stargateState = EnumStargateState.IDLE;
				markDirty();
				
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_OPEN, 1.0f);				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(EnumGateAction.ACTIVATE_CHEVRON, -1, customData.getBoolean("final"))), targetPoint);
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
				
			default:
				super.executeTask(scheduledTask, customData);
		}
	}
	
	// -----------------------------------------------------------------
	// Ring rotation	
	protected EnumSymbol targetSymbol = EnumSymbol.ORIGIN;
	protected boolean targetSymbolDialing = false;
	protected EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	
	public void markStargateIdle() {
		stargateState = EnumStargateState.IDLE;
		markDirty();
	}

	public void setEndingSymbol(EnumSymbol symbol) {
//		rendererState.ringCurrentSymbol = symbol;
		
		markDirty();
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
		
		if (gateAddress.size() == 8) {
			return new Object[] {null, "stargate_failure_full", "Already dialed 8 chevrons"};
		}
		
//		EnumSymbol symbol = null;
//		
//		if (args.isInteger(0))
//			symbol = EnumSymbol.valueOf(args.checkInteger(0));
//		else if (args.isString(0))
//			symbol = EnumSymbol.forEnglishName(args.checkString(0));
//		
//		if (symbol == null)
//			throw new IllegalArgumentException("bad argument #1 (symbol name/index invalid)");
//		
//		boolean moveOnly = symbol == targetSymbol;
//		
//		if (dialedAddress.contains(symbol)) {
//			return new Object[] {null, "stargate_failure_contains", "Dialed address contains this symbol already"};
//		}
//		
//		this.targetSymbol = symbol;	
//		this.targetSymbolDialing = true;
//		
//		spinDirection = spinDirection.opposite();
//		
//		double distance = spinDirection.getDistance(EnumSymbol.ORIGIN.angle, symbol.angle);
////			Aunis.info("position: " + getStargateRendererState().ringCurrentSymbol.angle + ", target: " + targetSymbol + ", direction: " + spinDirection + ", distance: " + distance + ", moveOnly: " + moveOnly);
//		
//		if (distance < (StargateRingSpinHelper.getStopAngleTraveled() + 5))
//			spinDirection = spinDirection.opposite();
//		
//		int symbolCount = getEnteredSymbolsCount() + 1;
//		boolean lock = symbolCount == 8 || (symbolCount == 7 && symbol == EnumSymbol.ORIGIN);
//		
//		if (moveOnly) {
//			if (lock) {
//				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1f);
//				
//				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_SHUT_SOUND));
//				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SOUND));
//			}
//			
//			else
//				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCKING, 0.2f);
//		}
//		
//		stargateState = EnumStargateState.DIALING;
//		getServerRingSpinHelper().requestStart(EnumSymbol.ORIGIN.angle, spinDirection, symbol, lock, context, moveOnly);
//		ringRollLoopPlayed = false || moveOnly;
		
//		sendSignal(context, "stargate_spin_start", new Object[] { symbolCount, lock, targetSymbol.englishName });
		
		markDirty();
		
		return new Object[] {"stargate_spin"};
	}

	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function() -- Tries to open the gate")
	public Object[] engageGate(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null, "stargate_failure_not_merged", "Stargate is not merged" };
		
		if (stargateState.idle()) {
			EnumGateState gateState = StargateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);
	
			if (gateState == EnumGateState.OK) {
				if (isLinked()) {
					getLinkedDHD(world).activateSymbol(EnumSymbol.BRB.id);
				}
				
				return new Object[] {"stargate_engage"};
			}
			
			else {
				targetSymbol = null;
				targetSymbolDialing = false;
				
				markStargateIdle();
				markDirty();
				
				sendSignal(null, "stargate_failed", new Object[] {});
				return new Object[] {null, "stargate_failure_opening", "Stargate failed to open", gateState.toString()};
			}
		}
		
		else {
			return new Object[] {null, "stargate_failure_busy", "Stargate is busy", "BUSY"};
		}
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback(doc = "function() -- Tries to close the gate")
	public Object[] disengageGate(Context context, Arguments args) {
		if (!isMerged())
			return new Object[] { null, "stargate_failure_not_merged", "Stargate is not merged" };
		
		if (stargateState.engaged()) {
			if (getStargateState().initiating()) {
				StargateRenderingUpdatePacketToServer.closeGatePacket(this, false);
				return new Object[] { "stargate_disengage" };
			}
			
			else
				return new Object[] {null, "stargate_failure_wrong_end", "Unable to close the gate on this end"};
		}
		
		else {
			return new Object[] {null, "stargate_failure_not_open", "The gate is closed"};
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
