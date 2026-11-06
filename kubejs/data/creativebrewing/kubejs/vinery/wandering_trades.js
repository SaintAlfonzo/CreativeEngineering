// requires: vinery
// Wandering Winemaker stock (Vinery): draws 8 from its own pool. Moved from the pack's
// server_scripts/economy/wandering_trades.js 2026-11-01 (creative_brewing.md 4.5), rows unchanged. The pack's handler
// (Wandering Trader pools) runs first; this one only touches the Winemaker pool. Helpers: global.trades, Winemaker
// half in the CB jar's kubejs_startup/vinery/winemaker_trades.js. Reference: feature_writeup/wandering_trader.md
MoreJSEvents.wandererTrades(e => {
	if (!global.CB || !global.CB.on('vinery')) return
	var i = global.item
	var t = global.trades

	t.winemakerReset()
	// no Fruits Delight apple sapling: Vinery's apple tree is the pack's apple tree
	if (global.has('fruitsdelight')) {
		t.winemakerSell([
			i.bayberry_sapling, i.durian_sapling, i.fig_sapling, i.hawberry_sapling,
			i.kiwi_sapling, i.lychee_sapling, i.mango_sapling, i.mangosteen_sapling, i.orange_sapling,
			i.peach_sapling, i.pear_sapling, i.persimmon_sapling,
		], { emeralds: 5, maxUses: 8 })
		t.winemakerSell([i.pineapple_sapling], { emeralds: 3, maxUses: 8 })
	}
	// Vinery's own list sells the white jungle grape in this slot instead of its seeds
	t.winemakerSell([i.grape_seeds_white_jungle], { emeralds: 1, maxUses: 8 })

	console.log('[wandering trades] winemaker ' + t.winemakerSize())
})
