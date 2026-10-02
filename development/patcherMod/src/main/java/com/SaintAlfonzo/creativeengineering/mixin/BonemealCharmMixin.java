package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.util.BonemealCharms;

import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * "Infinite bonemeal" - the Winemaker-armor-set trick from Lets Do: Vinery
 * ({@code satisfyu.vinery.mixin.BoneMealItemMixin}), generalized from a 4-piece armor
 * requirement onto a single held item. That mixin injects at the {@code RETURN} of
 * {@code BoneMealItem#useOn} - by then the bone meal has already been consumed and the
 * method has decided to return {@code InteractionResult.CONSUME} - and, gated by a
 * probability roll, calls {@code ItemStack#grow(1)} on the just-used stack to refund the
 * one that was eaten. A second independent roll chips durability off the gear responsible
 * (their full armor set; here, the charm item) as the trade-off.
 *
 * <p>Bone meal always occupies whichever hand actually triggered {@code useOn}
 * ({@link UseOnContext#getHand()}), so the charm has to be checked in the OTHER hand -
 * membership in {@link BonemealCharms#TAG} (populated in KubeJS, reload-able) is what
 * qualifies an item, not a fixed item id.
 *
 * <p>Vanilla-class target, matched by SRG name with {@code remap = false} - same idiom as
 * {@link LivingEntityShieldMixin} / {@link VanillaShieldDurabilityMixin} (no refmap; the pack
 * only runs reobfuscated). Wrapped in a catch-all so a bug here can never break bone meal for
 * everyone: it degrades to vanilla (no refund, no extra damage).
 */
@Mixin(BoneMealItem.class)
public abstract class BonemealCharmMixin {

    @Inject(method = "m_6225_", at = @At("RETURN"), remap = false)
    private void creativeengineering$refundBonemeal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            if (!CEConfig.BONEMEAL_CHARM.enabled.get()) return;
            if (cir.getReturnValue() != InteractionResult.CONSUME) return;

            Player player = context.getPlayer();
            if (player == null) return;

            InteractionHand otherHand = context.getHand() == InteractionHand.MAIN_HAND
                    ? InteractionHand.OFF_HAND
                    : InteractionHand.MAIN_HAND;

            ItemStack charm = player.getItemInHand(otherHand);
            if (charm.isEmpty() || !charm.is(BonemealCharms.TAG)) return;

            RandomSource random = context.getLevel().getRandom();

            if (random.nextFloat() < CEConfig.BONEMEAL_CHARM.chance.get()) {
                context.getItemInHand().grow(1);
            }
            if (random.nextFloat() < CEConfig.BONEMEAL_CHARM.damageChance.get()) {
                charm.hurtAndBreak(CEConfig.BONEMEAL_CHARM.damagePerUse.get(), player,
                        p -> p.broadcastBreakEvent(otherHand));
            }
        } catch (Throwable ignored) {
            // fall through to vanilla's own result - no refund, no extra damage
        }
    }
}
