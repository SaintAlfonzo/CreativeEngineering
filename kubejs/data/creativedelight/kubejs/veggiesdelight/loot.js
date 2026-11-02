// requires: veggiesdelight
// requires: rusticdelight
// Veggies Delight loot rows: its crop crates and 9 chest entries (Creative Delight members #2, moved from the pack's
// loot/07_rules.js 2026-10-25, rows unchanged), and Rustic Delight's bell pepper crates (vintageport #5): its bell pepper
// is Rustic's, whose crates and seed bag had no pack rule. Rules are collected here and applied by the pack's loot
// engine in its fixed phase order (loot/05_tuning.js), so file order does not matter.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {
    if (!global.lootTuning) return
    var L = global.lootTuning
    var i = global.item

    L.pack(i.broccoli_crate,                i.broccoli)
    L.pack(i.cauliflower_crate,             i.cauliflower)
    L.pack(i.garlic_crate,                  i.garlic)
    L.pack(i.sweet_potato_crate,            i.sweet_potato)
    L.pack(i.turnip_crate,                  i.turnip)
    L.pack(i.zucchini_crate,                i.zucchini)

    L.pack(i.bell_pepper_red_crate,    i.bell_pepper_red)
    L.pack(i.bell_pepper_green_crate,  i.bell_pepper_green)
    L.pack(i.bell_pepper_yellow_crate, i.bell_pepper_yellow)
    L.pack(i.bell_pepper_seeds_bag,    i.bell_pepper_seeds)

    // ── Veggies Delight: its 9 chest injections, as entries in each table's main pool ─────────
    // Upstream added an extra 1-3 roll pool to each table (GLMs, not ported). Here the same items join the main pool
    // at a small share, so the table's budget stays as it is. Its bell pepper and seeds are Rustic Delight's red
    // pepper and pepper seeds. feature_writeup/veggies_delight.md 4.11
    var VD_LOOT = [
        ['minecraft:chests/abandoned_mineshaft', [i.bell_pepper_seeds, i.broccoli_seeds, i.cauliflower_seeds, i.turnip_seeds, i.zucchini_seeds], 0.015, [2, 4]],
        ['minecraft:chests/pillager_outpost', [i.garlic], 0.04, [4, 12]],
        ['minecraft:chests/shipwreck_supply', [i.cauliflower_seeds, i.turnip, i.zucchini], 0.03, [2, 4]],
        ['minecraft:chests/simple_dungeon', [i.bell_pepper_seeds, i.broccoli_seeds], 0.015, [2, 4]],
        ['minecraft:chests/village/village_desert_house', [i.bell_pepper_seeds, i.bell_pepper_red], 0.03, [1, 3]],
        ['minecraft:chests/village/village_plains_house', [i.bell_pepper_seeds, i.sweet_potato, i.garlic, i.broccoli, i.turnip], 0.02, [1, 3]],
        ['minecraft:chests/village/village_savanna_house', [i.bell_pepper_red, i.zucchini], 0.03, [1, 3]],
        ['minecraft:chests/village/village_snowy_house', [i.cauliflower, i.cauliflower_seeds, i.sweet_potato, i.turnip], 0.02, [1, 3]],
        ['minecraft:chests/village/village_taiga_house', [i.cauliflower, i.cauliflower_seeds, i.garlic, i.sweet_potato, i.turnip], 0.02, [1, 3]],
    ]
    VD_LOOT.forEach(function (row) {
        row[1].forEach(function (item) { L.add(row[0], item, { share: row[2], count: row[3] }) })
    })
})()
