package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.surface.PassThrough;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Surrounded webs (plan_passthrough.md 4.3): RETURN of {@code LivingEntity#handleOnClimbable} ({@code m_21297_}), the
 * motion the land move of {@code travel} is about to use, the same place vanilla caps a ladder slide. In a surrounded
 * web {@code surface.PassThrough#surroundedMotion} sets the vertical speed: jump ({@code jumping}, {@code f_20899_};
 * players' input, mobs' jump control) climbs, sneak descends, otherwise the entity hangs. Common side: the local player
 * moves itself on the client, mobs on the server. SRG names, {@code remap = false}.
 */
@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingClimbMixin {

    @Shadow
    protected boolean f_20899_;

    @Inject(method = "m_21297_", at = @At("RETURN"), cancellable = true)
    private void creativecompat$surroundedClimb(Vec3 motion, CallbackInfoReturnable<Vec3> cir) {
        try {
            Vec3 out = PassThrough.surroundedMotion((LivingEntity) (Object) this, cir.getReturnValue(), f_20899_);
            if (out != null) cir.setReturnValue(out);
        } catch (Throwable ignored) {
            // keep vanilla's motion if the lookup breaks
        }
    }
}
