package net.satisfy.vinery.core.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Experience effect: orbs give {@code amount x (1 + (1 + level) x 0.5)}. Port: the call's argument, not the local. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @ModifyArg(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private int vinery$modifyExperienceAmount(int originalAmount) {
        Player player = vinery$touching;
        vinery$touching = null;
        if (player == null) {
            return originalAmount;
        }
        MobEffectInstance effect = player.getEffect(MobEffectRegistry.get(MobEffectRegistry.EXPERIENCE_EFFECT));
        if (effect == null) {
            return originalAmount;
        }
        return (int) (originalAmount + originalAmount * (1 + effect.getAmplifier()) * 0.5);
    }

    private Player vinery$touching;

    @org.spongepowered.asm.mixin.injection.Inject(method = "playerTouch", at = @At("HEAD"))
    private void vinery$rememberPlayer(Player player, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        vinery$touching = player;
    }
}
