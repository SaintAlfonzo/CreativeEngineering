package com.SaintAlfonzo.creativecompat.itemphysic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central registry + query surface for the fire/lava burning split and per-item float
 * behaviour that {@code mixin/ItemPhysicServerMixin} layers on top of ItemPhysic.
 *
 * <h3>Canonical item tags (the normal way to drive this)</h3>
 * Resolved live via {@link ItemStack#is(TagKey)}, so membership changes take effect on the
 * next {@code /reload} with no relog or restart:
 * <ul>
 *   <li>{@code creativeengineering:physics/floats}  — floats in water</li>
 *   <li>{@code creativeengineering:physics/sinks}   — force-sink; wins over everything float-side</li>
 *   <li>{@code creativeengineering:physics/fireproof} — survives open fire, still burns/melts in lava</li>
 *   <li>{@code creativeengineering:physics/lavaproof} — immune to <em>both</em> open fire and lava (wins over fireproof)</li>
 * </ul>
 * Defaults (config/itemphysic.json): no tag ⇒ the item sinks and is destroyed by fire and lava.
 *
 * <h3>Programmatic rules (KubeJS startup, for id-based one-offs)</h3>
 * The static setters below add id/tag rules on top of the canonical tags. They also expose the
 * finer {@link Burn#FIRE_ONLY}/{@link Burn#LAVA_ONLY} states, which the canonical tags do not.
 * These live in a startup script and need a game restart to change; the canonical tags do not.
 *
 * <p>State is JVM-global and side-agnostic.
 */
public final class ItemPhysicSplit {

    private static final Logger LOGGER = LoggerFactory.getLogger("creativeengineering/itemphysic");

    public static final String TAG_NS = "creativeengineering";
    public static final TagKey<Item> FLOATS    = itemTag(TAG_NS, "physics/floats");
    public static final TagKey<Item> SINKS     = itemTag(TAG_NS, "physics/sinks");
    public static final TagKey<Item> FIREPROOF = itemTag(TAG_NS, "physics/fireproof");
    public static final TagKey<Item> LAVAPROOF = itemTag(TAG_NS, "physics/lavaproof");

    /** How an item entity reacts to being in fire / lava. */
    public enum Burn {
        /** Destroyed by both open fire and lava (the default). */
        BURN,
        /** Destroyed by open fire / being on fire, but survives lava. (programmatic only) */
        FIRE_ONLY,
        /** Destroyed by lava, but survives open fire. (programmatic only) */
        LAVA_ONLY,
        /** Immune to both — never catches fire, never takes fire/lava damage. */
        NEVER
    }

    private static final Map<ResourceLocation, Burn> BURN_BY_ID   = new HashMap<>();
    private static final Map<ResourceLocation, Burn> BURN_BY_TAG  = new LinkedHashMap<>();
    private static final Set<ResourceLocation>       FLOAT_BY_ID  = new HashSet<>();
    private static final Set<ResourceLocation>       FLOAT_BY_TAG = new HashSet<>();

    private static final Map<ResourceLocation, TagKey<Item>> TAG_CACHE = new HashMap<>();

    private ItemPhysicSplit() {}

    // ------------------------------------------------------------------------
    // Registration API (called from KubeJS startup scripts)
    // ------------------------------------------------------------------------

    /** Wipe every programmatic rule. A startup script calls this before re-adding its ruleset. */
    public static void reset() {
        BURN_BY_ID.clear();
        BURN_BY_TAG.clear();
        FLOAT_BY_ID.clear();
        FLOAT_BY_TAG.clear();
        LOGGER.info("[itemphysic-split] programmatic rules cleared");
    }

    public static void setBurn(String itemId, String behavior)   { BURN_BY_ID.put(rl(itemId), parse(behavior)); }
    public static void setBurnTag(String tagId, String behavior) { BURN_BY_TAG.put(rl(tagId), parse(behavior)); }

    public static void fireOnly(String itemId)     { BURN_BY_ID.put(rl(itemId), Burn.FIRE_ONLY); }
    public static void lavaOnly(String itemId)     { BURN_BY_ID.put(rl(itemId), Burn.LAVA_ONLY); }
    public static void unburnable(String itemId)   { BURN_BY_ID.put(rl(itemId), Burn.NEVER); }
    public static void burnable(String itemId)     { BURN_BY_ID.put(rl(itemId), Burn.BURN); }

    public static void fireOnlyTag(String tagId)   { BURN_BY_TAG.put(rl(tagId), Burn.FIRE_ONLY); }
    public static void lavaOnlyTag(String tagId)   { BURN_BY_TAG.put(rl(tagId), Burn.LAVA_ONLY); }
    public static void unburnableTag(String tagId) { BURN_BY_TAG.put(rl(tagId), Burn.NEVER); }
    public static void burnableTag(String tagId)   { BURN_BY_TAG.put(rl(tagId), Burn.BURN); }

    public static void floats(String itemId)       { FLOAT_BY_ID.add(rl(itemId)); }
    public static void sinks(String itemId)        { FLOAT_BY_ID.remove(rl(itemId)); }
    public static void floatsTag(String tagId)     { FLOAT_BY_TAG.add(rl(tagId)); }
    public static void sinksTag(String tagId)      { FLOAT_BY_TAG.remove(rl(tagId)); }

    /** One-line summary for the startup script to log. */
    public static String summary() {
        return String.format("canonical tags: %s / %s / %s / %s | programmatic: %d+%d burn, %d+%d float",
                FLOATS.location(), SINKS.location(), FIREPROOF.location(), LAVAPROOF.location(),
                BURN_BY_ID.size(), BURN_BY_TAG.size(), FLOAT_BY_ID.size(), FLOAT_BY_TAG.size());
    }

    // ------------------------------------------------------------------------
    // Queries (called from the mixin, per item entity per tick)
    // ------------------------------------------------------------------------

    /**
     * Fire/lava destruction decision for the mixin.
     *
     * @param lava true if the item entity is currently in lava (vs. open fire)
     * @return {@code TRUE} = destroy it, {@code FALSE} = immune, {@code null} = no rule —
     *         the caller falls back to ItemPhysic's own burningItems list.
     */
    public static Boolean burnDecision(ItemStack stack, boolean lava) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.is(LAVAPROOF)) return Boolean.FALSE;                      // immune to both
        if (stack.is(FIREPROOF)) return lava ? Boolean.TRUE : Boolean.FALSE; // survives fire, burns in lava
        // if-chain, not a switch: an enum switch compiles to a synthetic ItemPhysicSplit$1 that loads lazily and
        // was missing at runtime (NoClassDefFoundError, crashes 2026-09-09 and 09-17).
        Burn burn = burnBehavior(stack);
        if (burn == Burn.NEVER)     return Boolean.FALSE;
        if (burn == Burn.FIRE_ONLY) return !lava;
        if (burn == Burn.LAVA_ONLY) return lava;
        return null;   // BURN
    }

    /** Programmatic burn behaviour for this stack; {@link Burn#BURN} if no rule matches. */
    public static Burn burnBehavior(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Burn.BURN;
        if (!BURN_BY_ID.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            Burn b = (id == null) ? null : BURN_BY_ID.get(id);
            if (b != null) return b;
        }
        if (!BURN_BY_TAG.isEmpty()) {
            for (Map.Entry<ResourceLocation, Burn> e : BURN_BY_TAG.entrySet()) {
                if (stack.is(tag(e.getKey()))) return e.getValue();
            }
        }
        return Burn.BURN;
    }

    /** True when this stack must sink regardless of any float rule ({@code physics/sinks} tag). */
    public static boolean forcedSink(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(SINKS);
    }

    /** Whether this stack should float in water, on top of ItemPhysic's own swimming list. */
    public static boolean isFloat(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.is(FLOATS)) return true;                         // canonical tag — the hot path
        if (!FLOAT_BY_ID.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && FLOAT_BY_ID.contains(id)) return true;
        }
        if (!FLOAT_BY_TAG.isEmpty()) {
            for (ResourceLocation t : FLOAT_BY_TAG) {
                if (stack.is(tag(t))) return true;
            }
        }
        return false;
    }

    /**
     * Is this item entity currently in contact with lava (as opposed to open fire)?
     * Mirrors ItemPhysic's own lava test and adds a block-level fallback for the
     * "resting in a shallow lava pool" case.
     */
    public static boolean touchingLava(ItemEntity item) {
        try {
            if (item.isEyeInFluid(FluidTags.LAVA)) return true;
            if (item.getFluidHeight(FluidTags.LAVA) > 0.0D) return true;
        } catch (Throwable ignored) {
            // fluid maps not populated yet this tick
        }
        Level level = item.level;
        if (level == null) return false;
        BlockPos pos = item.blockPosition();
        return level.getFluidState(pos).is(FluidTags.LAVA)
                || level.getFluidState(pos.below()).is(FluidTags.LAVA);
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private static Burn parse(String s) {
        if (s == null) return Burn.BURN;
        switch (s.trim().toLowerCase()) {
            case "fire":  case "fire_only":  case "fireonly":     return Burn.FIRE_ONLY;
            case "lava":  case "lava_only":  case "lavaonly":     return Burn.LAVA_ONLY;
            case "never": case "none": case "unburnable": case "lavaproof": case "immune": return Burn.NEVER;
            default: return Burn.BURN;
        }
    }

    private static ResourceLocation rl(String id) {
        return new ResourceLocation(id.startsWith("#") ? id.substring(1) : id);
    }

    private static TagKey<Item> itemTag(String ns, String path) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(ns, path));
    }

    private static TagKey<Item> tag(ResourceLocation id) {
        return TAG_CACHE.computeIfAbsent(id, k -> TagKey.create(Registry.ITEM_REGISTRY, k));
    }
}
