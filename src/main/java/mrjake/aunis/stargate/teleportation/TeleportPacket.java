package mrjake.aunis.stargate.teleportation;

import javax.vecmath.Vector2f;

import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.SoundEventEnum;
import mrjake.aunis.stargate.network.StargatePos;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Class used for teleporting entities with saving their motion
 * 
 * @author MrJake222
 */
public class TeleportPacket {
	private BlockPos sourceGatePos;
	private StargatePos targetGatePos;
	
	private Entity entity;

	private float rotation;
	private Vector2f motionVector;
	
	public TeleportPacket(Entity entity, BlockPos source, StargatePos target, float rotation) {
		this.entity = entity;
		this.sourceGatePos = source;
		this.targetGatePos = target;
		
		this.rotation = rotation;
	}
	
	public void teleport() {
		TeleportHelper.teleportEntity(entity, sourceGatePos, targetGatePos, rotation, motionVector);
		
		AunisSoundHelper.playSoundEvent(targetGatePos.getWorld(), targetGatePos.getTileEntity().getGateCenterPos(), SoundEventEnum.WORMHOLE_GO);
	}

	public TeleportPacket setMotion(Vector2f motion) {
		this.motionVector = motion;
		
		return this;
	}
}
