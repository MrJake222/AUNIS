package mrjake.aunis.worldgen.stargate.finders;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import mrjake.aunis.api.worldgen.stargate.OptimalPlaceFinderAbstract;
import mrjake.aunis.api.worldgen.stargate.OptimalStargatePlace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * TODO SurfaceGatePlaceFinder
 */
public final class OptimalPlaceFinderSurface extends OptimalPlaceFinderAbstract {
    public OptimalPlaceFinderSurface(JsonObject settings) {
        super(settings);
    }

    @Nonnull
    @Override
    public OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos startPos) {
        return null;
    }
}
