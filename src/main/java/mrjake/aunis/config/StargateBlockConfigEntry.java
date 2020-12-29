package mrjake.aunis.config;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

final class StargateBlockConfigEntry {
    private final ResourceLocation location;
    private final int meta;

    StargateBlockConfigEntry(ResourceLocation location, int meta) {
        this.location = location;
        this.meta = meta;
    }

    boolean contains(IBlockState state) {
        return state.getBlock().getRegistryName().equals(location) && (meta == state.getBlock().getMetaFromState(state) || meta == OreDictionary.WILDCARD_VALUE);
    }

    static StargateBlockConfigEntry fromString(String line) {
        String[] parts = line.trim().split(":", 3);
        Block block = Block.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));

        if (block != null && block != Blocks.AIR) {
            return new StargateBlockConfigEntry(block.getRegistryName(), ( parts.length == 2 || parts[2] == "*" ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(parts[2]) ) );
        }
        return null;
    }
}
