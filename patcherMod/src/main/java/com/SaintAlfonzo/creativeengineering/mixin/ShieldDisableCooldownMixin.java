package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.shield.ShieldConfig;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Vanilla disables a player's raised shield for a flat 100 ticks (5s) once an attacker's weapon
 * lands a hit through it while {@code ItemStack#canDisableShield} answers true (vanilla axes by
 * default, via {@code AxeItem}'s Forge-only override) - {@code Player#disableShield(boolean)}
 * ({@code m_36384_}), bytecode-confirmed as a single {@code bipush 100} immediately before the
 * {@code ItemCooldowns#addCooldown} call. Immersive Engineering's own shield-disabling handler and
 * Alex's Mobs' void worm shot both call this exact method directly too (bypassing
 * {@code canDisableShield} entirely), so retuning this one constant covers every shield in the pack
 * uniformly - vanilla, Immersive Engineering's, and all seven tier shields alike. No mod in this
 * pack (including responsiveshields, which only retunes the unrelated shield-raise-time constant)
 * touches this method.
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
