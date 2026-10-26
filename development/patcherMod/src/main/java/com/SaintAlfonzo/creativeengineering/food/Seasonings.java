package com.SaintAlfonzo.creativeengineering.food;

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
import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
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
     * share a food with (either side listing the other is enough).
     */
    public record Definition(ResourceLocation id, int nutrition, float saturation, float heal, List<Effect> effects,
                             Set<ResourceLocation> combines) {
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
            Definition def = new Definition(id,
                    o.has("nutrition") ? o.get("nutrition").getAsInt() : 0,
                    o.has("saturation") ? o.get("saturation").getAsFloat() : 0f,
                    o.has("heal") ? o.get("heal").getAsFloat() : 0f,
                    List.copyOf(effects), Set.copyOf(combines));
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
        return da != null && db != null && !a.equals(b) && (da.combines().contains(b) || db.combines().contains(a));
    }

    /**
     * True if every one of {@code adding} is defined, new to the food, and every pair among the food's seasonings plus
     * {@code adding} combines.
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
        return true;
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
        for (ResourceLocation id : of(stack)) {
            Definition def = DEFINITIONS.get(id);
            if (def != null) {
                apply(player, def);
            }
        }
    }

    private static void apply(Player player, Definition def) {
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
