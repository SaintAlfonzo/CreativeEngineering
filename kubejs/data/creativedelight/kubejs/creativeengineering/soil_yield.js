// Soil yield: extra produce from a ripe crop grown on better soil (feature_writeup/crop_yields.md, TODO farming/crops #23).
// Each switched-on crop gets one pool per soil, appended to its harvest table: rolls = binomial(tries, chance), each roll
// one of the crop's produce, only when the crop is ripe and the block below it (offset rows below for two-high and water
// crops) is that soil. The crop's own pools are untouched, so a crop that is off, or on plain farmland at 0 tries, drops
// exactly what its mod ships. Values: CDConfig [crop_drops.soil_yield] (soil tries and chances; one switch per crop in
// [crop_drops.soil_yield.crops], all off by default). A crop whose key the config lacks stays off.
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
        farmlandTries: 0, farmlandChance: 0.0,
        richFarmlandTries: 2, richFarmlandChance: 0.35,
        fertileSoilTries: 2, fertileSoilChance: 0.25,
        richSoilTries: 2, richSoilChance: 0.35,
    }

    // key: CDConfig crop switch (SoilYield.CROPS, same order). block: the table's block. ripe: block state properties of
    // a ripe harvest. produce: item keys, one picked per roll. knife: what a roll gives instead when the crop is cut with a
    // knife, as the crop's own table does. below: rows from the crop down to its soil (default 1).
    function crops(i, b) {
        return [
            { key: 'wheat', block: b.wheat_crop, ripe: { age: '7' }, produce: [i.wheat] },
            { key: 'carrots', block: b.carrots, ripe: { age: '7' }, produce: [i.minecraft_carrot] },
            { key: 'potatoes', block: b.potatoes, ripe: { age: '7' }, produce: [i.minecraft_potato] },
            { key: 'beetroots', block: b.beetroots, ripe: { age: '3' }, produce: [i.minecraft_beetroot] },
            { key: 'cabbages', block: b.cabbages, ripe: { age: '7' }, produce: [i.farmersdelight_cabbage] },
            { key: 'onions', block: b.onions, ripe: { age: '7' }, produce: [i.onion_farmersdelight] },
            // The bottom vine only: a vine on a rope has no soil below.
            { key: 'tomatoes', block: b.tomatoes, ripe: { age: '3' }, produce: [i.farmersdelight_tomato] },
            // Panicles sit on the rice, which stands in water on the soil.
            { key: 'rice', block: b.rice_panicles, ripe: { age: '3' }, produce: [i.rice_panicle], knife: i.farmersdelight_rice, below: 2 },
            { key: 'bell_peppers', block: b.bell_peppers, ripe: { age: '7' },
                produce: [i.bell_pepper_red, i.bell_pepper_green, i.bell_pepper_yellow] },
            { key: 'coffee', block: b.coffee_crop, ripe: { age: '5' }, produce: [i.coffee_beans] },
            { key: 'cotton', block: b.cotton_crop, ripe: { age: '3' }, produce: [i.cotton_boll] },
            { key: 'ender_crop', block: b.ender_crop, ripe: { age: '7' }, produce: [i.ender_pearl] },
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
        ]
    }

    function soils(i, cfg) {
        return [
            { name: 'farmland', block: i.farmland, tries: cfg.farmlandTries, chance: cfg.farmlandChance },
            { name: 'rich farmland', block: i.rich_farmland, tries: cfg.richFarmlandTries, chance: cfg.richFarmlandChance },
            { name: 'fertile soil', block: i.fertile_soil, tries: cfg.fertileSoilTries, chance: cfg.fertileSoilChance },
            { name: 'rich soil', block: i.rich_soil, tries: cfg.richSoilTries, chance: cfg.richSoilChance },
        ].map(function (s) {
            s.tries = Math.round(Number(s.tries))
            s.chance = Number(s.chance)
            return s
        }).filter(function (s) { return s.tries > 0 && s.chance > 0 })
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

    function pool(crop, soil, i) {
        return {
            rolls: { type: 'minecraft:binomial', n: soil.tries, p: soil.chance },
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
            console.info(TAG + (on.length ? 'every soil at 0 tries' : 'no crop switched on') + ': crop drops as their mods ship them')
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
            return s.name + ' ' + s.tries + ' x ' + s.chance
        }).join(', '))
    })

})()
