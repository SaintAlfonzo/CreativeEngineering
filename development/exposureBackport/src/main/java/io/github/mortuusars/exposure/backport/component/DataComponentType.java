package io.github.mortuusars.exposure.backport.component;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/**
 * Backport of 1.20.5+ item data components, stored in ItemStack NBT.
 * <p>
 * Each component is one tag entry keyed by its id ({@code "exposure:camera_film"}), written with the component's
 * codec through {@link NbtOps}. The accessors are instance methods, so upstream's {@code stack.get(TYPE)} reads
 * {@code TYPE.get(stack)} here. Semantics follow vanilla: {@link #get} falls back to the item's default (what
 * 1.21 sets with {@code Item.Properties.component(...)}, here {@link #setDefault}), and {@link #set} with null removes.
 * <p>
 * ItemStack NBT syncs to clients by itself; the {@code networkSynchronized(...)} codec is only kept for
 * {@link #streamCodec()}.
 */
public class DataComponentType<T> {
    private final ResourceLocation id;
    private final String key;
    private final Codec<T> codec;
    private final StreamCodec<? super net.minecraft.network.FriendlyByteBuf, T> streamCodec;
    private final Map<Item, T> defaults = new IdentityHashMap<>();

    /** Every component type created, for {@link #applyComponents}. */
    private static final java.util.List<DataComponentType<?>> ALL = new java.util.concurrent.CopyOnWriteArrayList<>();

    protected DataComponentType(ResourceLocation id, Codec<T> codec) {
        this(id, codec, null);
    }

    @SuppressWarnings("unchecked")
    protected DataComponentType(ResourceLocation id, Codec<T> codec, @Nullable StreamCodec<? super net.minecraft.network.FriendlyByteBuf, T> streamCodec) {
        this.id = id;
        this.key = id.toString();
        this.codec = codec;
        ALL.add(this);
        this.streamCodec = streamCodec != null ? streamCodec
                : (StreamCodec<? super net.minecraft.network.FriendlyByteBuf, T>) (StreamCodec<?, T>) io.github.mortuusars.exposure.backport.codec.ByteBufCodecs.fromCodec(codec);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public ResourceLocation id() {
        return id;
    }

    /** NBT key in the stack's tag. */
    public String key() {
        return key;
    }

    public Codec<T> codec() {
        return codec;
    }

    public Codec<T> codecOrThrow() {
        return codec;
    }

    /** The network codec given to {@code networkSynchronized}, or the NBT codec over the wire. */
    @SuppressWarnings("unchecked")
    public StreamCodec<net.minecraft.network.FriendlyByteBuf, T> streamCodec() {
        return (StreamCodec<net.minecraft.network.FriendlyByteBuf, T>) streamCodec;
    }

    // ---- defaults (Item.Properties.component) ----

    /** Equivalent of 1.21's {@code new Item.Properties().component(type, value)} for {@code item}. */
    public DataComponentType<T> setDefault(ItemLike item, T value) {
        defaults.put(item.asItem(), value);
        return this;
    }

    public @Nullable T getDefault(Item item) {
        return defaults.get(item);
    }

    /** Registers a default value on an item while it's being created; returns the item (for registry suppliers). */
    public static <I extends Item, T> I withDefault(I item, DataComponentType<T> type, T value) {
        type.setDefault(item, value);
        return item;
    }

    // ---- ItemStack accessors (stack.get(TYPE) -> TYPE.get(stack)) ----

    public @Nullable T get(ItemStack stack) {
        @Nullable T stored = getStored(stack);
        return stored != null ? stored : (stack.isEmpty() ? null : defaults.get(stack.getItem()));
    }

    public T getOrDefault(ItemStack stack, T defaultValue) {
        @Nullable T value = get(stack);
        return value != null ? value : defaultValue;
    }

    public Optional<T> getOptional(ItemStack stack) {
        return Optional.ofNullable(get(stack));
    }

    /** True if the stack has the component, either stored in NBT or as the item's default. */
    public boolean has(ItemStack stack) {
        return isStored(stack) || (!stack.isEmpty() && defaults.containsKey(stack.getItem()));
    }

    /** True only if the stack's NBT carries the component (1.21: present in the component patch). */
    public boolean isStored(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(key);
    }

    // Storage. Vanilla components that already have a 1.19.2 NBT home (display name, EntityTag) override these.

    public @Nullable T set(ItemStack stack, @Nullable T value) {
        @Nullable T previous = get(stack);
        if (value == null) {
            removeStored(stack);
            return previous;
        }
        setStored(stack, value);
        return previous;
    }

    public @Nullable T remove(ItemStack stack) {
        @Nullable T previous = get(stack);
        removeStored(stack);
        return previous;
    }

    public @Nullable T update(ItemStack stack, T defaultValue, UnaryOperator<T> updater) {
        return set(stack, updater.apply(getOrDefault(stack, defaultValue)));
    }

    public <U> @Nullable T update(ItemStack stack, T defaultValue, U argument, java.util.function.BiFunction<T, U, T> updater) {
        return set(stack, updater.apply(getOrDefault(stack, defaultValue), argument));
    }

    /** Copies this component (stored value only) from one stack to another, like 1.21's {@code copyFrom}. */
    public void copy(ItemStack from, ItemStack to) {
        CompoundTag fromTag = from.getTag();
        if (fromTag != null && fromTag.contains(key)) {
            to.getOrCreateTag().put(key, fromTag.get(key).copy());
        } else {
            removeStored(to);
        }
    }

    protected void setStored(ItemStack stack, T value) {
        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value)
                .getOrThrow(false, err -> Exposure.LOGGER.error("Cannot encode component '{}' value '{}': {}", key, value, err));
        stack.getOrCreateTag().put(key, encoded);
    }

    protected void removeStored(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(key)) {
            tag.remove(key);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    protected @Nullable T getStored(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key)) {
            return null;
        }
        Tag encoded = tag.get(key);
        return codec.parse(NbtOps.INSTANCE, encoded)
                .resultOrPartial(err -> Exposure.LOGGER.error("Cannot decode component '{}' from '{}': {}", key, encoded, err))
                .orElse(null);
    }

    /**
     * {@code to.applyComponents(from.getComponents())}: everything {@code from} carries goes onto {@code to}: its NBT
     * (merged over {@code to}'s) and the item defaults of {@code from} that it doesn't store itself.
     */
    public static ItemStack applyComponents(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) {
            return to;
        }
        if (from.getTag() != null) {
            to.getOrCreateTag().merge(from.getTag().copy());
        }
        for (DataComponentType<?> type : ALL) {
            type.copyDefaultIfAbsent(from, to);
        }
        return to;
    }

    private void copyDefaultIfAbsent(ItemStack from, ItemStack to) {
        T value = defaults.get(from.getItem());
        if (value != null && !isStored(from) && !isStored(to)) {
            setStored(to, value);
        }
    }

    @Override
    public String toString() {
        return key;
    }

    public static class Builder<T> {
        private @Nullable Codec<T> codec;
        private @Nullable StreamCodec<? super net.minecraft.network.FriendlyByteBuf, T> streamCodec;

        public Builder<T> persistent(Codec<T> codec) {
            this.codec = codec;
            return this;
        }

        /** Kept for {@link #streamCodec()} (CameraSetting sends values with it); stacks themselves sync as NBT. */
        public Builder<T> networkSynchronized(StreamCodec<? super net.minecraft.network.FriendlyByteBuf, T> streamCodec) {
            this.streamCodec = streamCodec;
            return this;
        }

        public Builder<T> cacheEncoding() {
            return this;
        }

        public DataComponentType<T> build(ResourceLocation id) {
            Preconditions.checkNotNull(codec, "Component '%s' needs a persistent codec: it is stored in NBT", id);
            return new DataComponentType<>(id, codec, streamCodec);
        }
    }
}
