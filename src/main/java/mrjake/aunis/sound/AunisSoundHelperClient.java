package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.math.BlockPos;

public class AunisSoundHelperClient {

	private static Map<BlockPos, Map<SoundPositionedEnum, PositionedSoundRecord>> positionedSoundRecordsMap = new HashMap<>();
	
	private static PositionedSoundRecord getSoundRecord(SoundPositionedEnum soundEnum, BlockPos pos) {
		return new PositionedSoundRecord(soundEnum.resourceLocation, soundEnum.soundCategory, 0.5f, 1.0f, soundEnum.repeat, 0, AttenuationType.LINEAR, pos.getX()+0.5f, pos.getY()+0.5f, pos.getZ()+0.5f);
	}
	
	public static void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play) {
		Map<SoundPositionedEnum, PositionedSoundRecord> soundRecordsMap = positionedSoundRecordsMap.get(pos);
		
		if (soundRecordsMap == null) {
			soundRecordsMap = new HashMap<>();
			positionedSoundRecordsMap.put(pos, soundRecordsMap);
		}
		
		PositionedSoundRecord soundRecord = soundRecordsMap.get(soundEnum);
		
		if (soundRecord == null) {
			soundRecord = getSoundRecord(soundEnum, pos);
			soundRecordsMap.put(soundEnum, soundRecord);
		}
			
		SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
		
		if (play) {
			if (!soundHandler.isSoundPlaying(soundRecord))
				soundHandler.playSound(soundRecord);
		}
		
		else
			Minecraft.getMinecraft().getSoundHandler().stopSound(soundRecord);
	}
}
