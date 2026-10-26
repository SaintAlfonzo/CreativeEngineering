// requires: vintagedelight
// Vintage Delight chest loot (vintageport #5, vintage_delight.md S11). Rules for the pre-roll loot framework
// (server_scripts/loot/05_tuning.js, global.lootTuning); the pack's rules are in loot/07_rules.js.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
    if (!global.lootTuning) return
    var L = global.lootTuning
    var i = global.item

    // ── crates and bags split out of their loose crop's entry, like the Farmer's Delight crates ──
    L.pack(i.oat_bale,           i.oat)
    L.pack(i.oat_bag,            i.raw_oats)
    L.pack(i.peanut_crate,       i.peanut)
    L.pack(i.cucumber_crate,     i.cucumber)
    L.pack(i.ghost_pepper_crate, i.ghost_pepper)

    // ── the crops of the port's 7 chest tables, as entries in each vanilla table's main pool ──
    // Upstream added an extra 1-3 roll pool per table (GLMs, not ported). Same pattern as Veggies (07_rules.js): the
    // items join the main pool at a small share. Salt dust and pickled pepper left out: not crops.
    var VTD_LOOT = [
        ['minecraft:chests/bastion_hoglin_stable', [i.ghost_pepper, i.ghost_pepper_seeds], 0.03, [2, 5]],
        ['minecraft:chests/ruined_portal', [i.ghost_pepper, i.ghost_pepper_seeds], 0.03, [1, 3]],
        ['minecraft:chests/village/village_desert_house', [i.cucumber, i.cucumber_seeds], 0.03, [1, 3]],
        ['minecraft:chests/village/village_plains_house', [i.peanut, i.oat, i.oat_seeds], 0.02, [1, 3]],
        ['minecraft:chests/village/village_savanna_house', [i.cucumber, i.cucumber_seeds, i.peanut], 0.02, [1, 3]],
        ['minecraft:chests/village/village_snowy_house', [i.oat, i.oat_seeds], 0.03, [1, 3]],
        ['minecraft:chests/village/village_taiga_house', [i.oat, i.raw_oats], 0.03, [1, 3]],
    ]
    VTD_LOOT.forEach(function (row) {
        row[1].forEach(function (item) { L.add(row[0], item, { share: row[2], count: row[3] }) })
    })
})()
