package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.shield.ShieldConfig;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla disables a player's raised shield for a flat 100 ticks (5s) when a PLAYER's weapon lands a
 * hit through it while {@code ItemStack#canDisableShield} answers true (vanilla axes, via
 * {@code AxeItem}'s Forge override) - {@code Player#disableShield(boolean)} ({@code m_36384_}),
 * bytecode-confirmed as a single {@code bipush 100} immediately before the
 * {@code ItemCooldowns#addCooldown} call. Forge puts that cooldown on the shield actually raised, so
 * this covers every shield. Immersive Engineering's shock upgrade and Alex's Mobs' void worm shot call
 * this method directly too. A MOB's axe goes through a separate method with its own 100,
 * {@code Mob#maybeDisableShield} - see {@link MobShieldDisableMixin}. Both read
 * {@code [shields].disableCooldownTicks} (20 in the pack). No other mod in the pack touches either
 * method (responsiveshields only retunes the unrelated raise-time constant).
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} - same idiom as
 * {@link LivingEntityShieldMixin}.
 */
@Mixin(Player.class)
public abstract class ShieldDisableCooldownMixin {

    @ModifyConstant(method = "m_36384_", constant = @Constant(intValue = 100), remap = false)
    private int creativeengineering$disableCooldownTicks(int vanilla) {
        try {
            return ShieldConfig.disableCooldownTicks();
        } catch (Throwable ignored) {
            return vanilla;
        }
    }
}
