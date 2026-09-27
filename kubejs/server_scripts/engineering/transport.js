ServerEvents.recipes(e => {

console.log('Hello! The transport event has started!')

const R = global.NCH
var i = global.item;

// ############################## Resource Swap ##############################  
 
// Replace iron in little logistics w/ iron plate
R.replaceInput(
  { mod: 'littlelogistics' },
  i.ingot_iron,
  i.any_plate_iron, e)

// ############################## Rail ##############################

//Replace rail recipe w/ iron rod
R.remove({id:'minecraft:rail'}, e)
R.shaped(Item.of(i.rail, 12), ['A A','ABA', 'A A'], {A: i.rod_iron, B: i.any_stick}, e)
//Rail sequence assembly, workbench
const incRail = R.incomplete('rail')
R.createSequencedAssembly(Item.of(i.rail, 8), i.any_stick, [
	e.recipes.create.deploying(incRail, [incRail, i.rod_iron])
	], ['rail'], e, 'rails_sequence_assembly', 3)
R.ieBlueprint(Item.of(i.rail, 12), [Item.of(i.rod_iron, 4), i.any_stick], 'railtransport', e)
 
// ############################## Detector Rail ##############################
 
//Replace detector w/ rail craft, IE workbench recipe
R.remove({id:'minecraft:detector_rail'}, e)
R.shaped(Item.of(i.detector_rail, 6), ['A A','ABA', 'ACA'], {A: i.rail, B: i.iron_pressure_plate, C: i.redstone}, e)
R.ieBlueprint(Item.of(i.detector_rail, 8), [i.iron_pressure_plate, Item.of(i.rail, 8), i.redstone], 'railtransport', e)
 
// ############################## Activator Rail ############################## 
 
//Replace activator w/ rail craft, IE workbench recipe
R.remove({id:'minecraft:activator_rail'}, e)
R.shaped(Item.of(i.activator_rail, 6), ['ACA','ABA', 'ACA'], {A: i.rail, B: i.redstone_torch, C: i.redstone}, e)
R.ieBlueprint(Item.of(i.activator_rail, 8), [i.redstone_torch, Item.of(i.rail, 8), Item.of(i.redstone, 2)], 'railtransport', e)
 
// ############################## Powered Rail ############################## 
 
//Replace powered w/ rail craft, IE workbench recipe
R.remove({id:'minecraft:powered_rail'}, e)
R.shaped(Item.of(i.powered_rail, 6), ['ACA','ABA', 'ACA'], {A: i.rail, B: i.redstone, C: i.any_rod_gold}, e)
R.ieBlueprint(Item.of(i.powered_rail, 8), [i.redstone, Item.of(i.rail, 8), Item.of(i.any_rod_gold, 2)], 'railtransport', e)
 
// ############################## Controller Rail ############################## 
 
//Replace controller w/ rail craft, IE workbench recipe
R.remove({id:'create:crafting/kinetics/controller_rail'}, e)
R.shaped(Item.of(i.controller_rail, 6), ['ACA','ABA', 'ACA'], {A: i.powered_rail, B: i.electron_tube, C: i.redstone}, e)
R.ieBlueprint(Item.of(i.controller_rail, 8), [i.electron_tube, Item.of(i.powered_rail, 8), Item.of(i.redstone, 2)], 'railtransport', e)
 
// ############################## Vehicle Link ############################## 
  
// Put iron wire in vehicle link item
R.replaceInput(
  { id: 'littlelogistics:spring' },
  i.string,
  i.any_wire_iron, e)
// Chain to vehicle link
R.shapeless(Item.of(i.ll_spring, 2), [i.chain], e)
//vehicle link sequence assembly
const incLlSpring = R.incomplete('ll_spring')
R.createSequencedAssembly(Item.of(i.ll_spring, 6), i.nugget_iron, [
	e.recipes.create.deploying(incLlSpring, [incLlSpring, i.any_wire_iron])
		.id(`createdeploying/vehicle_link_step1_nugget`),
	e.recipes.create.deploying(incLlSpring, [incLlSpring, i.nugget_iron])
		.id(`createdeploying/vehicle_link_step2_ingot`),
	e.recipes.create.deploying(incLlSpring, [incLlSpring, i.any_wire_iron])
		.id(`createdeploying/vehicle_link_step3_nugget`)
], ['ll_spring'], e, 'vehicle_link_sequenced_assembly', 1)
//workbench recipe for vehicle links
R.ieBlueprint(Item.of(i.ll_spring, 6), [Item.of(i.any_wire_iron, 2), Item.of(i.nugget_iron, 2)], 'railtransport', e)
 
// ############################## Train Cars and Barges ############################## 
 
// replace seater car recipe
R.remove({id:'littlelogistics:seater_car'}, e)
R.shaped(Item.of(i.ll_seater_car, 4), ['PPP', 'I I'], {P: i.planks, I: i.block_iron}, e)
 
// Fluid train car w/ create tank
R.remove({id:'littlelogistics:fluid_car'}, e)
R.shaped(Item.of(i.ll_fluid_car, 1), ['T', 'C'], {T: i.cr_tank, C: i.ll_seater_car}, e)
 
// Fluid barge w/ create tank
R.remove({id:'littlelogistics:fluid_barge'}, e)
R.shaped(Item.of(i.ll_fluid_barge, 1), [' T ', 'III'], {T: i.cr_tank, I: i.any_plate_iron}, e)
 
//replace furnace w/ blast furnace in locomotive and tug
R.replaceInput(
  { id: 'littlelogistics:steam_locomotive' },
  i.furnace,
  i.blast_furnace, e)
R.replaceInput(
  { id: 'littlelogistics:tug' },
  i.furnace,
  i.blast_furnace, e)
 
// Replace stairs in seater barge for CR seat
R.replaceInput(
  { id: 'littlelogistics:seater_barge' },
  i.any_wooden_stairs,
  i.any_seat, e)
 
// ############################## Wireless ##############################
 
//Remove wireless transmitter and receiver
R.remove({id:'littlelogistics:receiver_component'}, e)
R.remove({id:'littlelogistics:transmitter_component'}, e)
//replace w/ redstone link
R.replaceInput(
  { input: 'littlelogistics:receiver_component' },
  i.ll_receiver_component,
  i.redstone_link, e
)
R.replaceInput(
  { input: 'littlelogistics:transmitter_component' },
  i.ll_transmitter_component,
  i.redstone_link, e
)
 
// ############################## Hoppers ############################## 

//Brass in rapid hopper
R.remove({id:'littlelogistics:rapid_hopper'}, e)
R.shaped(Item.of(i.ll_rapid_hopper, 1), ['BHB', ' B '], {B: i.plate_brass, H: i.hopper}, e)
R.ieBlueprint(Item.of(i.ll_rapid_hopper, 1), [Item.of(i.plate_brass, 2), Item.of(i.hopper, 1)], 'redstonecomponents', e)
 
//Fluid hopper rebuild
R.remove({id:'littlelogistics:fluid_hopper'}, e)
R.shaped(Item.of(i.ll_fluid_hopper, 1), ['GPG','GTG', 'PH '], {G: i.glass, P: i.cr_pipe, T: i.cr_tank, H: i.hopper}, e)
 
// ############################## Charger ##############################  
 
//Replace vehicle charger recipe
R.remove({id:'littlelogistics:vessel_charger'}, e)
R.shaped(Item.of(i.ll_vessel_charger, 1), [' E ','VIV', 'RIR'], {E: i.mv_coil, V: i.vacuum_tube, I: i.any_rod_iron, R: i.redstone_block}, e)
 
console.log('The transport event has finished!')
})
