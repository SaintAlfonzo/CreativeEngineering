// Creative Engineering; moved from kubejs/server_scripts/engineering/fabric.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e=>{
console.log('fabric starting')

var i = global.item
var f = global.fluid
var R = global.NCH

// IE tough fabric to wool partial ingredient replacement
R.replaceInput({ id: 'architects_palette:spool' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'another_furniture:white_curtain' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'handcrafted:white_cushion' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'handcrafted:white_sheet' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'handcrafted:white_curtain' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'create:crafting/kinetics/attribute_filter' },
  i.any_wool, [i.any_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'create:crafting/kinetics/filter' },
  i.any_wool, [i.any_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'create:crafting/kinetics/nozzle' },
  i.any_wool, [i.any_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'compressedcreativity:crafting/mesh_woven' },
  i.any_wool, [i.any_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'projectred_transmission:white_insulated_wire' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'beautify:oak_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:spruce_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:birch_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:dark_oak_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:jungle_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:acacia_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:mangrove_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:crimson_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:warped_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'beautify:quartz_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/quark/ancient_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/quark/blossom_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/quark/bamboo_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/architects_palette/twisted_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/botania/livingwood_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/botania/dreamwood_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'everycomp:bd/vinery/cherry_picture_frame' },
  i.white_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.remove({id: 'farmersdelight:painting_from_canvas'}, e)
R.replaceInput({ id: 'minecraft:painting' },
  i.any_wool, [i.white_wool, i.hemp_fabric, i.paper, i.canvas], e)
R.replaceInput({ id: 'supplementaries:flags/flag_white' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'minecraft:white_banner' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'comforts:sleeping_bag_white' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'comforts:hammock_white' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)

R.replaceInput({ id: 'botania:holy_cloak' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'botania:invisibility_cloak' },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: i.ia_robe_helmet },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: i.ia_robe_chestplate },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: i.ia_robe_leggings },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: i.ia_robe_boots },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: i.hoglin_mount },  // Hoglin Trophy (display name; id kept, nethers_delight.md 4.5)
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'vinery:winemaker_leggings' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'railways:sequenced_assembly/white_conductor_cap' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)

R.replaceInput({ id: 'botania:flower_bag' },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'itemfilters:always_true' },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'pneumaticcraft:thermal_lagging' },
  i.any_wool, [i.white_wool, i.hemp_fabric], e)
R.replaceInput({ id: 'another_furniture:white_lamp' },
  i.white_wool, [i.white_wool, i.hemp_fabric], e)

console.log('fabric done')
})
