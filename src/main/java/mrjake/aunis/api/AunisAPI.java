package mrjake.aunis.api;

import java.util.function.Function;

import com.google.gson.JsonObject;

import mrjake.aunis.api.worldgen.stargate.OptimalPlaceFinderAbstract;
import mrjake.aunis.worldgen.stargate.JsonStargateGenerator;

public final class AunisAPI {

    /**
     * Register custom stargate place finder
     * @param name name for JSON
     * @param creator finder create function. You can use constructor here
     */
    public static void registerStargatePlaceFinder(String name, Function<JsonObject, OptimalPlaceFinderAbstract> creator) {
        JsonStargateGenerator.registerPlaceFinder(name, creator);
    }
}
