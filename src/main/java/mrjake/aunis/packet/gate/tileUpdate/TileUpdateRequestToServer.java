package mrjake.aunis.packet.gate.tileUpdate;

import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TileUpdateRequestToServer extends PositionedPacket {
	public TileUpdateRequestToServer() {}	
	
	
	public TileUpdateRequestToServer(BlockPos pos) {
		super(pos);
	}
	
	public static class TileUpdateServerHandler implements IMessageHandler<TileUpdateRequestToServer, TileUpdatePacketToClient> {

		@Override
		public TileUpdatePacketToClient onMessage(TileUpdateRequestToServer message, MessageContext ctx) {
						
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				TileEntity te = world.getTileEntity(message.pos);
				
				if (te != null) {
					if (te instanceof ITileEntityUpgradeable)
						return new TileUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState(), ((ITileEntityUpgradeable) te).getUpgradeRendererState());
					else
						return new TileUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState());
				}
				
			}
			
			return null;
		}
		
	}
}
