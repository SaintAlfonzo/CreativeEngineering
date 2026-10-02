ServerEvents.recipes(e=>{
console.log('canning starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

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

// grapes, and mite crust wont be canned, remove them. ensure that food effects carry over from the ingredients or can be added manually, 
// this will be more important later. verify the bowl from bone broth is returned. 

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
    // Raw - Salt, Vinegar
// Fruit
    // Raw - Sugar, Vinegar
// Meat
    // Raw, cooked - Bone broth, Vinegar

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
R.shapeless(Item.of(i.canned_beetroot, 1), [i.empty_can, Item.of(i.beetroot, 6), i.salt], e, 'canned_beetroot_salt')

// Cabbage- 2, 1.6
// 3x  ~= 1BP
// 5x  -- 1BP, 2M
// 3x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_cabbage, 1), [i.empty_can, Item.of(i.any_cabbage, 5)], e, 'canned_cabbage')
R.shapeless(Item.of(i.canned_cabbage, 1), [i.empty_can, Item.of(i.any_cabbage, 3), i.salt], e, 'canned_cabbage_salt')

// Carrot- 3, 3.6
// 2x  ~= 1BP
// 3x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_carrot, 1), [i.empty_can, Item.of(i.carrot, 3)], e, 'canned_carrot')
R.shapeless(Item.of(i.canned_carrot, 1), [i.empty_can, Item.of(i.carrot, 2), i.salt], e, 'canned_carrot_salt')

// Onion- 2, 1.6
// 3x  ~= 1BP
// 5x  -- 1BP, 2M
// 3x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_onion, 1), [i.empty_can, Item.of(i.onion, 5)], e, 'canned_onion')
R.shapeless(Item.of(i.canned_onion, 1), [i.empty_can, Item.of(i.onion, 3), i.salt], e, 'canned_onion_salt')

// Potato- 1, 0.6
// 7x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_potato, 1), [i.empty_can, Item.of(i.any_potato, 8)], e, 'canned_potato')
R.shapeless(Item.of(i.canned_potato, 1), [i.empty_can, Item.of(i.any_potato, 6), i.salt], e, 'canned_potato_salt')

// Tomato- 1, 0.6
// 7x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_tomato, 1), [i.empty_can, Item.of(i.tomato, 8)], e, 'canned_tomato')
R.shapeless(Item.of(i.canned_tomato, 1), [i.empty_can, Item.of(i.tomato, 6), i.salt], e, 'canned_tomato_salt')

// Bell Pepper (Green, Red, Yellow)- 1, 0.2
// 9x  ~= 1BP
// 8x  -- 1BP, 2M
// 6x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_bell_pepper, 1), [i.empty_can, Item.of(i.any_bell_pepper, 8)], e, 'canned_bell_pepper')
R.shapeless(Item.of(i.canned_bell_pepper, 1), [i.empty_can, Item.of(i.any_bell_pepper, 6), i.salt], e, 'canned_bell_pepper_salt')

// Cactus- 3, 1.8
// 2x  ~= 1BP
// 4x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_cactus, 1), [i.empty_can, Item.of(i.cactus_flesh, 4)], e, 'canned_cactus')
R.shapeless(Item.of(i.canned_cactus, 1), [i.empty_can, Item.of(i.cactus_flesh, 2), i.salt], e, 'canned_cactus_salt')

// Gongylidia- 3, 7.2
// 1x  ~= 1BP
// 2x  -- 1BP, 2M
// 1x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_gongylidia, 1), [i.empty_can, Item.of(i.gongylidia, 2)], e, 'canned_gongylidia')
R.shapeless(Item.of(i.canned_gongylidia, 1), [i.empty_can, i.gongylidia, i.salt], e, 'canned_gongylidia_salt')

// Mint Leaves- 2, 0.8
// 4x  ~= 1BP
// 6x  -- 1BP, 2M
// 4x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_mint, 1), [i.empty_can, Item.of(i.mint_leaves, 6)], e, 'canned_mint')
R.shapeless(Item.of(i.canned_mint, 1), [i.empty_can, Item.of(i.mint_leaves, 4), i.salt], e, 'canned_mint_salt')

// Pumpkin Slice- 3, 1.8
// 2x  ~= 1BP
// 4x  -- 1BP, 2M
// 2x* -> 1BP, 2M
R.shapeless(Item.of(i.canned_pumpkin, 1), [i.empty_can, Item.of(i.pumpkin_slice, 4)], e, 'canned_pumpkin')
R.shapeless(Item.of(i.canned_pumpkin, 1), [i.empty_can, Item.of(i.pumpkin_slice, 2), i.salt], e, 'canned_pumpkin_salt')

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

// Acorn- 2, 0.4
// 3x  ~= 1FM
// 8x  -- 1FM, 3M
// 6x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_acorn, 1), [i.empty_can, Item.of(i.acorn, 8)], e, 'canned_acorn')
R.shapeless(Item.of(i.canned_acorn, 1), [i.empty_can, Item.of(i.acorn, 6), i.sugar], e, 'canned_acorn_sugar')

// Apple- 4, 2.4
// 1x  ~= 1FM
// 3x  -- 1FM, 3M
// 2x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_apple, 1), [i.empty_can, Item.of(i.apple, 3)], e, 'canned_apple')
R.shapeless(Item.of(i.canned_apple, 1), [i.empty_can, Item.of(i.apple, 2), i.sugar], e, 'canned_apple_sugar')

//TODO Merge Bananas

// Banana (Alex's Mobs)- 4, 2.4
// 1x  ~= 1FM
// 3x  -- 1FM, 3M
// 2x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.banana, 3)], e, 'canned_banana')
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.banana, 2), i.sugar], e, 'canned_banana_sugar')

// Banana (Neapolitan)- 2, 0.8
// 3x  ~= 1FM
// 7x  -- 1FM, 3M
// 5x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.neapolitan_banana, 7)], e, 'canned_banana_neapolitan')
R.shapeless(Item.of(i.canned_banana, 1), [i.empty_can, Item.of(i.neapolitan_banana, 5), i.sugar], e, 'canned_banana_neapolitan_sugar')

// Cantaloupe Slice- 4, 6.4
// 1x  ~= 1FM
// 2x  -- 1FM, 3M
// 1x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_cantaloupe, 1), [i.empty_can, Item.of(i.cantaloupe_slice, 2)], e, 'canned_cantaloupe')
R.shapeless(Item.of(i.canned_cantaloupe, 1), [i.empty_can, i.cantaloupe_slice, i.sugar], e, 'canned_cantaloupe_sugar')

// Cherry- 2, 1.2
// 3x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_cherry, 1), [i.empty_can, Item.of(i.cherry, 6)], e, 'canned_cherry')
R.shapeless(Item.of(i.canned_cherry, 1), [i.empty_can, Item.of(i.cherry, 4), i.sugar], e, 'canned_cherry_sugar')

// Glow Berries- 2, 0.4
// 3x  ~= 1FM
// 8x  -- 1FM, 3M
// 6x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_glow_berry, 1), [i.empty_can, Item.of(i.glow_berries, 8)], e, 'canned_glow_berry')
R.shapeless(Item.of(i.canned_glow_berry, 1), [i.empty_can, Item.of(i.glow_berries, 6), i.sugar], e, 'canned_glow_berry_sugar')

// Melon Slice- 2, 1.2
// 3x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_melon, 1), [i.empty_can, Item.of(i.melon, 6)], e, 'canned_melon')
R.shapeless(Item.of(i.canned_melon, 1), [i.empty_can, Item.of(i.melon, 4), i.sugar], e, 'canned_melon_sugar')

// Mulberry- 3, 0.6
// 2x  ~= 1FM
// 5x  -- 1FM, 3M
// 3x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_mulberry, 1), [i.empty_can, Item.of(i.mulberry, 5)], e, 'canned_mulberry')
R.shapeless(Item.of(i.canned_mulberry, 1), [i.empty_can, Item.of(i.mulberry, 3), i.sugar], e, 'canned_mulberry_sugar')

// Salmonberries- 2, 2.4
// 2x  ~= 1FM
// 4x  -- 1FM, 3M
// 3x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_salmonberry, 1), [i.empty_can, Item.of(i.salmonberries, 4)], e, 'canned_salmonberry')
R.shapeless(Item.of(i.canned_salmonberry, 1), [i.empty_can, Item.of(i.salmonberries, 3), i.sugar], e, 'canned_salmonberry_sugar')

// Strawberries- 3, 0.3
// 2x  ~= 1FM
// 6x  -- 1FM, 3M
// 4x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.strawberries, 6)], e, 'canned_strawberry')
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.strawberries, 4), i.sugar], e, 'canned_strawberry_sugar')

// White Strawberries- 5, 0.5
// 1x  ~= 1FM
// 3x  -- 1FM, 3M
// 2x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.white_strawberries, 3)], e, 'canned_strawberry_white')
R.shapeless(Item.of(i.canned_strawberry, 1), [i.empty_can, Item.of(i.white_strawberries, 2), i.sugar], e, 'canned_strawberry_white_sugar')

// Sweet Berries- 2, 0.4
// 3x  ~= 1FM
// 8x  -- 1FM, 3M
// 6x* -> 1FM, 3M
R.shapeless(Item.of(i.canned_sweet_berry, 1), [i.empty_can, Item.of(i.sweet_berries, 8)], e, 'canned_sweet_berry')
R.shapeless(Item.of(i.canned_sweet_berry, 1), [i.empty_can, Item.of(i.sweet_berries, 6), i.sugar], e, 'canned_sweet_berry_sugar')

// ============================================================
// Raw Meat
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
R.shapeless(Item.of(i.canned_raw_beef, 1), [i.empty_can, Item.of(i.beef, 4), i.bone_broth], e, 'canned_raw_beef_broth')

// Chicken- 2, 1.2
// 3x  ~= 1MM
// 8x  -- 1MM, 4M
// 6x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_chicken, 1), [i.empty_can, Item.of(i.chicken, 8)], e, 'canned_raw_chicken')
R.shapeless(Item.of(i.canned_raw_chicken, 1), [i.empty_can, Item.of(i.chicken, 6), i.bone_broth], e, 'canned_raw_chicken_broth')

// Mutton- 2, 1.2
// 3x  ~= 1MM
// 8x  -- 1MM, 4M
// 6x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_mutton, 1), [i.empty_can, Item.of(i.mutton, 8)], e, 'canned_raw_mutton')
R.shapeless(Item.of(i.canned_raw_mutton, 1), [i.empty_can, Item.of(i.mutton, 6), i.bone_broth], e, 'canned_raw_mutton_broth')

// Porkchop- 3, 1.8
// 2x  ~= 1MM
// 5x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.porkchop, 5)], e, 'canned_raw_pork')
R.shapeless(Item.of(i.canned_raw_pork, 1), [i.empty_can, Item.of(i.porkchop, 4), i.bone_broth], e, 'canned_raw_pork_broth')

// Rabbit- 3, 1.8
// 2x  ~= 1MM
// 5x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_rabbit, 1), [i.empty_can, Item.of(i.rabbit, 5)], e, 'canned_raw_rabbit')
R.shapeless(Item.of(i.canned_raw_rabbit, 1), [i.empty_can, Item.of(i.rabbit, 4), i.bone_broth], e, 'canned_raw_rabbit_broth')

// Ham- 5, 3
// 1x  == 1MM
// 3x  -- 1MM, 4M
// 2x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_ham, 1), [i.empty_can, Item.of(i.ham, 3)], e, 'canned_raw_ham')
R.shapeless(Item.of(i.canned_raw_ham, 1), [i.empty_can, Item.of(i.ham, 2), i.bone_broth], e, 'canned_raw_ham_broth')

// Chevon- 2, 1.2
// 3x  ~= 1MM
// 8x  -- 1MM, 4M
// 6x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_chevon, 1), [i.empty_can, Item.of(i.raw_goat, 8)], e, 'canned_raw_chevon')
R.shapeless(Item.of(i.canned_raw_chevon, 1), [i.empty_can, Item.of(i.raw_goat, 6), i.bone_broth], e, 'canned_raw_chevon_broth')

// Hoglin Loin- 3, 1.5
// 2x  ~= 1MM
// 6x  -- 1MM, 4M
// 4x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_hoglin, 1), [i.empty_can, Item.of(i.hoglin_loin, 6)], e, 'canned_raw_hoglin')
R.shapeless(Item.of(i.canned_raw_hoglin, 1), [i.empty_can, Item.of(i.hoglin_loin, 4), i.bone_broth], e, 'canned_raw_hoglin_broth')

// Kangaroo- 4, 4.8
// 1x  ~= 1MM
// 3x  -- 1MM, 4M
// 2x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_kangaroo, 1), [i.empty_can, Item.of(i.kangaroo_meat, 3)], e, 'canned_raw_kangaroo')
R.shapeless(Item.of(i.canned_raw_kangaroo, 1), [i.empty_can, Item.of(i.kangaroo_meat, 2), i.bone_broth], e, 'canned_raw_kangaroo_broth')

// Moose Ribs- 3, 3.6
// 1x  ~= 1MM
// 4x  -- 1MM, 4M
// 2x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_moose, 1), [i.empty_can, Item.of(i.moose_ribs, 4)], e, 'canned_raw_moose')
R.shapeless(Item.of(i.canned_raw_moose, 1), [i.empty_can, Item.of(i.moose_ribs, 2), i.bone_broth], e, 'canned_raw_moose_broth')

// Bison- 4, 4
// 1x  == 1MM
// 3x  -- 1MM, 4M
// 2x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_bison, 1), [i.empty_can, Item.of(i.raw_bison, 3)], e, 'canned_raw_bison')
R.shapeless(Item.of(i.canned_raw_bison, 1), [i.empty_can, Item.of(i.raw_bison, 2), i.bone_broth], e, 'canned_raw_bison_broth')

// Bunfungus- 4, 3.2
// 1x  ~= 1MM
// 4x  -- 1MM, 4M
// 2x* -> 1MM, 4M
R.shapeless(Item.of(i.canned_raw_bunfungus, 1), [i.empty_can, Item.of(i.raw_bunfungus, 4)], e, 'canned_raw_bunfungus')
R.shapeless(Item.of(i.canned_raw_bunfungus, 1), [i.empty_can, Item.of(i.raw_bunfungus, 2), i.bone_broth], e, 'canned_raw_bunfungus_broth')

console.log('canning done')
})
