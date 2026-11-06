package com.SaintAlfonzo.creativedelight.food;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.SaintAlfonzo.creativedelight.CDConfig;

import javax.annotation.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Pack seasonings: a food carries the seasonings put on it as NBT {@code Seasonings:["ns:id", ...]} (sorted, so the
 * order they went on doesn't split stacks), and eating it adds each one's bonus on top of the food. Definitions come
 * from KubeJS at startup ({@code development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js} -> {@link #define}), on both sides: the
 * server needs them to match {@link SeasoningRecipe} and apply the bonus, the client for {@link SeasoningTooltip}.
 * Salt's {@code Salted} is its own mechanism and untouched.
 */
public final class Seasonings {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String TAG = "Seasonings";

    /** One effect a seasoning gives, duration in ticks. */
    public record Effect(ResourceLocation effect, int duration, int amplifier) {
    }

    /**
     * A seasoning's bonus: {@code nutrition} hunger points, {@code saturation} saturation points (absolute, capped at
     * the food level like vanilla), {@code heal} health points, timed effects. {@code combines}: the seasonings it may
     * share a food with (either side listing the other is enough). {@code name}: the display name from the script, used
     * by the tooltip when the lang key {@code seasoning.<ns>.<path>} is missing (empty when the script gave none).
     */
    public record Definition(ResourceLocation id, int nutrition, float saturation, float heal, List<Effect> effects,
                             Set<ResourceLocation> combines, String name, Kinds kinds) {
        /** True if the food's own properties change (eating speed, always edible, effect transform). */
        public boolean changesFood() {
            return kinds.fast() || kinds.alwaysEdible() || !kinds.transform().identity();
        }
    }

    /**
     * The seasoning kinds added by the Barbeque's Delight fold (food/seasoning #11, barbeques_delight.md 4.1):
     * {@code fast} eating (16 ticks), {@code alwaysEdible} (eat when full), {@code damage} health points on eating of
     * {@code damageType} ("fire", "magic" or "generic"), an effect {@code transform} that rewrites the food's own effects,
     * {@code station} (put on by a station such as the Grill, never by a craft; counts toward the cap only if
     * {@code [food] grilledCountsTowardMax}) and {@code combinesAll} (shares a food with any seasoning).
     */
    public record Kinds(boolean fast, boolean alwaysEdible, float damage, String damageType, Transform transform,
                        boolean station, boolean combinesAll) {
        static final Kinds NONE = new Kinds(false, false, 0f, "fire", Transform.IDENTITY, false, false);
    }

    /** Each of the food's own effects: chance times {@code chance}, duration times {@code duration}, amplifier plus {@code amplifier}. */
    public record Transform(float chance, float duration, int amplifier) {
        static final Transform IDENTITY = new Transform(1f, 1f, 0);

        public boolean identity() {
            return chance == 1f && duration == 1f && amplifier == 0;
        }
    }

    private static final Map<ResourceLocation, Definition> DEFINITIONS = new LinkedHashMap<>();

    private Seasonings() {}

    /** Registers (or replaces) one seasoning from its JSON; called by the KubeJS startup script. */
    public static void define(String json) {
        try {
            JsonObject o = JsonParser.parseString(json).getAsJsonObject();
            ResourceLocation id = new ResourceLocation(o.get("id").getAsString());
            List<Effect> effects = new ArrayList<>();
            if (o.has("effects")) {
                for (JsonElement e : o.getAsJsonArray("effects")) {
                    JsonObject eo = e.getAsJsonObject();
                    effects.add(new Effect(new ResourceLocation(eo.get("effect").getAsString()),
                            eo.has("duration") ? eo.get("duration").getAsInt() : 200,
                            eo.has("amplifier") ? eo.get("amplifier").getAsInt() : 0));
                }
            }
            Set<ResourceLocation> combines = new HashSet<>();
            if (o.has("combines")) {
                JsonArray arr = o.getAsJsonArray("combines");
                for (JsonElement e : arr) {
                    combines.add(new ResourceLocation(e.getAsString()));
                }
            }
            Transform transform = Transform.IDENTITY;
            if (o.has("transform") && o.get("transform").isJsonObject()) {
                JsonObject t = o.getAsJsonObject("transform");
                transform = new Transform(t.has("chance") ? t.get("chance").getAsFloat() : 1f,
                        t.has("duration") ? t.get("duration").getAsFloat() : 1f,
                        t.has("amplifier") ? t.get("amplifier").getAsInt() : 0);
            }
            Kinds kinds = new Kinds(bool(o, "fast"), bool(o, "alwaysEdible"),
                    o.has("damage") ? o.get("damage").getAsFloat() : 0f,
                    o.has("damageType") ? o.get("damageType").getAsString() : "fire",
                    transform, bool(o, "station"), bool(o, "combinesAll"));
            Definition def = new Definition(id,
                    o.has("nutrition") ? o.get("nutrition").getAsInt() : 0,
                    o.has("saturation") ? o.get("saturation").getAsFloat() : 0f,
                    o.has("heal") ? o.get("heal").getAsFloat() : 0f,
                    List.copyOf(effects), Set.copyOf(combines),
                    o.has("name") ? o.get("name").getAsString() : "", kinds);
            DEFINITIONS.put(id, def);
            LOGGER.info("[seasoning] defined {}", id);
        } catch (Exception e) {
            LOGGER.error("[seasoning] bad definition {}", json, e);
        }
    }

    /** Every defined seasoning id, in definition order. */
    public static List<ResourceLocation> ids() {
        return List.copyOf(DEFINITIONS.keySet());
    }

    /**
     * The tag listing the foods a seasoning may go on, {@code #<ns>:seasonable/<path>}: built from food families plus
     * single adds and removals by Creative Delight's {@code creativeengineering/seasoning_tags.js}.
     */
    public static TagKey<Item> seasonableTag(ResourceLocation id) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(id.getNamespace(), "seasonable/" + id.getPath()));
    }

    /** True if the food is in the seasonable tag of every one of {@code adding}. */
    public static boolean accepts(ItemStack food, List<ResourceLocation> adding) {
        for (ResourceLocation id : adding) {
            if (!food.is(seasonableTag(id))) {
                return false;
            }
        }
        return true;
    }

    public static Definition get(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }

    /** True if both are defined, differ, and at least one lists the other in {@code combines}. */
    public static boolean canCombine(ResourceLocation a, ResourceLocation b) {
        Definition da = DEFINITIONS.get(a);
        Definition db = DEFINITIONS.get(b);
        return da != null && db != null && !a.equals(b) && (da.combines().contains(b) || db.combines().contains(a)
                || da.kinds().combinesAll() || db.kinds().combinesAll());
    }

    /**
     * True if every one of {@code adding} is defined, new to the food, every pair among the food's seasonings plus
     * {@code adding} combines, and the result is within the cap ({@code [food] maxSeasonings}; station seasonings such as
     * {@code grilled} count only with {@code [food] grilledCountsTowardMax}). Seasonings that are no longer defined
     * count toward nothing.
     */
    public static boolean canAdd(List<ResourceLocation> existing, List<ResourceLocation> adding) {
        List<ResourceLocation> all = new ArrayList<>(existing);
        for (ResourceLocation id : adding) {
            if (DEFINITIONS.get(id) == null || all.contains(id)) {
                return false;
            }
            for (ResourceLocation other : all) {
                if (!canCombine(id, other)) {
                    return false;
                }
            }
            all.add(id);
        }
        return counted(all) <= CDConfig.FOOD.maxSeasonings();
    }

    /** How many of {@code ids} count toward the cap. */
    public static int counted(List<ResourceLocation> ids) {
        boolean stations = CDConfig.FOOD.grilledCountsTowardMax();
        int n = 0;
        for (ResourceLocation id : ids) {
            Definition def = DEFINITIONS.get(id);
            if (def != null && (stations || !def.kinds().station())) {
                n++;
            }
        }
        return n;
    }

    /** True if the seasoning is defined and put on by a station only (no craft). */
    public static boolean isStation(ResourceLocation id) {
        Definition def = DEFINITIONS.get(id);
        return def != null && def.kinds().station();
    }

    /**
     * A station (the Barbeque's Delight Grill) puts {@code id} on {@code stack}: added if it is defined and
     * {@link #canAdd} allows it. Returns whether it was added. Called by members through reflection (they do not
     * compile against Creative Delight).
     */
    public static boolean addFromStation(ItemStack stack, ResourceLocation id) {
        if (!canAdd(of(stack), List.of(id))) {
            return false;
        }
        with(stack, List.of(id));
        return true;
    }

    /** {@code stack} (modified) without {@code id}; the tag is dropped when the list is left empty. */
    public static ItemStack without(ItemStack stack, ResourceLocation id) {
        List<ResourceLocation> ids = new ArrayList<>(of(stack));
        if (!ids.remove(id)) {
            return stack;
        }
        CompoundTag tag = stack.getTag();
        if (ids.isEmpty()) {
            if (tag != null) {
                tag.remove(TAG);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            }
            return stack;
        }
        ListTag list = new ListTag();
        for (ResourceLocation x : ids) {
            list.add(StringTag.valueOf(x.toString()));
        }
        stack.getOrCreateTag().put(TAG, list);
        return stack;
    }

    /**
     * The food's properties with its seasonings' fast, always-edible and transform kinds applied ({@code base} itself
     * when none of them changes the food). Items whose {@code getFoodProperties(stack, entity)} calls this get the
     * kinds: the Barbeque's Delight skewers and combos (through reflection). Transforms apply in NBT order.
     */
    public static FoodProperties foodProperties(ItemStack stack, @Nullable FoodProperties base) {
        if (base == null) {
            return null;
        }
        List<Definition> defs = new ArrayList<>();
        for (ResourceLocation id : of(stack)) {
            Definition def = DEFINITIONS.get(id);
            if (def != null && def.changesFood()) {
                defs.add(def);
            }
        }
        if (defs.isEmpty()) {
            return base;
        }
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(base.getNutrition())
                .saturationMod(base.getSaturationModifier());
        if (base.isMeat()) {
            b.meat();
        }
        boolean fast = base.isFastFood();
        boolean always = base.canAlwaysEat();
        for (Definition def : defs) {
            fast |= def.kinds().fast();
            always |= def.kinds().alwaysEdible();
        }
        if (fast) {
            b.fast();
        }
        if (always) {
            b.alwaysEat();
        }
        for (Pair<MobEffectInstance, Float> e : base.getEffects()) {
            MobEffectInstance ins = e.getFirst();
            if (ins == null) {
                continue;
            }
            float chance = e.getSecond();
            float dur = ins.getDuration();
            int amp = ins.getAmplifier();
            for (Definition def : defs) {
                Transform t = def.kinds().transform();
                chance *= t.chance();
                dur *= t.duration();
                amp += t.amplifier();
            }
            MobEffect effect = ins.getEffect();
            int duration = Math.max(1, Math.round(dur));
            int amplifier = Math.max(0, amp);
            b.effect(() -> new MobEffectInstance(effect, duration, amplifier), Math.min(1f, chance));
        }
        return b.build();
    }

    private static boolean bool(JsonObject o, String key) {
        return o.has(key) && o.get(key).getAsBoolean();
    }

    /** The seasonings on a stack, in NBT order (empty if none). */
    public static List<ResourceLocation> of(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_LIST)) {
            return List.of();
        }
        ListTag list = tag.getList(TAG, Tag.TAG_STRING);
        List<ResourceLocation> out = new ArrayList<>(list.size());
        for (int n = 0; n < list.size(); n++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(n));
            if (id != null) {
                out.add(id);
            }
        }
        return out;
    }

    /** {@code stack} (modified) with {@code adding} added to its seasonings, the list kept sorted. */
    public static ItemStack with(ItemStack stack, Collection<ResourceLocation> adding) {
        Set<ResourceLocation> all = new TreeSet<>(Comparator.comparing(ResourceLocation::toString));
        all.addAll(of(stack));
        all.addAll(adding);
        ListTag list = new ListTag();
        for (ResourceLocation id : all) {
            list.add(StringTag.valueOf(id.toString()));
        }
        stack.getOrCreateTag().put(TAG, list);
        return stack;
    }

    /**
     * Eating a seasoned food: each seasoning's bonus after the food's own. The event's stack is the copy taken before
     * the bite, so the NBT is still there.
     */
    @SubscribeEvent
    public static void onEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level.isClientSide) {
            return;
        }
        ItemStack stack = event.getItem();
        if (!stack.isEdible()) {
            return;
        }
        applyEaten(player, stack);
    }

    /**
     * Each seasoning's on-eat bonus for {@code stack}. Public for the Barbeque's Delight combos, which run it for every
     * skewer they hold (through reflection).
     */
    public static void applyEaten(Player player, ItemStack stack) {
        for (ResourceLocation id : of(stack)) {
            Definition def = DEFINITIONS.get(id);
            if (def != null) {
                apply(player, def);
            }
        }
    }

    private static void apply(Player player, Definition def) {
        // damage before heal, as Barbeque's Delight ran its chili / buffalo burn before cumin's heal
        if (def.kinds().damage() > 0) {
            DamageSource source = switch (def.kinds().damageType()) {
                case "magic" -> DamageSource.MAGIC;
                case "generic" -> DamageSource.GENERIC;
                default -> DamageSource.IN_FIRE;
            };
            player.hurt(source, def.kinds().damage());
        }
        FoodData food = player.getFoodData();
        if (def.nutrition() > 0) {
            food.setFoodLevel(Math.min(20, food.getFoodLevel() + def.nutrition()));
        }
        if (def.saturation() > 0) {
            food.setSaturation(Math.min(food.getFoodLevel(), food.getSaturationLevel() + def.saturation()));
        }
        if (def.heal() > 0) {
            player.heal(def.heal());
        }
        for (Effect e : def.effects()) {
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.effect());
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect, e.duration(), e.amplifier()));
            }
        }
    }
}
