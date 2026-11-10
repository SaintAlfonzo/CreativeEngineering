// requires: creativedelight
// requires: hearthandharvest
// requires: morejs
// Hearth and Harvest trades (hearth_and_harvest.md 2.2, step 21, N15), through the pack's helpers; none of 1.4.1's own trade
// code is ported. Prices follow corn_delight/trades.js.
MoreJSEvents.villagerTrades(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var farmer = 'minecraft:farmer'

	// Level 1 - Novice: buys the new crops
	e.addTrade(farmer, 1, [Item.of(i.hops, 24)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(2).priceMultiplier(0.05)
	e.addTrade(farmer, 1, [Item.of(i.raspberry, 24)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(2).priceMultiplier(0.05)
	e.addTrade(farmer, 1, [Item.of(i.sunflower_seeds, 32)], Item.of(i.emerald, 1)).maxUses(16).villagerExperience(2).priceMultiplier(0.05)

	// Botanist (Beautify's villager, when loaded): sells mums
	if (global.has('beautify')) {
		;['white', 'red', 'orange', 'yellow', 'light_blue', 'blue', 'purple', 'pink'].forEach(c => {
			e.addTrade('beautify:botanist', 1, [Item.of(i.emerald, 1)], Item.of(i[c + '_mum'], 4)).maxUses(12).villagerExperience(2).priceMultiplier(0.05)
		})
	}
})

MoreJSEvents.wandererTrades(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var t = global.trades
	t.sell(e, t.COMMON, [i.hops, i.raspberry_pips, i.sunflower_seeds, i.blueberry_pips], { emeralds: 1, maxUses: 12 })
})
