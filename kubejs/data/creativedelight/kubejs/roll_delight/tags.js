// requires: roll_delight
// Roll Delight 1.3.1 port tags (feature_writeup/roll_delight.md 3.1, 3.2, 3.3.1, 3.3.3, 4 steps 3, 14, 16). Food types
// come from food_types.json (handheld, golden, cooked/veggie), not from here.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('roll_delight')) return
	var i = global.item
	var t = global.tags

	// Nether Roll meat: any raw pork (raw porkchop, FD raw bacon) or Prepared Flesh (3.1)
	t.add(e, i.t_nether_roll_meat, [i.raw_pork, i.prepared_flesh])

	//######################### Chopstick foods (3.3) #########################
	// Read by the member's chopstick tooltip and its Finish-event hook. An explicit list (3.3.1, 3.3.3), not the roll
	// family tag: that family holds foods that are out (cabbage rolls, sweet rolls). Canned forms are never in it.
	// By rule: the four fish family tags (every raw / cooked whole fish and every raw / cooked cut), so a fish that
	// leaves the pack drops out by itself.
	t.add(e, i.t_chopstick_foods, [i.t_food_family_fish_raw, i.t_food_family_fish_raw_cut, i.t_food_family_fish_cooked,
		i.t_food_family_fish_cooked_cut])

	// G5 begin: FOODS is filtered by Item.exists below, then handed to global.tags
	var FOODS = [
		// Roll Delight
		i.rice_roll, i.rice_roll_slice, i.amazing_salmon_roll, i.amazing_salmon_roll_slice, i.mushroom_roll,
		i.mushroom_roll_slice, i.ancient_roll, i.ancient_roll_slice, i.golden_roll, i.golden_roll_slice, i.nether_roll,
		i.tobiko_roll, i.cooked_nether_wart,
		// 3.3 table rows: FD slices raw and cooked (Fish Tempura removed 2026-11-02), Hostile Guardian Roll and Sashimi, Ocean's rolls, Pufferfish
		// (typed hazard, so no fish tag holds it; it keeps its poison, chopsticks lengthen that too, 3.3.3)
		i.salmon_slice, i.cod_slice, i.salmon_slice_cooked, i.cod_slice_cooked, i.guardianroll,
		i.guardiansashimi, i.fugu_roll, i.elder_guardian_roll, i.pufferfish,
		// Rustic Delight (3.3.1)
		i.bell_pepper_roll_green, i.bell_pepper_roll_red, i.bell_pepper_roll_yellow, i.bell_pepper_roll_orange,
		i.bell_pepper_roll_white, i.bell_pepper_roll_pink, i.bell_pepper_roll_blue, i.bell_pepper_roll_purple,
		i.bell_pepper_roll_black, i.cherry_blossom_roll, i.calamari_roll, i.calamari, i.cooked_calamari, i.calamari_slice,
		i.cooked_calamari_slice, i.spring_rolls, i.fried_calamari, i.calamari_soup,
		// Oceanic Delight (3.3.1); i.caviar is oceanic_delight:salmon_eggs
		i.egg_roll, i.shrimp_roll, i.glow_squid_stew, i.squid_and_pickles, i.ancient_fish_eggs, i.plate_of_stuffed_pufferfish,
		i.sea_pickle_roll, i.sea_pickle_roll_slice, i.caviar, i.fish_egg_roll, i.fish_egg_roll_slice, i.sea_pickle_slices,
		i.fish_egg_noodle_soup, i.shrimp, i.shrimp_slices, i.fried_shrimp, i.elder_guardian_eye, i.pasta_with_eyeball,
		// Miner's Delight squid line (3.3.1)
		i.glow_ink_pasta, i.bowl_of_stuffed_squid, i.takoyaki, i.squid, i.glow_squid, i.baked_squid,
		// the pack's glowing calamari line (3.3.1)
		i.glowing_calamari, i.glowing_calamari_slice, i.glowing_calamari_roll,
		// noodle and pasta sweep (3.3.1)
		i.duck_noodles, i.kangaroo_pasta, i.lobster_pasta, i.cheesy_pasta, i.pearl_pasta, i.pasta_with_meatballs,
		i.pasta_with_mutton, i.raw_pasta, i.squid_ink_pasta, i.vegetable_noodles, i.noodle_soup, i.truffle_pasta, i.glowpasta,
		i.pasta_with_veggieballs, i.creamy_pasta_with_chicken_cuts, i.creamy_pasta_with_ham, i.crab_pasta,
		i.bell_pepper_pasta, i.pasta_with_broccoli, i.cheese_pasta, i.cucumber_noodles, i.pad_thai,
		// rice, FD and others (3.3.1)
		i.kelp_roll, i.kelp_roll_slice, i.salmon_roll, i.cod_roll, i.dumplings, i.cooked_rice, i.fried_rice,
		i.garlic_rice_with_cauliflower, i.shrimp_fried_rice, i.ender_paella,
		// round six by id (3.3.3)
		i.shrimp_rice, i.bowl_of_paella, i.globular_rice, i.bowl_of_pineapple_fried_rice, i.mushroom_rice,
		i.rice_and_vegetables, i.cooked_rice_with_beef, i.cooked_rice_with_chicken_cuts, i.cooked_rice_with_porkchop,
		i.squid_rings, i.stuffed_shulker_bowl, i.crab_rangoon, i.weird_caviar, i.sea_grape, i.braised_sea_pickle,
		i.honey_fried_kelp, i.dried_kelp, i.cooked_crab, i.cooked_whole_crab, i.lobster_tail_cooked, i.shrimp_stew,
	]
	// G5 end
	// End's Delight is to be removed (3.3.3): optional entries, added only while the mod's items are registered
	var OPTIONAL = [
		i.ender_noodle, i.ender_congee, i.ender_bamboo_rice, i.stir_fried_shulker_meat, i.raw_dragon_meat_cuts,
		i.roasted_dragon_meat_cuts, i.stuffed_rice_cake,
	]
	// Only registered ids go in; a listed id that is not (a mod removed, a renamed item) is logged once per tag load
	var missing = []
	var present = FOODS.filter(id => {
		if (id && Item.exists(id)) return true
		missing.push(String(id))
		return false
	})
	t.add(e, i.t_chopstick_foods, present.concat(OPTIONAL.filter(id => id && Item.exists(id))))
	if (missing.length) console.warn('[roll_delight] chopstick foods not registered: ' + missing.join(', '))
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('roll_delight')) return
	var b = global.blocks
	var t = global.tags
	// Medleys break like the FD feasts (knife, BlockRunner's cake and feast tier); wrapped chopsticks are bamboo (axe)
	var MEDLEYS = [b.legendary_roll_medley, b.mythical_roll_medley]
	t.add(e, b.t_fd_mineable_knife, MEDLEYS)
	t.add(e, b.t_blockrunner_confectionery, MEDLEYS)
	t.mineable(e, 'axe', null, b.wrapped_chopsticks)
})
