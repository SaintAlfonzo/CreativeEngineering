// requires: creativecompat
// Pack script: moved out of Creative Compat (Compat publishes compatibility fixes only; publish_readiness.md
// D1, PR-CP-01 to -03). Recipe ids keep the creativecompat namespace (forPack) so nothing already saved changes.
ServerEvents.recipes(e => {

console.log('misc mods starting')

var i = global.item
var f = global.fluid
var R = global.NCH.forPack('creativecompat')

// ============================================================
// Supplementaries
// ============================================================

//Remove pulley
R.remove({id: 'supplementaries:pulley'}, e)

// Soap
// Replace pork with animal fat. Creative Delight's Blubber and Whale Oil count too, while its [fats] switches are on
// (TODO dishes/ingredients #10); unloaded mods' fats drop out of the list.
// Delightful's Animal Oil Bottle is merged into Rustic Delight's Cooking Oil when both are loaded (CD swaps
// merges_delightful_oil.json). A merge runs replaceInput over every recipe, and KubeJS swaps the WHOLE ingredient that
// holds the merged item, so naming the bottle here collapsed the soap fats to Cooking Oil alone (r1120nv SQ-FO-08,
// TODO mod-integration #1). Name Cooking Oil in its place while the merge applies.
var oilMerged = Platform.isLoaded('delightful') && Platform.isLoaded('rusticdelight')
var SOAP_FAT = [i.animal_fat, oilMerged ? i.cooking_oil : i.animal_oil_bottle, i.fish_oil, i.tallow] // G5 (filtered by global.has below)
if (global.CD && global.CD.fats) {
	if (global.CD.fats('blubber')) SOAP_FAT.push(i.blubber)
	if (global.CD.fats('whaleOil')) SOAP_FAT.push(i.whale_oil)
}
SOAP_FAT = SOAP_FAT.filter(id => id && global.has(id)).map(id => Item.of(id, 1))
R.replaceInput({ id: 'supplementaries:soap' }, i.porkchop, SOAP_FAT, e)
//Efficient heating
if (global.has(i.soap, i.ash) && SOAP_FAT.length) R.createMixing(Item.of(i.soap, 9), [SOAP_FAT, Item.of(i.ash, 3), Fluid.of(f.water, 500)], e, 'soap_heated_mixing', 'heated')
if (global.has(i.soap, i.ash, f.plantoil)) R.createMixing(Item.of(i.soap, 2), [Item.of(i.ash, 2), Fluid.of(f.water, 250), Fluid.of(f.plantoil, 250)], e, 'soap_vegan_heated_mixing', 'heated')

// Flax is removed (F1, merges/materials #2; item_merges.md 2.22). Flax is off in supplementaries-common.toml, so
// Supplementaries' own flag recipes switch: rope from 3 string, sack from wheat and string, no flax to string, no
// flax doormat. These recipes have no flax flag and still name flax:
R.remove({id: 'supplementaries:flax_block'}, e)
R.remove({id: 'supplementaries:flax_block_uncrafting'}, e)
R.remove({id: 'supplementaries:botany_flax'}, e)
R.remove({id: 'botanypots:supplementaries/crop/flax'}, e)
R.remove({id: 'create:milling/compat/supplementaries/flax'}, e)
// Doormat (owner ruling, plan_farming_cooking_batch.md): upstream's flax shape, two Farmer's Delight straw in a row
R.shaped(i.doormat, ['SS'], { S: i.straw }, e, 'doormat_from_straw')

// ============================================================
// Immersive Weathering
// ============================================================

// Sand and red sand layers: 1 block <-> 9 layers, lossless with the 9th layer that turns a full pile of 8 into the
// block (surface/SurfaceLayers; upstream 3 sand -> 6 layers, 4 layers -> 1 sand)
if (Platform.isLoaded('immersive_weathering')) {
	R.remove({id: 'immersive_weathering:sand_layer_block'}, e)
	R.remove({id: 'immersive_weathering:sand_from_sand_layer'}, e)
	R.remove({id: 'immersive_weathering:red_sand_layer_block'}, e)
	R.remove({id: 'immersive_weathering:red_sand_from_red_sand_layer'}, e)
	R.shapeless(Item.of(i.sand_layer_block, 9), [i.sand], e, 'sand_layers_from_sand')
	R.shaped(i.sand, ['LLL', 'LLL', 'LLL'], { L: i.sand_layer_block }, e, 'sand_from_sand_layers')
	R.shapeless(Item.of(i.red_sand_layer_block, 9), [i.red_sand], e, 'red_sand_layers_from_red_sand')
	R.shaped(i.red_sand, ['LLL', 'LLL', 'LLL'], { L: i.red_sand_layer_block }, e, 'red_sand_from_red_sand_layers')
}

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
// Upgrade Aquatic
// ============================================================

// Bedrolls: removed items (gameplay/misc #10), craft and every dye recipe
R.bulk('remove', [i.bedroll, i.white_bedroll, i.orange_bedroll, i.magenta_bedroll, i.light_blue_bedroll, i.yellow_bedroll, i.lime_bedroll, i.pink_bedroll, i.gray_bedroll, i.light_gray_bedroll, i.cyan_bedroll, i.purple_bedroll, i.blue_bedroll, i.brown_bedroll, i.green_bedroll, i.red_bedroll, i.black_bedroll].map(id => [{ output: id }, e]))

// ============================================================
// Design Decor
// ============================================================

// Enchanted golden apple mechanical craft (blocks/framed-and-decor #9)
R.remove({ id: 'design_decor:mechanical_crafting/enchanted_golden_apple' }, e)

// Cardboard merge: Old and Weathered Cardboard Box are only made by washing, Dustrial box -> Old -> Weathered
R.remove({ output: i.cardboard_box }, e)
R.remove({ output: i.weathered_cardboard_box }, e)
R.createSplashing(i.cardboard_box, i.dustrial_decor_cardboard_box, e)
R.createSplashing(i.weathered_cardboard_box, i.cardboard_box, e)

// ============================================================
// Incubation
// ============================================================

// Nests, same U shape as Incubation's own hay nest (5 wheat).
// Twig nest: stock Incubation only has it from worldgen
R.shaped(i.twig_nest, ['T T', 'TTT'], { T: [i.twig, i.any_stick] }, e, 'twig_nest')
// Emu, duck and goose nests and the duck/goose egg crates: recipes ship in Incubation Additions
// (development/incubationAdditions, packs/<mod id>/data), loaded only with their bird's mod

console.log('misc mods finished')
})
