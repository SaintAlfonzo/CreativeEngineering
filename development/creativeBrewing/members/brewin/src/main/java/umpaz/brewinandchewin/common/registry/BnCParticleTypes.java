package umpaz.brewinandchewin.common.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.RegisterEvent.RegisterHelper;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;
import umpaz.brewinandchewin.client.particle.RagingParticleOptions;

public class BnCParticleTypes {
   public static final SimpleParticleType FOG = new SimpleParticleType(true);
   public static final ParticleType<RagingParticleOptions.StageOne> RAGING_STAGE_1 = create(RagingParticleOptions.StageOne.DESERIALIZER, RagingParticleOptions.StageOne.CODEC);
   public static final ParticleType<RagingParticleOptions.StageTwo> RAGING_STAGE_2 = create(RagingParticleOptions.StageTwo.DESERIALIZER, RagingParticleOptions.StageTwo.CODEC);
   public static final ParticleType<RagingParticleOptions.StageThree> RAGING_STAGE_3 = create(RagingParticleOptions.StageThree.DESERIALIZER, RagingParticleOptions.StageThree.CODEC);
   public static final ParticleType<RagingParticleOptions.StageFour> RAGING_STAGE_4 = create(RagingParticleOptions.StageFour.DESERIALIZER, RagingParticleOptions.StageFour.CODEC);
   public static final ParticleType<DrunkBubbleParticleOptions> DRUNK_BUBBLE = create(DrunkBubbleParticleOptions.DESERIALIZER, DrunkBubbleParticleOptions.CODEC);

   @SuppressWarnings("deprecation")
   private static <T extends ParticleOptions> ParticleType<T> create(ParticleOptions.Deserializer<T> deserializer, Codec<T> codec) {
      return new ParticleType<T>(false, deserializer) {
         @Override
         public Codec<T> codec() {
            return codec;
         }
      };
   }

   public static void registerAll(RegisterHelper<ParticleType<?>> helper) {
      helper.register(BrewinAndChewin.asResource("drunk_bubble"), DRUNK_BUBBLE);
      helper.register(BrewinAndChewin.asResource("fog"), FOG);
      helper.register(BrewinAndChewin.asResource("raging_stage_1"), RAGING_STAGE_1);
      helper.register(BrewinAndChewin.asResource("raging_stage_2"), RAGING_STAGE_2);
      helper.register(BrewinAndChewin.asResource("raging_stage_3"), RAGING_STAGE_3);
      helper.register(BrewinAndChewin.asResource("raging_stage_4"), RAGING_STAGE_4);
   }
}
