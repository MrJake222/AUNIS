package mrjake.aunis;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class AunisSoundEvents {
	public static SoundEvent dhdPress;
	public static SoundEvent dhdPressBRB;
	
	public static SoundEvent gateOpen;
	public static SoundEvent gateClose;
	
	public static SoundEvent ringRoll;
	public static SoundEvent chevronLockDHD;
	
	public static Map<BlockPos, PositionedSoundRecord> ringRollSoundMap = new HashMap<BlockPos, PositionedSoundRecord>();
	
	static {
		dhdPress = new SoundEvent( new ResourceLocation("aunis", "dhd_press") );
		dhdPressBRB = new SoundEvent( new ResourceLocation("aunis", "dhd_brb") );
		
		gateOpen = new SoundEvent( new ResourceLocation("aunis", "gate_open") );
		gateClose = new SoundEvent( new ResourceLocation("aunis", "gate_close") );
		
		ringRoll = new SoundEvent( new ResourceLocation("aunis", "ring_roll") );
		
		chevronLockDHD = new SoundEvent( new ResourceLocation("aunis", "chevron_lock_dhd") );
	}
}
