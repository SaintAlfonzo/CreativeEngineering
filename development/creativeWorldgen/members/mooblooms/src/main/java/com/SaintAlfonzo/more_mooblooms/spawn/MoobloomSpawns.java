package com.SaintAlfonzo.more_mooblooms.spawn;

import com.SaintAlfonzo.more_mooblooms.MMConfig;
import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.entity.MMEntities;
import com.SaintAlfonzo.more_mooblooms.entity.Moobloom;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Natural cow spawns that become mooblooms (O6-O8, feature_writeup/mooblooms.md 4.9). Called by
 * mixin/NaturalSpawnerMixin for every entity the natural spawner (ongoing and chunk generation) is about to add;
 * anything other than a {@code minecraft:cow} goes through untouched.
 *
 * Rules: {@code data/<ns>/moobloom_spawns/*.json}, all files combined:
 * <pre>{"rules": [{"biomes": ["minecraft:flower_forest", "#some:tag"],
 *                 "replace": [{"variant": "more_mooblooms:lily", "percent": 50}, {"variant": "any", "percent": 10}]}]}</pre>
 * Per cow: the biome's rule (an exact id beats any tag; among tags the first by file name then order wins),
 * roll 0-100 against its entries in turn; a miss rolls {@code rareChance}; a miss there leaves the cow.
 * A moobloom spawned directly (a biome modifier adds mooblooms, like the witherbloom pack) takes its variant from
 * the same rule ({@link #directVariant}). The mod's only rule is the witherbloom pack's (soul sand valleys, which
 * have no cows), and rareChance defaults to 0, so on its own it never swaps a cow (D24).
 */
public final class MoobloomSpawns {

    /** "any" = a weighted pick over every variant's any_weight. */
    private record Entry(Optional<ResourceLocation> variant, double percent) {}

    private record Rule(List<Entry> entries) {}

    private static volatile Map<ResourceLocation, Rule> byBiome = Map.of();
    private static volatile List<Map.Entry<TagKey<Biome>, Rule>> byTag = List.of();

    /**
     * The spawn swap. Returns {@code entity} unchanged, or a finalized moobloom in its place. {@code level} must be
     * the level the spawner is adding to (a WorldGenRegion during chunk generation).
     */
    public static Entity swap(ServerLevelAccessor level, Entity entity, MobSpawnType reason) {
        if (entity.getType() != EntityType.COW || !(entity instanceof Cow cow)) return entity;
        if (!MMConfig.on(MMConfig.SPAWN_SWAP)) return entity;
        RandomSource random = cow.getRandom();
        MoobloomVariant variant = roll(level.getBiome(cow.blockPosition()), random);
        if (variant == null) return entity;

        Moobloom moobloom = MMEntities.MOOBLOOM.get().create(level.getLevel());
        if (moobloom == null) return entity;
        moobloom.moveTo(cow.getX(), cow.getY(), cow.getZ(), cow.getYRot(), cow.getXRot());
        moobloom.setVariant(variant);
        moobloom.finalizeSpawn(level, level.getCurrentDifficultyAt(moobloom.blockPosition()), reason, null, null);
        moobloom.setAge(cow.getAge());
        return moobloom;
    }

    /** The variant this cow becomes, or null to stay a cow. */
    static MoobloomVariant roll(Holder<Biome> biome, RandomSource random) {
        Rule rule = ruleFor(biome);
        if (rule != null) {
            double r = random.nextDouble() * 100.0;
            for (Entry e : rule.entries()) {
                r -= e.percent();
                if (r < 0) return resolve(e.variant(), random);
            }
        }
        if (random.nextDouble() < MMConfig.num(MMConfig.RARE_CHANCE)) {
            return MoobloomVariants.pickWeighted(false, random);
        }
        return null;
    }

    /** A directly spawned moobloom's variant: its biome's rule rolled, without rareChance; null = no rule or a miss. */
    @Nullable
    public static MoobloomVariant directVariant(Holder<Biome> biome, RandomSource random) {
        Rule rule = ruleFor(biome);
        if (rule == null) return null;
        double r = random.nextDouble() * 100.0;
        for (Entry e : rule.entries()) {
            r -= e.percent();
            if (r < 0) return resolve(e.variant(), random);
        }
        return null;
    }

    private static MoobloomVariant resolve(Optional<ResourceLocation> variant, RandomSource random) {
        if (variant.isEmpty()) return MoobloomVariants.pickWeighted(false, random);
        return MoobloomVariants.get(false, variant.get());
    }

    private static Rule ruleFor(Holder<Biome> biome) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isPresent()) {
            Rule exact = byBiome.get(key.get().location());
            if (exact != null) return exact;
        }
        for (Map.Entry<TagKey<Biome>, Rule> e : byTag) {
            if (biome.is(e.getKey())) return e.getValue();
        }
        return null;
    }

    /** {@code data/<ns>/moobloom_spawns/*.json}, read in file-name order. */
    public static final class Loader extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new GsonBuilder().create();

        public Loader() {
            super(GSON, "moobloom_spawns");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, Rule> exact = new HashMap<>();
            List<Map.Entry<TagKey<Biome>, Rule>> tags = new ArrayList<>();
            int count = 0;
            for (Map.Entry<ResourceLocation, JsonElement> file : new TreeMap<>(files).entrySet()) {
                try {
                    JsonObject root = GsonHelper.convertToJsonObject(file.getValue(), "moobloom_spawns");
                    for (JsonElement r : GsonHelper.getAsJsonArray(root, "rules")) {
                        JsonObject ruleJson = GsonHelper.convertToJsonObject(r, "rule");
                        Rule rule = parseRule(file.getKey(), ruleJson);
                        for (JsonElement b : GsonHelper.getAsJsonArray(ruleJson, "biomes")) {
                            String s = GsonHelper.convertToString(b, "biome");
                            if (s.startsWith("#")) {
                                tags.add(Map.entry(TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s.substring(1))), rule));
                            } else {
                                // the first rule naming a biome wins, like tags
                                exact.putIfAbsent(new ResourceLocation(s), rule);
                            }
                        }
                        count++;
                    }
                } catch (RuntimeException e) {
                    MoreMooblooms.LOGGER.warn("[more_mooblooms] skipped moobloom spawn file {}: {}", file.getKey(), e.getMessage());
                }
            }
            byBiome = Map.copyOf(exact);
            byTag = List.copyOf(tags);
            MoreMooblooms.LOGGER.info("[more_mooblooms] loaded {} moobloom spawn rules ({} biome ids, {} biome tags)",
                    count, exact.size(), tags.size());
        }

        private static Rule parseRule(ResourceLocation file, JsonObject ruleJson) {
            List<Entry> entries = new ArrayList<>();
            double sum = 0;
            for (JsonElement e : GsonHelper.getAsJsonArray(ruleJson, "replace")) {
                JsonObject o = GsonHelper.convertToJsonObject(e, "replace entry");
                String v = GsonHelper.getAsString(o, "variant");
                double percent = GsonHelper.getAsDouble(o, "percent");
                if (percent < 0) throw new IllegalArgumentException("negative percent");
                Optional<ResourceLocation> id = v.equals("any") ? Optional.empty() : Optional.of(new ResourceLocation(v));
                if (id.isPresent() && !MoobloomVariants.all(false).isEmpty() && !MoobloomVariants.has(false, id.get())) {
                    MoreMooblooms.LOGGER.warn("[more_mooblooms] {}: unknown variant {} (the default variant spawns instead)", file, v);
                }
                entries.add(new Entry(id, percent));
                sum += percent;
            }
            if (sum > 100.0) {
                MoreMooblooms.LOGGER.warn("[more_mooblooms] {}: a rule adds up to {}%, scaled down to 100%", file, sum);
                double scale = 100.0 / sum;
                entries.replaceAll(en -> new Entry(en.variant(), en.percent() * scale));
            }
            return new Rule(List.copyOf(entries));
        }
    }

    private MoobloomSpawns() {}
}
