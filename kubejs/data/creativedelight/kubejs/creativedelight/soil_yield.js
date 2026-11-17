// requires: creativedelight
// Soil yield: extra produce from a ripe crop grown on better soil (feature_writeup/crop_yields.md, TODO farming/crops #23,
// wave 2: plan_crop_yields_wave2.md, #53 to #55).
// Each switched-on crop gets one pool per soil, appended to its harvest table: rolls = binomial sized to bonus x the
// crop's average produce rounded to the nearest 0.5 (Rich +33%, Fertile Soil +66% by default; the nether's Rich Soul
// Soil and Fertile Soul Soil the same), each roll one of the crop's produce, only when the crop is ripe and the block
// below it (offset rows below for two-high and water crops) is that soil. The crop's own pools are untouched, so a crop
// that is off, or on plain farmland at 0 bonus, drops exactly what its mod ships. Values: CDConfig
// [crop_drops.soil_yield] (one bonus per soil; one switch per crop in [crop_drops.soil_yield.crops] and per bush in
// [...bushes], all on by default). A crop whose key the config lacks stays off. Ender Crop is out: it only grows on
// tilled End Stone.
//
// Bases come from Creative Delight's soil_yield.json through crops.SoilYieldPicks.lootBase, the table the own-pick
// mixins read too, so the pick and the break use one set of numbers. A crop picked by its own right-click (tomato vines,
// cucumber, ghost pepper, zucchini, the bushes, propelplant pearls, colony shearing) gets the same extra from those
// mixins; this script covers the breaks.
//
// The same pass strips Fortune from every crop table in FORTUNE_FREE (wave 2 part A: Fortune never changes a crop's
// yield; melons and cantaloupe are left alone), and gives Vintage's cucumber and ghost pepper their seeds on a ripe
// break (part G).
//
// Same pre-roll mechanism as rusticdelight/crop_drops.js: ServerEvents.genericLootTables rewrites the raw table JSON
// before the game parses it. New pools are only appended (Forge names pools by position), and modify() resets the table
// type, so it is set back to minecraft:block. The bell pepper rewrite finds its pools by their entries; these pools
// have three entries and no random_chance, so it never mistakes one for its own.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var TAG = '[soil-yield] '

    var DEFAULTS = {
        farmlandBonus: 0.0,
        richFarmlandBonus: 0.33,
        fertileSoilBonus: 0.66,
        richSoilBonus: 0.33,
        richSoulSoilBonus: 0.33,
        fertileSoulSoilBonus: 0.66,
    }

    // key: CDConfig switch (crops or bushes) and soil_yield.json 'loot' key, which holds the base (the table's average
    // produce per ripe harvest, rounded to 0.5, no Fortune; crop_yields.md 2.1). block: the table's block. ripe: block
    // state properties of a ripe harvest. produce: item keys, one picked per roll. knife: what a roll gives instead when
    // the crop is cut with a knife, as the crop's own table does. below: rows from the crop down to its soil (default 1).
    // nether: true = the soul soils, else the overworld soils.
    function crops(i, b) {
        return [ // G5 begin: crops of absent mods are filtered out (global.CD.absentIn) where the list is read
            { key: 'wheat', block: b.wheat_crop, ripe: { age: '7' }, produce: [i.wheat] },
            { key: 'carrots', block: b.carrots, ripe: { age: '7' }, produce: [i.minecraft_carrot] },
            { key: 'potatoes', block: b.potatoes, ripe: { age: '7' }, produce: [i.minecraft_potato] },
            { key: 'beetroots', block: b.beetroots, ripe: { age: '3' }, produce: [i.minecraft_beetroot] },
            { key: 'cabbages', block: b.cabbages, ripe: { age: '7' }, produce: [i.farmersdelight_cabbage] },
            { key: 'onions', block: b.onions, ripe: { age: '7' }, produce: [i.onion_farmersdelight] },
            // The bottom vine only: a vine on a rope has no soil below (its pick reads the bottom vine's soil).
            { key: 'tomatoes', block: b.tomatoes, ripe: { age: '3' }, produce: [i.farmersdelight_tomato] },
            // Panicles sit on the rice, which stands in water on the soil.
            { key: 'rice', block: b.rice_panicles, ripe: { age: '3' }, produce: [i.rice_panicle], knife: i.farmersdelight_rice, below: 2 },
            { key: 'bell_peppers', block: b.bell_peppers, ripe: { age: '7' },
                produce: [i.bell_pepper_red, i.bell_pepper_green, i.bell_pepper_yellow] },
            { key: 'coffee', block: b.coffee_crop, ripe: { age: '5' }, produce: [i.coffee_beans] },
            { key: 'cotton', block: b.cotton_crop, ripe: { age: '3' }, produce: [i.cotton_boll] },
            { key: 'tobacco', block: b.tobacco_crop, ripe: { blockstate: '7' }, produce: [i.tobacco_leafs] },
            { key: 'flax', block: b.flax_crop, ripe: { age: '7', half: 'lower' }, produce: [i.flax] },
            // Fibre drops from the grown bottom and from the top; the pool goes on the top (the part players cut).
            { key: 'hemp', block: b.hemp, ripe: { growth: 'top0' }, produce: [i.hemp_fiber], below: 2 },
            // Both halves drop corn; the pool goes on the lower half only.
            { key: 'corn', block: b.corn_crop, ripe: { age: '7', upper: 'false' }, produce: [i.corn] },
            { key: 'cave_carrots', block: b.cave_carrots, ripe: { age: '3' }, produce: [i.cave_carrot] },
            { key: 'broccoli', block: b.broccoli_crop, ripe: { age: '5' }, produce: [i.broccoli] },
            { key: 'cauliflower', block: b.cauliflower_crop, ripe: { age: '7' }, produce: [i.cauliflower] },
            { key: 'garlic', block: b.garlic_crop, ripe: { age: '5' }, produce: [i.garlic] },
            { key: 'sweet_potato', block: b.sweet_potato_crop, ripe: { age: '5' }, produce: [i.sweet_potato] },
            { key: 'turnip', block: b.turnip_crop, ripe: { age: '5' }, produce: [i.turnip] },
            { key: 'zucchini', block: b.zucchini_crop, ripe: { age: '7' }, produce: [i.zucchini] },
            { key: 'cucumber', block: b.cucumber_crop, ripe: { age: '7' }, produce: [i.cucumber] },
            { key: 'oats', block: b.oat_crop, ripe: { age: '7' }, produce: [i.oat], knife: i.raw_oats },
            { key: 'peanuts', block: b.peanut_crop, ripe: { age: '7' }, produce: [i.peanut] },
            { key: 'adzuki', block: b.adzuki_sprouts, ripe: { age: '6', flowering: 'true' }, produce: [i.adzuki_beans] },
            // Bushes (wave 2, 5.2): the break; the own pick is the mixins'.
            { key: 'sweet_berry', block: b.sweet_berry_bush, ripe: { age: '3' }, produce: [i.sweet_berries] },
            { key: 'strawberry', block: b.strawberry_bush, ripe: { age: '6', type: 'red' }, produce: [i.strawberries] },
            { key: 'strawberry', block: b.strawberry_bush, ripe: { age: '6', type: 'white' }, produce: [i.white_strawberries] },
            { key: 'blueberry', block: b.blueberry_bush, ripe: { age: '4' }, produce: [i.blueberry] },
            { key: 'cranberry', block: b.cranberry_bush, ripe: { age: '4' }, produce: [i.cranberry] },
            // The pack's lemon_tree table drops lemons from the lower half only; the soil is under it.
            { key: 'lemon', block: b.lemon_tree, ripe: { age: '4', half: 'lower' }, produce: [i.lemon] },
            // Nether (wave 2, 6): the soul soils. Wart grows on Soul Sand and Nulch (not Rich Soul Soil), ghost pepper on
            // #vintagedelight:ghost_pepper_soil. Colonies age only on Rich Soul Soil; a ripe break gives 5 fungi
            // (shears give the colony itself, no extra). Propelplant pearls come only from the pick (mixin).
            { key: 'nether_wart', block: b.nether_wart_crop, ripe: { age: '3' }, produce: [i.nether_wart], nether: true },
            { key: 'ghost_pepper', block: b.ghost_pepper_crop, ripe: { age: '7' }, produce: [i.ghost_pepper], nether: true },
            { key: 'crimson_fungus_colony', block: i.crimson_fungus_colony, ripe: { age: '3' }, produce: [i.crimson_fungus],
                notTool: i.any_shears, nether: true },
            { key: 'warped_fungus_colony', block: i.warped_fungus_colony, ripe: { age: '3' }, produce: [i.warped_fungus],
                notTool: i.any_shears, nether: true },
        ] // G5 end
    }

    function soils(i, cfg) {
        return [ // G5 begin: soils of absent mods are filtered out (global.CD.absentIn) below
            { name: 'farmland', block: i.farmland, bonus: cfg.farmlandBonus },
            { name: 'rich farmland', block: i.rich_farmland, bonus: cfg.richFarmlandBonus },
            { name: 'fertile soil', block: i.fertile_soil, bonus: cfg.fertileSoilBonus },
            { name: 'rich soil', block: i.rich_soil, bonus: cfg.richSoilBonus },
            { name: 'rich soul soil', block: global.blocks.rich_soul_soil, bonus: cfg.richSoulSoilBonus, nether: true },
            { name: 'fertile soul soil', block: i.nulch, bonus: cfg.fertileSoulSoilBonus, nether: true },
        // G5 end
        ].map(function (s) {
            s.bonus = Number(s.bonus)
            return s
        }).filter(function (s) { return s.bonus > 0 && !global.CD.absentIn(s.block) })
    }

    var $Bases = (function () {
        try {
            return Java.loadClass('com.SaintAlfonzo.creativedelight.crops.SoilYieldPicks')
        } catch (err) {
            return null
        }
    })()

    // The crop's base from soil_yield.json, or NaN (row skipped with a warning).
    function base(key) {
        return $Bases ? Number($Bases.lootBase(key)) : NaN
    }

    var $SoilYield = (function () {
        try {
            var cd = global.NCH.cfgClass('creativedelight')   // CDConfig, registered by cd_core.js
            return cd ? cd.CROP_DROPS.SOIL_YIELD : null
        } catch (err) {
            return null
        }
    })()

    function cropOn(key) {
        if (!$SoilYield) return false
        var on = $SoilYield.cropOn(key)
        if (on == null) {
            console.warn(TAG + key + ': no switch in CDConfig [crop_drops.soil_yield.crops], left off')
            return false
        }
        return on == true
    }

    function entry(crop, item, i) {
        if (!crop.knife) return { type: 'minecraft:item', name: item }
        return { type: 'minecraft:alternatives', children: [
            { type: 'minecraft:item', name: crop.knife,
                conditions: [{ condition: 'minecraft:match_tool', predicate: { tag: i.any_knife.substring(1) } }] },
            { type: 'minecraft:item', name: item },
        ] }
    }

    // Extra mean = bonus x base, as binomial(n, p) with n = ceil(extra) and p = extra / n. A one-item crop (base 1) at
    // 0.33 gets binomial(1, 0.33): a 33% chance of one more harvest. A variable crop gets its count raised by the same
    // share of its mean (carrots 3.71 at 0.66: binomial(3, 0.816), +2.45).
    function rolls(crop, soil) {
        var extra = crop.base * soil.bonus
        var n = Math.max(1, Math.ceil(extra - 1e-9))
        return { type: 'minecraft:binomial', n: n, p: Math.min(1, Math.round(extra / n * 10000) / 10000) }
    }

    function pool(crop, soil, i) {
        var conditions = [
            { condition: 'minecraft:block_state_property', block: crop.block, properties: crop.ripe },
            { condition: 'minecraft:location_check', offsetY: -(crop.below || 1), predicate: { block: { blocks: [soil.block] } } },
        ]
        if (crop.notTool) conditions.push({ condition: 'minecraft:inverted',
            term: { condition: 'minecraft:match_tool', predicate: { tag: crop.notTool.substring(1) } } })
        return {
            rolls: rolls(crop, soil),
            bonus_rolls: 0.0,
            entries: crop.produce.map(function (item) { return entry(crop, item, i) }),
            conditions: conditions,
        }
    }

    // Part G: Vintage's cucumber and ghost pepper put their seed bonus on age 3 of an age-7 crop, so a ripe break gave
    // no seeds. A ripe break now drops the same 1 to 4 seeds (1 + binomial(3, 0.5714)) as the age-3 break; that pool
    // stays as it is. The own pick drops no seeds (the plant keeps growing).
    function ripeSeeds(i, b) {
        return [
            { block: b.cucumber_crop, seeds: i.cucumber_seeds },
            { block: b.ghost_pepper_crop, seeds: i.ghost_pepper_seeds },
        ]
    }

    function seedPool(row) {
        return {
            rolls: 1.0,
            bonus_rolls: 0.0,
            entries: [{ type: 'minecraft:item', name: row.seeds, functions: [
                { function: 'minecraft:set_count', count: { type: 'minecraft:binomial', n: 3, p: 0.5714286 }, add: false },
                { function: 'minecraft:set_count', count: 1.0, add: true },
            ] }],
            conditions: [{ condition: 'minecraft:block_state_property', block: row.block, properties: { age: '7' } }],
        }
    }

    // Part A: Fortune never changes a crop's yield. Every apply_bonus with minecraft:fortune in the table (on an entry,
    // a nested entry or the pool) goes: binomial_with_bonus_count(extra, p) becomes the Fortune-0 roll it already is,
    // set_count binomial(extra, p) with add (the bonus adds to the count), keeping the function's own conditions;
    // uniform_bonus_count and ore_drops are dropped (they add nothing at Fortune 0). Returns the number of terms changed.
    function stripFortune(node) {
        var n = 0
        if (Array.isArray(node)) {
            node.forEach(function (x) { n += stripFortune(x) })
            return n
        }
        if (!node || typeof node !== 'object') return 0
        if (Array.isArray(node.functions)) {
            node.functions = node.functions.map(function (f) {
                if (!f || f.function !== 'minecraft:apply_bonus' || f.enchantment !== 'minecraft:fortune') return f
                n++
                if (f.formula !== 'minecraft:binomial_with_bonus_count') return null
                var g = { function: 'minecraft:set_count', add: true,
                    count: { type: 'minecraft:binomial', n: f.parameters.extra, p: f.parameters.probability } }
                if (f.conditions) g.conditions = f.conditions
                return g
            }).filter(function (f) { return f != null })
        }
        Object.keys(node).forEach(function (k) {
            if (k !== 'functions' && node[k] && typeof node[k] === 'object') n += stripFortune(node[k])
        })
        return n
    }

    function fortuneFree() {
        if (!$Bases) return []
        var out = []
        var arr = $Bases.fortuneFree()
        for (var k = 0; k < arr.length; k++) out.push(String(arr[k]))
        return out
    }

    // Edits a table's raw pools (and table-level functions) as plain JS, then writes them back. fn(pools, functions)
    // returns true when it changed anything. modify() resets the table type, so it is set back to minecraft:block.
    function edit(event, table, fn) {
        if (!Platform.isLoaded(table.split(':')[0])) return
        try {
            event.modify(table, function (t) {
                var pools = JSON.parse(String(t.pools.toString()))
                var functions = t.functions ? JSON.parse(String(t.functions.toString())) : []
                if (fn(pools, functions)) {
                    t.pools = JsonIO.parseRaw(JSON.stringify(pools))
                    if (functions.length) t.functions = JsonIO.parseRaw(JSON.stringify(functions))
                }
                t.type = 'minecraft:block'
            })
        } catch (err) {
            console.warn(TAG + table + ': ' + err)
        }
    }

    ServerEvents.genericLootTables(function (event) {
        if (!global.CD) return
        var i = global.item
        var cfg = global.NCH.cfg(DEFAULTS, 'CROP_DROPS.SOIL_YIELD')
        var soilList = soils(i, cfg)
        var on = crops(i, global.blocks).filter(function (c) {
            if (!cropOn(c.key) || global.CD.absentIn(c.block)) return false
            c.base = base(c.key)
            if (!(c.base > 0)) {
                console.warn(TAG + c.key + ': no base in soil_yield.json, left off')
                return false
            }
            return true
        })
        var i2 = i, b2 = global.blocks

        // Part A first, so the soil pools (which never read Fortune) are appended to stripped tables.
        var stripped = 0, strippedTables = 0
        fortuneFree().forEach(function (table) {
            edit(event, table, function (pools, functions) {
                var n = stripFortune(pools) + stripFortune({ functions: functions })
                stripped += n
                if (n) strippedTables++
                return n > 0
            })
        })
        console.info(TAG + 'Fortune stripped: ' + stripped + ' terms on ' + strippedTables + ' crop tables')

        // Part G: seeds on a ripe cucumber and ghost pepper break.
        ripeSeeds(i2, b2).forEach(function (row) {
            if (global.CD.absentIn(row.block)) return
            var parts = row.block.split(':')
            edit(event, parts[0] + ':blocks/' + parts[1], function (pools) {
                pools.push(seedPool(row))
                return true
            })
        })

        if (!on.length || !soilList.length) {
            console.info(TAG + (on.length ? 'every soil at 0 bonus' : 'no crop switched on') + ': crop drops as their mods ship them')
            return
        }
        var done = []
        on.forEach(function (crop) {
            var parts = crop.block.split(':')
            edit(event, parts[0] + ':blocks/' + parts[1], function (pools) {
                var added = 0
                soilList.forEach(function (s) {
                    if (!s.nether != !crop.nether) return
                    pools.push(pool(crop, s, i2))
                    added++
                })
                if (added && done.indexOf(crop.key) < 0) done.push(crop.key)
                return added > 0
            })
        })
        console.info(TAG + done.length + ' crops (' + done.join(', ') + '), soils: ' + soilList.map(function (s) {
            return s.name + ' +' + Math.round(s.bonus * 100) + '%'
        }).join(', '))
    })

})()
