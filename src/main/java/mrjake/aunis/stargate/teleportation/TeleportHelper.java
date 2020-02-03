package mrjake.aunis.stargate.teleportation;

import java.util.Iterator;

import javax.vecmath.Vector2f;

import mrjake.aunis.AunisProps;
import mrjake.aunis.stargate.StargateNetwork.StargatePos;
import mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import mrjake.aunis.tileentity.stargate.StargateOrlinBaseTile;
import mrjake.vector.Matrix2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TeleportHelper {
	
	enum EnumFlipAxis {
		X(0x01),
		Z(0x02);
		
		public int mask;
		
		EnumFlipAxis(int mask) {
			this.mask = mask;
		}
		
		public static boolean masked(EnumFlipAxis flipAxis, int in) {
			return (in & flipAxis.mask) != 0;
		}
	}
	
	private static void translateTo00(Vector2f center, Vector2f v) {
		v.x -= center.x;
		v.y -= center.y;
	}
	
	public static void rotateAround00(Vector2f v, float rotation, int flip) {
		Matrix2f m = new Matrix2f();
		Matrix2f p = new Matrix2f();
		
		float sin = MathHelper.sin(rotation);
		float cos = MathHelper.cos(rotation);
		
		if ( EnumFlipAxis.masked(EnumFlipAxis.X, flip) )
			v.x *= -1;
		
		if ( EnumFlipAxis.masked(EnumFlipAxis.Z, flip) )
			v.y *= -1;
		/*if (flip != null) {
			if ( flip == Axis.X )
				v.x *= -1;
			else
				v.y *= -1;
		}*/
		
		m.m00 = cos;	m.m10 = -sin;
		m.m01 = sin;	m.m11 =  cos;
		p.m00 = v.x;	p.m10 = 0;
		p.m01 = v.y;	p.m11 = 0;
		
		Matrix2f out = Matrix2f.mul(m, p, null);
		
		v.x = out.m00;
		v.y = out.m01;
	}
	
	private static void translateToDest(Vector2f v, Vector2f dest) {
		v.x += dest.x;
		v.y += dest.y;
	}
	
	// Kindly borrowed from SGCraft beacause I have no idea how Minecraft dimension teleportation system works
	// and there is hardly any docs
	private static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vec3d position, float yaw) {
		MinecraftServer server = player.getServer();
		PlayerList playerList = server.getPlayerList();
		
		WorldServer oldWorld = player.getServerWorld();
		WorldServer newWorld = (WorldServer) getWorld(newDimension);
		player.dimension = newDimension;
		
		player.closeScreen();
		player.connection.sendPacket( new SPacketRespawn(player.dimension,
				player.world.getDifficulty(), newWorld.getWorldInfo().getTerrainType(),
				player.interactionManager.getGameType()) );
		
		oldWorld.removeEntityDangerously(player);
		player.isDead = false;
		player.setLocationAndAngles(position.x, position.y, position.z, yaw, player.rotationPitch);
		newWorld.spawnEntity(player);
		player.setWorld(newWorld);
		playerList.preparePlayer(player, oldWorld);
		player.connection.setPlayerLocation(position.x, position.y, position.z, yaw, player.rotationPitch);
		player.interactionManager.setWorld(newWorld);
		playerList.updateTimeAndWeatherForPlayer(player, newWorld);
		playerList.syncPlayerInventory(player);
		
		Iterator<PotionEffect> var6 = player.getActivePotionEffects().iterator();
		while (var6.hasNext()) {
			PotionEffect effect = (PotionEffect)var6.next();
			player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), effect));
		}
		player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldWorld.provider.getDimension(), newDimension);
		
	}
	
	private static void transferEntityToDimension(Entity entity, int oldDimension, int newDimension, Vec3d position, float yaw) {
//        MinecraftServer server = entity.getServer();
//        PlayerList playerList = server.getPlayerList();
        
        WorldServer oldWorld = (WorldServer) getWorld(oldDimension);
        WorldServer newWorld = (WorldServer) getWorld(newDimension);
        entity.dimension = newDimension;
        
        oldWorld.removeEntityDangerously(entity);
        entity.isDead = false;
        entity.setLocationAndAngles(position.x, position.y, position.z, yaw, entity.rotationPitch);
        newWorld.spawnEntity(entity);
        entity.setWorld(newWorld);
    }
	
	public static void teleportEntity(Entity entity, BlockPos sourceGatePos, StargatePos targetGatePos, float rotation, Vector2f motionVector) {		
		World world = entity.getEntityWorld();
		int sourceDim = world.provider.getDimension();
		
		StargateAbstractBaseTile sourceTile = (StargateAbstractBaseTile) world.getTileEntity(sourceGatePos);
		
		// TODO Cross dimension entity teleport not supported YET
//		if (sourceDim != targetGatePos.getDimension() && !(entity instanceof EntityPlayerMP))
//			return;
		
		EnumFacing sourceFacing = world.getBlockState(sourceGatePos).getValue(AunisProps.FACING_HORIZONTAL);
		EnumFacing targetFacing = targetGatePos.getWorld().getBlockState(targetGatePos.getPos()).getValue(AunisProps.FACING_HORIZONTAL);
		
		int flipAxis = 0;
		
		if (sourceFacing.getAxis() == targetFacing.getAxis())
			flipAxis |= EnumFlipAxis.X.mask;
		else
			flipAxis |= EnumFlipAxis.Z.mask;
		
		float yDiff = 0;
		if (sourceTile instanceof StargateOrlinBaseTile)
			yDiff = 1.5f;
		
		float yaw = getRotation(entity, rotation, flipAxis);
		Vec3d pos = getPosition(entity, sourceGatePos, targetGatePos.getPos(), rotation, targetFacing.getAxis()==Axis.Z ? ~flipAxis : flipAxis, yDiff);
		boolean isPlayer = entity instanceof EntityPlayerMP;
		
		if (sourceDim == targetGatePos.getDimension()) {
			entity.rotationYaw = yaw;
			entity.setPositionAndUpdate(pos.x, pos.y, pos.z);
		}
		
		else {			
			if (isPlayer) {
				EntityPlayerMP player = (EntityPlayerMP) entity;
				
				boolean flying = player.capabilities.isFlying;
				
				transferPlayerToDimension(player, targetGatePos.getDimension(), pos, yaw);
				
				player.capabilities.isFlying = flying;
				player.sendPlayerAbilities();
			}
			
			else {
				transferEntityToDimension(entity, sourceDim, targetGatePos.getDimension(), pos, yaw);
			}
		}
		
		setMotion(entity, rotation, motionVector);
		
		sourceTile.entityPassing(isPlayer, false);
		
		StargateAbstractBaseTile targetTile = (StargateAbstractBaseTile) getWorld(targetGatePos.getDimension()).getTileEntity(targetGatePos.getPos());
		targetTile.entityPassing(isPlayer, true);
	}
	
	/* public static void teleportPlayer(EntityPlayerMP player, BlockPos sourceGatePos, StargatePos targetGatePos, float rotation, Vector2f motionVector) {
		World world = player.getEntityWorld();
		
		EnumFacing sourceFacing = world.getBlockState(sourceGatePos).getValue(BlockFaced.FACING);
		EnumFacing targetFacing = targetGatePos.getWorld().getBlockState(targetGatePos.getPos()).getValue(BlockFaced.FACING);
		
		int flipAxis = 0;
		
		if (sourceFacing.getAxis() == targetFacing.getAxis())
			flipAxis |= EnumFlipAxis.X.mask;
		else
			flipAxis |= EnumFlipAxis.Z.mask;
		
		float yaw = getRotation(player, rotation, flipAxis);
		Vec3d pos = getPosition(player, sourceGatePos, targetGatePos.getPos(), rotation, targetFacing.getAxis()==Axis.Z ? ~flipAxis : flipAxis);
		int sourceDim = world.provider.getDimension();
		
		if (sourceDim == targetGatePos.getDimension()) {
			player.rotationYaw = yaw;
			player.setPositionAndUpdate(pos.x, pos.y, pos.z);
		}
		
		else {
			boolean flying = player.capabilities.isFlying;
			
			transferPlayerToDimension(player, targetGatePos.getDimension(), pos, yaw);
			
			player.capabilities.isFlying = flying;
			player.sendPlayerAbilities();
		}
		
		setMotion(player, rotation, motionVector);
	} */
	
	public static float getRotation(Entity player, float rotation, int flipAxis) {
		Vec3d lookVec = player.getLookVec();
		Vector2f lookVec2f = new Vector2f( (float)(lookVec.x), (float)(lookVec.z) );
		
		rotateAround00(lookVec2f, rotation, flipAxis);
		
		return (float) Math.toDegrees(MathHelper.atan2(lookVec2f.x, lookVec2f.y));		
	}
	
	public static void setMotion(Entity player, float rotation, Vector2f motionVec2f) {		
		if (motionVec2f != null) {		
			rotateAround00(motionVec2f, rotation, 0);
					
			player.motionX = motionVec2f.x;
			player.motionZ = motionVec2f.y;
			player.velocityChanged = true;
		}
	}
	
	public static Vec3d getPosition(Entity player, BlockPos sourceGatePos, BlockPos targetGatePos, float rotation, int flipAxis, float yDiff) {
		Vector2f sourceCenter = new Vector2f( sourceGatePos.getX()+0.5f, sourceGatePos.getZ()+0.5f );
		Vector2f destCenter = new Vector2f( targetGatePos.getX()+0.5f, targetGatePos.getZ()+0.5f );
		Vector2f playerPosition = new Vector2f( (float)(player.posX), (float)(player.posZ) );  
		
		translateTo00(sourceCenter, playerPosition);
		rotateAround00(playerPosition, rotation, flipAxis);				
		translateToDest(playerPosition, destCenter);
		
		float y = (float) (targetGatePos.getY() + ( player.posY - sourceGatePos.getY() )) + yDiff;
		return new Vec3d(playerPosition.x, y, playerPosition.y);
	}
	
	public static World getWorld(int dimension) {
		World world = DimensionManager.getWorld(0);
		
		if (dimension == 0)
			return world;
		
		return world.getMinecraftServer().getWorld(dimension);
	}

	public static boolean frontSide(EnumFacing sourceFacing, Vector2f motionVec) {
		Axis axis = sourceFacing.getAxis();
		AxisDirection direction = sourceFacing.getAxisDirection();
		float motion;
		
		if (axis == Axis.X)
			motion = motionVec.x;			
		else
			motion = motionVec.y;
				
		// If facing positive, then player should move negative
		if (direction == AxisDirection.POSITIVE)
			return motion <= 0;
		else
			return motion >= 0;
	}
	
}
