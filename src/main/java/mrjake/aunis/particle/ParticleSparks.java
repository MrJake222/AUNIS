package mrjake.aunis.particle;

import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.world.World;

public class ParticleSparks extends ParticleFirework.Spark {

	public ParticleSparks(World world, double x, double y, double z, double motionX, double motionZ, boolean falling, ParticleManager particleManager) {
		super(world, x, y, z, 0, 0, 0, particleManager);
		
		this.motionX = motionX;
		this.motionZ = motionZ;
		
        this.motionY = 0;
	}
}
