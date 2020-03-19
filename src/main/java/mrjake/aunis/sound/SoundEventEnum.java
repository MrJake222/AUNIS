package mrjake.aunis.sound;

import mrjake.aunis.Aunis;
import mrjake.aunis.util.EnumKeyMap;
import mrjake.aunis.util.EnumKeyInterface;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public enum SoundEventEnum implements EnumKeyInterface<Integer> {
	
	// ----------------------------------------------------------
	// Stargate - General
	
	WORMHOLE_GO(0, "wormhole_go"),
	WORMHOLE_FLICKER(1, "wormhole_flicker"),
	
	
	// ----------------------------------------------------------
	// Stargate - Milky Way
	
	DHD_MILKYWAY_PRESS(10, "dhd_milkyway_press"),
	DHD_MILKYWAY_PRESS_BRB(11, "dhd_milkyway_press_brb"),
	
	GATE_MILKYWAY_OPEN(12, "gate_milkyway_open"), // not working
	GATE_MILKYWAY_CLOSE(13, "gate_milkyway_close"),
	GATE_MILKYWAY_DIAL_FAILED(14, "gate_milkyway_dial_fail"),
	GATE_MILKYWAY_DIAL_FAILED_COMPUTER(15, "gate_milkyway_dial_fail_computer"),
	GATE_MILKYWAY_INCOMING(16, "gate_milkyway_incoming"),
	
	GATE_MILKYWAY_CHEVRON_SHUT(17, "gate_milkyway_chevron_shut"),
	GATE_MILKYWAY_CHEVRON_OPEN(18, "gate_milkyway_chevron_open"),
	GATE_ORLIN_DIAL(19, "gate_orlin_dial"),
	
	
	// ----------------------------------------------------------
	// Ring transporter
	
	RINGS_TRANSPORT(100, "rings_transport"),
	RINGS_CONTROLLER_BUTTON(101, "rings_controller_button");
	
	
	// ----------------------------------------------------------
	
	private int id;
	public SoundEvent soundEvent;
	
	SoundEventEnum(int id, String name) {
		this.id = id;
		this.soundEvent = createSoundEvent(name);
	}
	
	@Override
	public Integer getKey() {
		return id;
	}
	
	private static EnumKeyMap<Integer, SoundEventEnum> idMap = new EnumKeyMap<Integer, SoundEventEnum>(values());
	
	public static SoundEventEnum valueOf(int id) {
		return idMap.valueOf(id);
	}
	
	private static SoundEvent createSoundEvent(String name) {
		ResourceLocation resourceLocation = new ResourceLocation(Aunis.ModID, name);
		return new SoundEvent(resourceLocation).setRegistryName(resourceLocation);
	}
}
