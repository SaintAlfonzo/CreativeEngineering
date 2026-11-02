// requires: brewinandchewin
// Brewin' and Chewin' (backport) recipe changes: feature_writeup/alcohol.md 8.6 and 8.7. Brewin' dishes are Creative
// Delight's (kubejs/data/creativedelight/kubejs/brewinandchewin/recipes.js).
// Moved from the pack's server_scripts/cooking/alcohol.js 2026-11-01 (creative_brewing.md 4.5), rows unchanged; the Vinery
// rows went to ../vinery/alcohol.js. Recipe ids are creativebrewing:<type>/<suffix> (were creativeengineering:).
// The port's data stays 4.5.0, so each change removes the upstream id and adds the pack recipe here.
ServerEvents.recipes(e => {
if (!global.CB || !global.CB.on('brewinandchewin')) return
global.CB.recipes(() => {

console.log('alcohol recipes starting')

var i = global.item
var f = global.fluid
var R = global.NCH

//######################### Pickling moves to the Pickling Jar #########################
// The keg is for beer fluids, the cask for wine, the Vintage Pickling Jar for all pickling (vintage_delight.md S7, 3.2).
// Same inputs as the keg; the 250 mB of honey becomes a honey bottle; the jar's pickle time.
R.remove({ id: 'brewinandchewin:fermenting/kimchi' }, e)
R.vdPickling(Item.of(i.kimchi, 2), [i.cabbage, i.vegetables, i.kelp], e, 'brewin_kimchi')
// Pickled Pickles are merged into Creative Delight's Pickled Sea Pickle (sea pickles + salt, no honey; TODO
// dishes/pickles-and-ferments #1), so the honey recipe is not rebuilt.
R.remove({ id: 'brewinandchewin:fermenting/pickled_pickles_from_honey' }, e)

//######################### Keg fermenting #########################
// Flaxen Cheese: 2.0's shape (brown mushroom x2, sugar)
R.remove({ id: 'brewinandchewin:fermenting/flaxen_cheese_from_milk' }, e)
R.bncFermenting(Fluid.of(f.flaxen_cheese, 1000), [i.brown_mushroom, i.brown_mushroom, i.any_sweetener_brewing], f.milk, e, 'flaxen_cheese_from_milk',
	{ temperature: 4, category: 'meals' })

// Pale Jane: any bark, not just Farmer's Delight's
R.remove({ id: 'brewinandchewin:fermenting/pale_jane_from_rice_wine' }, e)
R.bncFermenting(Fluid.of(f.pale_jane, 1000), [i.honey_bottle, i.any_bark, i.lily_of_the_valley, i.any_sweetener_brewing], f.rice_wine, e, 'pale_jane_from_rice_wine',
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

})
})
