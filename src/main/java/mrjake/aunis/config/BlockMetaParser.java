package mrjake.aunis.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class BlockMetaParser {
	
	/**
	 * Parses array of configured blocks. For format see {@link BlockMetaParser#getBlockStateFromString(String)}
	 * 
	 * @param config Array of single lines
	 * @return List of {@link IBlockState}s or empty list.
	 */
	@Nonnull
	static List<IBlockState> parseConfig(String[] config) {
		List<IBlockState> list = new ArrayList<>();

		for (String line : config) {
			IBlockState state = getBlockStateFromString(line);
			
			if(state != null) {
				list.add(state);
			}
		}
		
		return list;
	}
	
	/**
	 * Parses single line of the config.
	 * 
	 * @param line Consists of modid:blockid[:meta]
	 * @return {@link IBlockState} when valid block, {@code null} otherwise.
	 */
	@Nullable
	@SuppressWarnings("deprecation")
	static IBlockState getBlockStateFromString(String line) {
        String[] parts = line.trim().split(":", 3);
        Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);

        if (block != null && block != Blocks.AIR) {
        	if (parts.length == 2 || parts[2].equals("*"))
        		return block.getDefaultState();
        	
            return block.getStateFromMeta(Integer.parseInt(parts[2]));
        }
        
        return null;
    }
}
