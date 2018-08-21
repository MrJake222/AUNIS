package mrjake.aunis.packet.gate.teleportPlayer;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.AunisSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayWormholeSoundPacketToClient implements IMessage {
	public PlayWormholeSoundPacketToClient() {}
	
	public BlockPos pos;
	
	public PlayWormholeSoundPacketToClient(BlockPos targetGatePos) {
		pos = targetGatePos;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong( pos.toLong() );
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong( buf.readLong() );
	}

	
	public static class PlayWormholeSoundClientHandler implements IMessageHandler<PlayWormholeSoundPacketToClient, IMessage> {

		@Override
		public IMessage onMessage(PlayWormholeSoundPacketToClient message, MessageContext ctx) {
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				EntityPlayerSP player = Minecraft.getMinecraft().player;
				World world = player.getEntityWorld();
				
				world.playSound(player, message.pos, AunisSoundEvents.wormholeGo, SoundCategory.BLOCKS, 1.0f, 1.0f);
				
			});
			
			return null;
		}
		
	}
	

}
