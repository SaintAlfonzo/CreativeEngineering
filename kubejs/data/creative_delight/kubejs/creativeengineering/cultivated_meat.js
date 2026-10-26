// Cultivated meat: the sample syringe, cultures from samples, culture growth, cutting and culture blocks. Moved from
// the pack's kubejs/server_scripts/cooking/cultivated_meat.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	const F = global.foodFamily(e)
	var f = global.fluid
	var i = global.item

	global.CD.recipes(() => {

		// ============================================================
		// Syringe
		// ============================================================

		R.shaped(Item.of(i.sample_syringe, 1), ['RG ','G G', ' GP'], {R: i.rod_iron, G: i.glass_pane, P: i.plate_iron}, e)

		// ============================================================
		// Sample recipes
		// ============================================================

		i.culture_base = '#creativeengineering:culture_base';

		R.shapeless(Item.of(i.culture_beef, 1), [i.cow_sample_syringe, i.culture_base, i.slime], e, 'beef_sample')
		R.shapeless(Item.of(i.culture_pork, 1), [i.pig_sample_syringe, i.culture_base, i.slime], e, 'pork_sample')
		R.shapeless(Item.of(i.culture_poultry, 1), [i.chicken_sample_syringe, i.culture_base, i.slime], e, 'poultry_sample')
		R.shapeless(Item.of(i.culture_mutton, 1), [i.sheep_sample_syringe, i.culture_base, i.slime], e, 'mutton_sample')
		R.shapeless(Item.of(i.culture_rabbit, 1), [i.rabbit_sample_syringe, i.culture_base, i.slime], e, 'rabbit_sample')
		R.shapeless(Item.of(i.culture_cod, 1), [i.cod_sample_syringe, i.culture_base, i.slime], e, 'cod_sample')
		R.shapeless(Item.of(i.culture_salmon, 1), [i.salmon_sample_syringe, i.culture_base, i.slime], e, 'salmon_sample')

		R.createMixing(Item.of(i.culture_beef, 3), [i.culture_beef, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'beef_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_pork, 3), [i.culture_pork, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'pork_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_poultry, 3), [i.culture_poultry, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'poultry_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_mutton, 3), [i.culture_mutton, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'mutton_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_rabbit, 3), [i.culture_rabbit, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'rabbit_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_cod, 3), [i.culture_cod, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'cod_culture_mixing', 'heated')
		R.createMixing(Item.of(i.culture_salmon, 3), [i.culture_salmon, Fluid.of(f.diluted_bonemeal, 250), Fluid.of(f.plantoil, 250)], e, 'salmon_culture_mixing', 'heated')

		F.meatCut(i.culture_beef, i.raw_beef, 4)
		F.meatCut(i.culture_pork, i.raw_pork, 4)
		F.meatCut(i.culture_poultry, i.raw_chicken, 3)
		F.meatCut(i.culture_mutton, i.raw_mutton, 4)
		F.meatCut(i.culture_rabbit, i.rabbit, 3)
		F.fishSlice(i.culture_cod, i.cod_slice, 8)
		F.fishSlice(i.culture_salmon, i.salmon_slice, 8)

		// ============================================================
		// Culture blocks (9 <-> 1)
		// ============================================================

		R.shapeless(Item.of(i.culture_block_beef, 1), [Item.of(i.culture_beef, 9)], e)
		R.shapeless(Item.of(i.culture_beef, 9), [Item.of(i.culture_block_beef, 1)], e)
		R.shapeless(Item.of(i.culture_block_pork, 1), [Item.of(i.culture_pork, 9)], e)
		R.shapeless(Item.of(i.culture_pork, 9), [Item.of(i.culture_block_pork, 1)], e)
		R.shapeless(Item.of(i.culture_block_mutton, 1), [Item.of(i.culture_mutton, 9)], e)
		R.shapeless(Item.of(i.culture_mutton, 9), [Item.of(i.culture_block_mutton, 1)], e)
		R.shapeless(Item.of(i.culture_block_poultry, 1), [Item.of(i.culture_poultry, 9)], e)
		R.shapeless(Item.of(i.culture_poultry, 9), [Item.of(i.culture_block_poultry, 1)], e)
		R.shapeless(Item.of(i.culture_block_rabbit, 1), [Item.of(i.culture_rabbit, 9)], e)
		R.shapeless(Item.of(i.culture_rabbit, 9), [Item.of(i.culture_block_rabbit, 1)], e)
		R.shapeless(Item.of(i.culture_block_cod, 1), [Item.of(i.culture_cod, 9)], e)
		R.shapeless(Item.of(i.culture_cod, 9), [Item.of(i.culture_block_cod, 1)], e)
		R.shapeless(Item.of(i.culture_block_salmon, 1), [Item.of(i.culture_salmon, 9)], e)
		R.shapeless(Item.of(i.culture_salmon, 9), [Item.of(i.culture_block_salmon, 1)], e)
	})
})
