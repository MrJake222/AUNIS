package mrjake.aunis.worldgen.stargate.finders;

import com.google.gson.JsonObject;

import mrjake.aunis.Aunis;
import mrjake.aunis.api.worldgen.stargate.OptimalPlaceFinderAbstract;
import mrjake.aunis.api.worldgen.stargate.OptimalStargatePlace;
import mrjake.aunis.config.AunisConfig;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/**
 * Temporary solution
 * TODO FIX IT OR REMOVE because it doesn't work currently. It will try to search for optimal place and it will fail because of {@link #isBiomeAllowed(Biome)} check in method {@link #checkForPlace(World, int, int)}
 */
public final class OptimalPlaceFinderOverworld extends OptimalPlaceFinderAbstract {

    public OptimalPlaceFinderOverworld(JsonObject settings){
        super(settings);
    }

    @Override
    public OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos startPos) {
//		boolean nether = rand.nextFloat() < AunisConfig.mysteriousConfig.netherChance;
        BlockPos pos;
        int tries = 0;

        WorldServer worldToSpawn = world.getMinecraftServer().getWorld(0);

        do {
            int x = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (world.rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (world.rand.nextBoolean() ? -1 : 1);
            int z = (int) (AunisConfig.mysteriousConfig.minOverworldCoords + (world.rand.nextFloat() * (AunisConfig.mysteriousConfig.maxOverworldCoords - AunisConfig.mysteriousConfig.minOverworldCoords))) * (world.rand.nextBoolean() ? -1 : 1);

            pos = checkForPlace(worldToSpawn, x/16, z/16);
            tries++;
        } while (pos == null && tries < 100);

        if (tries == 100) {
            Aunis.logger.debug("StargateGenerator: Failed to find place");

            return null;
        }

        return new OptimalStargatePlace(pos, findOptimalRotation(worldToSpawn, pos));
    }

    private static final int SG_SIZE_X = 12;
    private static final int SG_SIZE_Z = 13;

    private static final int SG_SIZE_X_PLAINS = 11;
    private static final int SG_SIZE_Z_PLAINS = 11;

    private BlockPos checkForPlace(World world, int chunkX, int chunkZ) {
        if (world.isChunkGeneratedAt(chunkX, chunkZ))
            return null;

        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);

        int y = chunk.getHeightValue(8, 8);

        if (y > 240)
            return null;

        BlockPos pos = new BlockPos(chunkX*16, y, chunkZ*16);

        Biome biome = chunk.getBiome(pos, world.getBiomeProvider());

        if(!isBiomeAllowed(biome))
            return null;

        String biomeName = biome.getRegistryName().getResourcePath();

        boolean desert = biomeName.contains("desert");

        if (!biomeName.contains("ocean") && !biomeName.contains("river") && !biomeName.contains("beach")) {
//		if (biomeName.contains("Ocean")) {
            int x = desert ? SG_SIZE_X : SG_SIZE_X_PLAINS;
            int z = desert ? SG_SIZE_Z : SG_SIZE_Z_PLAINS;

            int y1 = chunk.getHeightValue(0, 0);
            int y2 = chunk.getHeightValue(x, z);

            int y3 = chunk.getHeightValue(x, 0);
            int y4 = chunk.getHeightValue(0, z);

            // No steep hill
            if (Math.abs(y1 - y2) <= 1 && Math.abs(y3 - y4) <= 1) {
                return pos.subtract(new BlockPos(0, 1, 0));
            }

            else {
                Aunis.logger.debug("StargateGenerator: too steep");
            }
        }

        else {
            Aunis.logger.debug("StargateGenerator: failed, " + biomeName);
        }

        return null;
    }

    private static final int MAX_CHECK = 100;

    private static Rotation findOptimalRotation(World world, BlockPos pos) {
        BlockPos start = pos.add(0, 5, 5);
        int max = -1;
        EnumFacing maxFacing = EnumFacing.EAST;

        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            RayTraceResult rayTraceResult = world.rayTraceBlocks(new Vec3d(start), new Vec3d(start.offset(facing, MAX_CHECK)));

            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                int distance = (int) rayTraceResult.getBlockPos().distanceSq(start);
//				Aunis.info(facing.getName().toUpperCase() + ": distance: " + distance);

                if (distance > max) {
                    max = distance;
                    maxFacing = facing;
                }
            }

            else {
//				Aunis.info(facing.getName().toUpperCase() + ": null");

                max = 100000;
                maxFacing = facing;
            }
        }

//		Aunis.info("maxFacing: " + maxFacing.getName().toUpperCase());
        switch (maxFacing) {
            case SOUTH: return Rotation.CLOCKWISE_90;
            case WEST:  return Rotation.CLOCKWISE_180;
            case NORTH: return Rotation.COUNTERCLOCKWISE_90;
            case EAST:  return Rotation.NONE;
            default:    return Rotation.NONE;
        }
    }
}
