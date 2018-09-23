package mrjake.aunis.packet.gate.teleportPlayer;

import javax.vecmath.Vector2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.BlockFaced;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.stargate.TeleportHelper;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
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
					
					EnumFacing sourceFacing = world.getBlockState(message.gatePos).getValue(BlockFaced.FACING);
					
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.gatePos);
					
					Vector2f motionVector = new Vector2f(message.motionX, message.motionZ);
					
					if (TeleportHelper.frontSide(sourceFacing, motionVector)) {
						gateTile.scheduledTeleportMap.put(message.entityId, gateTile.scheduledTeleportMap.get(message.entityId).setMotion(motionVector));
						
						world.playSound(null, message.gatePos, AunisSoundHelper.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
						gateTile.teleportEntity(message.entityId);
					}
					
					/*else {
						((EntityPlayerMP)world.getEntityByID(message.entityId)).onKillCommand();
						gateTile.removeEntityFromTeleportList(message.entityId);
					}*/
					
				});
			}
			
			return null;
		}
		
	}
}