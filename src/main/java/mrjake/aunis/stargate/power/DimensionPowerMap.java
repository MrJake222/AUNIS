package mrjake.aunis.stargate.power;

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
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class DimensionPowerMap {
	
	private static final Map<String, StargateEnergyRequired> DEFAULTS_MAP = new HashMap<String, StargateEnergyRequired>();
	
	static {
		DEFAULTS_MAP.put("overworld", new StargateEnergyRequired(0, 0));
		DEFAULTS_MAP.put("the_nether", new StargateEnergyRequired(3686400, 1600));
		DEFAULTS_MAP.put("the_end", new StargateEnergyRequired(5529600, 2400));
		DEFAULTS_MAP.put("moon.moon", new StargateEnergyRequired(7372800, 3200));
		DEFAULTS_MAP.put("planet.mars", new StargateEnergyRequired(11059200, 4800));
		DEFAULTS_MAP.put("planet.venus", new StargateEnergyRequired(12288000, 5334));
		DEFAULTS_MAP.put("planet.asteroids", new StargateEnergyRequired(14745600, 6400));
	}
	
	private static File dimensionsDataFile;
	private static Map<String, StargateEnergyRequired> DIMENSION_POWER_OFFSET_STRING_MAP;
	private static Map<DimensionType, StargateEnergyRequired> DIMENSION_POWER_OFFSET_MAP;
		
	public static StargateEnergyRequired getCost(DimensionType from, DimensionType to) {
		StargateEnergyRequired reqFrom = DIMENSION_POWER_OFFSET_MAP.get(from);
		StargateEnergyRequired reqTo = DIMENSION_POWER_OFFSET_MAP.get(to);
		
		if (reqFrom == null || reqTo == null) {
			Aunis.logger.error("Tried to get a cost of a non-existing dimension. This is a bug.");
			return new StargateEnergyRequired(0, 0);
		}
		
		int energyToOpen = Math.abs(reqFrom.energyToOpen - reqTo.energyToOpen);
		int keepAlive = Math.abs(reqFrom.keepAlive - reqTo.keepAlive);
		
		return new StargateEnergyRequired(energyToOpen, keepAlive);
	}
	
	public static void load(File modConfigDir) {
		DIMENSION_POWER_OFFSET_MAP = null;
	
		dimensionsDataFile = new File(modConfigDir, "aunis_dimensions.json");
		
		try {			
			Type typeOfHashMap = new TypeToken<Map<String, StargateEnergyRequired>>() { }.getType();
			DIMENSION_POWER_OFFSET_STRING_MAP = new GsonBuilder().create().fromJson(new FileReader(dimensionsDataFile), typeOfHashMap);
		}
		
		catch (FileNotFoundException exception) {
			DIMENSION_POWER_OFFSET_STRING_MAP = new HashMap<String, StargateEnergyRequired>();
		}
	}
	
	public static void update() throws IOException {
		if (DIMENSION_POWER_OFFSET_MAP == null) {
			DIMENSION_POWER_OFFSET_MAP = new HashMap<DimensionType, StargateEnergyRequired>();
			
			for (String dimName : DIMENSION_POWER_OFFSET_STRING_MAP.keySet()) {
				try {
					DIMENSION_POWER_OFFSET_MAP.put(DimensionType.byName(dimName), DIMENSION_POWER_OFFSET_STRING_MAP.get(dimName));
				}
				
				catch (IllegalArgumentException ex) {
					// Probably removed a mod
					Aunis.info("DimensionType not found: " + dimName);
				}
			}
		}
		
		int originalSize = DIMENSION_POWER_OFFSET_MAP.size();
		
		for (DimensionType dimType : DimensionManager.getRegisteredDimensions().keySet()) {
			if (!DIMENSION_POWER_OFFSET_MAP.containsKey(dimType)) {
				// Biomes O' Plenty Nether fix
				if (dimType.getName().equals("Nether"))
					dimType = DimensionType.NETHER;
				
				if (DEFAULTS_MAP.containsKey(dimType.getName()))
					DIMENSION_POWER_OFFSET_MAP.put(dimType, DEFAULTS_MAP.get(dimType.getName()));
				else
					DIMENSION_POWER_OFFSET_MAP.put(dimType, new StargateEnergyRequired(0, 0));
			}
		}
				
		if (originalSize != DIMENSION_POWER_OFFSET_MAP.size()) {
			FileWriter writer = new FileWriter(dimensionsDataFile);
			
			DIMENSION_POWER_OFFSET_STRING_MAP.clear();
			for (DimensionType dimType : DIMENSION_POWER_OFFSET_MAP.keySet()) {
				DIMENSION_POWER_OFFSET_STRING_MAP.put(dimType.getName(), DIMENSION_POWER_OFFSET_MAP.get(dimType));
			}
			
			writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(DIMENSION_POWER_OFFSET_STRING_MAP));
			writer.close();
		}
	}
}
