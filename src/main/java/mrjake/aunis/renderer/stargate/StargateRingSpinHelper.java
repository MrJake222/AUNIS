package mrjake.aunis.renderer.stargate;

import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.integration.opencomputers.OCHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.renderer.SpinHelper;
import mrjake.aunis.renderer.state.StargateSpinState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumSymbol;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.SpinStateRequest;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.client.multiplayer.WorldClient;
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
	private static final int STOP_TIME_TICK = 35;

	
	private BlockPos pos;
	private StargateRenderer renderer;
	private TargetPoint targetPoint;
	
	/**
	 * OpenComputers {@link Context} to send events to.
	 */
	private Context context;
	
	public StargateRingSpinHelper(World world, BlockPos pos, StargateRenderer renderer, StargateSpinState state) {
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
		
		super.requestStart(startingRotation, direction);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, boolean moveOnly) {
		getStargateSpinState().targetSymbol = targetSymbol;
		
		if (moveOnly) {
			getStargateSpinState().computerInitializedStop = true;
			onStopReached(targetSymbol.angle);
		}
		
		else
			this.requestStart(startingRotation, direction);
		
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.SPIN_STATE, new SpinStateRequest(direction, targetSymbol, lock, moveOnly)), this.targetPoint);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, Context context, boolean moveOnly) {
		this.context = context;
		
		this.requestStart(startingRotation, direction, targetSymbol, lock, moveOnly);
	}
		
	@Override
	public void requestStop() {
		setSpeedUpTimeTick(STOP_TIME_TICK);		
		getStargateSpinState().computerInitializedStop = false;
		
		super.requestStop();
	}
	
	public void requestStopByComputer(boolean finalChevron) {
		requestStop();
		
		getStargateSpinState().computerInitializedStop = true;
		getStargateSpinState().finalChevron = finalChevron;
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
					
					renderer.requestFinalMove(getStargateSpinState().finalChevron);
				}
			}
				
			else {
				if (!getStargateSpinState().lockSoundPlayed && (effectiveTick*1.25f >= (state.tickStopRequested + speedUpTimeTick))) {
					getStargateSpinState().lockSoundPlayed = true;
										
					// Play final chevron lock sound
					if (renderer.getState().dialingComplete) {
						renderer.moveFinalChevron((long) effectiveTick);
						AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.chevronLockDHD, 0.5f);
					}
				}
			}
		}
		
		else {
			StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
			gateTile.setRollPlayed();
		}
	}
		
	@Override
	protected void onStopReached(double angle) {
		getStargateSpinState().lockSoundPlayed = false;
				
		StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
		EnumSymbol symbol = EnumSymbol.fromAngle(angle % 360);
		Aunis.info("ending angle: " + angle + ", symbol: " + symbol);
		
		// Server side
		if (!world.isRemote) {
			gateTile.markStargateIdle();
			gateTile.setEndingSymbol(symbol);
			
			if (getStargateSpinState().computerInitializedStop) {
				if (gateTile.addSymbolToAddress(getStargateSpinState().targetSymbol, gateTile.getLinkedDHD(world), true)) {	
					
					if (gateTile.isLinked())
						AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, getStargateSpinState().targetSymbol.id, gateTile.getLinkedDHD(world)), targetPoint );
					
					int symbolCount = gateTile.getEnteredSymbolsCount();
					boolean lock = symbolCount == 8 || (symbolCount == 7 && getStargateSpinState().targetSymbol == EnumSymbol.ORIGIN);
					
					if (lock)
						GateRenderingUpdatePacketToServer.attemptLightUp(world, gateTile);
					
					OCHelper.sendSignalToReachable(gateTile.node(), context, "stargate_spin_chevron_engaged", new Object[] { symbolCount, lock, getStargateSpinState().targetSymbol.name });
				}
			}
		}
		
		// Client
		else {
			gateTile.getStargateRenderer().getState().ringCurrentSymbol = symbol;
		}
	}
}