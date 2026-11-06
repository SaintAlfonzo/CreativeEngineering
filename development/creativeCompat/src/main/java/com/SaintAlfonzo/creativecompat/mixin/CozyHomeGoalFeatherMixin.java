package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.cats.CatFeather;
import net.cozy.cozysimprovedcats.entity.custom.ImprovedCatEntity;
import net.cozy.cozysimprovedcats.goals.CatHomeGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Feather on a Stick's ground lure works on Cozy cats in Wander and Guard mode as it does on a vanilla cat that is
 * not sitting ({@link CatFeather#chasing}). Cozy's home goal (priority 2, every tick) otherwise stops the cat or walks
 * it back to its bed over DI's lure path; it gives way while the lure pulls this cat and resumes after.
 * {@code canContinueToUse} calls {@code canUse} (m_8036_; an override of Goal, so SRG-named in the jar), so one
 * injection covers both. Stay mode is excluded before this
 * (DiFeatherEntityMixin), as sitting is for vanilla cats.
 */
@Mixin(value = CatHomeGoal.class, remap = false)
public abstract class CozyHomeGoalFeatherMixin {

    @Shadow(remap = false) @Final private ImprovedCatEntity cat;

    @Inject(method = "m_8036_", at = @At("RETURN"), cancellable = true, remap = false)
    private void creativecompat$featherLure(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && CatFeather.chasing(this.cat)) {
            cir.setReturnValue(false);
        }
    }
}
