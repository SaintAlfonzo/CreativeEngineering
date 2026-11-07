// Port glue (ours, GPLv3): the part of Resourceful's ByteCodec the port needs, on FriendlyByteBuf.
package earth.terrarium.tempad.common.utils;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BufCodec<T> {
    void encode(T value, FriendlyByteBuf buf);

    T decode(FriendlyByteBuf buf);

    static <T> BufCodec<T> of(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        return new BufCodec<>() {
            @Override
            public void encode(T value, FriendlyByteBuf buf) {
                writer.accept(buf, value);
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
                return reader.apply(buf);
            }
        };
    }

    static <T> BufCodec<T> unit(T value) {
        return of((buf, v) -> { }, buf -> value);
    }

    BufCodec<Integer> INT = of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);
}
