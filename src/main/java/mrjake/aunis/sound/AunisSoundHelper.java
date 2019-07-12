package mrjake.aunis.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class AunisSoundHelper {
	public static SoundEvent dhdPress;
	public static SoundEvent dhdPressBRB;
	
	public static SoundEvent gateOpen;
	public static SoundEvent gateClose;
	public static SoundEvent gateDialFail;
	
	public static SoundEvent chevronLockDHD;
	public static SoundEvent chevronIncoming;
	public static SoundEvent wormholeGo;
	
	public static SoundEvent wormholeFlicker;
	public static SoundEvent ringsTransport;
	public static SoundEvent ringsControllerButton;
	
	public static SoundEvent chevronShut;
	public static SoundEvent chevronOpen;
	public static SoundEvent chevronLocking;
	
	public static ResourceLocation ringRollStart;
	public static ResourceLocation ringRollLoop;
	public static ResourceLocation wormholeLoop;
		
	private static Map<EnumAunisPositionedSound, AunisSound> aunisSounds = new HashMap<>();
	private static List<AunisPositionedSound> aunisPositionedSounds = new ArrayList<>();
	
	public static void playPositionedSound(EnumAunisPositionedSound enumSound, BlockPos pos, boolean play) {
		// Mouse.setGrabbed(false);
		
		AunisSound sound = aunisSounds.get(enumSound);
		
		if (sound == null)
			return;
		
		AunisPositionedSound positionedSound = new AunisPositionedSound(sound, pos);
		
		int index = aunisPositionedSounds.indexOf(positionedSound);
		
		// Element found
		if (index >= 0)
			positionedSound = aunisPositionedSounds.get(index);
		else
			aunisPositionedSounds.add(positionedSound);
		
		if (play)
			positionedSound.playSound();
		else
			positionedSound.stopSound();
		
	}
	
	static {		
		dhdPress = new SoundEvent( new ResourceLocation("aunis", "dhd_press") );
		dhdPressBRB = new SoundEvent( new ResourceLocation("aunis", "dhd_brb") );
		
		gateOpen = new SoundEvent( new ResourceLocation("aunis", "gate_open") );
		gateClose = new SoundEvent( new ResourceLocation("aunis", "gate_close") );
		gateDialFail = new SoundEvent( new ResourceLocation("aunis", "gate_dial_fail") );
		
		chevronLockDHD = new SoundEvent( new ResourceLocation("aunis", "chevron_lock_dhd") );	
		chevronIncoming = new SoundEvent( new ResourceLocation("aunis", "chevron_incoming") );	
		wormholeGo = new SoundEvent( new ResourceLocation("aunis", "wormhole_go") );
		
		wormholeFlicker = new SoundEvent( new ResourceLocation("aunis", "wormhole_flicker") );
		ringsTransport = new SoundEvent( new ResourceLocation("aunis", "rings_transport") );
		ringsControllerButton = new SoundEvent( new ResourceLocation("aunis", "rings_controller_button") );
		
		chevronShut = new SoundEvent( new ResourceLocation("aunis", "chevron_shut") );
		chevronOpen = new SoundEvent( new ResourceLocation("aunis", "chevron_open") );
		chevronLocking = new SoundEvent( new ResourceLocation("aunis", "chevron_locking") );
		
		// ------------------------------------------------------------------------------------------------------------
		ringRollStart = new ResourceLocation("aunis", "ring_roll_start");
		ringRollLoop = new ResourceLocation("aunis", "ring_roll_loop");
		wormholeLoop = new ResourceLocation("aunis", "wormhole_loop");
		
		aunisSounds.put(EnumAunisPositionedSound.RING_ROLL_START, new AunisSound(AunisSoundHelper.ringRollStart, SoundCategory.AMBIENT, false));
		aunisSounds.put(EnumAunisPositionedSound.RING_ROLL_LOOP, new AunisSound(AunisSoundHelper.ringRollLoop, SoundCategory.AMBIENT, true));
		aunisSounds.put(EnumAunisPositionedSound.WORMHOLE, new AunisSound(AunisSoundHelper.wormholeLoop, SoundCategory.AMBIENT, true));
	}
	
	public static void playSound(WorldClient world, BlockPos pos, SoundEvent soundEvent, float volume) {
		world.playSound(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEvent, SoundCategory.AMBIENT, volume, 1.0f, false);
	}
}
