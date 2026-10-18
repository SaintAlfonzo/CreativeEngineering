package umpaz.brewinandchewin.common.effect;

import com.mojang.math.Vector3f;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import umpaz.brewinandchewin.client.particle.DrunkBubbleParticleOptions;

// Port: the drunk bubble particle is spawned by LivingEntityMixin in place of the swirl (1.19.2 effects have no
// particle field).
public class TipsyEffect extends MobEffect {
   public static final int COLOR = 13208334;

   public TipsyEffect() {
      super(MobEffectCategory.NEUTRAL, COLOR);
   }

   public static ParticleOptions getParticle(int color) {
      return new DrunkBubbleParticleOptions(
         new Vector3f((float)(color >> 16 & 0xFF) / 255.0F, (float)(color >> 8 & 0xFF) / 255.0F, (float)(color & 0xFF) / 255.0F), 0.25F
      );
   }

   @Override
   public boolean isDurationEffectTick(int duration, int amplifier) {
      return true;
   }
}
