package mrjake.aunis.sound;

import java.util.HashMap;
import java.util.Map;

import mrjake.aunis.AunisSoundEvents;
import net.minecraft.util.ResourceLocation;

public class Sounds {
	public static Map<String, Boolean> soundRepeatMap = new HashMap<String, Boolean>();
	public static Map<String, ResourceLocation> soundResourceMap = new HashMap<String, ResourceLocation>();
	
	static {
		soundRepeatMap.put("ringRoll", false);
		soundRepeatMap.put("wormhole", true);
		
		soundResourceMap.put("ringRoll", AunisSoundEvents.ringRoll);
		soundResourceMap.put("wormhole", AunisSoundEvents.wormholeLoop);
	}
}
