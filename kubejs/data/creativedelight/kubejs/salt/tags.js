// requires: salt
// The Salt's saltable tag (#salt:can_be_salted, i.t_can_be_salted): the Salting craft takes any food in it
// (salt.md 5, TODO food/seasoning #4 and #1, 2026-11-03).
// Added: every cooked meat and fish and every meat or fish dish that upstream's tag misses (151 foods: food_families.json
// members whose recipes take a meat or fish, or that are cooked meat or fish themselves; Rustic Delight's calamari roll
// and Delightful's crab rangoon among them). Left out: cookies, puddings and ice cream made from mob parts, Miner's
// Delight's crunchy and nutritional bars, Ocean's Delight's raw stuffed cod. Each mod's entries only when it is loaded.
// Removed: the intermediates bone broth, cooked rice and tomato sauce (food/seasoning #1).
// Other files add single foods next to their mod (moredelight/tags.js, veggiesdelight/tags.js).
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var i = global.item
	var t = global.tags
	var MEAT_DISHES = [ // G5 begin: filtered by mod below, then handed to global.tags
		// alexsdelight
		i.bison_burger, i.bison_patty, i.bunfungus_sandwich, i.bison_cooked, i.bunfungus_cooked,
		i.bunfungus_drumstick_cooked, i.catfish_slice_cooked, i.centipede_leg_cooked, i.kangaroo_shank_cooked,
		i.moose_rib_cooked_loose, i.kangaroo_pasta, i.kangaroo_stew, i.lobster_pasta,
		// alexsmobs
		i.catfish_cooked_mob, i.kangaroo_meat_cooked, i.lobster_tail_cooked, i.moose_ribs_cooked, i.kangaroo_burger,
		i.shrimp_fried_rice,
		// brewinandchewin
		i.cheesy_pasta, i.horror_lasagna,
		// buzzier_bees
		i.glazed_porkchop,
		// corn_delight
		i.classic_corn_dog, i.corn_dog, i.corn_soup, i.taco,
		// delightful
		i.crab_rangoon, i.sinigang, i.wrapped_cantaloupe,
		// endersdelight
		i.crawling_sandwich, i.crispy_skewer, i.ender_paella, i.endermite_stew, i.steak_fries,
		// ends_delight
		i.dried_endermite_meat, i.end_barbecue_stick, i.ender_bamboo_rice, i.ender_congee, i.ender_noodle, i.ender_sausage,
		i.enderman_gristle_stew, i.roasted_dragon_meat, i.roasted_dragon_meat_cuts, i.roasted_dragon_steak,
		i.roasted_shulker_meat, i.roasted_shulker_meat_slice, i.shulker_soup, i.smoked_dragon_leg,
		i.stir_fried_shulker_meat,
		// fruitsdelight
		i.fig_chicken_stew, i.lychee_chicken, i.orange_chicken, i.orange_marinated_pork, i.pineapple_marinated_pork,
		// fungidelight
		i.mutton_chops_with_truffle, i.pork_marsala_with_mushrooms, i.rabbit_with_morels, i.steak_with_mushrooms,
		i.stuffed_morels,
		// hostiledelight
		i.aurorasoup, i.carrotmite, i.cookedguardianfillet, i.cookedrottenham, i.cookendermite, i.glimmeringchowder,
		i.glowpasta, i.greenpie, i.grilledghast, i.guardianburger, i.guardianross, i.guardiansandwich, i.guardiansashimi,
		i.guardiansoup, i.guardiantartar, i.miteymeal, i.rottenpie, i.suspiciousbaconandaggs,
		// miners_delight
		i.baked_spider_leg, i.bat_rolls, i.bat_soup, i.cave_hamburger, i.cooked_arthropod, i.glow_ink_pasta,
		i.insect_sandwich, i.insect_stew, i.insect_wrap, i.seasoned_arthropods, i.squid_sandwich,
		// moredelight
		i.chicken_salad, i.chicken_sandwich_with_egg_and_tomato, i.cooked_rice_with_beef, i.cooked_rice_with_chicken_cuts,
		i.cooked_rice_with_porkchop, i.creamy_pasta_with_chicken_cuts, i.creamy_pasta_with_ham, i.diced_potatoes_with_beef,
		i.diced_potatoes_with_chicken_cuts, i.diced_potatoes_with_porkchop, i.egg_with_bacon_sandwich,
		i.hamburger_with_egg, i.loaded_hamburger, i.porkchop_sandwich, i.simple_hamburger, i.steak_sandwich,
		// oceanic_delight
		i.caviar_toast, i.fish_egg_noodle_soup, i.fish_egg_roll, i.fish_egg_roll_slice, i.fried_shrimp, i.globular_rice,
		i.pasta_with_eyeball, i.seafood_skewer, i.shrimp_chips, i.shrimp_rice, i.shrimp_roll, i.shrimp_stew,
		i.steamed_beef,
		// oceansdelight
		i.cabbage_wrapped_elder_guardian, i.cooked_elder_guardian_slice, i.cooked_guardian_tail, i.cooked_stuffed_cod,
		i.elder_guardian_roll, i.fugu_roll,
		// quarkdelight
		i.cooked_whole_crab, i.crab_pasta, i.miso_with_bamboo_sprouts,
		// rusticdelight
		i.calamari_roll, i.coffee_braised_beef, i.fried_calamari, i.fried_chicken, i.spring_rolls,
		i.stuffed_bell_pepper_green, i.stuffed_bell_pepper_red, i.stuffed_bell_pepper_yellow,
		// veggiesdelight
		i.cacciatore, i.chicken_fajitas_wrap, i.fish_and_chips, i.garlic_baked_cod, i.garlic_chicken_stew,
		i.steak_and_broccoli, i.stuffed_zucchini_boat, i.stuffed_zucchinis, i.turnip_beef_stew, i.turnip_cake,
		i.turnip_mutton_skewer,
		// vintagedelight
		i.cheese_burger, i.deluxe_burger, i.ghostly_chili, i.pad_thai, i.stuffed_burrito, i.surstromming
	] // G5 end
	var add = MEAT_DISHES.filter(id => typeof id === 'string' && Platform.isLoaded(id.split(':')[0]))
	t.add(e, i.t_can_be_salted, add)
	t.remove(e, i.t_can_be_salted, [i.bone_broth, i.cooked_rice, i.tomato_sauce])
})
