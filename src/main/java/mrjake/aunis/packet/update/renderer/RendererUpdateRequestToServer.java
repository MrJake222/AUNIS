package mrjake.aunis.packet.update.renderer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.packet.AunisPacketHandler;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.tesr.ITileEntityUpgradeable;
import mrjake.aunis.tileentity.ITileEntityRendered;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RendererUpdateRequestToServer extends PositionedPacket {
	public RendererUpdateRequestToServer() {}	
	
	public int entityID;
	
	public RendererUpdateRequestToServer(BlockPos pos, EntityPlayer player) {
		super(pos);
		
		this.entityID = player.getEntityId();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(entityID);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		entityID = buf.readInt();
	}	
	
	public static class TileUpdateServerHandler implements IMessageHandler<RendererUpdateRequestToServer, IMessage> {

		@Override
		public RendererUpdatePacketToClient onMessage(RendererUpdateRequestToServer message, MessageContext ctx) {
						
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			
			if (world.isBlockLoaded(message.pos)) {
				world.addScheduledTask(() -> {
					TileEntity te = world.getTileEntity(message.pos);
					
					if (te != null) {
						Entity entity = world.getEntityByID(message.entityID);
						
						if (entity instanceof EntityPlayerMP) {
							RendererUpdatePacketToClient packet;
							
							if (te instanceof ITileEntityUpgradeable)
								packet = new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState(), ((ITileEntityUpgradeable) te).getUpgradeRendererState());
							else
								packet = new RendererUpdatePacketToClient(message.pos, ((ITileEntityRendered) te).getRendererState());
							
							AunisPacketHandler.INSTANCE.sendTo(packet, (EntityPlayerMP) entity);	
						}
					}
				});
			}
			
			return null;
		}
		
	}
}
