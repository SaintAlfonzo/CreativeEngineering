package com.SaintAlfonzo.creativecommon.trader;

// Reserved for Creative Economy: wandering trader trades and spawning move to the future economy mod (trading and loot
// chests; owner, 2026-11-07, creative_split.md 2.3). Stays in Common until that mod is planned.

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.SaintAlfonzo.creativecommon.CommonConfig;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Wandering Trader spawn rate and trade counts, driven by {@link CommonConfig.WanderingTrader}.
 *
 * <ul>
 *   <li>{@code mixin.WanderingTraderSpawnerMixin} swaps the spawner's 24000-tick wait for
 *       {@link #spawnDelay(int)} ({@code traderIntervalTicks}). Only the wait changes; the
 *       25/50/75% chance sequence stays vanilla.</li>
 *   <li>{@code mixin.WanderingTraderTradesMixin} swaps the 5 common offers for
 *       {@link #commonTrades(int)} and adds {@link #addExtraRareOffers} after vanilla's one rare.</li>
 * </ul>
 *
 * The pools themselves are Forge's {@code VillagerTrades.WANDERING_TRADER_TRADES} (1 = common,
 * 2 = rare), rebuilt from {@code WandererTradesEvent} at server start and then edited by KubeJS
 * ({@code economy/wandering_trades.js}, through {@link TradeListings}); /dumptrades lists them.
 */
public final class WanderingTraderTweaks {

    private WanderingTraderTweaks() {}

    public static int spawnDelay(int vanilla) {
        CommonConfig.WanderingTrader cfg = CommonConfig.WANDERING_TRADER;
        return cfg.enabled.get() ? cfg.traderIntervalTicks.get() : vanilla;
    }

    public static int commonTrades(int vanilla) {
        CommonConfig.WanderingTrader cfg = CommonConfig.WANDERING_TRADER;
        return cfg.enabled.get() ? cfg.commonTrades.get() : vanilla;
    }

    /**
     * Rare offers beyond vanilla's one. Listings are tried in random order without repeats, and an
     * offer whose result matches one already on the trader (vanilla's rare pick, or a pool that
     * registers the same listing twice, as Supplementaries does) is skipped.
     */
    public static void addExtraRareOffers(Entity trader, RandomSource random, MerchantOffers offers,
                                          VillagerTrades.ItemListing[] rare) {
        CommonConfig.WanderingTrader cfg = CommonConfig.WANDERING_TRADER;
        if (!cfg.enabled.get() || rare == null) return;
        int wanted = cfg.rareTrades.get() - 1;
        if (wanted <= 0) return;

        List<Integer> order = new ArrayList<>(rare.length);
        for (int i = 0; i < rare.length; i++) order.add(i);
        Collections.shuffle(order, new Random(random.nextLong()));

        int added = 0;
        for (int idx : order) {
            if (added >= wanted) break;
            MerchantOffer offer = rare[idx].getOffer(trader, random);
            if (offer == null || alreadySold(offers, offer.getResult())) continue;
            offers.add(offer);
            added++;
        }
    }

    private static boolean alreadySold(MerchantOffers offers, ItemStack result) {
        for (MerchantOffer o : offers) {
            if (ItemStack.isSameItemSameTags(o.getResult(), result)) return true;
        }
        return false;
    }
}
