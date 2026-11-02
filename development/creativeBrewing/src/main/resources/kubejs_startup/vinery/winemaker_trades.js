// requires: vinery
// Wandering Winemaker (Vinery) pool helpers, added to the pack's global.trades (startup_scripts/func/trade_helper.js,
// which loads first: the pack folder runs before the CB jar). Moved from trade_helper.js 2026-11-01
// (creative_brewing.md 4.5), unchanged apart from the class lookup: WinemakerTrades is CB Java
// (com.SaintAlfonzo.creativebrewing.trader), TradeListings stays in patcherMod and goes through global.trades._java.
// The stock rows: kubejs/data/creativebrewing/kubejs/vinery/wandering_trades.js. CB rebuild + restart.
;(function () {
	var T = global.trades
	if (!T) { console.error('[creativebrewing] global.trades missing: Winemaker trade helpers not added'); return }
	var $WinemakerTrades = null
	var winemaker = () => $WinemakerTrades || ($WinemakerTrades = Java.loadClass('com.SaintAlfonzo.creativebrewing.trader.WinemakerTrades'))

	// Wandering Winemaker (Vinery): its own pool, separate from the Wandering Trader's.
	// Call winemakerReset() once at the top of the event, then winemakerSell() as often as needed.
	T.winemakerReset = function () {
		winemaker().reset()
	}

	T.winemakerSell = function (items, opts) {
		const o = this._opts(opts)
		const TL = this._java('TradeListings')
		const WT = winemaker()
		let added = 0
		this._arr(items).forEach(id => {
			if (!this._validItem(id, 'winemakerSell')) return
			const listing = TL.item(id, o.count, o.emeralds, o.maxUses)
			for (let w = 0; w < o.weight; w++) if (WT.add(listing)) added++
		})
		return added
	}

	T.winemakerSize = function () {
		return winemaker().size()
	}
})()
