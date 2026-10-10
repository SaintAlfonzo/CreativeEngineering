package com.SaintAlfonzo.more_mooblooms.mixin;

import com.SaintAlfonzo.more_mooblooms.compat.BuzzierBeesSet;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sunny off (R4, feature_writeup/mooblooms.md 4.11), buzzier_bees set only. Buzzier Bees' own MobEntityMixin
 * injects at the TAIL of {@code Mob.isSunBurnTick} (m_21527_), cancellable, default priority 1000, and returns
 * true for a mob with Sunny. This one has priority 900, so Mixin applies it first and its call sits in front of
 * BB's before the same final {@code return false}: for a mob that has Sunny while {@code disableSunny} is on, it
 * returns false (what vanilla returns there anyway) and BB's callback never runs.
 *
 * It only cancels for a mob that has Sunny, so other mods' callbacks at this point (Breezy has one at RETURN)
 * run as before for every other mob. With the effect denied and stripped (BuzzierBeesSet) no mob should have it;
 * this is the second layer. A vanilla-class mixin: nothing of Buzzier Bees is touched (4.0).
 */
@Mixin(value = Mob.class, priority = 900, remap = false)
public abstract class MobSunBurnMixin {

    @Inject(method = "m_21527_", at = @At("TAIL"), cancellable = true, remap = false)
    private void more_mooblooms$noSunnyBurn(CallbackInfoReturnable<Boolean> cir) {
        if (BuzzierBeesSet.blocksSunnyBurn((Mob) (Object) this)) cir.setReturnValue(false);
    }
}
