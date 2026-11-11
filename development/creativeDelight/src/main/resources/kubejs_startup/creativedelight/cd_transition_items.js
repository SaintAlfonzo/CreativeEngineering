// Creative Delight's sequenced assembly incomplete items (the food rows). Moved from Creative Engineering's
// changes/transition_items.js on 2026-11-06 (plan_cb_publish.md 15): CD's assemblies looked them up there, so without
// the Engineering jar every one failed (`incomplete('...') not registered`). Ids unchanged: 'kubejs:incomplete_<key>'
// whichever script registers them. Row format and texture rules: see Engineering's transition_items.js header.
//
//   const inc = R.incomplete('cod_roll')
//   R.createSequencedAssembly(out, in, [...], ['cod_roll'], e, 'any_recipe_id_you_want')

StartupEvents.registry('item', e => {
	const R = global.NCH
	const SEQUENCED_ASSEMBLIES = [
		// cooking
		['crab_bars',                'quarkdelight:items/crab_bars'],
		['cod_roll',                 'farmersdelight:item/cod_roll'],
		['salmon_roll',              'farmersdelight:item/salmon_roll'],
		['kelp_roll',                'farmersdelight:item/kelp_roll'],
		['rice_medley',              'farmersdelight:item/rice_roll_medley_block'],
		// Roll Delight port (CD roll_delight/recipes.js, roll_delight.md 4 step 8)
		['rice_roll'],
		['amazing_salmon_roll'],
		['mushroom_roll'],
		['ancient_roll'],
		['tobiko_roll',              'roll_delight:item/tobiko_roll_slice'],   // the mod's tobiko roll model uses this sprite
		['nether_roll'],
		['legendary_roll_medley'],
		['mythical_roll_medley'],
		// Large Meals port (CD largemeals/recipes.js, large_meals.md 4 step 8): the feast assemblies, keyed by the serving
		['omurice'],
		['mushroom_pot_pie'],
		['roasted_mutton_rack'],
		['bbq_stick'],
		['grilled_salmon'],
		['bacon_and_eggs'],
		['roasted_mutton_chops'],
		['enchanted_golden_apple'],
		// Golden Food: enchanted forms by the apple's assembly (CD goldenfood/golden.js, golden_food.md 3.4). Their item
		// models point at the golden form's sprite, so the texture is named.
		['enchanted_golden_carrot',                  'minecraft:item/golden_carrot'],
		['enchanted_golden_melon_slice',             'goldenfood:item/golden_melon_slice'],
		['enchanted_golden_sweet_berries',           'goldenfood:item/golden_sweet_berries'],
		['enchanted_golden_glow_berries',            'goldenfood:item/golden_glow_berries'],
		['enchanted_golden_chorus_fruit',            'goldenfood:item/golden_chorus_fruit'],
		['enchanted_golden_baked_potato',            'goldenfood:item/golden_baked_potato'],
		['enchanted_golden_beetroot',                'goldenfood:item/golden_beetroot'],
		['enchanted_golden_dried_kelp',              'goldenfood:item/golden_dried_kelp'],
		['enchanted_golden_cooked_beef',             'goldenfood:item/golden_cooked_beef'],
		['enchanted_golden_cooked_porkchop',         'goldenfood:item/golden_cooked_porkchop'],
		['enchanted_golden_cooked_mutton',           'goldenfood:item/golden_cooked_mutton'],
		['enchanted_golden_cooked_chicken',          'goldenfood:item/golden_cooked_chicken'],
		['enchanted_golden_cooked_rabbit',           'goldenfood:item/golden_cooked_rabbit'],
		['enchanted_golden_cooked_cod',              'goldenfood:item/golden_cooked_cod'],
		['enchanted_golden_cooked_salmon',           'goldenfood:item/golden_cooked_salmon'],
		['enchanted_golden_bread',                   'goldenfood:item/golden_bread'],
		['enchanted_golden_cookie',                  'goldenfood:item/golden_cookie'],
		['enchanted_golden_pumpkin_pie',             'goldenfood:item/golden_pumpkin_pie'],
		['enchanted_golden_milk_bucket',             'goldenfood:item/golden_milk_bucket'],
		['enchanted_golden_honey_bottle',            'goldenfood:item/golden_honey_bottle'],
		['ham_and_cheese_sandwich'],                                 // alcohol.js (alcohol.md 8.6)
		['quiche'],
		['zucchini_quiche'],                                         // CD veggiesdelight/recipes.js (food_families.md quiche/whole)
		['vegan_pizza'],                                             // CD veggiesdelight/recipes.js (creativedelight/members #30)
		['pizza',                    'brewinandchewin:item/pizza'],  // Supreme Pizza, CD brewinandchewin/recipes.js (creativedelight/members #20)
		// Rustic Delight rolls (CD rusticdelight/recipes.js, creativedelight/members #26)
		['bell_pepper_roll_green'], ['bell_pepper_roll_red'], ['bell_pepper_roll_yellow'],
		['bell_pepper_roll_orange'], ['bell_pepper_roll_white'], ['bell_pepper_roll_pink'],
		['bell_pepper_roll_blue'], ['bell_pepper_roll_purple'], ['bell_pepper_roll_black'],
		['cherry_blossom_roll'], ['calamari_roll'], ['glowing_calamari_roll'], ['rice_roll_royale'],
		['stuffed_zucchini_boat'],
		['stuffed_zucchinis'],
		['turnip_mutton_skewer'],
		['vegetarian_burger'],
		// meals (cooking.js "Meals", TODO dishes/meals #2, #3)
		['mutton_wrap'],
		['chicken_fajitas_wrap'],                                    // Veggies Delight (veggies_delight.md 4.7)
		['vegetables_wrap'],
		['fruit_salad'],
		['hamburger'],
		['cheeseburger'],
		['deluxe_cheeseburger'],
		['simple_hamburger'],
		['hamburger_with_egg'],
		['loaded_hamburger'],
		['bison_burger'],
		['kangaroo_burger'],
		['cheese_burger'],                                           // Vintage Delight (vintage_delight.md 4 step 9)
		['deluxe_burger'],
		['stuffed_burrito'],
		['shepherds_pie'],
		['horror_lasagna'],
		['stuffed_potato'],
		['crispy_skewer'],
		['kebab_wrap'],                                               // Barbeque's Delight combos (plan_meals_rolls_bbq.md C8)
		['kebab_sandwich'],
		['crawling_sandwich'],
		['steak_and_potatoes']
	]
	SEQUENCED_ASSEMBLIES.forEach(([key, texture, color]) => R.registerIncomplete(e, key, texture, color))
	console.log(`[creativedelight] transition items (${SEQUENCED_ASSEMBLIES.length})`)
})
