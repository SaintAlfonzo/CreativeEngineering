package com.SaintAlfonzo.solraspberrypie.mixin;

import com.tarinoita.solsweetpotato.tracking.benefits.BenefitsHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Raspberry Pie owns the whole ladder (2, 12.4 #33): Apple Pie never applies its own benefits, whatever a world's
 * serverconfig still lists. A world created before the pack emptied Apple Pie's benefit list kept them, and they added
 * hearts on top of the ladder. Removal (logout, /foodlist clear) still runs, and stale modifiers are stripped at login.
 */
@Mixin(value = BenefitsHandler.class, remap = false)
public abstract class BenefitsHandlerMixin {
    @Inject(method = "updateBenefits", at = @At("HEAD"), cancellable = true)
    private static void solrp$noBenefits(Player player, double diversity, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "tickBenefits", at = @At("HEAD"), cancellable = true)
    private static void solrp$noEffectTicks(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
