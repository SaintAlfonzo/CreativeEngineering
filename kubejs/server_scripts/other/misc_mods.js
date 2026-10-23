ServerEvents.recipes(e => {

console.log('misc mods starting')

var i = global.item
var f = global.fluid
var R = global.NCH

// ============================================================
// Supplementaries
// ============================================================

//Remove pulley
R.remove({id: 'supplementaries:pulley'}, e)

// Soap
// Replace pork with animal fat
R.replaceInput({ id: 'supplementaries:soap' },
  i.porkchop, [Item.of(i.animal_fat, 1), Item.of(i.animal_oil_bottle, 1), Item.of(i.fish_oil, 1), Item.of(i.tallow, 1)], e)
//Efficient heating
R.createMixing(Item.of(i.soap, 9), [[Item.of(i.animal_fat, 1), Item.of(i.animal_oil_bottle, 1), Item.of(i.fish_oil, 1), Item.of(i.tallow, 1)], Item.of(i.ash, 3), Fluid.of(f.water, 500)], e, 'soap_heated_mixing', 'heated')
R.createMixing(Item.of(i.soap, 2), [Item.of(i.ash, 2), Fluid.of(f.water, 250), Fluid.of(f.plantoil, 250)], e, 'soap_vegan_heated_mixing', 'heated')

// Fodder
//Replace flax bale w/ rice and straw bale
R.replaceInput({ id: 'supplementaries:fodder' },
  i.flax_block, [Item.of(i.rice_bale, 1), Item.of(i.straw_bale, 1)], e)

//Remove supplementaries crank
R.remove({id: 'supplementaries:crank'}, e)

// ============================================================
// Quark
// ============================================================

//Bamboo
//Remove quark bamboo mats
R.remove({id: 'quark:building/crafting/bamboo_mat'}, e)
R.remove({id: 'quark:building/crafting/bamboo_mat_carpet'}, e)
R.remove({id: 'quarkdelight:cutting/knife/bamboo_mat'}, e)
R.remove({id: 'quarkdelight:cutting/knife/bamboo_mat_carpet'}, e)

//Create tree fertilizer dead recipe, missing tag input but doesnt want to remove
R.remove({id: 'create:crafting/tree_fertilizer'}, e)

// ============================================================
// Incubation
// ============================================================

// Nests, same U shape as Incubation's own hay nest (5 wheat).
// Twig nest: stock Incubation only has it from worldgen
R.shaped(Item.of(i.twig_nest, 1), ['T T', 'TTT'], { T: [i.twig, i.any_stick] }, e, 'twig_nest')
// Emu, duck and goose nests and the duck/goose egg crates: recipes ship in Incubation Additions
// (development/incubationAdditions, packs/<mod id>/data), loaded only with their bird's mod

console.log('misc mods finished')
})
