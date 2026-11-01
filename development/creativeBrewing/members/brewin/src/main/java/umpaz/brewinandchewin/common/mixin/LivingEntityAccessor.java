package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// Port: 1.21 updateEffectVisibility -> 1.19.2 updateInvisibilityStatus (recomputes the effect colour and visibility).
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
   @Invoker("updateInvisibilityStatus")
   void brewinandchewin$invokeUpdateEffectVisibility();
}
