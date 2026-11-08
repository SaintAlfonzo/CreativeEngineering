// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/sizing/TimedoorPlacementSettings.kt (SizingType). Translated to Java
// for Forge 1.19.2: ByteCodec -> BufCodec, MapCodec -> NBT reader/writer.
package earth.terrarium.tempad.api.sizing;

import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SizingType<T extends TimedoorPlacementSettings>(ResourceLocation id, BufCodec<T> byteCodec,
                                                               Function<CompoundTag, T> loader, BiConsumer<T, CompoundTag> saver) {
}
