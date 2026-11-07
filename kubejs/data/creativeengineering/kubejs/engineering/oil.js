// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/oil.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The oil event has started!')

var i = global.item
var f = global.fluid
var R = global.NCH

// Unify bio oils to plant oil
// Add seeds to squeezing
R.bulk('ieSqueezing', [
	[Fluid.of(f.plantoil, 60), i.flax_seeds, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_red, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_red_savanna, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_red_taiga, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_red_jungle, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_white, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_white_savanna, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_white_taiga, e, undefined, undefined, 6400],
	[Fluid.of(f.plantoil, 50), i.grape_seeds_white_jungle, e, undefined, undefined, 6400],
])

//Remove pneumaticcraft crop to bio oil
R.remove({id: 'pneumaticcraft:thermo_plant/vegetable_oil_from_crops'}, e)
//R.remove({id:'pneumaticcraft:thermo_plant/vegetable_oil_from_seeds'}, e)
//R.pcPressureChamber(Fluid.of(f.plantoil), ['#forge:seeds'], 2, e)

console.log('The oil event has finished!')
})
