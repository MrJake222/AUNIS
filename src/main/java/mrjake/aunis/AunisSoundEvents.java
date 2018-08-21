package mrjake.aunis;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AunisSoundEvents {
	public static SoundEvent dhdPress;
	public static SoundEvent dhdPressBRB;
	
	public static SoundEvent gateOpen;
	public static SoundEvent gateClose;
	public static SoundEvent gateDialFail;
	
	public static SoundEvent ringRoll;
	public static SoundEvent chevronLockDHD;
	
	public static SoundEvent chevronIncoming;
	public static SoundEvent wormholeGo;
	
	public static Map<BlockPos, PositionedSoundRecord> ringRollSoundMap;
	
	static {		
		ringRollSoundMap = new HashMap<BlockPos, PositionedSoundRecord>();
		
		dhdPress = new SoundEvent( new ResourceLocation("aunis", "dhd_press") );
		dhdPressBRB = new SoundEvent( new ResourceLocation("aunis", "dhd_brb") );
		
		gateOpen = new SoundEvent( new ResourceLocation("aunis", "gate_open") );
		gateClose = new SoundEvent( new ResourceLocation("aunis", "gate_close") );
		gateDialFail = new SoundEvent( new ResourceLocation("aunis", "gate_dial_fail") );
		
		ringRoll = new SoundEvent( new ResourceLocation("aunis", "ring_roll") );
		chevronLockDHD = new SoundEvent( new ResourceLocation("aunis", "chevron_lock_dhd") );	
		
		chevronIncoming = new SoundEvent( new ResourceLocation("aunis", "chevron_incoming") );	
		wormholeGo = new SoundEvent( new ResourceLocation("aunis", "wormhole_go") );
	}
	
	public static void playSound(World world, BlockPos pos, SoundEvent soundEvent) {
		world.playSound(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
	}
}
