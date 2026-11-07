// requires: creativedelight
// requires: rusticdelight
// requires: morejs
// Rustic Delight 1.7.0 Wandering Trader stock (feature_writeup/rustic_delight.md 5.4): Pale and Dark seeds as rare
// offers, one seed for 48 emeralds (owner 2026-11-02; upstream sells them in the common pool at 5 emeralds, 3 uses). Base seeds and beans stay the port's own
// common trades. Pools and helper as economy/wandering_trades.js.
MoreJSEvents.wandererTrades(e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return
	var i = global.item
	var t = global.trades
	t.sell(e, t.RARE, [i.pale_bell_pepper_seeds, i.dark_bell_pepper_seeds], { emeralds: 48, maxUses: 3 })
})
