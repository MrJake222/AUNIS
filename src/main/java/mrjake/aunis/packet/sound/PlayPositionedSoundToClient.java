package mrjake.aunis.packet.sound;

import io.netty.buffer.ByteBuf;
import mrjake.aunis.Aunis;
import mrjake.aunis.packet.PositionedPacket;
import mrjake.aunis.sound.AunisSoundHelper;
import mrjake.aunis.sound.EnumAunisPositionedSound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayPositionedSoundToClient extends PositionedPacket {
	public PlayPositionedSoundToClient() {}
	
	public EnumAunisPositionedSound soundEnum;
	
	public PlayPositionedSoundToClient(BlockPos pos, EnumAunisPositionedSound soundEnum) {
		super(pos);
		
		this.soundEnum = soundEnum;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		
		buf.writeInt(soundEnum.id);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		
		soundEnum = EnumAunisPositionedSound.valueOf(buf.readInt());
	}
	
	
	public static class PlayPositionedSoundClientHandler implements IMessageHandler<PlayPositionedSoundToClient, IMessage> {

		@Override
		public IMessage onMessage(PlayPositionedSoundToClient message, MessageContext ctx) {
			Aunis.proxy.addScheduledTask(ctx, () -> {
				AunisSoundHelper.playPositionedSound(message.soundEnum, message.pos, true);
				Aunis.info("playing sound: " + message.soundEnum + " at pos: " + message.pos);
			});
			
			return null;
		}
		
	}
}
