// requires: creativedelight
// requires: hearthandharvest
// Recipes for Hearth and Harvest content ported into the Creative Delight member hearthandharvest (hearth_and_harvest.md
// 4.0, 4.1). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		// Raspberry Pips, the bush's planting item: 1 Raspberry -> 1 Pips (1.4.1 with Berry Good, shapeless)
		R.shapeless(i.raspberry_pips, [i.raspberry], e, 'raspberry_pips_from_raspberry')

		// Watering cans (4.0): copper is 1.4.1's recipe (5 copper ingots around a bucket). The tiers are smithing upgrades,
		// which keep the can's water and fertiliser (vanilla smithing copies the base's NBT).
		R.shaped(Item.of(i.watering_can), ['C  ', 'CBC', 'CC '], {C: i.ingot_copper, B: i.bucket}, e, 'watering_can')
		R.smithing(i.iron_watering_can, i.watering_can, i.block_iron, e, 'iron_watering_can')
		R.smithing(i.golden_watering_can, i.iron_watering_can, i.block_gold, e, 'golden_watering_can')
		R.smithing(i.diamond_watering_can, i.golden_watering_can, i.diamond, e, 'diamond_watering_can')
		R.smithing(i.netherite_watering_can, i.diamond_watering_can, i.ingot_netherite, e, 'netherite_watering_can')
		R.smithing(i.dragon_watering_can, i.netherite_watering_can, i.dragon_scale, e, 'dragon_watering_can')
	})
})
