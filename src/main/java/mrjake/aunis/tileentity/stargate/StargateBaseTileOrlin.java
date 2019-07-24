package mrjake.aunis.tileentity.stargate;

import java.util.Arrays;
import java.util.List;

import li.cil.oc.api.network.SimpleComponent;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.gate.renderingUpdate.GateRenderingUpdatePacketToServer;
import mrjake.aunis.particle.ParticleBlender;
import mrjake.aunis.renderer.stargate.StargateRendererBase;
import mrjake.aunis.renderer.stargate.orlin.StargateRendererOrlin;
import mrjake.aunis.renderer.state.stargate.StargateRendererStateBase;
import mrjake.aunis.stargate.EnumStargateState;
import mrjake.aunis.stargate.teleportation.EventHorizon;
import mrjake.aunis.tileentity.DHDTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class StargateBaseTileOrlin extends StargateBaseTile implements SimpleComponent {
	
	// ------------------------------------------------------------------------
	// Particles
	
	private static final List<ParticleBlender> GATE_OPEN_PARTICLES = Arrays.asList(
			new ParticleBlender(-2.71127f,  0.188794f, 5.76731f, 1, -0.1f, 0, false, (motion) -> { motion.motionZ = -0.03f + Math.random()*0.06f; }),
			new ParticleBlender(2.698340f,  0.210467f, 1.65171f, 1, 0.1f, 0, false, (motion) -> { motion.motionZ = -0.03f + Math.random()*0.06f; }),
			new ParticleBlender(-2.81152f,  0.007747f, 4.34894f, 1, true,  (motion) -> { motion.motionZ = 0.03f + Math.random()*0.05f; }),
			new ParticleBlender(0.880709f, -0.045567f, 6.63663f, 2, true,  (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = 0.03f + Math.random()*0.01f; }),
			new ParticleBlender(-1.27690f, -0.025613f, 1.15695f, 5, false, (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = 0.03f + Math.random()*0.01f; }),
			new ParticleBlender(1.276900f, -0.025613f, 1.15695f, 5, false, (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = 0.03f + Math.random()*0.01f; }),
			new ParticleBlender(2.279630f,  0.453827f, 5.72200f, 5, 0, -0.01f, true, (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = -0.03f + Math.random()*-0.01f; }),
			new ParticleBlender(-2.36438f,  0.644607f, 5.53441f, 5, 0, -0.01f, true, (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = -0.03f + Math.random()*-0.01f; }),
			new ParticleBlender(-1.26211f,  0.45161f, 1.12577f, 5, 0, -0.01f, true, (motion) -> { motion.motionX = -0.03f + Math.random()*0.06f; motion.motionZ = -0.03f + Math.random()*-0.01f; })
		);
	
	private void spawnParticles() {
		for (ParticleBlender particle : GATE_OPEN_PARTICLES) {
			particle.spawn(world, pos);
		}
		
//		for (ParticleBlender particle : Arrays.asList(
//		
//		)) {
//			particle.spawn(world, pos);
//		}
	}
	
	// ------------------------------------------------------------------------
	// Ticking
	
	@Override
	public void onLoad() {
		super.onLoad();
		
		renderer = new StargateRendererOrlin(this);
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
//			spawnParticles();
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
					GateRenderingUpdatePacketToServer.attemptLightUp(world, this);
					GateRenderingUpdatePacketToServer.attemptOpen(world, this, null, false);
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
	// OpenComputers
	
	@Override
	public String getComponentName() {
		return "stargate_orlin";
	}

	
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
