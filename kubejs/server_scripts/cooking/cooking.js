ServerEvents.recipes(e => {

console.log('Hello! The cooking event has started!')

// Every other food and cooking row moved to Creative Delight 2026-10-25, sorted by the mod of the food it makes
// (kubejs/data/creative_delight/kubejs/<mod>/recipes.js; the heatless meal rule is in farmersdelight/recipes.js "Meals",
// the egg solution fluid in create/recipes.js). What is left is Brewin' and Chewin's food, held back for the planned
// Creative Brewing mod with the rest of Brewin' and Chewin'.

var i = global.item
var f = global.fluid
var R = global.NCH

//######################### Egg solution #########################
// The pack's egg solution fluid (create/recipes.js in Creative Delight): 250 mB stands in for one egg and its milk.
R.createMixing(i.vegetable_omelet, [i.t_cheese_wedge, i.onion, i.carrot, i.bowl, Fluid.of(f.egg_solution, 500)], e,
	'vegetable_omelet_egg_solution', 'heated')
// Quiche: one 500 mB spout in place of the milk and the two eggs of the assembly below
var incQuicheEgg = R.incomplete('quiche')
R.createSequencedAssembly(i.quiche, i.pie_crust, [
	e.recipes.create.deploying(incQuicheEgg, [incQuicheEgg, Ingredient.of([i.cooked_bacon, i.brown_mushroom])]),
	e.recipes.create.filling(incQuicheEgg, [incQuicheEgg, Fluid.of(f.egg_solution, 500)]),
	e.recipes.create.deploying(incQuicheEgg, [incQuicheEgg, i.t_cheese_wedge]),
	e.recipes.create.deploying(incQuicheEgg, [incQuicheEgg, Ingredient.of([i.cabbage, i.cabbage_leaf])])
], ['quiche'], e, 'quiche_egg_solution_sequenced_assembly', 1)

//######################### Prepared #########################
// Scarlet Pierogi: beetroot in place of nether wart
R.replaceInput({ id: 'brewinandchewin:cooking/scarlet_pierogi' }, i.nether_wart, i.beetroot, e)

// Supreme Pizza (owner 2026-10-25, rustic_delight.md 5.5): bell pepper, cheese, onion, mushroom, tomato sauce and cooked
// meat on one dough (was cheese, 2 toppings, tomato sauce, dough). Vintage Delight's cheese and meat pizzas stay the
// simple ones.
R.remove({ id: 'brewinandchewin:pizza' }, e)
R.shaped(i.pizza, ['bfo', 'mtc', ' d '], { b: i.any_bell_pepper, f: i.t_cheese_wedge, o: i.onion, m: i.mushrooms, t: i.tomato_sauce,
	c: i.cooked_meat, d: i.dough }, e, 'pizza')

// Ham and Cheese Sandwich: a side is any bread (#forge:bread, baking/baked-goods #28). Smoked ham cuts into two porkchops, so it gives double.
R.remove({ id: 'brewinandchewin:ham_and_cheese_sandwich' }, e)
var sandwichSide = i.t_bread
R.shapeless(Item.of(i.ham_and_cheese_sandwich, 1), [sandwichSide, i.cooked_pork, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich')
R.shapeless(Item.of(i.ham_and_cheese_sandwich, 2), [sandwichSide, i.smoked_ham, i.flaxen_cheese_wedge, sandwichSide], e, 'ham_and_cheese_sandwich_smoked_ham')
var incSandwich = R.incomplete('ham_and_cheese_sandwich')
R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 2), i.t_bread, [
	e.recipes.create.deploying(incSandwich, [incSandwich, i.cooked_pork]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.t_bread])
], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_sequenced_assembly', 1)
R.createSequencedAssembly(Item.of(i.ham_and_cheese_sandwich, 4), i.t_bread, [
	e.recipes.create.deploying(incSandwich, [incSandwich, i.smoked_ham]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.flaxen_cheese_wedge]),
	e.recipes.create.deploying(incSandwich, [incSandwich, i.t_bread])
], ['ham_and_cheese_sandwich'], e, 'ham_and_cheese_sandwich_smoked_ham_sequenced_assembly', 1)

// Quiche: one recipe for bacon and mushroom (JEI cycles them), a cabbage leaf or a whole cabbage. TODO dishes/meals #2
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

//Horror Lasagna
//Make cooked shapeless
R.shapeless(Item.of(i.horror_lasagna, 1), [i.scarlet_cheese_wedge, i.scarlet_cheese_wedge, i.beetroot_soup, i.tomato_sauce, i.tomato_sauce, i.pasta, i.cooked_horror_lasagna_meat, i.cooked_horror_lasagna_meat, i.bowl], e, 'horror_lasagna_cooked_manual_only')

//######################### Meals #########################
// Heatless meal rule: Creative Delight farmersdelight/recipes.js "Meals". Bone broth routes (TODO dishes/ingredients #4).
R.shapeless(i.creamy_onion_soup, [i.bone_broth, i.t_cheese_wedge, i.onion, i.onion, i.t_soup_veggie_cooked, i.t_bread], e, 'creamy_onion_soup_cooked_manual_only')
// Pastas take no broth: the pot's bowl (tomato sauce brings its own)
R.shapeless(i.cheesy_pasta, [i.flaxen_cheese_wedge, i.pasta, i.tomato_sauce, i.cooked_fish, i.cooked_fish], e, 'cheesy_pasta_cooked_manual_only')

// Vegetable omelet: the pot takes fried eggs; or fold two omelettes (each one cooked fried egg) with the veggies
R.replaceInput({ id: 'brewinandchewin:cooking/vegetable_omelet' }, i.eggs, i.cooked_eggs, e)
R.shapeless(i.vegetable_omelet, [i.omelette, i.omelette, i.t_cheese_wedge, i.onion, i.carrot, i.bowl], e, 'vegetable_omelet_from_omelette_manual_only')

// Assembly meals (TODO dishes/meals #3)
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

console.log('The cooking event has finished!')
})
