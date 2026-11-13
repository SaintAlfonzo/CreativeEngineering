package com.SaintAlfonzo.solraspberrypie.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Alex's Mobs Fleet-Footed fix (12.2). The effect keeps its "remove the bonus after" countdown in a field of the one
 * shared effect object, so every entity with the effect decrements it and one sprint-jumping player keeps everyone's
 * +20% on. The countdown moves to a per-entity map; the rest is the same rule: bonus while sprinting, airborne and the
 * effect not about to end, removed 5 ticks after that stops. {@code lastDuration} is written by isDurationEffectTick
 * just before this call for the same entity, so it stays as it is. Applied only with Alex's Mobs (RpMixinPlugin).
 */
@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.effect.EffectFleetFooted", remap = false)
public abstract class FleetFootedMixin {
    @Unique
    private static final AttributeModifier SOLRP$BONUS = new AttributeModifier(UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF29A"),
            "fleetfooted speed bonus", 0.2F, AttributeModifier.Operation.ADDITION);
    @Unique
    private static final Map<LivingEntity, Integer> SOLRP$REMOVE_AFTER = new WeakHashMap<>();

    @Shadow
    private int lastDuration;

    @Inject(method = "m_6742_(Lnet/minecraft/world/entity/LivingEntity;I)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void solrp$perEntity(LivingEntity entity, int amplifier, CallbackInfo ci) {
        ci.cancel();
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean active = entity.isSprinting() && !entity.isOnGround() && lastDuration > 2;
        int removeAfter;
        synchronized (SOLRP$REMOVE_AFTER) {
            removeAfter = SOLRP$REMOVE_AFTER.getOrDefault(entity, 0);
            if (removeAfter > 0) removeAfter--;
            if (active) {
                if (!speed.hasModifier(SOLRP$BONUS)) speed.addTransientModifier(SOLRP$BONUS);
                removeAfter = 5;
            }
            SOLRP$REMOVE_AFTER.put(entity, removeAfter);
        }
        if (removeAfter <= 0 || lastDuration < 2) speed.removeModifier(SOLRP$BONUS);
    }
}
