package mrjake.aunis.renderer.stargate;

import li.cil.oc.api.machine.Context;
import mrjake.aunis.Aunis;
import mrjake.aunis.integration.opencomputers.OCHelper;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToClient;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumGateAction;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacket.EnumPacket;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.renderer.SpinHelper;
import mrjake.aunis.renderer.state.SpinState;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.stargate.EnumSpinDirection;
import mrjake.aunis.stargate.EnumStargateState;
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
	 * Symbol being locked or engaged.
	 */
	private EnumSymbol targetSymbol;
	
	/**
	 * OpenComputers {@link Context} to send events to.
	 */
	private Context context;
	
	/**
	 * Indicates if final chevron lock sound has been played
	 */
	private boolean lockSoundPlayed;
	
	/**
	 * If the ring is spinned by a computer
	 */
	private boolean computerInitializedStop;
	private boolean finalChevron;
	
	public StargateRingSpinHelper(World world, BlockPos pos, StargateRenderer renderer, SpinState state) {
		super(world, state);
		
		this.pos = pos;
		this.renderer = renderer;
		this.targetPoint = new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512);
	}
	
	@Override
	protected double getAnglePerTick() {
		return ANGLE_PER_TICK;
	}
	
	@Override
	public void requestStart(double startingRotation, EnumSpinDirection direction) {
		setSpeedUpTimeTick(25);
		
		super.requestStart(startingRotation, direction);
		Aunis.info("startingRotation: " + startingRotation);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, boolean moveOnly) {
		this.targetSymbol = targetSymbol;
		
		if (moveOnly) {
			computerInitializedStop = true;
			onStopReached(targetSymbol.angle);
		}
		
		else
			this.requestStart(startingRotation, direction);
		
		AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.SPIN_STATE, new SpinStateRequest(direction, targetSymbol.angle, lock, moveOnly)), this.targetPoint);
	}
	
	public void requestStart(double startingRotation, EnumSpinDirection direction, EnumSymbol targetSymbol, boolean lock, Context context, boolean moveOnly) {
		this.context = context;
		
		this.requestStart(startingRotation, direction, targetSymbol, lock, moveOnly);
	}
		
	@Override
	public void requestStop() {
		setSpeedUpTimeTick(STOP_TIME_TICK);		
		this.computerInitializedStop = false;
		
		super.requestStop();
	}
	
	public void requestStopByComputer(boolean finalChevron) {
		requestStop();
		
		this.computerInitializedStop = true;
		this.finalChevron = finalChevron;
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
			if (computerInitializedStop) {
				
				if (!lockSoundPlayed && (effectiveTick >= (state.tickStopRequested + speedUpTimeTick - 5))) {
					lockSoundPlayed = true;
					
					renderer.requestFinalMove(finalChevron);
				}
			}
				
			else {
				if (!lockSoundPlayed && (effectiveTick*1.25f >= (state.tickStopRequested + speedUpTimeTick))) {
					lockSoundPlayed = true;
										
					// Play final chevron lock sound
					if (renderer.state.dialingComplete) {
						renderer.moveFinalChevron();
						AunisSoundHelper.playSound((WorldClient) world, pos, AunisSoundHelper.chevronLockDHD, 0.5f);
					}
				}
			}
		}
	}
		
	@Override
	protected void onStopReached(double angle) {
		lockSoundPlayed = false;
		
		Aunis.info("ending angle: " + angle);
		
		// Server side
		if (!world.isRemote && computerInitializedStop) {			
			StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(pos);
			gateTile.markStargateIdle();
			
			if (gateTile.addSymbolToAddress(targetSymbol, gateTile.getLinkedDHD(world), true)) {	
				
				if (gateTile.isLinked())
					AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.DHD_RENDERER_UPDATE, targetSymbol.id, gateTile.getLinkedDHD(world)), targetPoint );
				
				int symbolCount = gateTile.getEnteredSymbolsCount();
				boolean lock = symbolCount == 8 || (symbolCount == 7 && targetSymbol == EnumSymbol.ORIGIN);
				
				if (lock) {
//					AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_FINAL_COMPUTER, gateTile), targetPoint );
					
					// context.signal("stargate_chevron_locked", new Object[] { symbolCount, targetSymbol.name });
//					OCHelper.sendSignalToReachable(gateTile.node(), context, "stargate_spin_chevron_locked", new Object[] { symbolCount, targetSymbol.name });
					
					GateRenderingUpdatePacketToServer.attemptLightUp(world, gateTile);
				}
				
				else {
//					AunisPacketHandler.INSTANCE.sendToAllTracking( new GateRenderingUpdatePacketToClient(EnumPacket.GATE_RENDERER_UPDATE, EnumGateAction.ACTIVATE_NEXT_COMPUTER, gateTile), targetPoint );
					
					// context.signal("stargate_chevron_engaged", new Object[] { symbolCount, targetSymbol.name });
//					OCHelper.sendSignalToReachable(gateTile.node(), context, "stargate_spin_chevron_engaged", new Object[] { symbolCount, targetSymbol.name });
				}
				
				OCHelper.sendSignalToReachable(gateTile.node(), context, "stargate_spin_chevron_engaged", new Object[] { symbolCount, lock, targetSymbol.name });
				
				
			}
			
			else {
				
			}
		}
	}
}