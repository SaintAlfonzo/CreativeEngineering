// requires: creativedelight
// requires: hearthandharvest
// Block tags of the Hearth and Harvest worldgen port (hearth_and_harvest.md 2.2, N23-8). Rates and biomes are in the
// pack tables: wild_flowers.json, wild_crops.json, structure_groups.json.

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var b = global.blocks

	t.add(e, '#minecraft:small_flowers', [b.hh_four_leaf_clover])
})
