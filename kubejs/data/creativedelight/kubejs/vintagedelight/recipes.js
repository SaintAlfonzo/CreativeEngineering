// requires: creativedelight
// requires: vintagedelight
// Vintage Delight recipe changes (feature_writeup/vintage_delight.md, TODO ports/vintageport). Creative Delight member
// script: recipe ids are creativedelight:<type>/<suffix> (global.CD.recipes). The port's data stays as upstream ships
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
		R.vdPickling(Item.of(i.leather, 2), [Item.of(i.rotten_flesh, 2), Item.of(i.any_bark, 2), i.salt, i.vinegar_jar], e,
			'leather_from_fermenting', { time: 4800 }) // the jar's leftover (empty mason jar) comes back through the leftover slots
		// Fermented spider eye: the sugar slot takes the plain sweetener tag like the crafting recipe (minecraft/recipes.js;
		// replaceInput does not reach this recipe type). Rest as upstream.
		R.remove({ id: 'vintagedelight:fermenting/fermented_spider_eye_from_fermenting' }, e)
		R.vdPickling(Item.of(i.fermented_spider_eye, 5), [Item.of(i.spider_eye, 5), i.any_sweetener_plain], e,
			'fermented_spider_eye_from_fermenting', { time: 1200 })
		// Pickled Sea Pickle (dishes/pickles-and-ferments #1): the Pickle recipe's pattern, 5 sea pickles + salt -> 5, no
		// honey. It replaces Brewin's Pickled Pickles (merged, vintage_items.js); their honey recipe is gone from alcohol.js.
		R.vdPickling(Item.of(i.pickled_sea_pickle, 5), [Item.of(i.sea_pickle, 5), i.salt], e, 'pickled_sea_pickle')

		// Pickle: upstream's 5 cucumbers + salt -> 5 pickles, each cucumber slot also takes a zucchini (the zucchini pickles into
		// the standard pickle; Veggies Delight only).
		R.remove({ id: 'vintagedelight:fermenting/pickle_from_fermenting' }, e)
		var PICKLE_VEG = Platform.isLoaded('veggiesdelight') ? [i.cucumber, i.zucchini] : i.cucumber
		R.vdPickling(Item.of(i.pickle, 5), [PICKLE_VEG, PICKLE_VEG, PICKLE_VEG, PICKLE_VEG, PICKLE_VEG, i.salt], e,
			'pickle_from_fermenting', { time: 2400 })

		//######################### Kimchi #########################
		// One kimchi (merges/dishes #9, owner 2026-11-02): Vintage's kimchi wins, Brewin's is removed (vintage_items.js). Beside
		// upstream's ghost pepper recipe (kept), one recipe per pickleable in the pepper's slot: 4 cabbage leaves + salt + it
		// -> 4 kimchi, upstream's time; no pickled pepper on the side (no pepper goes in).
		var kimchiBases = [
			['cucumber', Platform.isLoaded('veggiesdelight') ? [i.cucumber, i.zucchini] : i.cucumber],
			['onion', i.onion],
			['egg', i.eggs],
			['sea_pickle', i.sea_pickle],
			['beetroot', i.beetroot],
			['fish', i.t_whole_raw_fish]
		]
		kimchiBases.forEach(b => {
			R.vdPickling(Item.of(i.vintagedelight_kimchi, 4), [Item.of(i.cabbage_leaf, 4), i.salt, b[1]], e, 'kimchi_from_' + b[0])
		})

		//######################### Nut mash #########################
		// One nut butter (merges/dishes #7, owner 2026-11-02): Vintage's nut mash, mason jar only. The bottle and Delightful's
		// nut butter merge into the jar (swaps); the bottle <-> jar crafts go first, or the merge's swap would turn them into
		// jar -> 3 jars. Pot: 5 nuts (acorn or peanut, raw or roasted) + sugar -> 3 jars, each in an empty mason jar
		// (was 5 roasted peanuts -> 1).
		;['vintagedelight:cooking/nut_mash_jar', 'vintagedelight:jam/nut_mash_jar', 'vintagedelight:jam/nut_mash_jar_deconstruct']
			.forEach(id => R.remove({ id: id }, e))
		var NUT = [i.t_nuts_acorn, i.roasted_acorn, i.peanut, i.roasted_peanut].filter(n => String(n).charAt(0) === '#' || Item.exists(n))
		R.fdCooking([NUT, NUT, NUT, NUT, NUT, i.any_sugar], Item.of(i.nut_mash_jar, 3), e, 'nut_mash_jar', 2.0, 400, i.empty_mason_jar)

		//######################### Pickle soup #########################
		// Pickle soup (dishes/pickles-and-ferments #3): the pickle slot takes any pickled veggie (#creativedelight:pickled_veg,
		// tags.js), and the carrot and potato slots any vegetable (the veggie variant). Beetroot, salad ingredient, time and
		// xp as upstream.
		R.remove({ id: 'vintagedelight:cooking/pickle_soup' }, e)
		R.fdCooking([i.t_pickled_veg, i.vegetables, i.vegetables, i.beetroot, i.salad_ingredients], i.pickle_soup, e,
			'pickle_soup', 1.0, 200)

		//######################### One salt #########################
		// The Salt's salt and Vintage's salt block are merged (vintage_items.js; swaps in the pack's item_merges.js). The
		// swap leaves two 9 <-> 1 pairs: Vintage's packing (any #forge:salt) and The Salt's unpacking stay. The salt bucket
		// is gone with both its recipes.
		R.remove({ id: 'salt:salt_packing' }, e)
		R.remove({ id: 'vintagedelight:salt_block_deconstruct' }, e)
		R.remove({ id: 'vintagedelight:salt_from_smelting' }, e)
		R.remove({ id: 'vintagedelight:salt_bucket_to_salt' }, e)

		// The 3 froglight jar recipes took Vintage's salt block, which no longer exists as an item: rebuilt on The Salt's
		// block (the merged one). Same inputs, time and outputs otherwise.
		var froglights = [
			['ochre', i.ochre_froglight, i.blaze_powder, i.magma_cream],
			['verdant', i.verdant_froglight, i.glowstone, i.glow_berries],
			['pearlescent', i.pearlescent_froglight, i.prismarine_crystals, i.sea_pickle]
		]
		froglights.forEach(f => {
			var id = f[0] + '_froglight_from_fermenting'
			R.remove({ id: 'vintagedelight:fermenting/' + id }, e)
			R.vdPickling(Item.of(f[1], 3), [Item.of(i.slime_block, 2), f[2], f[3], i.salt_salt_block, i.vinegar_jar], e, id,
				{ time: 2400 }) // vinegar jar: its empty mason jar returns as the leftover
		})

		// Tactical Fishing writes a catch recipe for every bucket item. No salt bucket (removed) and no powdered sugar
		// bucket catch.
		R.remove({ id: 'tactical_fishing:fishing_vintagedelight_salt_bucket' }, e)
		R.remove({ id: 'tactical_fishing:fishing_creativeengineering_powdered_sugar_bucket' }, e)

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
		// Oat dough is a #forge:dough: no furnace, smoker or campfire bread (its 3 raw oats -> bread craft stays, as wheat). It bakes
		// into bread in the oven like every dough (farmersdelight/recipes.js "Bread from dough").
		R.remove({ id: 'vintagedelight:bread_from_smelting' }, e)
		R.remove({ id: 'vintagedelight:bread_from_smoking' }, e)

		//######################### Veggie Noodles #########################
		// The cucumber noodles are Veggie Noodles (client namechanges.js; TODO dishes/meals #43): the cucumber dough
		// (cut into the noodles) takes cucumber, broccoli, zucchini or its slice, raw cactus steak or dandelion leaves
		R.remove({ id: 'vintagedelight:cucumber_dough' }, e)
		R.shapeless(Item.of(i.cucumber_dough, 2), [i.t_veggie_noodle_veg, i.any_dough], e, 'cucumber_dough')

		//######################### Overnight oats #########################
		// A craft beside the Pickling Jar recipe (TODO dishes/meals #22): 3 raw oats, a fruit slice, a berry, milk, bowl
		R.shapeless(i.overnight_oats, [i.raw_oats, i.raw_oats, i.raw_oats, i.t_fruit_salad_slice, i.t_berries, i.milk, i.bowl], e, 'overnight_oats')

		//######################### Burgers #########################
		// The pack's burger rule (creativedelight/recipes.js): shaped, any #forge:bread, cheese never milk; the assembly is the only
		// automation. Pattern: toppings on top, patty in the middle, bread at the bottom.
		;[
			'vintagedelight:cheese_burger', 'vintagedelight:deluxe_burger',
			'veggiesdelight:compat/vintagedelight/cheese_burger', 'veggiesdelight:compat/vintagedelight/deluxe_burger',
		].forEach(id => R.remove({ id: id }, e))
		// Vintage Delight's two burgers (vintage_delight.md 4 step 9): its cheese slice, so neither grid matches Delightful's
		// cheeseburgers (wedges) while both lines ship; beef or Veggies Delight's veggie patty (Veggies' compat versions).
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
