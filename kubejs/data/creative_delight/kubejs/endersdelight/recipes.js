// requires: endersdelight
// Ender's Delight recipe changes: chorus juice (the pack's chorus juice fluid is its liquid form), grid routes,
// assemblies. Non-member file (2.1): recipe ids are creative_delight:<type>/<suffix> (global.CD.recipes). Moved from
// the pack's kubejs/server_scripts/cooking/cooking.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var f = global.fluid
	var R = global.NCH

	global.CD.recipes(() => {

		//######################### Fluids #########################
		//Chorus juice
		// The pack's chorus juice fluid (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/fluids.js, TODO food/drinks #4). Grid: Ender's Delight's
		// 1 fruit + sugar + bottle (kept). Pot: 2 fruit + sugar -> 2 bottles (manual only: the fluid is the mixer route).
		// Create mixer, heated: 1 fruit + sugar + 250 mB water -> 250 mB; spout, drain and IE Bottling Machine at 250 mB.
		R.fdCooking([i.chorus_fruit, i.chorus_fruit, i.any_sweetener_drink], Item.of(i.chorus_juice, 2), e, 'chorus_juice_manual_only', 0.35, 200, i.glass_bottle)
		if (Platform.isLoaded('create')) {
			R.createMixing(Fluid.of(f.chorus_juice, 250), [i.chorus_fruit, i.any_sweetener_drink, Fluid.of(f.water, 250)], e, 'chorus_juice', 'heated')
			R.createFilling(i.chorus_juice, [i.glass_bottle, Fluid.of(f.chorus_juice, 250)], e, 'chorus_juice')
			R.createEmptying([i.glass_bottle, Fluid.of(f.chorus_juice, 250)], i.chorus_juice, e, 'chorus_juice')
		}
		if (Platform.isLoaded('immersiveengineering')) {
			R.ieBottling(i.chorus_juice, f.t_chorus_juice, i.glass_bottle, e, undefined, 250)
		}

		//######################### Crates #########################
		// One unpack route for both chorus crates (#forge:storage_blocks/chorus_fruit: this mod's and End's; ends_delight.md 3.1)
		R.remove({ id: 'endersdelight:chorus_fruit_from_crate' }, e)
		R.shapeless(Item.of(i.chorus_fruit, 9), [i.t_storage_chorus_fruit], e, 'chorus_fruit_from_crate')

		//######################### Shulker bowls #########################
		// One shulker shell -> 12 bowls (Ender's Delight's gave 8; TODO recipes/cooking #4)
		R.remove({ id: 'endersdelight:shulker_bowl' }, e)
		R.shapeless(Item.of(i.shulker_bowl, 12), [i.shulker_shell], e, 'shulker_bowl')

		//######################### Meals #########################
		// Heatless meal rule: farmersdelight/recipes.js "Meals". Expensive grid routes for pot meals (TODO dishes/meals #4):
		// chorus and endermite stews come in Ender's Delight's shulker bowl.
		R.shapeless(i.chorus_stew, [i.popped_chorus_fruit, i.popped_chorus_fruit, i.chorus_flower, i.shulker_bowl], e, 'chorus_stew_cooked_manual_only')
		R.shapeless(i.endermite_stew, [i.enderman, i.mite_crust, i.mite_crust, i.shulker_bowl], e, 'endermite_stew_cooked_manual_only')

		// Assembly meals (TODO dishes/meals #3)
		// Crispy skewer: shaped, a diagonal skewer
		R.remove({ id: 'endersdelight:crispy_skewer' }, e)
		R.shaped(i.crispy_skewer, [' MC', ' P ', 'S  '], { M: i.mite_crust, C: i.chorus_fruit, P: i.popped_chorus_fruit, S: i.stick },
			e, 'crispy_skewer_manual_only')
		if (Platform.isLoaded('create')) {
			var incCrispySkewer = R.incomplete('crispy_skewer')
			R.createSequencedAssembly(i.crispy_skewer, i.stick, [
				e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.chorus_fruit]),
				e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.popped_chorus_fruit]),
				e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.mite_crust])
			], ['crispy_skewer'], e, 'crispy_skewer_sequenced_assembly', 1)
		}
		// Crawling sandwich: shaped
		R.remove({ id: 'endersdelight:crawling_sandwich' }, e)
		R.shaped(i.crawling_sandwich, ['SMC', ' B '], { S: i.shulker_loot, M: i.mite_crust, C: i.chorus_fruit, B: i.t_bread },
			e, 'crawling_sandwich_manual_only')
		if (Platform.isLoaded('create')) {
			var incCrawlingSandwich = R.incomplete('crawling_sandwich')
			R.createSequencedAssembly(i.crawling_sandwich, i.t_bread, [
				e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.shulker_loot]),
				e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.mite_crust]),
				e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.chorus_fruit])
			], ['crawling_sandwich'], e, 'crawling_sandwich_sequenced_assembly', 1)
		}

		//######################### Ender #########################
		//Ender food changes
		//Remove wood bowl items
		R.remove({ id: 'endersdelight:twisted_cereal_wood' }, e)
		// (Quark's chorus fruit block removals: quark/recipes.js)
		// Stuffed shulker shapeless
		R.shapeless(Item.of(i.stuffed_shulker, 1), [i.tomato_sauce, i.tomato_sauce, i.cooked_rice, i.cooked_rice, i.brown_mushroom, i.chorus_fruit, i.shulker_mollusk, i.shulker_shell], e, 'stuffed_shulker_cooked_manual_only')
		//Ender paella shapeless
		R.shapeless(Item.of(i.ender_paella, 1), [i.cooked_rice, i.cooked_rice, i.shulker_loot, i.chorus_flower, i.chorus_flower, i.ender_sight, i.shulker_bowl], e, 'ender_paella_cooked_manual_only')
		//Pearl pasta shapeless
		R.shapeless(Item.of(i.pearl_pasta, 1), [i.enderman, i.pasta, i.pasta, i.chorus_juice, i.chorus_juice, i.shulker_bowl], e, 'pearl_pasta_cooked_manual_only')
	})
})
