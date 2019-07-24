package mrjake.aunis.packet.gate.teleportPlayer;

import javax.vecmath.Vector2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisProps;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.stargate.teleportation.TeleportHelper;
import mrjake.aunis.tileentity.stargate.StargateBaseTile;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MotionToServer extends PositionedPacket {
	public MotionToServer() {}
	
	private int entityId;
	private float motionX;
	private float motionZ;
	
	public MotionToServer(int entityId, BlockPos pos, float motionX, float motionZ) {
		super(pos);
		
		this.entityId = entityId;
		this.motionX = motionX;
		this.motionZ = motionZ;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(entityId);
		
		buf.writeFloat(motionX);
		buf.writeFloat(motionZ);

	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		entityId = buf.readInt();
		
		motionX = buf.readFloat();
		motionZ = buf.readFloat();
	}

	
	public static class MotionServerHandler implements IMessageHandler<MotionToServer, IMessage> {

		@Override
		public IMessage onMessage(MotionToServer message, MessageContext ctx) {
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			world.addScheduledTask(() -> {
				
				EnumFacing sourceFacing = world.getBlockState(message.pos).getValue(AunisProps.FACING_HORIZONTAL);
				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.pos);
				
				Vector2f motionVector = new Vector2f(message.motionX, message.motionZ);
				
				if (TeleportHelper.frontSide(sourceFacing, motionVector)) {					
					gateTile.setMotionOfPassingEntity(message.entityId, motionVector);
					gateTile.teleportEntity(message.entityId);
				}
				
				else {
					gateTile.removeEntity(message.entityId);
				}
				
			});
			
			return null;
		}
		
	}
}