// Chest loot — TUNING.  Config-driven "never in any chest" / "N× as often in every chest".
//
// Edit loot_config.json (this folder), then /reload. Rebuild the catalog after mod updates with
// `python3 tools/gen_loot_config.py` (keeps your edits). `/loottuning` prints what the last reload did.
//
// Two stages, both rebuilt on every /reload:
//   1. PRE-ROLL  ServerEvents.genericLootTables — every table loot_index.json lists as holding a
//      direct entry of a tuned item gets its raw JSON pools rewritten before the game parses them:
//      the item's weight is cut so it's picked `frequency`× as often per roll (removed = entry
//      dropped), and the lost share goes to the pool's other entries ("redistribute") or to an
//      added empty entry ("empty"), per settings.slack. Pools are never dropped (pool count/names
//      stay stable for mods that look pools up in code).
//   2. POST-ROLL LootJS, every chest roll — catches what the JSON pass can't see (global loot
//      modifiers, pools/tables mods build in code, tag entries): removed items are stripped, reduced
//      items are kept with probability `frequency` — unless the pre-roll pass already covers that
//      item for this table (index "reach"), so nothing is reduced twice.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var DIR = 'kubejs/server_scripts/loot/'
    var CONFIG_PATH = DIR + 'loot_config.json'
    var INDEX_PATH = DIR + 'loot_index.json'
    var MISSING = 'loot-tuning:missing-table'
    var UNCHANGED = 'loot-tuning:unchanged'
    var TAG = '[loot-tuning] '

    var T = {
        config: null,
        index: null,
        settings: { enabled: true, slack: 'redistribute', log_tables: false },
        explicit: {},   // item id -> factor (0 = remove), from config.items
        patterns: [],   // { key, test(id), factor, except{} } in file order
        cache: {},      // item id -> factor | null
        active: false,
        preRollRan: false,
        stats: null,
    }

    function freshStats() {
        return {
            rules: 0, tables: 0, entriesRemoved: 0, poolsReweighted: 0, fallbackPools: 0,
            missingTables: [], postRemoved: 0, postThinned: 0, warnings: [],
        }
    }

    function warn(msg) {
        T.stats.warnings.push(msg)
        console.warn(TAG + msg)
    }

    function normId(s) {
        s = String(s)
        return s.indexOf(':') < 0 ? 'minecraft:' + s : s
    }

    function readJson(path) {
        try {
            return JSON.parse(String(JsonIO.readString(path)))
        } catch (err) {
            console.error(TAG + 'could not read ' + path + ': ' + err)
            return null
        }
    }

    // ------------------------------------------------------------------------------ rules

    // A config value -> factor in [0,1], or undefined for "no rule" (frequency 1 / invalid).
    function factorOf(spec, where) {
        if (!spec || typeof spec !== 'object') return undefined
        if (spec.remove === true) return 0
        if (spec.frequency === undefined || spec.frequency === 1) return undefined
        var f = spec.frequency
        if (typeof f !== 'number' || isNaN(f) || f < 0) {
            warn(where + ': frequency must be a number from 0 to 1, ignoring ' + JSON.stringify(f))
            return undefined
        }
        if (f > 1) {
            warn(where + ': frequency ' + f + ' clamped to 1 (boosting is not supported here)')
            return undefined
        }
        return f
    }

    function parsePattern(key, spec) {
        var f = factorOf(spec, 'pattern ' + key)
        if (f === undefined) return null
        var test = null
        if (key.charAt(0) === '@') {
            var prefix = key.substring(1) + ':'
            test = function (id) { return id.indexOf(prefix) === 0 }
        } else if (key.charAt(0) === '/' && key.lastIndexOf('/') > 0) {
            var cut = key.lastIndexOf('/')
            var re = new RegExp(key.substring(1, cut), key.substring(cut + 1))
            test = function (id) { return re.test(id) }
        } else {
            warn('pattern "' + key + '" ignored: use "@modid" or "/regex/" (tags aren\'t bound while loot loads)')
            return null
        }
        var except = {}
        var list = spec.except || []
        for (var i = 0; i < list.length; i++) except[normId(list[i])] = true
        return { key: key, test: test, factor: f, except: except }
    }

    function loadRules() {
        T.explicit = {}
        T.patterns = []
        T.cache = {}
        var items = T.config.items || {}
        for (var id in items) {
            var f = factorOf(items[id], id)
            if (f === undefined) continue
            if (!Item.exists(id)) warn(id + ' is not a registered item (rule kept anyway)')
            T.explicit[id] = f
            T.stats.rules++
        }
        var pats = T.config.patterns || {}
        for (var key in pats) {
            var p = parsePattern(key, pats[key])
            if (p) {
                T.patterns.push(p)
                T.stats.rules++
            }
        }
    }

    // factor for an item id, or undefined when it isn't tuned
    function ruleFor(id) {
        var hit = T.cache[id]
        if (hit !== undefined) return hit === null ? undefined : hit
        var f = T.explicit[id]
        if (f === undefined) {
            for (var i = 0; i < T.patterns.length; i++) {
                var p = T.patterns[i]
                if (!p.except[id] && p.test(id)) {
                    f = p.factor
                    break
                }
            }
        }
        T.cache[id] = f === undefined ? null : f
        return f
    }

    // ------------------------------------------------------------------------------ pre-roll

    function entryType(e) {
        return normId(e.type || 'minecraft:item')
    }

    function weightOf(e) {
        return typeof e.weight === 'number' ? e.weight : 1
    }

    // Smallest multiple of the pool's old total weight that turns every probability into an integer
    // weight within 1% (nonzero ones never round to 0).
    function integerWeights(qs, base) {
        var best = null
        for (var k = 1; k <= 1000; k++) {
            var n = base * k
            var ok = true
            var out = []
            for (var i = 0; i < qs.length; i++) {
                var x = qs[i] * n
                var r = Math.round(x)
                if (qs[i] > 0 && r < 1) {
                    r = 1
                    ok = false
                } else if (qs[i] > 0 && Math.abs(r - x) > 0.01 * x) {
                    ok = false
                }
                out.push(r)
            }
            if (ok) return out
            best = out
            if (n > 1000000) break
        }
        return best
    }

    // Composite children (alternatives/group/sequence) carry no pool weight of their own, so a tuned
    // child is dropped (remove) or gated with a random_chance condition. Returns false when emptied.
    function tuneChildren(entry, touched) {
        var kept = []
        for (var i = 0; i < entry.children.length; i++) {
            var c = entry.children[i]
            if (c.children) {
                if (!tuneChildren(c, touched)) continue
            } else if (entryType(c) === 'minecraft:item' && c.name != null) {
                var f = ruleFor(normId(c.name))
                if (f === 0) {
                    touched.removed++
                    touched.any = true
                    continue
                }
                if (f !== undefined) {
                    c.conditions = (c.conditions || []).concat([{ condition: 'minecraft:random_chance', chance: f }])
                    touched.any = true
                }
            }
            kept.push(c)
        }
        entry.children = kept
        return kept.length > 0
    }

    // Pools whose entries don't map 1:1 onto candidates (composites, expand:true tags) can't be
    // reweighted exactly — tuned items there get dropped or gated by a random_chance condition instead.
    function needsFallback(entries) {
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i]
            if (e.children || (entryType(e) === 'minecraft:tag' && e.expand)) return true
        }
        return false
    }

    function fallbackPool(pool, stats) {
        var touched = { any: false, removed: 0 }
        var out = []
        for (var i = 0; i < pool.entries.length; i++) {
            var e = pool.entries[i]
            if (e.children) {
                if (!tuneChildren(e, touched)) continue
            } else if (entryType(e) === 'minecraft:item' && e.name != null) {
                var f = ruleFor(normId(e.name))
                if (f === 0) {
                    touched.removed++
                    touched.any = true
                    continue
                }
                if (f !== undefined) {
                    e.conditions = (e.conditions || []).concat([{ condition: 'minecraft:random_chance', chance: f }])
                    touched.any = true
                }
            }
            out.push(e)
        }
        if (!touched.any) return false
        pool.entries = out.length ? out : [{ type: 'minecraft:empty', weight: 1 }]
        stats.entriesRemoved += touched.removed
        stats.fallbackPools++
        return true
    }

    function rewritePool(pool, slack, stats) {
        var entries = pool.entries
        if (!entries || !entries.length) return false
        if (needsFallback(entries)) return fallbackPool(pool, stats)

        var fs = []
        var touched = false
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i]
            var f = 1
            if (entryType(e) === 'minecraft:item' && e.name != null) {
                var r = ruleFor(normId(e.name))
                if (r !== undefined) {
                    f = r
                    touched = true
                }
            }
            fs.push(f)
        }
        if (!touched) return false

        var ws = []
        var total = 0
        for (var j = 0; j < entries.length; j++) {
            ws.push(weightOf(entries[j]))
            total += ws[j]
        }
        if (total <= 0) return false

        // target probabilities per roll: tuned entries get exactly f × their old share
        var qs = []
        var kept = 0     // share of untouched entries
        var lost = 0     // share the tuned entries gave up
        for (var k = 0; k < entries.length; k++) {
            var p = ws[k] / total
            qs.push(fs[k] * p)
            if (fs[k] === 1) kept += p
            else lost += (1 - fs[k]) * p
        }
        var emptyShare = 0
        if (slack === 'empty' || kept <= 0) {
            emptyShare = lost
        } else {
            for (var m = 0; m < entries.length; m++) {
                if (fs[m] === 1) qs[m] = qs[m] * (kept + lost) / kept
            }
        }

        var iw = integerWeights(emptyShare > 0 ? qs.concat([emptyShare]) : qs, total)
        var out = []
        var real = 0
        for (var n = 0; n < entries.length; n++) {
            var entry = entries[n]
            if (qs[n] <= 0) {
                stats.entriesRemoved++
                continue
            }
            if (typeof entry.quality === 'number' && entry.quality !== 0) {
                entry.quality = Math.round(entry.quality * iw[n] / ws[n])
            }
            entry.weight = iw[n]
            out.push(entry)
            if (entryType(entry) !== 'minecraft:empty') real++
        }
        if (emptyShare > 0) out.push({ type: 'minecraft:empty', weight: iw[iw.length - 1] })
        pool.entries = real ? out : [{ type: 'minecraft:empty', weight: 1 }]
        stats.poolsReweighted++
        return true
    }

    function rewriteTable(b, table) {
        var pools = JSON.parse(String(b.pools.toString()))
        if (!pools.length) throw new Error(MISSING)
        var before = { removed: T.stats.entriesRemoved, pools: T.stats.poolsReweighted + T.stats.fallbackPools }
        var changed = false
        for (var i = 0; i < pools.length; i++) {
            if (rewritePool(pools[i], T.settings.slack, T.stats)) changed = true
        }
        if (!changed) throw new Error(UNCHANGED)
        b.pools = JsonIO.parseRaw(JSON.stringify(pools))
        var type = T.index.tables[table]
        if (type) b.type = type
        T.stats.tables++
        if (T.settings.log_tables) {
            console.info(TAG + table + ': ' + (T.stats.entriesRemoved - before.removed) + ' entries removed, ' +
                (T.stats.poolsReweighted + T.stats.fallbackPools - before.pools) + ' pools rewritten')
        }
    }

    function preRoll(event) {
        if (!T.active) return
        var targets = {}
        var items = T.index.items
        for (var id in items) {
            if (ruleFor(id) === undefined) continue
            var edit = items[id].edit
            for (var i = 0; i < edit.length; i++) targets[edit[i]] = true
        }
        for (var table in targets) {
            try {
                event.modify(table, function (b) { rewriteTable(b, table) })
            } catch (err) {
                var msg = String(err)
                if (msg.indexOf(MISSING) >= 0) T.stats.missingTables.push(table)
                else if (msg.indexOf(UNCHANGED) < 0) warn(table + ': ' + msg)
            }
        }
        T.preRollRan = true
        console.info(TAG + T.stats.rules + ' rules -> rewrote ' + T.stats.tables + ' tables (' +
            T.stats.entriesRemoved + ' entries removed, ' + T.stats.poolsReweighted + ' pools reweighted, ' +
            T.stats.fallbackPools + ' via conditions)' +
            (T.stats.missingTables.length ? ', ' + T.stats.missingTables.length +
                ' indexed tables no longer exist - re-run tools/gen_loot_config.py' : ''))
    }

    // ------------------------------------------------------------------------------ post-roll

    // true when the pre-roll rewrite already governs this item for rolls of `table`
    function coveredPreRoll(id, table) {
        if (!T.preRollRan) return false
        var ix = T.index.items[id]
        if (!ix) return false
        if (ix.reach === '*') return true
        if (!ix.reachSet) {
            ix.reachSet = {}
            for (var i = 0; i < ix.reach.length; i++) ix.reachSet[ix.reach[i]] = true
        }
        return ix.reachSet[table] === true
    }

    function postRoll(ctx) {
        var tid = ctx.getLootTableId()
        var table = tid == null ? '' : String(tid)
        var rand = ctx.getRandom()
        ctx.removeLoot(ItemFilter.custom(function (stack) {
            var id = String(stack.id)
            var f = ruleFor(id)
            if (f === undefined) return false
            if (f <= 0) {
                T.stats.postRemoved++
                return true
            }
            if (coveredPreRoll(id, table) || rand.nextFloat() < f) return false
            T.stats.postThinned++
            return true
        }))
    }

    // ------------------------------------------------------------------------------ wiring

    T.stats = freshStats()
    T.config = readJson(CONFIG_PATH)
    T.index = readJson(INDEX_PATH)
    if (T.config && T.index) {
        var s = T.config.settings || {}
        if (s.enabled === false) T.settings.enabled = false
        if (s.slack === 'empty' || s.slack === 'redistribute') T.settings.slack = s.slack
        else if (s.slack !== undefined) warn('settings.slack must be "redistribute" or "empty", using "redistribute"')
        T.settings.log_tables = s.log_tables === true
        if (T.settings.enabled) loadRules()
        T.active = T.settings.enabled && T.stats.rules > 0
    } else {
        console.error(TAG + 'disabled: missing ' + CONFIG_PATH + ' or ' + INDEX_PATH +
            ' - run python3 tools/gen_loot_config.py')
    }
    global.lootTuning = T

    ServerEvents.genericLootTables(preRoll)

    LootJS.modifiers(event => {
        if (!T.active) return
        event.addLootTypeModifier(LootType.CHEST)
            .apply(ctx => postRoll(ctx))
            .logName('CE/loot/tuning')
    })

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('loottuning')
                .requires(src => src.hasPermission(2))
                .executes(ctx => {
                    var st = T.stats
                    var lines = [
                        'Loot tuning: ' + (T.active ? 'active' : 'inactive') + ', slack=' + T.settings.slack +
                            ', ' + st.rules + ' rules',
                        'pre-roll: ' + st.tables + ' tables rewritten, ' + st.entriesRemoved + ' entries removed, ' +
                            st.poolsReweighted + ' pools reweighted, ' + st.fallbackPools + ' via conditions',
                        'post-roll since reload: ' + st.postRemoved + ' stacks removed, ' + st.postThinned + ' thinned',
                    ]
                    if (st.missingTables.length) lines.push('stale index, missing tables: ' + st.missingTables.join(', '))
                    if (st.warnings.length) lines.push(st.warnings.length + ' warnings (logs/kubejs/server.log)')
                    for (var i = 0; i < lines.length; i++) ctx.source.sendSuccess(Text.of(lines[i]), false)
                    return 1
                })
        )
    })

})()
