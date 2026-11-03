// One incomplete / transition item per sequenced assembly (and per Engineer's-Workbench chain
// that reuses the same "in-progress" sprite).
//
// Add a row here for each sequenced assembly, then reference it in the recipe by passing its key
// wrapped in an array as the transitionalItem argument (the key is independent of the recipe id):
//
//   const inc = R.incomplete('rose_gold_rod')            // id for the sequence steps
//   R.createSequencedAssembly(out, in, [
//       e.recipes.create.deploying(inc, [inc, ...]),
//   ], ['rose_gold_rod'], e, 'any_recipe_id_you_want')
//
// Row: [key, texture?, colorHex?]
//   texture  - OMIT to auto-derive a grey-tinted copy of the output item's sprite
//              (global.item[key] -> '<ns>:item/<path>'). Pass a string when that guess is wrong:
//              block items (-> '<ns>:block/<name>'), mods using 'items/', a stand-in sprite, etc.
//   colorHex - tint applied to layer 0, defaults to '#808080'.
//
// Registered item id is always 'kubejs:incomplete_<key>'.

StartupEvents.registry('item', e => {
	const R = global.NCH
	console.log('transition items starting')

	// TODO
	// Add "const incComponentSteel = R.incomplete('component_steel')" definitions by file to item dict


	// ---- static rows -------------------------------------------------------------------------
	const SEQUENCED_ASSEMBLIES = [
		// key                       texture override (omit = auto from global.item[key])
		['item',                     'vinery:item/mash'],          // generic shared fallback (i.incomplete)

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
		['enchanted_golden_mushroom_stew',           'goldenfood:item/golden_mushroom_stew'],
		['enchanted_golden_beetroot_soup',           'goldenfood:item/golden_beetroot_soup'],
		['enchanted_golden_rabbit_stew',             'goldenfood:item/golden_rabbit_stew'],
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
		['crawling_sandwich'],
		['steak_and_potatoes'],

		// redstone
		['redstone_torch'],
		['daylight_sensor',          'minecraft:block/daylight_detector'],
		['rose_quartz_lamp',         'create:block/rose_quartz_lamp'],
		['transmitter'],
		['redstone_link',            'create:block/redstone_link'],
		['repeater'],
		['fast_repeater'],
		['slow_repeater'],
		['comparator'],
		['redstone_randomizer'],
		['rain_detector'],
		['thunder_detector'],

		// crystal
		['budding_amethyst',         'minecraft:block/budding_amethyst'],
		['echo_shard'],

		// end gear — echo sequenced assembly (server_scripts/magic/end_gear.js).
		// Create: Deep Dark keeps its sprites under 'items/' (not 'item/') and names the
		// armour sprites 'echo_shard_<piece>', so those five need an explicit texture; the
		// four creativeengineering:* echo tools (echo_tools.js) auto-derive fine.
		['echo_sword',               'create_deep_dark:items/echo_sword'],
		['echo_pickaxe'],
		['echo_axe'],
		['echo_shovel'],
		['echo_hoe'],
		['echo_helmet',              'create_deep_dark:items/echo_shard_helmet'],
		['echo_chestplate',          'create_deep_dark:items/echo_shard_chestplate'],
		['echo_leggings',            'create_deep_dark:items/echo_shard_leggings'],
		['echo_boots',               'create_deep_dark:items/echo_shard_boots'],
		['echo_shield',              'minecraft:entity/shield_base'],

		// end gear — felsic sequenced assembly (server_scripts/equipment/end_gear.js).
		// Felsic Gear keeps its sprites under 'items/' (not 'item/'), so all nine need an
		// explicit texture, same as ingot_felsic above.
		['felsic_helmet',            'felsic_gear:items/felsic_helmet'],
		['felsic_chestplate',        'felsic_gear:items/felsic_chestplate'],
		['felsic_leggings',          'felsic_gear:items/felsic_leggings'],
		['felsic_boots',             'felsic_gear:items/felsic_boots'],
		['felsic_sword',             'felsic_gear:items/felsic_sword'],
		['felsic_pickaxe',           'felsic_gear:items/felsic_pickaxe'],
		['felsic_axe',               'felsic_gear:items/felsic_axe'],
		['felsic_shovel',            'felsic_gear:items/felsic_shovel'],
		['felsic_hoe',               'felsic_gear:items/felsic_hoe'],
		['felsic_shield',            'minecraft:entity/shield_base'],

		// end gear — dragon sequenced assembly (server_scripts/equipment/end_gear.js).
		// dragonloot keeps its sprites under 'item/' (not 'items/'), so all fourteen auto-derive fine.
		['dragon_sword'],
		['dragon_pickaxe'],
		['dragon_axe'],
		['dragon_shovel'],
		['dragon_hoe'],
		['dragon_helmet'],
		['dragon_chestplate'],
		['dragon_leggings'],
		['dragon_boots'],
		['dragon_bow'],
		['dragon_crossbow'],
		['dragon_horse_armor'],
		['dragon_trident'],
		['dragon_anvil'],
		['dragon_shield',            'minecraft:entity/shield_base'],
		['air_brake'],

		// transport
		['rail'],
		['ll_spring'],

		// metal
		['ingot_felsic',             'felsic_gear:items/felsic_ingot'],
		['spool'],
		['chain'],
		['copper_bars',              'minecraft:block/iron_bars'],
		['iron_bars',                'minecraft:block/iron_bars'],
		['cast_iron_bars',           'minecraft:block/iron_bars'],
		['gold_bars',                'minecraft:block/iron_bars'],
		['zinc_bars',                'minecraft:block/iron_bars'],
		['brass_bars',               'minecraft:block/iron_bars'],
		// IE components (server_scripts/engineering/components.js) — IE's item sprite is
		// 'material_component_<m>', not 'component_<m>', so the auto-guess would 404.
		['component_iron',           'immersiveengineering:item/material_component_iron'],
		['component_steel',          'immersiveengineering:item/material_component_steel'],
		['amethyst_lens'],
	]

	// ---- wire coils + coil blocks + pack components ----------------------------------------
	// Keep COIL_METALS in sync with server_scripts/metal/metal_base_materials.js.
	//   coil_<m>       : wire-coil-in-progress  — grey-tint of the wire coil's item sprite, taken
	//                    from i.wire_coil_<m> (the dict has one key per id since 2026-10-27, so
	//                    there is no i.coil_<m> to auto-derive from; the incomplete id stays
	//                    kubejs:incomplete_coil_<m>). creativeengineering:* for the 9 pack coils,
	//                    immersiveengineering:* for copper / steel / electrum.
	//   coil_block_<m> : coil-block-in-progress — the block's own side texture; the coil-block
	//                    item model is a 3D block, so there is no flat item sprite to derive from.
	//                    copper -> IE coil_lv, electrum -> IE coil_mv (placeholder until a
	//                    creativeengineering:coil_electrum block exists); the other 10 (incl.
	//                    steel) -> the pack's own coil block.
	const COIL_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'brass', 'constantan', 'steel', 'electrum']
	const coilSprite = m => String(global.item['wire_coil_' + m]).replace(':', ':item/')
	const coilBlockSide = m =>
		m === 'copper'   ? 'immersiveengineering:block/metal_decoration/coil_lv_side' :
		m === 'electrum' ? 'immersiveengineering:block/metal_decoration/coil_mv_side' :
		                   'creativeengineering:block/coil_block/coil_' + m + '_side'

	COIL_METALS.forEach(m => {
		SEQUENCED_ASSEMBLIES.push(['coil_' + m, coilSprite(m)])
		SEQUENCED_ASSEMBLIES.push(['coil_block_' + m, coilBlockSide(m)])
	})

	// HV wire coil (i.wirecoil_hv = immersiveengineering:wirecoil_steel, the electrical steel
	// coil, distinct from the coil_steel structural rope above) + its paired HV coil block
	// (i.coil_block_hv = immersiveengineering:coil_hv, IE's own block - no flat item sprite).
	SEQUENCED_ASSEMBLIES.push(['wirecoil_hv'])
	SEQUENCED_ASSEMBLIES.push(['coil_block_hv', 'immersiveengineering:block/metal_decoration/coil_hv_side'])

	// The 13 creativeengineering:component_<m> items (startup_scripts/items/components.js).
	// Each has its own creativeengineering:item/component_<m> sprite → auto-derive is correct.
	// (iron + steel are IE items, handled as explicit rows above.)
	const COMPONENT_METALS = ['copper', 'gold', 'diamond', 'zinc', 'netherite', 'aluminum',
		'lead', 'silver', 'nickel', 'uranium', 'brass', 'electrum', 'constantan']
	COMPONENT_METALS.forEach(m => SEQUENCED_ASSEMBLIES.push(['component_' + m]))

	// ---- register --------------------------------------------------------------------------
	SEQUENCED_ASSEMBLIES.forEach(([key, texture, color]) => R.registerIncomplete(e, key, texture, color))

	console.log(`transition items finished (${SEQUENCED_ASSEMBLIES.length})`)
})
