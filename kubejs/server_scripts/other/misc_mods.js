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
// Emu nest (patcherMod nest/EmuNests.java): only emus lay in it
R.shaped(Item.of(i.emu_nest, 1), ['B B', 'BBB'], { B: i.any_bark }, e, 'emu_nest')
// Duck and goose nests (patcherMod nest/WaterfowlNests.java): grass + tall grass, with the two
// kinds swapped between the pair so the two U shapes never match each other
R.shaped(Item.of(i.duck_nest, 1), ['T T', 'GGG'], { G: i.grass, T: i.tall_grass }, e, 'duck_nest')
R.shaped(Item.of(i.goose_nest, 1), ['G G', 'TTT'], { G: i.grass, T: i.tall_grass }, e, 'goose_nest')

console.log('misc mods finished')
})


/*ServerEvents.recipes(e => {

console.log('misc starting')

CE.remove({id: 'create:crafting/tree_fertilizer'}, e)
CE.remove({id: 'explorerscompass:explorers_compass'}, e)
CE.replaceInput(
  { id: 'naturescompass:natures_compass' },
  '#minecraft:logs',
  'botania:terrasteel_ingot', e)
  
//Remove pulley
CE.remove({id: 'supplementaries:pulley'}, e)  
  
//Quiver recipe
CE.shaped(Item.of('supplementaries:quiver',1), ['TCT', 'TCT', ' T '], {T: ['minecraft:leather', 'immersiveengineering:hemp_fabric', 'farmersdelight:canvas'], C: 'minecraft:copper_ingot'}, e)
//Swap rabbit hide in slingshot
CE.replaceInput({ id: 'supplementaries:slingshot' },
  'minecraft:rabbit_hide',['minecraft:leather', 'immersiveengineering:hemp_fabric', 'farmersdelight:canvas'], e)
  
//Swap redstone in wind_vane for iron rod
CE.replaceInput({ id: 'supplementaries:wind_vane' },
  'minecraft:redstone','immersiveengineering:stick_iron', e)
  
// Rods in iron frame
CE.replaceInput({ id: 'suppsquared:metal_frame' },
  'minecraft:iron_nugget','immersiveengineering:stick_iron', e)
  
// Soap
// Replace pork with animal fat
CE.replaceInput({ id: 'supplementaries:soap' },
  'minecraft:porkchop',[Item.of('delightful:animal_fat', 1), Item.of('delightful:animal_oil_bottle', 1), Item.of('alexsmobs:fish_oil', 1), Item.of('immersive_weathering:tallow', 1)], e)
//Efficient heating
CE.createMixing(Item.of('supplementaries:soap', 9), [[Item.of('delightful:animal_fat', 1), Item.of('delightful:animal_oil_bottle', 1), Item.of('alexsmobs:fish_oil', 1), Item.of('immersive_weathering:tallow', 1)], Item.of('supplementaries:ash', 3), Fluid.of('minecraft:water', 500)], 'heated', e, 'soap_heated_mixing')
CE.createMixing(Item.of('supplementaries:soap', 2), [Item.of('supplementaries:ash', 2), Fluid.of('minecraft:water', 250), Fluid.of('immersiveengineering:plantoil', 250)], 'heated', e, 'soap_vegan_heated_mixing')

// Fodder
//Replace flax bale w/ rice and straw bale 
CE.replaceInput({ id: 'supplementaries:fodder' },
  'supplementaries:flax_block',[Item.of('farmersdelight:rice_bale', 1), Item.of('farmersdelight:straw_bale', 1)], e)
  
//Remove supplementaries crank
CE.remove({id: 'supplementaries:crank'}, e)  
  
//Bamboo
//Remove quark bamboo mats
CE.remove({id: 'quark:building/crafting/bamboo_mat'}, e)
CE.remove({id: 'quark:building/crafting/bamboo_mat_carpet'}, e)
CE.remove({id: 'quarkdelight:cutting/knife/bamboo_mat'}, e)
CE.remove({id: 'quarkdelight:cutting/knife/bamboo_mat_carpet'}, e)

// IE tough fabric to wool partial ingredient replacement
CE.replaceInput({ id: 'architects_palette:spool' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'another_furniture:white_curtain' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'handcrafted:white_cushion' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'handcrafted:white_sheet' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'handcrafted:white_curtain' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'create:crafting/kinetics/attribute_filter' },
  '#minecraft:wool',['#minecraft:wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'create:crafting/kinetics/filter' },
  '#minecraft:wool',['#minecraft:wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'create:crafting/kinetics/nozzle' },
  '#minecraft:wool',['#minecraft:wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'compressedcreativity:crafting/mesh_woven' },
  '#minecraft:wool',['#minecraft:wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'projectred_transmission:white_insulated_wire' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'beautify:oak_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:spruce_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:birch_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:dark_oak_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:jungle_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:acacia_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:mangrove_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:crimson_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:warped_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'beautify:quartz_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/quark/azalea_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/quark/ancient_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/quark/blossom_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/quark/bamboo_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/architects_palette/twisted_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/botania/livingwood_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/botania/dreamwood_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'everycomp:bd/vinery/cherry_picture_frame' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.remove({id: 'farmersdelight:painting_from_canvas'}, e)
CE.replaceInput({ id: 'minecraft:painting' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric', 'minecraft:paper', 'farmersdelight:canvas'], e)
CE.replaceInput({ id: 'supplementaries:flags/flag_white' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'minecraft:white_banner' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'comforts:sleeping_bag_white' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'comforts:hammock_white' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
  
CE.replaceInput({ id: 'botania:holy_cloak' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'botania:invisibility_cloak' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'immersive_armors:robe_helmet' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
CE.replaceInput({ id: 'immersive_armors:robe_chestplate' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'immersive_armors:robe_leggings' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'immersive_armors:robe_boots' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'nethersdelight:hoglin_mount' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'vinery:vinemaker_leggings' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'railways:sequenced_assembly/white_conductor_cap' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
  
CE.replaceInput({ id: 'botania:flower_bag' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'itemfilters:always_true' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e) 
CE.replaceInput({ id: 'pneumaticcraft:thermal_lagging' },
  '#minecraft:wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)   
CE.replaceInput({ id: 'another_furniture:white_lamp' },
  'minecraft:white_wool',['minecraft:white_wool', 'immersiveengineering:hemp_fabric'], e)
  
  // all fucked up, fix later
// Replace bandage recipe, efficient alt bottling recipe
CE.remove({ id: 'pneumaticcraft:bandage' }, e)
CE.shapeless(Item.of('pneumaticcraft:bandage',1),[['farmersdelight:canvas', 'immersiveengineering:hemp_fabric'],Item.of('minecraft:honey_bottle',4)], e, 'bandage_honey_bottle_shapeless')
CE.shapeless(Item.of('pneumaticcraft:bandage',1),[['farmersdelight:canvas', 'immersiveengineering:hemp_fabric'],Item.of('pneumaticcraft:glycerol',4)], e, 'bandage_glycerol_shapeless')
CE.ieBottling('pneumaticcraft:bandage', '#forge:fluid/honey', 1000, 'farmersdelight:canvas', e, 'bandage_honey_bottling')
CE.ieBottling('pneumaticcraft:bandage', '#forge:fluid/honey', 1000, 'immersiveengineering:hemp_fabric', e, 'bandage_honey_bottling_2')
CE.createFilling('pneumaticcraft:bandage',[Fluid.of('create:honey', 1000),['farmersdelight:canvas', 'immersiveengineering:hemp_fabric']], e, 'bandage_honey_filling')

CE.ieBottling(Item.of('pneumaticcraft:bandage', 2), '#forge:phenolic_resin', 250, 'immersiveengineering:hemp_fabric', e, 'bandage_phenolic_bottling')
CE.ieBottling(Item.of('pneumaticcraft:bandage', 2), '#forge:phenolic_resin', 250, 'farmersdelight:canvas', e, 'bandage_phenolic_bottling_2')
CE.createFilling('pneumaticcraft:bandage',[Fluid.of('immersiveengineering:phenolic_resin', 250),['farmersdelight:canvas', 'immersiveengineering:hemp_fabric']], e, 'bandage_phenolic_filling')


CE.replaceInput({ id: 'pneumaticcraft:bandage' },
  '#minecraft:wool_carpets',, e) 
  
// IE bottling rebuilds, update when fluids decided
//CE.ieBottling(eth_bucket, '#forge:fluid/ethanol', 1000, 'minecraft:bucket', e)
//CE.ieBottling(biofuel_bucket, '#forge:fluid/biofuel', 1000, 'minecraft:bucket', e)
//CE.ieBottling(biofuel_bucket, '#forge:fluid/yeast_culture', 1000, 'minecraft:bucket', e)

//Remove lightmans chocolate currency
e.remove({output: 'lightmanscurrency:coinpile_chocolate_copper'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_copper'})
e.remove({output: 'lightmanscurrency:coinpile_chocolate_iron'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_iron'})
e.remove({output: 'lightmanscurrency:coinpile_chocolate_gold'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_gold'})
e.remove({output: 'lightmanscurrency:coinpile_chocolate_emerald'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_emerald'})
e.remove({output: 'lightmanscurrency:coinpile_chocolate_diamond'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_diamond'})
e.remove({output: 'lightmanscurrency:coinpile_chocolate_netherite'})
e.remove({input: 'lightmanscurrency:coinpile_chocolate_netherite'})
  
//Rebuilt enderstorage crafts for variable input
e.remove({ id: 'enderstorage:ender_pouch' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: 'enderstorage:ender_pouch' },
  pattern: ["BLB", "LPL", "BWB"],
  key: {
    P: { tag: 'forge:ender_pearls' },
    L: { tag: 'forge:leather' },
    B: { item: 'minecraft:blaze_powder' },
    W: [{ item: 'minecraft:white_wool' }, { item: 'immersiveengineering:hemp_fabric' }]}})
e.remove({ id: 'enderstorage:ender_chest' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: 'enderstorage:ender_chest' },
  pattern: ["BWB", "OCO", "BPB"],
  key: {
    P: { tag: 'forge:ender_pearls' },
    O: { tag: 'forge:obsidian' },
    C: { tag: 'forge:chests/wooden' },
    B: { item: 'minecraft:blaze_rod' },
    W: [{ item: 'minecraft:white_wool' }, { item: 'immersiveengineering:hemp_fabric' }]}})
e.remove({ id: 'enderstorage:ender_tank' })
e.custom({
  type: 'enderstorage:create_recipe',
  result: { item: 'enderstorage:ender_tank' },
  pattern: ["BWB", "OCO", "BPB"],
  key: {
    P: { tag: 'forge:ender_pearls' },
    O: { tag: 'forge:obsidian' },
    C: { item: 'minecraft:cauldron' },
    B: { item: 'minecraft:blaze_rod' },
    W: [{ item: 'minecraft:white_wool' }, { item: 'immersiveengineering:hemp_fabric' }]}})

console.log('misc finished')

/*console.log('=== Registry enumeration test ===');

    // Method 1: vanilla registry
    try {
        var MCRegistryClass = Java.loadClass('net.minecraft.core.Registry');
        var keys1 = MCRegistryClass.ITEM.keySet();
        console.log('[Method 1] size: ' + keys1.size());
        var it1 = keys1.iterator();
        var sample1 = [];
        while (it1.hasNext() && sample1.length < 5) {
            sample1.push(it1.next().toString());
        }
        console.log('[Method 1] sample: ' + sample1.join(', '));
    } catch (err) {
        console.log('[Method 1] threw: ' + err);
    }

    // Method 2: Forge registry
    try {
        var FeRegClass = Java.loadClass('net.minecraftforge.registries.ForgeRegistries');
        var values2 = FeRegClass.ITEMS.getValues();
        console.log('[Method 2] size: ' + values2.size());
    } catch (err) {
        console.log('[Method 2] threw: ' + err);
    }
})

console.log('Hello! The test scripts event has started!')
ServerEvents.loaded(event => {
    
});*/
