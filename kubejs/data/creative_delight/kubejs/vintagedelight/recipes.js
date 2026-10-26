// requires: vintagedelight
// Vintage Delight recipe changes (feature_writeup/vintage_delight.md, TODO ports/vintageport). Creative Delight member
// script: recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes). The port's data stays as upstream ships
// it, so each change removes the upstream id and adds the pack recipe here.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('vintagedelight')) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Pickling Jar #########################
		// No slime block from the jar (vintageport #17); slime comes from Create (create/slime.js).
		R.remove({ id: 'vintagedelight:fermenting/slime_block_from_fermenting' }, e)

		// The two jar recipes that took only salt dust, rebuilt on #forge:salt (vintageport #10). Same inputs, time and
		// outputs as upstream.
		R.remove({ id: 'vintagedelight:fermenting/berserks_brew' }, e)
		R.vdPickling(Item.of(i.potion, '{Potion:"minecraft:water",CustomPotionEffects:[{Id:5,Amplifier:2,Duration:600},' +
			'{Id:11,Amplifier:3,Duration:600},{Id:20,Amplifier:0,Duration:400}],CustomPotionColor:11141120,' +
			'display:{Name:\'{"text":"Berserker\\\'s Brew","italic":false}\'}}'),
			[i.blaze_powder, i.salt, i.crimson_fungus, i.fermented_spider_eye, i.ghast_tear], e, 'berserks_brew',
			{ time: 400, container: i.glass_bottle })
		R.remove({ id: 'vintagedelight:fermenting/leather_from_fermenting' }, e)
		R.vdPickling(Item.of(i.leather, 2), [Item.of(i.rotten_flesh, 2), Item.of(i.tree_bark, 2), i.salt, i.vinegar], e,
			'leather_from_fermenting', { time: 4800, secondary: i.glass_bottle })

		//######################### Salted fish #########################
		// Salted cod and salmon are out (vintageport #11, removed_items.js); Surstromming stays. The Salt's Salted
		// cooked fish is a different thing and stays.
		R.remove({ id: 'vintagedelight:salted_cod' }, e)
		R.remove({ id: 'vintagedelight:salted_salmon' }, e)

		//######################### Wild ghost pepper seeds #########################
		// 2 seeds from the millstone and crushing wheels, 3 from the IE Crusher (were 1 each; vintageport #12).
		R.remove({ id: 'vintagedelight:create/milling/wild_ghost_peppers' }, e)
		R.remove({ id: 'vintagedelight:immersiveengineering/crusher/wild_ghost_pepper' }, e)
		R.createCrushing([Item.of(i.ghost_pepper_seeds, 2), i.cyan_dye], i.wild_ghost_peppers, e, 'wild_ghost_pepper', true)
		R.ieCrushing(Item.of(i.ghost_pepper_seeds, 3), i.wild_ghost_peppers, e, 'wild_ghost_pepper',
			[{ chance: 1.0, output: i.cyan_dye }], 3200)

		// Rows below: Creative Delight members #3, moved from the pack's server_scripts/cooking/cooking.js 2026-10-25, unchanged.

		//######################### Bread #########################
		// Vintage Delight's oat dough is a #forge:dough: no furnace bread either (its 3 raw oats -> bread craft stays, as wheat)
		R.remove({ id: 'vintagedelight:bread_from_smelting' }, e)
		R.remove({ id: 'vintagedelight:bread_from_smoking' }, e)

		//######################### Burgers #########################
		// The pack's burger rule (creativeengineering/recipes.js): shaped, any #forge:bread, cheese never milk; the assembly is the only
		// automation. Pattern: toppings on top, patty in the middle, bread at the bottom.
		;[
			'vintagedelight:cheese_burger', 'vintagedelight:deluxe_burger',
			'veggiesdelight:compat/vintagedelight/cheese_burger', 'veggiesdelight:compat/vintagedelight/deluxe_burger',
		].forEach(id => R.remove({ id: id }, e))
		// Vintage Delight's two burgers (vintage_delight.md 4 step 9): its cheese slice, so neither grid matches Delightful's
		// cheeseburgers (wedges) while both lines ship; beef or Veggies Delight's vegetarian patty (Veggies' compat versions).
		// The assemblies start on the slice; the deluxe one on the patty, like the egg burger
		var vtdPatty = [i.beef_patty, i.cooked_vegetarian_patty]
		;[
			// [key, pattern, keys, assembly base, deploy steps in order]
			['cheese_burger', ['STO', 'CP ', ' B '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, C: i.cheese_slice, P: vtdPatty, B: i.t_bread },
				i.t_bread, [i.cheese_slice, vtdPatty, i.salad_ingredients, i.tomato, i.onion]],
			['deluxe_burger', ['STO', 'APE', 'CB '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, A: i.cooked_bacon, P: vtdPatty, E: i.cooked_eggs, C: i.cheese_slice, B: i.t_bread },
				i.beef_patty, [i.cheese_slice, i.cooked_eggs, i.cooked_bacon, i.salad_ingredients, i.tomato, i.onion, i.t_bread]],
		].forEach(([key, pattern, keys, base, steps]) => {
			R.shaped(i[key], pattern, keys, e, key + '_manual_only')
			var inc = R.incomplete(key)
			R.createSequencedAssembly(i[key], base,
				steps.map(s => e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])),
				[key], e, key + '_sequenced_assembly', 1)
		})
	})
})
