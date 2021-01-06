package mrjake.aunis.config;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import mrjake.aunis.worldgen.stargate.JsonStargateGenerator;

public final class StargateGeneratorConfig {
    private static final Type MAP_TYPE = new TypeToken<Map<String, StargateGeneratorConfigEntry>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, JsonStargateGenerator> STARGATE_GENERATORS = new HashMap<>();

    public static void load(File modConfigDir){
        final File generatorsConfigFile = new File(modConfigDir, "aunis_generators.json");

        if(!generatorsConfigFile.exists()) {
            STARGATE_GENERATORS.put("overworld", getDefaultOverworld());
            STARGATE_GENERATORS.put("orlin", getDefaultOrlin());
            save(modConfigDir);
            return;
        }

        STARGATE_GENERATORS.clear();

        try (FileReader reader = new FileReader(generatorsConfigFile)) {
            Map<String, StargateGeneratorConfigEntry> entries = GSON.fromJson(reader, MAP_TYPE);
            STARGATE_GENERATORS.putAll(
                    entries.entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    en -> new JsonStargateGenerator(en.getKey(), en.getValue())
                            ))
            );
            if(!STARGATE_GENERATORS.containsKey("orlin"))
                STARGATE_GENERATORS.put("orlin", getDefaultOrlin());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(File modConfigDir) {
        final File generatorsConfigFile = new File(modConfigDir, "aunis_generators.json");

        try (FileWriter writer = new FileWriter(generatorsConfigFile)) {
            GSON.toJson(STARGATE_GENERATORS.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    en -> en.getValue().getConfig()
            )), MAP_TYPE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static JsonStargateGenerator findGenerator(String name){
        return STARGATE_GENERATORS.get(name);
    }

    private static JsonStargateGenerator getDefaultOverworld(){
        StargateGeneratorConfigEntry config = new StargateGeneratorConfigEntry();

        config.template = "aunis:sg_%biome%_%size%";
        config.generatorType = "overworld";

        config.stargate.datablockOffsetY = -3;
        config.stargate.upgrades.capacitors = 1;
        config.stargate.upgrades.glyphMilkyWay = true;
        config.stargate.upgrades.chevronUpgrade = true;

        config.dhd.despawnChance = 0;
        config.dhd.fuel = 3600;
        config.dhd.upgrades.chevronUpgrade = true;

        final JsonArray biomeWhitelist = new JsonArray();
        biomeWhitelist.add("plains");
        biomeWhitelist.add("desert");
        config.generatorSettings.add("biomeWhitelist", biomeWhitelist);

        config.dimWhitelist = Collections.singletonList(0);

        return new JsonStargateGenerator("overworld", config);
    }

    private static JsonStargateGenerator getDefaultOrlin(){
        StargateGeneratorConfigEntry config = new StargateGeneratorConfigEntry();

        config.template = "aunis:sg_nether_%size%";
        config.enableMysteriousPage = false;
        config.generatorType = "nether";

        config.stargate.upgrades.capacitors = 1;
        config.stargate.upgrades.glyphMilkyWay = true;
        config.stargate.upgrades.chevronUpgrade = true;

        config.dhd.despawnChance = 0;
        config.dhd.fuel = 3600;
        config.dhd.upgrades.chevronUpgrade = true;

        config.dimWhitelist = Collections.singletonList(-1);

        return new JsonStargateGenerator("orlin", config);
    }

    public static List<String> getMysteriousPageGenerators() {
        return STARGATE_GENERATORS.values().stream()
                .filter(generator -> generator.getConfig().enableMysteriousPage)
                .map(JsonStargateGenerator::getName)
                .collect(Collectors.toList());
    }
}
