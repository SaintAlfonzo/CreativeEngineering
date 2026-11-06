package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.shield.ShieldConfig;
import com.SaintAlfonzo.creativeequipment.shield.ShieldTier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the player's real vanilla iron shield's anvil repair material configurable
 * ({@code [shields.iron].repairItem}, an iron ingot by default) instead of vanilla's own
 * plank-tag check. {@code shield.TierShieldItem#isValidRepairItem} does the equivalent for the
 * seven dedicated tier items via a real Java override that fully replaces vanilla's; this mirrors
 * it for {@code Items.SHIELD}, which isn't ours to override directly.
 *
 * <p>Targets {@code ShieldItem} (not the base {@code Item}) because that is where vanilla's own
 * override of this method actually lives. {@code TierShieldItem}'s own Java override of the same
 * method means this injected code never runs for the seven dedicated items - calling
 * {@code isValidRepairItem} on one of them dispatches straight to their own override body, never
 * touching this inherited one - so no identity check is strictly required for correctness, but
 * one is kept anyway in case some other mod ever registers a plain, un-subclassed
 * {@code ShieldItem} of its own.
 *
 * <p><b>Deliberate behaviour change:</b> the real iron shield no longer repairs with planks once
 * this is active - only with whatever {@code [shields.iron].repairItem} names (an iron ingot by
 * default), matching every other tier shield's repair rule instead of vanilla's own.
 */
@Mixin(ShieldItem.class)
public abstract class VanillaShieldRepairMixin {

    @Inject(method = "m_6832_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$ironShieldRepair(ItemStack toRepair, ItemStack repair,
            CallbackInfoReturnable<Boolean> cir) {
        try {
            if ((Object) this != Items.SHIELD) {
                return;
            }
            String id = ShieldConfig.repairItem(ShieldTier.IRON);
            if (repair.isEmpty() || id == null || id.isBlank()) {
                cir.setReturnValue(false);
                return;
            }
            ResourceLocation rl = ResourceLocation.tryParse(id.trim());
            cir.setReturnValue(rl != null && ForgeRegistries.ITEMS.getValue(rl) == repair.getItem());
        } catch (Throwable ignored) {
            // fall through to vanilla's own plank check
        }
    }
}
