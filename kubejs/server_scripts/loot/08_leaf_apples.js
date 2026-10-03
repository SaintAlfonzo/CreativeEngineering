// Block loot — LEAF APPLES.  Oak and dark oak leaves no longer drop apples.
//
// Apples come from apple trees (Fruits Delight and Vinery, placed by worldgen/wild_crops.js in oak forests)
// until the two apple trees are merged; feature_writeup/crops.md section 2.8. Myrtrees rubberwood leaves
// copy the oak table and still drop apples (not in TABLES).
//
// Pre-roll, same mechanism as 06_dead_maps.js: ServerEvents.genericLootTables rewrites the raw table JSON
// before the game parses it. The apple pool stays (Forge names pools by position) with an empty entry.
// modify() resets the table type to minecraft:generic, so it is set back to minecraft:block.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var TAG = '[leaf-apples] '
    var SKIP = 'leaf-apples:skip'

    var TABLES = [
        'minecraft:blocks/oak_leaves',
        'minecraft:blocks/dark_oak_leaves',
    ]

    function dropsApple(entry, apple) {
        if (entry.name === apple) return true
        var kids = entry.children || []
        for (var k = 0; k < kids.length; k++) if (dropsApple(kids[k], apple)) return true
        return false
    }

    function rewrite(b, apple, stats) {
        var pools = JSON.parse(String(b.pools.toString()))
        if (!pools.length) throw new Error(SKIP)
        var removed = 0
        for (var p = 0; p < pools.length; p++) {
            var entries = pools[p].entries || []
            var kept = entries.filter(function (e) { return !dropsApple(e, apple) })
            removed += entries.length - kept.length
            pools[p].entries = kept.length ? kept : [{ type: 'minecraft:empty', weight: 1 }]
        }
        if (!removed) throw new Error(SKIP)
        b.pools = JsonIO.parseRaw(JSON.stringify(pools))
        b.type = 'minecraft:block'
        stats.tables++
    }

    ServerEvents.genericLootTables(function (event) {
        var apple = global.item.apple
        var stats = { tables: 0 }
        TABLES.forEach(function (table) {
            try {
                event.modify(table, function (b) { rewrite(b, apple, stats) })
            } catch (err) {
                if (String(err).indexOf(SKIP) < 0) console.warn(TAG + table + ': ' + err)
            }
        })
        console.info(TAG + 'apples removed from ' + stats.tables + ' of ' + TABLES.length + ' leaf tables')
    })

})()
