ServerEvents.recipes(e => {
console.log('Hello! The create utilities event has started!')
const R = global.NCH
var i = global.item;

// ############################## Bearings ##############################

//Replace slab w/ turntable
R.replaceInput(
  { id: 'create:crafting/kinetics/windmill_bearing' },
  i.wood_slab,
  i.turntable, e
)
R.replaceInput(
  { id: 'create:crafting/kinetics/mechanical_bearing' },
  i.wood_slab,
  i.turntable, e
)
R.replaceInput(
  { id: 'create:crafting/kinetics/clockwork_bearing' },
  i.wood_slab,
  i.turntable, e
)

// ############################## Chain Drive ##############################
 
//Chain drive
//Replace nuggets w/ chain, increased yield
R.remove({id:'create:crafting/kinetics/encased_chain_drive'}, e)
R.shapeless(Item.of(i.encased_chain_drive, 3), [Item.of(i.chain, 3), i.andesite_casing], e)
 
// ############################## Display Link ############################## 
 
//Display link
//Remove current, replace with more complex recipe and higher yield
R.remove({id:'create:crafting/logistics/display_link'}, e)
R.shaped(Item.of(i.display_link, 1), ['T','B', 'C'], {T: i.transmitter, B: i.brass_casing, C: i.coil_block_copper}, e)

// ############################## Drill ############################## 

//Add drillhead to drill recipe 
R.remove({id:'create:crafting/kinetics/mechanical_drill'}, e)
R.shaped(Item.of(i.mechanical_drill, 1), ['D','C'], {D: i.drillhead_iron, C: i.andesite_casing}, e)
R.createDeploying(Item.of(i.mechanical_drill, 1), [i.andesite_casing, i.drillhead_iron], e)

// ############################## Pipes ##############################

//Fluid valve
//Swap to any plate
R.remove({id:'create:crafting/kinetics/fluid_valve'}, e)
R.shapeless(Item.of(i.cr_fluid_valve, 1), [Item.of(i.cr_pipe, 1), i.any_plate], e)

// ############################## Pulleys ##############################

//Replace wool w/ rope
R.replaceInput(
  { id: 'create:crafting/kinetics/rope_pulley' },
  i.any_wool,
  i.any_rope, e
)

console.log('The create utilites event has finished!')
})
