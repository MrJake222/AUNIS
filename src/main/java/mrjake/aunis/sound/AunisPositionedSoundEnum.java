package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public enum AunisPositionedSoundEnum {
	RING_ROLL(0, new ResourceLocation(Aunis.ModID, "ring_roll"), SoundCategory.AMBIENT, false),
	WORMHOLE(1, new ResourceLocation(Aunis.ModID, "wormhole_loop"), SoundCategory.AMBIENT, true);
	
	public int id;
	public ResourceLocation resourceLocation;
	public SoundCategory soundCategory;
	public boolean repeat;
	
	AunisPositionedSoundEnum(int id, ResourceLocation resourceLocation, SoundCategory soundCategory, boolean repeat) {
		this.id = id;
		this.resourceLocation = resourceLocation;
		this.soundCategory = soundCategory;
		this.repeat = repeat;
	}
	
	private static Map<Integer, AunisPositionedSoundEnum> ID_MAP = new HashMap<Integer, AunisPositionedSoundEnum>(values().length);
	
	static {
		for (AunisPositionedSoundEnum positionedSound : values()) {
			ID_MAP.put(positionedSound.id, positionedSound);
		}
	}
	
	public static AunisPositionedSoundEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
}
