package mrjake.aunis.packet.update.renderer;

import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RendererUpdateRequestToServer extends PositionedPacket {
	public RendererUpdateRequestToServer() {}	
	
	
	public RendererUpdateRequestToServer(BlockPos pos) {
		super(pos);
	}
	
	public static class TileUpdateServerHandler implements IMessageHandler<RendererUpdateRequestToServer, RendererUpdatePacketToClient> {

		@Override
		public RendererUpdatePacketToClient onMessage(RendererUpdateRequestToServer message, MessageContext ctx) {
						
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				TileEntity te = world.getTileEntity(message.pos);
				
				if (te != null) {
					if (te instanceof ITileEntityUpgradeable)
						return new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState(), ((ITileEntityUpgradeable) te).getUpgradeRendererState());
					else
						return new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState());
				}
				
			}
			
			return null;
		}
		
	}
}
