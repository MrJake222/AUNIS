package mrjake.aunis.particle;

import mrjake.aunis.renderer.stargate.orlin.StargateRendererOrlin;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ParticleBlender {
	
	private float x;
	private float y;
	private float z;
	private int moduloTicks;
	private float motionX;
	private float motionZ;
	private boolean falling;
	private RandomizeInterface randomize;

	public ParticleBlender(float x, float y, float z, int moduloTicks, float motionX, float motionZ, boolean falling, RandomizeInterface randomize) {
		this.x = x * StargateRendererOrlin.GATE_SCALE + 0.5f;
		this.y = z * StargateRendererOrlin.GATE_SCALE + 1.0f;
		this.z = -y * StargateRendererOrlin.GATE_SCALE + 0.5f;
		this.moduloTicks = moduloTicks;
		this.motionX = motionX;
		this.motionZ = motionZ;
		this.falling = falling;
		
		this.randomize = randomize;
	}
	
	public ParticleBlender(float x, float y, float z, int moduloTicks, boolean falling, RandomizeInterface randomize) {
		this(x, y, z, moduloTicks, 0, 0, falling, randomize);
	}

	public void spawn(World world, BlockPos pos) {
		if (world.getTotalWorldTime() % moduloTicks != 0)
			return;
		
		float x = this.x + pos.getX();
		float y = this.y + pos.getY();
		float z = this.z + pos.getZ();
		
		Motion motion = new Motion(motionX, motionZ);
		
		if (randomize != null)
			randomize.randomize(motion);
		
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleWhiteSmoke(world, x, y, z, motion.motionX, motion.motionZ, falling));
	}
		
	public static class Motion {	
		public Motion(float motionX, float motionZ) {
			this.motionX = motionX;
			this.motionZ = motionZ;
		}
//		private boolean xSet = false;
		public double motionX;
//		
//		public void setMotionX(double motionX) {
//			this.motionX = motionX;
//			this.xSet = true;
//		}
//		
//		public double getMotionX() {
//			return motionX;
//		}
//		
//		public boolean isXSet() {
//			return xSet;
//		}
//		
//		private boolean zSet;
		public double motionZ;
		
//		public void setMotionZ(double motionZ) {
//			this.motionZ = motionZ;
//			this.zSet = true;
//		}	
//		
//		public double getMotionZ() {
//			return motionZ;
//		}
//		
//		public boolean isZSet() {
//			return zSet;
//		}
	}
	
	public static interface RandomizeInterface {
		public abstract void randomize(Motion motion);
	}
}
