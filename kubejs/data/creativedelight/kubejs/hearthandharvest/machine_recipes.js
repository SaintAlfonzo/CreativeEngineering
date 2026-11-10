// requires: creativedelight
// requires: hearthandharvest
// Recipes of the Hearth and Harvest block machines (hearth_and_harvest.md 3.1, 3.2). Recipe ids are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {
		// The blocks, 1.4.1's shapes with the section 3.1 input mapping (vanilla inputs are unchanged)
		R.shaped(i.fluid_jug, [' I ', 'IBI', 'IBI'], {I: i.ingot_iron, B: i.bucket}, e, 'fluid_jug')
		R.shapeless(i.fluid_jug, [i.fluid_jug], e, 'fluid_jug_cleaning') // crafts away the contents (1.4.1 jug_cleaning)
		R.shaped(i.water_trough, ['n n', 'iii'], {n: i.nugget_iron, i: i.ingot_iron}, e, 'water_trough')
		R.shaped(i.field_sprinkler, ['CCC', ' C ', 'SSS'], {C: i.ingot_copper, S: i.wood_slab}, e, 'field_sprinkler')
		R.shaped(Item.of(i.bottle_crate, 2), ['sss', 'www'], {s: i.stick, w: i.wood_slab}, e, 'bottle_crate')
		R.shaped(i.stomping_basin, ['C C', 'PSP'], {C: i.ingot_copper, P: i.planks, S: i.wood_slab}, e, 'stomping_basin')

		// Stomping Basin, the eval set (D12, N26-6): berry, cherry, glow berry, melon and oil recipes at 1.4.1's amounts.
		// Apple cider and the grape juices are not ported; no slime, sponge, sugar or other extras before the eval.
		R.hhStomping({ id: f.blueberry_juice, amount: 500 }, [{ id: i.t_fruits_blueberry, count: 4 }], e, 'blueberry_juice')
		R.hhStomping({ id: f.raspberry_juice, amount: 500 }, [{ id: i.t_fruits_raspberries, count: 4 }], e, 'raspberry_juice')
		R.hhStomping({ id: f.sweet_berry_juice, amount: 500 }, [{ id: i.sweet_berries, count: 4 }], e, 'sweet_berry_juice')
		R.hhStomping({ id: f.cherry_juice, amount: 500 }, [{ id: i.cherry, count: 4 }], e, 'cherry_juice')
		R.hhStomping({ id: f.glow_berry_juice, amount: 500 }, [{ id: i.glow_berries, count: 4 }], e, 'glow_berry_juice')
		R.hhStomping({ id: f.melon_juice, amount: 500 }, [{ id: i.melon, count: 4 }], e, 'melon_juice')
		R.hhStomping({ id: f.seed_oil, amount: 1000 }, [{ id: i.t_crops_corn, count: 4 }], e, 'seed_oil_from_corn')
		R.hhStomping({ id: f.seed_oil, amount: 500 }, [{ id: i.peanut, count: 4 }], e, 'seed_oil_from_peanuts')
		R.hhStomping({ id: f.seed_oil, amount: 125 }, [{ id: i.t_seeds, count: 8 }], e, 'seed_oil_from_seeds')
	})
})
