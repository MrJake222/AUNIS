package mrjake.aunis.renderer.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.StateUpdatePacketToClient;
import mrjake.aunis.packet.stargate.StargateRenderingUpdatePacketToServer;
import mrjake.aunis.renderer.SpinHelper;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.StateTypeEnum;
import mrjake.aunis.state.StargateRingStopRequest;
import mrjake.aunis.state.StargateSpinState;
import mrjake.aunis.state.StargateSpinStateRequest;
import mrjake.aunis.tileentity.stargate.StargateMilkyWayBaseTile;
import mrjake.aunis.tileentity.util.ScheduledTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Class handling ring spinning. Does the calculations to smoothly start
 * and stop the ring spinning.
 *
 */
public class StargateRingSpinHelper extends SpinHelper {
	
	private static final double ANGLE_PER_TICK = 1.0;
	public static final int STOP_TIME_TICK = 35;

	private BlockPos pos;
	private StargateRendererSG1 renderer;
	private TargetPoint targetPoint;
	
	/**
	 * OpenComputers {@link Context} to send events to.
	 */
	private Object context;
	
	public StargateRingSpinHelper(World world, BlockPos pos, StargateRendererSG1 renderer, StargateSpinState state) {
		super(world, state);
		
		this.pos = pos;
		this.renderer = renderer;
		this.targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
	}
	
	private StargateSpinState getStargateSpinState() {
		return (StargateSpinState) state;
	}
	
	@Override
	protected double getAnglePerTick() {
		return ANGLE_PER_TICK;
	}
	
	@Override
	public void requestStart(double startingRotation, EnumSpinDirection direction) {
		setSpeedUpTimeTick(25);
//		Aunis.info("requestStart isSpinning: " + state.isSpinning + ", startingRotation: " + startingRotation + ", state.startingRotation: " + state.startingRotation);

		super.requestStart(startingRotation, direction);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, boolean moveOnly) {
		getStargateSpinState().targetSymbol = targetSymbol;
		getStargateSpinState().finalChevron = lock;
		
		if (moveOnly) {
//			getStargateSpinState().computerInitializedStop = true;
			requestStopByComputer(world.getTotalWorldTime(), true);
			onStopReached(targetSymbol.angle);
		}
		
		else
			this.requestStart(startingRotation, direction);
		
		if (!world.isRemote)
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.SPIN_STATE, new StargateSpinStateRequest(direction, targetSymbol, lock, moveOnly)), this.targetPoint);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, Object context, boolean moveOnly) {
		this.context = context;
		
		this.requestStart(startingRotation, direction, targetSymbol, lock, moveOnly);
	}
		
	@Override
	public void requestStop(long worldTicks) {
		setSpeedUpTimeTick(STOP_TIME_TICK);		
		getStargateSpinState().computerInitializedStop = false;
		
		super.requestStop(worldTicks);
	}
	
	public void requestStopByComputer(long worldTicks, boolean moveOnly) {
		if (!moveOnly)
			requestStop(worldTicks);
		
		getStargateSpinState().computerInitializedStop = true;
		
		if (!world.isRemote) {			
			AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, StateTypeEnum.STARGATE_RING_STOP, new StargateRingStopRequest(worldTicks, moveOnly)), targetPoint);
		}
		
		else {
			StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(pos);

			long time;
			
			if (moveOnly)
				time = world.getTotalWorldTime();
			else
				time = state.tickStart + state.tickStopRequested + speedUpTimeTick - 5;
			
			gateTile.addComputerActivation(time, getStargateSpinState().finalChevron);
		}			
	}
	
	public static double getStopAngleTraveled() {
		return STOP_TIME_TICK * ANGLE_PER_TICK;
	}
	
	@Override
	protected double getStopTickShift() {
		return 5;
	}
	
	protected void stopRequestedAction(double effectiveTick) {
		if (world.isRemote) {			
			if (getStargateSpinState().computerInitializedStop) {
				
				if (!getStargateSpinState().lockSoundPlayed && (effectiveTick >= (state.tickStopRequested + speedUpTimeTick - 5))) {
					getStargateSpinState().lockSoundPlayed = true;
					
					renderer.requestFinalMove(state.tickStart + state.tickStopRequested + speedUpTimeTick - 5, getStargateSpinState().finalChevron);
				}
			}
				
			else {
				if (!getStargateSpinState().lockSoundPlayed && (effectiveTick*1.25f >= (state.tickStopRequested + speedUpTimeTick))) {
					getStargateSpinState().lockSoundPlayed = true;
										
					// Play final chevron lock sound
					if (renderer.isDialingComplete()) {						
						renderer.moveFinalChevron(state.tickStart + state.tickStopRequested + 15);
					}
				}
			}
		}
		
		else {
			StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(pos);
			
			if (getStargateSpinState().computerInitializedStop) {
				if (!getStargateSpinState().lockSoundPlayed && (effectiveTick >= (state.tickStopRequested + speedUpTimeTick - 5))) {
					getStargateSpinState().lockSoundPlayed = true;
					
					gateTile.setRingRollStopFlag(true);
										
					if (getStargateSpinState().finalChevron) {
						AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_SHUT, 1f);
						
						gateTile.addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_SHUT_SOUND));
						gateTile.addTask(new ScheduledTask(EnumScheduledTask.STARGATE_CHEVRON_OPEN_SOUND));
					} 
					
					else
						AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.CHEVRON_LOCKING, 0.2f);
					
					gateTile.setRollPlayed();
				}
			}
		}
	}
		
	@Override
	protected void onStopReached(double angle) {
		getStargateSpinState().lockSoundPlayed = false;
				
		StargateMilkyWayBaseTile gateTile = (StargateMilkyWayBaseTile) world.getTileEntity(pos);
		
		if (angle < 0)
			angle += 360;
		
		EnumSymbol symbol = EnumSymbol.fromAngle(angle % 360);
//		Aunis.info("ending angle: " + angle + ", symbol: " + symbol);
		
		// Server side
		if (!world.isRemote) {
			gateTile.markStargateIdle();
			gateTile.setEndingSymbol(symbol);
			
			if (getStargateSpinState().computerInitializedStop) {
				if (gateTile.addSymbolToAddress(getStargateSpinState().targetSymbol, gateTile.getLinkedDHD(world), true)) {	
					
					if (gateTile.isLinked())
						gateTile.getLinkedDHD(world).activateSymbol(getStargateSpinState().targetSymbol.id);
					
					int symbolCount = gateTile.getEnteredSymbolsCount();
					boolean lock = symbolCount == 8 || (symbolCount == 7 && getStargateSpinState().targetSymbol == EnumSymbol.ORIGIN);
					
					Aunis.info("symbolCount: " + symbolCount + ", target: " + getStargateSpinState().targetSymbol + ", lock: " + lock);
					
					if (lock)
						StargateRenderingUpdatePacketToServer.attemptLightUp(world, gateTile);
					
					gateTile.sendSignal(context, "stargate_spin_chevron_engaged", new Object[] { symbolCount, lock, getStargateSpinState().targetSymbol.name });
				}
			}
		}
		
		// Client
		else {
			gateTile.setRendererCurrentSymbol(symbol);
		}
	}
}