package mrjake.aunis.renderer.biomes;

import java.util.EnumSet;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.util.BlockHelpers;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public enum BiomeOverlayEnum {
	NORMAL("", TextFormatting.GRAY),
	FROST("_frost", TextFormatting.DARK_AQUA),
	MOSSY("_mossy", TextFormatting.DARK_GREEN),
	AGED("_aged", TextFormatting.GRAY),
	SOOTY("_sooty", TextFormatting.DARK_GRAY);
	
	public String suffix;
	public TextFormatting color;

	BiomeOverlayEnum(String suffix, TextFormatting color) {
		this.suffix = suffix;
		this.color = color;
	}
	
	/**
	 * Called every 1-2 seconds from {@link TileEntity} to update it's
	 * frosted/moss state.
	 * 
	 * @param world
	 * @param topmostBlock Topmost block of the structure (Stargates should pass top chevron/ring)
	 * @param supportedOverlays will only return enums which are in this Set
	 * @return
	 */
	public static BiomeOverlayEnum updateBiomeOverlay(World world, BlockPos topmostBlock, EnumSet<BiomeOverlayEnum> supportedOverlays) {
		BiomeOverlayEnum ret = getBiomeOverlay(world, topmostBlock);
		
		if (supportedOverlays.contains(ret))
			return ret;
		
		return NORMAL;
	}
	
	private static BiomeOverlayEnum getBiomeOverlay(World world, BlockPos topmostBlock) {
		Biome biome = world.getBiome(topmostBlock);

		if (biome == Biomes.HELL)
			return SOOTY;

		if (!BlockHelpers.isBlockDirectlyUnderSky(world, topmostBlock))
			return NORMAL;
		
		if (AunisConfig.stargateConfig.isJungleBiome(biome))
			return MOSSY;
		
		if (biome.getTemperature(topmostBlock) <= AunisConfig.stargateConfig.frostyTemperatureThreshold)
			return FROST;
		
		return NORMAL;
	}

	public static BiomeOverlayEnum fromString(String name) {
		for (BiomeOverlayEnum biomeOverlay : values()) {
			if (biomeOverlay.toString().equals(name)) {
				return biomeOverlay;
			}
		}
		
		return null;
	}
}
