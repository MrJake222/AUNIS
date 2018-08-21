package mrjake.aunis.packet.gate.teleportPlayer;

import javax.vecmath.Vector2f;

import org.lwjgl.util.vector.Matrix2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.AunisPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TeleportPlayerToClient implements IMessage {
	public TeleportPlayerToClient() {}
	
	private BlockPos sourceGatePos;
	private BlockPos targetGatePos;
	private float rotation;
	
	private String sourceAxisName;
	
	public TeleportPlayerToClient(BlockPos source, BlockPos target, float rotation, EnumFacing.Axis sourceAxis) {
		sourceGatePos = source;
		targetGatePos = target;
		
		this.rotation = rotation;
		this.sourceAxisName = sourceAxis.getName();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( sourceGatePos.toLong() );
		buf.writeLong( targetGatePos.toLong() );
		
		buf.writeFloat(rotation);
		
		if ( sourceAxisName.equals("x") )
			buf.writeByte(0);
		else
			buf.writeByte(1);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		sourceGatePos = BlockPos.fromLong( buf.readLong() );
		targetGatePos = BlockPos.fromLong( buf.readLong() );
		
		rotation = buf.readFloat();
		
		if (buf.readByte() == 0)
			sourceAxisName = "x";
		else
			sourceAxisName = "z";
	}

	
	public static class TeleportPlayerToClientHandler implements IMessageHandler<TeleportPlayerToClient, IMessage> {

		private void translateTo00(Vector2f center, Vector2f v) {
			v.x -= center.x;
			v.y -= center.y;
		}
		
		private void rotateAround00(Vector2f v, float rotation, String flip) {
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
		
		private void translateToDest(Vector2f v, Vector2f dest) {
			v.x += dest.x;
			v.y += dest.y;
		}
		
		@Override
		public IMessage onMessage(TeleportPlayerToClient message, MessageContext ctx) {
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = Minecraft.getMinecraft().player;
				Vec3d lookVec = player.getLookVec();
				Vector2f lookVec2f = new Vector2f( (float)(lookVec.x), (float)(lookVec.z) );
				
				rotateAround00(lookVec2f, message.rotation, message.sourceAxisName);
				
				player.rotationYaw = (float) Math.toDegrees( MathHelper.atan2(lookVec2f.x, lookVec2f.y) );
				
				// ------------------------------------------------------------------------
				
				Vector2f sourceCenter = new Vector2f( message.sourceGatePos.getX()+0.5f, message.sourceGatePos.getZ()+0.5f );
				Vector2f destCenter = new Vector2f( message.targetGatePos.getX()+0.5f, message.targetGatePos.getZ()+0.5f );
				Vector2f playerPosition = new Vector2f( (float)(player.posX), (float)(player.posZ) );  
				
				translateTo00(sourceCenter, playerPosition);
				rotateAround00(playerPosition, message.rotation, message.sourceAxisName);				
				translateToDest(playerPosition, destCenter);
				
				// ------------------------------------------------------------------------
				
				Vector2f motionVec2f = new Vector2f( (float)(player.motionX), (float)(player.motionZ) );
				rotateAround00(motionVec2f, message.rotation, null);
				
				player.motionX = motionVec2f.x;
				player.motionZ = motionVec2f.y;
				
				// ------------------------------------------------------------------------
				
				float y = (float) (message.targetGatePos.getY() + ( player.posY - message.sourceGatePos.getY() ));
				player.setPositionAndUpdate(playerPosition.x, y, playerPosition.y);
				
				//TeleportationHelper.teleportClient(player, message.sourceGatePos, message.targetGatePos, message.rotation, message.sourceAxisName);
				
				// Player teleported, unlock it on the server
				AunisPacketHandler.INSTANCE.sendToServer( new UnlockPlayerToServer(player.getEntityId(), message.sourceGatePos) );
			});
			
			return null;
		}
		
	}
	
}
