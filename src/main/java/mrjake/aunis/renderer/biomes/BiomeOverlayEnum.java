package mrjake.aunis.renderer.biomes;

import java.util.EnumSet;

import mrjake.aunis.Aunis;
import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.util.BlockHelpers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public enum BiomeOverlayEnum {
	NORMAL("", TextFormatting.GRAY),
	FROST("_frost", TextFormatting.DARK_AQUA),
	MOSSY("_mossy", TextFormatting.DARK_GREEN),
	AGED("_aged", TextFormatting.GRAY),
	SOOTY("_sooty", TextFormatting.DARK_GRAY);
	
	public String suffix;
	private TextFormatting color;
	private String unlocalizedName;

	BiomeOverlayEnum(String suffix, TextFormatting color) {
		this.suffix = suffix;
		this.color = color;
		this.unlocalizedName = "gui.stargate.biome_overlay." + name().toLowerCase();
	}
	
	public String getLocalizedColorizedName() {
		return color + Aunis.proxy.localize(unlocalizedName);
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

		// If not Nether and block not under sky
		if (world.provider.getDimensionType() != DimensionType.NETHER && !BlockHelpers.isBlockDirectlyUnderSky(world, topmostBlock))
			return NORMAL;
		
		if (biome.getTemperature(topmostBlock) <= AunisConfig.stargateConfig.frostyTemperatureThreshold)
			return FROST;
		
		BiomeOverlayEnum overlay = AunisConfig.stargateConfig.getBiomeOverrideBiomes().get(biome);
		
		if (overlay != null)
			return overlay;
		
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
