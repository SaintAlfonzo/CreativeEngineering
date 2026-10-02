package io.github.mortuusars.exposure.advancements.predicate;

import com.mojang.serialization.Codec;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;
import io.github.mortuusars.exposure.backport.codec.ByteBufCodecs;
import io.github.mortuusars.exposure.backport.codec.StreamCodec;
import io.github.mortuusars.exposure.util.ExtraData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public record ExtraDataPredicate(ExtraData data) {
   public static final Codec<ExtraDataPredicate> CODEC = BackportCodecs.LENIENT_COMPOUND_TAG.xmap((tag) -> new ExtraDataPredicate(new ExtraData(tag)), (predicate) -> predicate.data);
   public static final StreamCodec<ByteBuf, ExtraDataPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map((tag) -> new ExtraDataPredicate(new ExtraData(tag)), (predicate) -> predicate.data);

   public boolean matches(@Nullable Tag tag) {
      return tag != null && this.compareNbt(tag);
   }

   private boolean compareNbt(@Nullable Tag other) {
      if (this.data == other || this.data == null) {
         return true;
      } else if (other == null) {
         return false;
      } else if (other instanceof CompoundTag otherCompound) {
         if (otherCompound.size() < this.data.size()) {
            return false;
         }
         for (String key : this.data.getAllKeys()) {
            if (!NbtUtils.compareNbt(this.data.get(key), otherCompound.get(key), true)) {
               return false;
            }
         }
         return true;
      } else {
         return this.data.equals(other);
      }
   }
}
