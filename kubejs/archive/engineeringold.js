
/*

ServerEvents.recipes(e => {

console.log('Hello! The engineering event has started!')

const CE = global.CE

let item = {};
var i = global.item;
var R = global.NCH

//Frequently used item assignments
//Crystals, shards
item.speaker = 'supplementaries:speaker_block';
item.amethyst_shard = 'minecraft:amethyst_shard';
item.amethyst_block = 'minecraft:amethyst_block';
item.quartz = 'minecraft:quartz';
item.echo_shard = 'minecraft:echo_shard';
item.sculk = 'minecraft:sculk';
item.budding_amethyst = 'minecraft:budding_amethyst';
item.small_amethyst_bud = 'minecraft:small_amethyst_bud';
item.amethyst_cluster = 'minecraft:amethyst_cluster';
item.prismarine_crystals = 'minecraft:prismarine_crystals';
i.glass_bottle = 'minecraft:glass_bottle';

//Redstone / Rose quartz
item.redstone = 'minecraft:redstone';
item.redstone_torch = 'minecraft:redstone_torch';
item.redstone_block = 'minecraft:redstone_block';
item.rose_quartz = 'create:rose_quartz';
item.rose_quartz_block = 'create_compressed:rose_quartz_crystal_block';
item.polished_rose_quartz = 'create:polished_rose_quartz';
item.polished_rose_quartz_block = 'create_compressed:rose_quartz_polished_block';
item.rose_quartz_plate = 'create_things_and_misc:rose_quartz_sheet';
item.rose_quartz_lamp = 'create:rose_quartz_lamp';
item.electron_tube = 'create:electron_tube';
item.redstone_lamp = 'minecraft:redstone_lamp';
item.daylight_sensor = 'minecraft:daylight_detector';
	item.daylight_detector = 'minecraft:daylight_detector';
item.noteblock = 'minecraft:note_block';
	item.note_block = 'minecraft:note_block';
item.jukebox = 'minecraft:jukebox';
item.iron_pressure_plate = 'minecraft:heavy_weighted_pressure_plate';
item.hopper = 'minecraft:hopper';
item.transmitter = 'creativeengineering:transmitter';
item.redstone_link = 'create:redstone_link';
item.repeater = 'minecraft:repeater';
item.comparator = 'minecraft:comparator';
item.fast_repeater = 'gates:fast_repeater';
item.slow_repeater = 'gates:slow_repeater';
item.redstone_randomizer = 'quark:redstone_randomizer';
item.clock = 'minecraft:clock';
item.rain_detector = 'gates:rain_detector';
item.thunder_detector = 'gates:thunder_detector';
item.component_electronic = 'immersiveengineering:component_electronic';
item.wire_coil_block_steel = 'immersiveengineering:coil_hv';
item.copper_bulb = 'everythingcopper:copper_lamp';

//Create Components
item.andesite_alloy = 'create:andesite_alloy';
item.andesite_casing = 'create:andesite_casing';
item.brass_casing = 'create:brass_casing';
item.cog = 'create:cogwheel'; 
item.shaft = 'create:shaft'; 
item.large_cog = 'create:large_cogwheel'; 
item.gold_hand = 'create:brass_hand';
item.brass_hand = 'create:brass_hand';
item.precision_mechanism = 'create:precision_mechanism';
item.vibration_mechanism = 'create_things_and_misc:vibration_mechanism';
item.redstone_mechanism = 'vintageimprovements:redstone_module';
item.mechanical_piston = 'create:mechanical_piston';
item.mechanical_crafter = 'create:mechanical_crafter';
item.mechanical_press = 'create:mechanical_press';
item.mechanical_saw = 'create:mechanical_saw';
item.cr_pipe = 'create:fluid_pipe';
item.cr_tank = 'create:fluid_tank';
item.redstone_link = 'create:redstone_link';
item.turntable = 'create:turntable';
item.chain_drive = 'create:encased_chain_drive';
item.transmitter = 'creativeengineering:transmitter';
item.display_link = 'create:display_link';

//IE Components
item.vacuum_tube = 'immersiveengineering:electron_tube';

//metal
item.copper_ingot = 'minecraft:copper_ingot';
item.copper_plate = '#forge:plates/copper';
item.copper_rod = 'immersiveposts:stick_copper';
item.copper_block = 'minecraft:copper_block';
item.iron_block = 'minecraft:iron_block';
item.iron_ingot = 'minecraft:iron_ingot';
item.iron_nugget = 'minecraft:iron_nugget';
item.iron_plate = '#forge:plates/iron';
item.iron_wire = '#forge:wires/iron';
item.iron_rod = '#forge:rods/iron';
item.cast_iron_ingot = 'createdeco:cast_iron_ingot';
item.cast_iron_nugget = 'createdeco:cast_iron_nugget';
item.gold_ingot = 'minecraft:gold_ingot';
item.gold_nugget = 'minecraft:gold_nugget';
item.gold_plate = '#forge:plates/gold';
item.gold_rod = '#forge:rods/gold';
item.zinc_nugget = 'create:zinc_nugget';
item.brass_ingot = 'create:brass_ingot';
item.brass_plate = 'create:brass_sheet';
item.steel_ingot = 'immersiveengineering:ingot_steel'
item.electrum_ingot = 'immersiveengineering:ingot_electrum'
item.sheetmetal_slabs = '#forge:sheetmetal_slabs';
item.springs_stiff = '#forge:springs_stiff';
item.springs_weak = '#forge:springs_weak';
item.small_springs_stiff = '#forge:small_springs_stiff';
item.small_springs_weak = '#forge:small_springs_weak';

//coil
item.mv_coil = 'immersiveengineering:coil_mv';
item.lv_coil = 'immersiveengineering:coil_lv';

//dust
item.glowstone = 'minecraft:glowstone_dust';
item.glowstone_block = 'minecraft:glowstone';

//blade
item.sawblade = 'immersiveengineering:sawblade';

//rail
item.rail = 'minecraft:rail';
item.detector_rail = 'minecraft:detector_rail';
item.activator_rail = 'minecraft:activator_rail';
item.powered_rail = 'minecraft:powered_rail';

// misc resources
item.string = 'minecraft:string';
item.glass = 'minecraft:glass';
item.stick = '#forge:rods/wooden';
item.lapis = 'minecraft:lapis_lazuli';
item.diamond = 'minecraft:diamond';
item.planks = '#minecraft:planks';
item.cobblestone = 'minecraft:cobblestone';
	item.cobble = 'minecraft:cobblestone';
item.anystone = '#forge:stone';

//misc items
item.wood_slab = '#minecraft:wooden_slabs';
item.smooth_stone_slab = 'minecraft:smooth_stone_slab';
item.incomplete = 'kubejs:incomplete_item';
item.incomplete_vibration_mechanism = 'create_things_and_misc:incomplete_vibration_mechanism';
item.incomplete_redstone_mechanism = 'vintageimprovements:incomplete_redstone_module';
item.blueprint = 'immersiveengineering:blueprint';
item.empty_schematic = 'create:empty_schematic';
item.crafting_table = 'minecraft:crafting_table';
item.chain = 'minecraft:chain';

// ############################## Crystals / Shards ###########################

//Quartz
//IE Prismarine crystal crushing
//R.ieCrushing(Item.of(item.quartz, 2),item.prismarine_crystals, e, [{chance: 0.25,output:item.quartz}])

//Amethyst
//Budding amethyst sequenced assembly
CE.createSequencedAssembly(Item.of(item.budding_amethyst, 1),item.amethyst_block,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.budding_amethyst]),
	e.recipes.create.filling(item.incomplete, [item.incomplete, Fluid.of('minecraft:water', 250)])
	],item.incomplete,4,e,'budding_amethyst_sequenced_assembly')
//Amethyst cluster crushing
//R.ieCrushing(Item.of(item.amethyst_shard, 1),Item.of(item.amethyst_cluster, 9), e, [{chance: 0.5,output:item.amethyst_shard}])

//Echo shard
//shaped craft
CE.shaped(Item.of(item.echo_shard, 1), [' A ','ABA', ' A '], {A: item.sculk, B: item.amethyst_shard}, e)
// Sequenced assembly
CE.createSequencedAssembly(Item.of(item.echo_shard, 1),item.amethyst_shard,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.sculk]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.sculk]),
	e.recipes.create.pressing(item.incomplete, item.incomplete)
	],item.incomplete,1,e,'echo_shard_sequenced_assembly')

// ############################## Redstone / Rose Quartz ###########################
//Redstone
		//IE Bottling- requires mixin, BottlingRecipes dont take nbt
		//CE.ieBottling([{ item: 'minecraft:redstone' }],{fluid: '#forge:fluid/potion',amount: 25,nbt: { Bottle: 'REGULAR', Potion: 'minecraft:strength' }},25,'create:cinder_flour',e)
	
//redstone torch
//seq assemble	
CE.createSequencedAssembly(Item.of(item.redstone_torch, 1),item.stick,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone])
	],item.incomplete,1,e,'redstone_torch_sequenced_assembly')
//Blueprint recipe
CE.ieBlueprint(Item.of(item.redstone_torch, 4), [Item.of(item.stick, 3), Item.of(item.redstone, 3)],'redstonecomponents', e)
	
//Rose quartz
//Heated mixing
CE.createMixing(Item.of(item.rose_quartz, 1), [Item.of(item.redstone, 6), item.quartz], 'heated', e, 'rose_quartz_heated_mixing')
//Arc furnace
CE.ieArcFurnace(item.rose_quartz, item.quartz, e, [], [item.redstone, item.redstone, item.redstone, item.redstone])
//Pressure Champer
CE.pcPressureChamber(Item.of(item.rose_quartz, 3), [Item.of(item.quartz, 3), Item.of(item.redstone, 9)], 3, e)

//Polished rose quartz
//Charging
CE.createCharging({item: item.polished_rose_quartz, count: 1}, {item: item.rose_quartz}, e, 6000, 300)
CE.createCharging({item: item.polished_rose_quartz_block, count: 1}, {item: item.rose_quartz_block}, e, 50000, 1000)

//Rose quartz plate
//Replace sheet recipe w/ cutting
CE.remove({id:'create_things_and_misc:rose_quartz_plate_craft'}, e)
CE.createSaw(Item.of(item.rose_quartz_plate, 2),item.polished_rose_quartz, e)

//Electron tube
//Electron tube deploying, arc furnace
CE.createDeploying(item.electron_tube, [item.polished_rose_quartz, item.iron_plate], e)
CE.ieArcFurnace(item.electron_tube, item.polished_rose_quartz, e, [], [item.iron_nugget, item.iron_nugget, item.iron_nugget, item.iron_nugget])
//Blueprint recipe
CE.ieBlueprint(Item.of(item.electron_tube, 2), [Item.of(item.polished_rose_quartz, 2), item.iron_plate],'redstonecomponents', e)

// Rose Quartz Lamp
//Replace shapeless w/ more expensive recipe
CE.remove({id:'create:crafting/kinetics/rose_quartz_lamp'}, e)
CE.shaped(Item.of(item.rose_quartz_lamp), ['ABA','BCB','ABA'], {A: item.zinc_nugget, B: item.polished_rose_quartz, C: item.redstone}, e)
// Sequenced assembly
CE.createSequencedAssembly(Item.of(item.rose_quartz_lamp, 1),item.redstone,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.polished_rose_quartz]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.zinc_nugget])
	],item.incomplete,3,e,'rose_quartz_lamp_sequenced_assembly')
//Blueprint recipe
CE.ieBlueprint(Item.of(item.rose_quartz_lamp, 1), [Item.of(item.polished_rose_quartz, 2),Item.of(item.zinc_nugget, 2), item.redstone],'redstonecomponents', e)

//Redstone lamp
//Add alt rose quartz recipe
CE.shaped(Item.of(item.redstone_lamp, 1), [' A ','ABA', ' A '], {A: item.redstone, B: item.rose_quartz}, e)
//Blueprint recipe
CE.ieBlueprint(Item.of(item.redstone_lamp, 1), [Item.of(item.redstone, 3),Item.of(item.rose_quartz, 1)],'redstonecomponents', e)
CE.ieBlueprint(Item.of(item.redstone_lamp, 1), [Item.of(item.redstone, 3),Item.of(item.glowstone_block, 1)],'redstonecomponents', e, 'redstone_to_redstone_lamp_by_rose_quartz')

//Copper bulb
//Replace w/ electron tube recipe
CE.remove({id:'everythingcopper:copper_lamp'}, e)
CE.shaped(Item.of(item.copper_bulb, 2), [' C ','CEC', ' R '], {C: item.copper_block, E: item.electron_tube, R: item.redstone}, e)
//Blueprint recipe
CE.ieBlueprint(Item.of(item.copper_bulb, 3), [Item.of(item.copper_block, 2),Item.of(item.electron_tube, 1), Item.of(item.redstone, 1)],'redstonecomponents', e)

//Daylight sensor
//Quartz, Rose Quartz plate sequenced assembly
CE.createSequencedAssembly(Item.of(item.daylight_sensor, 1),item.wood_slab,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.quartz]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.glass])
	],item.incomplete,2,e,'daylight_sensor_sequenced_assembly')
CE.createSequencedAssembly(Item.of(item.daylight_sensor, 1),item.wood_slab,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.rose_quartz_plate]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.glass])
	],item.incomplete,2,e,'daylight_sensor_sequenced_assembly_rose_quartz')	
//blueprint recipes
CE.ieBlueprint(Item.of(item.daylight_sensor, 1), [item.wood_slab,Item.of(item.quartz, 2), Item.of(item.glass, 2)],'redstonecomponents', e)
CE.ieBlueprint(Item.of(item.daylight_sensor, 1), [item.wood_slab,Item.of(item.rose_quartz_plate, 2), Item.of(item.glass, 2)],'redstonecomponents', e, 'rose_quartz_to_daylight_sensor_by_redstonecomponents')

//Dropper
//Replace cobble w/ stone, add wooden chute, workbench recipe
CE.remove({id:'minecraft:dropper'}, e)
CE.shaped(Item.of('minecraft:dropper', 1), ['SSS','SHS', 'SRS'], {S: item.anystone, H: 'quark:chute', R: item.redstone}, e)
CE.ieBlueprint(Item.of('minecraft:dropper', 1), ['quark:chute',item.redstone,item.anystone,item.anystone,item.anystone,item.anystone],'redstonecomponents', e, 'dropper_by_redstonecomponents')

//Dispenser
//Remove vanilla craft, must be crafted from dropper
CE.remove({id:'minecraft:dispenser'}, e)

//Observer
//Replace cobble w/ stone, workbench recipe
CE.replaceInput(
  { id: 'minecraft:observer' },
  item.cobble,
  item.anystone, e
)
CE.ieBlueprint(Item.of('minecraft:observer', 1), [item.quartz,item.redstone,item.anystone,item.anystone,item.anystone,item.anystone],'redstonecomponents', e, 'observer_by_redstonecomponents')

//Pison
//Replace cobble w/ stone, workbench recipe
CE.replaceInput(
  { id: 'minecraft:pison' },
  item.cobble,
  item.anystone, e
)
CE.ieBlueprint(Item.of('minecraft:piston', 1), [item.iron_ingot,item.redstone,item.planks,item.planks,item.anystone,item.anystone],'redstonecomponents', e, 'piston_by_redstonecomponents')

//Sticky piston
//Slime filling, more efficient
CE.createFilling('minecraft:sticky_piston',[Fluid.of('create_things_and_misc:slime', 50),['minecraft:piston']], e, 'piston_slime_filling')

//Hopper
//Workbench recipe
CE.ieBlueprint(Item.of('minecraft:hopper', 1), [Item.of(item.iron_ingot, 3), '#forge:chests'],'redstonecomponents', e, 'hopper_by_redstonecomponents')

//Transmitter craft, seq assemble, workbench recipe
CE.shaped(Item.of(item.transmitter, 1), [' L ','PPP', 'RAR'], {A: item.amethyst_shard, R: item.redstone, P: item.copper_plate, L: 'minecraft:lightning_rod'}, e)
CE.createSequencedAssembly(Item.of(item.transmitter, 1),item.copper_plate,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.amethyst_shard]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, 'minecraft:lightning_rod'])
	],item.incomplete,1,e,'transmitter_sequenced_assembly')
CE.ieBlueprint(Item.of(item.transmitter, 2), [Item.of(item.copper_plate, 2),Item.of(item.redstone, 2), Item.of('minecraft:lightning_rod', 1), Item.of(item.amethyst_shard, 1)],'redstonecomponents', e)

// Redstone link andesite tier, add transmitter
CE.remove({id:'create:crafting/logistics/redstone_link'}, e)
CE.shaped(Item.of(item.redstone_link, 2), ['T','C'], {T: item.transmitter, C: item.andesite_casing}, e)
//Seq assemble, workbench recipe
CE.createSequencedAssembly(Item.of(item.redstone_link, 4),item.andesite_casing,[
	e.recipes.create.cutting(item.incomplete, item.incomplete),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.transmitter]),
	e.recipes.create.cutting(item.incomplete, item.incomplete)
	],item.incomplete,1,e,'redstone_link_sequenced_assembly')
CE.ieBlueprint(Item.of(item.redstone_link, 4), [Item.of(item.andesite_casing, 1), Item.of(item.transmitter, 1)],'redstonecomponents', e)

// ##############################   Gates  #############################

//Repeater 
// seq assemble, workbench recipes
CE.createSequencedAssembly(Item.of(item.repeater, 1),item.smooth_stone_slab,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch])
	],item.incomplete,1,e,'repeater_sequenced_assembly')
CE.ieBlueprint(Item.of(item.repeater, 1), [Item.of(item.smooth_stone_slab, 1), Item.of(item.redstone, 2), Item.of(item.stick, 1)],'redstonecomponents', e)
	
//Fast Repeater 
// seq assemble, workbench recipes
CE.createSequencedAssembly(Item.of(item.fast_repeater, 1),item.repeater,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.gold_nugget]),
	e.recipes.create.pressing(item.incomplete, item.incomplete)
	],item.incomplete,5,e,'fast_repeater_sequenced_assembly')
CE.ieBlueprint(Item.of(item.fast_repeater, 1), [Item.of(item.repeater, 1), Item.of(item.gold_nugget, 3)],'redstonecomponents', e)

//Slow Repeater 
//Change to cast iron, seq assemble, workbench recipes
CE.remove({id:'gates:slow_repeater'}, e)
CE.shapeless(Item.of(item.slow_repeater, 1),[item.cast_iron_ingot, item.repeater], e)
CE.createSequencedAssembly(Item.of(item.slow_repeater, 1),item.repeater,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.cast_iron_nugget]),
	e.recipes.create.pressing(item.incomplete, item.incomplete)
	],item.incomplete,5,e,'slow_repeater_sequenced_assembly')
CE.ieBlueprint(Item.of(item.slow_repeater, 1), [Item.of(item.repeater, 1), Item.of(item.cast_iron_nugget, 3)],'redstonecomponents', e)

//Comparator
// seq assemble, workbench recipes
CE.createSequencedAssembly(Item.of(item.comparator, 1),item.smooth_stone_slab,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.quartz]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch])
	],item.incomplete,1,e,'comparator_sequenced_assembly')
CE.ieBlueprint(Item.of(item.comparator, 1), [Item.of(item.smooth_stone_slab, 1), Item.of(item.redstone, 1), Item.of(item.stick, 1), Item.of(item.quartz, 1)],'redstonecomponents', e)

//Randomizer
// seq assemble, workbench recipes
CE.createSequencedAssembly(Item.of(item.redstone_randomizer, 1),item.smooth_stone_slab,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.prismarine_crystals]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.redstone_torch])
	],item.incomplete,1,e,'randomizer_sequenced_assembly')
CE.ieBlueprint(Item.of(item.redstone_randomizer, 1), [Item.of(item.smooth_stone_slab, 1), Item.of(item.redstone, 1), Item.of(item.stick, 1), Item.of(item.prismarine_crystals, 1)],'redstonecomponents', e)

//Rain detector
//Replace slabs w/ daylight detector, seq assemble and workbench recipe
CE.remove({id:'gates:rain_detector'}, e)
CE.shaped(Item.of(item.rain_detector),['ggg', 'lll', ' d '],{
    d: item.daylight_sensor,
    g: item.glass,
    l: item.lapis},e)
CE.createSequencedAssembly(Item.of(item.rain_detector, 1),item.daylight_sensor,[
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.lapis]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.glass])
	],item.incomplete,2,e,'rain_detector_sequenced_assembly')	
CE.ieBlueprint(Item.of(item.rain_detector, 1), [item.daylight_sensor,Item.of(item.lapis, 2), Item.of(item.glass, 2)],'redstonecomponents', e)
    
//Thunder detector
//Replace slabs w/ rain detector, seq assemble and workbench recipe
CE.remove({id:'gates:thunder_detector'}, e)
CE.shaped(Item.of(item.thunder_detector),['ggg', 'ppp', ' r '],{
    r: item.rain_detector,
    g: item.glass,
    p: {
        type: 'forge:partial_nbt',
        item: 'minecraft:potion',
        nbt: '{Potion:"minecraft:mundane"}'
    }}, e, 'thunder_detector')  
CE.createSequencedAssembly(Item.of(item.thunder_detector, 1),item.rain_detector,[
	e.recipes.create.filling(item.incomplete, [item.incomplete, Fluid.of('create:potion', 250, '{Potion:"minecraft:mundane"}')]),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, item.glass])
	],item.incomplete,2,e,'thunder_detector_sequenced_assembly')	

		// IE workbench doesnt accept nbt ingredients 
		//CE.ieBlueprint(Item.of(item.thunder_detector, 1), [item.rain_detector, Item.of('minecraft:potion', '{Potion:"minecraft:mundane"}'), Item.of('minecraft:potion', '{Potion:"minecraft:mundane"}'), Item.of(item.glass, 2)],'redstonecomponents', e, 'thunder_detector_ie_workbench')

//Remove wireless components, create redstone links fit solution better
CE.remove({id:'gates:receiver'}, e)
CE.remove({id:'gates:transmitter'}, e)
CE.remove({id:'gates:redstone_torch_pearl'}, e)
CE.remove({id:'gates:wireless_redstone_lamp'}, e)
CE.remove({id:'gates:portable_redstone_transmitter'}, e)
CE.remove({id:'gates:frequency_changer'}, e)
CE.remove({id:'gates:waxed_redstone_wire'}, e)


// ############################## Vibrating ##############################

//Sand to shells and heart of the sea
CE.viVibrating([
    { item: 'twigs:tangerine_seashell', count: 1, chance: 0.225 },                      
    { item: 'twigs:bronzed_seashell', count: 1, chance: 0.225 },
    { item: 'twigs:opaline_seashell', count: 1, chance: 0.225 }, 
    { item: 'twigs:roseate_seashell', count: 1, chance: 0.225 },
    { item: 'minecraft:nautilus_shell', count: 1, chance: 0.09 },    
    { item: 'minecraft:heart_of_the_sea', count: 1, chance: 0.01 }    
  ],'minecraft:sand',e, 300,'sand_vibrating_to_shells')

// ############################## Dust ##############################

//Glowstone
//IE Crushing glow sacs
//R.ieCrushing(Item.of('minecraft:cyan_dye', 1),'minecraft:glow_ink_sac', e, [{chance: 0.50,output:item.glowstone}])

//Sulfur
// Remove sulfur drop from scoria
CE.remove({id:'vintageimprovements:crushing/scoria'}, e)
CE.remove({id:'vintageimprovements:crushing/scoria_recycling'}, e)
// Crush blaze powder to sulfur
CE.createCrushing(Item.of('immersiveengineering:dust_sulfur', 1).withChance(0.75), 'minecraft:blaze_powder', e)
//R.ieCrushing(Item.of('immersiveengineering:dust_sulfur', 1),'minecraft:blaze_powder', e, [{chance: 0.25,output:'immersiveengineering:dust_sulfur'}])
//Make TNT require sulfur, nether 
CE.remove({id:'minecraft:tnt'}, e)
CE.shaped(Item.of('minecraft:tnt',4), ['ABA','BAB', 'ABA'], {A: 'minecraft:gunpowder',B: 'immersiveengineering:dust_sulfur'}, e)

// ############################## Misc Resources ##############################

// Ersatz leather waxing recipe balance 
CE.remove({id:'immersiveengineering:crafting/ersatz_leather'}, e)
CE.shapeless(Item.of('immersiveengineering:ersatz_leather', 1),['minecraft:honeycomb','immersiveengineering:hemp_fabric'], e)
CE.createDeploying(Item.of('immersiveengineering:ersatz_leather', 1),['immersiveengineering:hemp_fabric', 'minecraft:honeycomb'], e)

console.log('The engineering event has finished!')
})

*/