package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public enum SoundPositionedEnum {
	MILKYWAY_RING_ROLL(0, new ResourceLocation(Aunis.ModID, "gate_milkyway_ring_roll"), SoundCategory.AMBIENT, false),
	WORMHOLE_LOOP(1, new ResourceLocation(Aunis.ModID, "wormhole_loop"), SoundCategory.AMBIENT, true);
	
	public int id;
	public ResourceLocation resourceLocation;
	public SoundCategory soundCategory;
	public boolean repeat;
	
	SoundPositionedEnum(int id, ResourceLocation resourceLocation, SoundCategory soundCategory, boolean repeat) {
		this.id = id;
		this.resourceLocation = resourceLocation;
		this.soundCategory = soundCategory;
		this.repeat = repeat;
	}
	
	private static Map<Integer, SoundPositionedEnum> ID_MAP = new HashMap<Integer, SoundPositionedEnum>(values().length);
	
	static {
		for (SoundPositionedEnum positionedSound : values()) {
			ID_MAP.put(positionedSound.id, positionedSound);
		}
	}
	
	public static SoundPositionedEnum valueOf(int id) {
		return ID_MAP.get(id);
	}
}
