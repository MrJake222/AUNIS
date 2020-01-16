package mrjake.aunis.tileentity.stargate;

import mrjake.aunis.Aunis;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.packet.state.StateUpdatePacketToClient;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.renderer.stargate.orlin.StargateRendererOrlin;
import mrjake.aunis.renderer.state.SparkState;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.EnumScheduledTask;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.state.EnumStateType;
import mrjake.aunis.state.State;
import mrjake.aunis.tileentity.DHDTile;
import mrjake.aunis.tileentity.ScheduledTask;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class StargateBaseTileOrlin extends StargateBaseTile { //implements SimpleComponent {
	
	
	// ------------------------------------------------------------------------
	// Ticking
	
	@Override
	public void onLoad() {
		super.onLoad();
		
		renderer = new StargateRendererOrlin(this);
	}
	
	public long animStart;
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			renderer.spawnParticles();
		}
	}
	
	
	// ------------------------------------------------------------------------
	// Redstone
	
	private boolean isPowered;
	
	public void redstonePowerUpdate(boolean power) {
		if ((isPowered && !power) || (!isPowered && power)) {
			isPowered = power;
			
			if (isPowered) {
				if (stargateState == EnumStargateState.IDLE) {
					if (GateRenderingUpdatePacketToServer.checkDialedAddress(world, this)) {
						startSparks();
						AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.GATE_ORLIN_DIAL, 1.0f);
						
						addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_ORLIN_OPEN));
					}
					
					else {
						Aunis.info("wrong dialed address");
					}
				}
			}
			
			else {
				if (stargateState == EnumStargateState.ENGAGED_INITIATING)
					GateRenderingUpdatePacketToServer.closeGatePacket(this, false);
			}
			
			markDirty();
			Aunis.info("Gate is powered: " + isPowered);
		}
	}
	
	
	
	
	// ------------------------------------------------------------------------
	// Rendering
	
	@Override
	protected EventHorizon getEventHorizon()  {
		if (eventHorizon == null)
			eventHorizon = new EventHorizon(world, pos, -0.5f, 1.5f, 0.7f, 2.7f);
		
		return eventHorizon;
	}
	
	private StargateRendererOrlin renderer;
	private StargateRendererStateBase rendererState = new StargateRendererStateBase();
	
	@Override
	public StargateRendererBase getRenderer() {
		return renderer;
	}
	
	public StargateRendererOrlin getRendererOrlin() {
		return renderer;
	}
	
	@Override
	protected StargateRendererStateBase getRendererState() {
		return rendererState;
	}
	
	@Override
	public void render(double x, double y, double z, float partialTicks) {
		getEventHorizon().render(x, y, z);
		
		x += 0.5;
//		y += 1.0;
		z += 0.5;
		
		super.render(x, y, z, partialTicks);
	}
	
	
	// ------------------------------------------------------------------------
	// States
	
	@Override
	public State getState(EnumStateType stateType) {
		switch (stateType) {
			case SPARK_STATE:
				return null;
				// Shouldn't be done this way
				
			default:
				return super.getState(stateType);
		}
	}

	@Override
	public State createState(EnumStateType stateType) {
		switch (stateType) {
			case SPARK_STATE:
				return new SparkState();
				
			default:
				return super.createState(stateType);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setState(EnumStateType stateType, State state) {
		switch (stateType) {
			case SPARK_STATE:
				SparkState sparkState = (SparkState) state;
				getRendererOrlin().sparkFrom(sparkState.sparkIndex, sparkState.spartStart);
				
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
		
		addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_ORLIN_SPARK, 5));
	}
	
	// ------------------------------------------------------------------------
	// Scheduled tasks
	
	@Override
	public void executeTask(EnumScheduledTask scheduledTask) {
		switch (scheduledTask) {
			case STARGATE_ORLIN_OPEN:
				GateRenderingUpdatePacketToServer.attemptLightUp(world, this);
				GateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);
				
				break;
				
			case STARGATE_ORLIN_SPARK:
				Aunis.info("sparkIndex: " + sparkIndex);
				
				AunisPacketHandler.INSTANCE.sendToAllTracking(new StateUpdatePacketToClient(pos, EnumStateType.SPARK_STATE, new SparkState(sparkIndex, world.getTotalWorldTime())), targetPoint);
				
				if (sparkIndex < 6 && sparkIndex != -1)
					addTask(new ScheduledTask(this, world.getTotalWorldTime(), EnumScheduledTask.STARGATE_ORLIN_SPARK, 24));
				
				sparkIndex++;
				
				break;
				
			default:
				super.executeTask(scheduledTask);
				break;
		}
	}
	

	// ------------------------------------------------------------------------
	// OpenComputers
	
//	@Override
//	public String getComponentName() {
//		return "stargate_orlin";
//	}

	
	// ------------------------------------------------------------------------
	// NBT
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setBoolean("isPowered", isPowered);
		
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		isPowered = compound.getBoolean("isPowered");
		
		super.readFromNBT(compound);
	}
	
	
	// ------------------------------------------------------------------------
	// Overrides
	
	@Override
	protected int getMaxChevrons(boolean computer, DHDTile dhdTile) {
		return 7;
	}

	@Override
	protected void firstGlyphDialed(boolean computer) {}

	@Override
	protected void lastGlyphDialed(boolean computer) {}

	@Override
	protected void dialingFailed(boolean stopRing) {}

	@Override
	public DHDTile getLinkedDHD(World world) {
		return null;
	}

	@Override
	protected void clearLinkedDHDButtons(boolean dialingFailed) {}
}
