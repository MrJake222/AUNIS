package mrjake.aunis.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class AunisPositionedSound extends AunisSound {

	private BlockPos pos;
	private PositionedSoundRecord positionedSoundRecord;
	
	public AunisPositionedSound(ResourceLocation resourceLocation, SoundCategory soundCategory, boolean repeat, BlockPos pos) {
		super(resourceLocation, soundCategory, repeat);
		this.pos = pos;
	}

	public AunisPositionedSound(AunisSound sound, BlockPos pos) {
		super(sound.resourceLocation, sound.soundCategory, sound.repeat);
		this.pos = pos;
	}

	
	private PositionedSoundRecord getRecord() {		
		if (positionedSoundRecord == null)
			positionedSoundRecord = new PositionedSoundRecord(resourceLocation, soundCategory, 0.5f, 1.0f, repeat, 0, AttenuationType.LINEAR, pos.getX()+0.5f, pos.getY()+0.5f, pos.getZ()+0.5f);
				
		return positionedSoundRecord;
	}
	
	public void playSound() {
		SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
		
		if ( !soundHandler.isSoundPlaying(getRecord()) )
			soundHandler.playSound(getRecord());
	}
	
	public void stopSound() {
		SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
		
		if ( soundHandler.isSoundPlaying(getRecord()) )
			soundHandler.stopSound(getRecord());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AunisPositionedSound other = (AunisPositionedSound) obj;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}
}
