package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.shield.ShieldConfig;
import com.SaintAlfonzo.creativeequipment.shield.ShieldTier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the player's real vanilla iron shield ({@code Items.SHIELD}, folded into the tier system
 * as {@code ShieldTier.IRON}) read its max durability from {@code [shields.iron].durability}
 * instead of vanilla's fixed 336. {@code shield.TierShieldItem#getMaxDamage(ItemStack)} does the
 * equivalent for the seven dedicated tier items through a real Java override of Forge's
 * stack-aware {@code IForgeItem#getMaxDamage(ItemStack)}; vanilla's {@code ShieldItem} isn't ours
 * to override, and doesn't override that Forge method itself either, so its default
 * implementation falls through to the plain vanilla no-arg {@code Item#getMaxDamage()} below -
 * which is what {@code ItemStack#getMaxDamage()}, and everything that enforces or displays
 * durability, ultimately reads for it. Hooking that no-arg method directly is what reaches it.
 *
 * <p>Targets the base {@code Item} class, not {@code ShieldItem}, because that is genuinely where
 * the vanilla method lives - the identity check below is what keeps this from touching every
 * other item in the game. No {@code ShieldConfig.enabled()} gate, matching
 * {@code TierShieldItem#getMaxDamage}: durability always follows config, independent of whether
 * the tier-shield blocking feature itself is switched on.
 */
@Mixin(Item.class)
public abstract class VanillaShieldDurabilityMixin {

    @Inject(method = "m_41462_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$ironShieldDurability(CallbackInfoReturnable<Integer> cir) {
        try {
            if ((Item) (Object) this == Items.SHIELD) {
                cir.setReturnValue(ShieldConfig.durability(ShieldTier.IRON));
            }
        } catch (Throwable ignored) {
            // fall through to vanilla's own maxDamage field
        }
    }
}
