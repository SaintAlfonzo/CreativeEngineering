// Brewin' and Chewin' (backport) recipe changes: feature_writeup/alcohol.md 8.6 and 8.7.
// The port's data stays 4.5.0, so each change removes the upstream id and adds the pack recipe here.
// Also the JEED providers for the Vinery port's wine effects (alcohol.md B7.5), at the end.
ServerEvents.recipes(e => {

console.log('alcohol recipes starting')

var i = global.item
var f = global.fluid
var R = global.NCH

//######################### Keg fermenting #########################
// Flaxen Cheese: 2.0's shape (brown mushroom x2, sugar)
R.remove({ id: 'brewinandchewin:fermenting/flaxen_cheese_from_milk' }, e)
R.bncFermenting(Fluid.of(f.flaxen_cheese, 1000), [i.brown_mushroom, i.brown_mushroom, i.sugar], f.milk, e, 'flaxen_cheese_from_milk',
	{ temperature: 4, category: 'meals' })

// Pale Jane: any bark, not just Farmer's Delight's
R.remove({ id: 'brewinandchewin:fermenting/pale_jane_from_rice_wine' }, e)
R.bncFermenting(Fluid.of(f.pale_jane, 1000), [i.honey_bottle, i.any_bark, i.lily_of_the_valley, i.sugar], f.rice_wine, e, 'pale_jane_from_rice_wine',
	{ temperature: 4, time: 4800 })

// Beer: dandelion in place of wheat seeds
R.remove({ id: 'brewinandchewin:fermenting/beer_from_water' }, e)
R.bncFermenting(Fluid.of(f.beer, 1000), [i.wheat, i.dandelion, i.brown_mushroom], f.water, e, 'beer_from_water')

// Mead: red mushroom in place of wheat seeds (the only mead now; Vinery's is gone, alcohol.md B7.6)
R.remove({ id: 'brewinandchewin:fermenting/mead_from_honey' }, e)
R.bncFermenting(Fluid.of(f.mead, 1000), [i.wheat, i.red_mushroom, i.sweet_berries], f.honey, e, 'mead_from_honey')

// Bloody Mary: salt in place of sweet berries
R.remove({ id: 'brewinandchewin:fermenting/bloody_mary_from_vodka' }, e)
R.bncFermenting(Fluid.of(f.bloody_mary, 1000), [i.tomato, i.cabbage, i.salt], f.vodka, e, 'bloody_mary_from_vodka',
	{ temperature: 4, time: 4800 })

// Rice Wine: two rice
R.remove({ id: 'brewinandchewin:fermenting/rice_wine_from_water' }, e)
R.bncFermenting(Fluid.of(f.rice_wine, 1000), [i.rice, i.rice, i.brown_mushroom], f.water, e, 'rice_wine_from_water')

// Steel Toe Stout: any metal dust and blaze powder (its Resistance II, 90 s is in the port)
R.remove({ id: 'brewinandchewin:fermenting/steel_toe_stout_from_strongroot_ale' }, e)
R.bncFermenting(Fluid.of(f.steel_toe_stout, 1000), [i.t_metal_dusts, i.crimson_fungus, i.nether_wart, i.blaze_powder], f.strongroot_ale, e,
	'steel_toe_stout_from_strongroot_ale', { temperature: 1, time: 4800 })

//######################### Food #########################
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

// Quiche: the mushroom one takes a cabbage leaf or a whole cabbage
R.remove({ id: 'brewinandchewin:quiche_from_mushroom' }, e)
R.shaped(i.quiche, ['blb', 'mcm', 'eCe'], {
	b: i.brown_mushroom, l: [i.cabbage_leaf, i.cabbage], m: i.milk, c: i.t_cheese_wedge, e: i.minecraft_egg, C: i.pie_crust
}, e, 'quiche_from_mushroom')
// Sequenced assembly. The filling goes on first: with milk first both recipes would start the same, and Create
// only ever picks one of them.
var quicheCabbage = Ingredient.of([i.cabbage, i.cabbage_leaf])
;[
	['quiche_bacon', i.cooked_bacon],
	['quiche_mushroom', i.brown_mushroom],
].forEach(([key, filling]) => {
	var inc = R.incomplete(key)
	R.createSequencedAssembly(i.quiche, i.pie_crust, [
		e.recipes.create.deploying(inc, [inc, filling]),
		e.recipes.create.filling(inc, [inc, Fluid.of(f.milk, 250)]),
		e.recipes.create.deploying(inc, [inc, i.eggs]),
		e.recipes.create.deploying(inc, [inc, i.eggs]),
		e.recipes.create.deploying(inc, [inc, i.t_cheese_wedge]),
		e.recipes.create.deploying(inc, [inc, quicheCabbage])
	], [key], e, key + '_sequenced_assembly', 1)
})

//######################### Spout and drain #########################
// A tankard holds 250 mB, as the keg pours. The cheese fluids stay keg-only (they can't be poured back in 4.5.0 either).
;[
	[i.beer, f.beer], [i.vodka, f.vodka], [i.brewinandchewin_mead, f.mead], [i.egg_grog, f.egg_grog],
	[i.strongroot_ale, f.strongroot_ale], [i.rice_wine, f.rice_wine], [i.glittering_grenadine, f.glittering_grenadine],
	[i.steel_toe_stout, f.steel_toe_stout], [i.pale_jane, f.pale_jane], [i.saccharine_rum, f.saccharine_rum],
	[i.salty_folly, f.salty_folly], [i.bloody_mary, f.bloody_mary], [i.red_rum, f.red_rum],
	[i.withering_dross, f.withering_dross], [i.kombucha, f.kombucha], [i.dread_nog, f.dread_nog],
].forEach(([drink, fluid]) => {
	var name = drink.split(':')[1]
	R.createFilling(drink, [i.tankard, Fluid.of(fluid, 250)], e, name)
	R.createEmptying([i.tankard, Fluid.of(fluid, 250)], drink, e, name)
})

//######################### Vinery banner #########################
// Advancements are gone (alcohol.md 10.2), so wine_somelier no longer hands it out. Any banner over a nether star,
// ringed by the 7 most complex wines (alcohol.md 10.5: scored by recipe depth, juice region and ingredients).
// Wines ignore their age NBT here, so any vintage works. Upstream's smithing recipe (banner + netherite ingot -> 2)
// still copies it.
R.shaped(i.vinery_standard, ['JMA', 'PBC', 'LNH'], {
	J: i.jellie_wine, M: i.bottle_mojang_noir, A: i.aegis_wine, P: i.apple_wine,
	C: i.cristel_wine, L: i.lilitu_wine, H: i.chenet_wine, B: i.any_banner, N: i.nether_star
}, e, 'vinery_standard')

//######################### JEED: wine effects (Vinery port) #########################
// The wine -> effect map is the port's [wine] effects config, so editing it and running /reload moves the providers.
if (Platform.isLoaded('vinery')) {
	try {
		var winesByEffect = Java.loadClass('net.satisfy.vinery.core.wine.WineEffects').winesByEffect()
		var effectIds = winesByEffect.keySet().iterator()
		while (effectIds.hasNext()) {
			var effectId = String(effectIds.next())
			var wineList = winesByEffect.get(effectId)
			var wines = []
			for (var n = 0; n < wineList.size(); n++) wines.push(String(wineList.get(n)))
			R.jeedProvider(effectId, wines, e, 'vinery_' + effectId.replace(':', '_'))
		}
	} catch (err) {
		console.warn('alcohol.js: no JEED wine providers (' + err + ')')
	}
}

})
