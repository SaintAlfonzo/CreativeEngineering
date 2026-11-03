// requires: vintagedelight
// requires: lootjs
// Vintage Delight chest loot (vintageport #5, vintage_delight.md S11). Rules for the pre-roll loot framework
// (Creative Worldgen loot/05_tuning.js, global.lootTuning); the pack's rules are in Worldgen's loot/07_rules.js.
//
// The rules sit in a callback, so nothing leaks into the top-level scope all server scripts share.

// Queued with global.CWG.lootRules (Creative Worldgen's loot engine, loot/05_tuning.js): its script pack can load
// after this one, so the rules run when the engine of the current reload exists. No Worldgen jar: no rules.
if (global.CWG) global.CWG.lootRules(function (L) {
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

    // ── Mixed Seeds (creativedelight/members #32, vintage_delight.md 2.3a): farm-side chests, a few per find ──
    var MIXED_SEEDS_CHESTS = [
        'minecraft:chests/village/village_plains_house', 'minecraft:chests/village/village_taiga_house',
        'minecraft:chests/village/village_savanna_house', 'minecraft:chests/village/village_snowy_house',
        'minecraft:chests/village/village_desert_house', 'minecraft:chests/pillager_outpost',
    ]
    MIXED_SEEDS_CHESTS.forEach(function (table) { L.add(table, i.mixed_seeds, { share: 0.04, count: [1, 4] }) })
})

// Mixed Seeds from grass (creativedelight/members #32): 2 % per grass or tall grass broken without shears, beside the
// vanilla wheat seed drop (12.5 %).
LootJS.modifiers(event => {
    if (!global.CD || !global.CD.on('vintagedelight')) return
    var i = global.item
    event.addBlockLootModifier([i.grass, i.tall_grass])
        .not(n => n.matchMainHand(i.any_shears))
        .randomChance(0.02)
        .addLoot(i.mixed_seeds)
        .logName('CE/vintage_mixed_seeds_grass')
})
