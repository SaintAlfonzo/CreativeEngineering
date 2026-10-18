package umpaz.brewinandchewin.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

// Port: see DrunkBubbleParticleOptions; one Deserializer per stage replaces the stream codecs.
public abstract class RagingParticleOptions implements ParticleOptions {
   private final float scale;

   public RagingParticleOptions(float size) {
      this.scale = size;
   }

   public float getScale() {
      return this.scale;
   }

   public static <T extends RagingParticleOptions> Codec<T> createCodec(Function<T, Float> sizeFunction, Function<Float, T> constructor) {
      return RecordCodecBuilder.create(inst -> inst.group(Codec.floatRange(0.0F, 1.0F).fieldOf("size").forGetter(sizeFunction)).apply(inst, constructor));
   }

   @SuppressWarnings("deprecation")
   public static <T extends RagingParticleOptions> ParticleOptions.Deserializer<T> createDeserializer(Function<Float, T> constructor) {
      return new ParticleOptions.Deserializer<>() {
         @Override
         public T fromCommand(ParticleType<T> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            return constructor.apply(reader.readFloat());
         }

         @Override
         public T fromNetwork(ParticleType<T> type, FriendlyByteBuf buf) {
            return constructor.apply(buf.readFloat());
         }
      };
   }

   @Override
   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeFloat(this.scale);
   }

   @Override
   public String writeToString() {
      return String.format(Locale.ROOT, "%s %.2f", ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()), this.scale);
   }

   public static class StageOne extends RagingParticleOptions {
      public static final Codec<StageOne> CODEC = createCodec(RagingParticleOptions::getScale, StageOne::new);
      public static final ParticleOptions.Deserializer<StageOne> DESERIALIZER = createDeserializer(StageOne::new);

      public StageOne(float size) {
         super(size);
      }

      @Override
      public ParticleType<?> getType() {
         return BnCParticleTypes.RAGING_STAGE_1;
      }
   }

   public static class StageTwo extends RagingParticleOptions {
      public static final Codec<StageTwo> CODEC = createCodec(RagingParticleOptions::getScale, StageTwo::new);
      public static final ParticleOptions.Deserializer<StageTwo> DESERIALIZER = createDeserializer(StageTwo::new);

      public StageTwo(float size) {
         super(size);
      }

      @Override
      public ParticleType<?> getType() {
         return BnCParticleTypes.RAGING_STAGE_2;
      }
   }

   public static class StageThree extends RagingParticleOptions {
      public static final Codec<StageThree> CODEC = createCodec(RagingParticleOptions::getScale, StageThree::new);
      public static final ParticleOptions.Deserializer<StageThree> DESERIALIZER = createDeserializer(StageThree::new);

      public StageThree(float size) {
         super(size);
      }

      @Override
      public ParticleType<?> getType() {
         return BnCParticleTypes.RAGING_STAGE_3;
      }
   }

   public static class StageFour extends RagingParticleOptions {
      public static final Codec<StageFour> CODEC = createCodec(RagingParticleOptions::getScale, StageFour::new);
      public static final ParticleOptions.Deserializer<StageFour> DESERIALIZER = createDeserializer(StageFour::new);

      public StageFour(float size) {
         super(size);
      }

      @Override
      public ParticleType<?> getType() {
         return BnCParticleTypes.RAGING_STAGE_4;
      }
   }
}
