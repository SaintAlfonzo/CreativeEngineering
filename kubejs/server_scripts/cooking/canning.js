ServerEvents.recipes(e=>{
console.log('canning starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// Generated cans (tools/gen_can_types.py -> generation_scripts/food/can_types.json): one meal = the food it holds x the
// section multiplier, effects copied with scaled durations. Each can has its recipes in the json: n of the source(s) (any
// of the listed alternatives per slot) + an empty can, plus the additive variants. canSection('x') makes the recipes of one
// section; recipe ids are the can key plus the variant suffix (canned_cookies, canned_cookies_sugar). A recipe with
// "servings" makes a partly full can (alt inputs too small to fill one in 8 slots); a can with "pie" also turns back
// into that pie with a pie crust (the whole can is used, opened or not).
var canTypes = {}
try {
	canTypes = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/can_types.json'))).cans
} catch (err) {
	console.error('[canning] could not read can_types.json, run tools/gen_can_types.py: ' + err)
}
function canSection(section) {
	var made = 0
	Object.keys(canTypes).forEach(key => {
		var can = canTypes[key]
		if (can.section !== section) return
		can.recipes.forEach(rec => {
			try {
				var input = [i.empty_can]
				rec.inputs.forEach(inp => {
					var ids = inp.any.map(k => i[k])
					ids.forEach((id, slot) => {
						if (typeof id !== 'string') throw new Error(inp.any[slot] + ' is not in item_dict')
					})
					if (ids.length === 1) {
						input.push(inp.n > 1 ? Item.of(ids[0], inp.n) : ids[0])
					} else {
						for (var n = 0; n < inp.n; n++) input.push(ids) // every slot takes any of the alternatives
					}
				})
				if (typeof i[key] !== 'string') throw new Error(key + ' is not in item_dict')
				var out = rec.servings ? Item.of(i[key], 1, '{CanServings:' + rec.servings + '}') : Item.of(i[key], 1)
				R.shapeless(out, input, e, key + rec.suffix)
				made++
			} catch (err) {
				console.warn('[canning] ' + key + rec.suffix + ' skipped: ' + err)
			}
		})
		if (can.pie) {
			if (typeof i[can.pie] !== 'string') console.warn('[canning] ' + key + '_pie skipped: ' + can.pie + ' is not in item_dict')
			else { R.shapeless(Item.of(i[can.pie], 1), [i[key], i.pie_crust], e, key + '_pie'); made++ }
		}
	})
	console.log('[canning] ' + section + ': ' + made + ' recipes')
}

// ============================================================
// Other Changes TODO
// ============================================================

// BUILT (patcherMod food.CanServings, feature_writeup/food.md 3.6):
// - Cans hold several servings: per can in generation_scripts/food/can_servings.json -> #creativeengineering:cans/servings/<n>
//   (tags/cantags.js). Servings left are NBT, not durability, so full cans still stack to 64 and can't be enchanted;
//   a partly eaten can shows a durability-style bar and "Servings: x / y".
// - The last serving rolls an empty can / crushed can / nothing: CEConfig [canning] emptyCanChance, crushedCanChance.
// - In recipes: R.shaped(..., cans) / R.shapeless(..., cans), cans = true -> each can loses one serving per craft
//   (same roll on the last one). Example:
//   R.shapeless(Item.of(i.beef_cooked), [i.canned_beef], e, 'uncan_beef', null, null, true)

// TODO 
// Canning automation

// Not canned (removed 2026-10-13): mint, acorn, grapes, mite crust, raw ham/chevon/hoglin/kangaroo/moose/bison/bunfungus.
// Coverage worksheet: feature_writeup/can_parity.md
// ensure that food effects carry over from the ingredients or can be added manually, this will be more important later. verify the bowl from bone broth is returned. 

// Design:
// Cans hold multiple meals, increasing meals per stack
// Raw, low hp, low sat food can be canned with many ingredients to make can with fewer, high value meals
	// Encourages canning to increase value
// Cooked, mid value (high tier vanilla) foods can with several ingredients to get can with several valuable meals, slightly increased value
	// Benefits from stack increase, slight value increase, mostly static
// Prepared meals, high value foods can with few ingredients to get can with many valueable meals, higher value overall
	// Used for rationing high value meals, stackable effect foods, lower SOL value overall

// ============================================================
// Removals
// ============================================================

// Every Canned Goods food can (2x food + iron ingot, plus the Hot Hot Hotpot), its own can
// recipes, and can -> iron ingot recycling (with 4+ cans per ingot below, that would duplicate iron).
// Food canning comes back later, built on the #creativeengineering:food_type tags (feature_writeup/food.md).
// Kept: Soda, Reef-Cola, Oculemonade, Mini Fizz (drinks made in a can), canned dirt, canned golem.
R.bulk('remove', [
	// food cans
	'aaa', 'recipe_canned_apples', 'recipe_canned_beef', 'recipe_canned_beets', 'recipe_canned_carrots',
	'recipe_canned_chicken', 'recipe_canned_chorus_fruit', 'recipe_canned_fish', 'recipe_canned_glow_berries',
	'recipe_canned_mushrooms', 'recipe_canned_pork', 'recipe_canned_sweet_berries', 'recipe_dried', 'recipebp',
	'recipebrs', 'recipecakje', 'recipecie', 'recipecm', 'recipecr', 'recipecs', 'recipeg_a', 'recipegc', 'recipehb',
	'recipems', 'recipena', 'recipep', 'recipepp', 'recipepuffer', 'recipepumpkin', 'reciperf', 'recipers', 'recipese',
	'recipesusstew', 'recipetrop', 'hot_hot_hotpot_recipe',
	// can crafting
	'ironintocan', 'irontocrushedcan',
	// can -> iron ingot
	'fullemptycan', 'recycling_cans',
].map(id => [{ id: 'canned_goods:' + id }, e]))

// ============================================================
// Cans
// ============================================================

// Cans per iron ingot: stonecutter 4, crafting 6, deployer 6, Engineer's Workbench 8 (a plate is one ingot).
R.stonecutting(Item.of(i.empty_can, 4), i.ingot_iron, e, 'empty_can')
R.shaped(Item.of(i.empty_can, 12), ['P', 'P'], { P: i.any_plate_iron }, e, 'empty_can')
R.createDeploying(Item.of(i.empty_can, 12), [i.any_plate_iron, i.any_plate_iron], e, 'empty_can')
R.ieBlueprint(Item.of(i.empty_can, 32), [Item.of(i.plate_iron, 4)], 'components', e, 'empty_can')

// Can Recycle
// WIP

// ============================================================
// Canning
// ============================================================

// Can be done with ingredient + can
// Ingredient + can + additive stretches ingredient, better yield
// Veggie
    // Raw - Salt, Bone Broth, Vinegar
// Fruit
    // Raw - Sugar, Honey (return bottle), Vinegar
// Meat
    // Raw, cooked - Salt, Bone Broth, Vinegar

// Vinegar added later

// ============================================================
// Raw Veggie
// ============================================================

// Goal
// (Baked Potato- 5, 6) sized meals

// Food- Hunger Points, Saturation
// Ingredient to Baked Potato Ratio
// Basic Canning Ratio
// Advanced Canning Ratio

// Beetroot- 1, 1.2, 2 Meals/Can
// 5x ==  1BP            
// 8x --  1BP, 2M 
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_beetroot, 1), [i.empty_can, Item.of(i.beetroot, 8)], e, 'canned_beetroot')
R.shapeless(Item.of(i.canned_beetroot, 1), [i.empty_can, Item.of(i.beetroot, 6), [i.salt, i.bone_broth]], e, 'canned_beetroot_salt')

// Cabbage- 2, 1.6
// 3x  ~= 1BP
// 5x  -- 1BP, 2M
// 3x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_cabbage, 1), [i.empty_can, Item.of(i.any_cabbage, 5)], e, 'canned_cabbage')
R.shapeless(Item.of(i.canned_cabbage, 1), [i.empty_can, Item.of(i.any_cabbage, 3), [i.salt, i.bone_broth]], e, 'canned_cabbage_salt')

// Carrot- 3, 3.6
// 2x  ~= 1BP
// 3x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_carrot, 1), [i.empty_can, Item.of(i.carrot, 3)], e, 'canned_carrot')
R.shapeless(Item.of(i.canned_carrot, 1), [i.empty_can, Item.of(i.carrot, 2), [i.salt, i.bone_broth]], e, 'canned_carrot_salt')

// Onion- 2, 1.6
// 3x  ~= 1BP
// 5x  -- 1BP, 2M
// 3x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_onion, 1), [i.empty_can, Item.of(i.onion, 5)], e, 'canned_onion')
R.shapeless(Item.of(i.canned_onion, 1), [i.empty_can, Item.of(i.onion, 3), [i.salt, i.bone_broth]], e, 'canned_onion_salt')

// Potato- 1, 0.6
// 7x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_potato, 1), [i.empty_can, Item.of(i.any_potato, 8)], e, 'canned_potato')
R.shapeless(Item.of(i.canned_potato, 1), [i.empty_can, Item.of(i.any_potato, 6), [i.salt, i.bone_broth]], e, 'canned_potato_salt')

// Tomato- 1, 0.6
// 7x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_tomato, 1), [i.empty_can, Item.of(i.tomato, 8)], e, 'canned_tomato')
R.shapeless(Item.of(i.canned_tomato, 1), [i.empty_can, Item.of(i.tomato, 6), [i.salt, i.bone_broth]], e, 'canned_tomato_salt')

// Bell Pepper (Green, Red, Yellow)- 1, 0.2
// 9x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_bell_pepper, 1), [i.empty_can, Item.of(i.any_bell_pepper, 8)], e, 'canned_bell_pepper')
R.shapeless(Item.of(i.canned_bell_pepper, 1), [i.empty_can, Item.of(i.any_bell_pepper, 6), [i.salt, i.bone_broth]], e, 'canned_bell_pepper_salt')

// Cactus Flesh- 3, 1.8
// 2x  ~= 1BP
// 4x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_cactus_flesh, 1), [i.empty_can, Item.of(i.cactus_flesh, 4)], e, 'canned_cactus_flesh')
R.shapeless(Item.of(i.canned_cactus_flesh, 1), [i.empty_can, Item.of(i.cactus_flesh, 2), [i.salt, i.bone_broth]], e, 'canned_cactus_flesh_salt')

// Gongylidia- 3, 7.2
// 1x  ~= 1BP
// 2x  -- 1BP, 2M
// 1x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_gongylidia, 1), [i.empty_can, Item.of(i.gongylidia, 2)], e, 'canned_gongylidia')
R.shapeless(Item.of(i.canned_gongylidia, 1), [i.empty_can, i.gongylidia, [i.salt, i.bone_broth]], e, 'canned_gongylidia_salt')

// Pumpkin Slice- 3, 1.8
// 2x  ~= 1BP
// 4x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_pumpkin, 1), [i.empty_can, Item.of(i.pumpkin_slice, 4)], e, 'canned_pumpkin')
R.shapeless(Item.of(i.canned_pumpkin, 1), [i.empty_can, Item.of(i.pumpkin_slice, 2), [i.sugar, i.honey_bottle]], e, 'canned_pumpkin_salt')

// ============================================================
// Raw Fruit
// ============================================================

// Goal
// (Fruit Meal- 5, 3) sized meals, 3 Meals/Can
// Largest meal where the lowest value fruit (2, 0.4) still fits the grid at the basic ratio

// Food- Hunger Points, Saturation
// Ingredient to Fruit Meal Ratio
// Basic Canning Ratio
// Advanced Canning Ratio

// Apple- 4, 2.4
// 1x  ~= 1FM
// 3x  -- 1FM, 3M
// 2x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_apple, 1), [i.empty_can, Item.of(i.apple, 3)], e, 'canned_apple')
R.shapeless(Item.of(i.canned_apple, 1), [i.empty_can, Item.of(i.apple, 2), [i.sugar, i.honey_bottle]], e, 'canned_apple_sugar')

// Banana (Neapolitan only; Alex's Mobs bananas are not canned, can_parity.md)- 2, 0.8
// 3x  ~= 1FM
// 7x  -- 1FM, 3M
// 5x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.neapolitan_banana, 7)], e, 'canned_banana')
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.neapolitan_banana, 5), [i.sugar, i.honey_bottle]], e, 'canned_banana_sugar')

// Cantaloupe Slice- 4, 6.4
// 1x  ~= 1FM
// 2x  -- 1FM, 3M
// 1x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_cantaloupe, 1), [i.empty_can, Item.of(i.cantaloupe_slice, 2)], e, 'canned_cantaloupe')
R.shapeless(Item.of(i.canned_cantaloupe, 1), [i.empty_can, i.cantaloupe_slice, [i.sugar, i.honey_bottle]], e, 'canned_cantaloupe_sugar')

// Cherry- 2, 1.2
// 3x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_cherry, 1), [i.empty_can, Item.of(i.cherry, 6)], e, 'canned_cherry')
R.shapeless(Item.of(i.canned_cherry, 1), [i.empty_can, Item.of(i.cherry, 4), [i.sugar, i.honey_bottle]], e, 'canned_cherry_sugar')

// Glow Berries- 2, 0.4
// 3x  ~= 1FM
// 8x  -- 1FM, 3M
// 6x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_glow_berry, 1), [i.empty_can, Item.of(i.glow_berries, 8)], e, 'canned_glow_berry')
R.shapeless(Item.of(i.canned_glow_berry, 1), [i.empty_can, Item.of(i.glow_berries, 6), [i.sugar, i.honey_bottle]], e, 'canned_glow_berry_sugar')

// Melon Slice- 2, 1.2
// 3x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_melon, 1), [i.empty_can, Item.of(i.melon, 6)], e, 'canned_melon')
R.shapeless(Item.of(i.canned_melon, 1), [i.empty_can, Item.of(i.melon, 4), [i.sugar, i.honey_bottle]], e, 'canned_melon_sugar')

// Mulberry- 3, 0.6
// 2x  ~= 1FM
// 5x  -- 1FM, 3M
// 3x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_mulberry, 1), [i.empty_can, Item.of(i.mulberry, 5)], e, 'canned_mulberry')
R.shapeless(Item.of(i.canned_mulberry, 1), [i.empty_can, Item.of(i.mulberry, 3), [i.sugar, i.honey_bottle]], e, 'canned_mulberry_sugar')

// Salmonberries- 2, 2.4
// 2x  ~= 1FM
// 4x  -- 1FM, 3M
// 3x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_salmonberry, 1), [i.empty_can, Item.of(i.salmonberries, 4)], e, 'canned_salmonberry')
R.shapeless(Item.of(i.canned_salmonberry, 1), [i.empty_can, Item.of(i.salmonberries, 3), [i.sugar, i.honey_bottle]], e, 'canned_salmonberry_sugar')

// Strawberries- 3, 0.3
// 2x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.strawberries, 6)], e, 'canned_strawberry')
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.strawberries, 4), [i.sugar, i.honey_bottle]], e, 'canned_strawberry_sugar')

// White Strawberries- 5, 0.5
// 1x  ~= 1FM
// 3x  -- 1FM, 3M
// 2x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_white_strawberry, 1), [i.empty_can, Item.of(i.white_strawberries, 3)], e, 'canned_white_strawberry')
R.shapeless(Item.of(i.canned_white_strawberry, 1), [i.empty_can, Item.of(i.white_strawberries, 2), [i.sugar, i.honey_bottle]], e, 'canned_white_strawberry_sugar')

// Sweet Berries- 2, 0.4
// 3x  ~= 1FM
// 8x  -- 1FM, 3M
// 6x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_sweet_berry, 1), [i.empty_can, Item.of(i.sweet_berries, 8)], e, 'canned_sweet_berry')
R.shapeless(Item.of(i.canned_sweet_berry, 1), [i.empty_can, Item.of(i.sweet_berries, 6), [i.sugar, i.honey_bottle]], e, 'canned_sweet_berry_sugar')

// The other fruits (fruitsdelight etc.) are generated with the same Fruit Meal and the same sizing rule: the basic
// recipe is worth about 0.8 of the can, the sugar/honey one 0.7 of that. Sliced lemon/orange are alt inputs.
canSection('raw_fruit')

// ============================================================
// Raw Meat
// Sliced cuts are alt inputs (own recipe sized by value). Where 8 slices are worth less than the whole recipe
// (chicken cuts, mutton chops), 8 slices make a half full can.
// ============================================================

// Goal
// (Meat Meal- 5, 3) sized meals, 4 Meals/Can
// Largest meal where the lowest value meat (2, 1.2) still fits the grid at the basic ratio

// Food- Hunger Points, Saturation
// Ingredient to Meat Meal Ratio
// Basic Canning Ratio
// Advanced Canning Ratio

// Beef- 3, 1.8
// 2x  ~= 1MM
// 5x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_beef, 1), [i.empty_can, Item.of(i.beef, 5)], e, 'canned_raw_beef')
R.shapeless(Item.of(i.canned_raw_beef, 1), [i.empty_can, Item.of(i.beef, 4), [i.salt, i.bone_broth]], e, 'canned_raw_beef_broth')
// Minced Beef- 2, 1.2: 8x (6x*) -> full can
R.shapeless(Item.of(i.canned_raw_beef, 1), [i.empty_can, Item.of(i.minced_beef, 8)], e, 'canned_raw_beef_minced')
R.shapeless(Item.of(i.canned_raw_beef, 1), [i.empty_can, Item.of(i.minced_beef, 6), [i.salt, i.bone_broth]], e, 'canned_raw_beef_minced_broth')

// Chicken- 2, 1.2
// 3x  ~= 1MM
// 8x  -- 1MM, 4M
// 6x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_chicken, 1), [i.empty_can, Item.of(i.chicken, 8)], e, 'canned_raw_chicken')
R.shapeless(Item.of(i.canned_raw_chicken, 1), [i.empty_can, Item.of(i.chicken, 6), [i.salt, i.bone_broth]], e, 'canned_raw_chicken_broth')
// Raw Chicken Cuts- 1, 0.6: 8x -> 2 of 4 servings
R.shapeless(Item.of(i.canned_raw_chicken, 1, '{CanServings:2}'), [i.empty_can, Item.of(i.chicken_cuts, 8)], e, 'canned_raw_chicken_cuts')

// Mutton- 2, 1.2
// 3x  ~= 1MM
// 8x  -- 1MM, 4M
// 6x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_mutton, 1), [i.empty_can, Item.of(i.mutton, 8)], e, 'canned_raw_mutton')
R.shapeless(Item.of(i.canned_raw_mutton, 1), [i.empty_can, Item.of(i.mutton, 6), [i.salt, i.bone_broth]], e, 'canned_raw_mutton_broth')
// Raw Mutton Chops- 1, 0.6: 8x -> 2 of 4 servings
R.shapeless(Item.of(i.canned_raw_mutton, 1, '{CanServings:2}'), [i.empty_can, Item.of(i.mutton_chops, 8)], e, 'canned_raw_mutton_chops')

// Porkchop- 3, 1.8
// 2x  ~= 1MM
// 5x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.porkchop, 5)], e, 'canned_raw_pork')
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.porkchop, 4), [i.salt, i.bone_broth]], e, 'canned_raw_pork_broth')
// Raw Bacon- 2, 1.2: 8x (6x*) -> full can
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.farmersdelight_bacon, 8)], e, 'canned_raw_pork_bacon')
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.farmersdelight_bacon, 6), [i.salt, i.bone_broth]], e, 'canned_raw_pork_bacon_broth')

// Rabbit- 3, 1.8
// 2x  ~= 1MM
// 5x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_rabbit, 1), [i.empty_can, Item.of(i.rabbit, 5)], e, 'canned_raw_rabbit')
R.shapeless(Item.of(i.canned_raw_rabbit, 1), [i.empty_can, Item.of(i.rabbit, 4), [i.salt, i.bone_broth]], e, 'canned_raw_rabbit_broth')

// Shulker (generated, same Meat Meal and sizing rule; Shulker Filet is an alt input)
canSection('raw_meat')

// ============================================================
// Raw Fish
// ============================================================

// Goal: the raw meat one (Meat Meal 5, 3, 4 servings, salt or bone broth). Every sliced fish is an alt input; 8 slices
// are worth half the whole recipe, so they make a half full can. Tropical Fish reuses Canned Goods' can.
canSection('raw_fish')

// ============================================================
// Hazard
// ============================================================

// Canned Goods' own cans (Pufferfish, Poisonous Potatos, Night Berries = spider eyes, Tough Jerky = rotten flesh):
// Meat Meal sized, no additive. Canned Goods' code applies the bad effects when a serving is eaten.
canSection('hazard')

// ============================================================
// Cookies
// ============================================================

// Goal
// (Cookie Type x2) sized meals, 5 Meals/Can
// All fast
// Sugar additive

// Food- Hunger Points, Saturation

// Cookie- 2, 1.2
// (4, 2.4) x5
canSection('cookies')

// ============================================================
// Cooked Veggies
// ============================================================

// Goal
// (Veggie Item x2) -> 4 Meals/can
// 6 Veggies per can craft, salt additive for 5 veggies

// Food- Hunger Points, Saturation

// Baked Potato (accepts slices)
// Cactus Steak
// Cooked Rice
// Dried Kelp
// Roasted Bell Pepper (all colors)

// Meal = the item x2 (hunger and saturation points), so a can keeps the item's modifier. The sliced baked potato counts as a
// baked potato. Generated: can_types.json, section cooked_veggie.
canSection('cooked_veggie')

// ============================================================
// Cooked Meat - all fish accept sliced
// Not canned cooked: patties, bacon, bison, smoked ham, chevon, hoglin, kangaroo, moose, bunfungus, lobster, crab
// ============================================================

// Goal
// (Meat Item x2) -> 3 Meals/can
// 5 Meat per can craft, salt additive for 4 meat

// Meal = the item x2. Whole and sliced cuts share a can for chicken, mutton and every fish that has a sliced form.
// Honey Glazed Porkchop, Jerky and Maple Glazed Bacon get their own cans. The additive is salt or bone broth, like the
// raw meat cans. Fish are in the same section (cooked_meat) of can_types.json.
canSection('cooked_meat')

// ============================================================
// Bread
// ============================================================

// Goal
// (Bread Type x2) sized meals, 4 Meals/Can
// 6 Bread / can
// No additives, special honey bread additive recipe, All bread items but scones, bread slice, toast, tortilla

// Food- Hunger Points, Saturation 

// Bread- 5, 6
// (10, 12) x4 meals
// Canned Honey Bread also has a second recipe: 5 bread + a honey bottle.
canSection('bread')

// ============================================================
// Salad
// ============================================================

// Goal
// (Salad Type x1.25) -> 8 Meals/can 
// 6 Salads / can
// Vinegar, apple cider vinegar additive (to be added) for 5 salads
// All fast, copy effect with shorter duration (.75x)

// Food- Hunger Points, Saturation

// All salads but potato salad, maggot salad

// Meal = the salad x1.25, fast, effects at 0.75x duration, 8 meals per can. No additive yet (vinegar comes later:
// 5 salads + vinegar). Generated: can_types.json, section salad.
canSection('salad')

// ============================================================
// Soup
// ============================================================

// Goal
// (Soup Type x1.25) sized meals, 8 Meals/can
// 6 Soups / can
// Salt, bone broth additive for 3 soups
// Copy effect with shorter duration (.75x)

// Food- Hunger Points, Saturation

// All soups but cave explorer and the wooden soups that were removed

// Meal = the soup x1.25, effects at 0.75x duration, 8 meals per can; 3 soups + salt or bone broth also works.
// Suspicious stew is left out: its effects come from its NBT, a can would lose them. Generated: can_types.json, section soup.
canSection('soup')

// ============================================================
// Meals
// ============================================================

// Goal
// TBD

// Food- Hunger Points, Saturation

// Pastas (Bell pepper, cheesy, crab, kangaroo, lobster, meatballs, mutton chops, pearl[not wood], squid ink)
	// include [veg noodles, duck noodles], rename to veg pasta and pasta w/ duck

// Your suggestions for other meals in alignment with irl canning and the other cans in this mod?

// Placeholder until the goal is set (gen_can_types.py SECTIONS 'meal'): 2 pastas -> 4 meals of 0.75 pasta, effects
// 0.75x. Duck and vegetable noodles can as Pasta with Duck / Vegetable Pasta.
canSection('meal')

// ============================================================
// Pie
// ============================================================

// Goal
// 1 Can = 1 Pie
// (1 Pie Type Slice) sized meals, 4 Meals/can
// Effect same as pie slice
// Pie crust replacement recipe, generated from existing pie recipes dynamically
// 4 pie slices + can to canned pie
// Can + pie crust to pie

// Rename canned to 'pie filling'

// Food- Hunger Points, Saturation

// All pie (pie filling), pie + pie crust recipe

// Generated (section pie): 4 slices -> filling, slice-sized meals, effects as the slice. Fruitsdelight's durian and
// pineapple pies are single items, 4 of them fill a can (no pie back). Blueberry, cherry, crimson berry, green apple,
// nightshade and truffle slices have no source in the pack yet, so no can.
canSection('pie')

// ============================================================
// Feasts
// ============================================================

// Goal
// 1~2 feasts -> 6-8 meals

// Food- Hunger Points, Saturation

// All, including stuffed shulker, combine nether delight into single canned hoglin feast

// Placeholder (section feast): one placed feast block -> 6 servings worth the feast's serving. The stuffed hoglin's
// three plates share Canned Hoglin Feast (valued as the ham plate).
canSection('feast')

// ============================================================
// Drinks
// ============================================================

// Goal
// TBD

// Food- Hunger Points, Saturation

// Honey bottles, milk (powdered milk)

// Placeholder (section drink): 4 -> 4 servings. Honey reuses Canned Goods' can (its code clears poison); Canned Powdered
// Milk clears every effect like milk (cans.js), 1 hunger. Buckets and bottles are given back by the crafting grid.
canSection('drink')

// ============================================================
// Golden, Enchanted Golden
// ============================================================

// Goal
// (Golden Type x0.25) -> 10 Meals/can
// 2 golden -> canned golden
// Copy effect with shorter duration (.25x)
// Required honey additive

// Food- Hunger Points, Saturation

// All

// Meal = the golden food x0.25 (1 hunger for a golden apple), effects at 0.25x duration, 10 meals per can.
// 2 golden foods + a honey bottle (returns the bottle). New cans: Canned Goods' own golden apple / carrot / notch apple
// cans add code effects on top of their food, so they are not reused. Generated: can_types.json, section golden.
canSection('golden')

// ============================================================
// Special
// ============================================================

// Goal
// TBD

// Food- Hunger Points, Saturation

// Chorus fruit, ancient fruit

// Fruit Meal sized like raw fruit. Chorus reuses Canned Goods' can (its code teleports).
canSection('special')


console.log('canning done')
})
