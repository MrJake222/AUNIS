package mrjake.aunis.particle;

import mrjake.aunis.renderer.stargate.StargateOrlinRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to recalc Blender coords to Minecraft ones and rotate them
 * 
 * @author MrJake222
 */
public abstract class ParticleBlender {
	
	private SimpleVector pos;
	private float y;
	
	private int moduloTicks;
	private int moduloTicksSlower;
	
	private float motionX;
	private float motionZ;
	private boolean falling;
	private RandomizeInterface randomize;
	
	public ParticleBlender(float x, float y, float z, int moduloTicks, int moduloTicksSlower, float motionX, float motionZ, boolean falling, RandomizeInterface randomize) {
		this.pos = new SimpleVector(x * StargateOrlinRenderer.GATE_SCALE, -y * StargateOrlinRenderer.GATE_SCALE);
		this.y = z * StargateOrlinRenderer.GATE_SCALE;
		
		this.moduloTicks = moduloTicks;
		this.moduloTicksSlower = moduloTicksSlower;
		
		this.motionX = motionX;
		this.motionZ = motionZ;
		this.falling = falling;
		
		this.randomize = randomize;
	}
	
	public ParticleBlender(float x, float y, float z, int moduloTicks, int moduloTicksSlower, boolean falling, RandomizeInterface randomize) {
		this(x, y, z, moduloTicks, moduloTicksSlower, 0, 0, falling, randomize);
	}

	public int getModuloTicks(boolean slower) {
		return slower ? moduloTicksSlower : moduloTicks;
	}
	
	public void spawn(World world, BlockPos pos, float horizontalRotation, boolean slower) {
		if (world.getTotalWorldTime() % getModuloTicks(slower) != 0)
			return;
		
		SimpleVector position = this.pos.rotate((int) horizontalRotation);
		SimpleVector motion = new SimpleVector(motionX, motionZ);
		
		if (randomize != null)
			randomize.randomize(motion);
		
		motion = motion.rotate((int) horizontalRotation);
				
		Minecraft.getMinecraft().effectRenderer.addEffect(createParticle(world, position.x + pos.getX() + 0.5f, y + pos.getY(), position.z + pos.getZ() + 0.5f, motion.x, motion.z, falling));
	}
	
	/**
	 * Spawn particle
	 * 
	 * @param world
	 * @param d
	 * @param f
	 * @param e
	 * @param x
	 * @param z
	 * @param falling
	 * @return
	 */
	protected abstract Particle createParticle(World world, double x, float y, double z, double motionX, double motionZ, boolean falling);

	public static class SimpleVector {
		public double x;
		public double z;
		
		public SimpleVector(double x, double z) {
			this.x = x;
			this.z = z;
		}
		
		public SimpleVector rotate(int rotation) {
			switch (rotation) {
		        case 90:
		            return new SimpleVector(z, -x);
		            
		        case 180:
		            return new SimpleVector(-x, -z);
		            
		        case 270:
		            return new SimpleVector(-z, x);
		            
		        default:
		            return this;
			}	
		}
		
		public SimpleVector offset(BlockPos pos) {
			return new SimpleVector(x + pos.getX(), z + pos.getZ());
		}
		
		@Override
		public String toString() {
			return "[" + x + ", " + z + "]";
		}
	}
	
	public static interface RandomizeInterface {
		public abstract void randomize(SimpleVector motion);
	}
}
