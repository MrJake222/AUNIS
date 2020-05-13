package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.Aunis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public enum SoundPositionedEnum {
	MILKYWAY_RING_ROLL(0, "gate_milkyway_ring_roll", Aunis.soundCategory, false),
	WORMHOLE_LOOP(1, "wormhole_loop", Aunis.soundCategory, true),
	UNIVERSE_RING_ROLL(2, "gate_universe_roll", Aunis.soundCategory, false),
	BEAMER_LOOP(3, "beamer_loop", Aunis.soundCategory, true);
	
	public int id;
	public ResourceLocation resourceLocation;
	public SoundCategory soundCategory;
	public boolean repeat;
	
	SoundPositionedEnum(int id, String name, SoundCategory soundCategory, boolean repeat) {
		this.id = id;
		this.resourceLocation = new ResourceLocation(Aunis.ModID, name);
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
