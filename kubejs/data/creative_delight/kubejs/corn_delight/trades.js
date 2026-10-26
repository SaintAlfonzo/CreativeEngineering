// requires: corn_delight
// requires: morejs
// Corn Delight trades (feature_writeup/corn_delight.md 2.6): 1.2.11's farmer and wandering trader trades, which 1.0.3
// does not have. Non-member file. Both events run after the pack's handlers, so the pack's trade removals never
// reach these.
MoreJSEvents.villagerTrades(e => {
	if (!global.CD) return
	var i = global.item
	var v = { farmer: 'minecraft:farmer' }

	// Level 1 - Novice: buys corn (upstream 16-32 or 32-48 a trade, one random price; here one fixed price)
	e.addTrade(v.farmer, 1, [Item.of(i.corn, 24)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(2).priceMultiplier(0.05)
	// Level 2 - Apprentice: sells Cornbread and Tortillas (upstream 6-16 for 6-16 emeralds)
	e.addTrade(v.farmer, 2, [Item.of(i.emerald, 8)], Item.of(i.cornbread, 8)).maxUses(16).villagerExperience(5).priceMultiplier(0.05)
	e.addTrade(v.farmer, 2, [Item.of(i.emerald, 8)], Item.of(i.tortilla, 8)).maxUses(16).villagerExperience(5).priceMultiplier(0.05)
})

// Wandering Trader: corn (kernels are already in the pack's COMMON seeds list, economy/wandering_trades.js) and the
// Popcorn Box in RARE, where 1.2.11 meant it (its code put it in the common list by mistake). Pools and helper as
// economy/wandering_trades.js.
MoreJSEvents.wandererTrades(e => {
	if (!global.CD) return
	var i = global.item
	var t = global.trades
	t.sell(e, t.COMMON, [i.corn], { emeralds: 1, maxUses: 12 })
	t.sell(e, t.RARE, [i.popcorn_box], { emeralds: 2, maxUses: 4 })
})
