package mrjake.aunis.tileentity.stargate;

import java.util.Arrays;
import java.util.List;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.AunisProps;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.config.StargateDimensionConfig;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.renderer.stargate.StargateAbstractRendererState;
import mrjake.aunis.renderer.stargate.StargateOrlinRendererState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.sound.SoundPositionedEnum;
import mrjake.aunis.sound.StargateSoundEventEnum;
import mrjake.aunis.sound.StargateSoundPositionedEnum;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.StargateClosedReasonEnum;
import mrjake.aunis.stargate.StargateOpenResult;
import mrjake.aunis.stargate.merging.StargateAbstractMergeHelper;
import mrjake.aunis.stargate.merging.StargateOrlinMergeHelper;
import mrjake.aunis.stargate.network.StargateNetwork;
import mrjake.aunis.stargate.network.StargatePos;
import mrjake.aunis.stargate.network.SymbolMilkyWayEnum;
import mrjake.aunis.stargate.network.SymbolTypeEnum;
import mrjake.aunis.stargate.power.StargateAbstractEnergyStorage;
import mrjake.aunis.stargate.power.StargateEnergyRequired;
import mrjake.aunis.state.StargateOrlinSparkState;
import mrjake.aunis.state.State;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.tileentity.util.ScheduledTask;
import mrjake.aunis.util.AunisAxisAlignedBB;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StargateOrlinBaseTile extends StargateAbstractBaseTile {
		
	// ------------------------------------------------------------------------
	// Stargate state
	
	private int openCount = 0;
	
	public boolean isBroken() {
		return openCount == AunisConfig.stargateConfig.stargateOrlinMaxOpenCount;
	}
	
	@Override
	public SymbolTypeEnum getSymbolType() {
		return SymbolTypeEnum.MILKYWAY;
	}
	
	@Override
	public void dialingFailed(StargateOpenResult result) {
		super.dialingFailed(result);
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAILED_SOUND, 30));
	}
	
	@Override
	protected void addFailedTaskAndPlaySound() {
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAIL, 83));
		playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
	}
	
	@Override
	public void openGate(StargatePos targetGatePos, boolean isInitiating) {
		super.openGate(targetGatePos, isInitiating);
		
		if (world.provider.getDimensionType() == DimensionType.OVERWORLD)
			StargateNetwork.get(world).setLastActivatedOrlins(gateAddressMap.get(SymbolTypeEnum.MILKYWAY));
	}
	
	@Override
	protected void disconnectGate() {
		super.disconnectGate();
		
		openCount++;
		
		if (isBroken()) {
			StargateOrlinMergeHelper.INSTANCE.updateMembersBrokenStatus(world, pos, facing, true);
		}
		
		if (isBroken())
			addTask(new ScheduledTask(EnumScheduledTask.STARGATE_FAILED_SOUND, 5));
	}
	
	@Override
	public boolean canAcceptConnectionFrom(StargatePos targetGatePos) {
		return super.canAcceptConnectionFrom(targetGatePos) && targetGatePos.dimensionID == DimensionType.NETHER.getId() && !isBroken();
	}
	
	public void updateNetherAddress() {
		dialedAddress.clear();
		dialedAddress.addAll(network.getNetherGate().subList(0, StargateDimensionConfig.netherOverworld8thSymbol() ? 7 : 6));
		dialedAddress.addSymbol(SymbolMilkyWayEnum.ORIGIN);
		
		Aunis.info("Orlin's dialed address: " + dialedAddress);
	}
	
	public StargateEnergyRequired getEnergyRequiredToDial() {
		return getEnergyRequiredToDial(network.getStargate(dialedAddress));
	}
	
	public int getEnergyStored() {
		return getEnergyStorage().getEnergyStored();
	}
	
	// ------------------------------------------------------------------------
	// Ticking
	
	@Override
	public BlockPos getGateCenterPos() {
		return pos.offset(EnumFacing.UP, 1);
	}
	
	public long animStart;
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			if (!world.getBlockState(pos).getValue(AunisProps.RENDER_BLOCK) && rendererStateClient != null)
				Aunis.proxy.orlinRendererSpawnParticles(world, getRendererStateClient());
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Redstone
	
	private boolean isPowered;
	
	public void redstonePowerUpdate(boolean power) {
		if (!isMerged())
			return;
		
		if ((isPowered && !power) || (!isPowered && power)) {
			isPowered = power;
						
			if (isPowered && stargateState.idle() && !isBroken()) {
				switch (checkAddressAndEnergy(dialedAddress)) {
					case OK:
						stargateState = EnumStargateState.DIALING;
						
						startSparks();
						AunisSoundHelper.playSoundEvent(world, getGateCenterPos(), SoundEventEnum.GATE_ORLIN_DIAL);
						
						addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ORLIN_OPEN));
						break;
						
					case ADDRESS_MALFORMED:
						Aunis.logger.error("Orlin's gate - wrong dialed address");
						break;
						
					case NOT_ENOUGH_POWER:
						Aunis.info("Orlin's gate - Not enough power");
						break;
						
					case ABORTED:
						break;
				}
			}
			
			else if (!isPowered && stargateState.initiating()) {
				attemptClose(StargateClosedReasonEnum.REQUESTED);
			}
			
			markDirty();
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Merging
	
	@Override
	public StargateAbstractMergeHelper getMergeHelper() {
		return StargateOrlinMergeHelper.INSTANCE;
	}
		
	
	// ------------------------------------------------------------------------
	// Killing
	
	@Override
	protected AunisAxisAlignedBB getHorizonKillingBox(boolean server) {
		return new AunisAxisAlignedBB(-0.5, 1, -0.5, 0.5, 2, 1.5);
	}
	
	@Override
	protected int getHorizonSegmentCount(boolean server) {
		return 2;
	}
	
	@Override
	protected List<AunisAxisAlignedBB> getGateVaporizingBoxes(boolean server) {
		return Arrays.asList(new AunisAxisAlignedBB(-0.5, 1, -0.5, 0.5, 2, 0.5));
	}
		
	
	// ------------------------------------------------------------------------
	// Rendering
	
	@Override
	protected AunisAxisAlignedBB getHorizonTeleportBox(boolean server) {
		return new AunisAxisAlignedBB(-1.0, 0.6, -0.15, 1.0, 2.7, -0.05);
	}
	
//	@Override
//	protected StargateAbstractRendererState getRendererStateServer() {
//		return StargateAbstractRendererState.builder()
//				.setStargateState(stargateState).build();
//	}
	
	@Override
	protected StargateAbstractRendererState createRendererStateClient() {
		return new StargateOrlinRendererState();
	}
	
	@Override
	public StargateOrlinRendererState getRendererStateClient() {
		return (StargateOrlinRendererState) super.getRendererStateClient();
	}
	
	
	// ------------------------------------------------------------------------
	// Sounds
	
	@Override
	protected SoundPositionedEnum getPositionedSound(StargateSoundPositionedEnum soundEnum) {
		return null;
	}

	@Override
	protected SoundEventEnum getSoundEvent(StargateSoundEventEnum soundEnum) {
		switch (soundEnum) {
			case OPEN: return SoundEventEnum.GATE_MILKYWAY_OPEN;
			case CLOSE: return SoundEventEnum.GATE_MILKYWAY_CLOSE;
			case DIAL_FAILED: return SoundEventEnum.GATE_MILKYWAY_DIAL_FAILED;
			default: return null;
		}
	}
	
	
	// ------------------------------------------------------------------------
	// States

	@Override
	public State createState(StateTypeEnum stateType) {
		switch (stateType) {
			case SPARK_STATE:
				return new StargateOrlinSparkState();
				
			default:
				return super.createState(stateType);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setState(StateTypeEnum stateType, State state) {
		switch (stateType) {
			case SPARK_STATE:
				StargateOrlinSparkState sparkState = (StargateOrlinSparkState) state;
				getRendererStateClient().sparkFrom(sparkState.sparkIndex, sparkState.spartStart);
				
				break;
				
			default:
				super.setState(stateType, state);
				break;
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Sparks
	
	private int sparkIndex;
	
	public void startSparks() {
		sparkIndex = 0;
		
		addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ORLIN_SPARK, 5));
	}
	
	
	// ------------------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask, NBTTagCompound customData) {
		switch (scheduledTask) {
			case STARGATE_ORLIN_OPEN:
				StargatePos targetGatePos = network.getStargate(dialedAddress);
				
				if (hasEnergyToDial(targetGatePos)) {
					targetGatePos.getTileEntity().incomingWormhole(dialedAddress.size());
				}
				
				attemptOpenDialed();
				break;
				
			case STARGATE_ORLIN_SPARK:				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.SPARK_STATE, new StargateOrlinSparkState(sparkIndex, world.getTotalWorldTime())), targetPoint);
				
				if (sparkIndex < 6 && sparkIndex != -1)
					addTask(new ScheduledTask(EnumScheduledTask.STARGATE_ORLIN_SPARK, 24));
				
				sparkIndex++;
				
				break;
				
			case STARGATE_FAILED_SOUND:
				playSoundEvent(StargateSoundEventEnum.DIAL_FAILED);
				
				break;
				
			default:
				super.executeTask(scheduledTask, customData);
				break;
		}
	}
	

	// ------------------------------------------------------------------------
	// Power
	
	private StargateAbstractEnergyStorage energyStorate = new StargateAbstractEnergyStorage();
	
	@Override
	protected StargateAbstractEnergyStorage getEnergyStorage() {
		return energyStorate;
	}
	
	@Override
	protected StargateEnergyRequired getEnergyRequiredToDial(StargatePos targetGatePos) {
		return super.getEnergyRequiredToDial(targetGatePos).mul(AunisConfig.powerConfig.stargateOrlinEnergyMul).cap(AunisConfig.powerConfig.stargateEnergyStorage/4 - 1000000);
	}
	
//	@Override
//	protected int getMaxEnergyStorage() {
//		return AunisConfig.powerConfig.stargateOrlinEnergyStorage;
//	}
//	
//	@Override
//	protected boolean canReceiveEnergy() {
//		return !isBroken();
//	}
	
	public static final AunisAxisAlignedBB RENDER_BOX = new AunisAxisAlignedBB(-5.5, 0, -0.5, 5.5, 10.5, 0.5);
	
	@Override
	protected AunisAxisAlignedBB getRenderBoundingBoxRaw() {
		return RENDER_BOX;
	}
	
	// ------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isPowered", isPowered);
		compound.setInteger("openCount", openCount);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		isPowered = compound.getBoolean("isPowered");
		openCount = compound.getInteger("openCount");
		
		super.readFromNBT(compound);
	}
	
	@Optional.Method(modid = "opencomputers")
	@Callback
	public Object[] getGateType(Context context, Arguments args) {
		return new Object[] {isMerged() ? "ORLIN" : null};
	}
}
