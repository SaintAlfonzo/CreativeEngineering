package io.github.mortuusars.exposure.backport.codec;

import com.mojang.datafixers.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Backport of 1.20.5+ {@code net.minecraft.network.codec.StreamCodec}: a paired buffer encoder/decoder.
 * Same shape and method names as vanilla's, so upstream Exposure codecs port with only an import change.
 * {@code B} is the buffer type (ByteBuf or FriendlyByteBuf; 1.21's RegistryFriendlyByteBuf maps to FriendlyByteBuf).
 */
public interface StreamCodec<B, V> extends StreamDecoder<B, V>, StreamEncoder<B, V> {
    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(B buffer, V value) {
                encoder.encode(buffer, value);
            }
        };
    }

    static <B, V> StreamCodec<B, V> ofMember(StreamMemberEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return of((buffer, value) -> encoder.encode(value, buffer), decoder);
    }

    static <B, V> StreamCodec<B, V> unit(V instance) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buffer) {
                return instance;
            }

            @Override
            public void encode(B buffer, V value) {
                if (!value.equals(instance)) {
                    throw new IllegalStateException("Can't encode '" + value + "', expected '" + instance + "'");
                }
            }
        };
    }

    default <O> StreamCodec<B, O> apply(CodecOperation<B, V, O> operation) {
        return operation.apply(this);
    }

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> factory, Function<? super O, ? extends V> getter) {
        StreamCodec<B, V> self = this;
        return new StreamCodec<>() {
            @Override
            public O decode(B buffer) {
                return factory.apply(self.decode(buffer));
            }

            @Override
            public void encode(B buffer, O value) {
                self.encode(buffer, getter.apply(value));
            }
        };
    }

    default <O> StreamCodec<O, V> mapStream(Function<O, ? extends B> bufferFactory) {
        StreamCodec<B, V> self = this;
        return new StreamCodec<>() {
            @Override
            public V decode(O buffer) {
                return self.decode(bufferFactory.apply(buffer));
            }

            @Override
            public void encode(O buffer, V value) {
                self.encode(bufferFactory.apply(buffer), value);
            }
        };
    }

    default <U> StreamCodec<B, U> dispatch(Function<? super U, ? extends V> typeGetter,
                                           Function<? super V, ? extends StreamCodec<? super B, ? extends U>> codecGetter) {
        StreamCodec<B, V> self = this;
        return new StreamCodec<>() {
            @Override
            public U decode(B buffer) {
                V type = self.decode(buffer);
                StreamCodec<? super B, ? extends U> codec = codecGetter.apply(type);
                return codec.decode(buffer);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void encode(B buffer, U value) {
                V type = typeGetter.apply(value);
                StreamCodec<B, U> codec = (StreamCodec<B, U>) codecGetter.apply(type);
                self.encode(buffer, type);
                codec.encode(buffer, value);
            }
        };
    }

    static <B, T> StreamCodec<B, T> recursive(java.util.function.UnaryOperator<StreamCodec<B, T>> modifier) {
        return new StreamCodec<>() {
            private final Supplier<StreamCodec<B, T>> inner = com.google.common.base.Suppliers.memoize(() -> modifier.apply(this));

            @Override
            public T decode(B buffer) {
                return inner.get().decode(buffer);
            }

            @Override
            public void encode(B buffer, T value) {
                inner.get().encode(buffer, value);
            }
        };
    }

    default <S extends B> StreamCodec<S, V> cast() {
        @SuppressWarnings("unchecked") StreamCodec<S, V> self = (StreamCodec<S, V>) this;
        return self;
    }

    @FunctionalInterface
    interface CodecOperation<B, S, T> {
        StreamCodec<B, T> apply(StreamCodec<B, S> codec);
    }

    static <B, C, T1> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            Function<T1, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                return factory.apply(v1);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
            }
        };
    }

    static <B, C, T1, T2> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            java.util.function.BiFunction<T1, T2, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                return factory.apply(v1, v2);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            Function3<T1, T2, T3, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                return factory.apply(v1, v2, v3);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            Function4<T1, T2, T3, T4, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                return factory.apply(v1, v2, v3, v4);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            Function5<T1, T2, T3, T4, T5, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            Function6<T1, T2, T3, T4, T5, T6, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                T6 v6 = codec6.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            Function7<T1, T2, T3, T4, T5, T6, T7, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                T6 v6 = codec6.decode(buffer);
                T7 v7 = codec7.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6, v7);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
            }
        };
    }

    static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
            StreamCodec<? super B, T1> codec1, Function<C, T1> getter1,
            StreamCodec<? super B, T2> codec2, Function<C, T2> getter2,
            StreamCodec<? super B, T3> codec3, Function<C, T3> getter3,
            StreamCodec<? super B, T4> codec4, Function<C, T4> getter4,
            StreamCodec<? super B, T5> codec5, Function<C, T5> getter5,
            StreamCodec<? super B, T6> codec6, Function<C, T6> getter6,
            StreamCodec<? super B, T7> codec7, Function<C, T7> getter7,
            StreamCodec<? super B, T8> codec8, Function<C, T8> getter8,
            Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory) {
        return new StreamCodec<>() {
            @Override
            public C decode(B buffer) {
                T1 v1 = codec1.decode(buffer);
                T2 v2 = codec2.decode(buffer);
                T3 v3 = codec3.decode(buffer);
                T4 v4 = codec4.decode(buffer);
                T5 v5 = codec5.decode(buffer);
                T6 v6 = codec6.decode(buffer);
                T7 v7 = codec7.decode(buffer);
                T8 v8 = codec8.decode(buffer);
                return factory.apply(v1, v2, v3, v4, v5, v6, v7, v8);
            }

            @Override
            public void encode(B buffer, C value) {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
                codec7.encode(buffer, getter7.apply(value));
                codec8.encode(buffer, getter8.apply(value));
            }
        };
    }
}
