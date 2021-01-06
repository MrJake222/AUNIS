package mrjake.aunis.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class BiomeParser {
	
	/**
	 * Parses array of configured biomes.
	 * 
	 * @param config Array of single lines containing biome definitions.
	 * @return List of {@link Biome}s or empty list.
	 */
	@Nonnull
	static List<Biome> parseConfig(String[] config) {
		List<Biome> list = new ArrayList<>();

		for (String line : config) {
			Biome biome = getBiomeFromString(line);
			
			if(biome != null) {
				list.add(biome);
			}
		}
		
		return list;
	}
	
	/**
	 * Parses single line of the config.
	 * 
	 * @param line Consists of modid:biomename
	 * @return {@link IBlockState} when valid biome, {@code null} otherwise.
	 */
	@Nullable
	static Biome getBiomeFromString(String line) {
        String[] parts = line.trim().split(":", 2);
        return Biome.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
    }
}
