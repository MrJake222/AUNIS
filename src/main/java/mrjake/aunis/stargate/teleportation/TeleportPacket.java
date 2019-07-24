package mrjake.aunis.stargate.teleportation;

import javax.vecmath.Vector2f;

import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
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
		
		AunisSoundHelper.playSoundEvent(targetGatePos.getWorld(), targetGatePos.getPos(), EnumAunisSoundEvent.WORMHOLE_GO, 1.0f);
	}

	public TeleportPacket setMotion(Vector2f motion) {
		this.motionVector = motion;
		
		return this;
	}
}
