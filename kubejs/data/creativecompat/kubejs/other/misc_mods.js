// requires: creativecompat
// Creative Compat; moved from kubejs/server_scripts/other/misc_mods.js on 2026-11-01.
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

// Flax is removed (F1, merges/materials #2; item_merges.md 2.22). Flax is off in supplementaries-common.toml, so
// Supplementaries' own flag recipes switch: rope from 3 string, sack from wheat and string, no flax to string, no
// flax doormat. These recipes have no flax flag and still name flax:
R.remove({id: 'supplementaries:flax_block'}, e)
R.remove({id: 'supplementaries:flax_block_uncrafting'}, e)
R.remove({id: 'supplementaries:botany_flax'}, e)
R.remove({id: 'botanypots:supplementaries/crop/flax'}, e)
R.remove({id: 'create:milling/compat/supplementaries/flax'}, e)

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
