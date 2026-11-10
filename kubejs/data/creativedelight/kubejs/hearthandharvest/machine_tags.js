// requires: creativedelight
// requires: hearthandharvest
// Tags of the Hearth and Harvest block machines ported into the Creative Delight member hearthandharvest
// (hearth_and_harvest.md 3.1, 3.2). Bottle Crate membership: 1.4.1's short bottles mapped to pack ids (ominous bottle and
// its own cooking oil / syrup out, Rustic's in) plus every glass-bottle drink in kubejs/exported/food/drinks.json.
// The tall-wine entries are dropped (D10): Vinery's racks hold wines. Flat rendering for whatever has no 3D display model.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	t.add(e, i.t_hh_bottles, [
		i.glass_bottle, i.potion, i.splash_potion, i.lingering_potion, i.dragon_breath, i.experience_bottle, i.honey_bottle,
		i.farmersdelight_milk_bottle, i.neapolitan_milk_bottle, i.cooking_oil, i.syrup, i.chocolate_milk_bottle, i.goat_milk_bottle,
		i.brine_bottle, i.blueberry_juice, i.raspberry_juice, i.cherry_juice, i.sweet_berry_juice, i.glow_berry_juice,
		i.melon_juice, i.strong_melon_juice, i.hot_cocoa, i.strong_hot_cocoa, i.gamblers_tea, i.long_gamblers_tea,
		i.strong_gamblers_tea, i.cherry_cream_soda, i.passion_aloe_nectar, i.pickerelweed_juice
	])
	t.add(e, i.t_hh_crateable, [i.minecraft_egg, i.turtle_egg])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	t.add(e, '#minecraft:mineable/pickaxe', [i.fluid_jug, i.water_trough])
	t.add(e, '#minecraft:mineable/axe', [i.field_sprinkler, i.bottle_crate, i.stomping_basin])
})
