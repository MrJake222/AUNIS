package mrjake.aunis.sound;

import mrjake.aunis.Aunis;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class Sound {
	private boolean repeat;
	private ResourceLocation resourceLocation;
	
	private PositionedSoundRecord positionedSoundRecord;
	
	public Sound(String name, BlockPos pos) {
		this.repeat = Sounds.soundRepeatMap.get(name);
		this.resourceLocation = Sounds.soundResourceMap.get(name);
		
		Aunis.info("Creating sound "+name+" with resource "+resourceLocation);
		
		positionedSoundRecord = new PositionedSoundRecord(this.resourceLocation, SoundCategory.BLOCKS, 1.0f, 1.0f, this.repeat, 0, AttenuationType.LINEAR, pos.getX()+0.5f, pos.getY()+0.5f, pos.getZ()+0.5f);
	}
	
	public PositionedSoundRecord getSound() {
		return positionedSoundRecord;
	}
}
