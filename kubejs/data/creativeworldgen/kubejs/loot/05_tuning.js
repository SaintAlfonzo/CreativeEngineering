// priority: 100
// requires: creativeworldgen
// Chest loot — TUNING.  The pre-roll loot framework: every chest-loot edit the pack makes, applied to
// the loot table JSON before the game parses it wherever that is possible, and after the roll
// (LootJS) only where it isn't.
//
// Two ways to set rules, both applied by this file:
//   - loot_config.json (this folder): per-item remove / frequency knobs + "@mod" / "/regex/" / "#tag"
//     patterns (optional "tables" scope). Rebuild the catalog with `python3 tools/gen_loot_config.py`.
//   - script calls on global.lootTuning from 07_rules.js (API below). Priority 100 so the API exists
//     before any priority-0 script calls it.
// `/loottuning` prints what the last reload did.
//
// PRE-ROLL (ServerEvents.genericLootTables, raw JSON, exact odds). Per table, in this order:
//   1. alias()             a table containers name but nobody ships is created from a sibling
//   2. useVanilla()        a jar's override of a vanilla table is swapped back to vanilla's pools
//   3. addPool() / restoreInjection()  whole pools appended (restoreInjection: only where Forge
//                          skipped the mod's own injection because the vanilla table is overridden)
//   4. add()               an item entry added to a pool at a set share
//   5. pack() / companion()  packed food split out of its loose entry; companions beside an anchor
//   6. tuning              frequency / remove / ban: weights rewritten so each tuned entry's chance
//                          per roll is exactly f x the old one (f > 1 boosts, f = 0 drops the entry);
//                          the difference goes to the pool's other entries or an empty entry
//                          (settings.slack). Pools are never dropped.
// POST-ROLL (LootJS, every chest roll) — only what the JSON pass can't reach: items a global loot
//   modifier adds, and entries mods add in code to existing pools (the catalog marks those items
//   "post_roll_only"). Removed stacks are stripped (the slot stays empty), reduced ones thinned,
//   boosts can't apply. Anything the pre-roll pass already governs for the rolled table is skipped.
//
// Bans (ban()) are removals that must never be needed: the mod's own loot switch is off. They
// raise a LOUD warning (error log + red chat to ops) when the mod's switch reads on again, when a
// table outside the mod's own ships the item, or when the item turns up in a rolled chest.
//
// '#tag' targets: tags aren't bound while loot tables load, so 95_tag_capture.js reads each tag's
// members from the tag event of the same reload (it runs earlier, in the reload's prepare phase).
//
// Every reload writes kubejs/exported/loot_runtime_additions.json (pools and items these rules added)
// so the generator doesn't mistake them for mod injections in the next /dumploot.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    // Both files sit next to this script (data/creativeworldgen/kubejs/loot/). JsonIO only reads files, so the pack folder
    // copy is read while it exists (dev) and the jar copy otherwise (CWGResources, as Creative Delight's wild crops do).
    var DIR = 'data/creativeworldgen/kubejs/loot/'
    var CONFIG_PATH = DIR + 'loot_config.json'
    var INDEX_PATH = DIR + 'loot_index.json'
    var ADDITIONS_PATH = 'kubejs/exported/loot_runtime_additions.json'
    var MISSING = 'loot-tuning:missing-table'
    var UNCHANGED = 'loot-tuning:unchanged'
    var TAG = '[loot-tuning] '
    var BAR = '!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!'
    var MAX_PACK_RATE = 0.9  // packed food never takes the whole loose entry

    var T = {
        config: null,
        index: null,
        settings: { enabled: true, slack: 'redistribute', log_tables: false },
        explicit: {},    // item id -> factor (0 = remove), from config.items
        patterns: [],    // rules from config.patterns (file order), then script calls (call order)
        bans: [],        // ban rules, checked before everything else
        scoped: false,   // any rule with a `tables` scope: rule lookups then depend on the table
        cache: {},       // item id (| table when scoped) -> factor | null
        banCache: {},    // item id -> ban rule | null
        tagsWanted: {},  // 'ns:tag' -> true, for 95_tag_capture.js
        tagMembers: {},  // 'ns:tag' -> { item id: true }
        tagsCaptured: false,
        packs: [],       // pack(): { packed, unpacked, rate (null = default), max (null = from count) }
        packByUnpacked: {},
        packDefault: 0.1,
        packBoosts: [],  // { key, test(table), mult }
        packExcludes: [],
        companions: [],  // companion(): { item, anchors[], rate, count, nbt, tables[] | null }
        adds: [],        // add(): { tables[], item, share, weight, count, pool, nbt, conditions }
        poolAdds: [],    // addPool() / restoreInjection(): { tables[], pool, restore, label }
        vanillaReverts: [], // useVanilla(): { tables[], winner matcher | null }
        aliases: {},     // alias(): missing table -> { source } | { pools }
        ops: {},         // table -> planned work, built at pre-roll
        additions: {},   // table -> { pools, items[] } this reload added (for the generator)
        curTable: '',
        alarmSeen: {},
        preRollRan: false,
        stats: null,
    }

    function freshStats() {
        return {
            rules: 0, tables: 0, entriesRemoved: 0, poolsReweighted: 0, fallbackPools: 0, boosted: 0,
            boostCapped: 0, boostSkipped: 0, missingTables: [], postRemoved: 0, postThinned: 0, warnings: [],
            banRemovedPre: 0, banRemovedPost: 0, alarms: [], alarmRepeats: 0,
            packSplit: 0, packEntries: 0, packPresent: 0, packFallback: 0,
            companions: 0, aliased: 0, reverted: 0, poolsAdded: 0, restored: 0, itemsAdded: 0,
            postOnly: [],
        }
    }

    function warn(msg) {
        T.stats.warnings.push(msg)
        console.warn(TAG + msg)
    }

    // LOUD: error-log banner + red chat to online ops, once per key per reload. Ops who join later
    // get the list too (PlayerEvents.loggedIn below).
    function alarm(key, lines) {
        if (T.alarmSeen[key]) {
            T.stats.alarmRepeats++
            return
        }
        T.alarmSeen[key] = true
        T.stats.alarms.push(lines[0])
        console.error(TAG + BAR)
        for (var i = 0; i < lines.length; i++) console.error(TAG + '!!! ' + lines[i])
        console.error(TAG + BAR)
        tellOps('[loot] ' + lines[0])
    }

    function tellOps(msg) {
        var server = null
        try { server = Utils.getServer() } catch (err) {}
        if (!server) return
        var players = server.getPlayerList().getPlayers()
        for (var i = 0; i < players.size(); i++) {
            var p = players.get(i)
            if (p.hasPermissions(2)) p.tell(Text.of(msg).red())
        }
    }

    function normId(s) {
        s = String(s)
        return s.indexOf(':') < 0 ? 'minecraft:' + s : s
    }

    function arr(v) {
        return v == null ? [] : (Array.isArray(v) ? v : [v])
    }

    function copy(v) {
        return JSON.parse(JSON.stringify(v))
    }

    function readJson(path) {
        try {
            var text = String(JsonIO.readString('kubejs/' + path))
            if (text === 'null') {
                var jar = Java.loadClass('com.SaintAlfonzo.creativeworldgen.CWGResources').text(path)
                text = jar == null ? 'null' : String(jar)
            }
            var data = JSON.parse(text)
            if (data === null) throw new Error('no such file in the pack folder or the creativeworldgen jar')
            return data
        } catch (err) {
            console.error(TAG + 'could not read ' + path + ': ' + err)
            return null
        }
    }

    // Java Errors (e.g. NoClassDefFoundError when a mod is gone) skip JS catch; guardThrowable
    // (the pack's _lib/java_guard.js) catches them. This script pack has its own scope, so it is read
    // from global (published by the pack folder's server scripts, which load before the jar packs).
    function callGuarded(fn) {
        if (typeof global.guardThrowable === 'function') return global.guardThrowable(fn)
        try {
            return { ok: true, value: fn(), error: null }
        } catch (err) {
            return { ok: false, value: undefined, error: err }
        }
    }

    // ------------------------------------------------------------------------------ matchers

    // A target -> { key, test(id) } or null. Targets: 'ns:id' | '@modid' | '/regex/flags' | '#ns:tag'
    // | a JS RegExp. Also used for table filters.
    function matcher(target, where) {
        if (target instanceof RegExp) {
            var re0 = target
            return { key: String(re0), test: function (id) { return re0.test(id) } }
        }
        if (target == null) {
            warn(where + ': null/undefined target (missing global.item key?)')
            return null
        }
        var s = String(target)
        var c = s.charAt(0)
        if (c === '@') {
            var prefix = s.substring(1) + ':'
            return { key: s, test: function (id) { return id.indexOf(prefix) === 0 } }
        }
        if (c === '/' && s.lastIndexOf('/') > 0) {
            var cut = s.lastIndexOf('/')
            var re = new RegExp(s.substring(1, cut), s.substring(cut + 1))
            return { key: s, test: function (id) { return re.test(id) } }
        }
        if (c === '#') {
            var tag = normId(s.substring(1))
            T.tagsWanted[tag] = true
            return {
                key: s,
                tag: tag,
                test: function (id) {
                    var m = T.tagMembers[tag]
                    return m !== undefined && m[id] === true
                },
            }
        }
        var exact = normId(s)
        return { key: exact, exact: exact, test: function (id) { return id === exact } }
    }

    function matchers(targets, where) {
        var out = []
        var list = arr(targets)
        for (var i = 0; i < list.length; i++) {
            var m = matcher(list[i], where)
            if (m) out.push(m)
        }
        return out
    }

    function anyMatch(list, id) {
        for (var i = 0; i < list.length; i++) if (list[i].test(id)) return true
        return false
    }

    // Table filters -> table ids. Exact ids are taken as given (warned when the index doesn't know
    // them); patterns are matched against the indexed chest tables.
    function resolveTables(filters, where) {
        var out = {}
        for (var i = 0; i < filters.length; i++) {
            var f = filters[i]
            if (f.exact) {
                if (!T.index.tables[f.exact] && !T.aliases[f.exact]) warn(where + ': ' + f.exact + ' is not an indexed chest table')
                out[f.exact] = true
                continue
            }
            for (var t in T.index.tables) if (f.test(t)) out[t] = true
        }
        return Object.keys(out)
    }

    // ------------------------------------------------------------------------------ rules

    // A factor spec -> factor >= 0 (0 remove, < 1 cut, > 1 boost), or undefined for "no rule".
    function factorOf(spec, where) {
        if (!spec || typeof spec !== 'object') return undefined
        if (spec.remove === true) return 0
        if (spec.frequency === undefined || spec.frequency === 1) return undefined
        var f = spec.frequency
        if (typeof f !== 'number' || isNaN(f) || f < 0) {
            warn(where + ': frequency must be a number >= 0, ignoring ' + JSON.stringify(f))
            return undefined
        }
        if (f > 100) {
            warn(where + ': frequency ' + f + ' capped at 100')
            f = 100
        }
        return f
    }

    // tables: optional scope ('ns:table' | '@mod' | '/regex/' | RegExp | list), matched against the table
    // that holds the entry (pre-roll) or the table that was rolled (post-roll)
    function makeRule(target, factor, except, where, tables) {
        var m = matcher(target, where)
        if (!m) return null
        var scope = tables == null ? null : matchers(tables, where + ' tables')
        if (scope) T.scoped = true
        return { key: m.key, test: m.test, factor: factor, except: matchers(except, where + ' except'), tables: scope }
    }

    function ruleHits(r, id, table) {
        return r.test(id) && !anyMatch(r.except, id) && (!r.tables || anyMatch(r.tables, table))
    }

    function loadRules() {
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
            var spec = pats[key]
            var pf = factorOf(spec, 'pattern ' + key)
            if (pf === undefined) continue
            var r = makeRule(key, pf, spec.except, 'pattern ' + key, spec.tables)
            if (r) {
                T.patterns.push(r)
                T.stats.rules++
            }
        }
    }

    function resetCaches() {
        T.cache = {}
        T.banCache = {}
    }

    function banFor(id) {
        var hit = T.banCache[id]
        if (hit !== undefined) return hit
        var out = null
        for (var i = 0; i < T.bans.length; i++) {
            if (ruleHits(T.bans[i], id, '')) {
                out = T.bans[i]
                break
            }
        }
        T.banCache[id] = out
        return out
    }

    // factor for an item id in a table, or undefined when it isn't tuned there. Bans beat everything;
    // an item's own config entry beats patterns; patterns go config file order, then script call order.
    function ruleFor(id, table) {
        var key = T.scoped ? id + '|' + table : id
        var hit = T.cache[key]
        if (hit !== undefined) return hit === null ? undefined : hit
        var f = banFor(id) ? 0 : T.explicit[id]
        if (f === undefined) {
            for (var i = 0; i < T.patterns.length; i++) {
                if (ruleHits(T.patterns[i], id, table)) {
                    f = T.patterns[i].factor
                    break
                }
            }
        }
        T.cache[key] = f === undefined ? null : f
        return f
    }

    // Pre-roll found a banned item's entry: expected in the mod's own tables, LOUD anywhere else.
    function banPreRoll(id) {
        var ban = banFor(id)
        if (!ban) return
        T.stats.banRemovedPre++
        if (anyMatch(ban.ownTables, T.curTable)) return
        alarm('pre|' + id + '|' + T.curTable, [
            'BANNED item ' + id + ' found in loot table ' + T.curTable + ' (pre-roll pass removed it)',
            'ban ' + ban.name + ': ' + ban.why,
            'Something other than the banned mod\'s own tables ships it. Check what added this table.',
        ])
    }

    function checkBanSwitches() {
        for (var i = 0; i < T.bans.length; i++) {
            var ban = T.bans[i]
            if (!ban.modLootOn) continue
            var res = callGuarded(ban.modLootOn)
            if (!res.ok) {
                warn('ban ' + ban.name + ': could not read the mod\'s loot switch: ' + res.error)
            } else if (String(res.value) === 'true') {
                alarm('switch|' + ban.key, [
                    'BANNED loot is switched ON again: ' + ban.name,
                    ban.why,
                    'The mod config was reset or edited. Turn it back off (see configs_edited/root_config).',
                ])
            }
        }
    }

    // ------------------------------------------------------------------------------ script API

    function addScriptRule(targets, factor, opts, where) {
        opts = opts || {}
        var list = arr(targets)
        for (var i = 0; i < list.length; i++) {
            var r = makeRule(list[i], factor, opts.except, where, opts.tables)
            if (!r) continue
            T.patterns.push(r)
            T.stats.rules++
        }
        resetCaches()
    }

    // L.frequency('#creativeworldgen:loot/chalk', 0.5)        half as often, every chest
    // L.frequency(['a:b', '@mod'], 0.25, { except: ['mod:keep'] })
    // L.frequency('minecraft:golden_apple', 2)                    boost: twice the chance per roll
    // L.frequency('minecraft:emerald', 2, { tables: /village/ })  only in matching tables
    //   A boost takes its extra share from the pool's other entries (their ratios kept). Boosts are
    //   pre-roll only: an item only a loot modifier adds can't be boosted.
    T.frequency = function (targets, f, opts) {
        var factor = factorOf({ frequency: f }, 'frequency(' + String(targets) + ')')
        if (factor === undefined) return
        addScriptRule(targets, factor, opts, 'frequency')
    }

    // L.remove(target, { except, tables })   never in any chest (or not in those tables)
    T.remove = function (targets, opts) {
        addScriptRule(targets, 0, opts, 'remove')
    }

    // L.ban(target, { name, why, modLootOn, ownTables, except })  removed everywhere + LOUD if it shows up
    //   name       label for the warnings (default: the target)
    //   why        text for the warning: which switch is supposed to keep it out
    //   modLootOn  function returning true when the mod's own loot switch is on (bad), checked every reload
    //   ownTables  tables expected to ship the item (the mod's own); default for '@mod' is that mod
    T.ban = function (targets, opts) {
        opts = opts || {}
        var list = arr(targets)
        for (var i = 0; i < list.length; i++) {
            var r = makeRule(list[i], 0, opts.except, 'ban', null)
            if (!r) continue
            var own = opts.ownTables
            if (own === undefined && String(list[i]).charAt(0) === '@') own = '@' + String(list[i]).substring(1)
            r.name = opts.name || r.key
            r.why = opts.why || '(no reason given)'
            r.modLootOn = opts.modLootOn || null
            r.ownTables = matchers(own, 'ban ownTables')
            T.bans.push(r)
            T.stats.rules++
        }
        resetCaches()
    }

    // L.pack(packedId, looseId, { rate, max })
    //   Splits each pool entry of looseId: its chance per roll stays the same, but the top `rate` of
    //   it becomes packed items. Think of the entry's roll as a number from 0 to 1: below 1 - rate you
    //   get the loose item as before; above it crates, and the higher the roll the more crates
    //   (1 up to `max`, each step half as likely as the one before). The pool's other entries don't
    //   change. max defaults from the loose entry's own count (a 1-7 potato stack -> up to 2 crates).
    T.pack = function (packed, unpacked, opts) {
        opts = opts || {}
        if (packed == null || unpacked == null) {
            warn('pack(' + packed + ', ' + unpacked + '): missing id (global.item key typo?)')
            return
        }
        var p = normId(packed)
        var u = normId(unpacked)
        if (!Item.exists(p)) {
            warn('pack: ' + p + ' is not a registered item, skipped')
            return
        }
        var rate = opts.rate === undefined ? null : opts.rate
        if (rate !== null && (typeof rate !== 'number' || !(rate >= 0))) {
            warn('pack ' + p + ': rate must be a number >= 0, using the default')
            rate = null
        }
        var pair = { packed: p, unpacked: u, rate: rate, max: opts.max || null }
        T.packs.push(pair)
        var list = T.packByUnpacked[u] = T.packByUnpacked[u] || []
        list.push(pair)
    }

    // L.packRate(0.1)  default share of a loose entry that becomes packed (capped at 0.9 after boosts)
    T.packRate = function (rate) {
        if (typeof rate !== 'number' || !(rate >= 0)) {
            warn('packRate: expected a number >= 0, got ' + rate)
            return
        }
        T.packDefault = rate
    }

    // L.packBoost(/village/, 3)  multiply the rate in matching tables (every matching boost multiplies)
    T.packBoost = function (tables, mult) {
        if (typeof mult !== 'number' || !(mult >= 0)) {
            warn('packBoost: multiplier must be a number >= 0, got ' + mult)
            return
        }
        var ms = matchers(tables, 'packBoost')
        for (var i = 0; i < ms.length; i++) T.packBoosts.push({ key: ms[i].key, test: ms[i].test, mult: mult })
    }

    // L.packExclude(/^wares:agreement\//)  never split in these tables
    T.packExclude = function (tables) {
        var ms = matchers(tables, 'packExclude')
        for (var i = 0; i < ms.length; i++) T.packExcludes.push(ms[i])
    }

    // L.companion(item, anchors, { rate, count, nbt, tables })
    //   Wherever an anchor item is a pool entry, `item` is added to the same pool with a chance per roll
    //   of rate x the anchor's (the rest of the pool gets that much rarer). nbt: SNBT string for set_nbt.
    T.companion = function (item, anchors, opts) {
        opts = opts || {}
        if (item == null || !Item.exists(normId(item))) {
            warn('companion: ' + item + ' is not a registered item, skipped')
            return
        }
        if (!(opts.rate > 0)) {
            warn('companion ' + item + ': rate must be > 0')
            return
        }
        T.companions.push({
            item: normId(item), anchors: matchers(anchors, 'companion anchors'), rate: opts.rate,
            count: opts.count || 1, nbt: opts.nbt || null,
            tables: opts.tables == null ? null : matchers(opts.tables, 'companion tables'),
        })
    }

    // L.add(tables, item, { share | weight, count, pool, nbt, conditions, except })
    //   Adds `item` to pool `pool` of each matching table (except: tables to skip). pool defaults to
    //   the table's main pool: the one with the most item entries (so never a guaranteed single-item
    //   pool like an outpost's crossbow). share: its chance per roll in
    //   that pool afterwards (0..1, the other entries shrink to make room); weight: a raw weight next to
    //   the pool's own. count: number or [min, max]. Skipped where the pool already holds the item
    //   (use frequency() for that).
    T.add = function (tables, item, opts) {
        opts = opts || {}
        if (item == null || !Item.exists(normId(item))) {
            warn('add: ' + item + ' is not a registered item, skipped')
            return
        }
        var share = opts.share
        if (share !== undefined && !(share > 0 && share < 1)) {
            warn('add ' + item + ': share must be between 0 and 1')
            return
        }
        if (share === undefined && !(opts.weight >= 1)) {
            warn('add ' + item + ': give share (0..1) or weight (>= 1)')
            return
        }
        T.adds.push({
            tables: matchers(tables, 'add tables'), except: matchers(opts.except, 'add except'),
            item: normId(item), share: share, weight: opts.weight,
            count: opts.count || 1, pool: opts.pool === undefined ? null : opts.pool, nbt: opts.nbt || null,
            conditions: opts.conditions || null,
        })
    }

    // L.addPool(tables, poolJson, { name })  append a whole pool (raw loot-table JSON) to each table
    T.addPool = function (tables, pool, opts) {
        opts = opts || {}
        T.poolAdds.push({ tables: matchers(tables, 'addPool tables'), pool: pool, restore: false, label: opts.name || 'addPool' })
    }

    // L.restoreInjection(tables, poolJson, { mod })
    //   The pool a mod injects in code (LootTableLoadEvent) into a vanilla table. Forge skips that
    //   event when a jar or datapack overrides the vanilla table, so the pool is re-added exactly
    //   there (index "overridden"), and nowhere Forge still injects it itself.
    T.restoreInjection = function (tables, pool, opts) {
        opts = opts || {}
        T.poolAdds.push({ tables: matchers(tables, 'restoreInjection tables'), pool: pool, restore: true, label: opts.mod || 'restore' })
    }

    // L.useVanilla(tables, { winner })  swap a jar's override of a vanilla table back to vanilla's pools
    //   (from the index). winner: only when the overriding source matches ('goldenfood', a regex...).
    T.useVanilla = function (tables, opts) {
        opts = opts || {}
        T.vanillaReverts.push({
            tables: matchers(tables, 'useVanilla tables'),
            winner: opts.winner == null ? null : (opts.winner instanceof RegExp ? opts.winner : new RegExp(String(opts.winner))),
        })
    }

    // L.alias(missingTable, sourceTable | [poolJson...])
    //   Creates a table that structure containers name but no mod ships (they'd always be empty), as
    //   one roll of a sibling table, or from the given pools. Never touches a table that exists.
    T.alias = function (missing, source) {
        var id = normId(missing)
        if (Array.isArray(source)) T.aliases[id] = { pools: source }
        else if (source != null) T.aliases[id] = { source: normId(source) }
        else warn('alias ' + id + ': no source')
    }

    // Called by 95_tag_capture.js from ServerEvents.tags('item'). Reads in the pre pass too: that
    // marks it invalid, so KubeJS re-runs the handler on the real tag load (see tag_helper.js).
    T.captureTags = function (event) {
        var pre = global.tags._isPreTag(event)
        var got = false
        for (var tag in T.tagsWanted) {
            var ids = global.tags._tagMembers(event, tag)
            if (pre) continue
            var set = {}
            for (var i = 0; i < ids.length; i++) set[ids[i]] = true
            T.tagMembers[tag] = set
            got = true
        }
        if (got) {
            T.tagsCaptured = true
            resetCaches()
        }
    }

    // Fallback when the capture didn't run: the registry's bound tags (last reload's; empty on the
    // very first load). Post-roll this is always current.
    function ensureTags() {
        if (T.tagsCaptured) return
        var any = false
        for (var tag in T.tagsWanted) {
            any = true
            var set = {}
            var ids = Ingredient.of('#' + tag).itemIds
            for (var i = 0; i < ids.length; i++) set[String(ids[i])] = true
            T.tagMembers[tag] = set
        }
        if (any) {
            warn('tag rules resolved from the registry, not this reload\'s tag event (95_tag_capture.js did not run?)')
            T.tagsCaptured = true
            resetCaches()
        }
    }

    // ------------------------------------------------------------------------------ planning

    function opFor(table) {
        var op = T.ops[table]
        if (!op) {
            op = T.ops[table] = { alias: null, vanilla: null, pools: [], adds: [], pack: false, companions: false }
        }
        return op
    }

    // The index tells post-roll which tables' rolls pre-roll already covers for an item. Items the
    // rules put into a table get that table (and the tables pulling it in) added.
    function extendIndex(item, tables, reachFrom) {
        var ix = T.index.items[item] = T.index.items[item] || { edit: [], reach: [] }
        ix.edit = ix.edit.concat(tables)
        if (ix.reach !== '*') {
            if (reachFrom === '*') ix.reach = '*'
            else ix.reach = ix.reach.concat(tables, reachFrom || [])
        }
        delete ix.reachSet
    }

    // `parent` now rolls `child` (alias, added loot_table pool): everything reachable through child
    // is reachable through parent too.
    function link(parent, child) {
        for (var id in T.index.items) {
            var ix = T.index.items[id]
            if (ix.reach === '*' || ix.reach.indexOf(child) < 0 || ix.reach.indexOf(parent) >= 0) continue
            ix.reach.push(parent)
            delete ix.reachSet
        }
    }

    function linkPools(table, pools) {
        for (var i = 0; i < pools.length; i++) {
            var entries = pools[i].entries || []
            for (var j = 0; j < entries.length; j++) {
                var e = entries[j]
                if (entryType(e) === 'minecraft:loot_table' && e.name) link(table, normId(e.name))
                if (entryType(e) === 'minecraft:item' && e.name) extendIndex(normId(e.name), [table])
            }
        }
    }

    function plan() {
        T.ops = {}
        var t, i, j, tables
        for (t in T.aliases) {
            var al = T.aliases[t]
            if (T.index.tables[t]) {
                warn('alias ' + t + ': the table exists, left alone')
                continue
            }
            var pools = al.pools ? copy(al.pools) : [{ rolls: 1, entries: [{ type: 'minecraft:loot_table', name: al.source }] }]
            opFor(t).alias = pools
            linkPools(t, pools)
        }
        for (i = 0; i < T.vanillaReverts.length; i++) {
            var rv = T.vanillaReverts[i]
            tables = resolveTables(rv.tables, 'useVanilla')
            for (j = 0; j < tables.length; j++) {
                var ov = (T.index.overridden || {})[tables[j]]
                if (!ov || !ov.vanilla_table) continue
                if (rv.winner && !rv.winner.test(ov.winner)) continue
                opFor(tables[j]).vanilla = ov.vanilla_table
            }
        }
        for (i = 0; i < T.poolAdds.length; i++) {
            var pa = T.poolAdds[i]
            tables = resolveTables(pa.tables, pa.label)
            for (j = 0; j < tables.length; j++) {
                if (pa.restore) {
                    var o = (T.index.overridden || {})[tables[j]]
                    if (!o || !o.vanilla) continue
                }
                opFor(tables[j]).pools.push(pa)
                linkPools(tables[j], [pa.pool])
            }
        }
        for (i = 0; i < T.adds.length; i++) {
            var ad = T.adds[i]
            tables = resolveTables(ad.tables, 'add ' + ad.item).filter(function (x) { return !anyMatch(ad.except, x) })
            for (j = 0; j < tables.length; j++) opFor(tables[j]).adds.push(ad)
            extendIndex(ad.item, tables)
        }
        for (i = 0; i < T.packs.length; i++) {
            var pair = T.packs[i]
            var ux = T.index.items[pair.unpacked]
            if (!ux) continue
            var got = []
            for (j = 0; j < ux.edit.length; j++) {
                if (anyMatch(T.packExcludes, ux.edit[j])) continue
                opFor(ux.edit[j]).pack = true
                got.push(ux.edit[j])
            }
            if (got.length) extendIndex(pair.packed, got, ux.reach)
        }
        for (i = 0; i < T.companions.length; i++) {
            var cp = T.companions[i]
            for (var id in T.index.items) {
                if (!anyMatch(cp.anchors, id)) continue
                var ax = T.index.items[id]
                var hit = []
                for (j = 0; j < ax.edit.length; j++) {
                    if (cp.tables && !anyMatch(cp.tables, ax.edit[j])) continue
                    opFor(ax.edit[j]).companions = true
                    hit.push(ax.edit[j])
                }
                if (hit.length) extendIndex(cp.item, hit, ax.reach)
            }
        }
    }

    // ------------------------------------------------------------------------------ pool helpers

    function entryType(e) {
        return normId(e.type || 'minecraft:item')
    }

    function weightOf(e) {
        return typeof e.weight === 'number' ? e.weight : 1
    }

    function itemsIn(entries) {
        var out = {}
        for (var i = 0; i < entries.length; i++) {
            if (entryType(entries[i]) === 'minecraft:item' && entries[i].name != null) out[normId(entries[i].name)] = true
        }
        return out
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

    // Composites (alternatives/group/sequence) and expand:true tags don't map 1:1 onto weighted
    // candidates, so those pools can't be reweighted exactly.
    function needsFallback(entries) {
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i]
            if (e.children || (entryType(e) === 'minecraft:tag' && e.expand)) return true
        }
        return false
    }

    // Rebuild a pool from target shares. parts: [{ entry, q (share of the old total), old (old weight
    // or null for new entries) }]. Quality scales with the weight.
    function setShares(pool, parts) {
        var total = 0
        for (var i = 0; i < parts.length; i++) if (parts[i].old !== null) total += parts[i].old
        if (total <= 0) total = 1
        var qs = []
        for (var i = 0; i < parts.length; i++) qs.push(parts[i].q)
        var iw = integerWeights(qs, total)
        var out = []
        for (var i = 0; i < parts.length; i++) {
            if (!(parts[i].q > 0) && parts[i].old === null) continue  // (existing weight-0 entries stay: Luck)
            var e = parts[i].entry
            var ref = parts[i].old !== null ? parts[i].old : parts[i].qualityRef
            if (typeof e.quality === 'number' && e.quality !== 0 && ref) e.quality = Math.round(e.quality * iw[i] / ref)
            e.weight = iw[i]
            out.push(e)
        }
        pool.entries = out
    }

    function countFn(count) {
        if (Array.isArray(count)) return { function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: count[0], max: count[1] } }
        return count > 1 ? { function: 'minecraft:set_count', count: count } : null
    }

    function itemEntry(item, count, nbt, conditions) {
        var e = { type: 'minecraft:item', name: item }
        var fns = []
        var c = countFn(count)
        if (c) fns.push(c)
        if (nbt) fns.push({ function: 'minecraft:set_nbt', tag: nbt })
        if (fns.length) e.functions = fns
        if (conditions) e.conditions = copy(conditions)
        return e
    }

    // index of the pool with the most item entries (ties: the first)
    function mainPool(pools) {
        var best = 0
        var most = -1
        for (var i = 0; i < pools.length; i++) {
            var n = 0
            var entries = pools[i].entries || []
            for (var j = 0; j < entries.length; j++) if (entryType(entries[j]) === 'minecraft:item') n++
            if (n > most) {
                most = n
                best = i
            }
        }
        return best
    }

    // largest stack an entry's set_count can give (1 without one)
    function maxCount(e) {
        var fns = e.functions || []
        for (var i = 0; i < fns.length; i++) {
            if (normId(fns[i].function) !== 'minecraft:set_count') continue
            var c = fns[i].count
            if (typeof c === 'number') return c
            if (c && typeof c.max === 'number') return c.max
            if (c && typeof c.n === 'number') return c.n
            if (c && typeof c.value === 'number') return c.value
        }
        return 1
    }

    function noteAdded(table, item) {
        var a = T.additions[table] = T.additions[table] || { pools: 0, items: [] }
        if (item && a.items.indexOf(item) < 0) a.items.push(item)
    }

    // ------------------------------------------------------------------------------ pre-roll steps

    function addPools(pools, op, table) {
        var names = {}
        for (var i = 0; i < pools.length; i++) if (pools[i].name) names[pools[i].name] = true
        var added = 0
        for (var i = 0; i < op.pools.length; i++) {
            var pa = op.pools[i]
            if (pa.pool.name && names[pa.pool.name]) continue
            pools.push(copy(pa.pool))
            added++
            if (pa.restore) T.stats.restored++
            else T.stats.poolsAdded++
        }
        if (added) {
            noteAdded(table, null)
            T.additions[table].pools += added
        }
        return added > 0
    }

    function addItems(pools, op, table) {
        var changed = false
        for (var i = 0; i < op.adds.length; i++) {
            var ad = op.adds[i]
            var pool = pools[ad.pool === null ? mainPool(pools) : ad.pool]
            if (!pool) {
                warn('add ' + ad.item + ': ' + table + ' has no pool ' + ad.pool)
                continue
            }
            var entries = pool.entries || []
            // an NBT variant (a specific book) is its own thing; a plain item already in the pool isn't re-added
            if ((!ad.nbt && itemsIn(entries)[ad.item]) || needsFallback(entries)) continue
            if (ruleFor(ad.item, table) === 0) continue  // removed by a rule: don't add it just to drop it
            var ne = itemEntry(ad.item, ad.count, ad.nbt, ad.conditions)
            if (ad.share === undefined) {
                ne.weight = Math.round(ad.weight)
                entries.push(ne)
                pool.entries = entries
            } else {
                var parts = []
                var total = 0
                for (var j = 0; j < entries.length; j++) total += weightOf(entries[j])
                for (var j = 0; j < entries.length; j++) parts.push({ entry: entries[j], q: (1 - ad.share) * weightOf(entries[j]) / total, old: weightOf(entries[j]) })
                parts.push({ entry: ne, q: ad.share, old: null })
                setShares(pool, parts)
            }
            T.stats.itemsAdded++
            noteAdded(table, ad.item)
            changed = true
        }
        return changed
    }

    function packRateFor(pair, table) {
        var r = pair.rate === null ? T.packDefault : pair.rate
        for (var i = 0; i < T.packBoosts.length; i++) {
            if (T.packBoosts[i].test(table)) r *= T.packBoosts[i].mult
        }
        return Math.min(r, MAX_PACK_RATE)
    }

    // Split each loose entry: (1 - r) of its weight stays loose, r becomes crate bands 1..max whose
    // shares halve per step (max 2: 2/3 one crate, 1/3 two). Other entries keep their exact odds.
    function packPool(pool, table) {
        var entries = pool.entries
        if (!entries || !entries.length) return false
        var present = itemsIn(entries)
        var plans = []
        for (var k = 0; k < entries.length; k++) {
            var e = entries[k]
            if (entryType(e) !== 'minecraft:item' || e.name == null) continue
            var pairs = T.packByUnpacked[normId(e.name)]
            if (!pairs) continue
            var use = []
            for (var m = 0; m < pairs.length; m++) {
                if (present[pairs[m].packed]) {
                    T.stats.packPresent++
                    continue
                }
                if (ruleFor(pairs[m].packed, table) === 0) continue  // packed item removed: no split at all
                var rate = packRateFor(pairs[m], table)
                if (rate > 0) use.push({ pair: pairs[m], rate: rate })
            }
            if (use.length) plans.push({ index: k, use: use })
        }
        if (!plans.length) return false
        if (needsFallback(entries)) {
            T.stats.packFallback += plans.length
            return false
        }

        var total = 0
        for (var k = 0; k < entries.length; k++) total += weightOf(entries[k])
        var byIndex = {}
        for (var k = 0; k < plans.length; k++) byIndex[plans[k].index] = plans[k]
        var parts = []
        for (var k = 0; k < entries.length; k++) {
            var ent = entries[k]
            var w = weightOf(ent)
            var p = w / total
            var pl = byIndex[k]
            if (!pl) {
                parts.push({ entry: ent, q: p, old: w })
                continue
            }
            // several packed forms of one loose item share its split evenly
            var r = 0
            for (var m = 0; m < pl.use.length; m++) r += pl.use[m].rate
            r = Math.min(r / pl.use.length, MAX_PACK_RATE)
            parts.push({ entry: ent, q: p * (1 - r), old: w })
            for (var m = 0; m < pl.use.length; m++) {
                var pair = pl.use[m].pair
                var max = pair.max || Math.max(1, Math.min(3, Math.round(maxCount(ent) / 4)))
                var norm = 0
                for (var b = 1; b <= max; b++) norm += Math.pow(0.5, b - 1)
                for (var b = 1; b <= max; b++) {
                    parts.push({
                        entry: itemEntry(pair.packed, b, null, ent.conditions),
                        q: p * r / pl.use.length * Math.pow(0.5, b - 1) / norm,
                        old: null,
                        qualityRef: null,
                    })
                    T.stats.packEntries++
                }
                noteAdded(table, pair.packed)
            }
            T.stats.packSplit++
        }
        setShares(pool, parts)
        return true
    }

    // companion(): the item beside each anchor entry at rate x the anchor's chance (additive). Pools
    // made only of anchors are skipped (a guaranteed item stays guaranteed).
    function companionPool(pool, table) {
        var entries = pool.entries
        if (!entries || !entries.length || needsFallback(entries)) return false
        var present = itemsIn(entries)
        var extra = []
        for (var i = 0; i < T.companions.length; i++) {
            var cp = T.companions[i]
            if ((!cp.nbt && present[cp.item]) || (cp.tables && !anyMatch(cp.tables, table))) continue
            if (ruleFor(cp.item, table) === 0) continue
            var anchorW = 0
            for (var k = 0; k < entries.length; k++) {
                var e = entries[k]
                if (entryType(e) === 'minecraft:item' && e.name != null && anyMatch(cp.anchors, normId(e.name))) anchorW += weightOf(e)
            }
            var poolW = 0
            for (var k = 0; k < entries.length; k++) poolW += weightOf(entries[k])
            // no anchor, or the anchor IS the pool (a guaranteed golden apple): adding would dilute the guarantee
            if (anchorW <= 0 || anchorW >= poolW) continue
            extra.push({ cp: cp, w: cp.rate * anchorW })
            present[cp.item] = true
        }
        if (!extra.length) return false
        var total = 0
        for (var k = 0; k < entries.length; k++) total += weightOf(entries[k])
        var parts = []
        for (var k = 0; k < entries.length; k++) parts.push({ entry: entries[k], q: weightOf(entries[k]) / total, old: weightOf(entries[k]) })
        for (var i = 0; i < extra.length; i++) {
            parts.push({ entry: itemEntry(extra[i].cp.item, extra[i].cp.count, extra[i].cp.nbt, null), q: extra[i].w / total, old: null })
            T.stats.companions++
            noteAdded(table, extra[i].cp.item)
        }
        setShares(pool, parts)
        return true
    }

    // Composite children carry no pool weight of their own: a removed child is dropped, a reduced one
    // gets a random_chance condition; a boost can't be expressed there. Returns false when emptied.
    function tuneChildren(entry, touched) {
        var kept = []
        for (var i = 0; i < entry.children.length; i++) {
            var c = entry.children[i]
            if (c.children) {
                if (!tuneChildren(c, touched)) continue
            } else if (entryType(c) === 'minecraft:item' && c.name != null) {
                var f = ruleFor(normId(c.name), T.curTable)
                if (f === 0) {
                    banPreRoll(normId(c.name))
                    touched.removed++
                    touched.any = true
                    continue
                }
                if (f !== undefined && f < 1) {
                    c.conditions = (c.conditions || []).concat([{ condition: 'minecraft:random_chance', chance: f }])
                    touched.any = true
                } else if (f > 1) {
                    T.stats.boostSkipped++
                }
            }
            kept.push(c)
        }
        entry.children = kept
        return kept.length > 0
    }

    function fallbackPool(pool) {
        var touched = { any: false, removed: 0 }
        var out = []
        for (var i = 0; i < pool.entries.length; i++) {
            var e = pool.entries[i]
            if (e.children) {
                if (!tuneChildren(e, touched)) continue
            } else if (entryType(e) === 'minecraft:item' && e.name != null) {
                var f = ruleFor(normId(e.name), T.curTable)
                if (f === 0) {
                    banPreRoll(normId(e.name))
                    touched.removed++
                    touched.any = true
                    continue
                }
                if (f !== undefined && f < 1) {
                    e.conditions = (e.conditions || []).concat([{ condition: 'minecraft:random_chance', chance: f }])
                    touched.any = true
                } else if (f > 1) {
                    T.stats.boostSkipped++
                }
            }
            out.push(e)
        }
        if (!touched.any) return false
        pool.entries = out.length ? out : [{ type: 'minecraft:empty', weight: 1 }]
        T.stats.entriesRemoved += touched.removed
        T.stats.fallbackPools++
        return true
    }

    function rewritePool(pool, slack) {
        var entries = pool.entries
        if (!entries || !entries.length) return false
        if (needsFallback(entries)) return fallbackPool(pool)

        var fs = []
        var touched = false
        for (var i = 0; i < entries.length; i++) {
            var e = entries[i]
            var f = 1
            if (entryType(e) === 'minecraft:item' && e.name != null) {
                var r = ruleFor(normId(e.name), T.curTable)
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

        // target probabilities per roll: tuned entries get exactly f x their old share. `lost` is what
        // the tuned entries gave up (negative when boosts took more than cuts freed).
        var qs = []
        var kept = 0
        var lost = 0
        var boosts = 0
        for (var k = 0; k < entries.length; k++) {
            var p = ws[k] / total
            qs.push(fs[k] * p)
            if (fs[k] === 1) kept += p
            else lost += (1 - fs[k]) * p
            if (fs[k] > 1) boosts++
        }
        if (kept > 0) T.stats.boosted += boosts  // a boost on an entry that owns its whole pool changes nothing
        var emptyShare = 0
        if (lost > 0 && (slack === 'empty' || kept <= 0)) {
            emptyShare = lost
        } else if (kept > 0) {
            var scale = (kept + lost) / kept
            if (scale < 0.01) {
                // boosts want more than the whole pool: untouched entries keep 1% of their odds
                T.stats.boostCapped++
                warn(T.curTable + ': boosts ask for more than the pool has; the other entries keep 1% of their odds')
                scale = 0.01
            }
            for (var m = 0; m < entries.length; m++) {
                if (fs[m] === 1) qs[m] = qs[m] * scale
            }
        }

        var iw = integerWeights(emptyShare > 0 ? qs.concat([emptyShare]) : qs, total)
        var out = []
        var real = 0
        for (var n = 0; n < entries.length; n++) {
            var entry = entries[n]
            if (qs[n] <= 0) {
                if (entryType(entry) === 'minecraft:item' && entry.name != null) banPreRoll(normId(entry.name))
                T.stats.entriesRemoved++
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
        T.stats.poolsReweighted++
        return true
    }

    function rewriteTable(b, table) {
        var pools = JSON.parse(String(b.pools.toString()))
        var op = T.ops[table] || null
        T.curTable = table
        var changed = false
        var created = false
        if (op && op.alias) {
            if (pools.length) {
                warn('alias ' + table + ': the table exists in game, left alone')
                throw new Error(UNCHANGED)
            }
            pools = copy(op.alias)
            created = changed = true
            T.stats.aliased++
        }
        if (!pools.length) throw new Error(MISSING)
        if (op && op.vanilla) {
            pools = copy(op.vanilla.pools || [])
            changed = true
            T.stats.reverted++
        }
        if (op && op.pools.length && addPools(pools, op, table)) changed = true
        if (op && op.adds.length && addItems(pools, op, table)) changed = true
        for (var i = 0; i < pools.length; i++) {
            if (op && op.pack && packPool(pools[i], table)) changed = true
            if (op && op.companions && companionPool(pools[i], table)) changed = true
            if (rewritePool(pools[i], T.settings.slack)) changed = true
        }
        if (!changed) throw new Error(UNCHANGED)
        b.pools = JsonIO.parseRaw(JSON.stringify(pools))
        var type = T.index.tables[table] || (created ? 'minecraft:chest' : null)
        if (op && op.vanilla && op.vanilla.type) type = op.vanilla.type
        if (type) b.type = type
        T.stats.tables++
        if (T.settings.log_tables) console.info(TAG + 'rewrote ' + table)
    }

    function isActive() {
        return T.settings.enabled && T.config !== null && T.index !== null
    }

    // Items the catalog marks post_roll_only that a rule tunes: pre-roll can't reach them (a loot
    // modifier or mod code adds them), so their rule acts after the roll. Listed once per reload.
    function reportPostRollOnly() {
        var items = T.config.items || {}
        for (var id in items) {
            if (items[id].post_roll_only && ruleFor(id, '') !== undefined) T.stats.postOnly.push(id)
        }
        if (T.stats.postOnly.length) {
            console.info(TAG + T.stats.postOnly.length + ' tuned item(s) can only be tuned after the roll ' +
                '(a removed stack leaves an empty slot, boosts do nothing): ' + T.stats.postOnly.join(', '))
        }
    }

    function writeAdditions() {
        if (typeof global.writeExportJson !== 'function') return
        var res = callGuarded(function () {
            global.writeExportJson(ADDITIONS_PATH, {
                about: 'Written by Creative Worldgen\'s loot/05_tuning.js at every reload: pools and item entries the ' +
                    'pack\'s own rules added pre-roll. tools/gen_loot_config.py ignores them when it diffs /dumploot.',
                tables: T.additions,
            })
        })
        if (!res.ok) warn('could not write ' + ADDITIONS_PATH + ': ' + res.error)
    }

    function preRoll(event) {
        if (!isActive()) return
        ensureTags()
        checkBanSwitches()
        plan()
        var targets = {}
        for (var t in T.ops) targets[t] = true
        var items = T.index.items
        for (var id in items) {
            var edit = items[id].edit
            for (var i = 0; i < edit.length; i++) {
                if (!targets[edit[i]] && ruleFor(id, edit[i]) !== undefined) targets[edit[i]] = true
            }
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
        for (var tag in T.tagsWanted) {
            if (!Object.keys(T.tagMembers[tag] || {}).length) warn('#' + tag + ' has no members: its rules match nothing')
        }
        var st = T.stats
        console.info(TAG + st.rules + ' rules -> rewrote ' + st.tables + ' tables (' +
            st.entriesRemoved + ' entries removed, ' + st.poolsReweighted + ' pools reweighted, ' +
            st.fallbackPools + ' via conditions, ' + st.boosted + ' boosted entries; packed ' + st.packSplit +
            ' loose entries into ' + st.packEntries + ' crate entries; ' + st.companions + ' companions, ' +
            st.itemsAdded + ' items + ' + st.poolsAdded + ' pools added, ' + st.restored + ' injections restored, ' +
            st.reverted + ' reverted to vanilla, ' + st.aliased + ' aliases)' +
            (st.missingTables.length ? ', ' + st.missingTables.length +
                ' indexed tables no longer exist - re-run tools/gen_loot_config.py' : ''))
        reportPostRollOnly()
        writeAdditions()
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
        ensureTags()
        ctx.removeLoot(ItemFilter.custom(function (stack) {
            var id = String(stack.id)
            var f = ruleFor(id, table)
            if (f === undefined || f >= 1) return false
            if (f <= 0) {
                T.stats.postRemoved++
                var ban = banFor(id)
                if (ban) {
                    T.stats.banRemovedPost++
                    alarm('post|' + id + '|' + table, [
                        'BANNED item ' + id + ' rolled in chest loot ' + (table || '(unknown table)') + ' (post-roll stripped it)',
                        'ban ' + ban.name + ': ' + ban.why,
                        'Something added it after the pre-roll pass: a loot modifier or code pool. Check that mod\'s config.',
                    ])
                }
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
    } else {
        console.error(TAG + 'disabled: missing ' + CONFIG_PATH + ' or ' + INDEX_PATH +
            ' - run python3 tools/gen_loot_config.py')
    }
    global.lootTuning = T

    // Rules from other jars' script packs (Creative Delight's loot.js files): packs load in namespace hash order, so
    // one can load before this file has made the fresh T. They queue their rules with global.CWG.lootRules(fn) (the
    // queue is made at startup, _worldgen_core.js) and run here before the tag event that captures '#tag' members.
    ServerEvents.highPriorityData(function () {
        var q = global.CWG ? global.CWG._lootQueue : null
        if (!q) return
        var fns = q.splice(0, q.length)
        fns.forEach(function (fn) {
            var res = callGuarded(function () { fn(T) })
            if (!res.ok) warn('queued loot rules failed: ' + res.error)
        })
    })

    ServerEvents.genericLootTables(preRoll)

    LootJS.modifiers(event => {
        if (!isActive()) return
        event.addLootTypeModifier(LootType.CHEST)
            .apply(ctx => postRoll(ctx))
            .logName('CE/loot/tuning')
    })

    PlayerEvents.loggedIn(event => {
        var st = T.stats
        if (!st.alarms.length || !event.player.hasPermissions(2)) return
        event.player.tell(Text.of('[loot] ' + st.alarms.length + ' loot ban warning(s) since the last reload:').red())
        for (var i = 0; i < st.alarms.length && i < 5; i++) event.player.tell(Text.of('  ' + st.alarms[i]).red())
    })

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('loottuning')
                .requires(src => src.hasPermission(2))
                .executes(ctx => {
                    var st = T.stats
                    var tags = []
                    for (var tag in T.tagsWanted) tags.push('#' + tag + ' (' + Object.keys(T.tagMembers[tag] || {}).length + ')')
                    var lines = [
                        'Loot tuning: ' + (isActive() ? 'active' : 'inactive') + ', slack=' + T.settings.slack +
                            ', ' + st.rules + ' rules (' + T.bans.length + ' bans)',
                        'pre-roll: ' + st.tables + ' tables rewritten, ' + st.entriesRemoved + ' entries removed, ' +
                            st.poolsReweighted + ' pools reweighted, ' + st.fallbackPools + ' via conditions, ' +
                            st.boosted + ' boosted' + (st.boostSkipped ? ' (' + st.boostSkipped + ' boosts impossible in composite pools)' : ''),
                        'packed: ' + T.packs.length + ' pairs, rate ' + T.packDefault + ', ' + T.packBoosts.length +
                            ' boosts -> ' + st.packSplit + ' loose entries split into ' + st.packEntries + ' crate entries' +
                            (st.packFallback ? ', ' + st.packFallback + ' skipped (composite pools)' : ''),
                        'added: ' + st.companions + ' companions, ' + st.itemsAdded + ' items, ' + st.poolsAdded + ' pools, ' +
                            st.restored + ' restored injections, ' + st.reverted + ' tables reverted to vanilla, ' + st.aliased + ' aliases',
                        'post-roll since reload: ' + st.postRemoved + ' stacks removed, ' + st.postThinned + ' thinned; ' +
                            st.postOnly.length + ' tuned items are post-roll only',
                        'bans: ' + st.banRemovedPre + ' entries removed pre-roll, ' + st.banRemovedPost + ' stacks stripped post-roll',
                    ]
                    if (tags.length) lines.push('tag rules: ' + tags.join(', '))
                    if (st.missingTables.length) lines.push('stale index, missing tables: ' + st.missingTables.join(', '))
                    if (st.warnings.length) lines.push(st.warnings.length + ' warnings (logs/kubejs/server.log)')
                    for (var i = 0; i < lines.length; i++) ctx.source.sendSuccess(Text.of(lines[i]), false)
                    for (var j = 0; j < st.alarms.length; j++) ctx.source.sendSuccess(Text.of('ALARM: ' + st.alarms[j]).red(), false)
                    return 1
                })
        )
    })

})()
