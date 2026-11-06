// requires: creativeworldgen
// Chest loot — DEAD MAPS.  Drop explorer-map entries that point at structures we've disabled.
//
// An exploration_map whose destination never generates makes the server scan ~10k candidate
// chunks on the roll and hang past the 60 s watchdog (Monobank vault rolling a ruined_portal
// map: debug/crash-2026-09-27_15.32.34-server.txt). patcherMod's ChunkGeneratorLocateMixin
// stops the hang (such maps come out blank); this pass removes the dud map from the table so
// the roll lands on real loot instead.
//
// Pre-roll, same mechanism as 05_tuning.js: ServerEvents.genericLootTables rewrites the raw JSON
// before the game parses it (a LootJS post-roll edit is too late: the search runs during the roll).
// The loot-table map can't be enumerated from here, so tables are listed explicitly.
// tools/loot_census.py / a jar grep for "exploration_map" finds new ones after mod updates.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var TAG = '[dead-maps] '

    // Structure tags / ids disabled in kubejs/generation_scripts/worldgen/structure_config.json
    // (whole structure_set at frequency 0). Matched against exploration_map "destination".
    var DEAD_DESTINATIONS = {
        'minecraft:ruined_portal': true,
    }

    var TABLES = [
        'monobank:monobank/village/desert',
        'monobank:monobank/village/plains',
        'monobank:monobank/village/savanna',
        'monobank:monobank/village/snowy',
        'monobank:monobank/village/taiga',
    ]

    var SKIP = 'dead-maps:skip'

    function normalize(id) {
        var s = String(id).replace(/^#/, '')
        return s.indexOf(':') < 0 ? 'minecraft:' + s : s
    }

    function isDeadMap(entry) {
        var fns = entry.functions || []
        for (var i = 0; i < fns.length; i++) {
            var f = fns[i]
            if (normalize(f.function) === 'minecraft:exploration_map' && f.destination != null &&
                DEAD_DESTINATIONS[normalize(f.destination)]) return true
        }
        return false
    }

    function rewrite(b, table, stats) {
        var pools = JSON.parse(String(b.pools.toString()))
        if (!pools.length) throw new Error(SKIP) // table doesn't exist (mod removed): don't create it
        var removed = 0
        for (var i = 0; i < pools.length; i++) {
            var entries = pools[i].entries || []
            var kept = entries.filter(function (e) { return !isDeadMap(e) })
            removed += entries.length - kept.length
            pools[i].entries = kept.length ? kept : [{ type: 'minecraft:empty', weight: 1 }]
        }
        if (!removed) throw new Error(SKIP)
        b.pools = JsonIO.parseRaw(JSON.stringify(pools))
        stats.tables++
        stats.entries += removed
    }

    ServerEvents.genericLootTables(function (event) {
        var stats = { tables: 0, entries: 0 }
        TABLES.forEach(function (table) {
            try {
                event.modify(table, function (b) { rewrite(b, table, stats) })
            } catch (err) {
                if (String(err).indexOf(SKIP) < 0) console.warn(TAG + table + ': ' + err)
            }
        })
        console.info(TAG + 'removed ' + stats.entries + ' explorer-map entries from ' + stats.tables + ' tables')
    })

})()
