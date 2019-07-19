package mrjake.aunis.packet.update.renderer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.PositionedPlayerPacket;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RendererUpdateRequestToServer extends PositionedPlayerPacket {
	public RendererUpdateRequestToServer() {}	
		
	public RendererUpdateRequestToServer(BlockPos pos, EntityPlayer player) {
		super(pos, player);
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
	}	
	
	public static class TileUpdateServerHandler implements IMessageHandler<RendererUpdateRequestToServer, IMessage> {

		@Override
		public RendererUpdatePacketToClient onMessage(RendererUpdateRequestToServer message, MessageContext ctx) {
						
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				world.addScheduledTask(() -> {
					TileEntity te = world.getTileEntity(message.pos);
					
					if (te != null) {													
						if (te instanceof ITileEntityUpgradeable)
							message.respond(world, new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState(), ((ITileEntityUpgradeable) te).getUpgradeRendererState()));
						else
							message.respond(world, new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState()));
					}
				});
			}
			
			return null;
		}
		
	}
}
