package mrjake.aunis.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

@Config(modid="aunis", name="aunis")
public class AunisConfig {
	
	@Name("Stargate size")
	@RequiresWorldRestart
	public static StargateSizeEnum stargateSize = StargateSizeEnum.SMALL; 
	
	@Name("Stargate config options")
	public static StargateConfig stargateConfig = new StargateConfig();
	
	@Name("DHD config options")
	public static DHDConfig dhdConfig = new DHDConfig();
	
	@Name("Transport rings options")
	public static RingsConfig ringsConfig = new RingsConfig();
	
	@Name("Power draw options")
	public static PowerConfig powerConfig = new PowerConfig();
	
	@Name("Debug options")
	public static DebugConfig debugConfig = new DebugConfig();
	
	@Name("Mysterious Page options")
	public static MysteriousConfig mysteriousConfig = new MysteriousConfig();
	
	@Name("AutoClose options")
	public static AutoCloseConfig autoCloseConfig = new AutoCloseConfig();
	
	public static class StargateConfig {
		@Name("Orlin's gate max open count")
		@RangeInt(min=0)
		public int stargateOrlinMaxOpenCount = 2;
		
		@Name("Universe dialer max horizontal reach radius")
		@RangeInt(min=0, max=64)
		public int universeDialerReach = 10;

		@Name("Universe dialer nearby radius")
		public int universeGateNearbyReach = 1024;
	}
	
	public static class PowerConfig {
		@Name("Stargate's internal buffer size")
		@RangeInt(min=0)
		public int stargateEnergyStorage = 71280000;
		
		@Name("Stargate's max power throughput")
		@RangeInt(min=1)
		public int stargateMaxEnergyTransfer = 26360;
		
		@Name("Stargate wormhole open power draw")
		@RangeInt(min=0)
		public int openingBlockToEnergyRatio = 4608;
		
		@Name("Stargate wormhole sustain power draw")
		@RangeInt(min=0)
		public int keepAliveBlockToEnergyRatioPerTick = 2;

		@Name("Stargate instability threshold(seconds to close)")
		@RangeInt(min=1)
		public int instabilitySeconds = 20;
		
		@Name("Orlin's gate energy multiplier")
		@RangeDouble(min=0)
		public double stargateOrlinEnergyMul = 1.5;
	}
	
	public static class RingsConfig {
		@Name("Rings range's radius horizontal")
		@RangeInt(min=1, max=256)
		public int rangeFlat = 25;
		
		@Name("Rings vertical reach")
		@RangeInt(min=1, max=256)
		public int rangeVertical = 256;

		@Name("Ignore rings check for blocks to replace")
		public boolean ignoreObstructionCheck = false;
	}
	
	public static class DHDConfig {
		@Name("DHD range's radius horizontal")
		@RangeInt(min=1)
		public int rangeFlat = 10;
		
		@Name("DHD range's radius vertical")
		@RangeInt(min=1)
		public int rangeVertical = 5;
		
		@Name("DHD's max fluid capacity")
		@RangeInt(min=1)
		public int fluidCapacity = 60000;

		@Name("Energy per 1mB Naquadah")
		@RangeInt(min=1)
		public int energyPerNaquadah = 10240;

		@Name("Generation multiplier")
		@RangeInt(min=1)
		@Comment({
			"Energy per 1mB is multiplied by this",
			"Consumed mB/t is equal to this"
		})
		public int powerGenerationMultiplier = 1;
	}
	
	public static class DebugConfig {
		@Name("Check gate merge")
		public boolean checkGateMerge = true;
		
		@Name("Allow charging the crystal by hand")
		public boolean allowHandCrystalCharging = false;
		
		@Name("Render bounding boxes")
		public boolean renderBoundingBoxes = false;
		
		@Name("Render whole kawoosh bounding box")
		public boolean renderWholeKawooshBoundingBox = false;
	}
	
	public static class MysteriousConfig {
		@Name("Max overworld XZ-coords generation")
		@RangeInt(min=1, max=30000000)
		public int maxOverworldCoords = 30000;
		
		@Name("Min overworld XZ-coords generation")
		@RangeInt(min=1, max=30000000)
		public int minOverworldCoords = 15000;
		
		@Name("Chance of despawning DHD")
		@RangeDouble(min=0, max=1)
		public double despawnDhdChance = 0.05;
	}
	
	public static class AutoCloseConfig {
		@Name("autoclose enabled")
		public boolean autocloseEnabled = true;
		
		@Name("Seconds to autoclose with no players nearby")
		@RangeInt(min=1, max=300)
		public int secondsToAutoclose = 5;
	}
}
