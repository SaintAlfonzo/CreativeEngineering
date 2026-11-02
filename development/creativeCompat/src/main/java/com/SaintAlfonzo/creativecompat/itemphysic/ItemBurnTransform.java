package com.SaintAlfonzo.creativecompat.itemphysic;

import com.SaintAlfonzo.creativeengineering.util.BurnBursts;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * "When this item entity would be destroyed by fire/lava, do something else instead."
 * Sits on top of {@link ItemPhysicSplit}: the split decides <em>whether</em> an item burns,
 * this decides what its ashes look like.
 *
 * <p>Driven from KubeJS startup ({@code global.itemPhysics.burnReplace/burnExplode}); the
 * mixin {@code ItemPhysicServerMixin} calls {@link #apply} from the {@code health <= 0}
 * branch of {@code ItemPhysicServer.hurt} when the killing blow {@code source.isFire()}.
 *
 * <p>Rules are matched id-first, then tag rules in registration order. Tag <em>membership</em>
 * is resolved live ({@link ItemStack#is(TagKey)}) so a {@code /reload} moves items between
 * buckets; the rule <em>parameters</em> (result item, chance, power) are startup-only.
 *
 * <p>State is JVM-global and side-agnostic; the effects only ever run server-side.
 */
public final class ItemBurnTransform {

    private static final Logger LOGGER = LoggerFactory.getLogger("creativeengineering/itemphysic");

    public enum Kind { REPLACE, EXPLODE }

    /** One transform. Only the fields relevant to {@link #kind} are meaningful. */
    public static final class Rule {
        public final Kind kind;
        public final ResourceLocation result;   // REPLACE: item to drop instead
        public final boolean keepCount;         // REPLACE: roll once per item in the stack (vs. once total)
        public final float power;               // EXPLODE: explosion radius
        public final float chance;              // both: 0..1 probability (per item for REPLACE+keepCount)
        public final boolean shrapnel;          // EXPLODE: also fire a shrapnel burst (util.BurnBursts, patcherMod)

        private Rule(Kind kind, ResourceLocation result, boolean keepCount, float power, float chance, boolean shrapnel) {
            this.kind = kind;
            this.result = result;
            this.keepCount = keepCount;
            this.power = power;
            this.chance = Math.max(0f, Math.min(1f, chance));
            this.shrapnel = shrapnel;
        }

        static Rule replace(ResourceLocation result, float chance, boolean keepCount) {
            return new Rule(Kind.REPLACE, result, keepCount, 0f, chance, false);
        }

        static Rule explode(float power, float chance, boolean shrapnel) {
            return new Rule(Kind.EXPLODE, null, false, Math.max(0f, power), chance, shrapnel);
        }
    }

    private static final Map<ResourceLocation, Rule> BY_ID  = new HashMap<>();
    private static final Map<ResourceLocation, Rule> BY_TAG = new LinkedHashMap<>();
    private static final Set<ResourceLocation> EXCLUDE_ID  = new HashSet<>();
    private static final Set<ResourceLocation> EXCLUDE_TAG = new LinkedHashSet<>();
    private static final Map<ResourceLocation, TagKey<Item>> TAG_CACHE = new HashMap<>();

    /**
     * Entity scoreboard tag stamped on items this class spawns, so a REPLACE result that
     * lands back in the same fire/lava (e.g. dirty glass into the lava that made it) is
     * cleaned up normally instead of transforming forever.
     */
    public static final String PRODUCT_TAG = "creativeengineering:burn_product";

    private ItemBurnTransform() {}

    // ------------------------------------------------------------------------
    // Registration API (KubeJS startup)
    // ------------------------------------------------------------------------

    public static void reset() {
        BY_ID.clear();
        BY_TAG.clear();
        EXCLUDE_ID.clear();
        EXCLUDE_TAG.clear();
        LOGGER.info("[burn-transform] rules cleared");
    }

    /** {@code idOrTag} starting with {@code #} is a tag rule. */
    public static void replace(String idOrTag, String resultId, double chance, boolean keepCount) {
        Rule r = Rule.replace(rl(resultId), (float) chance, keepCount);
        if (isTag(idOrTag)) BY_TAG.put(rl(idOrTag), r); else BY_ID.put(rl(idOrTag), r);
    }

    public static void explode(String idOrTag, double power, double chance, boolean shrapnel) {
        Rule r = Rule.explode((float) power, (float) chance, shrapnel);
        if (isTag(idOrTag)) BY_TAG.put(rl(idOrTag), r); else BY_ID.put(rl(idOrTag), r);
    }

    public static void clear(String idOrTag) {
        if (isTag(idOrTag)) BY_TAG.remove(rl(idOrTag)); else BY_ID.remove(rl(idOrTag));
    }

    /**
     * Never transform this item/tag, even if a rule would otherwise match — checked before
     * any rule. Use it for the end-state products (dirty glass, ash) so they burn away
     * normally instead of re-triggering their own transform when re-dropped.
     */
    public static void exclude(String idOrTag) {
        if (idOrTag == null || idOrTag.isEmpty()) return;
        if (isTag(idOrTag)) EXCLUDE_TAG.add(rl(idOrTag)); else EXCLUDE_ID.add(rl(idOrTag));
    }

    public static boolean isEmpty() {
        return BY_ID.isEmpty() && BY_TAG.isEmpty();
    }

    private static boolean isExcluded(ItemStack stack) {
        if (!EXCLUDE_ID.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (id != null && EXCLUDE_ID.contains(id)) return true;
        }
        for (ResourceLocation t : EXCLUDE_TAG) {
            if (stack.is(tag(t))) return true;
        }
        return false;
    }

    public static String summary() {
        return String.format("burn-transform: %d id + %d tag rules, %d id + %d tag excludes",
                BY_ID.size(), BY_TAG.size(), EXCLUDE_ID.size(), EXCLUDE_TAG.size());
    }

    // ------------------------------------------------------------------------
    // Query + effect (mixin)
    // ------------------------------------------------------------------------

    /** The rule for this stack, or {@code null} — mixin skips the transform when null. */
    public static Rule ruleFor(ItemStack stack) {
        if (stack == null || stack.isEmpty() || isEmpty()) return null;
        if (isExcluded(stack)) return null;
        if (!BY_ID.isEmpty()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
            Rule r = (id == null) ? null : BY_ID.get(id);
            if (r != null) return r;
        }
        if (!BY_TAG.isEmpty()) {
            for (Map.Entry<ResourceLocation, Rule> e : BY_TAG.entrySet()) {
                if (stack.is(tag(e.getKey()))) return e.getValue();
            }
        }
        return null;
    }

    /**
     * Run the transform for a dying item entity. The original {@code item} is discarded by
     * the caller either way; {@code stack} is a copy taken before discard.
     */
    public static void apply(ItemEntity item, ItemStack stack, Rule rule) {
        Level level = item.level;
        if (level == null || level.isClientSide) return;
        RandomSource rng = level.random;

        if (rule.kind == Kind.EXPLODE) {
            if (rng.nextFloat() < rule.chance) {
                // 9-arg overload so we can pass our own DamageSource — vanilla's scales with
                // difficulty and Peaceful zeroes it for players. null calculator = default.
                // NONE = entity damage + knockback, no block break; false = no fire.
                level.explode(null, BurnBlastDamage.INSTANCE, null,
                        item.getX(), item.getY() + 0.25D, item.getZ(),
                        rule.power, false, Explosion.BlockInteraction.NONE);
                if (rule.shrapnel) {
                    BurnBursts.burst(level, item.position(), null);
                }
            }
            return;
        }

        // REPLACE
        Item result = ForgeRegistries.ITEMS.getValue(rule.result);
        if (result == null || result == Items.AIR) return;

        int rolls = rule.keepCount ? Math.max(1, stack.getCount()) : 1;
        int produced = 0;
        for (int n = 0; n < rolls; n++) {
            if (rng.nextFloat() < rule.chance) produced++;
        }
        if (produced <= 0) return;

        ItemEntity drop = new ItemEntity(level, item.getX(), item.getY() + 0.2D, item.getZ(),
                new ItemStack(result, produced));
        // small upward pop so it has a chance to clear a lava pool; flag it so it can't loop
        drop.setDeltaMovement((rng.nextDouble() - 0.5D) * 0.1D, 0.28D, (rng.nextDouble() - 0.5D) * 0.1D);
        drop.setDefaultPickUpDelay();
        drop.addTag(PRODUCT_TAG);
        level.addFreshEntity(drop);
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private static boolean isTag(String s) {
        return s != null && s.startsWith("#");
    }

    private static ResourceLocation rl(String id) {
        return new ResourceLocation(id.startsWith("#") ? id.substring(1) : id);
    }

    private static TagKey<Item> tag(ResourceLocation id) {
        return TAG_CACHE.computeIfAbsent(id, k -> TagKey.create(Registry.ITEM_REGISTRY, k));
    }
}
