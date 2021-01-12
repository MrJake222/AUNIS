package mrjake.aunis.api.worldgen.stargate;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;

public abstract class OptimalPlaceFinderAbstract {
    private final Predicate<String> biomePredicate;

    public OptimalPlaceFinderAbstract(JsonObject settings){
        if(settings.has("biomeWhitelist")) {
            final List<String> allowedBiomes = jsonArrayToStringList(settings.getAsJsonArray("biomeWhitelist"));
            biomePredicate = allowedBiomes::contains;
        } else if(settings.has("biomeBlacklist")) {
            final List<String> blacklistedBiomes = jsonArrayToStringList(settings.getAsJsonArray("biomeBlacklist"));
            biomePredicate = ((Predicate<String>) blacklistedBiomes::contains).negate();
        } else {
            biomePredicate = Predicates.alwaysTrue();
        }
    }

    @Nonnull
    public abstract OptimalStargatePlace findOptimalPlace(WorldServer world, BlockPos startPos);

    @Nullable
    public ITemplateProcessor getTemplateProcessor(){
        return null;
    }

    protected final boolean isBiomeAllowed(Biome biome){
        return biomePredicate.test(biome.getRegistryName().getResourceDomain());
    }

    protected static List<String> jsonArrayToStringList(JsonArray array) {
        return jsonArrayToStream(array).map(JsonElement::getAsString).collect(Collectors.toList());
    }

    protected static Stream<JsonElement> jsonArrayToStream(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false);
    }
}
