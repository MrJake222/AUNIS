package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.block.DHDBlock;
import mrjake.aunis.block.StargateBaseBlock;
import mrjake.aunis.tileentity.TileEntityRenderer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdateRequestToServer implements IMessage {
	public TileUpdateRequestToServer() {}	
	
	protected BlockPos tilePos;
	
	public TileUpdateRequestToServer(BlockPos pos) {
		tilePos = pos;
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
			Block block = world.getBlockState(message.tilePos).getBlock();
			
			if ( block instanceof StargateBaseBlock || block instanceof DHDBlock ) {				
				TileEntityRenderer te = (TileEntityRenderer) world.getTileEntity(message.tilePos);
				
				if (te != null)
					return new TileUpdatePacketToClient(te.getRendererState());
				
			}
			
			return null;
		}
		
	}
}
