package mrjake.aunis.item.dialer;

import mrjake.aunis.util.EnumKeyInterface;
import mrjake.aunis.util.EnumKeyMap;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum UniverseDialerMode implements EnumKeyInterface<Byte> {
	NEARBY(0, "item.aunis.universe_dialer.mode_scan", "nearby"),
	MEMORY(1, "item.aunis.universe_dialer.mode_saved", "saved");
	
	public final byte id;
	public final String translationKey;
	public final String tagName;

	private UniverseDialerMode(int id, String translationKey, String tagName) {
		this.tagName = tagName;
		this.id = (byte) id;
		this.translationKey = translationKey;
	}

	public UniverseDialerMode other() {
		switch (this) {
			case NEARBY: return MEMORY;
			case MEMORY: return NEARBY;
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public String localize() {
		return I18n.format(translationKey);
	}
	
	@Override
	public Byte getKey() {
		return id;
	}
	
	private static final EnumKeyMap<Byte, UniverseDialerMode> ID_MAP = new EnumKeyMap<Byte, UniverseDialerMode>(values());
	
	public static UniverseDialerMode valueOf(byte id) {
		return ID_MAP.valueOf(id);
	}
}