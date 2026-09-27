ServerEvents.recipes(e => {

console.log('Hello! The engineering event has started!')

var i = global.item
var f = global.fluid
var R = global.NCH

i.copper_bulb = 'everythingcopper:copper_lamp';

// ############################## Crystals / Shards ###########################

//Amethyst
//Budding amethyst sequenced assembly
R.createSequencedAssembly(Item.of(i.budding_amethyst, 1), i.amethyst_block, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.budding_amethyst]),
	e.recipes.create.filling(i.incomplete, [i.incomplete, Fluid.of(f.water, 250)])
], i.incomplete, e, 'budding_amethyst_sequenced_assembly', 4)

//Echo shard
//shaped craft
R.shaped(Item.of(i.echo_shard, 1), [' A ','ABA', ' A '], {A: i.sculk, B: i.amethyst_shard}, e)
// Sequenced assembly
R.createSequencedAssembly(Item.of(i.echo_shard, 1), i.amethyst_shard, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.sculk]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.sculk]),
	e.recipes.create.pressing(i.incomplete, i.incomplete)
], i.incomplete, e, 'echo_shard_sequenced_assembly', 1)

// ############################## Redstone / Rose Quartz ###########################

//redstone torch
//seq assemble
R.createSequencedAssembly(Item.of(i.redstone_torch, 1), i.stick, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone])
], i.incomplete, e, 'redstone_torch_sequenced_assembly', 1)
//Blueprint recipe
R.ieBlueprint(Item.of(i.redstone_torch, 4), [Item.of(i.stick, 3), Item.of(i.redstone, 3)], 'redstonecomponents', e)

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

//Rose quartz plate
//Replace sheet recipe w/ cutting
R.remove({id: 'create_things_and_misc:rose_quartz_plate_craft'}, e)
R.createSaw(Item.of(i.rose_quartz_plate, 2), i.polished_rose_quartz, e)

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
R.createSequencedAssembly(Item.of(i.rose_quartz_lamp, 1), i.redstone, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.polished_rose_quartz]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.nugget_zinc])
], i.incomplete, e, 'rose_quartz_lamp_sequenced_assembly', 3)
//Blueprint recipe
R.ieBlueprint(Item.of(i.rose_quartz_lamp, 1), [Item.of(i.polished_rose_quartz, 2), Item.of(i.nugget_zinc, 2), i.redstone], 'redstonecomponents', e)

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
R.createSequencedAssembly(Item.of(i.daylight_sensor, 1), i.wood_slab, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.quartz]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.glass])
], i.incomplete, e, 'daylight_sensor_sequenced_assembly', 2)
R.createSequencedAssembly(Item.of(i.daylight_sensor, 1), i.wood_slab, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.rose_quartz_plate]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.glass])
], i.incomplete, e, 'daylight_sensor_sequenced_assembly_rose_quartz', 2)
//blueprint recipes
R.ieBlueprint(Item.of(i.daylight_sensor, 1), [i.wood_slab, Item.of(i.quartz, 2), Item.of(i.glass, 2)], 'redstonecomponents', e)
R.ieBlueprint(Item.of(i.daylight_sensor, 1), [i.wood_slab, Item.of(i.rose_quartz_plate, 2), Item.of(i.glass, 2)], 'redstonecomponents', e, 'rose_quartz_to_daylight_sensor_by_redstonecomponents')

//Dropper
//Replace cobble w/ stone, add wooden chute, workbench recipe
R.remove({id: 'minecraft:dropper'}, e)
R.shaped(Item.of('minecraft:dropper', 1), ['SSS','SHS', 'SRS'], {S: i.anystone, H: 'quark:chute', R: i.redstone}, e)
R.ieBlueprint(Item.of('minecraft:dropper', 1), ['quark:chute', i.redstone, i.anystone, i.anystone, i.anystone, i.anystone], 'redstonecomponents', e, 'dropper_by_redstonecomponents')

//Dispenser
//Remove vanilla craft, must be crafted from dropper
R.remove({id: 'minecraft:dispenser'}, e)

//Observer
//Replace cobble w/ stone, workbench recipe
R.replaceInput({id: 'minecraft:observer'}, i.cobblestone, i.anystone, e)
R.ieBlueprint(Item.of('minecraft:observer', 1), [i.quartz, i.redstone, i.anystone, i.anystone, i.anystone, i.anystone], 'redstonecomponents', e, 'observer_by_redstonecomponents')

//Piston
//Replace cobble w/ stone, workbench recipe
R.replaceInput({id: 'minecraft:piston'}, i.cobblestone, i.anystone, e)
R.ieBlueprint(Item.of('minecraft:piston', 1), [i.ingot_iron, i.redstone, i.planks, i.planks, i.anystone, i.anystone], 'redstonecomponents', e, 'piston_by_redstonecomponents')

//Sticky piston
//Slime filling, more efficient
R.createFilling('minecraft:sticky_piston', [Fluid.of(f.slime, 50), 'minecraft:piston'], e, 'piston_slime_filling')

//Hopper
//Workbench recipe
R.ieBlueprint(Item.of('minecraft:hopper', 1), [Item.of(i.ingot_iron, 3), '#forge:chests'], 'redstonecomponents', e, 'hopper_by_redstonecomponents')

//Transmitter craft, seq assemble, workbench recipe
R.shaped(Item.of(i.transmitter, 1), [' L ','PPP', 'RAR'], {A: i.amethyst_shard, R: i.redstone, P: i.any_plate_copper, L: 'minecraft:lightning_rod'}, e)
R.createSequencedAssembly(Item.of(i.transmitter, 1), i.any_plate_copper, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.amethyst_shard]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, 'minecraft:lightning_rod'])
], i.incomplete, e, 'transmitter_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.transmitter, 2), [Item.of(i.any_plate_copper, 2), Item.of(i.redstone, 2), Item.of('minecraft:lightning_rod', 1), Item.of(i.amethyst_shard, 1)], 'redstonecomponents', e)

// Redstone link andesite tier, add transmitter
R.remove({id: 'create:crafting/logistics/redstone_link'}, e)
R.shaped(Item.of(i.redstone_link, 2), ['T','C'], {T: i.transmitter, C: i.andesite_casing}, e)
//Seq assemble, workbench recipe
R.createSequencedAssembly(Item.of(i.redstone_link, 4), i.andesite_casing, [
	e.recipes.create.cutting(i.incomplete, i.incomplete),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.transmitter]),
	e.recipes.create.cutting(i.incomplete, i.incomplete)
], i.incomplete, e, 'redstone_link_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.redstone_link, 4), [Item.of(i.andesite_casing, 1), Item.of(i.transmitter, 1)], 'redstonecomponents', e)

// ##############################   Gates  #############################

//Repeater
// seq assemble, workbench recipes
R.createSequencedAssembly(Item.of(i.repeater, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch])
], i.incomplete, e, 'repeater_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.repeater, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 2), Item.of(i.stick, 1)], 'redstonecomponents', e)

//Fast Repeater
// seq assemble, workbench recipes
R.createSequencedAssembly(Item.of(i.fast_repeater, 1), i.repeater, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.nugget_gold]),
	e.recipes.create.pressing(i.incomplete, i.incomplete)
], i.incomplete, e, 'fast_repeater_sequenced_assembly', 5)
R.ieBlueprint(Item.of(i.fast_repeater, 1), [Item.of(i.repeater, 1), Item.of(i.nugget_gold, 3)], 'redstonecomponents', e)

//Slow Repeater
//Change to cast iron, seq assemble, workbench recipes
R.remove({id: 'gates:slow_repeater'}, e)
R.shapeless(Item.of(i.slow_repeater, 1), [i.ingot_cast_iron, i.repeater], e)
R.createSequencedAssembly(Item.of(i.slow_repeater, 1), i.repeater, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.nugget_cast_iron]),
	e.recipes.create.pressing(i.incomplete, i.incomplete)
], i.incomplete, e, 'slow_repeater_sequenced_assembly', 5)
R.ieBlueprint(Item.of(i.slow_repeater, 1), [Item.of(i.repeater, 1), Item.of(i.nugget_cast_iron, 3)], 'redstonecomponents', e)

//Comparator
// seq assemble, workbench recipes
R.createSequencedAssembly(Item.of(i.comparator, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.quartz]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch])
], i.incomplete, e, 'comparator_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.comparator, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 1), Item.of(i.stick, 1), Item.of(i.quartz, 1)], 'redstonecomponents', e)

//Randomizer
// seq assemble, workbench recipes
R.createSequencedAssembly(Item.of(i.redstone_randomizer, 1), i.smooth_stone_slab, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.prismarine_crystals]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.redstone_torch])
], i.incomplete, e, 'randomizer_sequenced_assembly', 1)
R.ieBlueprint(Item.of(i.redstone_randomizer, 1), [Item.of(i.smooth_stone_slab, 1), Item.of(i.redstone, 1), Item.of(i.stick, 1), Item.of(i.prismarine_crystals, 1)], 'redstonecomponents', e)

//Rain detector
//Replace slabs w/ daylight detector, seq assemble and workbench recipe
R.remove({id: 'gates:rain_detector'}, e)
R.shaped(Item.of(i.rain_detector), ['ggg', 'lll', ' d '], {
	d: i.daylight_sensor,
	g: i.glass,
	l: i.lapis
}, e)
R.createSequencedAssembly(Item.of(i.rain_detector, 1), i.daylight_sensor, [
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.lapis]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.glass])
], i.incomplete, e, 'rain_detector_sequenced_assembly', 2)
R.ieBlueprint(Item.of(i.rain_detector, 1), [i.daylight_sensor, Item.of(i.lapis, 2), Item.of(i.glass, 2)], 'redstonecomponents', e)

//Thunder detector
//Replace slabs w/ rain detector, seq assemble and workbench recipe
R.remove({id: 'gates:thunder_detector'}, e)
R.shaped(Item.of(i.thunder_detector), ['ggg', 'ppp', ' r '], {
	r: i.rain_detector,
	g: i.glass,
	p: {
		type: 'forge:partial_nbt',
		item: 'minecraft:potion',
		nbt: '{Potion:"minecraft:mundane"}'
	}
}, e, 'thunder_detector')
R.createSequencedAssembly(Item.of(i.thunder_detector, 1), i.rain_detector, [
	e.recipes.create.filling(i.incomplete, [i.incomplete, Fluid.of(f.create_potion, 250, '{Potion:"minecraft:mundane"}')]),
	e.recipes.create.deploying(i.incomplete, [i.incomplete, i.glass])
], i.incomplete, e, 'thunder_detector_sequenced_assembly', 2)

		// IE workbench doesnt accept nbt ingredients
		//R.ieBlueprint(Item.of(i.thunder_detector, 1), [i.rain_detector, Item.of('minecraft:potion', '{Potion:"minecraft:mundane"}'), Item.of('minecraft:potion', '{Potion:"minecraft:mundane"}'), Item.of(i.glass, 2)], 'redstonecomponents', e, 'thunder_detector_ie_workbench')

//Remove wireless components, create redstone links fit solution better
R.remove({id: 'gates:receiver'}, e)
R.remove({id: 'gates:transmitter'}, e)
R.remove({id: 'gates:redstone_torch_pearl'}, e)
R.remove({id: 'gates:wireless_redstone_lamp'}, e)
R.remove({id: 'gates:portable_redstone_transmitter'}, e)
R.remove({id: 'gates:frequency_changer'}, e)
R.remove({id: 'gates:waxed_redstone_wire'}, e)

// ############################## Vibrating ##############################

//Sand to shells and heart of the sea
R.viVibrating([
	{ item: 'twigs:tangerine_seashell', count: 1, chance: 0.225 },
	{ item: 'twigs:bronzed_seashell', count: 1, chance: 0.225 },
	{ item: 'twigs:opaline_seashell', count: 1, chance: 0.225 },
	{ item: 'twigs:roseate_seashell', count: 1, chance: 0.225 },
	{ item: 'minecraft:nautilus_shell', count: 1, chance: 0.09 },
	{ item: 'minecraft:heart_of_the_sea', count: 1, chance: 0.01 }
], 'minecraft:sand', e, 'sand_vibrating_to_shells', 300)

// ############################## Dust ##############################

//Sulfur
// Remove sulfur drop from scoria
R.remove({id: 'vintageimprovements:crushing/scoria'}, e)
R.remove({id: 'vintageimprovements:crushing/scoria_recycling'}, e)
// Crush blaze powder to sulfur
R.createCrushing(Item.of('immersiveengineering:dust_sulfur', 1).withChance(0.75), 'minecraft:blaze_powder', e)
//Make TNT require sulfur, nether
R.remove({id: 'minecraft:tnt'}, e)
R.shaped(Item.of('minecraft:tnt', 4), ['ABA','BAB', 'ABA'], {A: 'minecraft:gunpowder', B: 'immersiveengineering:dust_sulfur'}, e)

// ############################## Misc Resources ##############################

// Ersatz leather waxing recipe balance
R.remove({id: 'immersiveengineering:crafting/ersatz_leather'}, e)
R.shapeless(Item.of('immersiveengineering:ersatz_leather', 1), ['minecraft:honeycomb', 'immersiveengineering:hemp_fabric'], e)
R.createDeploying(Item.of('immersiveengineering:ersatz_leather', 1), ['immersiveengineering:hemp_fabric', 'minecraft:honeycomb'], e)

console.log('The engineering event has finished!')
})
