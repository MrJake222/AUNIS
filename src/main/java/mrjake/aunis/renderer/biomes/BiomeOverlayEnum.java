package mrjake.aunis.renderer.biomes;

import mrjake.aunis.config.AunisConfig;
import mrjake.aunis.util.BlockHelpers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public enum BiomeOverlayEnum {
	NORMAL(""),
	FROST("_frost"),
	MOSSY("_mossy");
	
	public String suffix;

	BiomeOverlayEnum(String suffix) {
		this.suffix = suffix;
	}
	
	/**
	 * Called every 1-2 seconds from {@link TileEntity} to update it's
	 * frosted/moss state.
	 * 
	 * @param world
	 * @param topmostBlock Topmost block of the structure (Stargates should pass top chevron/ring)
	 * @return
	 */
	public static BiomeOverlayEnum updateBiomeOverlay(World world, BlockPos topmostBlock) {
		if (!BlockHelpers.isBlockDirectlyUnderSky(world, topmostBlock))
			return NORMAL;
		
		return getOverlayFromBiome(world, topmostBlock);
	}
	
	public static BiomeOverlayEnum getOverlayFromBiome(World world, BlockPos pos) {		
		Biome biome = world.getBiome(pos);
		
		if (biome.getTemperature(pos) < 0.1)
			return FROST;
		
		if (AunisConfig.stargateConfig.isJungleBiome(biome))
			return MOSSY;
		
		return NORMAL;
	}
}
