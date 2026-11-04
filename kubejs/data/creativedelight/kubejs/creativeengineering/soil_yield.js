// Soil yield: extra produce from a ripe crop grown on better soil (feature_writeup/crop_yields.md, TODO farming/crops #23).
// Each switched-on crop gets one pool per soil, appended to its harvest table: rolls = binomial sized to bonus x the
// crop's average produce (Rich +33%, Fertile Soil +66% by default), each roll
// one of the crop's produce, only when the crop is ripe and the block below it (offset rows below for two-high and water
// crops) is that soil. The crop's own pools are untouched, so a crop that is off, or on plain farmland at 0 bonus, drops
// exactly what its mod ships. Values: CDConfig [crop_drops.soil_yield] (one bonus per soil; one switch per crop in
// [crop_drops.soil_yield.crops], all on by default). A crop whose key the config lacks stays off. Ender Crop is out: it
// only grows on tilled End Stone.
//
// Same pre-roll mechanism as rusticdelight/crop_drops.js: ServerEvents.genericLootTables rewrites the raw table JSON
// before the game parses it. New pools are only appended (Forge names pools by position), and modify() resets the table
// type, so it is set back to minecraft:block. The bell pepper rewrite finds its pools by their entries; these pools
// have three entries and no random_chance, so it never mistakes one for its own.
//
// Loot only: a crop picked by its own right-click (FD tomato vines on ropes, rice panicles' rice, Vintage's cucumber and
// ghost pepper, Veggies' zucchini) does not roll its table and gets nothing from the soil. Quark's right-click replant
// and every break do roll it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

    var TAG = '[soil-yield] '

    var DEFAULTS = {
        farmlandBonus: 0.0,
        richFarmlandBonus: 0.33,
        fertileSoilBonus: 0.66,
        richSoilBonus: 0.33,
    }

    // key: CDConfig crop switch (SoilYield.CROPS, same order). base: the table's average produce per ripe harvest, no
    // Fortune (crop_yields.md 2.1, loot dump 2026-10-26); the soil pool adds bonus x base. block: the table's block. ripe: block state properties of
    // a ripe harvest. produce: item keys, one picked per roll. knife: what a roll gives instead when the crop is cut with a
    // knife, as the crop's own table does. below: rows from the crop down to its soil (default 1).
    function crops(i, b) {
        return [
            { key: 'wheat', base: 1.00, block: b.wheat_crop, ripe: { age: '7' }, produce: [i.wheat] },
            { key: 'carrots', base: 3.71, block: b.carrots, ripe: { age: '7' }, produce: [i.minecraft_carrot] },
            { key: 'potatoes', base: 3.71, block: b.potatoes, ripe: { age: '7' }, produce: [i.minecraft_potato] },
            { key: 'beetroots', base: 1.00, block: b.beetroots, ripe: { age: '3' }, produce: [i.minecraft_beetroot] },
            { key: 'cabbages', base: 1.00, block: b.cabbages, ripe: { age: '7' }, produce: [i.farmersdelight_cabbage] },
            { key: 'onions', base: 3.71, block: b.onions, ripe: { age: '7' }, produce: [i.onion_farmersdelight] },
            // The bottom vine only: a vine on a rope has no soil below.
            { key: 'tomatoes', base: 1.50, block: b.tomatoes, ripe: { age: '3' }, produce: [i.farmersdelight_tomato] },
            // Panicles sit on the rice, which stands in water on the soil.
            { key: 'rice', base: 1.00, block: b.rice_panicles, ripe: { age: '3' }, produce: [i.rice_panicle], knife: i.farmersdelight_rice, below: 2 },
            { key: 'bell_peppers', base: 1.45, block: b.bell_peppers, ripe: { age: '7' },
                produce: [i.bell_pepper_red, i.bell_pepper_green, i.bell_pepper_yellow] },
            { key: 'coffee', base: 3.50, block: b.coffee_crop, ripe: { age: '5' }, produce: [i.coffee_beans] },
            { key: 'cotton', base: 2.00, block: b.cotton_crop, ripe: { age: '3' }, produce: [i.cotton_boll] },
            { key: 'tobacco', base: 6.71, block: b.tobacco_crop, ripe: { blockstate: '7' }, produce: [i.tobacco_leafs] },
            { key: 'flax', base: 1.00, block: b.flax_crop, ripe: { age: '7', half: 'lower' }, produce: [i.flax] },
            // Fibre drops from the grown bottom and from the top; the pool goes on the top (the part players cut).
            { key: 'hemp', base: 2.88, block: b.hemp, ripe: { growth: 'top0' }, produce: [i.hemp_fiber], below: 2 },
            // Both halves drop corn; the pool goes on the lower half only.
            { key: 'corn', base: 1.00, block: b.corn_crop, ripe: { age: '7', upper: 'false' }, produce: [i.corn] },
            { key: 'cave_carrots', base: 3.00, block: b.cave_carrots, ripe: { age: '3' }, produce: [i.cave_carrot] },
            { key: 'broccoli', base: 1.00, block: b.broccoli_crop, ripe: { age: '5' }, produce: [i.broccoli] },
            { key: 'cauliflower', base: 1.00, block: b.cauliflower_crop, ripe: { age: '7' }, produce: [i.cauliflower] },
            { key: 'garlic', base: 2.14, block: b.garlic_crop, ripe: { age: '5' }, produce: [i.garlic] },
            { key: 'sweet_potato', base: 2.71, block: b.sweet_potato_crop, ripe: { age: '5' }, produce: [i.sweet_potato] },
            { key: 'turnip', base: 1.00, block: b.turnip_crop, ripe: { age: '5' }, produce: [i.turnip] },
            { key: 'zucchini', base: 2.71, block: b.zucchini_crop, ripe: { age: '7' }, produce: [i.zucchini] },
            { key: 'cucumber', base: 1.00, block: b.cucumber_crop, ripe: { age: '7' }, produce: [i.cucumber] },
            { key: 'oats', base: 1.00, block: b.oat_crop, ripe: { age: '7' }, produce: [i.oat], knife: i.raw_oats },
            { key: 'peanuts', base: 3.71, block: b.peanut_crop, ripe: { age: '7' }, produce: [i.peanut] },
            { key: 'adzuki', base: 2.50, block: b.adzuki_sprouts, ripe: { age: '6', flowering: 'true' }, produce: [i.adzuki_beans] },
        ]
    }

    function soils(i, cfg) {
        return [
            { name: 'farmland', block: i.farmland, bonus: cfg.farmlandBonus },
            { name: 'rich farmland', block: i.rich_farmland, bonus: cfg.richFarmlandBonus },
            { name: 'fertile soil', block: i.fertile_soil, bonus: cfg.fertileSoilBonus },
            { name: 'rich soil', block: i.rich_soil, bonus: cfg.richSoilBonus },
        ].map(function (s) {
            s.bonus = Number(s.bonus)
            return s
        }).filter(function (s) { return s.bonus > 0 })
    }

    var $SoilYield = (function () {
        try {
            return Java.loadClass('com.SaintAlfonzo.creativedelight.CDConfig').CROP_DROPS.SOIL_YIELD
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
        return {
            rolls: rolls(crop, soil),
            bonus_rolls: 0.0,
            entries: crop.produce.map(function (item) { return entry(crop, item, i) }),
            conditions: [
                { condition: 'minecraft:block_state_property', block: crop.block, properties: crop.ripe },
                { condition: 'minecraft:location_check', offsetY: -(crop.below || 1), predicate: { block: { blocks: [soil.block] } } },
            ],
        }
    }

    ServerEvents.genericLootTables(function (event) {
        if (!global.CD) return
        var i = global.item
        var cfg = global.NCH.cfg(DEFAULTS, 'CROP_DROPS.SOIL_YIELD')
        var soilList = soils(i, cfg)
        var on = crops(i, global.blocks).filter(function (c) { return cropOn(c.key) })
        if (!on.length || !soilList.length) {
            console.info(TAG + (on.length ? 'every soil at 0 bonus' : 'no crop switched on') + ': crop drops as their mods ship them')
            return
        }
        var done = []
        on.forEach(function (crop) {
            var parts = crop.block.split(':')
            var table = parts[0] + ':blocks/' + parts[1]
            if (!Platform.isLoaded(parts[0])) return
            try {
                event.modify(table, function (t) {
                    var pools = JSON.parse(String(t.pools.toString()))
                    soilList.forEach(function (s) { pools.push(pool(crop, s, i)) })
                    t.pools = JsonIO.parseRaw(JSON.stringify(pools))
                    t.type = 'minecraft:block'
                    done.push(crop.key)
                })
            } catch (err) {
                console.warn(TAG + table + ': ' + err)
            }
        })
        console.info(TAG + done.length + ' crops (' + done.join(', ') + '), soils: ' + soilList.map(function (s) {
            return s.name + ' +' + Math.round(s.bonus * 100) + '%'
        }).join(', '))
    })

})()
