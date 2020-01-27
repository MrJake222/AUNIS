package mrjake.aunis.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleBlenderSparks extends ParticleBlender {

	public ParticleBlenderSparks(float x, float y, float z, int moduloTicks, int moduloTicksSlower, float motionX, float motionZ, boolean falling, RandomizeInterface randomize) {
		super(x, y, z, moduloTicks, moduloTicksSlower, motionX, motionZ, falling, randomize);
	}
	
	public ParticleBlenderSparks(float x, float y, float z, int moduloTicks, int moduloTicksSlower, boolean falling, RandomizeInterface randomize) {
		super(x, y, z, moduloTicks, moduloTicksSlower, falling, randomize);
	}

	@Override
	protected Particle createParticle(World world, double x, float y, double z, double motionX, double motionZ, boolean falling) {
		return new ParticleSparks(world, x, y, z, motionX, motionZ, falling, Minecraft.getMinecraft().effectRenderer);
	}

}
