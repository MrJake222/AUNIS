package mrjake.aunis.packet.gate.teleportPlayer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UnlockPlayerToServer implements IMessage {
	public UnlockPlayerToServer() {}
	
	private int entityID;
	private BlockPos sourceGate;
	
	public UnlockPlayerToServer(int entityID, BlockPos sourceGate) {
		this.entityID = entityID;
		this.sourceGate = sourceGate;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityID);
		buf.writeLong( sourceGate.toLong() );
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		entityID = buf.readInt();
		sourceGate = BlockPos.fromLong( buf.readLong() );
	}
	
	
	public static class UnlockPlayerHandler implements IMessageHandler<UnlockPlayerToServer, IMessage> {

		@Override
		public IMessage onMessage(UnlockPlayerToServer message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			World world = player.getEntityWorld();
			
			player.getServerWorld().addScheduledTask(() -> {
				
				if ( world.isBlockLoaded(message.sourceGate) ) {
					StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.sourceGate);
					
					gateTile.unlockPlayer(message.entityID);
				}
				
			});
			
			return null;
		}
		
	}
}
