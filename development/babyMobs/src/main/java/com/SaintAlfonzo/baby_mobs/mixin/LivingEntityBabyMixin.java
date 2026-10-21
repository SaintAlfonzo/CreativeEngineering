package com.SaintAlfonzo.baby_mobs.mixin;

import com.SaintAlfonzo.baby_mobs.age.BabyAges;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Baby behaviour for the mobs {@link BabyAges} handles, at the LivingEntity level because Squid and Dolphin don't
 * override these: m_6162_ isBaby, m_6134_ getScale, m_8107_ aiStep (age tick), m_6779_ canAttack (baby dolphins are
 * passive), m_7350_ onSyncedDataUpdated (hitbox refresh on the client when the flag flips). Vanilla's own checks of
 * isBaby() then do the rest: voice pitch 1.5, no loot, no experience, dimensions x getScale.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityBabyMixin {

    @Inject(method = "m_6162_", at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$isBaby(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Mob mob && BabyAges.handles(mob)) cir.setReturnValue(BabyAges.isBaby(mob));
    }

    @Inject(method = "m_6134_", at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$scale(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof Mob mob && BabyAges.handles(mob)) cir.setReturnValue(BabyAges.scale(mob));
    }

    @Inject(method = "m_8107_", at = @At("TAIL"), remap = false)
    private void babyMobs$ageTick(CallbackInfo ci) {
        if ((Object) this instanceof Mob mob && BabyAges.handles(mob)) BabyAges.tick(mob);
    }

    @Inject(method = "m_6779_", at = @At("HEAD"), cancellable = true, remap = false)
    private void babyMobs$noBabyAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (BabyAges.blocksAttack((LivingEntity) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "m_7350_", at = @At("TAIL"), remap = false)
    private void babyMobs$synced(EntityDataAccessor<?> key, CallbackInfo ci) {
        if ((Object) this instanceof Mob mob && BabyAges.handles(mob) && BabyAges.isBabyKey(mob, key)) mob.refreshDimensions();
    }
}
