package mrjake.aunis.packet.gate.teleportPlayer;

import javax.vecmath.Vector2f;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisProps;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.packet.PositionedPacket;
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
			
			if ( world.getBlockState(message.pos).getBlock() instanceof StargateBaseBlock ) {
				world.addScheduledTask(() -> {
					
					EnumFacing sourceFacing = world.getBlockState(message.pos).getValue(AunisProps.FACING_HORIZONTAL);
					
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.pos);
					
					Vector2f motionVector = new Vector2f(message.motionX, message.motionZ);
					
					if (TeleportHelper.frontSide(sourceFacing, motionVector)) {
						gateTile.scheduledTeleportMap.put(message.entityId, gateTile.scheduledTeleportMap.get(message.entityId).setMotion(motionVector));
						
						world.playSound(null, message.pos, AunisSoundHelper.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
						gateTile.teleportEntity(message.entityId);
					}
					
					else {
//						((EntityPlayerMP)world.getEntityByID(message.entityId)).onKillCommand();
						gateTile.removeEntityFromTeleportList(message.entityId);
					}
					
				});
			}
			
			return null;
		}
		
	}
}