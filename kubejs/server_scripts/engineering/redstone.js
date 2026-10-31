ServerEvents.recipes(e => {

console.log('Hello! The redstone event has started!')

var i = global.item
var f = global.fluid
var R = global.NCH

// ############################## Vanilla ##############################

//redstone torch
//seq assemble
const incRedstoneTorch = R.incomplete('redstone_torch')
R.createSequencedAssembly(Item.of(i.redstone_torch, 1), i.stick, [
	e.recipes.create.deploying(incRedstoneTorch, [incRedstoneTorch, i.redstone])
], ['redstone_torch'], e, 'redstone_torch_sequenced_assembly', 1)
//Blueprint recipe
R.ieBlueprint(Item.of(i.redstone_torch, 4), [Item.of(i.stick, 3), Item.of(i.redstone, 3)], 'redstonecomponents', e)

//Redstone lamp
//Add alt rose quartz recipe
R.shaped(Item.of(i.redstone_lamp, 1), [' A ','ABA', ' A '], {A: i.redstone, B: i.rose_quartz}, e)
//Blueprint recipe
R.ieBlueprint(Item.of(i.redstone_lamp, 1), [Item.of(i.redstone, 3), Item.of(i.rose_quartz, 1)], 'redstonecomponents', e)
R.ieBlueprint(Item.of(i.redstone_lamp, 1), [Item.of(i.redstone, 3), Item.of(i.glowstone_block, 1)], 'redstonecomponents', e, 'redstone_to_redstone_lamp_by_rose_quartz')

//Copper bulb
//Replace w/ electron tube recipe
R.remove({id: 'everythingcopper:copper_lamp'}, e)
R.shaped(Item.of(i.copper_bulb, 2), [' C ','CEC', ' R '], {C: i.block_copper, E: i.electron_tube, R: i.redstone}, e)
//Blueprint recipe
R.ieBlueprint(Item.of(i.copper_bulb, 3), [Item.of(i.block_copper, 2), Item.of(i.electron_tube, 1), Item.of(i.redstone, 1)], 'redstonecomponents', e)

//Daylight sensor
//Quartz, Rose Quartz plate sequenced assembly
const incDaylightSensor = R.incomplete('daylight_sensor')
R.createSequencedAssembly(Item.of(i.daylight_sensor, 1), i.wood_slab, [
	e.recipes.create.deploying(incDaylightSensor, [incDaylightSensor, i.quartz]),
	e.recipes.create.deploying(incDaylightSensor, [incDaylightSensor, i.glass])
], ['daylight_sensor'], e, 'daylight_sensor_sequenced_assembly', 2)
R.createSequencedAssembly(Item.of(i.daylight_sensor, 1), i.wood_slab, [
	e.recipes.create.deploying(incDaylightSensor, [incDaylightSensor, i.rose_quartz_plate]),
	e.recipes.create.deploying(incDaylightSensor, [incDaylightSensor, i.glass])
], ['daylight_sensor'], e, 'daylight_sensor_sequenced_assembly_rose_quartz', 2)
//blueprint recipes
R.ieBlueprint(Item.of(i.daylight_sensor, 1), [i.wood_slab, Item.of(i.quartz, 2), Item.of(i.glass, 2)], 'redstonecomponents', e)
R.ieBlueprint(Item.of(i.daylight_sensor, 1), [i.wood_slab, Item.of(i.rose_quartz_plate, 2), Item.of(i.glass, 2)], 'redstonecomponents', e, 'rose_quartz_to_daylight_sensor_by_redstonecomponents')

//Dropper
//Replace cobble w/ stone, add wooden chute, workbench recipe
R.remove({id: i.dropper}, e)
R.shaped(Item.of(i.dropper, 1), ['SSS','SHS', 'SRS'], {S: i.anystone, H: i.q_chute, R: i.redstone}, e)
R.ieBlueprint(Item.of(i.dropper, 1), [i.q_chute, i.redstone, i.anystone, i.anystone, i.anystone, i.anystone], 'redstonecomponents', e, 'dropper_by_redstonecomponents')

//Dispenser: vanilla craft kept

//Observer
//Replace cobble w/ stone, workbench recipe
R.replaceInput({id: i.observer}, i.cobblestone, i.anystone, e)
R.ieBlueprint(Item.of(i.observer, 1), [i.quartz, i.redstone, i.anystone, i.anystone, i.anystone, i.anystone], 'redstonecomponents', e, 'observer_by_redstonecomponents')

//Piston
//Replace cobble w/ stone, workbench recipe
R.replaceInput({id: i.piston}, i.cobblestone, i.anystone, e)
R.ieBlueprint(Item.of(i.piston, 1), [i.ingot_iron, i.redstone, i.planks, i.planks, i.anystone, i.anystone], 'redstonecomponents', e, 'piston_by_redstonecomponents')

//Sticky piston
//Slime filling, more efficient
R.createFilling(i.sticky_piston, [Fluid.of(f.slime, 50), i.piston], e, 'piston_slime_filling')

//Hopper
//Workbench recipe
R.ieBlueprint(Item.of(i.hopper, 1), [Item.of(i.ingot_iron, 3), i.any_chest], 'redstonecomponents', e, 'hopper_by_redstonecomponents')

// ############################## Create ##############################

//Rose quartz
//Heated mixing
R.createMixing(Item.of(i.rose_quartz, 1), [Item.of(i.redstone, 6), i.quartz], e, 'rose_quartz_heated_mixing', 'heated')
//Arc furnace
R.ieArcFurnace(i.rose_quartz, i.quartz, e, 'rose_quartz_arc_furnace', [], [i.redstone, i.redstone, i.redstone, i.redstone])
//Pressure Chamber
R.pcPressureChamber(Item.of(i.rose_quartz, 3), [Item.of(i.quartz, 3), Item.of(i.redstone, 9)], 3, e)

//Polished rose quartz
//Charging
R.createCharging({item: i.polished_rose_quartz, count: 1}, {item: i.rose_quartz}, e, 'polished_rose_quartz_charging', 6000, 300)
R.createCharging({item: i.polished_rose_quartz_block, count: 1}, {item: i.rose_quartz_crystal_block}, e, 'polished_rose_quartz_block_charging', 50000, 1000)
//Sand blasting: one item out per item in, 80 % stays rough, 20 % polished; loop the belt for the rest
R.remove({id: 'create_dd:sanding/rose_quartz'}, e)
R.ddSanding([Item.of(i.rose_quartz).withChance(0.8), Item.of(i.polished_rose_quartz).withChance(0.2)],
	i.rose_quartz, e, 'rose_quartz', { exclusive: true })

//Rose quartz plate
//Replace sheet recipe w/ cutting
R.remove({id: 'create_things_and_misc:rose_quartz_plate_craft'}, e)
R.createSaw(Item.of(i.rose_quartz_plate, 2), i.polished_rose_quartz, e)
//Sawmill recipe w/ redstone return -> datapack json (secondaries can't go through KubeJS,
//kjs-ie SawmillRecipeSchema mangles them): kubejs/data/creativeengineering/recipes/sawmill/rose_quartz_sawing.json

//Electron tube
//Electron tube deploying, arc furnace
R.createDeploying(i.electron_tube, [i.polished_rose_quartz, i.any_plate_iron], e)
R.ieArcFurnace(i.electron_tube, i.polished_rose_quartz, e, 'electron_tube_arc_furnace', [], [i.nugget_iron, i.nugget_iron, i.nugget_iron, i.nugget_iron])
//Blueprint recipe
R.ieBlueprint(Item.of(i.electron_tube, 2), [Item.of(i.polished_rose_quartz, 2), i.any_plate_iron], 'redstonecomponents', e)

// Rose Quartz Lamp
//Replace shapeless w/ more expensive recipe
R.remove({id: 'create:crafting/kinetics/rose_quartz_lamp'}, e)
R.shaped(Item.of(i.rose_quartz_lamp), ['ABA','BCB','ABA'], {A: i.nugget_zinc, B: i.polished_rose_quartz, C: i.redstone}, e)
// Sequenced assembly
const incRoseQuartzLamp = R.incomplete('rose_quartz_lamp')
R.createSequencedAssembly(Item.of(i.rose_quartz_lamp, 1), i.redstone, [
	e.recipes.create.deploying(incRoseQuartzLamp, [incRoseQuartzLamp, i.polished_rose_quartz]),
	e.recipes.create.deploying(incRoseQuartzLamp, [incRoseQuartzLamp, i.nugget_zinc])
], ['rose_quartz_lamp'], e, 'rose_quartz_lamp_sequenced_assembly', 3)
//Blueprint recipe
R.ieBlueprint(Item.of(i.rose_quartz_lamp, 1), [Item.of(i.polished_rose_quartz, 2), Item.of(i.nugget_zinc, 2), i.redstone], 'redstonecomponents', e)

//Transmitter craft, seq assemble, workbench recipe
R.shaped(Item.of(i.transmitter, 1), [' L ','PPP', 'RAR'], {A: i.amethyst_shard, R: i.redstone, P: i.any_plate_copper, L: i.lightning_rod}, e)
const incTransmitter = R.incomplete('transmitter')
R.createSequencedAssembly(Item.of(i.transmitter, 1), i.any_plate_copper, [
	e.recipes.create.deploying(incTransmitter, [incTransmitter, i.redstone]),
	e.recipes.create.deploying(incTransmitter, [incTransmitter, i.amethyst_shard]),
	e.recipes.create.deploying(incTransmitter, [incTransmitter, i.lightning_rod])
], ['transmitter'], e, 'transmitter_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.transmitter, 2), [{ id: i.any_plate_copper, count: 2 }, Item.of(i.redstone, 2), Item.of(i.lightning_rod, 1), Item.of(i.amethyst_shard, 1)], 'redstonecomponents', e, '2_plate_copper_to_2_transmitter_by_redstonecomponents')

// Redstone link andesite tier, add transmitter
R.remove({id: 'create:crafting/logistics/redstone_link'}, e)
R.shaped(Item.of(i.redstone_link, 2), ['T','C'], {T: i.transmitter, C: i.andesite_casing}, e)
//Seq assemble, workbench recipe
const incRedstoneLink = R.incomplete('redstone_link')
R.createSequencedAssembly(Item.of(i.redstone_link, 4), i.andesite_casing, [
	e.recipes.create.cutting(incRedstoneLink, incRedstoneLink),
	e.recipes.create.deploying(incRedstoneLink, [incRedstoneLink, i.transmitter]),
	e.recipes.create.cutting(incRedstoneLink, incRedstoneLink)
], ['redstone_link'], e, 'redstone_link_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.redstone_link, 4), [Item.of(i.andesite_casing, 1), Item.of(i.transmitter, 1)], 'redstonecomponents', e)

// ##############################   Gates  #############################

//Removals
R.remove({id: 'gates:nand_gate'}, e)
R.remove({id: 'gates:nor_gate'}, e)
R.remove({id: 'gates:xor_gate'}, e)
R.remove({id: 'gates:xnor_gate'}, e)

//Repeater
// seq assemble, workbench recipes
const incRepeater = R.incomplete('repeater')
R.createSequencedAssembly(Item.of(i.repeater, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(incRepeater, [incRepeater, i.redstone_torch]),
	e.recipes.create.deploying(incRepeater, [incRepeater, i.redstone]),
	e.recipes.create.deploying(incRepeater, [incRepeater, i.redstone_torch])
], ['repeater'], e, 'repeater_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.repeater, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 2), Item.of(i.stick, 1)], 'redstonecomponents', e)

//Fast Repeater
// seq assemble, workbench recipes
const incFastRepeater = R.incomplete('fast_repeater')
R.createSequencedAssembly(Item.of(i.fast_repeater, 1), i.repeater, [
	e.recipes.create.deploying(incFastRepeater, [incFastRepeater, i.nugget_gold]),
	e.recipes.create.pressing(incFastRepeater, incFastRepeater)
], ['fast_repeater'], e, 'fast_repeater_sequenced_assembly', 5)
R.ieBlueprint(Item.of(i.fast_repeater, 1), [Item.of(i.repeater, 1), Item.of(i.nugget_gold, 3)], 'redstonecomponents', e)

//Slow Repeater
//Change to cast iron, seq assemble, workbench recipes
R.remove({id: 'gates:slow_repeater'}, e)
R.shapeless(Item.of(i.slow_repeater, 1), [i.ingot_cast_iron, i.repeater], e)
const incSlowRepeater = R.incomplete('slow_repeater')
R.createSequencedAssembly(Item.of(i.slow_repeater, 1), i.repeater, [
	e.recipes.create.deploying(incSlowRepeater, [incSlowRepeater, i.nugget_cast_iron]),
	e.recipes.create.pressing(incSlowRepeater, incSlowRepeater)
], ['slow_repeater'], e, 'slow_repeater_sequenced_assembly', 5)
R.ieBlueprint(Item.of(i.slow_repeater, 1), [Item.of(i.repeater, 1), Item.of(i.nugget_cast_iron, 3)], 'redstonecomponents', e)

//Comparator
// seq assemble, workbench recipes
const incComparator = R.incomplete('comparator')
R.createSequencedAssembly(Item.of(i.comparator, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(incComparator, [incComparator, i.redstone_torch]),
	e.recipes.create.deploying(incComparator, [incComparator, i.quartz]),
	e.recipes.create.deploying(incComparator, [incComparator, i.redstone_torch])
], ['comparator'], e, 'comparator_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.comparator, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 1), Item.of(i.stick, 1), Item.of(i.quartz, 1)], 'redstonecomponents', e)

//Randomizer
// seq assemble, workbench recipes
const incRedstoneRandomizer = R.incomplete('redstone_randomizer')
R.createSequencedAssembly(Item.of(i.redstone_randomizer, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(incRedstoneRandomizer, [incRedstoneRandomizer, i.redstone_torch]),
	e.recipes.create.deploying(incRedstoneRandomizer, [incRedstoneRandomizer, i.prismarine_crystals]),
	e.recipes.create.deploying(incRedstoneRandomizer, [incRedstoneRandomizer, i.redstone_torch])
], ['redstone_randomizer'], e, 'randomizer_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.redstone_randomizer, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 1), Item.of(i.stick, 1), Item.of(i.prismarine_crystals, 1)], 'redstonecomponents', e)

//Rain detector
//Replace slabs w/ daylight detector, seq assemble and workbench recipe
R.remove({id: 'gates:rain_detector'}, e)
R.shaped(Item.of(i.rain_detector), ['ggg', 'lll', ' d '], {
	d: i.daylight_sensor,
	g: i.glass,
	l: i.lapis
}, e)
const incRainDetector = R.incomplete('rain_detector')
R.createSequencedAssembly(Item.of(i.rain_detector, 1), i.daylight_sensor, [
	e.recipes.create.deploying(incRainDetector, [incRainDetector, i.lapis]),
	e.recipes.create.deploying(incRainDetector, [incRainDetector, i.glass])
], ['rain_detector'], e, 'rain_detector_sequenced_assembly', 2)
R.ieBlueprint(Item.of(i.rain_detector, 1), [i.daylight_sensor, Item.of(i.lapis, 2), Item.of(i.glass, 2)], 'redstonecomponents', e)

//Thunder detector
//Replace slabs w/ rain detector, seq assemble and workbench recipe
R.remove({id: 'gates:thunder_detector'}, e)
R.shaped(Item.of(i.thunder_detector), ['ggg', 'ppp', ' r '], {
	r: i.rain_detector,
	g: i.glass,
	p: i.mundane_potion
}, e, 'thunder_detector')
const incThunderDetector = R.incomplete('thunder_detector')
R.createSequencedAssembly(Item.of(i.thunder_detector, 1), i.rain_detector, [
	e.recipes.create.filling(incThunderDetector, [incThunderDetector, Fluid.of(f.create_potion, 250, '{Potion:"minecraft:mundane"}')]),
	e.recipes.create.deploying(incThunderDetector, [incThunderDetector, i.glass])
], ['thunder_detector'], e, 'thunder_detector_sequenced_assembly', 2)
R.ieBlueprint(Item.of(i.thunder_detector, 1), [i.rain_detector, i.mundane_potion, i.mundane_potion, Item.of(i.glass, 2)], 'redstonecomponents', e, 'thunder_detector_ie_workbench')

//Remove wireless components, create redstone links fit solution better
R.remove({id: 'gates:receiver'}, e)
R.remove({id: 'gates:transmitter'}, e)
R.remove({id: 'gates:redstone_torch_pearl'}, e)
R.remove({id: 'gates:wireless_redstone_lamp'}, e)
R.remove({id: 'gates:portable_redstone_transmitter'}, e)
R.remove({id: 'gates:frequency_changer'}, e)
R.remove({id: 'gates:waxed_redstone_wire'}, e)

// ############################## Project Red ##############################

//Remove misc crafting ingredients
R.remove({id:'projectred_core:draw_plate'}, e)
R.remove({id:'projectred_core:copper_coil'}, e)
R.remove({id:'projectred_core:iron_coil'}, e)
R.remove({id:'projectred_core:gold_coil'}, e)
R.remove({id:'projectred_core:motor'}, e)
R.remove({id:'projectred_core:woven_cloth'}, e)
R.remove({id:'projectred_core:sail'}, e)
R.remove({id:'cb_microblock:stone_rod'}, e)
R.remove({id:'cb_microblock:stone_saw'}, e)
R.remove({id:'cb_microblock:iron_saw'}, e)
R.remove({id:'cb_microblock:diamond_saw'}, e)

console.log('The redstone event has finished!')
})
