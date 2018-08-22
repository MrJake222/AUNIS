package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.tileentity.StargateBaseTile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StargateTileUpdateRequestToServer implements IMessage {
	public StargateTileUpdateRequestToServer() {}
	
	private BlockPos tilePos;
	
	public StargateTileUpdateRequestToServer(BlockPos tilePos) {
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
	
	
	public static class StargateTileUpdateServerHandler implements IMessageHandler<StargateTileUpdateRequestToServer, StargateTileUpdatePacketToClient> {

		@Override
		public StargateTileUpdatePacketToClient onMessage(StargateTileUpdateRequestToServer message, MessageContext ctx) {
			
			Aunis.info("Received StargateTileUpdateRequestToServer: tilePos:"+message.tilePos);
			
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if ( world.getBlockState(message.tilePos).getBlock() instanceof StargateBaseBlock ) {				
				StargateBaseTile gateTile = (StargateBaseTile) world.getTileEntity(message.tilePos);
				
				if (gateTile != null)
					return new StargateTileUpdatePacketToClient(gateTile.getRendererState(), gateTile.getLinkedDHD());
			}
			
			return null;
		}
		
	}
}
