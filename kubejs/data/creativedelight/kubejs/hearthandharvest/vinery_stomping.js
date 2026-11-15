// requires: creativedelight
// requires: hearthandharvest
// requires: vinery
// Stomping Basin mirrors of every Vinery Grapevine Pot pressing (plan_bugfix_1114 3.10, owner 2026-11-14). The pot turns
// 3 grapes of one type and an empty wine bottle into that type's juice bottle (GrapevinePotBlock GRAPES_PER_BOTTLE, takeJuice).
// The list is Vinery's GrapeTypeRegistry with a fruit and a bottle (read 2026-11-14): red, white, savanna, taiga and jungle,
// red and white each. Hops are a lattice type with no juice and are skipped. No other Vinery stomping exists: the donor
// jars' recipes are fermenting, apple mashing (the Apple Press) and crafting. A new grape type needs a row here.
// Four ingredients, so only a 2x2 basin group holds them (a single basin has one slot).
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		;[
			[i.red_grape,           i.red_grapejuice_wine_bottle,      'red_grapejuice'],
			[i.white_grape,         i.white_grapejuice_wine_bottle,    'white_grapejuice'],
			[i.savanna_grapes_red,  i.savanna_red_grapejuice_bottle,   'savanna_red_grapejuice'],
			[i.savanna_grapes_white, i.savanna_white_grapejuice_bottle, 'savanna_white_grapejuice'],
			[i.taiga_grapes_red,    i.taiga_red_grapejuice_bottle,     'taiga_red_grapejuice'],
			[i.taiga_grapes_white,  i.taiga_white_grapejuice_bottle,   'taiga_white_grapejuice'],
			[i.jungle_grapes_red,   i.jungle_red_grapejuice_bottle,    'jungle_red_grapejuice'],
			[i.jungle_grapes_white, i.jungle_white_grapejuice_bottle,  'jungle_white_grapejuice']
		].forEach(g => R.hhStomping(null, [{ id: g[0], count: 3 }, i.wine_bottle], e, g[2], g[1]))
	})
})
