package mrjake.aunis.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mrjake.aunis.renderer.biomes.BiomeOverlayEnum;
import mrjake.aunis.util.ItemMetaPair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
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
	
	@Name("Beamer options")
	public static BeamerConfig beamerConfig = new BeamerConfig();
	
	@Name("Recipe options")
	public static RecipeConfig recipeConfig = new RecipeConfig();
	
	@Name("Audio/Video")
	public static AudioVideoConfig avConfig = new AudioVideoConfig();
	
	@Name("WorldGen config")
	public static WorldGenConfig worldgenConfig = new WorldGenConfig();
	
	public static class StargateConfig {
		@Name("Orlin's gate max open count")
		@RangeInt(min=0)
		public int stargateOrlinMaxOpenCount = 2;
		
		@Name("Universe dialer max horizontal reach radius")
		@RangeInt(min=0, max=64)
		public int universeDialerReach = 10;

		@Name("Universe dialer nearby radius")
		public int universeGateNearbyReach = 1024;
		
		@Name("Disable animated Event Horizon")
		@Comment({
			"Changing this option will require you to reload resources manually.",
			"Just press F3+Q once in-game."
		})
		public boolean disableAnimatedEventHorizon = false;

		@Name("Temperature threshold for frosty overlay")
		@Comment({
			"Below this biome temperature the gate will receive frosty texture"
		})
		public float frostyTemperatureThreshold = 0.1f;
		
		// ---------------------------------------------------------------------------------------
		// Kawoosh blocks
		
		@Name("Kawoosh invincible blocks")
		@Comment({
			"Format: \"modid:blockid[:meta]\", for example: ",
			"\"minecraft:wool:7\"",
			"\"minecraft:stone\""
		})
		public String[] kawooshInvincibleBlocks = {};

		private List<IBlockState> cachedInvincibleBlocks = null;

		public boolean canKawooshDestroyBlock(IBlockState state) {
			if (cachedInvincibleBlocks == null) {
				cachedInvincibleBlocks = BlockMetaParser.parseConfig(kawooshInvincibleBlocks);
			}
			
			return !cachedInvincibleBlocks.contains(state);
		}
		
		
		// ---------------------------------------------------------------------------------------
		// Jungle biomes
		@Name("Biomes in which Stargates should be mossy")
		@Comment({
			"Format: \"modid:biomename\", for example: ",
			"\"minecraft:dark_forest\"",
			"\"minecraft:forest\""
		})
		public String[] jungleBiomes = {"minecraft:jungle", "minecraft:jungle_hills", "minecraft:jungle_edge", "minecraft:mutated_jungle", "minecraft:mutated_jungle_edge"};
		
		private List<Biome> cachedJungleBiomes = null;
		
		public boolean isJungleBiome(Biome biome) {
			if (cachedJungleBiomes == null) {
				cachedJungleBiomes = BiomeParser.parseConfig(jungleBiomes);
			}
			
			return cachedJungleBiomes.contains(biome);
		}
		
		
		// ---------------------------------------------------------------------------------------
		// Biome overlay override blocks
		
		@Name("Biome overlay override blocks")
		@SuppressWarnings("serial")
		@Comment({
			"Format: \"modid:blockid[:meta]\", for example: ",
			"\"minecraft:wool:7\"",
			"\"minecraft:stone\""
		})
		public Map<String, String[]> biomeOverrideBlocks = new HashMap<String, String[]>() {
			{
				put(BiomeOverlayEnum.NORMAL.toString(), new String[] {"minecraft:stone"});
				put(BiomeOverlayEnum.FROST.toString(), new String[] {"minecraft:ice"});
				put(BiomeOverlayEnum.MOSSY.toString(), new String[] {"minecraft:vine"});
				put(BiomeOverlayEnum.AGED.toString(), new String[] {"minecraft:cobblestone"});
				put(BiomeOverlayEnum.SOOTY.toString(), new String[] {"minecraft:coal_block"});
			}
		};
		
		private Map<BiomeOverlayEnum, List<ItemMetaPair>> cachedBiomeOverrideBlocks = null;
		private Map<ItemMetaPair, BiomeOverlayEnum> cachedBiomeOverrideBlocksReverse = null;
		
		private void genBiomeOverrideCache() {
			cachedBiomeOverrideBlocks = new HashMap<>();
			cachedBiomeOverrideBlocksReverse = new HashMap<>();
			
			for (Map.Entry<String, String[]> entry : biomeOverrideBlocks.entrySet()) {
				List<ItemMetaPair> parsedList = ItemMetaParser.parseConfig(entry.getValue());
				BiomeOverlayEnum biomeOverlay = BiomeOverlayEnum.fromString(entry.getKey());
				
				cachedBiomeOverrideBlocks.put(biomeOverlay, parsedList);
				
				for (ItemMetaPair stack : parsedList) {
					cachedBiomeOverrideBlocksReverse.put(stack, biomeOverlay);
				}
			}
		}
		
		public Map<BiomeOverlayEnum, List<ItemMetaPair>> getBiomeOverrideBlocks() {
			if (cachedBiomeOverrideBlocks == null) {
				genBiomeOverrideCache();
			}
			
			return cachedBiomeOverrideBlocks;
		}
		
		public Map<ItemMetaPair, BiomeOverlayEnum> getBiomeOverrideItemMetaPairs() {
			if (cachedBiomeOverrideBlocksReverse == null) {
				genBiomeOverrideCache();
			}
			
			return cachedBiomeOverrideBlocksReverse;
		}
				
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

		@Name("Stargate instability threshold (seconds of energy left before gate becomes unstable)")
		@RangeInt(min=1)
		public int instabilitySeconds = 20;
		
		@Name("Orlin's gate energy multiplier")
		@RangeDouble(min=0)
		public double stargateOrlinEnergyMul = 2.0;
		
		@Name("Universe gate energy multiplier")
		@RangeDouble(min=0)
		public double stargateUniverseEnergyMul = 1.5;
		
		@Name("Capacitors supported by Universe gates")
		public int universeCapacitors = 0;
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

		@RangeDouble(min=0, max=1)
		public double activationLevel = 0.4;
		
		@RangeDouble(min=0, max=1)
		public double deactivationLevel = 0.98;
	}
	
	public static class DebugConfig {
		@Name("Check gate merge")
		public boolean checkGateMerge = true;
		
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

		@Name("Mysterious page cooldown")
		@RangeInt(min=0)
		public int pageCooldown = 40;
	}
	
	public static class AutoCloseConfig {
		@Name("Autoclose enabled")
		public boolean autocloseEnabled = true;
		
		@Name("Seconds to autoclose with no players nearby")
		@RangeInt(min=1, max=300)
		public int secondsToAutoclose = 5;
	}
	
	public static class BeamerConfig {
		@Name("Fluid buffer capacity")
		@RangeInt(min=1)
		public int fluidCapacity = 60000;
		
		@Name("Energy buffer capacity")
		@RangeInt(min=1)
		public int energyCapacity = 17820000;
		
		@Name("Energy buffer max transfer")
		@RangeInt(min=1)
		public int energyTransfer = 26360;
		
		@Name("Fluid max transfer")
		@RangeInt(min=1)
		public int fluidTransfer = 100;
		
		@Name("Item max transfer")
		@RangeInt(min=1)
		public int itemTransfer = 4;

		@Name("Max gate-beamer distance")
		public int reach = 10;
		
		@Name("Should the beam be responsive to fluid color")
		public boolean enableFluidBeamColorization = true;

		@Name("Interval of signals being send to OC about transfers (in ticks)")
		@RangeInt(min=1)
		public int signalIntervalTicks = 20;
	}
	
	public static class RecipeConfig {
		
	}
	
	public static class AudioVideoConfig {
		@Name("Notebook page Glyph transparency")
		@RangeDouble(min=0, max=1)
		public double glyphTransparency = 0.75;
		
		@Name("Aunis volume")
		@RangeDouble(min=0, max=1)
		public float volume = 1;
		
		@Name("Notebook Page offset")
		@Comment({
			"Greater values render the Page more to the center of the screen, smaller render it closer to the borders.",
			"0 - for standard 16:9 (default),",
			"0.2 - for 4:3.",
		})
		public float pageNarrowing = 0;
	}
	
	public static class WorldGenConfig {
		@Name("Enable Naquadah ore generation")
		public boolean naquadahEnable = true;
		
		@Name("Naquadah vein size")
		public int naquadahVeinSize = 8;
		
		@Name("Naquadah max veins in chunk")
		public int naquadahMaxVeinInChunk = 16;
	}
	
	public static void resetCache() {
		stargateConfig.cachedInvincibleBlocks = null;
		stargateConfig.cachedJungleBiomes = null;
		stargateConfig.cachedBiomeOverrideBlocks = null;
		stargateConfig.cachedBiomeOverrideBlocksReverse = null;
	}
}
