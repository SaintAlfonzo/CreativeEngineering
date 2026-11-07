// requires: creativeengineering
// Creative Engineering; moved from kubejs/server_scripts/engineering/process.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {

console.log('Hello! The create process event has started!')

const R = global.NCH
var i = global.item;

// ############################## Blast Furnace ##############################

//Remove vanilla craft, require deepslate or blackstone recipe
R.remove({id:'minecraft:blast_furnace'}, e)

// ############################## Create Press ##############################

//Mechanical Press
//Replace brass w/ gold, more complex recipe
R.remove({id:'create:crafting/kinetics/mechanical_press'}, e)
R.shaped(Item.of(i.mechanical_press, 1), [' A ','BCB', ' D '], {A: i.shaft, B: i.springs_stiff, C: i.andesite_casing, D: i.block_iron}, e)

// ############################## Create Saw ##############################

//Sawblade
//Change to iron
R.remove({id:'immersiveengineering:crafting/sawblade'}, e)
R.shaped(Item.of(i.sawblade, 1), ['ABA','B B', 'ABA'], {A: i.ingot_iron, B: i.plate_iron}, e)
//Replace saw recipe
R.remove({id:'create:crafting/kinetics/mechanical_saw'}, e)
R.shaped(Item.of(i.mechanical_saw, 1), ['A','B'], {A: i.sawblade, B: i.andesite_casing}, e)
R.createDeploying(Item.of(i.mechanical_saw, 1), [i.andesite_casing, i.sawblade], e)

// ############################## Create Deployer ##############################

//Brass/gold hand
//Change brass hand to gold hand, unlocking deployers earlier
R.replaceInput(
  { id: 'create:crafting/kinetics/brass_hand' },
  i.plate_brass,
  i.any_plate_gold, e
)
//IE Workbench recipe
R.ieBlueprint(Item.of(i.gold_hand, 1), [{ id: i.any_plate_gold, count: 3 }, i.andesite_alloy], 'components', e, '3_plate_gold_to_1_brass_hand_by_components')

// ############################## Vibration Table ##############################

//Vibration Table
//Add spring variety, sheet metal slab, vibration mechanism to craft
R.remove({id:'vintageimprovements:craft/vibrating_table'}, e)
R.shaped(Item.of(i.vibrating_table, 1), ['ABA','ACA', 'DED'], {A: i.springs_stiff, B: i.sheetmetal_slabs, C: i.mechanical_piston, D: i.small_springs_weak, E: i.vibration_mechanism}, e)

// ############################## Mechanical Crafter ##############################

//Mechanical crafter
//Replace brass w/ gold, more complex recipe
R.remove({id:'create:crafting/kinetics/mechanical_crafter'}, e)
R.shaped(Item.of(i.mechanical_crafter, 2), ['GRG','CTC', 'GAG'], {G: i.ingot_gold, R: i.redstone_mechanism, C: i.cog, T: i.crafting_table, A: i.andesite_casing}, e)

// ############################## Alloy Furnace ##############################

// Alloy furnace nether-locked recipes
R.remove({id:'immersiveengineering:crafting/alloybrick'}, e)
R.remove({id:'createaddition:compat/immersiveengineering/item_application/kiln_brick'}, e)
R.remove({id:'createaddition:compat/immersiveengineering/item_application/kiln_brick_using_deployer'}, e)
R.remove({id:'immersiveengineering:crafting/blastbrick'}, e)
R.createItemApplication(i.alloybrick, i.polished_blackstone_bricks, i.nether_bricks, e)
R.shaped(Item.of(i.alloybrick, 2), ['AB','BA'], {A: i.polished_blackstone_bricks, B: i.nether_bricks}, e)

// ############################## Blast Furnace ##############################

//Cast iron alloy smelting
R.ieAlloy(Item.of(i.ingot_cast_iron, 1), i.ingot_iron, i.dust_coke, e)
R.ieAlloy(Item.of(i.ingot_cast_iron, 1), i.ingot_iron, i.dust_petcoke, e, 'cast_iron_petcoke')
// Blast furnace cast iron locked
R.shaped(Item.of(i.blastbrick, 3), ['ABA','BCB', 'ABA'], {A: i.nether_bricks, B: i.ingot_cast_iron, C: i.molten_nether_bricks}, e)

// ############################## Engineers Workbench ##############################

// Engineers workbench steel-locked
R.replaceInput(
  { id: 'immersiveengineering:crafting/workbench' },
  i.ingot_iron,
  i.ingot_steel, e
)

// ############################## Buckets ##############################

//Iron bucket from plates (vanilla's 3-ingot recipe is gone); the wooden one only carries water
R.remove({id:'minecraft:bucket'}, e)
R.shaped(Item.of(i.bucket, 1), ['P P',' P '], {P: i.any_plate_iron}, e, 'bucket_from_plates')
R.shaped(Item.of(i.wooden_bucket, 1), ['P P',' P '], {P: i.planks}, e, 'wooden_bucket')

console.log('The create process event has finished!')
})
