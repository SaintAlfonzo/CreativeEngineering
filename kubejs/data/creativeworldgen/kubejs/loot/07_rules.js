// requires: creativeworldgen
// Chest loot — RULES.  Script-side calls into the pre-roll loot framework (05_tuning.js, global.lootTuning).
//
// Per-item knobs stay in loot_config.json; this file is for named groups and anything that needs
// code. Everything here edits the loot table JSON before the roll (exact odds, the slot is refilled
// from the pool) unless the item only exists after the roll; /loottuning and the log list those.
// /reload applies it.
//
//   L.frequency(target, f, { except, tables })   f: 0 remove, < 1 rarer, > 1 boost, per roll
//   L.remove(target, { except, tables })          never (in those tables)
//   L.ban(target, { name, why, modLootOn, ownTables })  remove + LOUD warning if it ever shows up again
//   L.pack(packed, loose, { rate, max })          split packed food out of the loose item's entry
//   L.packRate(r)  L.packBoost(tables, mult)  L.packExclude(tables)
//   L.companion(item, anchors, { rate, count, nbt, tables })   item beside every anchor entry
//   L.add(tables, item, { share | weight, count, pool, nbt, except })
//   L.addPool(tables, poolJson)  L.restoreInjection(tables, poolJson, { mod })
//   L.useVanilla(tables, { winner })  L.alias(missingTable, sourceTable | [poolJson])
//   L.drop(tables, item, { count, chance, looting, knife, burning, cooked, tool })  L.dropShare(table, item, share)
//                                                 mob, block and fishing tables (09_drops.js)
// target: 'ns:id' | '@modid' | '/regex/' | '#ns:tag' (a global.item key), or a list of those.
// tables: 'ns:table' | '@mod' | '/regex/' | a JS regex | a list.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var L = global.lootTuning
    var i = global.item

    // ── bans: the mod's own loot switch is off; these catch anything that slips past ─────────

    L.ban('@pneumaticcraft', {
        name: 'PneumaticCraft',
        why: 'PneumaticCraft chest loot is off: enable_dungeon_loot = false in config/pneumaticcraft-common.toml',
        modLootOn: function () {
            var PNC = Java.loadClass('me.desht.pneumaticcraft.common.config.ConfigHelper')
            return String(PNC.common().general.enableDungeonLoot.get()) === 'true'
        },
    })

    L.ban('/^lightmanscurrency:coin(pile|block)?_(chocolate_)?(copper|iron|gold|emerald|diamond|netherite)$/', {
        name: 'Lightman\'s coins',
        why: 'Lightman\'s coin loot is off: [loot.chests] and [loot.entities] enabled=false in config/lightmanscurrency-common.lcconfig',
        ownTables: '@lightmanscurrency',
        modLootOn: function () {
            var LC = Java.loadClass('io.github.lightman314.lightmanscurrency.LCConfig').COMMON
            return String(LC.enableChestLoot.get()) === 'true' || String(LC.enableEntityDrops.get()) === 'true'
        },
    })

    // ── groups ────────────────────────────────────────────────────────────────────────────────

    // chalk sticks + chalk box (tags/loottags.js), half as often in every chest
    L.frequency(i.t_loot_chalk, 0.5)

    // ── packed food: crates / sacks split out of their loose contents' entry ──────────────────
    // rate = the share of the loose item's entry that becomes crates. The entry keeps its chance per
    // roll; a low roll within it still gives the loose food, a high one gives crates, the highest
    // the most crates (up to 2 for a 1-7 stack). No other entry in the pool changes.

    L.packRate(0.1)
    L.packBoost(/village/, 3)                 // villages: compressed food 3x as common (rate 0.3)
    L.packExclude(/^wares:agreement\//)       // Wares trade agreements: a crate would change the deal

    L.pack(i.farmersdelight_carrot_crate,   i.minecraft_carrot)
    L.pack(i.farmersdelight_potato_crate,   i.minecraft_potato)
    L.pack(i.farmersdelight_beetroot_crate, i.minecraft_beetroot)
    L.pack(i.cabbage_crate,                 i.farmersdelight_cabbage)
    L.pack(i.tomato_crate,                  i.farmersdelight_tomato)
    L.pack(i.onion_crate,                   i.onion_farmersdelight)
    L.pack(i.rice_bag,                      i.farmersdelight_rice)
    L.pack(i.apple_bag,                     i.apple)
    L.pack(i.cherry_bag,                    i.cherry)
    L.pack(i.red_grape_bag,                 i.red_grape)
    L.pack(i.white_grape_bag,               i.white_grape)
    L.pack(i.berry_sack,                    i.sweet_berries)
    L.pack(i.glowberry_sack,                i.glow_berries)
    L.pack(i.cocoa_beans_sack,              i.cocoa_beans)
    L.pack(i.chorus_crate,                  i.chorus_fruit)
    L.pack(i.ancient_fruit_crate,           i.ancient_fruit)
    // Veggies Delight's crates and chest entries: Creative Delight members #2 (kubejs/data/creativedelight/kubejs/veggiesdelight/loot.js)

    // ── Golden Food: its village overrides out, its loot folded into golden food pools ───────
    // goldenfood ships copies of all 16 vanilla village chest tables that only add its enchanted book.
    // Because they replace the vanilla files, Forge stops running other mods' injections into them
    // (Chalk, Botania, CC:Tweaked). So: vanilla's own pools back (no book), Chalk's injection
    // restored (Botania and CC:Tweaked left out), and golden food made twice as common.

    L.useVanilla(/^minecraft:chests\/village\//, { winner: /goldenfood/ })

    // The pool Chalk adds in code, from its bytecode (LootTableModification). Only re-added where the
    // vanilla table is overridden.
    L.restoreInjection([
        'minecraft:chests/village/village_cartographer', 'minecraft:chests/village/village_mason',
        'minecraft:chests/village/village_plains_house', 'minecraft:chests/village/village_savanna_house',
    ], {
        rolls: 1,
        entries: [{ type: 'minecraft:loot_table', name: 'chalk:chests/village_chalk_loot' }],
    }, { mod: 'chalk' })
    // Botania (botania:inject/village_chest: temple, toolsmith, weaponsmith) and CC:Tweaked (computercraft:treasure_disk:
    // cartographer) are not restored: owner 2026-11-03 keeps them out of village chests (plan_scratch_bugfix_1103 7.3).
    // Since the golden copies stopped being served, Forge injects them natively, so they are removed after the roll in
    // 10_removals.js.

    // The Golden Food enchantment book is not placed in any loot table (goldenfood's copies are reverted
    // above, which is what removed it from villages).
    L.frequency(i.t_loot_golden_food, 2)                                     // golden food twice as common

    // ── containers that name a table nobody ships (always empty): one roll of a sibling ───────
    L.alias('kaisyn:village/village_birch_forest_house',                    'minecraft:chests/village/village_plains_house')
    L.alias('kaisyn:village/village_forest_ruins/forest_ruins_butcher',     'minecraft:chests/village/village_butcher')
    L.alias('kaisyn:village/village_forest_ruins/forest_ruins_library',     'terralith:village/fortified/library')
    L.alias('kaisyn:village/village_forest_ruins/forest_ruins_temple',      'minecraft:chests/village/village_temple')
    L.alias('wythers:chests/village/forest_ruins_big_house',                'minecraft:chests/village/village_plains_house')
    L.alias('luna_structures:chests/taiga_inn/taiga_inn_barrels',           'simply_houses:chests/taiga_inn/taiga_inn_barrels')
    L.alias('minecraft:chests/village/village_leatherworker',               'minecraft:chests/village/village_tannery')
    L.alias('terralith:village/desert/fisherman',                           'minecraft:chests/village/village_fisher')
    L.alias('terralith:village/desert/leatherworker',                       'minecraft:chests/village/village_tannery')
    L.alias('terralith:village/desert/shepherd',                            'minecraft:chests/village/village_shepherd')
    L.alias('terralith:underground/giant_bee_hive', [{
        rolls: { type: 'minecraft:uniform', min: 2, max: 4 },
        entries: [
            { type: 'minecraft:item', name: i.honeycomb, weight: 10,
              functions: [{ function: 'minecraft:set_count', count: { type: 'minecraft:uniform', min: 1, max: 3 } }] },
            { type: 'minecraft:item', name: i.honey_bottle, weight: 6 },
            { type: 'minecraft:item', name: i.glass_bottle, weight: 3 },
        ],
    }])

})()
