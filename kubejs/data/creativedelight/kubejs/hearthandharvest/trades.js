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
	// Hops only with Vinery (plan_bugfix_1114 3.1)
	if (global.has('vinery')) global.trades.villager(e, farmer, 1, { buy: [i.hops, 24], sell: [i.emerald, 1], uses: 16, xp: 2, priceMultiplier: 0.05 })
	global.trades.villager(e, farmer, 1, { buy: [i.raspberry, 24], sell: [i.emerald, 1], uses: 16, xp: 2, priceMultiplier: 0.05 })
	global.trades.villager(e, farmer, 1, { buy: [i.sunflower_seeds, 32], sell: [i.emerald, 1], uses: 16, xp: 2, priceMultiplier: 0.05 })

	// Botanist (Beautify's villager, when loaded): sells mums
	if (global.has('beautify')) {
		;['white', 'red', 'orange', 'yellow', 'light_blue', 'blue', 'purple', 'pink'].forEach(c => {
			global.trades.villager(e, 'beautify:botanist', 1, { buy: [i.emerald, 1], sell: [i[c + '_mum'], 4], uses: 12, xp: 2, priceMultiplier: 0.05 })
		})
	}
})

MoreJSEvents.wandererTrades(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var i = global.item
	var t = global.trades
	var seeds = [i.raspberry_pips, i.sunflower_seeds, i.blueberry_pips]
	if (global.has('vinery')) seeds.unshift(i.hops)   // hops only with Vinery (plan_bugfix_1114 3.1)
	t.sell(e, t.COMMON, seeds, { emeralds: 1, maxUses: 12 })
})
