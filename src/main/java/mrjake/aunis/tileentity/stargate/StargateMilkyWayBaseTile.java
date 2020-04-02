package mrjake.aunis.tileentity.stargate;

import java.util.List;

import javax.annotation.Nullable;

import mrjake.aunis.block.AunisBlocks;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState;
import mrjake.aunis.renderer.stargate.StargateMilkyWayRendererState.StargateMilkyWayRendererStateBuilder;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.StargateMilkyWayMergeHelper;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
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
	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, stargateState.dialingComputer() ? 83 : 53));
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
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
		updateChevronLight(dialedAddress.size());

		if (isLinked()) {
			getLinkedDHD(world).activateSymbol((SymbolMilkyWayEnum) symbol);
		}
	}
	
	@Override
	public void addSymbolToAddressManual(SymbolInterface targetSymbol, Object context) {
		super.addSymbolToAddressManual(targetSymbol, context);
		
		stargateState = EnumStargateState.DIALING_COMPUTER;
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
	public StargateAbstractMergeHelper getMergeHelper() {
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
		
		super.readFromNBT(compound);
	}
	
	@Override
	public void prepare() {
		super.prepare();
		setLinkedDHD(null);
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
				.setStargateSize(stargateSize);
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
						break;
				}
				
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
			case STARGATE_SPIN_FINISHED:				
				addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN, 11));
				
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
				
//				updateChevronLight(); // TODO Check light update OC
				
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
				break;
		}
		
		super.executeTask(scheduledTask, customData);
	}
}
