package mrjake.aunis.packet.transportrings;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.tileentity.TransportRingsTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class StartRingsAnimationToClient extends PositionedPacket {
	public StartRingsAnimationToClient() {}
	
	public long animationStart;
	
	public StartRingsAnimationToClient(BlockPos pos, long animationStart) {
		super(pos);
		
		this.animationStart = animationStart;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeLong(animationStart);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		animationStart = buf.readLong();
	}
	
	public static class StartRingsAnimationToClientHandler implements IMessageHandler<StartRingsAnimationToClient, IMessage> {

		@Override
		public IMessage onMessage(StartRingsAnimationToClient message, MessageContext ctx) {
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = Aunis.proxy.getPlayerInMessageHandler(ctx);
				World world = player.getEntityWorld();
				
				TransportRingsTile ringsTile = (TransportRingsTile) world.getTileEntity(message.pos);
				ringsTile.getTransportRingsRenderer().animationStart(message.animationStart);		
				
				AunisSoundHelper.playSound((WorldClient) world, message.pos.add(0, 4, 0), AunisSoundHelper.ringsTransport);
			});
						
			return null;
		}
		
	}
}
