package mrjake.aunis.packet.gate.teleportPlayer;

import javax.vecmath.Vector2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.tileentity.StargateBaseTile;
import mrjake.aunis.tileentity.StargateBaseTile.TeleportPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MotionToServer implements IMessage {
	public MotionToServer() {}
	
	private int entityId;
	private BlockPos gatePos;
	private float motionX;
	private float motionZ;
	
	public MotionToServer(int entityId, BlockPos gatePos, float motionX, float motionZ) {
		this.entityId = entityId;
		this.gatePos = gatePos;
		this.motionX = motionX;
		this.motionZ = motionZ;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeLong( gatePos.toLong() );
		
		buf.writeFloat(motionX);
		buf.writeFloat(motionZ);

	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		gatePos = BlockPos.fromLong( buf.readLong() );
		
		motionX = buf.readFloat();
		motionZ = buf.readFloat();
	}

	
	public static class MotionServerHandler implements IMessageHandler<MotionToServer, IMessage> {

		@Override
		public IMessage onMessage(MotionToServer message, MessageContext ctx) {
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if ( world.getBlockState(message.gatePos).getBlock() instanceof StargateBaseBlock ) {
				world.addScheduledTask(() -> {
					
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.gatePos);
					TeleportPacket packet = gateTile.scheduledTeleportMap.get(message.entityId);
					
					packet.motionVector = new Vector2f(message.motionX, message.motionZ);
					gateTile.scheduledTeleportMap.put(message.entityId, packet);
					
					gateTile.teleportPlayer(message.entityId);
				});
			}
			
			return null;
		}
		
	}
}