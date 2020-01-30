package mrjake.aunis.stargate.teleportation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2f;

import mrjake.aunis.AunisConfig;
import mrjake.aunis.AunisProps;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.stargate.StargateMotionToClient;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisSoundEvent;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.util.BoundingBoxHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHorizon {
	
	private final float HORIZON_PLACEMENT = 0.6f;
	private final float HORIZON_THICKNESS = 0.2f;
	
	private World world;
	private BlockPos pos;
	
	private float widthLeft;
	private float widthRight;
	private float bottom;
	private float height;
	
	private AxisAlignedBB localBB;
	private AxisAlignedBB globalBB;
	
	public EventHorizon(World world, BlockPos pos, float widthLeft, float widthRight, float bottom, float height) {
		this.world = world;
		this.pos = pos;
		
		this.widthLeft = widthLeft;
		this.widthRight = widthRight;
		this.bottom = bottom;
		this.height = height;
		
		globalize();
	}
	
	public void reset() {
		scheduledTeleportMap.clear();
	}
	
	private void globalize() {
		float offset1 = HORIZON_PLACEMENT - HORIZON_THICKNESS;
		float offset2 = HORIZON_PLACEMENT + HORIZON_THICKNESS;
		
		EnumFacing facing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
		
		switch (facing.getAxis()) {
			case X:
				localBB = new AxisAlignedBB(offset1, bottom, widthLeft,  offset2, height, widthRight);
				
				break;
				
			case Z:
				localBB = new AxisAlignedBB(widthLeft, bottom, offset1,  widthRight, height, offset2);
				break;
				
			default:
				break;
		}
		
		globalBB = localBB.offset(pos);
	}
	
	@SideOnly(Side.CLIENT)
	public void render(double x, double y, double z) {
		if (AunisConfig.debugConfig.renderHorizonBoundingBox) {		
			BoundingBoxHelper.render(x, y, z, localBB);
		}
	}
	
	// ------------------------------------------------------------------------
	// Teleporting
	
	private Map<Integer, TeleportPacket> scheduledTeleportMap = new HashMap<>();
	
	public void scheduleTeleportation(StargatePos targetGate) {
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, globalBB);

//		Aunis.info(globalBB + ": " + entities + ", map: " + scheduledTeleportMap);
		
		for (Entity entity : entities) {
			int entityId = entity.getEntityId();
			
			if ( !scheduledTeleportMap.containsKey(entityId) ) {				
				try {
					World targetWorld = TeleportHelper.getWorld(targetGate.getDimension());
					
					BlockPos targetPos = targetGate.getPos();
					
					EnumFacing sourceFacing = world.getBlockState(pos).getValue(AunisProps.FACING_HORIZONTAL);
					EnumFacing targetFacing = targetWorld.getBlockState(targetPos).getValue(AunisProps.FACING_HORIZONTAL);
					
					float rotation = (float) Math.toRadians( EnumFacing.fromAngle(targetFacing.getHorizontalAngle() - sourceFacing.getHorizontalAngle()).getOpposite().getHorizontalAngle() );

					TeleportPacket packet = new TeleportPacket(entity, pos, targetGate, rotation);
					
					if (entity instanceof EntityPlayerMP) {
						scheduledTeleportMap.put(entityId, packet);
						AunisPacketHandler.INSTANCE.sendTo(new StargateMotionToClient(pos), (EntityPlayerMP) entity);
					}
					
					else {
						Vector2f motion = new Vector2f( (float)entity.motionX, (float)entity.motionZ );
						
						if (TeleportHelper.frontSide(sourceFacing, motion)) {
							scheduledTeleportMap.put(entityId, packet.setMotion(motion) );
							teleportEntity(entityId);
						}
						
						/*else {
							// TODO Make custom message appear
							// entity.onKillCommand();
						}*/
					}
				}
				
				catch (Exception e) {
					e.printStackTrace();
					
					scheduledTeleportMap.remove(entityId);
				}
			}
		}
	}
	
	public void teleportEntity(int entityId) {
		scheduledTeleportMap.get(entityId).teleport();
		
		AunisSoundHelper.playSoundEvent(world, pos, EnumAunisSoundEvent.WORMHOLE_GO, 1.0f);
		scheduledTeleportMap.remove(entityId);
	}
	
	public void removeEntity(int entityId) {
		scheduledTeleportMap.remove(entityId);
	}

	public void setMotion(int entityId, Vector2f motionVector) {
		scheduledTeleportMap.get(entityId).setMotion(motionVector);		
	}
}
