package mrjake.aunis.stargate;

import javax.vecmath.Vector2f;

import org.lwjgl.util.vector.Matrix2f;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TeleportHelper {
	
	private static void translateTo00(Vector2f center, Vector2f v) {
		v.x -= center.x;
		v.y -= center.y;
	}
	
	public static void rotateAround00(Vector2f v, float rotation, String flip) {
		Matrix2f m = new Matrix2f();
		Matrix2f p = new Matrix2f();
		
		float sin = MathHelper.sin(rotation);
		float cos = MathHelper.cos(rotation);
		
		if (flip != null) {
			if ( flip.equals("x") )
				v.x *= -1;
			else
				v.y *= -1;
		}
		
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
	
	public static void teleportServer(EntityPlayer player, BlockPos sourceGatePos, BlockPos targetGatePos, float rotation, String sourceAxisName, Vector2f motionVector) {
		setRotation(player, rotation, sourceAxisName);
		teleport(player, sourceGatePos, targetGatePos, rotation, sourceAxisName);
		setMotion(player, rotation, sourceAxisName, motionVector);
	}
	
	public static void setRotation(EntityPlayer player, float rotation, String sourceAxisName) {
		Vec3d lookVec = player.getLookVec();
		Vector2f lookVec2f = new Vector2f( (float)(lookVec.x), (float)(lookVec.z) );
		
		rotateAround00(lookVec2f, rotation, sourceAxisName);
		
		player.rotationYaw = (float) Math.toDegrees(MathHelper.atan2(lookVec2f.x, lookVec2f.y));		
	}
	
	public static void setMotion(EntityPlayer player, float rotation, String sourceAxisName, Vector2f motionVec2f) {		
		// Vector2f motionVec2f = new Vector2f( (float)(player.motionX), (float)(player.motionZ) );
		rotateAround00(motionVec2f, rotation, null);
		
		player.motionX = motionVec2f.x;
		player.motionZ = motionVec2f.y;
		player.velocityChanged = true;
	}
	
	public static void teleport(EntityPlayer player, BlockPos sourceGatePos, BlockPos targetGatePos, float rotation, String sourceAxisName) {
		Vector2f sourceCenter = new Vector2f( sourceGatePos.getX()+0.5f, sourceGatePos.getZ()+0.5f );
		Vector2f destCenter = new Vector2f( targetGatePos.getX()+0.5f, targetGatePos.getZ()+0.5f );
		Vector2f playerPosition = new Vector2f( (float)(player.posX), (float)(player.posZ) );  
		
		translateTo00(sourceCenter, playerPosition);
		rotateAround00(playerPosition, rotation, sourceAxisName);				
		translateToDest(playerPosition, destCenter);
		
		float y = (float) (targetGatePos.getY() + ( player.posY - sourceGatePos.getY() ));
		player.setPositionAndUpdate(playerPosition.x, y, playerPosition.y);
	}
	
}
