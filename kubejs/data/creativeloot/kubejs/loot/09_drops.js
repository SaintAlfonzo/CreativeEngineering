// priority: 99
// requires: creativeloot
// Mob, block and fishing loot — DROPS.  Adds drop rules to the loot engine (global.lootTuning, 05_tuning.js), for the
// tables the chest framework leaves alone (entities/, blocks/, gameplay/). Like the rest of the engine it rewrites the
// table JSON before the game parses it (ServerEvents.genericLootTables), so a drop is a real loot pool: the vanilla
// conditions and the same odds a loot modifier would give, rebuilt at every /reload from the rule calls.
// First user: Miner's Delight's drops (creativedelight members #51, miners_delight.md D8), which replace its loot
// modifiers.
//
//   L.drop(tables, item, opts)         one extra pool in each table, rolled with the table's own pools
//     count    number | [min, max]     stack size (default 1; [0, 4] can give nothing)
//     chance   0..1                    chance per roll (default 1)
//     looting  number                  + this chance per Looting level of the killer (random_chance_with_looting)
//     knife    true | false            the killer holds a knife (#forge:tools/knives) / does not (no killer counts as
//                                      no knife); omitted: either
//     burning  true | false            only when the mob died on fire / not on fire
//     cooked   item | { item, count }  dropped instead of `item` when the mob died on fire (Fire Aspect, lava, sun):
//                                      the raw item only drops from a mob that was not burning
//     tool     'pickaxe_dig' ...       block tables: the breaking tool can do this Forge tool action
//   L.dropShare(table, item, share)    `share` of the rolls of the table's main pool (the one with the most entries)
//                                      give `item` instead: the pool's other entries keep their ratios and Luck
//                                      bonuses (weights and qualities are scaled together); exact with no Luck
//     killedByPlayer true              only when a player killed the mob (minecraft:killed_by_player)
//     holding  id | '#tag'             block tables: the breaking tool is this item or in this tag; entity tables: the
//                                      killer's main hand
//     notHolding id | '#tag'           the opposite of holding
//     silkTouch true | false           block tables: the breaking tool has / has not Silk Touch
//     state    { prop: 'value' }       block tables: the broken block's state (minecraft:block_state_property; the
//                                      block is the table's, or stateBlock)
//   L.swapDrop(tables, item, into, opts)  when the conditions of opts hold (the drop() options above), entries of
//                                      `item` drop nothing and a pool gives `into` instead ({ item, count } or an id);
//                                      otherwise the table is unchanged (the knife-scavenging rules: a pumpkin broken
//                                      with a knife gives 4 slices)
//   L.replaceDrops(tables, item, opts) every pool of the table is replaced by one pool giving `item` (count option)
//   L.rewrite(tables, fn, name)        fn(pools, functions, table) edits the raw table JSON in place and returns true
//                                      when it changed something; for rules with no shape above (Creative Delight's
//                                      soil yield and bell pepper weights). Tables of a mod that is not loaded are
//                                      skipped. Runs before the pools and shares above.
//   L.afterTables(fn)                  fn() runs once after this reload's tables are rewritten (summary log lines)
//   L.onlyDrop(entities, item, opts)   post-roll (a LootJS modifier, so after every mod's global loot modifiers): a
//                                      kill of one of these entity types drops `item` and nothing else, whatever
//                                      added it (Miner's Delight's squid chain). opts.logName: the caller's
//                                      R.logName(...)
//
// Tables: exact ids only ('minecraft:entities/bat'). An empty vanilla table (bat, silverfish) is fine. The pools are
// named ce_drop_<n>_<item> so a /dumploot export shows where they came from.
//
// Loads right after 05_tuning.js (priority 99 < 100), which has made this reload's global.lootTuning. Wrapped in an
// IIFE: all server scripts share one top-level scope.

(function () {

	var TAG = '[loot-drops] '
	var KNIVES = 'forge:tools/knives'
	var T = global.lootTuning
	if (!T) {
		console.error(TAG + 'global.lootTuning missing: 05_tuning.js did not run, drop rules are off')
		return
	}

	T.drops = []       // drop(): { table, pool }
	T.dropShares = []  // dropShare(): { table, item, share }
	T.swaps = []       // swapDrop(), replaceDrops(): { table, item, conditions, pool, all }
	T.rewrites = []    // rewrite(): { table, fn, name }
	T.afterFns = []    // afterTables()
	T.onlyDrops = []   // onlyDrop(): { entity, item, logName }

	function warn(msg) {
		if (T.stats && T.stats.warnings) T.stats.warnings.push(msg)
		console.warn(TAG + msg)
	}

	function normId(s) {
		s = String(s)
		if (s.charAt(0) === '#') s = s.substring(1)
		return s.indexOf(':') < 0 ? 'minecraft:' + s : s
	}

	function arr(v) {
		return v == null ? [] : (Array.isArray(v) ? v : [v])
	}

	function typeOf(table) {
		var path = table.substring(table.indexOf(':') + 1)
		if (path.indexOf('entities/') === 0) return 'minecraft:entity'
		if (path.indexOf('blocks/') === 0) return 'minecraft:block'
		if (path.indexOf('gameplay/fishing') === 0) return 'minecraft:fishing'
		return null
	}

	function okItem(item, where) {
		if (item == null || !Item.exists(normId(item))) {
			warn(where + ': ' + item + ' is not a registered item, skipped')
			return false
		}
		return true
	}

	function okTable(table, where) {
		if (!typeOf(table)) {
			warn(where + ': ' + table + ' is not an entities/, blocks/ or gameplay/fishing table, skipped')
			return false
		}
		return true
	}

	function onFire(flag) {
		return { condition: 'minecraft:entity_properties', entity: 'this', predicate: { flags: { is_on_fire: flag } } }
	}

	function knifeCondition(flag) {
		var held = { condition: 'minecraft:entity_properties', entity: 'killer', predicate: { equipment: { mainhand: { tag: KNIVES } } } }
		return flag ? held : { condition: 'minecraft:inverted', term: held }
	}

	function isBlock(table) {
		return typeOf(table) === 'minecraft:block'
	}

	// An item or '#tag' as a 1.19.2 item predicate.
	function itemPredicate(id) {
		id = String(id)
		return id.charAt(0) === '#' ? { tag: id.substring(1) } : { items: [normId(id)] }
	}

	function heldCondition(id, table) {
		if (isBlock(table)) return { condition: 'minecraft:match_tool', predicate: itemPredicate(id) }
		return { condition: 'minecraft:entity_properties', entity: 'killer', predicate: { equipment: { mainhand: itemPredicate(id) } } }
	}

	function not(term) {
		return { condition: 'minecraft:inverted', term: term }
	}

	// The conditions of the drop() options for one table, or null after a warning.
	function conditionsFor(opts, table, where, chanceAllowed) {
		var c = []
		if (opts.knife === true || opts.knife === false) {
			if (isBlock(table)) {
				var tool = heldCondition('#' + KNIVES, table)
				c.push(opts.knife ? tool : not(tool))
			} else {
				c.push(knifeCondition(opts.knife))
			}
		}
		if (opts.burning === true || opts.burning === false) c.push(onFire(opts.burning))
		if (opts.tool) c.push({ condition: 'forge:can_tool_perform_action', action: String(opts.tool) })
		if (opts.killedByPlayer === true) c.push({ condition: 'minecraft:killed_by_player' })
		if (opts.holding != null) c.push(heldCondition(opts.holding, table))
		if (opts.notHolding != null) c.push(not(heldCondition(opts.notHolding, table)))
		if (opts.silkTouch === true || opts.silkTouch === false || opts.state) {
			if (!isBlock(table)) {
				warn(where + ': silkTouch and state are for block tables, ' + table + ' skipped')
				return null
			}
			if (opts.silkTouch === true || opts.silkTouch === false) {
				var silk = { condition: 'minecraft:match_tool', predicate: { enchantments: [{ enchantment: 'minecraft:silk_touch', levels: { min: 1 } }] } }
				c.push(opts.silkTouch ? silk : not(silk))
			}
			if (opts.state) {
				// the block of a blocks/<id> table is <ns>:<id>; stateBlock names it when the table is shared
				c.push({ condition: 'minecraft:block_state_property', properties: opts.state,
					block: opts.stateBlock ? normId(opts.stateBlock) : table.replace(':blocks/', ':') })
			}
		}
		if (!chanceAllowed) return c
		var chance = opts.chance === undefined ? 1 : opts.chance
		if (opts.looting > 0) {
			c.push({ condition: 'minecraft:random_chance_with_looting', chance: chance, looting_multiplier: opts.looting })
		} else if (chance < 1) {
			c.push({ condition: 'minecraft:random_chance', chance: chance })
		}
		return c
	}

	function itemEntry(item, count) {
		var e = { type: 'minecraft:item', name: normId(item) }
		if (Array.isArray(count)) {
			e.functions = [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: count[0], max: count[1] } }]
		} else if (count != null && count !== 1) {
			e.functions = [{ function: 'minecraft:set_count', count: count }]
		}
		return e
	}

	T.drop = function (tables, item, opts) {
		opts = opts || {}
		var where = 'drop ' + item
		if (!okItem(item, where)) return
		// (not typeof: a global.item value can reach Rhino as a Java string, typeof 'object')
		var cooked = opts.cooked == null ? null : (opts.cooked.item != null ? opts.cooked : { item: opts.cooked })
		if (cooked && !okItem(cooked.item, where + ' cooked')) return
		if (opts.chance !== undefined && !(opts.chance > 0 && opts.chance <= 1)) {
			warn(where + ': chance must be in (0, 1], skipped')
			return
		}
		var count = opts.count === undefined ? 1 : opts.count

		var entry = itemEntry(item, count)
		if (cooked) {
			var hot = itemEntry(cooked.item, cooked.count === undefined ? count : cooked.count)
			hot.conditions = [onFire(true)]
			entry = { type: 'minecraft:alternatives', children: [hot, entry] }
		}

		var list = arr(tables)
		for (var i = 0; i < list.length; i++) {
			var table = normId(list[i])
			if (!okTable(table, where)) continue
			var conditions = conditionsFor(opts, table, where, true)
			if (conditions == null) continue
			var pool = { rolls: 1, bonus_rolls: 0, entries: [entry] }
			if (conditions.length) pool.conditions = conditions
			T.drops.push({ table: table, pool: JSON.parse(JSON.stringify(pool)), item: normId(item) })
		}
	}

	T.swapDrop = function (tables, item, into, opts) {
		opts = opts || {}
		var where = 'swapDrop ' + item
		var to = into != null && into.item != null ? into : { item: into }
		if (!okItem(item, where) || !okItem(to.item, where + ' into')) return
		if (opts.chance !== undefined || opts.looting !== undefined) {
			warn(where + ': chance and looting do not apply to a swap, skipped')
			return
		}
		var list = arr(tables)
		for (var i = 0; i < list.length; i++) {
			var table = normId(list[i])
			if (!okTable(table, where)) continue
			var conditions = conditionsFor(opts, table, where, false)
			if (conditions == null) continue
			if (!conditions.length) {
				warn(where + ': a swap needs at least one condition, ' + table + ' skipped')
				continue
			}
			T.swaps.push({ table: table, item: normId(item), conditions: conditions,
				pool: { rolls: 1, bonus_rolls: 0, entries: [itemEntry(to.item, to.count === undefined ? 1 : to.count)],
					conditions: JSON.parse(JSON.stringify(conditions)) } })
		}
	}

	T.replaceDrops = function (tables, item, opts) {
		opts = opts || {}
		var where = 'replaceDrops ' + item
		if (!okItem(item, where)) return
		var list = arr(tables)
		for (var i = 0; i < list.length; i++) {
			var table = normId(list[i])
			if (!okTable(table, where)) continue
			T.swaps.push({ table: table, item: normId(item), all: true,
				pool: { rolls: 1, bonus_rolls: 0, entries: [itemEntry(item, opts.count === undefined ? 1 : opts.count)] } })
		}
	}

	T.rewrite = function (tables, fn, name) {
		var list = arr(tables)
		for (var i = 0; i < list.length; i++) {
			var table = normId(list[i])
			if (!okTable(table, 'rewrite ' + name)) continue
			T.rewrites.push({ table: table, fn: fn, name: name || 'rewrite' })
		}
	}

	T.onlyDrop = function (entities, item, opts) {
		opts = opts || {}
		if (!okItem(item, 'onlyDrop ' + item)) return
		arr(entities).forEach(function (e) {
			T.onlyDrops.push({ entity: normId(e), item: normId(item), logName: opts.logName || ('CL/only_drop/' + normId(e)) })
		})
	}

	T.afterTables = function (fn) {
		T.afterFns.push(fn)
	}

	// Every item entry of `item` (at any depth) gets the inverted swap conditions, so it drops only when they fail.
	function guardEntries(node, item, guard) {
		var n = 0
		if (Array.isArray(node)) {
			node.forEach(function (x) { n += guardEntries(x, item, guard) })
			return n
		}
		if (!node || typeof node !== 'object') return 0
		if (node.type === 'minecraft:item' && normId(node.name) === item) {
			node.conditions = (node.conditions || []).concat([JSON.parse(JSON.stringify(guard))])
			return 1
		}
		if (Array.isArray(node.children)) n += guardEntries(node.children, item, guard)
		if (Array.isArray(node.entries)) n += guardEntries(node.entries, item, guard)
		return n
	}

	function applySwap(pools, swap, table) {
		if (swap.all) {
			pools.splice(0, pools.length)
			pools.push(JSON.parse(JSON.stringify(swap.pool)))
			return true
		}
		var guard = not(swap.conditions.length === 1 ? swap.conditions[0] : { condition: 'minecraft:all_of', terms: swap.conditions })
		if (!guardEntries(pools, swap.item, guard)) {
			warn(table + ': no ' + swap.item + ' entry to swap, skipped')
			return false
		}
		pools.push(JSON.parse(JSON.stringify(swap.pool)))
		return true
	}

	T.dropShare = function (table, item, share) {
		var where = 'dropShare ' + item
		if (!okItem(item, where)) return
		table = normId(table)
		if (!okTable(table, where)) return
		if (!(share > 0 && share < 1)) {
			warn(where + ': share must be between 0 and 1, skipped')
			return
		}
		T.dropShares.push({ table: table, item: normId(item), share: share })
	}

	// The main pool's entries keep their ratios; the new entries get `share` of the new total. Weights (and qualities,
	// which scale Luck's effect with them) are multiplied by the smallest k that makes every new weight an integer
	// within 0.2%.
	function applyShares(pools, shares, table) {
		var best = -1
		var most = -1
		for (var p = 0; p < pools.length; p++) {
			var n = (pools[p].entries || []).length
			if (n > most) {
				most = n
				best = p
			}
		}
		if (best < 0 || most <= 0) {
			warn(table + ': no pool to share, dropShare skipped')
			return 0
		}
		var pool = pools[best]
		var total = 0
		for (var e = 0; e < pool.entries.length; e++) total += typeof pool.entries[e].weight === 'number' ? pool.entries[e].weight : 1
		var sum = 0
		for (var s = 0; s < shares.length; s++) sum += shares[s].share
		if (!(sum < 1) || total <= 0) {
			warn(table + ': dropShare shares add up to ' + sum + ', skipped')
			return 0
		}
		var k = 1
		for (; k <= 1000; k++) {
			var ok = true
			for (var s2 = 0; s2 < shares.length; s2++) {
				var x = shares[s2].share / (1 - sum) * total * k
				if (Math.round(x) < 1 || Math.abs(Math.round(x) - x) > 0.002 * x) ok = false
			}
			if (ok) break
		}
		if (k > 1000) k = 1000
		for (var e2 = 0; e2 < pool.entries.length; e2++) {
			var en = pool.entries[e2]
			en.weight = (typeof en.weight === 'number' ? en.weight : 1) * k
			if (typeof en.quality === 'number') en.quality = en.quality * k
		}
		for (var s3 = 0; s3 < shares.length; s3++) {
			pool.entries.push({ type: 'minecraft:item', name: shares[s3].item, weight: Math.max(1, Math.round(shares[s3].share / (1 - sum) * total * k)) })
		}
		return shares.length
	}

	ServerEvents.genericLootTables(function (event) {
		var byTable = {}
		function slot(table) {
			return byTable[table] = byTable[table] || { pools: [], shares: [], swaps: [], rewrites: [] }
		}
		T.rewrites.forEach(function (d) {
			if (Platform.isLoaded(d.table.split(':')[0])) slot(d.table).rewrites.push(d)
		})
		T.swaps.forEach(function (d) { slot(d.table).swaps.push(d) })
		T.drops.forEach(function (d) { slot(d.table).pools.push(d) })
		T.dropShares.forEach(function (d) { slot(d.table).shares.push(d) })
		var stats = { tables: 0, pools: 0, shares: 0, swaps: 0, rewrites: 0 }
		Object.keys(byTable).forEach(function (table) {
			var todo = byTable[table]
			try {
				event.modify(table, function (b) {
					var pools = JSON.parse(String(b.pools.toString()))
					var functions = b.functions ? JSON.parse(String(b.functions.toString())) : []
					for (var r = 0; r < todo.rewrites.length; r++) {
						var res = callGuardedRewrite(todo.rewrites[r], pools, functions, table)
						if (res) stats.rewrites++
					}
					if (functions.length) b.functions = JsonIO.parseRaw(JSON.stringify(functions))
					for (var w = 0; w < todo.swaps.length; w++) if (applySwap(pools, todo.swaps[w], table)) stats.swaps++
					if (todo.shares.length) stats.shares += applyShares(pools, todo.shares, table)
					for (var i = 0; i < todo.pools.length; i++) {
						var pool = todo.pools[i].pool
						pool.name = 'ce_drop_' + pools.length + '_' + todo.pools[i].item.replace(/[^a-z0-9_]/g, '_')
						pools.push(pool)
						stats.pools++
					}
					b.pools = JsonIO.parseRaw(JSON.stringify(pools))
					b.type = typeOf(table)
					stats.tables++
				})
			} catch (err) {
				warn(table + ': ' + err)
			}
		})
		console.info(TAG + stats.pools + ' drop pools and ' + stats.shares + ' shared entries in ' + stats.tables + ' tables')
		console.info(TAG + stats.swaps + ' swaps and ' + stats.rewrites + ' table rewrites')
		T.afterFns.forEach(function (fn) {
			try {
				fn()
			} catch (err) {
				warn('afterTables: ' + err)
			}
		})
	})

	// Post-roll rules: the queued rules ran at highPriorityData, before LootJS builds its modifiers on this reload.
	LootJS.modifiers(function (event) {
		T.onlyDrops.forEach(function (d) {
			event.addEntityLootModifier(d.entity)
				.removeLoot(Ingredient.all)
				.addLoot(d.item)
				.logName(d.logName)
		})
		if (T.onlyDrops.length) console.info(TAG + T.onlyDrops.length + ' only-drop rules')
	})

	function callGuardedRewrite(d, pools, functions, table) {
		try {
			return d.fn(pools, functions, table) === true
		} catch (err) {
			if (String(err).indexOf('[skip]') < 0) warn(table + ' (' + d.name + '): ' + err)
			return false
		}
	}

})()
