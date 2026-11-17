// Trade helper — global.trades. Edits the Wandering Trader's two pools, and adds villager trades (villager(), at the
// end). Call it inside MoreJSEvents.wandererTrades: MoreJS fires that at server start (after every mod has added its
// listings) and again on /reload, each time from the untouched pools, so edits never stack. Listings are built in
// patcherMod (trader.TradeListings); counts and spawn timers are CEConfig [wandering_trader]. Reference:
// feature_writeup/wandering_trader.md
console.log('Trade Helper starting')

global.trades = {

	COMMON: 1,
	RARE: 2,

	_classes: {},
	_java(name) {
		if (!this._classes[name]) this._classes[name] = Java.loadClass('com.SaintAlfonzo.creativecommon.trader.' + name)
		return this._classes[name]
	},

	_opts(opts) {
		opts = opts || {}
		return {
			emeralds: opts.emeralds || 1,
			count: opts.count || 1,
			maxUses: opts.maxUses || 12,
			weight: opts.weight || 1,
		}
	},

	_arr(v) { return Array.isArray(v) ? v : [v] },

	_validItem(id, ctx) {
		if (typeof id !== 'string' || id.length === 0 || id.charAt(0) === '#') {
			console.warn('global.trades.' + ctx + ': not an item id: ' + id + ' (missing item_dict key?)')
			return false
		}
		return true
	},

	// Emeralds for items, one listing per item, `weight` copies each (a copy is another chance to be picked).
	// t.sell(e, t.COMMON, [i.oak_sapling, i.birch_sapling], { emeralds: 5, maxUses: 8 })
	// Returns the number of listings added.
	sell(e, pool, items, opts) {
		const o = this._opts(opts)
		const list = e.getTrades(pool)
		const TL = this._java('TradeListings')
		let added = 0
		this._arr(items).forEach(id => {
			if (!this._validItem(id, 'sell')) return
			const listing = TL.item(id, o.count, o.emeralds, o.maxUses)
			if (listing == null) return
			for (let w = 0; w < o.weight; w++) { list.add(listing); added++ }
		})
		return added
	},

	// A random member of an item tag, picked again for every trader: one entry stands for the whole tag.
	// t.sellFromTag(e, t.RARE, i.t_trades_golden_food, { emeralds: 6, maxUses: 3 })
	sellFromTag(e, pool, tag, opts) {
		const o = this._opts(opts)
		if (typeof tag !== 'string' || tag.length === 0) {
			console.warn('global.trades.sellFromTag: not a tag: ' + tag)
			return 0
		}
		const listing = this._java('TradeListings').fromTag(tag, o.count, o.emeralds, o.maxUses)
		const list = e.getTrades(pool)
		for (let w = 0; w < o.weight; w++) list.add(listing)
		return o.weight
	},

	// Removes every listing whose result matches: 'ns:id' for one item, '@ns' for a whole mod.
	// The result is read from one sample roll; a listing that needs a live entity to roll can't be matched.
	// t.remove(e, [t.COMMON, t.RARE], ['@lightmanscurrency', i.glowstone])  -> removed listings (Java list)
	remove(e, pools, matchers) {
		const TL = this._java('TradeListings')
		const removed = []
		this._arr(pools).forEach(pool => {
			const taken = TL.take(e.getTrades(pool), this._arr(matchers))
			taken.forEach(l => removed.push(l))
		})
		return removed
	},

	// Moves matching listings into `toPool`, `weight` copies each (weight 20 = 20 times as likely to be picked).
	// t.move(e, [t.COMMON, t.RARE], [i.sealed_delivery_agreement], t.COMMON, 20)
	move(e, pools, matchers, toPool, weight) {
		const taken = this.remove(e, pools, matchers)
		const list = e.getTrades(toPool)
		const copies = weight || 1
		taken.forEach(l => { for (let w = 0; w < copies; w++) list.add(l) })
		return taken.length
	},

	// ---- Villager trades (plan_rules_fixes.md section 12c, V-33). Call inside MoreJSEvents.villagerTrades.

	// An item for a villager trade: an item_dict key ('emerald'), an id ('minecraft:emerald'), or [keyOrId, count].
	// Returns an ItemStack, or null (warned) for an unknown key, a tag or an empty value.
	_tradeStack(v, ctx) {
		var count = 1
		if (Array.isArray(v)) {
			count = v[1] || 1
			v = v[0]
		}
		if (typeof v === 'string' && v.indexOf(':') < 0 && v.charAt(0) !== '#') {
			var keyed = global.item[v]
			if (typeof keyed !== 'string') {
				console.warn('global.trades.villager: ' + ctx + ' "' + v + '" is not an item_dict key')
				return null
			}
			v = keyed
		}
		if (!this._validItem(v, 'villager ' + ctx)) return null
		return Item.of(v, count)
	},

	// One villager trade. profession: a global.profession key ('farmer') or an id ('minecraft:farmer'); level 1 to 5.
	// buy, buy2 (optional) and sell take a key, an id or [keyOrId, count]. uses, xp and priceMultiplier are optional
	// (MoreJS defaults when left out). Returns the MoreJS trade, or null when an item was rejected (nothing added).
	//   t.villager(e, 'farmer', 1, { buy: [i.broccoli, 26], sell: 'emerald', uses: 12, xp: 2, priceMultiplier: 0.05 })
	villager(e, profession, level, o) {
		if (!e) throw new Error('global.trades.villager called without the event')
		o = o || {}
		var prof = String(profession)
		if (prof.indexOf(':') < 0) {
			var keyed = global.profession ? global.profession[prof] : undefined
			if (typeof keyed !== 'string') {
				console.warn('global.trades.villager: "' + prof + '" is not a global.profession key')
				return null
			}
			prof = keyed
		}
		if (!(level >= 1 && level <= 5)) throw new Error('global.trades.villager: level must be 1 to 5, got ' + level)
		var buy = this._tradeStack(o.buy, 'buy')
		var buy2 = o.buy2 === undefined ? undefined : this._tradeStack(o.buy2, 'buy2')
		var sell = this._tradeStack(o.sell, 'sell')
		if (!buy || buy2 === null || !sell) return null
		var trade = e.addTrade(prof, level, buy2 ? [buy, buy2] : [buy], sell)
		if (o.uses !== undefined) trade.maxUses(o.uses)
		if (o.xp !== undefined) trade.villagerExperience(o.xp)
		if (o.priceMultiplier !== undefined) trade.priceMultiplier(o.priceMultiplier)
		return trade
	},
}

console.log('Trade Helper done')
