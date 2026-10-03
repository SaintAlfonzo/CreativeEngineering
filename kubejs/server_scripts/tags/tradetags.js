// Trade tags: item groups a Wandering Trader listing sells one random member of
// (economy/wandering_trades.js, global.trades.sellFromTag). The tag is read each time an offer
// is rolled, so changes here reach traders spawned after a /reload.
// No Nether or End items: no trader sells those (feature_writeup/wandering_trader.md).

var t = global.tags
var i = global.item

ServerEvents.tags('item', e => {
	// golden food: the same plain-golden set as #creativeengineering:loot/golden_food (golden apple,
	// golden carrot, Golden Food's golden_*), minus golden chorus fruit (End)
	t.add(e, i.t_trades_golden_food, [i.golden_apple, i.golden_carrot])
	t.addMatching(e, i.t_trades_golden_food, 'goldenfood:golden_*', { except: i.golden_chorus_fruit })

	// canned golden food; golden cans added later go here too
	t.add(e, i.t_trades_canned_golden, [i.canned_golden_apples, i.canned_golden_carrot])
})
