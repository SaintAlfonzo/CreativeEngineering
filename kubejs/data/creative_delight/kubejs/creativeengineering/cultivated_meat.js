// Reserved for Creative Cultivation: all cultivated-meat code (the sample syringe, the meat cultures, the culture blocks,
// their recipes, tags, lang and textures) is marked for a future own mod, Creative Cultivation, the way potion brewing
// and alcohol are marked for Creative Brewing (creative_delight.md 29.5). Until then it lives in Creative Delight's
// creativeengineering/ trees.
//
// Cultivated meat: the sample syringe, cultures from samples, culture growth, cutting and culture blocks. Moved from
// the pack's kubejs/server_scripts/cooking/cultivated_meat.js 2026-10-25, rows unchanged. Items and blocks:
// kubejs_startup/creativeengineering/cultures.js (CD jar); tags: cultivated_meat_tags.js beside this file.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	const F = global.CD.food(e)
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

		// Growth in the cooking pot, the same 1 -> 3 as the mixer: bone meal stands in for diluted bonemeal and Rustic
		// Delight's cooking oil for plant oil
		if (global.CD.on('rusticdelight')) {
			R.fdCooking([i.culture_beef, i.bonemeal, i.cooking_oil], Item.of(i.culture_beef, 3), e, 'beef_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_pork, i.bonemeal, i.cooking_oil], Item.of(i.culture_pork, 3), e, 'pork_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_poultry, i.bonemeal, i.cooking_oil], Item.of(i.culture_poultry, 3), e, 'poultry_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_mutton, i.bonemeal, i.cooking_oil], Item.of(i.culture_mutton, 3), e, 'mutton_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_rabbit, i.bonemeal, i.cooking_oil], Item.of(i.culture_rabbit, 3), e, 'rabbit_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_cod, i.bonemeal, i.cooking_oil], Item.of(i.culture_cod, 3), e, 'cod_culture_cooking', 0.35, 200)
			R.fdCooking([i.culture_salmon, i.bonemeal, i.cooking_oil], Item.of(i.culture_salmon, 3), e, 'salmon_culture_cooking', 0.35, 200)
		}

		// Cutting: the culture family (4 unless given; no bone meal from the fish cultures)
		F.cultureCut(i.culture_beef, i.raw_beef)
		F.cultureCut(i.culture_pork, i.raw_pork)
		F.cultureCut(i.culture_poultry, i.raw_chicken, 3)
		F.cultureCut(i.culture_mutton, i.raw_mutton)
		F.cultureCut(i.culture_rabbit, i.rabbit, 3)
		F.cultureCut(i.culture_cod, i.cod_slice, 8)
		F.cultureCut(i.culture_salmon, i.salmon_slice, 8)

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
