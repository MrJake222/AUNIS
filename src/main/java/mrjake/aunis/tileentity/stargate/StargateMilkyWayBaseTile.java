package mrjake.aunis.tileentity.stargate;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.gui.container.StargateContainerGuiState;
import mrjake.aunis.item.AunisItems;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.StateUpdateRequestToServer;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState;
import mrjake.aunis.sound.AunisPositionedSoundEnum;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumGateState;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.stargate.StargateSpinHelper;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.StargateSpinState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.state.UniversalEnergyState;
import mrjake.aunis.state.UpgradeRendererState;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.DHDTile.DHDUpgradeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.upgrade.ITileEntityUpgradeable;
import mrjake.aunis.upgrade.StargateUpgradeRenderer;
import mrjake.aunis.upgrade.UpgradeRenderer;
import mrjake.aunis.util.AunisAxisAlignedBB;
import mrjake.aunis.util.FacingToRotation;
import mrjake.aunis.util.ILinkable;
import mrjake.aunis.util.LinkingHelper;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class StargateMilkyWayBaseTile extends StargateAbstractBaseTile implements ITileEntityUpgradeable, ILinkable {
		
	private StargateUpgradeRenderer upgradeRenderer;
	private UpgradeRendererState upgradeRendererState;
	
	private BlockPos linkedDHD = null;	
	
	// ------------------------------------------------------------------------
	// Stargate state
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		updateChevronLight();
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
		
		if (isLinked())
			getLinkedDHD(world).clearSymbols();
	}
	
	@Override
	protected void failGate() {
		super.failGate();
		
		updateChevronLight();
		sendRenderingUpdate(EnumGateAction.CLEAR_CHEVRONS, dialedAddress.size(), isFinalActive);
		
		if (isLinked())
			getLinkedDHD(world).clearSymbols();
	}
	
	@Override
	public void onBlockBroken() {
		super.onBlockBroken();
		
		playPositionedSound(AunisPositionedSoundEnum.RING_ROLL, false);
		
		if (hasUpgrade() || getUpgradeRendererState().doInsertAnimation) {
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(AunisItems.crystalGlyphStargate));
		}
	}
	
	// ------------------------------------------------------------------------
	// Stargate Network
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return getStargateSizeConfig(server).teleportBox;
	}
	
	public void addSymbolToAddressDHD(EnumSymbol symbol) {		
		addSymbolToAddress(symbol);
		stargateState = EnumStargateState.DIALING;
		
		int maxChevrons = getLinkedDHD(world).isUpgradeInstalled(DHDUpgradeEnum.CHEVRON_UPGRADE) ? 8 : 7;
		NBTTagCompound taskData = new NBTTagCompound();
		
		if (dialedAddress.size() == maxChevrons || (dialedAddress.size() == 7 && symbol == EnumSymbol.ORIGIN)) {
			isFinalActive = true;
			taskData.setBoolean("final", true);
		}
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ACTIVATE_CHEVRON, 10, taskData));
		
		markDirty();
	}
	
	@Override
	public boolean canAddSymbol(EnumSymbol symbol) {
		if (dialedAddress.contains(symbol)) 
			return false;
		
		int maxSymbols = getLinkedDHD(world).isUpgradeInstalled(DHDUpgradeEnum.CHEVRON_UPGRADE) ? 8 : 7;
		
		if (dialedAddress.size() == maxSymbols)
			return false;
		
		return true;
	}
	
	@Override
	public void addSymbolToAddress(EnumSymbol symbol) {
		super.addSymbolToAddress(symbol);
		
		updateChevronLight();
		
		if (isLinked()) {
			getLinkedDHD(world).activateSymbol(symbol);
		}
	}
	
	@Override
	public void incomingWormhole(List<EnumSymbol> incomingAddress, int dialedAddressSize) {
		super.incomingWormhole(incomingAddress, dialedAddressSize);
		
		AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_INCOMING, 0.5f);
		
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, dialedAddressSize, true);
		
		if (isLinked()) {
			getLinkedDHD(world).clearSymbols();
		}
	}
	
	@Override
	public void openGate(boolean initiating, List<EnumSymbol> incomingAddress, boolean eightChevronDial) {
		super.openGate(initiating, incomingAddress, eightChevronDial);
	}
	
	@Override
	public void closeGate() {
		super.closeGate();
	}
	
	@Override
	public void dialingFailed() {
		super.dialingFailed();
		
		AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_DIAL_FAILED, 0.3f);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 53));
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
		
		compound.setInteger("stargateSize", stargateSize.id);

		compound.setBoolean("isSpinning", isSpinning);
		compound.setBoolean("locking", locking);
		compound.setLong("spinStartTime", spinStartTime);
		compound.setInteger("currentRingSymbol", currentRingSymbol.id);
		compound.setInteger("targetRingSymbol", targetRingSymbol.id);
		compound.setInteger("spinDirection", spinDirection.id);
		
		compound.setTag("itemHandler", itemStackHandler.serializeNBT());
		
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
		currentRingSymbol = EnumSymbol.valueOf(compound.getInteger("currentRingSymbol"));
		targetRingSymbol = EnumSymbol.valueOf(compound.getInteger("targetRingSymbol"));
		spinDirection = EnumSpinDirection.valueOf(compound.getInteger("spinDirection"));
	
		itemStackHandler.deserializeNBT(compound.getCompoundTag("itemHandler"));
		
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
	private EnumSymbol currentRingSymbol = EnumSymbol.ORIGIN;
	private EnumSymbol targetRingSymbol = EnumSymbol.ORIGIN;
	private EnumSpinDirection spinDirection = EnumSpinDirection.COUNTER_CLOCKWISE;
	private Object ringSpinContext;
	
	public void addSymbolToAddressManual(EnumSymbol targetSymbol, @Nullable Object context) {
		targetRingSymbol = targetSymbol;
		
		boolean moveOnly = targetRingSymbol == currentRingSymbol;
		locking = (dialedAddress.size() == 7) || (dialedAddress.size() == 6 && targetRingSymbol == EnumSymbol.ORIGIN);
		
		if (moveOnly) {
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, 0));
		}
		
		else {
			float distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			
			if (distance < StargateSpinHelper.getMinimalDistance() || distance > 180) {
				spinDirection = spinDirection.opposite();
				distance = spinDirection.getDistance(currentRingSymbol, targetRingSymbol);
			}
						
			// Aunis.info("position: " + currentRingSymbol + ", target: " + targetSymbol + ", direction: " + spinDirection + ", distance: " + distance + ", animEnd: " + StargateSpinHelper.getAnimationDuration(distance) + ", moveOnly: " + moveOnly + ", locking: " + locking);
			
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.SPIN_STATE, new StargateSpinState(targetRingSymbol, spinDirection)), targetPoint);
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_SPIN_FINISHED, StargateSpinHelper.getAnimationDuration(distance) - 5));
			playPositionedSound(AunisPositionedSoundEnum.RING_ROLL, true);
			
			isSpinning = true;
			spinStartTime = world.getTotalWorldTime();
			stargateState = EnumStargateState.DIALING;
			
			ringSpinContext = context;
			if (context != null)
				sendSignal(context, "stargate_spin_start", new Object[] { dialedAddress.size(), locking, targetSymbol.englishName });
		}
			
		markDirty();
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
		Aunis.info("Stargate size: " + getStargateSizeConfig(server));
		
		return getStargateSizeConfig(server).gateVaporizingBoxes;
	}
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
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
	protected StargateAbstractRendererState getRendererStateServer() {
		return new StargateMilkyWayRendererState(stargateSize, stargateState, dialedAddress.size(), isFinalActive, currentRingSymbol, spinDirection, isSpinning, targetRingSymbol, spinStartTime);
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
								
			case ENERGY_STATE:
				return new UniversalEnergyState(energyStorage.getEnergyStored(), energyTransferedLastTick);
				
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
				return new StargateContainerGuiState();
				
			case ENERGY_STATE:
				return new UniversalEnergyState();
				
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
			case UPGRADE_RENDERER_STATE:
				getUpgradeRenderer().setState((UpgradeRendererState) state);
				
				break;
		
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
						
					case CHEVRON_OPEN:
						getRendererStateClient().openChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_ACTIVATE_BOTH:
						getRendererStateClient().chevronTextureList.activateNextChevron(world.getTotalWorldTime());
						getRendererStateClient().chevronTextureList.activateFinalChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_CLOSE:
						getRendererStateClient().closeChevron(world.getTotalWorldTime());
						break;
						
					case CHEVRON_DIM:
						getRendererStateClient().chevronTextureList.deactivateFinalChevron(world.getTotalWorldTime());
						break;
						
					default:
						super.setState(stateType, gateActionState);
						break;
				}
				
				break;
		
			case GUI_STATE:
				StargateContainerGuiState guiState = (StargateContainerGuiState) state;
				
				gateAddress = guiState.getGateAddress();
				hasUpgrade = guiState.hasUpgrade();
				
				Aunis.info(this + ": Setting address to " + gateAddress);
				
				break;
				
//			case GUI_STATE:
//				if (stargateGui == null || !stargateGui.isOpen) {
//					stargateGui = new StargateMilkyWayGui(pos, (StargateMilkyWayGuiState) state);
//					Minecraft.getMinecraft().displayGuiScreen(stargateGui);
//				}
//				
//				else {
//					stargateGui.state = (StargateMilkyWayGuiState) state;
//				}
//				
//				break;
//				
//			case ENERGY_STATE:
//				if (stargateGui != null && stargateGui.isOpen) {
//					stargateGui.state.energy = ((UniversalEnergyState) state).energy;
//				}
//				
//				break;
				
			case SPIN_STATE:
				StargateSpinState spinState = (StargateSpinState) state;
				getRendererStateClient().spinHelper.initRotation(world.getTotalWorldTime(), spinState.targetSymbol, spinState.direction);
				
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
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.RENDERER_UPDATE, new StargateRendererActionState(EnumGateAction.CHEVRON_ACTIVATE, -1, customData.getBoolean("final"))), targetPoint);
				break;
				
			case STARGATE_SPIN_FINISHED:
				playPositionedSound(AunisPositionedSoundEnum.RING_ROLL, false);
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1.0f);
				
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN, 11));
				
				isSpinning = false;
				currentRingSymbol = targetRingSymbol;
				
				markDirty();
				break;
				
			case STARGATE_CHEVRON_OPEN:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_OPEN, 1.0f);
				sendRenderingUpdate(EnumGateAction.CHEVRON_OPEN, 0, false);
				
				if (canAddSymbol(targetRingSymbol)) {
					super.addSymbolToAddress(targetRingSymbol);
					
					if (locking) {
						if (StargateRenderingUpdatePacketToServer.checkDialedAddress(world, this)) {
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
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_OPEN, 1.0f);
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_LIGHT_UP, 4));
				
				break;
				
			case STARGATE_CHEVRON_LIGHT_UP:
				if (locking)
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE, 0, true);
				else
					sendRenderingUpdate(EnumGateAction.CHEVRON_ACTIVATE_BOTH, 0, false);
				
				updateChevronLight();
				
				if (locking)
					StargateRenderingUpdatePacketToServer.attemptLightUp(world, this);
				
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_CLOSE, 14));

				break;
				
			case STARGATE_CHEVRON_CLOSE:
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1.0f);
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);
				sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), locking, targetRingSymbol.englishName });				

				if (locking)
					stargateState = EnumStargateState.IDLE;
				else
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_DIM, 6));
				
				break;
				
			case STARGATE_MANUAL_OPEN:
				StargateRenderingUpdatePacketToServer.attemptOpen(world, this, getLinkedDHD(world), false);
				
				break;
				
			case STARGATE_CHEVRON_DIM:
				sendRenderingUpdate(EnumGateAction.CHEVRON_DIM, 0, false);
				stargateState = EnumStargateState.IDLE;
				
				break;
				
			case STARGATE_CHEVRON_FAIL:
				sendRenderingUpdate(EnumGateAction.CHEVRON_CLOSE, 0, false);
				AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_DIAL_FAILED_COMPUTER, 1.50f);
				
				dialingFailed();
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CLOSE, 10));
								
				break;
				
			default:
				super.executeTask(scheduledTask, customData);
		}
	}
	
	
	// -----------------------------------------------------------------------------
	// Item handler
	
	public static final List<Item> SUPPORTED_UPGRADES = Arrays.asList(
			AunisItems.crystalGlyphStargate);
	
	private ItemStackHandler itemStackHandler = new ItemStackHandler(10) {
		
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			Item item = stack.getItem();
			
			switch (slot) {
				case 0:				
				case 1:
				case 2:
				case 3:
					return SUPPORTED_UPGRADES.contains(item);
					
				case 4:
				case 5:
				case 6:
					return true;
					
				case 7:
				case 8:
				case 9:
					return item == AunisItems.pageNotebookItem;
					
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
			
			markDirty();
		}
	};
	
	public static enum StargateUpgradeEnum {
		CHEVRON_UPGRADE(AunisItems.crystalGlyphStargate);
		
		public Item item;

		private StargateUpgradeEnum(Item item) {
			this.item = item;
			
		}
	}
	
	public boolean isUpgradeInstalled(DHDUpgradeEnum upgrade) {
		for (int slot=0; slot<4; slot++) {
			if (itemStackHandler.getStackInSlot(slot).getItem() == upgrade.item)
				return true;
		}
		
		return false;
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
	// Power system
	
	@Override
	protected IEnergyStorage getEnergyStorage(int minEnergy) {
		if (isLinked()) {
			ItemStackHandler dhdItemStackHandler = (ItemStackHandler) getLinkedDHD(world).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			ItemStack crystalItemStack = dhdItemStackHandler.getStackInSlot(0);
					
			if (!crystalItemStack.isEmpty()) {
				IEnergyStorage crystalEnergyStorage = crystalItemStack.getCapability(CapabilityEnergy.ENERGY, null);
			
				if (crystalEnergyStorage.getEnergyStored() >= minEnergy)
					return crystalEnergyStorage;
			}
		}
		
		return super.getEnergyStorage(minEnergy);
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
		
		EnumSymbol targetSymbol = null;
		
		if (args.isInteger(0))
			targetSymbol = EnumSymbol.valueOf(args.checkInteger(0));
		else if (args.isString(0))
			targetSymbol = EnumSymbol.forEnglishName(args.checkString(0));
		
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
			EnumGateState gateState = StargateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);
	
			if (gateState == EnumGateState.OK) {
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
