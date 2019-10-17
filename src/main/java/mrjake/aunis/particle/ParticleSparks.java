package mrjake.aunis.particle;

import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.world.World;

public class ParticleSparks extends ParticleFirework.Spark {

	public ParticleSparks(World world, double x, double y, double z, ParticleManager particleManager) {
		super(world, x, y, z, 0, 0, 0, particleManager);
		
		this.motionX = 0.2 + Math.random()/10;
        this.motionY = 0;
        this.motionZ = -0.02 + Math.random()/25;
	}

//	private boolean falling;

//	public ParticleSparks(World world, double x, double y, double z, double motionX, double motionZ, boolean falling) {
//		super(world, x, y, z, 0, 0, 0);
//		
//		this.motionX = motionX;
////		this.motionY = 0.05;
//		this.motionZ = motionZ;
//		
//		this.falling = falling;
//		
//		this.particleScale = (float) (0.8f + (Math.random() * 1));
//	}
}
