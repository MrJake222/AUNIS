package mrjake.aunis.worldgen.stargate.finders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;

import mrjake.aunis.Aunis;
import mrjake.aunis.api.worldgen.stargate.OptimalPlaceFinderAbstract;
import mrjake.aunis.api.worldgen.stargate.OptimalStargatePlace;
import mrjake.aunis.worldgen.stargate.StargateGenerationHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

/**
 * TODO OptimalPlaceFinderSurface
 */
public final class OptimalPlaceFinderSurface extends OptimalPlaceFinderAbstract {
    /**
     * Directions check definitions
     */
    private static final List<StargateGenerationHelper.Direction> DIRECTIONS = Arrays.asList(
            new StargateGenerationHelper.Direction(EnumFacing.UP, false, 0).setRequiredMinimum(12).setIgnoreInMaximum(),
            new StargateGenerationHelper.Direction(EnumFacing.NORTH, true, 2),
            new StargateGenerationHelper.Direction(EnumFacing.SOUTH, true, 2),
            new StargateGenerationHelper.Direction(EnumFacing.WEST, true, 2),
            new StargateGenerationHelper.Direction(EnumFacing.EAST, true, 2));

    /**
     * Allowed blocks
     */
    private final List<Predicate<IBlockState>> allowedBlocksBelow;

    private static final int MINIMAL_FRONT_SPACE = 16;
    private static final int MINIMAL_SIDE_SPACE = 5;

    public OptimalPlaceFinderSurface(JsonObject settings) {
        super(settings);
        allowedBlocksBelow = Arrays.asList(Blocks.GRASS, Blocks.STONE, Blocks.DIRT, Blocks.SAND).stream().map(BlockMatcher::forBlock).collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos posIn) {
        BlockPos start = posIn;
        BlockPos current = start;
        int count = 0;
        int pass = 1;

        BlockPos found = null;
        Map.Entry<EnumFacing, StargateGenerationHelper.DirectionResult> frontResult = null;

        while (found == null) {
            Aunis.logger.debug("OptimalPlaceFinderSurface: count: " + count + ", pass: " + pass + ", current: " + current);

            for (BlockPos.MutableBlockPos pos : BlockPos.MutableBlockPos.getAllInBoxMutable(current, current.add(16, 16, 16))) {
                if (world.isAirBlock(pos.down()) || isBiomeAllowed(world.getBiome(pos)))
                    continue;

                StargateGenerationHelper.FreeSpace freeSpace = StargateGenerationHelper.getFreeSpaceInDirections(world, pos, DIRECTIONS, 16, allowedBlocksBelow);

                if (freeSpace != null) {
                    if (freeSpace.getMaxDistance().getValue().distance >= MINIMAL_FRONT_SPACE) {
                        int left = freeSpace.getDistance(freeSpace.getMaxDistance().getKey().rotateYCCW()).distance;
                        int right = freeSpace.getDistance(freeSpace.getMaxDistance().getKey().rotateY()).distance;

                        if (left >= MINIMAL_SIDE_SPACE && right >= MINIMAL_SIDE_SPACE) {
                            found = pos.toImmutable();
                            frontResult = freeSpace.getMaxDistance();

                            break;
                        }
                    }
                }
            }

            if (count == 0)
                current = start.add(16, 0, 0);
            else if (count == 1)
                current = start.add(0, 0, 16);
            else if (count == 2)
                current = start.add(16, 0, 16);
            else if (count == 3) {
                if (start.getY() > world.getActualHeight()) {
                    start = posIn.add(32*pass, 0, 32*pass);
                    pass++;
                }

                else {
                    start = start.add(0, 16, 0);
                }

                current = start;
            }

            count++;

            if (count > 3)
                count = 0;
        }

        Aunis.logger.debug("OptimalPlaceFinderSurface: /tp " + found.getX() + " " + found.getY() + " " + found.getZ());

        Rotation rotation;

        switch (frontResult.getKey()) {
            case SOUTH: rotation = Rotation.CLOCKWISE_180; break;
            case WEST:  rotation = Rotation.COUNTERCLOCKWISE_90; break;
            case NORTH: rotation = Rotation.NONE; break;
            case EAST:  rotation = Rotation.CLOCKWISE_90; break;
            default:    rotation = Rotation.NONE; break;
        }

        int y = Math.min(frontResult.getValue().ydiff, 0);
        y -= 2;

        BlockPos translate = new BlockPos(-7, y, -16).rotate(rotation);
        found = found.add(translate);
        return new OptimalStargatePlace(found, rotation);
    }
}
