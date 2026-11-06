package com.SaintAlfonzo.creativeequipment.shield;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * What a tier shield does once a hit HAS been blocked (vanilla's {@code LivingEntity#hurt} posts
 * {@link ShieldBlockEvent} right after {@code isDamageSourceBlocked}; server side only):
 * scales the absorbed damage per category, takes the configured share of durability, and applies
 * the on-block potion effects. Registered on the Forge bus in {@code CreativeEngineering}.
 *
 * <p>Vanilla's wear, {@code Player#hurtCurrentlyUsedShield}, always uses the full incoming amount
 * and can't be scaled, so for player holders this turns it off ({@code setShieldTakesDamage(false)})
 * and does the same bookkeeping itself with the configured percentage. Non-player holders
 * (skeletons, MineColonies guards) are left on vanilla's path so their own overrides still run.
 */
public final class ShieldBlockHandler {

    private ShieldBlockHandler() {}

    /**
     * The holder a knockback suppression applies to for the hit currently in flight, or
     * {@code null} for none - see {@link #onKnockback}. Vanilla's OWN attacker-recoil
     * ({@code LivingEntity#blockUsingShield}, fired from inside {@code hurt()} right after
     * {@link #onShieldBlock}) is deliberately left alone; what {@code knockbackThreshold} actually
     * controls is the push the HOLDER themselves takes, which vanilla applies separately and LATER
     * - from the attacker's own {@code Player#attack}/mob-attack code, once {@code hurt()} has
     * returned, via {@code LivingEntity#knockback} -> {@code LivingKnockBackEvent}. Both that call
     * and the shield's own attacker-recoil call end up going through the SAME
     * {@code LivingEntity#knockback} method and therefore the same event, so this is keyed to the
     * exact holder instance (never the attacker) and self-clears the moment it matches, so it can
     * only ever cancel the one knockback it was set up for.
     */
    private static LivingEntity suppressKnockbackHolder;

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        // Always written, even on every early return below, so a stale holder from a PREVIOUS
        // tier-shield hit can never leak into an unrelated later knockback.
        suppressKnockbackHolder = null;

        LivingEntity holder = event.getEntity();
        ItemStack shield = holder.getUseItem();
        ShieldTier tier = ShieldTier.of(shield);
        if (tier == null || !ShieldConfig.enabled()) {
            return;
        }

        DamageSource source = event.getDamageSource();
        ShieldCategory category = ShieldCategory.classify(source);

        int reductionPct = ShieldConfig.reduction(tier, category, source);
        if (reductionPct <= 0) {
            // Config changed between the block check and now: treat as not blocked.
            event.setCanceled(true);
            return;
        }

        float incoming = event.getOriginalBlockedDamage();
        float blocked = incoming * reductionPct / 100.0F;
        float remaining = incoming - blocked;
        if (remaining < 1.0F) {
            // Too small to register as a hit at all once reduced - round down to a full block
            // rather than letting a sliver of damage through.
            blocked = incoming;
            remaining = 0.0F;
        }
        event.setBlockedDamage(blocked);
        if (remaining < ShieldConfig.knockbackThreshold(tier)) {
            suppressKnockbackHolder = holder;
        }

        if (holder instanceof Player player && !player.level.isClientSide) {
            event.setShieldTakesDamage(false);
            wear(player, shield, tier, category, incoming);
        }

        ShieldEffects.apply(holder, ShieldConfig.shielderEffects(tier), category);
        LivingEntity attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        if (attacker != null && attacker != holder) {
            ShieldEffects.apply(attacker, ShieldConfig.attackerEffects(tier), category);
        }
    }

    /**
     * Cancels the ONE knockback {@link #onShieldBlock} flagged for {@code holder}, whenever it
     * actually arrives (immediately after, in the same call stack, for a living melee attacker;
     * possibly not at all, e.g. a positionless source that still counted as a block). Self-clears
     * on the first matching event either way, so it can't affect a second, later knockback to the
     * same holder from something unrelated.
     */
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() == suppressKnockbackHolder) {
            suppressKnockbackHolder = null;
            event.setCanceled(true);
        }
    }

    /** {@code Player#hurtCurrentlyUsedShield} with the wear scaled by the tier's per-category percentage. */
    private static void wear(Player player, ItemStack shield, ShieldTier tier, ShieldCategory category, float damage) {
        if (!shield.canPerformAction(ToolActions.SHIELD_BLOCK)) {
            return;
        }
        player.awardStat(Stats.ITEM_USED.get(shield.getItem()));
        if (damage < ShieldConfig.wearThreshold()) {
            return;
        }
        int pct = ShieldConfig.durabilityCost(tier, category);
        if (pct <= 0) {
            return;
        }

        // Vanilla wear is 1 + floor(damage). Scale it, rolling the fractional part so the
        // long-run average is exactly pct% (a plain round would make e.g. 40% of a 1-point hit free).
        float exact = (1 + Mth.floor(damage)) * pct / 100.0F;
        int cost = (int) exact + (player.getRandom().nextFloat() < exact - (int) exact ? 1 : 0);
        if (cost <= 0) {
            return;
        }

        InteractionHand hand = player.getUsedItemHand();
        shield.hurtAndBreak(cost, player, p -> p.broadcastBreakEvent(hand));
        if (shield.isEmpty()) {
            player.setItemSlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND,
                    ItemStack.EMPTY);
            player.stopUsingItem();
            player.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + player.level.random.nextFloat() * 0.4F);
        }
    }
}
