package com.SaintAlfonzo.cecapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.nio.file.Files;
import java.util.Optional;

/**
 * Gets the client into the test world. A template copied into {@code saves/<name>} by the runner is loaded
 * as is; without one the driver creates a superflat world itself (the default layers, one biome, no
 * structures, fixed seed, creative with cheats). 1.19.2 has no quick-play argument, so both go through
 * WorldOpenFlows from whatever screen the client is on.
 */
final class TestWorld {

    private TestWorld() {}

    static boolean exists(Minecraft mc, String name) {
        return Files.isDirectory(mc.getLevelSource().getBaseDir().resolve(name));
    }

    static void load(Minecraft mc, String name) {
        mc.createWorldOpenFlows().loadLevel(mc.screen, name);
    }

    /**
     * Step 1 of a fresh world: CreateWorldScreen.openFresh loads the datapacks (mods' biomes included) into a
     * WorldCreationContext; {@link #createFlat} reads it off the screen on a later tick.
     */
    static void openCreateScreen(Minecraft mc) {
        CreateWorldScreen.openFresh(mc, mc.screen);
    }

    static void createFlat(Minecraft mc, CreateWorldScreen screen, CapturePlan plan) {
        WorldCreationContext ctx = screen.worldGenSettingsComponent.settings();
        RegistryAccess.Frozen access = ctx.registryAccess();
        Registry<Biome> biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> structures = access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        FlatLevelGeneratorSettings defaults = FlatLevelGeneratorSettings.getDefault(biomes, structures);
        FlatLevelGeneratorSettings flat = defaults.withLayers(defaults.getLayersInfo(), Optional.of(HolderSet.direct()));
        flat.setBiome(biomes.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY,
                new ResourceLocation(plan.worldBiome()))));
        flat.updateLayers();

        WorldGenSettings replaced = WorldGenSettings.replaceOverworldGenerator(access, ctx.worldGenSettings(),
                new FlatLevelSource(structures, flat));
        WorldGenSettings wgs = new WorldGenSettings(plan.worldSeed(), false, false, replaced.dimensions());

        GameRules rules = new GameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, null);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, null);
        rules.getRule(GameRules.RULE_SENDCOMMANDFEEDBACK).set(false, null);
        rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, null);
        rules.getRule(GameRules.RULE_DO_PATROL_SPAWNING).set(false, null);
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, null);

        LevelSettings settings = new LevelSettings(plan.worldName(), GameType.CREATIVE, false, Difficulty.NORMAL,
                true, rules, DataPackConfig.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(plan.worldName(), settings, access, wgs);
    }
}
