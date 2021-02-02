package mrjake.aunis.tileentity.stargate;

import java.util.EnumSet;
import java.util.List;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateSizeEnum;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.renderer.stargate.StargateClassicRendererState.StargateClassicRendererStateBuilder;
import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.renderer.stargate.StargateUniverseRendererState;
import mrjake.aunis.renderer.stargate.StargateUniverseRendererState.StargateUniverseRendererStateBuilder;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateUniverseMergeHelper;
import mrjake.aunis.stargate.network.StargateAddress;
import mrjake.aunis.stargate.network.StargateAddressDynamic;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolInterface;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.network.SymbolUniverseEnum;
import mrjake.aunis.stargate.power.StargateEnergyRequired;
import mrjake.aunis.state.StargateRendererActionState;
import mrjake.aunis.state.StargateRendererActionState.EnumGateAction;
import mrjake.aunis.state.StargateUniverseSymbolState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class StargateUniverseBaseTile extends StargateClassicBaseTile {

	// --------------------------------------------------------------------------------
	// Dialing
	
	private StargateAddress addressToDial;
	private int addressPosition;
	private int maxSymbols;
	private boolean abortDialing;
	
	public void dial(StargateAddress stargateAddress, int glyphsToDial) {
		addressToDial = stargateAddress;
		addressPosition = 0;
		maxSymbols = glyphsToDial;
		abortDialing = false;
		
		stargateState = EnumStargateState.DIALING;
		
		AunisSoundHelper.playSoundEvent(world, getGateCenterPos(), SoundEventEnum.GATE_UNIVERSE_DIAL_START);
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_DIAL_NEXT, 30));
		sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, 9, true);
		updateChevronLight(9, true);
		
		markDirty();
	}
	
	public void abort() {
		abortDialing = true;
		markDirty();
	}
	
	@Override
	public void failGate() {
		super.failGate();
		
		if (targetRingSymbol != SymbolUniverseEnum.TOP_CHEVRON)
			addSymbolToAddressManual(SymbolUniverseEnum.TOP_CHEVRON, null);
	}
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		if (targetRingSymbol != SymbolUniverseEnum.TOP_CHEVRON)
			addSymbolToAddressManual(SymbolUniverseEnum.TOP_CHEVRON, null);
	}
	
	@Override
	public void addSymbolToAddressManual(SymbolInterface targetSymbol, Object context) {		
		if (context != null)
			stargateState = EnumStargateState.DIALING_COMPUTER;
		else
			stargateState = EnumStargateState.DIALING;
		
		if (stargateState.dialingComputer() && dialedAddress.size() == 0) {
			AunisSoundHelper.playSoundEvent(world, getGateCenterPos(), SoundEventEnum.GATE_UNIVERSE_DIAL_START);
			sendRenderingUpdate(EnumGateAction.LIGHT_UP_CHEVRONS, 9, true);
			
			NBTTagCompound taskData = new NBTTagCompound();
			taskData.setInteger("symbolToDial", targetSymbol.getId());
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_DIAL_NEXT, 30, taskData));
			ringSpinContext = context;
		}
			
		else
			super.addSymbolToAddressManual(targetSymbol, context);
	}
	
	@Override
	public void incomingWormhole(int dialedAddressSize) {
		super.incomingWormhole(9);
	}
	
	@Override
	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 80));
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAILED_SOUND, 40));
	}
	
	@Override
	protected int getMaxChevrons() {
		return 9;
	}
	
	@Override
	protected int getOpenSoundDelay() {
		return super.getOpenSoundDelay() + 10;
	}
	
	public static final EnumSet<BiomeOverlayEnum> SUPPORTED_OVERLAYS = EnumSet.of(
			BiomeOverlayEnum.NORMAL,
			BiomeOverlayEnum.FROST,
			BiomeOverlayEnum.MOSSY,
			BiomeOverlayEnum.AGED);
	
	@Override
	public EnumSet<BiomeOverlayEnum> getSupportedOverlays() {
		return SUPPORTED_OVERLAYS;
	}
	
	// --------------------------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_DIAL_NEXT:
				if (customData != null && customData.hasKey("symbolToDial"))
					super.addSymbolToAddressManual(getSymbolType().valueOfSymbol(customData.getInteger("symbolToDial")), ringSpinContext);
				else
					addSymbolToAddressManual(addressPosition >= maxSymbols ? getSymbolType().getOrigin() : addressToDial.get(addressPosition), null);
								
				addressPosition++;
				
				break;
				
			case STARGATE_SPIN_FINISHED:
				if (targetRingSymbol != SymbolUniverseEnum.TOP_CHEVRON) {
					if (canAddSymbol(targetRingSymbol) && !abortDialing) {
						addSymbolToAddress(targetRingSymbol);
						activateSymbolServer(targetRingSymbol);
						
						if (stargateState.dialingComputer()) {
							stargateState = EnumStargateState.IDLE;
							addTask(new ScheduledTask(EnumScheduledTask.STARGATE_DIAL_FINISHED, 15));
						}
						
						else {
							
							if (!stargateWillLock(targetRingSymbol))
								addTask(new ScheduledTask(EnumScheduledTask.STARGATE_DIAL_NEXT, 30));
							else {
								attemptOpenAndFail();
							}
						}
					}
					
					else {
						dialingFailed(abortDialing ? StargateOpenResult.ABORTED : StargateOpenResult.ADDRESS_MALFORMED);
						
						stargateState = EnumStargateState.IDLE;
						abortDialing = false;
					}
				}
				
				else
					stargateState = EnumStargateState.IDLE;
				
				markDirty();
				break;
			
			case STARGATE_FAILED_SOUND:
				playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
				break;
				
			case STARGATE_DIAL_FINISHED:
				sendSignal(ringSpinContext, "stargate_spin_chevron_engaged", new Object[] { dialedAddress.size(), stargateWillLock(targetRingSymbol), targetRingSymbol.getEnglishName() });
				break;
				
			default:
				break;
		}
		
		super.executeTask(scheduledTask, customData);
	}
	
	private void activateSymbolServer(SymbolInterface symbol) {
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_UNIVERSE_ACTIVATE_SYMBOL, new StargateUniverseSymbolState((SymbolUniverseEnum) symbol, false)), targetPoint);
	}
	
	// --------------------------------------------------------------------------------
	// States
	
	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case STARGATE_UNIVERSE_ACTIVATE_SYMBOL:
				return new StargateUniverseSymbolState();
			
			default:
				return super.createState(stateType);
		}
	}
	
	@Override
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case STARGATE_UNIVERSE_ACTIVATE_SYMBOL:
				StargateUniverseSymbolState symbolState = (StargateUniverseSymbolState) state;
				
				if (symbolState.dimAll)
					getRendererStateClient().clearSymbols(world.getTotalWorldTime());
				else
					getRendererStateClient().activateSymbol(world.getTotalWorldTime(), symbolState.symbol);
				
				break;
				
			case RENDERER_UPDATE:
				StargateRendererActionState gateActionState = (StargateRendererActionState) state;

				switch (gateActionState.action) {
					case CLEAR_CHEVRONS:
						getRendererStateClient().clearSymbols(world.getTotalWorldTime());
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
	
	// --------------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("addressPosition", addressPosition);
		
		if (addressToDial != null)
			compound.setTag("addressToDial", addressToDial.serializeNBT());
		
		compound.setInteger("maxSymbols", maxSymbols);
		compound.setBoolean("abortDialing", abortDialing);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		addressPosition = compound.getInteger("addressPosition");
		addressToDial = new StargateAddress(compound.getCompoundTag("addressToDial"));
		maxSymbols = compound.getInteger("maxSymbols");
		abortDialing = compound.getBoolean("abortDialing");
	}
	
	
	// --------------------------------------------------------------------------------
	// Stargate Network
	
	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.UNIVERSE;
	}
	
	@Override
	protected StargateEnergyRequired getEnergyRequiredToDial(StargatePos targetGatePos) {
		return super.getEnergyRequiredToDial(targetGatePos).mul(AunisConfig.powerConfig.stargateUniverseEnergyMul);
	}
	
	
	// --------------------------------------------------------------------------------
	// Teleportation
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return StargateSizeEnum.SMALL.teleportBox;
	}
	
	@Override
	public BlockPos getGateCenterPos() {
		return pos.up(4);
	}

	@Override
	protected AunisAxisAlignedBB getHorizonKillingBox(boolean server) {
		return StargateSizeEnum.SMALL.killingBox;
	}

	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return StargateSizeEnum.SMALL.horizonSegmentCount;
	}

	@Override
	protected List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {
		return StargateSizeEnum.SMALL.gateVaporizingBoxes;
	}
	
	
	// --------------------------------------------------------------------------------
	// Sounds

	@Override
	protected SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum) {
		switch (soundEnum) {
			case GATE_RING_ROLL: return SoundPositionedEnum.UNIVERSE_RING_ROLL;
		}
		
		return null;
	}

	@Override
	protected SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum) {
		switch (soundEnum) {
			case OPEN: return SoundEventEnum.GATE_UNIVERSE_OPEN;
			case CLOSE: return SoundEventEnum.GATE_UNIVERSE_CLOSE;
			case DIAL_FAILED: return SoundEventEnum.GATE_UNIVERSE_DIAL_FAILED;
			case INCOMING: return SoundEventEnum.GATE_UNIVERSE_DIAL_START;
			case CHEVRON_OPEN: return SoundEventEnum.GATE_MILKYWAY_CHEVRON_OPEN;
			case CHEVRON_SHUT: return targetRingSymbol == SymbolUniverseEnum.TOP_CHEVRON ? SoundEventEnum.GATE_UNIVERSE_CHEVRON_TOP_LOCK : SoundEventEnum.GATE_UNIVERSE_CHEVRON_LOCK;
		}
		
		return null;
	}

	
	// --------------------------------------------------------------------------------
	// Merging

	@Override
	public StargateAbstractMergeHelper getMergeHelper() {
		return StargateUniverseMergeHelper.INSTANCE;
	}

	
	// --------------------------------------------------------------------------------
	// Renderer states
	
	@Override
	protected StargateClassicRendererStateBuilder getRendererStateServer() {
		return new StargateUniverseRendererStateBuilder(super.getRendererStateServer())
				.setDialedAddress((stargateState.initiating() || stargateState.dialing()) ? dialedAddress : new StargateAddressDynamic(getSymbolType()))
				.setActiveChevrons(stargateState.idle() ? 0 : 9);
	}
	
	@Override
	protected StargateUniverseRendererState createRendererStateClient() {
		return new StargateUniverseRendererState();
	}
	
	@Override
	public StargateUniverseRendererState getRendererStateClient() {
		return (StargateUniverseRendererState) super.getRendererStateClient();
	}
}
