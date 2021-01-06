package mrjake.aunis.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import mrjake.aunis.Aunis;
import mrjake.aunis.stargate.power.StargateEnergyRequired;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class StargateDimensionConfig {
	
	private static final Map<String, StargateDimensionConfigEntry> DEFAULTS_MAP = new HashMap<String, StargateDimensionConfigEntry>();
	
	static {
		DEFAULTS_MAP.put("overworld", new StargateDimensionConfigEntry(0, 0, "netherOv"));
		DEFAULTS_MAP.put("the_nether", new StargateDimensionConfigEntry(3686400, 1600, "netherOv"));
		DEFAULTS_MAP.put("the_end", new StargateDimensionConfigEntry(5529600, 2400, null));
		DEFAULTS_MAP.put("moon.moon", new StargateDimensionConfigEntry(7372800, 3200, null));
		DEFAULTS_MAP.put("planet.mars", new StargateDimensionConfigEntry(11059200, 4800, null));
		DEFAULTS_MAP.put("planet.venus", new StargateDimensionConfigEntry(12288000, 5334, null));
		DEFAULTS_MAP.put("planet.asteroids", new StargateDimensionConfigEntry(14745600, 6400, null));
	}
	
	private static File dimensionConfigFile;
	private static Map<String, StargateDimensionConfigEntry> dimensionStringMap;
	private static Map<DimensionType, StargateDimensionConfigEntry> dimensionMap;
	
	public static StargateEnergyRequired getCost(DimensionType from, DimensionType to) {
		StargateDimensionConfigEntry reqFrom = dimensionMap.get(from);
		StargateDimensionConfigEntry reqTo = dimensionMap.get(to);
		
		if (reqFrom == null || reqTo == null) {
			Aunis.logger.error("Tried to get a cost of a non-existing dimension. This is a bug.");
			return new StargateEnergyRequired(0, 0);
		}
		
		int energyToOpen = Math.abs(reqFrom.energyToOpen - reqTo.energyToOpen);
		int keepAlive = Math.abs(reqFrom.keepAlive - reqTo.keepAlive);
		
		return new StargateEnergyRequired(energyToOpen, keepAlive);
	}
		
	public static boolean isGroupEqual(DimensionType from, DimensionType to) {
		StargateDimensionConfigEntry reqFrom = dimensionMap.get(from);
		StargateDimensionConfigEntry reqTo = dimensionMap.get(to);
		
		if (reqFrom == null || reqTo == null) {
			Aunis.logger.error("Tried to perform a group check for a non-existing dimension. This is a bug.");
			return false;
		}
		
		Aunis.info("[from="+from+", to="+to+", isGroupEqual="+reqFrom.isGroupEqual(reqTo)+"]");
		
		return reqFrom.isGroupEqual(reqTo);
	}
	
	public static boolean netherOverworld8thSymbol() {
		return !isGroupEqual(DimensionType.OVERWORLD, DimensionType.NETHER);
	}
	
	public static void load(File modConfigDir) {
		dimensionMap = null;
		dimensionConfigFile = new File(modConfigDir, "aunis_dimensions.json");
		
		try {			
			Type typeOfHashMap = new TypeToken<Map<String, StargateDimensionConfigEntry>>() { }.getType();
			dimensionStringMap = new GsonBuilder().create().fromJson(new FileReader(dimensionConfigFile), typeOfHashMap);
		}
		
		catch (FileNotFoundException exception) {
			dimensionStringMap = new HashMap<String, StargateDimensionConfigEntry>();
		}
	}
	
	public static void update() throws IOException {
		if (dimensionMap == null) {
			dimensionMap = new HashMap<DimensionType, StargateDimensionConfigEntry>();
			
			for (String dimName : dimensionStringMap.keySet()) {
				try {
					dimensionMap.put(DimensionType.byName(dimName), dimensionStringMap.get(dimName));
				}
				
				catch (IllegalArgumentException ex) {
					// Probably removed a mod
					Aunis.info("DimensionType not found: " + dimName);
				}
			}
		}
		
		int originalSize = dimensionMap.size();
		
		for (DimensionType dimType : DimensionManager.getRegisteredDimensions().keySet()) {
			if (!dimensionMap.containsKey(dimType)) {
				// Biomes O' Plenty Nether fix
				if (dimType.getName().equals("Nether"))
					dimType = DimensionType.NETHER;
				
				if (DEFAULTS_MAP.containsKey(dimType.getName()))
					dimensionMap.put(dimType, DEFAULTS_MAP.get(dimType.getName()));
				else
					dimensionMap.put(dimType, new StargateDimensionConfigEntry(0, 0, null));
			}
		}
				
		if (originalSize != dimensionMap.size()) {
			FileWriter writer = new FileWriter(dimensionConfigFile);
			
			dimensionStringMap.clear();
			for (DimensionType dimType : dimensionMap.keySet()) {
				dimensionStringMap.put(dimType.getName(), dimensionMap.get(dimType));
			}
			
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(dimensionStringMap));
			writer.close();
		}
	}
}
