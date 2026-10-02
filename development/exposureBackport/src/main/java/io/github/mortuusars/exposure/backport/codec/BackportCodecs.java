package io.github.mortuusars.exposure.backport.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * DFU / vanilla codec pieces that 1.19.2 (DFU 5) lacks: sized lists, validated codecs, and codecs for vanilla types
 * that only got one in 1.20+ (item/location predicates, int bounds, components, tags). Vanilla predicates go
 * through their own JSON (de)serializers, so data written for upstream parses the same way here.
 */
public final class BackportCodecs {
    private BackportCodecs() {
    }

    // ---- DFU 6+/7+ ----

    /** {@code codec.listOf(min, max)} */
    public static <T> Codec<List<T>> listOf(Codec<T> codec, int minSize, int maxSize) {
        return validate(codec.listOf(), list -> {
            if (list.size() < minSize) {
                return DataResult.error("List is too short: " + list.size() + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            if (list.size() > maxSize) {
                return DataResult.error("List is too long: " + list.size() + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            return DataResult.success(list);
        });
    }

    /** {@code codec.sizeLimitedListOf(max)} */
    public static <T> Codec<List<T>> sizeLimitedListOf(Codec<T> codec, int maxSize) {
        return listOf(codec, 0, maxSize);
    }

    /** {@code codec.validate(checker)} */
    public static <T> Codec<T> validate(Codec<T> codec, Function<T, DataResult<T>> checker) {
        return codec.flatXmap(checker, checker);
    }

    /** {@code Codec.string(min, max)} */
    public static Codec<String> string(int minLength, int maxLength) {
        return validate(Codec.STRING, s -> s.length() < minLength || s.length() > maxLength
                ? DataResult.error("String \"" + s + "\" length is outside [" + minLength + "-" + maxLength + "]")
                : DataResult.success(s));
    }

    /** {@code Codec.lazyInitialized(supplier)} */
    public static <T> Codec<T> lazyInitialized(Supplier<Codec<T>> supplier) {
        com.google.common.base.Supplier<Codec<T>> memoized = com.google.common.base.Suppliers.memoize(supplier::get);
        return Codec.PASSTHROUGH.flatXmap(dynamic -> memoized.get().parse(dynamic), value -> memoized.get()
                .encodeStart(JsonOps.INSTANCE, value).map(json -> new Dynamic<>(JsonOps.INSTANCE, json)));
    }

    /** {@code Codec.withAlternative(primary, alternative)}: decode with either, encode with the primary. */
    public static <T> Codec<T> withAlternative(Codec<T> primary, Codec<? extends T> alternative) {
        return Codec.either(primary, alternative).xmap(either -> either.map(Function.identity(), Function.identity()),
                com.mojang.datafixers.util.Either::left);
    }

    /** {@code Either::unwrap} (DFU 6+) */
    public static <T> T unwrap(com.mojang.datafixers.util.Either<? extends T, ? extends T> either) {
        return either.map(Function.identity(), Function.identity());
    }

    /** {@code dataResult.getOrThrow()} (DFU 7): the value, or IllegalStateException with the error message. */
    public static <T> T getOrThrow(DataResult<T> result) {
        return result.getOrThrow(false, err -> {
        });
    }

    public static boolean isError(DataResult<?> result) {
        return result.error().isPresent();
    }

    public static boolean isSuccess(DataResult<?> result) {
        return result.error().isEmpty();
    }

    // ---- vanilla types without a codec in 1.19.2 ----

    /** {@code UUIDUtil.STRING_CODEC}: dashed UUID string. */
    public static final Codec<java.util.UUID> UUID_STRING = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(java.util.UUID.fromString(string));
        } catch (IllegalArgumentException e) {
            return DataResult.error("Invalid UUID " + string + ": " + e.getMessage());
        }
    }, java.util.UUID::toString);

    /** {@code UUIDUtil.LENIENT_CODEC}: int array or string; encodes as the int array. */
    public static final Codec<java.util.UUID> UUID_LENIENT = withAlternative(net.minecraft.core.UUIDUtil.CODEC, UUID_STRING);

    /** {@code ExtraCodecs.JSON} */
    public static final Codec<JsonElement> JSON = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE, json));

    /** {@code Tag.CODEC} */
    public static final Codec<Tag> TAG = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue(), tag -> new Dynamic<>(NbtOps.INSTANCE, tag));

    /** {@code TagParser.LENIENT_CODEC}: a compound tag as an object or as an SNBT string; encodes as SNBT. */
    public static final Codec<net.minecraft.nbt.CompoundTag> LENIENT_COMPOUND_TAG = withAlternative(
            Codec.STRING.comapFlatMap(snbt -> {
                try {
                    return DataResult.success(net.minecraft.nbt.TagParser.parseTag(snbt));
                } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                    return DataResult.error("Not SNBT: " + snbt + ": " + e.getMessage());
                }
            }, net.minecraft.nbt.CompoundTag::toString),
            net.minecraft.nbt.CompoundTag.CODEC);

    /** {@code ComponentSerialization.CODEC} */
    public static final Codec<Component> COMPONENT = JSON.flatXmap(json -> {
        try {
            Component component = Component.Serializer.fromJson(json);
            return component != null ? DataResult.success(component) : DataResult.error("Not a component: " + json);
        } catch (Exception e) {
            return DataResult.error("Not a component: " + json + ": " + e.getMessage());
        }
    }, component -> DataResult.success(Component.Serializer.toJsonTree(component)));

    /** {@code ItemPredicate.CODEC}: reads 1.21-style JSON too, see {@link #itemPredicateFromJson}. */
    public static final Codec<ItemPredicate> ITEM_PREDICATE = jsonBacked(BackportCodecs::itemPredicateFromJson, ItemPredicate::serializeToJson, "item predicate");

    /**
     * {@link ItemPredicate#fromJson} that also takes the 1.20.5+ shape, so upstream data parses unchanged:
     * <ul>
     * <li>{@code "items": "id"} / {@code "items": "#tag"} -> {@code "items": ["id"]} / {@code "tag": "tag"}</li>
     * <li>{@code "predicates": {"exposure:frame": {...}}} (item sub-predicates) -> Forge's custom item predicate
     * {@code {"type": "exposure:frame", "frame": {...}, <the rest>}} (one sub-predicate per predicate)</li>
     * </ul>
     */
    public static ItemPredicate itemPredicateFromJson(@org.jetbrains.annotations.Nullable JsonElement json) {
        if (json == null || !json.isJsonObject()) {
            return ItemPredicate.fromJson(json);
        }
        JsonObject obj = json.getAsJsonObject().deepCopy();
        JsonElement items = obj.get("items");
        if (items != null && items.isJsonPrimitive()) {
            String value = items.getAsString();
            obj.remove("items");
            if (value.startsWith("#")) {
                obj.addProperty("tag", value.substring(1));
            } else {
                com.google.gson.JsonArray array = new com.google.gson.JsonArray();
                array.add(value);
                obj.add("items", array);
            }
        }
        JsonElement predicates = obj.remove("predicates");
        if (predicates != null && predicates.isJsonObject() && predicates.getAsJsonObject().size() > 0) {
            var entries = predicates.getAsJsonObject().entrySet();
            if (entries.size() > 1) {
                io.github.mortuusars.exposure.Exposure.LOGGER.warn("Item predicate has {} sub-predicates, 1.19.2 supports one; using the first: {}", entries.size(), json);
            }
            var first = entries.iterator().next();
            obj.addProperty("type", first.getKey());
            obj.add(new net.minecraft.resources.ResourceLocation(first.getKey()).getPath(), first.getValue());
        }
        return ItemPredicate.fromJson(obj);
    }

    /** {@code LocationPredicate.CODEC} */
    public static final Codec<LocationPredicate> LOCATION_PREDICATE = jsonBacked(LocationPredicate::fromJson, LocationPredicate::serializeToJson, "location predicate");

    /** {@code MinMaxBounds.Ints.CODEC} */
    public static final Codec<MinMaxBounds.Ints> INTS = jsonBacked(MinMaxBounds.Ints::fromJson, MinMaxBounds.Ints::serializeToJson, "int bounds");

    /** {@code MinMaxBounds.Doubles.CODEC} */
    public static final Codec<MinMaxBounds.Doubles> DOUBLES = jsonBacked(MinMaxBounds.Doubles::fromJson, MinMaxBounds.Doubles::serializeToJson, "double bounds");

    public static <T> Codec<T> jsonBacked(Function<JsonElement, T> fromJson, Function<T, JsonElement> toJson, String what) {
        return JSON.flatXmap(json -> {
            try {
                return DataResult.success(fromJson.apply(json));
            } catch (Exception e) {
                return DataResult.error("Cannot parse " + what + " from " + json + ": " + e.getMessage());
            }
        }, value -> {
            JsonElement json = toJson.apply(value);
            return DataResult.success(json != null ? json : JsonNull.INSTANCE);
        });
    }

    public static JsonObject emptyJsonObject() {
        return new JsonObject();
    }
}
