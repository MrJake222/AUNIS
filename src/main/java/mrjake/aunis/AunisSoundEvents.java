package mrjake.aunis;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.sound.Sound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
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
	
	public static SoundEvent chevronLockDHD;
	public static SoundEvent chevronIncoming;
	public static SoundEvent wormholeGo;
	
	public static ResourceLocation ringRoll;
	public static ResourceLocation wormholeLoop;
	
	public static Map<BlockPos, Sound> ringRollSoundMap;
	public static Map<BlockPos, Sound> wormholeSoundMap;
	public static Map<String, Map<BlockPos, Sound>> soundNameMap;
	
	public static void playPositionedSound(String mapName, BlockPos pos, boolean play) {
		Map<BlockPos, Sound> soundMap = soundNameMap.get(mapName);
		
		if (soundMap != null) {
			Sound sound = soundMap.get(pos);	
			
			if (sound == null) {
				sound = new Sound(mapName, pos);
				soundMap.put(pos, sound);
				soundNameMap.put(mapName, soundMap);
			}
			
			SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
			
			if ( play && !soundHandler.isSoundPlaying(sound.getSound()) )
				soundHandler.playSound( sound.getSound() );
			
			else if ( !play && soundHandler.isSoundPlaying(sound.getSound()) )
				soundHandler.stopSound(sound.getSound());
		}
	}
	
	static {
		ringRollSoundMap = new HashMap<BlockPos, Sound>();
		wormholeSoundMap = new HashMap<BlockPos, Sound>();
		
		soundNameMap = new HashMap<String, Map<BlockPos, Sound>>();
		soundNameMap.put("ringRoll", ringRollSoundMap);
		soundNameMap.put("wormhole", wormholeSoundMap);
		
		dhdPress = new SoundEvent( new ResourceLocation("aunis", "dhd_press") );
		dhdPressBRB = new SoundEvent( new ResourceLocation("aunis", "dhd_brb") );
		
		gateOpen = new SoundEvent( new ResourceLocation("aunis", "gate_open") );
		gateClose = new SoundEvent( new ResourceLocation("aunis", "gate_close") );
		gateDialFail = new SoundEvent( new ResourceLocation("aunis", "gate_dial_fail") );
		
		chevronLockDHD = new SoundEvent( new ResourceLocation("aunis", "chevron_lock_dhd") );	
		chevronIncoming = new SoundEvent( new ResourceLocation("aunis", "chevron_incoming") );	
		wormholeGo = new SoundEvent( new ResourceLocation("aunis", "wormhole_go") );
		
		ringRoll = new ResourceLocation("aunis", "ring_roll");
		wormholeLoop = new ResourceLocation("aunis", "wormhole_loop");
	}
	
	public static void playSound(World world, BlockPos pos, SoundEvent soundEvent) {
		world.playSound(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
	}
}
