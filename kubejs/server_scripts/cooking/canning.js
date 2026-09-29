ServerEvents.recipes(e=>{
console.log('canning starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Other Changes TODO
// ============================================================

// Cans consumable multiple times, configurable, renders and executes through durability, bar enchanting
// Chance based can/ crushed can return from eating / crafting

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

// ============================================================
// Canning
// ============================================================

// Not built yet. Plan: can foods by type (#creativeengineering:food_type/*, keys i.t_food_*),
// each type with its own additive:
// Veggie
    // Raw - Salt
// Fruit
    // Raw - Sugar
// Meat
    // Raw -

// ============================================================
// WIP
// ============================================================

console.log('canning done')
})
