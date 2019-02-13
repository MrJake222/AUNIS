package mrjake.aunis.packet.gate.tileUpdate;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.tileentity.TileEntity;
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
			
			if (world.isBlockLoaded(message.tilePos)) {
				TileEntity te = world.getTileEntity(message.tilePos);
				
				if (te != null) {
					if (te instanceof ITileEntityUpgradeable)
						return new TileUpdatePacketToClient(((ITileEntityRendered) te).getRendererState(), ((ITileEntityUpgradeable) te).getUpgradeRendererState());
					else
						return new TileUpdatePacketToClient(((ITileEntityRendered) te).getRendererState());
				}
				
			}
			
			return null;
		}
		
	}
}
