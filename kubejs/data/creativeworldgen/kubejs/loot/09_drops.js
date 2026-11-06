// priority: 99
// requires: creativeworldgen
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

        var conditions = []
        if (opts.knife === true || opts.knife === false) conditions.push(knifeCondition(opts.knife))
        if (opts.burning === true || opts.burning === false) conditions.push(onFire(opts.burning))
        if (opts.tool) conditions.push({ condition: 'forge:can_tool_perform_action', action: String(opts.tool) })
        var chance = opts.chance === undefined ? 1 : opts.chance
        if (opts.looting > 0) {
            conditions.push({ condition: 'minecraft:random_chance_with_looting', chance: chance, looting_multiplier: opts.looting })
        } else if (chance < 1) {
            conditions.push({ condition: 'minecraft:random_chance', chance: chance })
        }

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
            var pool = { rolls: 1, bonus_rolls: 0, entries: [entry] }
            if (conditions.length) pool.conditions = conditions
            T.drops.push({ table: table, pool: JSON.parse(JSON.stringify(pool)), item: normId(item) })
        }
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
        T.drops.forEach(function (d) {
            var t = byTable[d.table] = byTable[d.table] || { pools: [], shares: [] }
            t.pools.push(d)
        })
        T.dropShares.forEach(function (d) {
            var t = byTable[d.table] = byTable[d.table] || { pools: [], shares: [] }
            t.shares.push(d)
        })
        var stats = { tables: 0, pools: 0, shares: 0 }
        Object.keys(byTable).forEach(function (table) {
            var todo = byTable[table]
            try {
                event.modify(table, function (b) {
                    var pools = JSON.parse(String(b.pools.toString()))
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
    })

})()
