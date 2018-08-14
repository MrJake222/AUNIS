package mrjake.aunis;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class AunisSoundEvents {
	public static SoundEvent dhdPress;
	public static SoundEvent dhdPressBRB;
	public static SoundEvent gateOpen;
	public static SoundEvent gateClose;
	
	static {
		dhdPress = new SoundEvent( new ResourceLocation("aunis", "dhd_press") );
		dhdPressBRB = new SoundEvent( new ResourceLocation("aunis", "dhd_brb") );
		gateOpen = new SoundEvent( new ResourceLocation("aunis", "gate_open") );
		gateClose = new SoundEvent( new ResourceLocation("aunis", "gate_close") );
	}
}
