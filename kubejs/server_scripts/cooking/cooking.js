ServerEvents.recipes(e => {

console.log('Hello! The cooking event has started!')

var i = global.item
var f = global.fluid
var R = global.NCH

i.catfish = 'alexsmobs:raw_catfish';

//######################### Tools #########################
//Remove stone smoker recipe
R.remove({ id: 'minecraft:smoker' }, e)
// Bamboo bowl: the vanilla bowl shape in bamboo, 1 bowl (planks give 4, so bamboo isn't the cheap route).
// TODO food/kitchen #10
R.shaped(i.bowl, ['B B', ' B '], { B: i.bamboo }, e, 'bowl_from_bamboo')
// Mortar and pestle: Old Guns' Gunsmith's Bench shape (now its Powder Pestle) in the crafting table.
// TODO food/kitchen #10
R.shaped(i.mortar_and_pestle, [' L ', 'G G', ' G '], { L: i.lever, G: i.polished_granite }, e, 'mortar_and_pestle')

//######################### Soups and bases #########################
//Bone broth
// Updated bone broth to accept bone meal + shapeless recipe
R.remove({ id: 'farmersdelight:cooking/bone_broth' }, e)
R.fdCooking([[i.bone, i.bonemeal], [i.glow_berries, i.mushrooms, i.hanging_roots, i.glow_lichen]], Item.of(i.bone_broth, 2), e, undefined, 1, 200, i.bowl)
R.shapeless(Item.of(i.bone_broth, 1), [[i.bone, i.bonemeal], [i.glow_berries, i.hanging_roots, i.glow_lichen, i.mushrooms], i.bowl], e, 'bone_broth_manual_only')
// Shapeless craft some stews w/ bonebroth (heatless meal rule: see "Meals" below)
R.shapeless(Item.of(i.beef_stew, 1), [i.bone_broth, i.cooked_beef, i.carrot, i.baked_potato], e, 'beef_stew_cooked_manual_only')
R.shapeless(Item.of(i.baked_cod_stew, 1), [i.bone_broth, i.cooked_cod, i.cooked_eggs, i.baked_potato, i.tomato_sauce], e, 'baked_cod_stew_cooked_manual_only')
R.shapeless(Item.of(i.noodle_soup, 1), [i.bone_broth, i.cooked_pork, i.cooked_eggs, i.pasta, i.dried_kelp], e, 'noodle_soup_cooked_manual_only')
// the raw carrot is gone (no cooked carrot); salad stays raw
R.shapeless(Item.of(i.chicken_soup, 1), [i.bone_broth, i.cooked_chicken, i.t_soup_veggie_cooked, i.salad_ingredients, i.t_soup_veggie_cooked], e, 'chicken_soup_cooked_manual_only')
R.shapeless(Item.of(i.cooked_rice, 1), [i.rice, i.bone_broth], e, 'cooked_rice_from_bone_broth_manual_only')

//Cooked rice
// Shapeless craft rice dishes w/ cooked_rice
R.shapeless(Item.of(i.fried_rice, 1), [i.cooked_rice, i.cooked_eggs, i.cooked_eggs, i.carrot, i.onion], e, 'fried_rice_cooked_manual_only')
R.shapeless(Item.of(i.mushroom_rice, 1), [i.cooked_rice, i.brown_mushroom, i.brown_mushroom, i.red_mushroom, i.baked_potato], e, 'mushroom_rice_cooked_manual_only')
R.shapeless(Item.of(i.sinigang, 1), [i.cooked_rice, i.cooked_fish, i.cooked_fish, i.cooked_pork, i.tomato_sauce, i.onion, i.fruit], e, 'sinigang_cooked_manual_only')

//######################### Fluids #########################

//Tomato sauce
// Tomato sauce merge
R.remove({ id: 'tmted:squeezer/tomato' }, e)
R.remove({ id: 'tmted:bottling/tomato_sauce' }, e)
R.ieSqueezing(Fluid.of(f.tomato_sauce, 250), i.tomato, e, undefined, undefined, 6400)
R.ieBottling(i.tomato_sauce, f.t_tomato_sauce, i.bowl, e, undefined, 250)
//Expensive shapeless craft
R.shapeless(Item.of(i.tomato_sauce, 1), [i.bowl, Item.of(i.tomato, 4)], e)

//Melon juice
//melon juice merge
R.remove({ id: 'tmted:squeezer/melon_juice_from_slice' }, e)
R.remove({ id: 'tmted:squeezer/melon_juice_from_block' }, e)
R.remove({ id: 'tmted:bottling/melon_juice' }, e)
R.remove({ id: 'create_central_kitchen:mixing/melon_juice' }, e)
R.ieSqueezing(Fluid.of(f.melon_juice, 125), i.melon, e, undefined, undefined, 6400)
R.ieSqueezing(Fluid.of(f.melon_juice, 1250), i.melon_block, e, undefined, undefined, 6400)
R.ieBottling(i.melon_juice, f.t_melon_juice, i.glass_bottle, e, undefined, 250)
R.createMixing(Fluid.of(f.melon_juice, 250), [i.any_sugar, Item.of(i.melon, 3)], e, undefined, 'n/a')

//Apple cider
// One chain on Central Kitchen's three cider fluids, run like a potion's (TODO drinks #3, item_merges.md 2.6).
// Base: FD pot, FR kettle, CCK mixing (2 apples + sugar -> 250 mB, heated) and the IE Mixer below at the same cost.
// Long (milk) and strong (honey), like redstone and glowstone: Create mixer 1000 mB + 1 reagent, heated; IE Mixer
// 1500 mB + 1 reagent, 6400 RF/t. Both land on the machines' standard pages, not Automated Potion Mixing.
// tmted's cider went to a dead fluid and bottled from an empty tag.
R.remove({ id: 'tmted:mixer/apple_cider' }, e)
R.remove({ id: 'tmted:bottling/apple_cider' }, e)
R.ieMixer(Fluid.of(f.apple_cider, 250), [Item.of(i.apple, 2), i.any_sugar], f.t_water, e, 'apple_cider')
;[
	[f.long_apple_cider, i.t_milk_bottle, 'long_apple_cider'],
	[f.strong_apple_cider, i.honey_bottle, 'strong_apple_cider'],
].forEach(([out, reagent, id]) => {
	R.createMixing(Fluid.of(out, 1000), [Fluid.of(f.apple_cider, 1000), reagent], e, id, 'heated')
	R.ieMixer(Fluid.of(out, 1500), reagent, f.t_apple_cider, e, id, 6400)
})
R.ieBottling(i.farmersdelight_apple_cider, f.t_apple_cider, i.glass_bottle, e, undefined, 250)
R.ieBottling(i.long_apple_cider, f.t_long_apple_cider, i.glass_bottle, e, undefined, 250)
R.ieBottling(i.strong_apple_cider, f.t_strong_apple_cider, i.glass_bottle, e, undefined, 250)

//######################### Sugar #########################
// Replace sugar recipes w/ sugar forge tag, allows glyceral, change sugar cube recipe to reflect
R.replaceInput({ input: i.sugar }, i.sugar, i.any_sugar, e)
R.remove({ id: 'supplementaries:sugar_cube' }, e)
R.shapeless(Item.of(i.sugar_cube, 1), Item.of(i.sugar, 9), e)

//######################### Flour, dough, bread
// Combine wheat flours and doughs into create version
R.remove({ id: 'create:crushing/misc/scrap_hay_block' }, e)
R.remove({ id: 'tmted:wheat_dough_from_waterbucket' }, e)
R.remove({ id: 'tmted:slime_ball' }, e)
R.remove({ id: 'tmted:crusher/wheat_flour_from_bale' }, e)
R.remove({ id: 'farmersdelight:bread_from_smelting' }, e)
R.remove({ id: 'farmersdelight:bread_from_smoking' }, e)
R.remove({ id: 'create_central_kitchen:crafting/dough_1' }, e)
R.remove({ id: 'create_central_kitchen:crafting/dough_2' }, e)
R.remove({ id: 'create_central_kitchen:crafting/dough_3' }, e)
R.remove({ id: 'create_central_kitchen:crafting/dough_4' }, e)
R.remove({ id: 'farmersdelight:wheat_dough_from_water' }, e)
R.remove({ id: 'tmted:bottling/wheat_dough' }, e)
R.replaceOutput({ output: i.out_flour }, i.out_flour, i.in_flour, e)
R.replaceInput({ input: i.out_flour }, i.out_flour, i.in_flour, e)
R.shapeless(Item.of(i.dough, 1), [i.flour, i.water_bucket], e)
R.ieBottling(i.dough, f.t_water, i.flour, e, undefined, 250)
R.ieBottling(i.dough_block, f.t_water, i.wheat_flour_pile, e, undefined, 1000)
R.ieCrushing(Item.of(i.flour, 2), i.wheat, e)
R.ieCrushing(Item.of(i.flour, 18), i.hay_bale, e)
// Crushing wheels + Millstone + mortar and pestle (foodGrinding, TODO food/kitchen #10)
R.foodGrinding([Item.of(i.flour, 13), Item.of(i.flour, 2).withChance(0.25)], i.hay_bale, e, 'hay_block_flour')
// PC wheat compat
R.remove({ id: 'pneumaticcraft:pressure_chamber/wheat_flour' }, e)
R.remove({ id: 'pneumaticcraft:explosion_crafting/wheat_flour' }, e)
R.pcPressureChamber(Item.of(i.flour, 3), [i.wheat], 1.5, e)
R.pcExplosionCrafting(Item.of(i.flour, 1), i.wheat, 50, e)
// More efficient craft w/ egg
R.remove({ id: 'tmted:wheat_dough_from_egg' }, e)
R.remove({ id: 'farmersdelight:wheat_dough_from_eggs' }, e)
R.shapeless(Item.of(i.dough, 6), [Item.of(i.flour, 3), i.eggs], e)

//######################### Cornmeal #########################
// creativeengineering:cornmeal (startup_scripts/cooking/cornmeal.js), TODO food/ingredients #1. Corn and kernels
// (corn_delight:corn_seeds) crush to it; a 9-block (corn crate, kernel bag) gives the same per-item ratio.
// The Millstone mirrors the crushing recipes below; Corn Delight's own corn and wild corn milling are removed (wild corn
// now grinds as corn). Corn gives no straw: the knife and IE crusher recipes that did keep their kernels only.
R.remove({ id: 'corn_delight:integration/create/milling/corn' }, e)
R.remove({ id: 'corn_delight:integration/create/milling/wild_corn' }, e)
R.remove({ id: 'corn_delight:cutting/corn' }, e)
R.remove({ id: 'corn_delight:cutting/wild_corn' }, e)
R.remove({ id: 'corn_delight:integration/immersiveengineering/crusher/wild_corn' }, e)
R.fdCutting(i.t_crops_corn, i.any_knife, Item.of(i.corn_seeds, 4), e, 'corn_kernels')   // 4 kernels a maize
R.fdCutting(i.wild_corn, i.any_knife, i.corn_seeds, e, 'wild_corn_kernels')
R.ieCrushing(i.corn_seeds, i.wild_corn, e, 'wild_corn_kernels', [], 3200)
// Crushing wheels, Millstone (same outputs) and mortar and pestle (foodGrinding; TODO food/kitchen #10,
// food/ingredients #14). Corn and wild corn 1.5 cornmeal + 0.25 kernels, kernels 0.333, crate 13.5, kernel bag 3.
// The mortar matches the machines' cornmeal average exactly: corn and wild corn 2 -> 3, kernels 3 -> 1. Crates and
// bags are machine-only, as are the kernels.
R.foodGrinding([i.cornmeal, Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 1).withChance(0.25)],
	i.t_crops_corn, e, 'corn_cornmeal', { mortar: { count: 3, inputs: 2 } })
R.foodGrinding([i.cornmeal, Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 1).withChance(0.25)],
	i.wild_corn, e, 'wild_corn_cornmeal', { mortar: { count: 3, inputs: 2 } })
R.foodGrinding([Item.of(i.cornmeal, 13), Item.of(i.cornmeal, 1).withChance(0.5), Item.of(i.corn_seeds, 2),
	Item.of(i.corn_seeds, 1).withChance(0.25)], i.corn_crate, e, 'corn_crate_cornmeal', { mortar: false })
R.foodGrinding([Item.of(i.cornmeal, 1).withChance(0.333)], i.t_seeds_corn, e, 'corn_kernels_cornmeal')
R.foodGrinding([Item.of(i.cornmeal, 3)], i.corn_kernel_bag, e, 'corn_kernel_bag_cornmeal', { mortar: false })
// IE crusher: corn 2 cornmeal + 0.5 kernels (replaces Corn Delight's corn -> kernels + straw), kernels 0.5 cornmeal.
// The crusher takes one item and can't roll its main output, so single kernels have no recipe; the bag does.
R.remove({ id: 'corn_delight:integration/immersiveengineering/crusher/corn' }, e)
R.ieCrushing(Item.of(i.cornmeal, 2), i.t_crops_corn, e, 'corn_cornmeal',
	[{ chance: 0.5, output: i.corn_seeds }], 3200)
R.ieCrushing(Item.of(i.cornmeal, 18), i.corn_crate, e, 'corn_crate_cornmeal',
	[{ chance: 1, output: Item.of(i.corn_seeds, 4) }, { chance: 0.5, output: i.corn_seeds }], 3200)
R.ieCrushing(Item.of(i.cornmeal, 4), i.corn_kernel_bag, e, 'corn_kernel_bag_cornmeal',
	[{ chance: 0.5, output: i.cornmeal }], 3200)
// PneumaticCraft: corn as wheat -> flour (3 at 1.5 bar, explosion 1 at 50% loss); kernels 4 -> 3. No crates/bags.
R.pcPressureChamber(Item.of(i.cornmeal, 3), [i.t_crops_corn], 1.5, e, 'corn_cornmeal')
R.pcExplosionCrafting(Item.of(i.cornmeal, 1), i.t_crops_corn, 50, e, 'corn_cornmeal')
R.pcPressureChamber(Item.of(i.cornmeal, 3), [Item.of(i.corn_seeds, 4)], 1.5, e, 'corn_kernels_cornmeal')
// Required in tortillas and cornbread batter (corn -> cornmeal, same counts). Raw tortilla: 3 cornmeal over a water
// bucket or bottle (the container comes back), or 1:1 by washing or the Bottler (250 mB water, as flour -> dough)
// in place of Corn Delight's washed corn (food/ingredients #14).
R.remove({ id: 'corn_delight:tortilla_raw' }, e)
R.remove({ id: 'corn_delight:integration/create/splashing/tortilla_raw' }, e)
R.shaped(Item.of(i.tortilla_raw, 3), ['CCC', ' W '], { C: i.cornmeal, W: [i.water_bucket, i.water_bottle] }, e, 'tortilla_raw')
R.createSplashing(i.tortilla_raw, i.cornmeal, e, 'cornmeal_tortilla_raw')
R.ieBottling(i.tortilla_raw, f.t_water, i.cornmeal, e, 'cornmeal_tortilla_raw', 250)
R.remove({ id: 'corn_delight:cornbread_batter' }, e)
R.remove({ id: 'corn_delight:integration/create/mixing/cornbread_batter_from_mixing' }, e)
R.shapeless(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), i.milk, i.eggs], e, 'cornbread_batter')
R.createMixing(Item.of(i.cornbread_batter, 3), [Item.of(i.cornmeal, 2), Fluid.of(f.milk, 250), i.eggs], e,
	'cornbread_batter', 'n/a')
//Add bread block recipes
R.remove({ id: 'architects_palette:bread_block' }, e)
R.remove({ id: 'architects_palette:bread_slab' }, e)
R.remove({ id: 'architects_palette:stonecutting/bread_nub' }, e)
R.smoking(i.bread_block, i.dough_block, e)
R.createSaw(Item.of(i.bread, 16), i.bread_block, e)
R.fdCutting(i.bread_block, i.any_knife, Item.of(i.bread, 16), e)
R.stonecutting(i.bread_slab, i.bread_block, e)
R.stonecutting(i.crustless_bread_block, i.bread_block, e)
R.stonecutting(i.bread_nub, i.bread_slab, e)
// Sourdough
//Swap flour to dough
R.remove({ id: 'pneumaticcraft:sourdough' }, e)
R.shapeless(Item.of(i.sourdough, 1), [i.yeast_culture_bucket, Item.of(i.dough, 8)], e)
// Make smoking
R.remove({ id: 'pneumaticcraft:sourdough_bread' }, e)
R.smoking(i.sourdough_bread, i.sourdough, e)
// slice to 2 bread
R.fdCutting(i.sourdough_bread, i.any_knife, Item.of(i.bread, 2), e)
// Mixing
R.createMixing(Item.of(i.sourdough, 8), [Fluid.of(f.yeast_culture, 500), Item.of(i.dough, 8)], e, undefined, 'heated')
// Bottling
R.ieBottling(i.sourdough, f.t_yeast_culture, i.dough, e, undefined, 50)

//######################### Pasta #########################
// More efficient press recipe for pasta
R.remove({ id: 'tmted:metalpress/raw_pasta' }, e)
R.ieMetalPress(Item.of(i.pasta, 2), i.dough, i.mold_wire, e)

//######################### Prepared #########################
// Scarlet Pierogi: beetroot in place of nether wart
R.replaceInput({ id: 'brewinandchewin:cooking/scarlet_pierogi' }, i.nether_wart, i.beetroot, e)

// Pizza: one dough on the bottom row in place of three wheat
R.remove({ id: 'brewinandchewin:pizza' }, e)
R.shaped(i.pizza, [' f ', 'ptp', ' d '], { f: i.t_cheese_wedge, p: i.t_pizza_topping, t: i.tomato_sauce, d: i.dough }, e, 'pizza')

// Ham and Cheese Sandwich: a side is a bread slice or a whole bread. Smoked ham cuts into two porkchops, so it gives double.
R.remove({ id: 'brewinandchewin:ham_and_cheese_sandwich' }, e)
var sandwichSide = [i.bread_slice, i.bread]
R.shapeless(Item.of(i.ham_and_cheese_sandwich, 1), [sandwichSide, i.cooked_pork, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich')
R.shapeless(Item.of(i.ham_and_cheese_sandwich, 2), [sandwichSide, i.smoked_ham, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich_smoked_ham')
var incSandwich = R.incomplete('ham_and_cheese_sandwich')
R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 2), i.bread_slice, [
	e.recipes.create.deploying(incSandwich, [incSandwich, i.cooked_pork]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.bread_slice])
], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_sequenced_assembly', 1)
R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 4), i.bread_slice, [
	e.recipes.create.deploying(incSandwich, [incSandwich, i.smoked_ham]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.bread_slice])
], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_smoked_ham_sequenced_assembly', 1)

// Quiche: one recipe for bacon and mushroom (JEI cycles them), a cabbage leaf or a whole cabbage. TODO food/meals #2
R.remove({ id: 'brewinandchewin:quiche_from_bacon' }, e)
R.remove({ id: 'brewinandchewin:quiche_from_mushroom' }, e)
var quicheFilling = [i.cooked_bacon, i.brown_mushroom]
R.shaped(i.quiche, ['blb', 'mcm', 'eCe'], {
	b: quicheFilling, l: [i.cabbage_leaf, i.cabbage], m: i.milk, c: i.t_cheese_wedge, e: i.minecraft_egg, C: i.pie_crust
}, e, 'quiche_manual_only')
// Sequenced assembly, one for both fillings
var incQuiche = R.incomplete('quiche')
R.createSequencedAssembly(i.quiche, i.pie_crust, [
	e.recipes.create.deploying(incQuiche, [incQuiche, Ingredient.of(quicheFilling)]),
	e.recipes.create.filling(incQuiche, [incQuiche, Fluid.of(f.milk, 250)]),
	e.recipes.create.deploying(incQuiche, [incQuiche, i.eggs]),
	e.recipes.create.deploying(incQuiche, [incQuiche, i.eggs]),
	e.recipes.create.deploying(incQuiche, [incQuiche, i.t_cheese_wedge]),
	e.recipes.create.deploying(incQuiche, [incQuiche, Ingredient.of([i.cabbage, i.cabbage_leaf])])
], ['quiche'], e, 'quiche_sequenced_assembly', 1)

//Pancakes
//Wheat to flour (or cornmeal, or rice)
R.remove({ id: 'supplementaries:pancake' }, e)
R.shapeless(Item.of(i.pancakes, 3), [i.any_sugar, i.egg, i.milk, [i.t_flour_or_cornmeal, i.rice]], e)
// Efficient mixing, cooking
R.createMixing(Item.of(i.pancakes, 5), [Fluid.of(f.milk, 250), i.any_sugar, i.egg, [i.t_flour_or_cornmeal, i.rice]], e, undefined, 'heated')

//Barbecue stick
//Remove exising, replace w/ cooked meat tag
R.remove({ id: 'farmersdelight:barbecue_stick' }, e)
R.shapeless(Item.of(i.bbq_stick, 1), [i.onion, i.tomato, i.cooked_meat, i.stick], e)
const incBbqStick = R.incomplete('bbq_stick')
R.createSequencedAssembly(Item.of(i.bbq_stick, 1), i.stick, [
	e.recipes.create.deploying(incBbqStick, [incBbqStick, i.onion]),
	e.recipes.create.deploying(incBbqStick, [incBbqStick, i.tomato]),
	e.recipes.create.deploying(incBbqStick, [incBbqStick, i.cooked_meat])
], ['bbq_stick'], e, 'bbq_stick_sequenced_assembly', 1)
// Crab Bar
//	Remove raw shapeless crab bar recipe, replace w/ cooked at lower efficiency
R.remove({ id: 'quarkdelight:crab_bars' }, e)
R.shapeless(Item.of(i.crab_bars, 2), [i.cooked_fish, i.cooked_crab, i.bonemeal], e, 'creativeengineering/crab_bars_shapeless_manual_only')
// Add raw ingredient cooking crab bar recipe
R.fdCooking([i.raw_fish, i.crab, i.bonemeal], Item.of(i.crab_bars, 4), e, undefined, 1, 200)
// Sequenced assembly
const incCrabBars = R.incomplete('crab_bars')
R.createSequencedAssembly(Item.of(i.crab_bars, 10), i.bonemeal, [
	e.recipes.create.deploying(incCrabBars, [incCrabBars, i.cooked_crab]),
	e.recipes.create.deploying(incCrabBars, [incCrabBars, i.cooked_fish]),
	e.recipes.create.pressing(incCrabBars, incCrabBars)
], ['crab_bars'], e, 'crab_bars_sequenced_assembly', 2)
// Fish stew
R.shapeless(Item.of(i.fish_stew, 1), [i.cooked_fish, i.cooked_fish, Item.of(i.tomato_sauce, 2), i.onion, i.bowl], e, 'fish_stew_cooked_manual_only')

// Cabbage rolls
R.shapeless(Item.of(i.cabbage_rolls, 1), [i.cabbage, i.cabbage, i.cabbage_roll_veg, i.cabbage_roll_veg], e, 'cabbage_rolls_manual_only')

//Grilled salmon
// Replace shaped recipe w/ shaped
R.remove({ id: 'farmersdelight:grilled_salmon' }, e)
R.shapeless(Item.of(i.grilled_salmon, 1), [i.onion, i.cabbage, i.sweet_berries, i.cooked_salmon, i.bowl], e)
// Sequenced assembly
const incGrilledSalmon = R.incomplete('grilled_salmon')
R.createSequencedAssembly(Item.of(i.grilled_salmon, 1), i.bowl, [
	e.recipes.create.deploying(incGrilledSalmon, [incGrilledSalmon, i.onion]),
	e.recipes.create.deploying(incGrilledSalmon, [incGrilledSalmon, i.cabbage]),
	e.recipes.create.deploying(incGrilledSalmon, [incGrilledSalmon, i.sweet_berries]),
	e.recipes.create.deploying(incGrilledSalmon, [incGrilledSalmon, i.cooked_salmon])
], ['grilled_salmon'], e, 'grilled_salmon_sequenced_assembly', 1)

// Frying with cooking oil in the pot, any whole fish (TODO food/meals #7). PneumaticCraft's Thermopneumatic deep
// frying is gone; Slice & Dice gives each pot recipe its heated mixing copy. Renames in namechanges.js:
// Fish Tempura, Raw Fish Tempura, Fries, Fish and Chips.
R.remove({ id: 'pneumaticcraft:thermo_plant/chips' }, e)
R.remove({ id: 'pneumaticcraft:thermo_plant/salmon_tempura' }, e)
R.remove({ id: 'pneumaticcraft:raw_salmon_tempura' }, e)
R.remove({ id: 'pneumaticcraft:cod_n_chips' }, e)
R.shapeless(i.raw_salmon_tempura, [i.t_food_raw_fish, i.batter], e, 'raw_fish_tempura')
R.fdCooking([i.t_cooking_oil, i.raw_salmon_tempura], i.salmon_tempura, e, 'fish_tempura', 0.35, 200)
R.fdCooking([i.t_cooking_oil, i.t_sliced_potatoes, i.t_sliced_potatoes], Item.of(i.chips, 2), e, 'fries', 0.35, 200)
R.fdCooking([i.t_cooking_oil, i.t_food_raw_fish, i.t_sliced_potatoes, i.t_sliced_potatoes], i.cod_n_chips, e,
	'fish_and_chips', 0.35, 200, i.paper)
R.shapeless(i.cod_n_chips, [i.chips, i.cooked_fish, i.paper], e, 'fish_and_chips_cooked_manual_only')

// Bacon and eggs
// Replace shaped recipe w/ shaped
R.remove({ id: 'farmersdelight:bacon_and_eggs' }, e)
R.shapeless(Item.of(i.bacon_and_eggs, 1), [i.cooked_bacon, i.cooked_bacon, i.cooked_eggs, i.cooked_eggs, i.bowl], e)
// Sequenced assembly
const incBaconAndEggs = R.incomplete('bacon_and_eggs')
R.createSequencedAssembly(Item.of(i.bacon_and_eggs, 1), i.bowl, [
	e.recipes.create.deploying(incBaconAndEggs, [incBaconAndEggs, i.cooked_bacon]),
	e.recipes.create.deploying(incBaconAndEggs, [incBaconAndEggs, i.cooked_eggs])
], ['bacon_and_eggs'], e, 'bacon_eggs_sequenced_assembly', 2)

//Salmon roll
//Make shapeless, already shapeless?
R.remove({ id: 'farmersdelight:salmon_roll' }, e)
R.shapeless(Item.of(i.salmon_roll, 2), [i.salmon_slice, i.salmon_slice, i.cooked_rice], e)
// Sequenced assembly
const incSalmonRoll = R.incomplete('salmon_roll')
R.createSequencedAssembly(Item.of(i.salmon_roll, 3), i.cooked_rice, [
	e.recipes.create.deploying(incSalmonRoll, [incSalmonRoll, i.salmon_slice])
], ['salmon_roll'], e, 'salmon_roll_sequenced_assembly', 3)
//Cod roll
//Make shapeless, already shapeless?
R.remove({ id: 'farmersdelight:cod_roll' }, e)
R.shapeless(Item.of(i.cod_roll, 2), [i.cod_slice, i.cod_slice, i.cooked_rice], e)
// Sequenced assembly
const incCodRoll = R.incomplete('cod_roll')
R.createSequencedAssembly(Item.of(i.cod_roll, 3), i.cooked_rice, [
	e.recipes.create.deploying(incCodRoll, [incCodRoll, i.cod_slice])
], ['cod_roll'], e, 'cod_roll_sequenced_assembly', 3)
//Kelp roll
// Sequenced assembly
const incKelpRoll = R.incomplete('kelp_roll')
R.createSequencedAssembly(Item.of(i.kelp_roll, 1), i.cooked_rice, [
	e.recipes.create.deploying(incKelpRoll, [incKelpRoll, i.carrot]),
	e.recipes.create.deploying(incKelpRoll, [incKelpRoll, i.dried_kelp]),
	e.recipes.create.deploying(incKelpRoll, [incKelpRoll, i.dried_kelp])
], ['kelp_roll'], e, 'kelp_roll_sequenced_assembly', 1)
//Rice Medley
// Sequenced assembly
const incRiceMedley = R.incomplete('rice_medley')
R.createSequencedAssembly(Item.of(i.rice_medley, 1), i.bowl, [
	e.recipes.create.deploying(incRiceMedley, [incRiceMedley, i.cod_roll]),
	e.recipes.create.deploying(incRiceMedley, [incRiceMedley, i.salmon_roll]),
	e.recipes.create.deploying(incRiceMedley, [incRiceMedley, i.kelp_roll_slice])
], ['rice_medley'], e, 'rice_medley_sequenced_assembly', 2)

//Horror Lasagna
//Make cooked shapeless
R.shapeless(Item.of(i.horror_lasagna, 1), [i.scarlet_cheese_wedge, i.scarlet_cheese_wedge, i.beetroot_soup, i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_horror_lasagna_meat, i.cooked_horror_lasagna_meat, i.bowl], e, 'horror_lasagna_cooked_manual_only')

//Pasta w/ mutton
//Make cooked shapeless
R.shapeless(Item.of(i.pasta_with_mutton, 1), [i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_mutton, i.cooked_mutton, i.bowl], e, 'pasta_with_mutton_cooked_manual_only')
//Add bulk mixing
R.createMixing(Item.of(i.pasta_with_mutton, 4), [Fluid.of(f.tomato_sauce, 1000), i.pasta, i.pasta, i.pasta, i.raw_mutton, i.raw_mutton, Item.of(i.bowl, 4)], e, undefined, 'heated')

//Roasted mutton chops
// Sequenced assembly
const incRoastedMuttonChops = R.incomplete('roasted_mutton_chops')
R.createSequencedAssembly(Item.of(i.roasted_mutton_chops, 1), i.bowl, [
	e.recipes.create.deploying(incRoastedMuttonChops, [incRoastedMuttonChops, i.tomato]),
	e.recipes.create.deploying(incRoastedMuttonChops, [incRoastedMuttonChops, i.beetroot]),
	e.recipes.create.deploying(incRoastedMuttonChops, [incRoastedMuttonChops, i.cooked_rice]),
	e.recipes.create.deploying(incRoastedMuttonChops, [incRoastedMuttonChops, i.cooked_mutton_chops])
], ['roasted_mutton_chops'], e, 'roast_mutton_sequenced_assembly', 1)

//######################### Meals #########################
// Heatless meal rule (TODO food/meals #1). A pot meal can also be crafted shapeless from the cooked form of each raw
// ingredient: raw meat -> i.cooked_beef / cooked_pork / cooked_chicken / cooked_mutton / cooked_fish / cooked_meat;
// potato -> i.baked_potato; tomato -> i.tomato_sauce (bowl, consumed); rice -> i.cooked_rice; egg -> i.cooked_eggs;
// any veggie -> i.t_soup_veggie_cooked; raw pasta stays i.pasta. Soups and stews add i.bone_broth, which brings the
// bowl (so no i.bowl next to it or next to tomato sauce). The grid route is the expensive one: at least one more main
// ingredient than the pot, or half the pot's count. Ingredients with no cooked form (carrot, beetroot, mushrooms) stay
// raw. Every one gets an id ending _manual_only: Create would otherwise run the shapeless recipe in an unheated basin,
// and mechanical crafters run shaped meals that have an assembly.

// Pot meals with no grid route yet
R.shapeless(i.vegetable_soup, [i.bone_broth, i.carrot, i.baked_potato, i.beetroot, i.salad_ingredients, i.t_soup_veggie_cooked], e, 'vegetable_soup_cooked_manual_only')
R.shapeless(i.vegetable_noodles, [i.carrot, i.brown_mushroom, i.pasta, i.salad_ingredients, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked, i.bowl], e, 'vegetable_noodles_cooked_manual_only')
R.shapeless(i.ratatouille, [i.tomato_sauce, i.onion, i.beetroot, i.t_soup_veggie_cooked, i.t_soup_veggie_cooked], e, 'ratatouille_cooked_manual_only')
var cookedCorn = [i.boiled_corn, i.grilled_corn]
R.shapeless(i.corn_soup, [i.bone_broth, cookedCorn, cookedCorn, i.salad_ingredients,
	[i.cooked_chicken, i.cooked_pork, i.cooked_beef, i.brown_mushroom], i.milk], e, 'corn_soup_cooked_manual_only')

// Expensive grid routes for pot meals (TODO food/meals #4). Baked cod stew is in "Soups and bases".
// Chorus and endermite stews come in Ender's Delight's shulker bowl; the grid dumplings make 1 (the pot makes 2).
R.shapeless(i.chorus_stew, [i.popped_chorus_fruit, i.popped_chorus_fruit, i.chorus_flower, i.shulker_bowl], e, 'chorus_stew_cooked_manual_only')
R.shapeless(i.endermite_stew, [i.enderman, i.mite_crust, i.mite_crust, i.shulker_bowl], e, 'endermite_stew_cooked_manual_only')
R.shapeless(i.crab_pasta, [i.cooked_crab, i.cooked_crab, i.pasta, i.t_soup_veggie_cooked, i.bowl], e, 'crab_pasta_cooked_manual_only')
R.shapeless(i.pumpkin_soup, [i.bone_broth, i.pumpkin_slice, i.pumpkin_slice, i.salad_ingredients, i.cooked_pork, i.milk], e, 'pumpkin_soup_cooked_manual_only')
R.shapeless(i.pasta_with_meatballs, [i.beef_patty, i.beef_patty, i.pasta, i.tomato_sauce], e, 'pasta_with_meatballs_cooked_manual_only')
R.shapeless(i.dumplings, [i.any_dough, i.cabbage, i.onion, [i.cooked_meat, i.brown_mushroom]], e, 'dumplings_cooked_manual_only')
R.shapeless(i.cavern_explorer_stew, [i.bone_broth, i.glow_shroom, i.glow_shroom, i.glow_ink, i.t_soup_veggie_cooked], e, 'cavern_explorer_stew_cooked_manual_only')
// Squid ink pasta: the grid, or ink + tomato sauce in a heated basin (2 a batch)
R.shapeless(i.squid_ink_pasta, [i.cooked_fish, i.pasta, i.tomato_sauce, i.ink_sac, i.ink_sac], e, 'squid_ink_pasta_cooked_manual_only')
R.createMixing(Item.of(i.squid_ink_pasta, 2), [Fluid.of(f.ink, 250), Fluid.of(f.tomato_sauce, 250),
	i.pasta, i.pasta, i.raw_fish, Item.of(i.bowl, 2)], e, 'squid_ink_pasta_mixing', 'heated')

// Dog food: two cooked meats for the pot's one raw; cooked rice brings the bowl (TODO food/meals #8)
R.shapeless(i.dog_food, [i.rotten_flesh, i.bonemeal, i.cooked_meat, i.cooked_meat, i.cooked_rice], e, 'dog_food_cooked_manual_only')

// Mutton wrap: a tortilla, not bread (FD's grid recipe and CCK's assembly both took bread). TODO food/meals #2
R.remove({ id: 'farmersdelight:mutton_wrap' }, e)
R.remove({ id: 'create_central_kitchen:sequenced_assembly/mutton_wrap' }, e)
R.shapeless(i.mutton_wrap, [i.t_tortilla, i.cooked_mutton, i.salad_ingredients, i.onion], e, 'mutton_wrap_manual_only')
var incMuttonWrap = R.incomplete('mutton_wrap')
R.createSequencedAssembly(i.mutton_wrap, i.t_tortilla, [
	e.recipes.create.deploying(incMuttonWrap, [incMuttonWrap, i.cooked_mutton]),
	e.recipes.create.deploying(incMuttonWrap, [incMuttonWrap, i.salad_ingredients]),
	e.recipes.create.deploying(incMuttonWrap, [incMuttonWrap, i.onion])
], ['mutton_wrap'], e, 'mutton_wrap_sequenced_assembly', 1)

// Scrambled eggs: one more egg than Incubation's
R.remove({ id: 'incubation:scrambled_eggs' }, e)
R.shapeless(i.scrambled_eggs, [i.eggs, i.eggs, i.eggs, i.milk, i.bowl], e, 'scrambled_eggs_manual_only')

// Vegetable omelet: the pot takes fried eggs; or fold two omelettes (each one cooked fried egg) with the veggies
R.replaceInput({ id: 'brewinandchewin:cooking/vegetable_omelet' }, i.eggs, i.cooked_eggs, e)
R.shapeless(i.vegetable_omelet, [i.omelette, i.omelette, i.t_cheese_wedge, i.onion, i.carrot, i.bowl], e, 'vegetable_omelet_from_omelette_manual_only')

// Exotic fruits: any Fruits Delight fruit (or melon) in the melon slot
R.remove({ id: 'quarkdelight:bowl_of_exotic_fruits' }, e)
R.shapeless(i.bowl_of_exotic_fruits, [i.ancient_fruit, i.t_exotic_fruit, i.chorus_fruit, i.bowl], e, 'bowl_of_exotic_fruits_manual_only')

// Fruit salad: any melon or fruit slice for the two melon slices (FD's and Delightful's recipes share the id)
R.remove({ id: 'farmersdelight:fruit_salad' }, e)
R.shapeless(i.fruit_salad, [i.apple, i.t_fruit_salad_slice, i.t_fruit_salad_slice, i.t_berries, i.t_berries,
	i.pumpkin_slice, i.bowl], e, 'fruit_salad_manual_only')
var incFruitSalad = R.incomplete('fruit_salad')
R.createSequencedAssembly(Item.of(i.fruit_salad, 2), i.bowl, [
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.apple]),
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.t_fruit_salad_slice]),
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.t_fruit_salad_slice]),
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.t_berries]),
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.t_berries]),
	e.recipes.create.deploying(incFruitSalad, [incFruitSalad, i.pumpkin_slice])
], ['fruit_salad'], e, 'fruit_salad_sequenced_assembly', 1)

// Burgers: shaped, bread or cornbread (any #forge:bread), cheese wedges never milk; the assembly is the only
// automation. Pattern: toppings on top, patty in the middle, bread at the bottom.
// Assemblies on bread need a different first step each, or Create only ever picks one of them: CCK's bacon, chicken
// and egg sandwiches already start with cooked bacon, cooked chicken and a fried egg. So the egg burger starts on
// the patty instead of the bread.
;[
	'farmersdelight:hamburger', 'create_central_kitchen:sequenced_assembly/hamburger',
	'delightful:food/cheeseburger', 'delightful:food/cheeseburger_from_milk',
	'delightful:food/deluxe_cheeseburger', 'delightful:food/deluxe_cheeseburger_from_milk',
	'moredelight:simple_hamburger', 'moredelight:hamburger_with_egg', 'moredelight:loaded_hamburger',
	'alexsdelight:bison_burger', 'alexsmobs:kangaroo_burger',
].forEach(id => R.remove({ id: id }, e))
var burgerCheese = i.t_cheese_wedge
;[
	// [key, pattern, keys, assembly base, deploy steps in order]
	['hamburger', ['STO', ' P ', ' B '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, P: i.beef_patty, B: i.t_bread },
		i.t_bread, [i.salad_ingredients, i.beef_patty, i.tomato, i.onion]],
	['cheeseburger', ['STO', 'CP ', ' B '], { S: i.salad_ingredients, T: i.tomato, O: i.onion, C: burgerCheese, P: i.beef_patty, B: i.t_bread },
		i.t_bread, [burgerCheese, i.beef_patty, i.salad_ingredients, i.tomato, i.onion]],
	['deluxe_cheeseburger', ['STO', 'PAP', 'CBC'], { S: i.salad_ingredients, T: i.tomato, O: i.onion, P: i.beef_patty, A: i.cooked_bacon, C: burgerCheese, B: i.t_bread },
		i.t_bread, [i.onion, i.beef_patty, burgerCheese, i.beef_patty, burgerCheese, i.cooked_bacon, i.salad_ingredients, i.tomato]],
	['simple_hamburger', ['P', 'B'], { P: i.beef_patty, B: i.t_bread },
		i.t_bread, [i.beef_patty]],
	['hamburger_with_egg', ['E', 'P', 'B'], { E: i.cooked_eggs, P: i.beef_patty, B: i.t_bread },
		i.beef_patty, [i.cooked_eggs, i.t_bread]],
	['loaded_hamburger', ['CTO', 'APE', ' B '], { C: i.cabbage, T: i.tomato, O: i.onion, A: i.cooked_bacon, P: i.beef_patty, E: i.cooked_eggs, B: i.t_bread },
		i.t_bread, [i.tomato, i.beef_patty, i.cabbage, i.cooked_bacon, i.cooked_eggs, i.onion]],
	['bison_burger', ['STR', ' P ', ' B '], { S: i.salad_ingredients, T: i.tomato, R: i.beetroot, P: i.bison_patty, B: i.t_bread },
		i.t_bread, [i.bison_patty, i.salad_ingredients, i.tomato, i.beetroot]],
	['kangaroo_burger', ['S C', 'K K', ' B '], { S: i.salad_ingredients, C: i.carrot, K: i.t_cooked_kangaroo, B: i.t_bread },
		i.t_bread, [i.t_cooked_kangaroo, i.t_cooked_kangaroo, i.salad_ingredients, i.carrot]],
].forEach(([key, pattern, keys, base, steps]) => {
	R.shaped(i[key], pattern, keys, e, key + '_manual_only')
	var inc = R.incomplete(key)
	R.createSequencedAssembly(i[key], base,
		steps.map(s => e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])),
		[key], e, key + '_sequenced_assembly', 1)
})

// Assembly meals (TODO food/meals #3)
// Shepherd's pie: a cheese wedge in place of milk
R.remove({ id: 'farmersdelight:shepherds_pie_block' }, e)
R.shapeless(i.shepherds_pie_block, [i.baked_potato, i.baked_potato, i.t_cheese_wedge, i.cooked_mutton, i.cooked_mutton,
	i.cooked_mutton, i.onion, i.onion, i.bowl], e, 'shepherds_pie_manual_only')
var incShepherdsPie = R.incomplete('shepherds_pie')
R.createSequencedAssembly(i.shepherds_pie_block, i.bowl, [
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.cooked_mutton]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.cooked_mutton]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.cooked_mutton]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.onion]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.onion]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.baked_potato]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.baked_potato]),
	e.recipes.create.deploying(incShepherdsPie, [incShepherdsPie, i.t_cheese_wedge])
], ['shepherds_pie'], e, 'shepherds_pie_sequenced_assembly', 1)
// Horror lasagna: the efficient route, 2 a run. Pasta goes in first: a sequenced assembly's first step wins over a
// plain recipe, so a bowl under a spout of tomato sauce would turn into lasagna instead of tomato sauce.
var incHorrorLasagna = R.incomplete('horror_lasagna')
R.createSequencedAssembly(Item.of(i.horror_lasagna, 2), i.bowl, [
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.pasta]),
	e.recipes.create.filling(incHorrorLasagna, [incHorrorLasagna, Fluid.of(f.tomato_sauce, 250)]),
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.beetroot]),
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.scarlet_cheese_wedge]),
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.cooked_horror_lasagna_meat]),
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.cooked_horror_lasagna_meat]),
	e.recipes.create.deploying(incHorrorLasagna, [incHorrorLasagna, i.scarlet_cheese_wedge])
], ['horror_lasagna'], e, 'horror_lasagna_sequenced_assembly', 1)
// Stuffed potato: FD's shapeless stays
var incStuffedPotato = R.incomplete('stuffed_potato')
R.createSequencedAssembly(i.stuffed_potato, i.baked_potato, [
	e.recipes.create.deploying(incStuffedPotato, [incStuffedPotato, i.cooked_beef]),
	e.recipes.create.filling(incStuffedPotato, [incStuffedPotato, Fluid.of(f.milk, 250)])
], ['stuffed_potato'], e, 'stuffed_potato_sequenced_assembly', 1)
// Crispy skewer: shaped, a diagonal skewer
R.remove({ id: 'endersdelight:crispy_skewer' }, e)
R.shaped(i.crispy_skewer, [' MC', ' P ', 'S  '], { M: i.mite_crust, C: i.chorus_fruit, P: i.popped_chorus_fruit, S: i.stick },
	e, 'crispy_skewer_manual_only')
var incCrispySkewer = R.incomplete('crispy_skewer')
R.createSequencedAssembly(i.crispy_skewer, i.stick, [
	e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.chorus_fruit]),
	e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.popped_chorus_fruit]),
	e.recipes.create.deploying(incCrispySkewer, [incCrispySkewer, i.mite_crust])
], ['crispy_skewer'], e, 'crispy_skewer_sequenced_assembly', 1)
// Crawling sandwich: shaped
R.remove({ id: 'endersdelight:crawling_sandwich' }, e)
R.shaped(i.crawling_sandwich, ['SMC', ' B '], { S: i.shulker_loot, M: i.mite_crust, C: i.chorus_fruit, B: i.minecraft_bread },
	e, 'crawling_sandwich_manual_only')
var incCrawlingSandwich = R.incomplete('crawling_sandwich')
R.createSequencedAssembly(i.crawling_sandwich, i.minecraft_bread, [
	e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.shulker_loot]),
	e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.mite_crust]),
	e.recipes.create.deploying(incCrawlingSandwich, [incCrawlingSandwich, i.chorus_fruit])
], ['crawling_sandwich'], e, 'crawling_sandwich_sequenced_assembly', 1)
// Steak and potatoes: shaped
R.remove({ id: 'farmersdelight:steak_and_potatoes' }, e)
R.shaped(i.steak_and_potatoes, ['ROP', ' B ', ' W '], { R: i.cooked_rice, O: i.onion, P: i.baked_potato, B: i.cooked_beef, W: i.bowl },
	e, 'steak_and_potatoes_manual_only')
var incSteakAndPotatoes = R.incomplete('steak_and_potatoes')
R.createSequencedAssembly(i.steak_and_potatoes, i.bowl, [
	e.recipes.create.deploying(incSteakAndPotatoes, [incSteakAndPotatoes, i.cooked_rice]),
	e.recipes.create.deploying(incSteakAndPotatoes, [incSteakAndPotatoes, i.baked_potato]),
	e.recipes.create.deploying(incSteakAndPotatoes, [incSteakAndPotatoes, i.cooked_beef]),
	e.recipes.create.deploying(incSteakAndPotatoes, [incSteakAndPotatoes, i.onion])
], ['steak_and_potatoes'], e, 'steak_and_potatoes_sequenced_assembly', 1)

//######################### Sweets #########################
// Cakes, pies, cookies, pie crust and cake base: all in baking.js (baking overhaul, feature_writeup/oven.md 2.6).
//Remove redundant pie
R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice' }, e)
R.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice_using_deployer' }, e)
R.remove({ id: 'create_central_kitchen:crafting/pumpkin_pie_from_slices' }, e)

//Muffins
// Fruits Delight's pot recipes ask for #forge:dough/wheat; use the general #forge:dough tag instead
// (TODO food/oven #3, "muffins not cookable?"). Otherwise identical: 2 berries, dough, milk bottle, egg, sugar ->
// 2 muffins, 0.1 xp, 200 ticks. #forge:sugar as the line-59 sugar swap gives every other recipe. The
// oven copies are in baking.js.
R.remove({ id: 'farmersdelight:cooking/blueberry_muffin' }, e)
R.remove({ id: 'farmersdelight:cooking/cranberry_muffin' }, e)
R.fdCooking([i.t_fruits_blueberry, i.t_fruits_blueberry, i.any_dough, i.t_milk_bottle, i.eggs, i.any_sugar],
	Item.of(i.blueberry_muffin, 2), e, 'blueberry_muffin', 0.1, 200)
R.fdCooking([i.t_fruits_cranberry, i.t_fruits_cranberry, i.any_dough, i.t_milk_bottle, i.eggs, i.any_sugar],
	Item.of(i.cranberry_muffin, 2), e, 'cranberry_muffin', 0.1, 200)

//Honey
//Honey item removals
R.remove({ id: 'buzzier_bees:honey/honey_apple' }, e)

//######################### Protein #########################
//Egg
//Remove redundant fried egg
R.remove({ id: 'incubation:fried_egg' }, e)
R.remove({ id: 'incubation:fried_egg_from_smoking' }, e)
R.remove({ id: 'incubation:fried_egg_from_campfire_cooking' }, e)
// Abnormals Delight overrides Farmer's Delight's three fried egg recipes with copies that don't load when
// Incubation is installed, which left only the cooking oil recipe. Put Farmer's Delight's back.
R.smelting(i.farmersdelight_fried_egg, i.minecraft_egg, e, undefined, 0.35, 200)
R.smoking(i.farmersdelight_fried_egg, i.minecraft_egg, e, undefined, 0.35, 100)
R.campfireCooking(i.farmersdelight_fried_egg, i.minecraft_egg, e, undefined, 0.35, 600)

//Cooking oil
// Slice & Dice turns Cooking Pot recipes into heated mixing after KubeJS runs, so R.remove can't reach its copy;
// it skips ids ending in _manual_only. Re-add the pot recipe under such an id and give the mixer its own ratio.
R.remove({ id: 'rusticdelight:cooking/cooking_oil' }, e)
R.fdCooking([i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients,
	i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients],
	Item.of(i.cooking_oil, 2), e, 'cooking_oil_manual_only', 0.35, 100, i.glass_bottle)
R.createMixing(Item.of(i.cooking_oil, 1), [i.t_cooking_oil_ingredients, i.t_cooking_oil_ingredients,
	i.t_cooking_oil_ingredients, i.glass_bottle], e, 'cooking_oil_heated_mixing', 'heated')

//Fish
//Increase slicing yield
R.remove({ id: 'farmersdelight:cutting/salmon' }, e)
R.remove({ id: 'farmersdelight:cutting/cod' }, e)
R.remove({ id: 'alexsdelight:cutting/raw_catfish_slice' }, e)

R.fdCutting(i.salmon, i.any_knife, [Item.of(i.salmon_slice, 6), Item.of(i.bonemeal, 1)], e)
R.fdCutting(i.cod, i.any_knife, [Item.of(i.cod_slice, 6), Item.of(i.bonemeal, 1)], e)
R.fdCutting(i.catfish, i.any_knife, Item.of(i.catfish_slice, 6), e)

//######################### Ender #########################
//Ender food changes
//Remove wood bowl items
R.remove({ id: 'endersdelight:twisted_cereal_wood' }, e)
R.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block' }, e)
R.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block_uncompress' }, e)
// Stuffed shulker shapeless
R.shapeless(Item.of(i.stuffed_shulker, 1), [i.tomato_sauce, i.tomato_sauce, i.cooked_rice, i.cooked_rice, i.brown_mushroom, i.chorus_fruit, i.shulker_mollusk, i.shulker_shell], e, 'stuffed_shulker_cooked_manual_only')
//Ender paella shapeless
R.shapeless(Item.of(i.ender_paella, 1), [i.cooked_rice, i.cooked_rice, i.shulker_flesh, i.chorus_flower, i.chorus_flower, i.ender_sight, i.shulker_bowl], e, 'ender_paella_cooked_manual_only')
//Pearl pasta shapeless
R.shapeless(Item.of(i.pearl_pasta, 1), [i.enderman, i.pasta, i.pasta, i.chorus_juice, i.chorus_juice, i.shulker_bowl], e, 'pearl_pasta_cooked_manual_only')

//######################### Misc #########################
//Crates
//Remove redundant crates
R.remove({ id: 'quark:building/crafting/compressed/carrot_crate' }, e)
R.remove({ id: 'quark:building/crafting/compressed/carrot_crate_uncompress' }, e)
R.remove({ id: 'quark:building/crafting/compressed/beetroot_crate' }, e)
R.remove({ id: 'quark:building/crafting/compressed/beetroot_crate_uncompress' }, e)
R.remove({ id: 'quark:building/crafting/compressed/potato_crate' }, e)
R.remove({ id: 'quark:building/crafting/compressed/potato_crate_uncompress' }, e)
R.remove({ id: 'quark:building/crafting/compressed/apple_crate' }, e)
R.remove({ id: 'quark:building/crafting/compressed/apple_crate_uncompress' }, e)

console.log('The cooking event has finished!')
})


/*ServerEvents.recipes(e => {

    console.log('Hello! The cooking event has started!')

    const CE = global.CE

    let food = {};
    let item = {};
    let fluid = {};
    //Frequently used food assignments
    f.milk = '#forge:milk';
    i.bone_broth = 'farmersdelight:bone_broth';
    i.cooked_rice = 'farmersdelight:cooked_rice';
    i.out_flour = 'tmted:wheat_flour';
    i.out_dough = 'farmersdelight:wheat_dough';
    i.in_flour = 'create:wheat_flour';
    i.in_dough = 'create:dough';
    i.hay_bale = 'minecraft:hay_bale';
    i.wheat = 'minecraft:wheat';
    i.flour = '#forge:flour/wheat';
    i.dough = '#forge:dough/wheat';
    i.bread = '#forge:bread/wheat';
    i.bread_block = 'architects_palette:bread_block';
    i.sourdough = 'pneumaticcraft:sourdough';
    i.sourdough_bread = 'pneumaticcraft:sourdough_bread';
    i.pasta = '#forge:pasta';
    i.sugar = '#forge:sugar';
    i.vegetables = '#forge:vegetables';
    i.salad_ingredients = '#forge:salad_ingredients';
    i.tomato_sauce = 'farmersdelight:tomato_sauce';
    i.fruit = '#forge:fruits';
    i.sweet_berries = 'minecraft:sweet_berries';
    i.melon = 'minecraft:melon_slice';
    i.melon_block = 'minecraft:melon';
    i.chorus_fruit = 'minecraft:chorus_fruit';
    i.chorus_flower = 'minecraft:chorus_flower';
    i.carrot = '#forge:crops/carrot';
    i.potato = '#forge:crops/potato';
    i.beetroot = '#forge:crops/beetroot'
    i.beetroot_soup = 'minecraft:beetroot_soup'
    i.baked_potato = 'minecraft:baked_potato';
    i.onion = '#forge:crops/onion';
    i.tomato = '#forge:crops/tomato';
    i.rice = '#forge:crops/rice';
    i.mushrooms = '#forge:mushrooms';
    i.brown_mushroom = 'minecraft:brown_mushroom';
    i.dried_kelp = 'minecraft:dried_kelp';
    i.cabbage = '#forge:crops/cabbage';
    i.cabbage_rolls = 'farmersdelight:cabbage_rolls';
    i.cabbage_roll_veg = '#farmersdelight:cabbage_roll_ingredients';
    i.cooked_beef = '#forge:cooked_beef';
    i.cooked_meat = '#forge:cooked_meat';
    i.raw_beef = '#forge:raw_beef';
    i.raw_mutton = '#forge:raw_mutton';
    i.cooked_mutton = '#forge:cooked_mutton';
    i.cooked_mutton_chops = 'farmersdelight:cooked_mutton_chops';
    i.cooked_pork = '#forge:cooked_pork';
    i.raw_pork = '#forge:raw_pork';
    i.cooked_bacon = 'farmersdelight:cooked_bacon';
    i.raw_chicken = '#forge:raw_chicken';
    i.cooked_chicken = '#forge:cooked_chicken';
    i.cooked_eggs = '#forge:cooked_eggs';
    	i.cooked_egg = '#forge:cooked_eggs';
    	i.fried_eggs = '#forge:cooked_eggs';
    	i.fried_egg = '#forge:cooked_eggs';
    i.eggs = '#forge:eggs';
    	i.egg = '#forge:eggs';
    	i.raw_eggs = '#forge:eggs';
    	i.raw_egg = '#forge:eggs';
    i.cooked_cod = '#forge:cooked_fishes/cod';
    i.cooked_salmon = '#forge:cooked_fishes/salmon';
    i.raw_salmon_slice = 'farmersdelight:salmon_slice';
    	i.salmon_slice = 'farmersdelight:salmon_slice';
    i.raw_cod_slice = 'farmersdelight:cod_slice';
    	i.cod_slice = 'farmersdelight:cod_slice';
    i.raw_fish = '#forge:raw_fishes';
    i.cooked_fish = '#forge:cooked_fishes';
    i.crab = 'quark:crab_leg';
    	i.raw_crab = 'quark:crab_leg';
    i.cooked_crab = 'quark:cooked_crab_leg';
    i.scarlet_cheese_wedge = 'brewinandchewin:scarlet_cheese_wedge';
    
    i.shulker_mollusk = 'endersdelight:shulker_mollusk';
    i.shulker_flesh = '#endersdelight:shulker_loot';
    	i.shulker_loot = '#endersdelight:shulker_loot';
    i.ender_sight = '#endersdelight:enderman_sight';
    i.enderman = '#endersdelight:enderman_loot';
    i.chorus_juice = 'endersdelight:chorus_juice';
    
    i.crab_bars = 'quarkdelight:crab_bars';
    i.i_crab_bars = 'kubejs:incomplete_crab_bars';
    i.grilled_salmon = 'farmersdelight:grilled_salmon';
    i.fish_stew = 'farmersdelight:fish_stew';
    i.bacon_and_eggs = 'farmersdelight:bacon_and_eggs';
    i.salmon_roll = 'farmersdelight:salmon_roll';
    i.i_salmon_roll = 'kubejs:incomplete_salmon_roll';
    i.cod_roll = 'farmersdelight:cod_roll';
    i.i_cod_roll = 'kubejs:incomplete_cod_roll';
    i.kelp_roll = 'farmersdelight:kelp_roll';
    i.i_kelp_roll = 'kubejs:incomplete_kelp_roll';
    i.kelp_roll_slice = 'farmersdelight:kelp_roll_slice';
    i.rice_medley = 'farmersdelight:rice_roll_medley_block';
   	i.i_rice_medley = 'kubejs:incomplete_rice_medley';
    i.horror_lasagna = 'brewinandchewin:horror_lasagna';
    i.horror_lasagna_meat = '#brewinandchewin:horror_lasagna_meats';
    i.cooked_horror_lasagna_meat = '#brewinandchewin:cooked_horror_lasagna_meats';
    i.pasta_with_mutton_chop = 'farmersdelight:pasta_with_mutton_chop';
    	i.pasta_with_mutton = 'farmersdelight:pasta_with_mutton_chop';
  	i.barbecue_stick = 'farmersdelight:barbecue_stick';
  		i.bbq_stick = 'farmersdelight:barbecue_stick';
		i.roasted_mutton_chops = 'farmersdelight:roasted_mutton_chops';
			i.roast_mutton_chops = 'farmersdelight:roasted_mutton_chops';
			i.roast_mutton = 'farmersdelight:roasted_mutton_chops';
		i.pancakes = 'supplementaries:pancake';
			i.pancake = 'supplementaries:pancake';
		i.melon_juice = 'farmersdelight:melon_juice';
		i.raw_salmon_tempura = 'pneumaticcraft:raw_salmon_tempura';
		i.salmon_tempura = 'pneumaticcraft:salmon_tempura';
    
    // Items
    item.bowl = 'minecraft:bowl';
    item.stick = 'minecraft:stick';
    item.bottle = 'minecraft:glass_bottle';
    item.shulker_bowl ='endersdelight:shulker_bowl';
    item.bonemeal = 'minecraft:bone_meal';
    	item.bone_meal = 'minecraft:bone_meal'; // Both spellings
    item.nether_wart = 'minecraft:nether_wart';
    item.incomplete = 'kubejs:incomplete_item';
    //Fluids
   	fluid.water = '#forge:fluid/water';
   	fluid.milk = 'minecraft:milk';
   	fluid.tomato_sauce = 'create_central_kitchen:tomato_sauce';
   	fluid.melon_juice = 'create_central_kitchen:melon_juice';
   	fluid.yeast = 'pneumaticcraft:yeast_culture';
   	fluid.plant_oil = 'immersiveengineering:plantoil';
   	
   	//######################### Tools #########################
   	//Remove smoker recipes minus blackstone
		CE.remove({ id: 'minecraft:smoker' }, e)
		CE.remove({ id: 'quark:building/crafting/furnaces/deepslate_smoker' }, e)
		
		//######################### Soups and bases #########################
		//Bone broth
    // Updated bone broth to accept bone meal + shapeless recipe
    CE.remove({ id: 'farmersdelight:cooking/bone_broth' }, e)
    CE.fdCooking([['minecraft:bone', 'minecraft:bone_meal'], ['minecraft:glow_berries', i.mushrooms, 'minecraft:hanging_roots', 'minecraft:glow_lichen']], Item.of(i.bone_broth, 2), e, 1, 200, item.bowl)
    CE.shapeless(Item.of(i.bone_broth, 1), [['minecraft:bone', 'minecraft:bone_meal'], ['minecraft:glow_berries', 'minecraft:hanging_roots', "minecraft:glow_lichen", i.mushrooms], item.bowl], e)
    // Shapeless craft some stews w/ bonebroth 
    CE.shapeless(Item.of('farmersdelight:beef_stew', 1), [i.bone_broth, i.cooked_beef, i.carrot, i.baked_potato], e)
    CE.shapeless(Item.of('farmersdelight:baked_cod_stew', 1), [i.bone_broth, i.cooked_cod, i.eggs, i.potato, i.tomato], e)
    CE.shapeless(Item.of('farmersdelight:noodle_soup', 1), [i.bone_broth, i.cooked_pork, i.cooked_eggs, i.pasta, i.dried_kelp], e)
    CE.shapeless(Item.of('farmersdelight:chicken_soup', 1), [i.bone_broth, i.cooked_chicken, i.vegetables, i.salad_ingredients, i.carrot], e)
    CE.shapeless(Item.of(i.cooked_rice, 1), [i.rice, i.bone_broth], e)
    
    //Cooked rice
    // Shapeless craft rice dishes w/ cooked_rice 
    CE.shapeless(Item.of('farmersdelight:fried_rice', 1), [i.cooked_rice, i.eggs, i.carrot, i.onion], e)
    CE.shapeless(Item.of('farmersdelight:mushroom_rice', 1), [i.cooked_rice, 'minecraft:brown_mushroom', 'minecraft:red_mushroom', [i.carrot, i.potato]], e)
    CE.shapeless(Item.of('delightful:sinigang', 1), [i.cooked_rice, i.cooked_fish, i.cooked_pork, i.tomato_sauce, i.onion, i.fruit], e)
    
    //######################### Fluids #########################
    
    //Tomato sauce
    // Tomato sauce merge
    CE.remove({ id: 'tmted:squeezer/tomato' }, e)
    CE.remove({ id: 'tmted:bottling/tomato_sauce' }, e)
    CE.ieSqueezing(Fluid.of('create_central_kitchen:tomato_sauce', 250), i.tomato, e, undefined, 6400)
    CE.ieBottling(i.tomato_sauce, '#forge:fluid/tomato_sauce', 250, item.bowl, e)
    //Expensive shapeless craft
    CE.shapeless(Item.of(i.tomato_sauce, 1), [item.bowl, Item.of(i.tomato, 4)], e)
    
    //Melon juice
    //melon juice merge
    CE.remove({ id: 'tmted:squeezer/melon_juice_from_slice' }, e)
    CE.remove({ id: 'tmted:squeezer/melon_juice_from_block' }, e)
    CE.remove({ id: 'tmted:bottling/melon_juice' }, e)
    CE.remove({ id: 'create_central_kitchen:mixing/melon_juice' }, e)
    CE.ieSqueezing(Fluid.of(fluid.melon_juice, 125), i.melon, e, undefined, 6400)
		CE.ieSqueezing(Fluid.of(fluid.melon_juice, 1250), i.melon_block, e, undefined, 6400)
		CE.ieBottling(i.melon_juice, '#forge:fluid/melon_juice', 250, item.bottle, e)
		CE.createMixing(Fluid.of(fluid.melon_juice, 250), [i.sugar, Item.of(i.melon, 3)], "n/a", e)
    
    //######################### Sugar #########################
    // Replace sugar recipes w/ sugar forge tag, allows glyceral, change sugar cube recipe to reflect
    CE.replaceInput({ input: 'minecraft:sugar' }, 'minecraft:sugar', i.sugar, e)
    CE.remove({id: 'supplementaries:sugar_cube' }, e)
    CE.shapeless(Item.of('supplementaries:sugar_cube', 1), Item.of(i.sugar, 9), e)
    
    //######################### Flour, dough, bread
    // Combine wheat flours and doughs into create version
    CE.remove({ id: 'create:crushing/misc/scrap_hay_block' }, e)
    CE.remove({ id: 'tmted:wheat_dough_from_waterbucket' }, e)
    CE.remove({ id: 'tmted:slime_ball' }, e)
    CE.remove({ id: 'tmted:crusher/wheat_flour_from_bale' }, e)
    CE.remove({ id: 'farmersdelight:bread_from_smelting' }, e)
    CE.remove({ id: 'farmersdelight:bread_from_smoking' }, e)
    CE.remove({ id: 'create_central_kitchen:crafting/dough_1' }, e)
    CE.remove({ id: 'create_central_kitchen:crafting/dough_2' }, e)
    CE.remove({ id: 'create_central_kitchen:crafting/dough_3' }, e)
    CE.remove({ id: 'create_central_kitchen:crafting/dough_4' }, e)
    CE.remove({ id: 'farmersdelight:wheat_dough_from_water' }, e)
    CE.remove({ id: 'tmted:bottling/wheat_dough' }, e)
    CE.replaceOutput({ out: i.out_flour }, i.out_flour, i.flour_in, e)
    CE.replaceInput({ in: i.out_flour }, i.out_flour, i.flour_in, e)
    CE.shapeless(Item.of(i.dough, 1), [i.flour, 'minecraft:water_bucket'], e)
    CE.ieBottling(i.dough, fluid.water, 250, i.flour, e)
    CE.ieBottling('create_compressed:dough_block', fluid.water, 1000, 'create_compressed:wheat_flour_pile', e)
    CE.ieCrushing(Item.of(i.flour, 2),i.wheat, e)
    CE.ieCrushing(Item.of(i.flour, 18),'minecraft:hay_block', e)
    CE.createCrushing([Item.of(i.flour, 13),Item.of(i.flour, 2).withChance(0.25)], 'minecraft:hay_block', e)
    // PC wheat compat
    CE.remove({ id: 'pneumaticcraft:pressure_chamber/wheat_flour' }, e)
    CE.remove({ id: 'pneumaticcraft:explosion_crafting/wheat_flour' }, e)
    CE.pcPressureChamber(Item.of(i.flour, 3), [i.wheat], 1.5, e)
    CE.pcExplosionCrafting(Item.of(i.flour, 1), i.wheat, 50, e)
    // More efficient craft w/ egg
    CE.remove({ id: 'tmted:wheat_dough_from_egg' }, e)
    CE.remove({ id: 'farmersdelight:wheat_dough_from_eggs' }, e)
    CE.shapeless(Item.of(i.dough, 6), [Item.of(i.flour, 3), i.eggs], e)
    //Add bread block recipes
    CE.remove({id:'architects_palette:bread_block'}, e)
    CE.remove({id:'architects_palette:bread_slab'}, e)
    CE.remove({id:'architects_palette:stonecutting/bread_nub'}, e)
    CE.smoking('architects_palette:bread_block', 'create_compressed:dough_block', e)
    CE.createSaw(Item.of(i.bread, 16), i.bread_block, e)
    CE.fdCutting(i.bread_block, '#forge:tools/knives', Item.of(i.bread, 16), e)
    CE.stonecutting('architects_palette:bread_slab', i.bread_block, e)
    CE.stonecutting('architects_palette:crustless_bread_block', i.bread_block, e)
    CE.stonecutting('architects_palette:bread_nub', 'architects_palette:bread_slab', e)
    // Sourdough
    //Swap flour to dough
    CE.remove({ id: 'pneumaticcraft:sourdough' }, e)
    CE.shapeless(Item.of(i.sourdough, 1), ['pneumaticcraft:yeast_culture_bucket', Item.of(i.dough, 8)], e)
    // Make smoking
    CE.remove({ id: 'pneumaticcraft:sourdough_bread' }, e)
    CE.smoking(i.sourdough_bread, i.sourdough, e)
    // slice to 2 bread 
    CE.fdCutting(i.sourdough_bread, '#forge:tools/knives', Item.of(i.bread, 2), e)
    // Mixing
    CE.createMixing(Item.of(i.sourdough, 8), [Fluid.of(fluid.yeast, 500), Item.of(i.dough, 8)], "heated", e)
    // Bottling
    CE.ieBottling(i.sourdough, '#forge:fluid/yeast_culture', 50, i.dough, e)
    
    //######################### Pasta #########################
    // More efficient press recipe for pasta
    CE.remove({ id: 'tmted:metalpress/raw_pasta' }, e)
    CE.ieMetalPress(Item.of(i.pasta, 2), i.dough, 'immersiveengineering:mold_wire', e)
    
    //######################### Prepared #########################
    //Pancakes
    //Wheat to flour
    CE.remove({ id: 'supplementaries:pancake' }, e)
    CE.shapeless(Item.of(i.pancakes, 3), [i.sugar, i.egg, i.milk, [i.flour, i.rice]], e)
    // Efficient mixing, cooking
    CE.createMixing(Item.of(i.pancakes, 5), [Fluid.of(fluid.milk, 250), i.sugar, i.egg, [i.flour, i.rice]], "heated", e)
  
    //Barbecue stick
    //Remove exising, replace w/ cooked meat tag
    CE.remove({ id: 'farmersdelight:barbecue_stick' }, e)
    CE.shapeless(Item.of(i.bbq_stick, 1), [i.onion, i.tomato, i.cooked_meat, item.stick], e)
    CE.createSequencedAssembly(Item.of(i.bbq_stick, 1),item.stick,
			[e.recipes.create.deploying(item.incomplete, [item.incomplete, i.onion]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.tomato]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cooked_meat])],
    	item.incomplete,1,e,'bbq_stick_sequenced_assembly')
    // Crab Bar
    //	Remove raw shapeless crab bar recipe, replace w/ cooked at lower efficiency
    CE.remove({ id: 'quarkdelight:crab_bars' }, e)
    CE.shapeless(Item.of(i.crab_bars, 2), [i.cooked_fish, i.cooked_crab, item.bonemeal], e, 'creativeengineering/crab_bars_shapeless_manual_only')
    // Add raw ingredient cooking crab bar recipe
    CE.fdCooking([i.raw_fish, i.crab, item.bonemeal], Item.of(i.crab_bars, 4), e, 1, 200)
    	//Unneeded if sliceanddice adds mixing for fd cooking	//CE.createMixing(Item.of(i.crab_bars, 4), [i.raw_fish, i.crab, item.bonemeal], "heated", e)
    // Sequenced assembly 
		CE.createSequencedAssembly(Item.of(i.crab_bars, 10),item.bonemeal,
			[e.recipes.create.deploying(i.i_crab_bars, [i.i_crab_bars, i.cooked_crab]),
			e.recipes.create.deploying(i.i_crab_bars, [i.i_crab_bars, i.cooked_fish]),
			e.recipes.create.pressing(i.i_crab_bars, i.i_crab_bars)],
			i.i_crab_bars,2,e,'crab_bars_sequenced_assembly')
		// Fish stew
		CE.shapeless(Item.of(i.fish_stew, 1), [i.cooked_fish, i.cooked_fish, Item.of(i.tomato_sauce, 2), i.onion, item.bowl], e)
		
		// Cabbage rolls
		CE.shapeless(Item.of(i.cabbage_rolls, 1), [i.cabbage, i.cabbage, i.cabbage_roll_veg, i.cabbage_roll_veg], e)
		
		//Grilled salmon
		// Replace shaped recipe w/ shaped
    CE.remove({ id: 'farmersdelight:grilled_salmon' }, e)
		CE.shapeless(Item.of(i.grilled_salmon, 1), [i.onion, i.cabbage, i.sweet_berries, i.cooked_salmon, item.bowl], e)
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.grilled_salmon, 1),item.bowl,[
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.onion]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cabbage]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.sweet_berries]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cooked_salmon])
			],item.incomplete,1,e,'grilled_salmon_sequenced_assembly')
		
		// Tempura 
		CE.createMixing(Item.of(i.salmon_tempura, 1), [Fluid.of(fluid.plant_oil, 250), i.raw_salmon_tempura], "heated", e)
		CE.replaceInput(
			{ id: 'pneumaticcraft:raw_salmon_tempura' },
			'minecraft:salmon',
			'#forge:raw_fishes/salmon', e
		)
		
		// Bacon and eggs
		// Replace shaped recipe w/ shaped
    CE.remove({ id: 'farmersdelight:bacon_and_eggs' }, e)
		CE.shapeless(Item.of(i.bacon_and_eggs, 1), [i.cooked_bacon, i.cooked_bacon, i.fried_egg, i.fried_egg, item.bowl], e)
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.bacon_and_eggs, 1),item.bowl,
			[e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cooked_bacon]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.fried_eggs])],
			item.incomplete,2,e,'bacon_eggs_sequenced_assembly')
		
		//Salmon roll
		//Make shapeless, already shapeless?
		CE.remove({ id: 'farmersdelight:salmon_roll' }, e)
		CE.shapeless(Item.of(i.salmon_roll, 2), [i.salmon_slice, i.salmon_slice, i.cooked_rice], e)
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.salmon_roll, 3),i.cooked_rice,
			[e.recipes.create.deploying(i.i_salmon_roll, [i.i_salmon_roll, i.salmon_slice])],
			i.i_salmon_roll,3,e,'salmon_roll_sequenced_assembly')
		//Cod roll
		//Make shapeless, already shapeless?
		CE.remove({ id: 'farmersdelight:cod_roll' }, e)
		CE.shapeless(Item.of(i.cod_roll, 2), [i.cod_slice, i.cod_slice, i.cooked_rice], e)
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.cod_roll, 3),i.cooked_rice,
			[e.recipes.create.deploying(i.i_cod_roll, [i.i_cod_roll, i.cod_slice])],
			i.i_cod_roll,3,e,'cod_roll_sequenced_assembly')
		//Kelp roll
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.kelp_roll, 1),i.cooked_rice,
			[e.recipes.create.deploying(i.i_kelp_roll, [i.i_kelp_roll, i.carrot]),
			e.recipes.create.deploying(i.i_kelp_roll, [i.i_kelp_roll, i.dried_kelp]),
			e.recipes.create.deploying(i.i_kelp_roll, [i.i_kelp_roll, i.dried_kelp])],
			i.i_kelp_roll,1,e,'kelp_roll_sequenced_assembly')
		//Rice Medley
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.rice_medley, 1),item.bowl,
			[e.recipes.create.deploying(i.i_rice_medley, [i.i_rice_medley, i.cod_roll]),
			e.recipes.create.deploying(i.i_rice_medley, [i.i_rice_medley, i.salmon_roll]),
			e.recipes.create.deploying(i.i_rice_medley, [i.i_rice_medley, i.kelp_roll_slice])],
			i.i_rice_medley,2,e,'rice_medley_sequenced_assembly')
		
		//Horror Lasagna
		//Make cooked shapeless
		CE.shapeless(Item.of(i.horror_lasagna, 1), [i.scarlet_cheese_wedge, i.scarlet_cheese_wedge, i.beetroot_soup, i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_horror_lasagna_meat, i.cooked_horror_lasagna_meat, item.bowl], e)
		
		//Pasta w/ mutton
		//Make cooked shapeless
		CE.shapeless(Item.of(i.pasta_with_mutton, 1), [i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_mutton, i.cooked_mutton, item.bowl], e)
		//Add bulk mixing
		CE.createMixing(Item.of(i.pasta_with_mutton, 4), [Fluid.of(fluid.tomato_sauce, 1000), i.pasta, i.pasta, i.pasta, i.raw_mutton, i.raw_mutton, Item.of(item.bowl, 4)], "heated", e)
		
		//Roasted mutton chops 
		// Sequenced assembly
		CE.createSequencedAssembly(Item.of(i.roast_mutton, 1),item.bowl,[
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.tomato]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.beetroot]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cooked_rice]),
			e.recipes.create.deploying(item.incomplete, [item.incomplete, i.cooked_mutton_chops])
			],item.incomplete,1,e,'roast_mutton_sequenced_assembly')
			
    //######################### Sweets #########################
    //Pie changes
    //Remove redundant pie
    CE.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice' }, e)
    CE.remove({ id: 'create_central_kitchen:cutting/pumpkin_pie_slice_using_deployer' }, e)
    CE.remove({ id: 'create_central_kitchen:crafting/pumpkin_pie_from_slices' }, e)
    
    //Cake
    // Redundant cake recipe
    CE.remove({ id: 'minecraft:cake' }, e)
    
    //Honey
    //Honey item removals
    CE.remove({ id:'buzzier_bees:honey/honey_apple' }, e)
    
    //######################### Protein #########################
    //Egg
    //Remove redundant fried egg
    CE.remove({ id: 'incubation:fried_egg' }, e)
    CE.remove({ id: 'incubation:fried_egg_from_smoking' }, e)
    CE.remove({ id: 'incubation:fried_egg_from_campfire_cooking' }, e)
    
    //######################### Ender #########################
    //Ender food changes
    //Remove wood bowl items
    CE.remove({ id: 'endersdelight:twisted_cereal_wood' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/chorus_fruit_block_uncompress' }, e)
    // Stuffed shulker shapeless
		//CE.shapeless(Item.of('endersdelight:stuffed_shulker', 1), [i.tomato_sauce, i.tomato_sauce, i.cooked_rice, i.cooked_rice, i.brown_mushroom, i.chorus_fruit, item.shulker_mollusk, 'minecraft:shulker_shell'], e)
		CE.shapeless(Item.of('endersdelight:stuffed_shulker', 1), [i.tomato_sauce, i.tomato_sauce, i.cooked_rice, i.cooked_rice, i.brown_mushroom, i.chorus_fruit, 'endersdelight:shulker_mollusk', 'minecraft:shulker_shell'], e)
		//Ender paella shapeless
		CE.shapeless(Item.of('endersdelight:ender_paella', 1), [i.cooked_rice, i.cooked_rice, i.shulker_flesh, i.chorus_flower, i.chorus_flower, i.ender_sight, item.shulker_bowl], e)
    //Pearl pasta shapeless
    CE.shapeless(Item.of('endersdelight:pearl_pasta', 1), [i.enderman, i.pasta, i.pasta, i.chorus_juice, i.chorus_juice, item.shulker_bowl], e)
    
 		//******************************************************************************************************************************************************************************************************************************  
    
    //######################### Misc #########################
    //Crates
    //Remove redundant crates
    CE.remove({ id: 'quark:building/crafting/compressed/carrot_crate' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/carrot_crate_uncompress' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/beetroot_crate' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/beetroot_crate_uncompress' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/potato_crate' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/potato_crate_uncompress' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/apple_crate' }, e)
    CE.remove({ id: 'quark:building/crafting/compressed/apple_crate_uncompress' }, e)
    
    console.log('The cooking event has finished!')

})
*/