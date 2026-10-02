package io.github.mortuusars.exposure.backport.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Backport of 1.20.5+ {@code net.minecraft.network.codec.ByteBufCodecs}, plus the vanilla {@code X.STREAM_CODEC}
 * constants that 1.19.2 lacks ({@code ResourceLocation.STREAM_CODEC} becomes {@link #RESOURCE_LOCATION}, and so on).
 * Wire formats match 1.21's where it matters (var-ints, length-prefixed strings and lists).
 */
public final class ByteBufCodecs {
    private ByteBufCodecs() {
    }

    public static final int MAX_INITIAL_COLLECTION_SIZE = 65536;

    private static FriendlyByteBuf friendly(ByteBuf buf) {
        return buf instanceof FriendlyByteBuf f ? f : new FriendlyByteBuf(buf);
    }

    public static final StreamCodec<ByteBuf, Boolean> BOOL = StreamCodec.of(ByteBuf::writeBoolean, ByteBuf::readBoolean);
    public static final StreamCodec<ByteBuf, Byte> BYTE = StreamCodec.of((buf, v) -> buf.writeByte(v), ByteBuf::readByte);
    public static final StreamCodec<ByteBuf, Short> SHORT = StreamCodec.of((buf, v) -> buf.writeShort(v), ByteBuf::readShort);
    public static final StreamCodec<ByteBuf, Integer> INT = StreamCodec.of(ByteBuf::writeInt, ByteBuf::readInt);
    public static final StreamCodec<ByteBuf, Integer> VAR_INT = StreamCodec.of((buf, v) -> friendly(buf).writeVarInt(v), buf -> friendly(buf).readVarInt());
    public static final StreamCodec<ByteBuf, Long> LONG = StreamCodec.of(ByteBuf::writeLong, ByteBuf::readLong);
    public static final StreamCodec<ByteBuf, Long> VAR_LONG = StreamCodec.of((buf, v) -> friendly(buf).writeVarLong(v), buf -> friendly(buf).readVarLong());
    public static final StreamCodec<ByteBuf, Float> FLOAT = StreamCodec.of(ByteBuf::writeFloat, ByteBuf::readFloat);
    public static final StreamCodec<ByteBuf, Double> DOUBLE = StreamCodec.of(ByteBuf::writeDouble, ByteBuf::readDouble);
    public static final StreamCodec<ByteBuf, byte[]> BYTE_ARRAY = StreamCodec.of((buf, v) -> friendly(buf).writeByteArray(v), buf -> friendly(buf).readByteArray());
    public static final StreamCodec<ByteBuf, String> STRING_UTF8 = stringUtf8(32767);
    public static final StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = StreamCodec.of((buf, v) -> friendly(buf).writeNbt(v), buf -> {
        CompoundTag tag = friendly(buf).readAnySizeNbt();
        return tag != null ? tag : new CompoundTag();
    });
    public static final StreamCodec<ByteBuf, Optional<CompoundTag>> OPTIONAL_COMPOUND_TAG = optional(COMPOUND_TAG);

    // ---- vanilla X.STREAM_CODEC constants (1.20.5+), with vanilla's buffer type (RegistryFriendlyByteBuf -> FriendlyByteBuf) ----

    /** {@code ResourceLocation.STREAM_CODEC} */
    public static final StreamCodec<ByteBuf, ResourceLocation> RESOURCE_LOCATION = StreamCodec.of((buf, v) -> friendly(buf).writeResourceLocation(v), buf -> friendly(buf).readResourceLocation());
    /** {@code UUIDUtil.STREAM_CODEC} */
    public static final StreamCodec<ByteBuf, UUID> UUID = StreamCodec.of((buf, v) -> friendly(buf).writeUUID(v), buf -> friendly(buf).readUUID());
    /** {@code BlockPos.STREAM_CODEC} */
    public static final StreamCodec<ByteBuf, BlockPos> BLOCK_POS = StreamCodec.of((buf, v) -> friendly(buf).writeBlockPos(v), buf -> friendly(buf).readBlockPos());
    /** {@code ComponentSerialization.STREAM_CODEC} */
    public static final StreamCodec<FriendlyByteBuf, Component> COMPONENT = StreamCodec.of(FriendlyByteBuf::writeComponent, FriendlyByteBuf::readComponent);
    /** {@code ItemStack.STREAM_CODEC} / {@code ItemStack.OPTIONAL_STREAM_CODEC} (1.19.2 writes empty stacks as absent either way). */
    public static final StreamCodec<FriendlyByteBuf, ItemStack> ITEM_STACK = StreamCodec.of(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);
    /**
     * {@code SoundEvent.DIRECT_STREAM_CODEC}: by id. A sound that isn't registered on the receiving side is recreated
     * from its id (1.19.2 SoundEvents don't carry their range over the wire).
     */
    public static final StreamCodec<ByteBuf, net.minecraft.sounds.SoundEvent> SOUND_EVENT = RESOURCE_LOCATION.map(
            location -> net.minecraft.core.Registry.SOUND_EVENT.getOptional(location).orElseGet(() -> new net.minecraft.sounds.SoundEvent(location)),
            net.minecraft.sounds.SoundEvent::getLocation);

    /** {@code Tag.STREAM_CODEC}: any tag, wrapped in a compound because 1.19.2 only writes compounds. */
    public static final StreamCodec<ByteBuf, Tag> TAG = StreamCodec.of((buf, v) -> {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("v", v);
        friendly(buf).writeNbt(wrapper);
    }, buf -> {
        CompoundTag wrapper = friendly(buf).readAnySizeNbt();
        return wrapper != null && wrapper.contains("v") ? wrapper.get("v") : new CompoundTag();
    });

    /** {@code ResourceKey.streamCodec(registry)} */
    public static <T> StreamCodec<ByteBuf, net.minecraft.resources.ResourceKey<T>> resourceKey(
            net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> registryKey) {
        return RESOURCE_LOCATION.map(location -> net.minecraft.resources.ResourceKey.create(registryKey, location),
                net.minecraft.resources.ResourceKey::location);
    }

    public static StreamCodec<ByteBuf, byte[]> byteArray(int maxSize) {
        return StreamCodec.of((buf, v) -> {
            if (v.length > maxSize) {
                throw new IllegalArgumentException("ByteArray with size " + v.length + " is bigger than allowed " + maxSize);
            }
            friendly(buf).writeByteArray(v);
        }, buf -> friendly(buf).readByteArray(maxSize));
    }

    public static StreamCodec<ByteBuf, String> stringUtf8(int maxLength) {
        return StreamCodec.of((buf, v) -> friendly(buf).writeUtf(v, maxLength), buf -> friendly(buf).readUtf(maxLength));
    }

    /** Any DFU codec over the wire as NBT ({@code ByteBufCodecs.fromCodec} / {@code fromCodecWithRegistries}). */
    public static <T> StreamCodec<ByteBuf, T> fromCodec(Codec<T> codec) {
        return TAG.map(tag -> codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, err -> {
            throw new io.netty.handler.codec.DecoderException("Failed to decode: " + err + " " + tag);
        }), value -> codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, err -> {
            throw new io.netty.handler.codec.EncoderException("Failed to encode: " + err + " " + value);
        }));
    }

    public static <T> StreamCodec<ByteBuf, T> fromCodecWithRegistries(Codec<T> codec) {
        return fromCodec(codec);
    }

    public static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(StreamCodec<? super B, V> codec) {
        return StreamCodec.of((buf, v) -> {
            if (v.isPresent()) {
                buf.writeBoolean(true);
                codec.encode(buf, v.get());
            } else {
                buf.writeBoolean(false);
            }
        }, buf -> buf.readBoolean() ? Optional.of(codec.decode(buf)) : Optional.empty());
    }

    public static int readCount(ByteBuf buf, int maxSize) {
        int count = friendly(buf).readVarInt();
        if (count > maxSize) {
            throw new io.netty.handler.codec.DecoderException(count + " elements exceeded max size of: " + maxSize);
        }
        return count;
    }

    public static void writeCount(ByteBuf buf, int count, int maxSize) {
        if (count > maxSize) {
            throw new io.netty.handler.codec.EncoderException(count + " elements exceeded max size of: " + maxSize);
        }
        friendly(buf).writeVarInt(count);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
            IntFunction<C> factory, StreamCodec<? super B, V> codec) {
        return collection(factory, codec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec<B, C> collection(
            IntFunction<C> factory, StreamCodec<? super B, V> codec, int maxSize) {
        return StreamCodec.of((buf, collection) -> {
            writeCount(buf, collection.size(), maxSize);
            for (V v : collection) {
                codec.encode(buf, v);
            }
        }, buf -> {
            int count = readCount(buf, maxSize);
            C collection = factory.apply(Math.min(count, MAX_INITIAL_COLLECTION_SIZE));
            for (int i = 0; i < count; i++) {
                collection.add(codec.decode(buf));
            }
            return collection;
        });
    }

    public static <B extends ByteBuf, V, C extends Collection<V>> StreamCodec.CodecOperation<B, V, C> collection(IntFunction<C> factory) {
        return codec -> collection(factory, codec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return codec -> collection(ArrayList::new, codec);
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list(int maxSize) {
        return codec -> collection(ArrayList::new, codec, maxSize);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
            IntFunction<? extends M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec) {
        return map(factory, keyCodec, valueCodec, Integer.MAX_VALUE);
    }

    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> map(
            IntFunction<? extends M> factory, StreamCodec<? super B, K> keyCodec, StreamCodec<? super B, V> valueCodec, int maxSize) {
        return StreamCodec.of((buf, map) -> {
            writeCount(buf, map.size(), maxSize);
            map.forEach((k, v) -> {
                keyCodec.encode(buf, k);
                valueCodec.encode(buf, v);
            });
        }, buf -> {
            int count = readCount(buf, maxSize);
            M map = factory.apply(Math.min(count, MAX_INITIAL_COLLECTION_SIZE));
            for (int i = 0; i < count; i++) {
                K k = keyCodec.decode(buf);
                V v = valueCodec.decode(buf);
                map.put(k, v);
            }
            return map;
        });
    }

    public static <T> StreamCodec<ByteBuf, T> idMapper(IntFunction<T> idLookup, ToIntFunction<T> idGetter) {
        return StreamCodec.of((buf, v) -> friendly(buf).writeVarInt(idGetter.applyAsInt(v)),
                buf -> idLookup.apply(friendly(buf).readVarInt()));
    }
}
