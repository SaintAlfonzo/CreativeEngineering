package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.trader.WanderingTraderTweaks;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wandering Trader offer counts ({@code [wandering_trader] commonTrades / rareTrades}).
 * {@code updateTrades} ({@code m_7604_}) passes a single {@code iconst_5} to
 * {@code addOffersFromItemListings} for the common pool (bytecode-confirmed, the only 5 in the
 * method), then adds one random rare offer; TAIL runs after that rare pick and is skipped by the
 * early return when either pool is missing.
 *
 * <p>Vinery's Wandering Winemaker overrides {@code updateTrades} with its own list, so it is not
 * affected. Vanilla-class target, matched by SRG name with {@code remap = false}.
 */
@Mixin(WanderingTrader.class)
public abstract class WanderingTraderTradesMixin {

    @ModifyConstant(method = "m_7604_", constant = @Constant(intValue = 5), remap = false)
    private int creativeengineering$commonTrades(int vanilla) {
        try {
            return WanderingTraderTweaks.commonTrades(vanilla);
        } catch (Throwable ignored) {
            return vanilla;
        }
    }

    @Inject(method = "m_7604_", at = @At("TAIL"), remap = false)
    private void creativeengineering$extraRareTrades(CallbackInfo ci) {
        WanderingTrader self = (WanderingTrader) (Object) this;
        WanderingTraderTweaks.addExtraRareOffers(self, self.getRandom(), self.getOffers(),
                VillagerTrades.WANDERING_TRADER_TRADES.get(2));
    }
}
