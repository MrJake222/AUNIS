package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdateRequestToServer implements IMessage {
	public TileUpdateRequestToServer() {}
	
	private BlockPos tilePos;
	
	public TileUpdateRequestToServer(BlockPos tilePos) {
		this.tilePos = tilePos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( tilePos.toLong() );		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tilePos = BlockPos.fromLong( buf.readLong() );		
	}
	
	
	public static class TileUpdateServerHandler implements IMessageHandler<TileUpdateRequestToServer, TileUpdatePacketToClient> {

		@Override
		public TileUpdatePacketToClient onMessage(TileUpdateRequestToServer message, MessageContext ctx) {
			
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			
			if ( world.isBlockLoaded(message.tilePos) ) {				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.tilePos);
				
				if (gateTile != null)					
					return new TileUpdatePacketToClient(message.tilePos, gateTile.getLinkedDHD());
			}
			
			return null;
		}
		
	}
}
