package mrjake.aunis.api.worldgen.stargate;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

/**
 * Best place for stargate
 */
public final class OptimalStargatePlace {
    public final BlockPos pos;
    public final Rotation rotation;

    public OptimalStargatePlace(BlockPos found, Rotation rotation) {
        this.pos = found;
        this.rotation = rotation;
    }

    public final boolean isInvalid() {
        return pos == null || rotation == null;
    }
}
