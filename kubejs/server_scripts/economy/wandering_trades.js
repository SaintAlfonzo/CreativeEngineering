// Wandering Trader and Wandering Winemaker stock.
// Pools: COMMON (25 offers drawn per trader) and RARE (5 drawn); counts and the two independent spawn
// timers are CEConfig [wandering_trader]. The Winemaker draws 8 from its own pool.
// Rule: nothing that skips a trip to the Nether or the End (nether wart, glowstone, basalt blocks,
// Nether/End plants and mobs), in any trader's stock.
// Helper: global.trades (startup_scripts/func/trade_helper.js). Reference: feature_writeup/wandering_trader.md

MoreJSEvents.wandererTrades(e => {
	// Fires at server start and on /reload: re-read the dict, top-level `i` is shared by every server script
	var i = global.item
	var t = global.trades

	// ---------- removals ----------
	// Tech blocks sold far below their recipe cost, and Lightman's Currency's coin-priced machines
	t.remove(e, [t.COMMON, t.RARE], ['@computercraft', '@advancedperipherals', '@lightmanscurrency'])
	// Nether-gated: glowstone; Architect's Palette sunstone (basalt) and moonstone (sunstone warped in a portal)
	t.remove(e, [t.COMMON, t.RARE], [i.glowstone_block, i.sunstone, i.moonstone])

	// Wares delivery agreements: both listings (regular 6 emeralds, rare 12) into COMMON at 20x weight each
	t.move(e, [t.COMMON, t.RARE], [i.sealed_delivery_agreement], t.COMMON, 20)

	// ---------- COMMON ----------
	// saplings not sold yet (Fruits Delight goes to the Winemaker; Architect's Palette twisted sapling is Nether)
	t.sell(e, t.COMMON, [
		i.azalea, i.flowering_azalea,
		i.blue_blossom_sapling, i.lavender_blossom_sapling, i.orange_blossom_sapling,
		i.pink_blossom_sapling, i.red_blossom_sapling, i.yellow_blossom_sapling,
		i.river_sapling, i.rubberwood_sapling,
	], { emeralds: 5, maxUses: 8 })

	// tall flowers
	t.sell(e, t.COMMON, [i.lilac, i.peony, i.rose_bush, i.sunflower, i.flowering_rush], { emeralds: 1, maxUses: 8 })

	// seeds and crop starters. Berry Good makes the berries unplantable, so the pips are sold, not the berries.
	// Left out as Nether/End: nether wart, chorus flower, propelplant cane, Farmer's Respite coffee beans
	// (its wild coffee only grows in the Nether).
	t.sell(e, t.COMMON, [
		i.corn_seeds, i.tea_seeds,
		i.blueberry, i.cranberry, i.hamimelon_seeds, i.lemon_seeds,
		i.sweet_berry_pips, i.glow_berry_pips,
		i.minecraft_carrot, i.minecraft_potato, i.cocoa_beans, i.bamboo,
		i.seed, i.mulberry,
	], { emeralds: 1, maxUses: 12 })

	// plants (no Nether roots, sprouts, vines or fungi)
	t.sell(e, t.COMMON, [
		i.grass, i.tall_grass, i.large_fern, i.dead_bush, i.seagrass, i.moss_carpet,
		i.glow_lichen, i.hanging_roots, i.big_dripleaf, i.glow_shroom,
		i.ivy, i.moss_clump, i.frosty_grass, i.frosty_fern,
	], { emeralds: 1, maxUses: 12 })
	t.sell(e, t.COMMON, [i.spore_blossom, i.four_leaf_clover], { emeralds: 3, maxUses: 4 })

	// natural blocks (no nylium or soul soil)
	t.sell(e, t.COMMON, [i.coarse_dirt], { emeralds: 1, count: 8, maxUses: 8 })
	t.sell(e, t.COMMON, [i.clay, i.mud, i.ice, i.snow_block, i.calcite, i.dripstone], { emeralds: 1, count: 4, maxUses: 8 })
	t.sell(e, t.COMMON, [i.grass_block], { emeralds: 1, count: 2, maxUses: 6 })
	t.sell(e, t.COMMON, [i.mycelium], { emeralds: 3, count: 2, maxUses: 6 })

	// ---------- RARE ----------
	// one random golden food / canned golden food per listing (tags in tags/tradetags.js)
	t.sellFromTag(e, t.RARE, i.t_trades_golden_food, { emeralds: 6, maxUses: 3 })
	t.sellFromTag(e, t.RARE, i.t_trades_canned_golden, { emeralds: 12, maxUses: 2 })

	// mob buckets (cosmic cod and stradpole stay out: End and Nether)
	t.sell(e, t.RARE, [i.cod_bucket, i.salmon_bucket, i.tadpole_bucket, i.squid_bucket], { emeralds: 3, maxUses: 4 })
	t.sell(e, t.RARE, [
		i.glow_squid_bucket, i.jellyfish_bucket, i.perch_bucket, i.crab_bucket, i.axolotl_bucket,
		i.terrapin_bucket, i.small_catfish_bucket, i.medium_catfish_bucket, i.large_catfish_bucket,
		i.mudskipper_bucket, i.lobster_bucket, i.flying_fish_bucket,
	], { emeralds: 5, maxUses: 4 })

	// sponge has no renewable source; the ancient sapling only grows in ancient cities
	t.sell(e, t.RARE, [i.sponge, i.ancient_sapling], { emeralds: 8, maxUses: 3 })

	// ---------- Wandering Winemaker ----------
	t.winemakerReset()
	// no Fruits Delight apple sapling: Vinery's apple tree is the pack's apple tree
	t.winemakerSell([
		i.bayberry_sapling, i.durian_sapling, i.fig_sapling, i.hawberry_sapling,
		i.kiwi_sapling, i.lychee_sapling, i.mango_sapling, i.mangosteen_sapling, i.orange_sapling,
		i.peach_sapling, i.pear_sapling, i.persimmon_sapling,
	], { emeralds: 5, maxUses: 8 })
	t.winemakerSell([i.pineapple_sapling], { emeralds: 3, maxUses: 8 })
	// Vinery's own list sells the white jungle grape in this slot instead of its seeds
	t.winemakerSell([i.grape_seeds_white_jungle], { emeralds: 1, maxUses: 8 })

	console.log('[wandering trades] common ' + e.getTrades(t.COMMON).size() + ', rare ' + e.getTrades(t.RARE).size()
		+ ', winemaker ' + t.winemakerSize())
})
