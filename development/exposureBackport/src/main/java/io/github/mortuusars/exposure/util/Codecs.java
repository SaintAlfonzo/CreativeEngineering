package io.github.mortuusars.exposure.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import io.github.mortuusars.exposure.backport.codec.BackportCodecs;

public class Codecs {
   public static final Codec<Double> POSITIVE_DOUBLE = BackportCodecs.validate(Codec.DOUBLE
      , f -> f > 0.0 ? DataResult.success(f) : DataResult.error("Value must be positive: " + f));

   public static Codec<int[]> intArrayCodec(int minSize, int maxSize) {
      return new Codec<int[]>() {
         public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
            if (input.length >= minSize && input.length <= maxSize) {
               ListBuilder<T> builder = ops.listBuilder();

               for (int inp : input) {
                  builder.add(ops.createInt(inp));
               }

               return builder.build(prefix);
            } else {
               return DataResult.error("Array size must be between " + minSize + " and " + maxSize + ", got: " + input.length);
            }
         }

         public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getIntStream(input)
               .flatMap(
                  stream -> {
                     int[] array = stream.toArray();
                     return array.length >= minSize && array.length <= maxSize
                        ? DataResult.success(Pair.of(array, input))
                        : DataResult.error("Array size must be between " + minSize + " and " + maxSize + ", got: " + array.length);
                  }
               );
         }
      };
   }

   public static Codec<byte[]> byteArrayCodec(int minSize, int maxSize) {
      return new Codec<byte[]>() {
         public <T> DataResult<T> encode(byte[] input, DynamicOps<T> ops, T prefix) {
            if (input.length >= minSize && input.length <= maxSize) {
               ListBuilder<T> builder = ops.listBuilder();

               for (byte inp : input) {
                  builder.add(ops.createByte(inp));
               }

               return builder.build(prefix);
            } else {
               return DataResult.error("Array size must be between " + minSize + " and " + maxSize + ", got: " + input.length);
            }
         }

         public <T> DataResult<Pair<byte[], T>> decode(DynamicOps<T> ops, T input) {
            return ops.getByteBuffer(input)
               .flatMap(
                  stream -> {
                     byte[] array = stream.array();
                     return array.length >= minSize && array.length <= maxSize
                        ? DataResult.success(Pair.of(array, input))
                        : DataResult.error("Array size must be between " + minSize + " and " + maxSize + ", got: " + array.length);
                  }
               );
         }
      };
   }

   public static Codec<Float> floatRange(float minInclusive, float maxInclusive) {
      return BackportCodecs.validate(Codec.FLOAT
         , 
            f -> f.compareTo(minInclusive) >= 0 && f.compareTo(maxInclusive) <= 0
                  ? DataResult.success(f)
                  : DataResult.error("Value must be between %s and %s: %s".formatted(minInclusive, maxInclusive, f))
         );
   }
}
