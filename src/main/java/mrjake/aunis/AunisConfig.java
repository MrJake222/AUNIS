package mrjake.aunis;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid="aunis", name="aunis")
public class AunisConfig {
	
//	public static BlockPos dhdRange = new BlockPos(10,3,10);

	@Name("DHD config options")
	public static DHDConfig dhdConfig = new DHDConfig();
	
	@Name("Transport rings options")
	public static RingsConfig ringsConfig = new RingsConfig();
	
	@Name("Power draw options")
	public static PowerConfig powerConfig = new PowerConfig();
	
	@Name("Debug options")
	public static DebugConfig debugConfig = new DebugConfig();
	
	
	public static class PowerConfig {
		@Name("Stargate's internal buffer size")
		@RangeInt(min=10000)
		public int stargateEnergyStorage = 15728640;
		
		@Name("Stargate's max power throughput")
		@RangeInt(min=1)
		public int stargateMaxEnergyTransfer = 5120;
		
		
		
		@Name("Stargate wormhole open power draw")
		@Comment({
			"Calculated as per block one-time draw.",
			"So, if this field is 150, 1500 RF would be required",
			"to open a tunnel 10m across. Drawed only once per open."
		})
		@RangeDouble(min=0)
		public double openingBlockToEnergyRatio = 1536 / 10.0; // 1,536 RF / 10m
		
		@Name("Stargate wormhole sustain power draw")
		@Comment({
			"Drawed each tick. This is multipiled by distance."
		})
		@RangeDouble(min=0)
		public double keepAliveBlockToEnergyRatioPerTick = 8 / 10.0; // 8 RF / 10m PER ONE TICK

		@Name("Stargate instability threshold(seconds to close)")
		@RangeInt(min=1)
		public int instabilitySeconds = 10;
		
		@Name("Energy draw multiplier for cross-dimension travel")
		@RangeDouble(min=1)
		public double crossDimensionMul = 2.0;
		
//		@Name("Energy draw multiplier for Nether")
//		@RangeDouble(min=1)
//		public double netherMultiplier = 2.0;
//
//		@Name("Energy draw multiplier for The End")
//		@RangeDouble(min=1)
//		public double theEndMultiplier = 2.5;
		
		
		
		@Name("Minimal energy required by DHD to be functional")
		@RangeInt(min=0)
		public int dhdMinimalEnergy = 5120;

		@Name("Power crystal buffer size")
		@RangeInt(min=10000)
		public int dhdCrystalEnergyStorage = 15728640; // 14 000 000
		
		@Name("Power crystal max IO")
		@RangeInt(min=10000)
		public int dhdCrystalMaxEnergyTransfer = 5120; // 140 000
	}
	
	public static class RingsConfig {
		@Name("Rings range's radius horizontal")
		@RangeInt(min=1)
		public int rangeFlat = 25;
	}
	
	public static class DHDConfig {
		@Name("DHD range's radius horizontal")
		@RangeInt(min=1)
		public int rangeFlat = 10;
		
		@Name("DHD range's radius vertical")
		@RangeInt(min=1)
		public int rangeVertical = 5;
	}
	
	public static class DebugConfig {
		@Name("Check gate merge")
		public boolean checkGateMerge = true;
	}
}
