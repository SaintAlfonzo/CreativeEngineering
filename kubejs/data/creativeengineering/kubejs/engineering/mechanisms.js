// Creative Engineering; moved from kubejs/server_scripts/engineering/mechanisms.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {
console.log('Hello! The mechanisms event has started!')
const R = global.NCH
var i = global.item;

// ############################## Precision ##############################

//Replace gold sheet w/ brass
R.replaceInput(
  { id: 'create:sequenced_assembly/precision_mechanism' },
  i.any_plate_gold,
  i.plate_brass, e
)
//Blueprint recipe
R.ieBlueprint(Item.of(i.precision_mechanism, 1), [Item.of(i.cog, 5), Item.of(i.large_cog, 5), Item.of(i.nugget_iron, 5), i.plate_brass], 'components', e)
 
// ############################## Vibration ##############################

//Replace w/ Echo shard recipe
R.remove({id:'create_things_and_misc:vibration_mecanism_craft'}, e)
// Sequenced assembly
R.createSequencedAssembly(Item.of(i.vibration_mechanism, 1), i.echo_shard, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.rose_quartz_plate]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.amethyst_shard]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.cog]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone])
	], i.i_vibration_mechanism, e, 'vibration_mechanism_sequenced_assembly', 3)
//Blueprint recipe
R.ieBlueprint(Item.of(i.vibration_mechanism, 1), [Item.of(i.rose_quartz_plate, 2), Item.of(i.amethyst_shard, 2), Item.of(i.cog, 2), Item.of(i.redstone, 2), i.echo_shard], 'redstonecomponents', e)
 
// ############################## Redstone ##############################

//Replace w/ component recipe
R.remove({id:'vintageimprovements:sequenced_assembly/redstone_module'}, e)
// Sequenced assembly
R.createSequencedAssembly(Item.of(i.redstone_mechanism, 1), i.any_plate_gold, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.rod_copper]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.electron_tube]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone]),
	e.custom({type: 'vintageimprovements:vibrating', ingredients: [{ item: i.incomplete }], results: [{ item: i.incomplete }]}),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_randomizer]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.rose_quartz_plate])
	], i.i_redstone_mechanism, e, 'redstone_mechanism_sequenced_assembly', 2)
//Blueprint recipe
R.ieBlueprint(Item.of(i.redstone_mechanism, 1), [Item.of(i.rod_copper, 2), Item.of(i.electron_tube, 1), Item.of(i.redstone, 2), Item.of(i.redstone_randomizer, 1), Item.of(i.rose_quartz_plate, 2), i.any_plate_gold], 'redstonecomponents', e)

console.log('The mechanisms event has finished!')
})
