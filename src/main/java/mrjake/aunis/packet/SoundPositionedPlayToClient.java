package mrjake.aunis.packet;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.sound.SoundPositionedEnum;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SoundPositionedPlayToClient extends PositionedPacket {
	public SoundPositionedPlayToClient() {}
	
	public SoundPositionedEnum soundEnum;
	public boolean play;
	
	public SoundPositionedPlayToClient(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		super(pos);
		
		this.soundEnum = soundEnum;
		this.play = play;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(soundEnum.id);
		buf.writeBoolean(play);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		soundEnum = SoundPositionedEnum.valueOf(buf.readInt());
		play = buf.readBoolean();
	}
	
	
	public static class PlayPositionedSoundClientHandler implements IMessageHandler<SoundPositionedPlayToClient, IMessage> {

		@Override
		public IMessage onMessage(SoundPositionedPlayToClient message, MessageContext ctx) {
			Aunis.proxy.addScheduledTaskClientSide(() -> {
				Aunis.proxy.playPositionedSoundClientSide(message.pos, message.soundEnum, message.play);
			});
			
			return null;
		}
		
	}
}
