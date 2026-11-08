package com.SaintAlfonzo.creativecalendar.seasons;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembered answers to Serene Seasons' per-biome and per-dimension questions (perf.md 19). Its grass and foliage
 * colour resolver asks them on every call: is the biome seasonal (blacklist scan), tropical (tropical list scan),
 * "less colour change" (list scan), is the dimension whitelisted (list scan), and reads changeGrassColor /
 * changeFoliageColor, each a ForgeConfigSpec lookup. Xaero's World Map calls the resolver up to 9 times per map pixel
 * and chunk meshing once per tinted block; on 2026-11-07 that was about 6% of the render thread.
 *
 * The answers depend only on the biome or dimension, the season tags and the Serene Seasons and Creative Calendar
 * configs, so they are kept until tags reload (SeasonTypes.onTagsUpdated) or either mod's config loads or reloads
 * (both mod buses, {@link #register}). The maps are replaced, not cleared, so a lookup racing a reload lands in the old
 * map. Only registry reference holders are cached (they compare by identity); direct holders are asked every time.
 * Read by mixin.SeasonBiomeConfigMixin, mixin.SeasonServerConfigMixin and mixin.SeasonColorUtilMixin.
 */
public final class SeasonAnswers {

    private static volatile Answers answers = new Answers();

    private SeasonAnswers() {}

    private static final class Answers {
        final Map<Holder<Biome>, Boolean> seasonal = new ConcurrentHashMap<>();
        final Map<Holder<Biome>, Boolean> tropical = new ConcurrentHashMap<>();
        final Map<Holder<Biome>, Boolean> lessColour = new ConcurrentHashMap<>();
        final Map<ResourceKey<Level>, Boolean> dimension = new ConcurrentHashMap<>();
        /** Config value objects (identity) to their last read value. */
        final Map<Object, Object> config = new ConcurrentHashMap<>();
    }

    public enum Question { SEASONAL, TROPICAL, LESS_COLOUR }

    private static Map<Holder<Biome>, Boolean> map(Answers a, Question q) {
        return switch (q) {
            case SEASONAL -> a.seasonal;
            case TROPICAL -> a.tropical;
            case LESS_COLOUR -> a.lessColour;
        };
    }

    /** The remembered answer, or null when not known yet (or the holder is not a registry reference). */
    @Nullable
    public static Boolean get(Question q, Holder<Biome> biome) {
        if (biome.kind() != Holder.Kind.REFERENCE) return null;
        return map(answers, q).get(biome);
    }

    public static void put(Question q, Holder<Biome> biome, boolean value) {
        if (biome.kind() == Holder.Kind.REFERENCE) map(answers, q).put(biome, value);
    }

    @Nullable
    public static Boolean dimension(ResourceKey<Level> dim) {
        return dim == null ? null : answers.dimension.get(dim);
    }

    public static void putDimension(ResourceKey<Level> dim, boolean value) {
        if (dim != null) answers.dimension.put(dim, value);
    }

    /** A config value read once per reload. {@code read} is the value's own get(). */
    public static Object config(Object key, java.util.function.Supplier<Object> read) {
        Map<Object, Object> m = answers.config;
        Object hit = m.get(key);
        if (hit == null) {
            hit = read.get();
            if (hit != null) m.put(key, hit);
        }
        return hit;
    }

    public static void clear() {
        answers = new Answers();
    }

    /**
     * Clears on config loading and reloading of Creative Calendar (its own mod bus) and of Serene Seasons (that mod's
     * bus: Forge posts ModConfigEvent only to the owner's bus; this includes the server config sent on join).
     */
    public static void register(IEventBus ownModBus) {
        listen(ownModBus);
        ModList.get().getModContainerById("sereneseasons").ifPresent(container -> {
            if (container instanceof FMLModContainer fml) listen(fml.getEventBus());
        });
    }

    private static void listen(IEventBus bus) {
        bus.addListener((ModConfigEvent.Loading event) -> clear());
        bus.addListener((ModConfigEvent.Reloading event) -> clear());
    }
}
