package mrjake.aunis.particle;

import net.minecraft.client.particle.ParticleExplosion;
import net.minecraft.world.World;

public class ParticleWhiteSmoke extends ParticleExplosion {

	private boolean falling;

	public ParticleWhiteSmoke(World world, double x, double y, double z, double motionX, double motionZ, boolean falling) {
		super(world, x, y, z, 0, 0, 0);
		
		this.motionX = motionX;
//		this.motionY = 0.05;
		this.motionZ = motionZ;
		
		this.falling = falling;
		
		this.particleScale = (float) (0.8f + (Math.random() * 1));
	}
	
	@Override
	public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.motionY += 0.004D * (falling ? -1 : 1);
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.8999999761581421D;
        this.motionY *= 0.8999999761581421D;
        this.motionZ *= 0.8999999761581421D;

        if (this.onGround)
        {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }
}
