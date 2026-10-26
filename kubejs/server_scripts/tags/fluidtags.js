ServerEvents.tags('fluid', e => {
	var t = global.tags
	var f = global.fluid

	// ── forge:fluid + a category subtag ────────────────────────────────────
	// [ subtag , fluid(s) ]
	;[
		// vanilla
		['forge:fluid/water', f.water],
		['forge:fluid/lava', f.lava],
		['forge:fluid/milk', f.milk],

		// food / drink
		['forge:fluid/tea', f.tea],
		['forge:fluid/apple_cider', f.apple_cider],
		['forge:fluid/long_apple_cider', f.long_apple_cider],
		['forge:fluid/strong_apple_cider', f.strong_apple_cider],
		['forge:fluid/honey', f.honey],
		['forge:fluid/chocolate/milk', f.chocolate],
		['forge:fluid/chocolate/dark', f.black_chocolate],
		['forge:fluid/chocolate/white', f.white_chocolate],
		['forge:fluid/chocolate/ruby', f.ruby_chocolate],
		['forge:fluid/hot_chocolate', f.hot_chocolate],
		['forge:fluid/caramel', f.caramel],
		['forge:fluid/tomato_sauce', f.tomato_sauce],
		['forge:fluid/melon_juice', f.melon_juice],
		['forge:fluid/chorus_juice', f.chorus_juice],
		['forge:fluid/carrot_juice', f.carrot_juice],
		['forge:fluid/dandelion_juice', f.dandelion_juice],
		['forge:fluid/turnip_water', f.turnip_water],
		['forge:fluid/egg_solution', f.egg_solution],
		['forge:fluid/frosting', f.frosting],
		['forge:fluid/milk_coffee', f.milk_coffee],
		['forge:fluid/honey_coffee', f.honey_coffee],
		['forge:fluid/chocolate_coffee', f.chocolate_coffee],
		['forge:fluid/dark_coffee', f.dark_coffee],
		['forge:fluid/syrup_coffee', f.syrup_coffee],
		['forge:fluid/pumpkin_coffee', f.pumpkin_coffee],
		['forge:fluid/cherry_blossom_coffee', f.cherry_blossom_coffee],
		['forge:fluid/lavender_tea', f.lavender_tea],
		['forge:fluid/matcha_latte', f.matcha_latte],
		['forge:fluid/berry_matcha_latte', f.berry_matcha_latte],
		['forge:fluid/yeast_culture', f.yeast_culture],

		// plant growth
		['forge:fluid/diluted_bonemeal', f.diluted_bonemeal],
		['forge:fluid/fertilizer', f.fertilizer],
		['forge:fluid/herbicide', f.herbicide],

		// crafting / industrial
		['forge:fluid/slime', f.slime],
		['forge:fluid/acid/redstone', f.redstone_acid],
		['forge:fluid/acid/etching', f.etching_acid],
		['forge:fluid/acetaldehyde', f.acetaldehyde],
		['forge:fluid/phenolic_resin', f.phenolic_resin],
		['forge:fluid/concrete', f.concrete],
		['forge:fluid/plastic', f.plastic],

 		// creosote
		['forge:fluid/creosote', f.creosote],

		// oil chain
		['forge:fluid/plant_oil', f.plantoil],
		['forge:fluid/diesel_sulfur', f.diesel_sulfur],
		['forge:fluid/lubricant_cracked', f.lubricant_cracked],
		['forge:fluid/gasoline_additives', f.gasoline_additives],
		['forge:fluid/naphtha', f.naphtha],
		['forge:fluid/naphtha_cracked', f.naphtha_cracked],
		['forge:fluid/napalm', f.napalm],
		['forge:fluid/benzene', f.benzene],
		['forge:fluid/propylene', f.propylene],
		['forge:fluid/ethylene', f.ethylene],
		['forge:fluid/lpg', f.lpg],

		// magic / potion
		['forge:fluid/dragon_breath', f.dragon_breath],
		['forge:fluid/ink', f.ink],
		['forge:fluid/potion', f.create_potion],
		['forge:fluid/xp', f.experience],
		['forge:fluid/hyper_xp', f.hyper_experience],
	].forEach(([tag, fluid]) => t.addFluid(e, tag, fluid))

	// Remove IE potion tag
	t.remove(e, 'forge:fluid/potion', f.ie_potion)

		// ── XP fluid merge: Create's Liquid Experience is the ONLY #forge:experience ──
	;['create_enchantment_industry:experience',
	  'create_enchantment_industry:flowing_experience']
		.forEach(fl => e.add('forge:experience', fl))

	;['pneumaticcraft:memory_essence', 'pneumaticcraft:memory_essence_flowing',
	  'sophisticatedcore:xp_still', 'sophisticatedcore:xp_flowing']
		.forEach(fl => e.remove('forge:experience', fl))


	// ── forge:fluid only (no meaningful subtag) ────────────────────────────
	/*T.addFluid(e, 'forge:fluid', [
		f.seed_oil, f.vegetable_oil,
		f.bioethanol, f.ie_biodiesel, f.pc_biodiesel,
		f.ie_ethanol, f.pc_ethanol,
		f.crude_oil, f.pc_oil,
		f.ip_kerosene, f.pc_kerosene,
		f.ip_diesel, f.pc_diesel,
		f.ip_lubricant, f.pc_lubricant,
		f.ip_gasoline, f.pc_gasoline,
		f.experience, f.memory_essence,
	])*/

	// ── fixes / removals ──────────────────────────────────────────────────
	var realWater = [f.water, f.flowing_water]
	var badWaterTagged = e.get('minecraft:water').getObjectIds()
		.map(rl => rl.toString())
		.filter(id => realWater.indexOf(id) === -1)

	if (badWaterTagged.length > 0) {
		t.remove(e, 'minecraft:water', badWaterTagged)
		console.log('[fluidtags] stripped minecraft:water tag from: ' + badWaterTagged.join(', '))
	}

	t.clearAll(e, [f.tmted_melon_juice, f.tmted_tomato_sauce, f.tmted_apple_cider, f.thatch]) // dead placeholder fluids
})
