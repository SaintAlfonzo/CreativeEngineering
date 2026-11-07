// requires: creativedelight
// requires: veggiesdelight
// requires: morejs
// Veggies Delight trades (Creative Delight members #2, moved from the pack's economy/trades.js and
// economy/wandering_trades.js 2026-10-25, rows unchanged): feature_writeup/veggies_delight.md 4.13. Both events run
// after the pack's handlers, so the pack's trade removals never reach these.
MoreJSEvents.villagerTrades(e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var i = global.item
	var v = { farmer: 'minecraft:farmer' }

	// Farmer: Veggies Delight's produce trades, as upstream had them in code. Its bell pepper trade is dropped with the crop.
	// Level 1 - Novice
	e.addTrade(v.farmer, 1, [Item.of(i.broccoli, 26)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
	e.addTrade(v.farmer, 1, [Item.of(i.cauliflower, 16)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(5).priceMultiplier(0.05)
	e.addTrade(v.farmer, 1, [Item.of(i.garlic, 26)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
	e.addTrade(v.farmer, 1, [Item.of(i.sweet_potato, 26)], Item.of(i.emerald, 1)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
	e.addTrade(v.farmer, 1, [Item.of(i.turnip, 16)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(5).priceMultiplier(0.05)
	e.addTrade(v.farmer, 1, [Item.of(i.zucchini, 16)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(5).priceMultiplier(0.05)
})

// Wandering Trader: upstream's list, minus its bell pepper seeds. Pools and helper as economy/wandering_trades.js.
MoreJSEvents.wandererTrades(e => {
	if (!global.CD || !global.CD.on('veggiesdelight')) return
	var i = global.item
	var t = global.trades
	t.sell(e, t.COMMON, [
		i.broccoli_seeds, i.cauliflower_seeds, i.turnip_seeds, i.zucchini_seeds,
		i.garlic_clove, i.sweet_potato, i.dandelion_leaf,
	], { emeralds: 1, maxUses: 12 })
})
