// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativeloot;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * {@code config/creativeloot-common.toml}. Holds {@code [wandering_trader]}, which moved here from Creative Common in the
 * wave 2 carve (plan_wave2_publish.md W2-2): {@link CreativeLoot} carries the player's values over once from
 * {@link #COMMON_FILE} before registering this spec (Forge reads COMMON configs after every constructor), and Creative
 * Common drops the section from its own file when Forge corrects it.
 */
public final class LootConfig {

	public static final String FILE = "creativeloot-common.toml";
	public static final String COMMON_FILE = "creativecommon-common.toml";
	public static final List<String> COMMON_SECTIONS = List.of("wandering_trader");

	public static final ForgeConfigSpec SPEC;
	public static final WanderingTrader WANDERING_TRADER;

	static {
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
		WANDERING_TRADER = new WanderingTrader(b);
		SPEC = b.build();
	}

	private LootConfig() {}

	/**
	 * Dials for {@code trader.WanderingTraderTweaks} (Wandering Trader wait and offer counts, through
	 * {@code mixin.WanderingTraderSpawnerMixin} / {@code mixin.WanderingTraderTradesMixin}). The Wandering
	 * Winemaker's spawner is the Vinery port's own (vinery-common.toml [trader]). What the pools hold is
	 * set in KubeJS ({@code kubejs/server_scripts/economy/wandering_trades.js}, through Creative Common's trade helper).
	 */
	public static final class WanderingTrader {
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.IntValue traderIntervalTicks;
		public final ForgeConfigSpec.IntValue commonTrades;
		public final ForgeConfigSpec.IntValue rareTrades;

		private WanderingTrader(ForgeConfigSpec.Builder b) {
			b.comment(
					"Wandering Trader spawning and offer counts (the Wandering Winemaker: vinery-common.toml [trader]).",
					"The spawner rolls once per interval, counted in server ticks (20 = 1 second) while the",
					"overworld runs with doMobSpawning on. Day length (Better Days), sleeping and /time do not",
					"change it. A roll is vanilla's: 25/50/75% chance (rising until a spawn), then 1 in 10, then",
					"a free spot near a random player; on average about 14 rolls per spawn.",
					"/gamerule doTraderSpawning false stops it and the Winemaker."
			).push("wandering_trader");

			enabled = b.comment("Off = vanilla Wandering Trader (24000-tick interval, 5 + 1 offers). Default off; the pack sets true.")
					.define("enabled", false);

			traderIntervalTicks = b.comment(
					"Ticks between Wandering Trader spawn rolls. Vanilla: 24000 (20 minutes). 96000 = 80 minutes",
					"(the pack's value). A change takes effect after the wait already counting down."
			).defineInRange("traderIntervalTicks", 24000, 1200, 100_000_000);

			commonTrades = b.comment(
					"Offers drawn from the common pool. Vanilla: 5 (the pack: 25). Each offer is a different listing; a pool",
					"smaller than this gives every listing once."
			).defineInRange("commonTrades", 5, 0, 200);

			rareTrades = b.comment("Offers drawn from the rare pool, all different items. Vanilla: 1 (the pack: 5).")
					.defineInRange("rareTrades", 1, 1, 50);

			b.pop(); // wandering_trader
		}
	}
}
