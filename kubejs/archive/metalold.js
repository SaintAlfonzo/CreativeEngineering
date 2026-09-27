ServerEvents.recipes(e => {

console.log('Hello! The metal event has started!')
console.log('EVENT:', e)

const CE = global.CE



/*

// Copper
// Everything copper changes
// remove compression
CE.remove({id:'everythingcopper:copper_nugget'}, e)
CE.remove({id:'everythingcopper:copper_ingot_from_nugget'}, e)
// Replace recipes w/ IE copper nug
CE.replaceInput(
  { mod: 'everythingcopper' },
  'everythingcopper:copper_nugget',
  '#forge:nuggets/copper', e
)
CE.replaceOutput(
  { mod: 'everythingcopper' },
  'everythingcopper:copper_nugget',
  'immersiveengineering:nugget_copper', e
)
// Remove unfinished copper shield
CE.remove({id:'everythingcopper:copper_shield'}, e)

// Create changes
// remove compression
CE.remove({id:'create:crafting/materials/copper_nugget'}, e)
CE.remove({id:'create:crafting/materials/copper_ingot'}, e)

//Create input and output swaps to IE copper nug
CE.replaceInput(
  { mod: 'create' },
  'create:copper_nugget',
  '#forge:nuggets/copper', e
)
CE.replaceOutput(
  { mod: 'create' },
  'create:copper_nugget',
  'immersiveengineering:nugget_copper', e
)
CE.replaceOutput(
  { mod: 'create_compressed' },
  'create:copper_nugget',
  'immersiveengineering:nugget_copper', e
)
CE.replaceOutput(
  { mod: 'createsifter' },
  'create:copper_nugget',
  'immersiveengineering:nugget_copper', e
)
CE.replaceInput(
  { mod: 'bellsandwhistles' },
  'create:copper_nugget',
  '#forge:nuggets/copper', e
)
CE.replaceInput(
  { mod: 'create_ultimate_factory' },
  'create:copper_nugget',
  '#forge:nuggets/copper', e
)
// Replace all copper nuggets with tag
e.replaceInput({}, 'immersiveengineering:nugget_copper', '#forge:nuggets/copper')

// Replace copper sheet recipes with copper plates
CE.replaceInput(
  {input: 'create:copper_sheet'},
  'create:copper_sheet',
  '#forge:plates/copper', e
)
CE.replaceOutput(
  {input: 'create:copper_sheet'},
  'create:copper_sheet',
  'immersiveengineering:plate_copper', e
)

// Replace all copper plates with tag
e.replaceInput({}, 'immersiveengineering:plate_copper', '#forge:plates/copper')

// Add rolling recipe for copper rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/copper_ingot'}, e)      
CE.createRolling(Item.of('immersiveposts:stick_copper', 4), '#forge:ingots/copper', e)
CE.remove({id:'immersiveengineering:metalpress/rod_copper'}, e)
CE.ieMetalPress(Item.of('immersiveposts:stick_copper', 4), '#forge:ingots/copper', 'immersiveengineering:mold_rod', e)

//Add recipes for copper wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'immersiveengineering:crafting/wire_copper'}, e)
CE.remove({id:'createaddition:rolling/copper_plate'}, e)
CE.shapeless(Item.of('immersiveengineering:wire_copper',2),['#forge:plates/copper','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('immersiveengineering:wire_copper', 4), '#forge:plates/copper', e)
CE.remove({id:'immersiveengineering:metalpress/wire_copper'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:wire_copper', 4), '#forge:plates/copper', 'immersiveengineering:mold_wire', e)

// Handle copper spool, lv wire spool
CE.remove({id:'createaddition:crafting/spool'}, e)
CE.remove({id:'createaddition:crafting/copper_spool'}, e)
CE.remove({id:'createaddition:crafting/festive_spool'}, e)
CE.shaped(Item.of('creativeengineering:spool',16), ['A','B','A'], {A: 'immersiveengineering:plate_iron',B: 'minecraft:iron_nugget'}, e)
CE.replaceInput(
  { mod: 'createaddition' },
  'createaddition:copper_spool',
  'immersiveengineering:wirecoil_copper', e
)
CE.replaceInput(
  { id: 'immersiveengineering:crafting/wirecoil_copper' },
  '#forge:rods/wooden',
  'creativeengineering:spool', e
)*/
/*
// Iron
// Iron sheet removal 
CE.remove({id:'create:pressing/iron_ingot'}, e)
CE.remove({id:'create_compressed:iron_sheet_block'}, e)
CE.remove({id:'create_compressed:iron_sheet'}, e)
// Replace sheet recipes w/ iron plate
CE.createPressing('immersiveengineering:plate_iron', Item.of('minecraft:iron_ingot', 1), e)
CE.shapeless(Item.of('immersiveengineering:plate_iron',9), Item.of('create_compressed:iron_sheet_block',1), e)
CE.shapeless(Item.of('create_compressed:iron_sheet_block',1), Item.of('immersiveengineering:plate_iron',9), e)

// Replace iron sheets in recipes w/ iron plates
CE.replaceInput(
  {input: 'create:iron_sheet'},
  'create:iron_sheet',
  '#forge:plates/iron', e
)

// Add rolling recipe for iron rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/iron_ingot'}, e)      
CE.createRolling(Item.of('immersiveengineering:stick_iron', 4), '#forge:ingots/iron', e)
CE.remove({id:'immersiveengineering:metalpress/rod_iron'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:stick_iron', 4), '#forge:ingots/iron', 'immersiveengineering:mold_rod', e)

//Add recipes for iron wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/iron_plate'}, e)
CE.shapeless(Item.of('creativeengineering:iron_wire',2),['#forge:plates/iron','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:iron_wire', 4), '#forge:plates/iron', e)
CE.remove({id:'immersiveengineering:metalpress/wire_iron'}, e)
CE.ieMetalPress(Item.of('creativeengineering:iron_wire', 4), '#forge:plates/iron', 'immersiveengineering:mold_wire', e)

// Gold
// Gold sheet removal 
CE.remove({id:'create:pressing/gold_ingot'}, e)
CE.remove({id:'create_compressed:gold_sheet_block'}, e)
CE.remove({id:'create_compressed:gold_sheet'}, e)
CE.remove({id:'immersiveengineering:crafting/plate_gold_hammering'}, e)
CE.remove({id:'immersiveengineering:metalpress/plate_gold'}, e)
// Replace sheet recipes w/ gold plate
CE.createPressing('immersiveengineering:plate_gold', Item.of('minecraft:gold_ingot', 1), e)
CE.shapeless(Item.of('immersiveengineering:plate_gold',1), [Item.of('immersiveengineering:hammer',1), Item.of('minecraft:gold_ingot',1)], e)
CE.ieMetalPress('immersiveengineering:plate_gold', 'minecraft:gold_ingot', 'immersiveengineering:mold_plate', e)
CE.shapeless(Item.of('immersiveengineering:plate_gold',9), Item.of('create_compressed:gold_sheet_block',1), e)
CE.shapeless(Item.of('create_compressed:gold_sheet_block',1), Item.of('immersiveengineering:plate_gold',9), e)
// Replace gold sheets in recipes w/ gold plates
CE.replaceInput(
  {input: 'create:gold_sheet'},
  'create:gold_sheet',
  '#forge:plates/gold', e
)

// Add rolling recipe for gold rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/gold_ingot'}, e)
CE.replaceInput(
	{id:'immersiveposts:has_gold_rod'}, 
	'#forge:ingots/gold',
	'minecraft:gold_ingot', e
)      
CE.createRolling(Item.of('immersiveposts:stick_gold', 4), 'minecraft:gold_ingot', e)
CE.remove({id:'immersiveengineering:metalpress/rod_gold'}, e)
CE.ieMetalPress(Item.of('immersiveposts:stick_gold', 4), 'minecraft:gold_ingot', 'immersiveengineering:mold_rod', e)

// Replace create recycle for gold sheet, whistle
CE.replaceInput(
	{id:'create:crushing/update_one/scrap_gold_sheet'}, 
	'create:golden_sheet',
	'#forge:plates/gold', e
)  
CE.replaceInput(
	{id:'create_things_and_misc:portable_whistle_craft'}, 
	'create:golden_sheet',
	'#forge:plates/gold', e
)  

//Add recipes for gold wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/gold_plate'}, e)
CE.shapeless(Item.of('creativeengineering:gold_wire',2),['#forge:plates/gold','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:gold_wire', 4), '#forge:plates/gold', e)
CE.remove({id:'immersiveengineering:metalpress/wire_gold'}, e)
CE.ieMetalPress(Item.of('creativeengineering:gold_wire', 4), '#forge:plates/gold', 'immersiveengineering:mold_wire', e)
// Replace recipes with CE gold wire
CE.replaceInput(
  {input: 'createaddition:gold_wire'},
  'createaddition:gold_wire',
  '#forge:wire/gold', e
)*/



















/*

// Netherite
// Remove hard to work netherite recipes
CE.remove({id:'vintageimprovements:rolling/netherite_plate'}, e)
CE.remove({id:'vintageimprovements:rolling/netherite_rod'}, e)
CE.remove({id:'vintageimprovements:coiling/netherite_wire'}, e)
CE.remove({id:'vintageimprovements:coiling/netherite_rod'}, e)

// Merge netherite shard item with netherite nugget recipes
CE.remove({id:'create_crush_everything:netherite_from_shards'}, e)
CE.remove({id:'createdeco:netherite_ingot'}, e)
CE.remove({id:'createdeco:netherite_nugget_from_netherite_ingot'}, e)
// CE.shapeless(Item.of('create_crush_everything:netherite_shard',9), Item.of('minecraft:netherite_ingot',1), e)
CE.shapeless(Item.of('minecraft:netherite_ingot',1), Item.of('create_crush_everything:netherite_shard',9), e)
// Replace recipes with create recycle netherite shard
CE.replaceInput(
  {input: 'createdeco:netherite_nugget'},
  'createdeco:netherite_nugget',
  'create_crush_everything:netherite_shard', e
)
// Increase yield of netherite recycling
CE.remove({id: 'create:crushing/netherite/scrap_netherite'}, e)
CE.createCrushing(Item.of('create_crush_everything:netherite_shard', 9), 'minecraft:netherite_ingot', e)
//Remove Create Deco netherite sheet
CE.remove({id: 'minecraft:pressing/netherite_sheet'}, e)
CE.replaceInput(
  {input: 'createdeco:netherite_sheet'},
  'createdeco:netherite_sheet',
  'vintageimprovements:netherite_sheet', e
)

// Zinc
// Remove extra zinc knife
CE.remove({id: 'create_things_and_misc:zinc_knif_craft'}, e)
// Remove VI zinc wire, replace with CE
CE.remove({id: 'immersiveengineering:metalpress/wire_zinc'}, e)
CE.remove({id: 'vintageimprovements:rolling/zinc_plate'}, e)
CE.replaceInput(
  {input: 'vintageimprovements:zinc_wire'},
  'vintageimprovements:zinc_wire',
  'creativeengineering:zinc_wire', e
)
//Add recipes for zinc wire, manual and rolling, double efficiency on roller and press
CE.shapeless(Item.of('creativeengineering:zinc_wire',2),['#forge:plates/zinc','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:zinc_wire', 4), '#forge:plates/zinc', e)
CE.ieMetalPress(Item.of('creativeengineering:zinc_wire', 4), '#forge:plates/zinc', 'immersiveengineering:mold_wire', e)
// Add Zinc rod recipe
CE.shaped(Item.of('vintageimprovements:zinc_rod', 4), ['A','A'], {A: 'create:zinc_ingot'}, e)
// Add rolling recipe for iron rod, double efficiency on roller and press
CE.remove({id:'vintageimprovements:rolling/zinc_ingot'}, e)      
CE.createRolling(Item.of('vintageimprovements:zinc_rod', 4), '#forge:ingots/zinc', e)
CE.remove({id:'immersiveengineering:metalpress/rod_zinc'}, e)
CE.ieMetalPress(Item.of('vintageimprovements:zinc_rod', 4), '#forge:ingots/zinc', 'immersiveengineering:mold_rod', e)
// Add zinc plate recipes
CE.createPressing('creativeengineering:zinc_plate', Item.of('create:zinc_ingot', 1), e)
CE.shapeless(Item.of('creativeengineering:zinc_plate',1), [Item.of('immersiveengineering:hammer',1), Item.of('create:zinc_ingot',1)], e)
//CE.ieMetalPress('creativeengineering:zinc_plate', 'create:zinc_ingot', 'immersiveengineering:mold_plate', e)
// Remove zinc plate recipes
CE.remove({id: 'createaddition:pressing/zinc_ingot'}, e)
CE.remove({id: 'minecraft:pressing/zinc_sheet'}, e)

//Aluminum
//Remove redundant aluminum items
CE.remove({id: 'vintageimprovements:pressing/aluminum_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/aluminum_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/aluminum_plate'}, e)

// Add rolling recipe for aluminum rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/aluminum_ingot'}, e)      
CE.createRolling(Item.of('immersiveengineering:stick_aluminum', 4), '#forge:ingots/aluminum', e)
CE.remove({id:'immersiveengineering:metalpress/rod_aluminum'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:stick_aluminum', 4), '#forge:ingots/aluminum', 'immersiveengineering:mold_rod', e)

//Add recipes for aluminum wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'immersiveengineering:crafting/wire_aluminum'}, e)
CE.remove({id:'createaddition:rolling/aluminum_plate'}, e)
CE.shapeless(Item.of('immersiveengineering:wire_aluminum',2),['#forge:plates/aluminum','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('immersiveengineering:wire_aluminum', 4), '#forge:plates/aluminum', e)
CE.remove({id:'immersiveengineering:metalpress/wire_aluminum'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:wire_aluminum', 4), '#forge:plates/aluminum', 'immersiveengineering:mold_wire', e)

//Lead
//Remove redundant lead items
CE.remove({id: 'vintageimprovements:pressing/lead_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/lead_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/lead_plate'}, e)
CE.remove({id: 'vintageimprovements:coiling/lead_rod'}, e)
CE.remove({id: 'vintageimprovements:coiling/lead_wire'}, e)

// Add rolling recipe for lead rod, double efficiency on roller and press
CE.remove({id:'vintageimprovements:rolling/lead_ingot'}, e)      
CE.createRolling(Item.of('immersiveposts:stick_lead', 4), '#forge:ingots/lead', e)
CE.remove({id:'immersiveengineering:metalpress/rod_lead'}, e)
CE.ieMetalPress(Item.of('immersiveposts:stick_lead', 4), '#forge:ingots/lead', 'immersiveengineering:mold_rod', e)

//Add recipes for lead wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'immersiveengineering:crafting/wire_lead'}, e)
CE.remove({id:'createaddition:rolling/lead_plate'}, e)
CE.shapeless(Item.of('immersiveengineering:wire_lead',2),['#forge:plates/lead','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('immersiveengineering:wire_lead', 4), '#forge:plates/lead', e)
CE.remove({id:'immersiveengineering:metalpress/wire_lead'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:wire_lead', 4), '#forge:plates/lead', 'immersiveengineering:mold_wire', e)

//Remove redundant silver items
CE.remove({id: 'vintageimprovements:pressing/silver_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/silver_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/silver_plate'}, e)

// Add rolling recipe for silver rod, double efficiency on roller and press
CE.createRolling(Item.of('immersiveposts:stick_silver', 4), '#forge:ingots/silver', e)
CE.remove({id:'immersiveengineering:metalpress/rod_silver'}, e)
CE.ieMetalPress(Item.of('immersiveposts:stick_silver', 4), '#forge:ingots/silver', 'immersiveengineering:mold_rod', e)

//Add recipes for silver wire, manual and rolling, double efficiency on roller and press
CE.shapeless(Item.of('creativeengineering:silver_wire',2),['#forge:plates/silver','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:silver_wire', 4), '#forge:plates/silver', e)
CE.remove({id:'immersiveengineering:metalpress/wire_silver'}, e)
CE.ieMetalPress(Item.of('creativeengineering:silver_wire', 4), '#forge:plates/silver', 'immersiveengineering:mold_wire', e)

//Remove redundant nickel items
CE.remove({id: 'vintageimprovements:pressing/nickel_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/nickel_ingot'}, e)
CE.remove({id:'immersiveengineering:metalpress/rod_nickel'}, e)
CE.remove({id: 'vintageimprovements:rolling/nickel_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/wire_nickel'}, e)

// Add rolling recipe for nickel rod, double efficiency on roller and press
CE.createRolling(Item.of('immersiveposts:stick_nickel', 4), '#forge:ingots/nickel', e)
CE.ieMetalPress(Item.of('immersiveposts:stick_nickel', 4), '#forge:ingots/nickel', 'immersiveengineering:mold_rod', e)

//Add recipes for nickel wire, manual and rolling, double efficiency on roller and press
CE.shapeless(Item.of('creativeengineering:nickel_wire',2),['#forge:plates/nickel','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:nickel_wire', 4), '#forge:plates/nickel', e)
CE.ieMetalPress(Item.of('creativeengineering:nickel_wire', 4), '#forge:plates/nickel', 'immersiveengineering:mold_wire', e)

//Remove redundant uranium items
CE.remove({id: 'vintageimprovements:pressing/uranium_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/uranium_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/uranium_plate'}, e)
CE.remove({id: 'vintageimprovements:coiling/uranium_wire'}, e)
CE.remove({id: 'vintageimprovements:coiling/uranium_rod'}, e)

// Add rolling recipe for uranium rod, double efficiency on roller and press
CE.createRolling(Item.of('immersiveposts:stick_uranium', 4), '#forge:ingots/uranium', e)
CE.remove({id:'immersiveengineering:metalpress/rod_uranium'}, e)
CE.ieMetalPress(Item.of('immersiveposts:stick_uranium', 4), '#forge:ingots/uranium', 'immersiveengineering:mold_rod', e)

//Remove redundant cast iron items
CE.remove({id: 'vintageimprovements:pressing/cast_iron_ingot'}, e)
//CE.remove({id: 'vintageimprovements:rolling/cast_iron_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/cast_iron_plate'}, e)
CE.remove({id: 'vintageimprovements:coiling/cast_iron_wire'}, e)
CE.remove({id: 'vintageimprovements:coiling/cast_iron_rod'}, e)

//Remove redundant steel items
CE.remove({id: 'vintageimprovements:rolling/steel_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/steel_plate'}, e)
CE.remove({id: 'davebuildingmod:rec_steel_ingot'}, e)
CE.remove({id: 'davebuildingmod:rec_steel_block'}, e)

// Add rolling recipe for steel rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/steel_ingot'}, e)
CE.createRolling(Item.of('immersiveengineering:stick_steel', 4), '#forge:ingots/steel', e)
CE.remove({id:'immersiveengineering:metalpress/rod_steel'}, e)
CE.ieMetalPress(Item.of('immersiveengineering:stick_steel', 4), '#forge:ingots/steel', 'immersiveengineering:mold_rod', e)

//Add recipes for steel wire, manual and rolling, double efficiency on roller and press
CE.remove({id:'immersiveengineering:crafting/wire_steel'}, e)
CE.remove({id:'createaddition:rolling/steel_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/wire_steel'}, e)
CE.shapeless(Item.of('immersiveengineering:wire_steel',2),['#forge:plates/steel','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('immersiveengineering:wire_steel', 4), '#forge:plates/steel', e)
CE.ieMetalPress(Item.of('immersiveengineering:wire_steel', 4), '#forge:plates/steel', 'immersiveengineering:mold_wire', e)

//Rose gold nugget recipes
CE.shapeless(Item.of('creativeengineering:rose_gold_nugget',9), Item.of('mca:rose_gold_ingot',1), e)
CE.shapeless(Item.of('mca:rose_gold_ingot',1), Item.of('creativeengineering:rose_gold_nugget',9), e)

// Add rose gold rod recipe
CE.shaped(Item.of('creativeengineering:rose_gold_rod', 4), ['A','A'], {A: 'mca:rose_gold_ingot'}, e)
// Add rolling recipe for rose gold rod, double efficiency on roller and press
CE.createRolling(Item.of('creativeengineering:rose_gold_rod', 4), 'mca:rose_gold_ingot', e)
CE.ieMetalPress(Item.of('creativeengineering:rose_gold_rod', 4), 'mca:rose_gold_ingot', 'immersiveengineering:mold_rod', e)

// Add brass rod recipe
CE.shaped(Item.of('createaddition:brass_rod', 4), ['A','A'], {A: 'create:brass_ingot'}, e)
// Add rolling recipe for brass rod, double efficiency on roller and press
CE.remove({id:'createaddition:rolling/brass_ingot'}, e)
CE.remove({id:'immersiveengineering:metalpress/rod_brass'}, e)
CE.createRolling(Item.of('createaddition:brass_rod', 4), 'create:brass_ingot', e)
CE.ieMetalPress(Item.of('createaddition:brass_rod', 4), 'create:brass_ingot', 'immersiveengineering:mold_rod', e)

// Remove extra brass knife
CE.remove({id: 'create_things_and_misc:brass_knife_craft'}, e)

//brass wire
CE.remove({id: 'vintageimprovements:rolling/brass_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/wire_brass'}, e)
CE.shapeless(Item.of('creativeengineering:brass_wire',2),['#forge:plates/brass','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('creativeengineering:brass_wire', 4), '#forge:plates/brass', e)
CE.ieMetalPress(Item.of('creativeengineering:brass_wire', 4), '#forge:plates/brass', 'immersiveengineering:mold_wire', e)

// constantan
CE.remove({id: 'vintageimprovements:rolling/constantan_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/wire_constantan'}, e)
CE.remove({id:'createaddition:rolling/constantan_ingot'}, e)
CE.remove({id:'immersiveengineering:metalpress/rod_constantan'}, e)
CE.remove({id: 'vintageimprovements:pressing/constantan_ingot'}, e)
CE.remove({id: 'vintageimprovements:rolling/constantan_ingot'}, e)
CE.createRolling(Item.of('immersiveposts:stick_constantan', 4), 'immersiveengineering:ingot_constantan', e)
CE.ieMetalPress(Item.of('immersiveposts:stick_constantan', 4), 'immersiveengineering:ingot_constantan', 'immersiveengineering:mold_rod', e)

//electrum
CE.remove({id: 'createaddition:crafting/electrum_ingot'}, e)
CE.remove({id: 'createaddition:crafting/electrum_nugget'}, e)
CE.replaceOutput(
  {output: 'createaddition:electrum_nugget'},
  'createaddition:electrum_nugget',
  'immersiveengineering:nugget_electrum', e
)
CE.replaceOutput(
  {output: 'createaddition:electrum_ingot'},
  'createaddition:electrum_ingot',
  'immersiveengineering:ingot_electrum', e
)
CE.replaceOutput(
  {output: 'createaddition:electrum_sheet'},
  'createaddition:electrum_sheet',
  'immersiveengineering:plate_electrum', e
)
CE.remove({id: 'vintageimprovements:rolling/electrum_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/wire_electrum'}, e)
CE.shapeless(Item.of('immersiveengineering:wire_electrum',2),['#forge:plates/electrum','immersiveengineering:wirecutter'], e)
CE.createRolling(Item.of('immersiveengineering:wire_electrum', 4), '#forge:plates/electrum', e)
CE.ieMetalPress(Item.of('immersiveengineering:wire_electrum', 4), '#forge:plates/electrum', 'immersiveengineering:mold_wire', e)

CE.remove({id:'immersiveengineering:crafting/wire_electrum'}, e)
CE.remove({id:'createaddition:rolling/electrum_ingot'}, e)
CE.remove({id:'createaddition:rolling/electrum_plate'}, e)
CE.remove({id:'immersiveengineering:metalpress/rod_electrum'}, e)
CE.createRolling(Item.of('immersiveposts:stick_electrum', 4), 'immersiveengineering:ingot_electrum', e)
CE.ieMetalPress(Item.of('immersiveposts:stick_electrum', 4), 'immersiveengineering:ingot_electrum', 'immersiveengineering:mold_rod', e)

//Redundant create compressed ores
CE.remove({id:'create_compressed:crushed_raw_osmium'}, e)
CE.remove({id:'create_compressed:crushed_raw_osmium_pile'}, e)
CE.remove({id:'create_compressed:crushed_raw_platinum'}, e)
CE.remove({id:'create_compressed:crushed_raw_platinum_pile'}, e)
CE.remove({id:'create_compressed:crushed_raw_tin'}, e)
CE.remove({id:'create_compressed:crushed_raw_tin_pile'}, e)

//Plate blocks
CE.shapeless(Item.of('immersiveengineering:plate_aluminum',9), Item.of('creativeengineering:aluminum_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:aluminum_plate_block',1), Item.of('immersiveengineering:plate_aluminum',9), e)

CE.shapeless(Item.of('createdeco:cast_iron_sheet',9), Item.of('creativeengineering:cast_iron_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:cast_iron_plate_block',1), Item.of('createdeco:cast_iron_sheet',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_constantan',9), Item.of('creativeengineering:constantan_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:constantan_plate_block',1), Item.of('immersiveengineering:plate_constantan',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_electrum',9), Item.of('creativeengineering:electrum_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:electrum_plate_block',1), Item.of('immersiveengineering:plate_electrum',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_lead',9), Item.of('creativeengineering:lead_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:lead_plate_block',1), Item.of('immersiveengineering:plate_lead',9), e)

CE.shapeless(Item.of('vintageimprovements:netherite_sheet',9), Item.of('creativeengineering:netherite_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:netherite_plate_block',1), Item.of('vintageimprovements:netherite_sheet',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_nickel',9), Item.of('creativeengineering:nickel_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:nickel_plate_block',1), Item.of('immersiveengineering:plate_nickel',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_silver',9), Item.of('creativeengineering:silver_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:silver_plate_block',1), Item.of('immersiveengineering:plate_silver',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_steel',9), Item.of('creativeengineering:steel_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:steel_plate_block',1), Item.of('immersiveengineering:plate_steel',9), e)

CE.shapeless(Item.of('immersiveengineering:plate_uranium',9), Item.of('creativeengineering:uranium_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:uranium_plate_block',1), Item.of('immersiveengineering:plate_uranium',9), e)

CE.shapeless(Item.of('creativeengineering:zinc_plate',9), Item.of('creativeengineering:zinc_plate_block',1), e)
CE.shapeless(Item.of('creativeengineering:zinc_plate_block',1), Item.of('creativeengineering:zinc_plate',9), e)

//Cast iron arc smelting
CE.ieArcFurnace(Item.of('createdeco:cast_iron_ingot', 1),'minecraft:iron_ingot', e)
CE.ieArcFurnace(Item.of('createdeco:cast_iron_block', 1),'minecraft:iron_block', e)*/































//Crushed Copper ore
//CE.remove({id:'create:crushing/copper_ore'}, e)
/*CE.createCrushing([
    Item.of('create:crushed_raw_copper', 1),
    Item.of('create:crushed_raw_copper', 1).withChance(0.5),
    Item.of('create:experience_nugget', 1).withChance(0.75),
    Item.of('minecraft:cobblestone', 1).withChance(0.12)
], 'minecraft:copper_ore', e)
CE.remove({id:'create:crushing/deepslate_copper_ore'}, e)
CE.createCrushing([
    Item.of('create:crushed_raw_copper', 2),
    Item.of('create:crushed_raw_copper', 1).withChance(0.25),
    Item.of('create:experience_nugget', 1).withChance(0.75),
    Item.of('minecraft:cobblestone', 1).withChance(0.12)
], 'minecraft:deepslate_copper_ore', e)*/
//CE.remove({id:'immersiveengineering:crusher/raw_ore_copper'}, e)
/*CE.ieCrushing(
  Item.of('create:crushed_raw_copper', 1),
  'minecraft:raw_copper', e,
  [{ chance: 0.333, output: Item.of('create:crushed_raw_copper', 1) }])*/
//CE.remove({id:'immersiveengineering:crusher/raw_block_copper'}, e)
/*CE.ieCrushing(
  Item.of('create:crushed_raw_copper', 12),
  'minecraft:raw_copper_block', e)*/
//CE.remove({id:'immersiveengineering:crusher/ore_copper'}, e)
//10% gold grit chance on crushed copper crushing
/*CE.ieCrushing(
  Item.of('create:crushed_raw_copper', 2),
  '#forge:ores/copper', e)*/
//Copper grit
//CE.remove({id:'immersiveengineering:crafting/hammercrushing_copper'}, e)
//CE.remove({id:'immersiveengineering:crafting/raw_hammercrushing_copper'}, e)
/*CE.ieCrushing(
  Item.of('immersiveengineering:dust_copper', 1),
  'create:crushed_raw_copper', e,
  [{ chance: 0.333, output: Item.of('immersiveengineering:dust_copper', 1) }])*/
/*CE.ieCrushing(
	Item.of('immersiveengineering:dust_copper', 12),
  'create_compressed:crushed_copper_pile', e)
  
CE.ieArcFurnace(
  Item.of('minecraft:copper_ingot', 1),
  'create:crushed_raw_copper', e,
  [{chance: 0.5, output: Item.of('minecraft:copper_ingot', 1)}]
)*/

//Remove existing create ore crushing
//CE.bulk('createcrushing' ore crushing readjusts






/*
// ---- Validation ----
function validateItemMaps(maps) {
  var missing = [];
  for (var mapName in maps) {
    var dictmap = maps[mapName];
    for (var key in dictmap) {
      var itemId = dictmap[key]; // renamed from `id` to avoid collision
      if (typeof itemId !== 'string' || itemId.length === 0) {
        missing.push(`${mapName}.${key} -> (empty/invalid value)`);
        continue;
      }
      if (itemId.startsWith('#')) {
        // Tag reference: check it resolves to at least one item
        var stacks = Ingredient.of(itemId).getStacks();
        if (!stacks || stacks.length === 0) {
          missing.push(`${mapName}.${key} -> ${itemId} (tag empty or unknown)`);
        }
      } else {
        // Plain item id
        if (!Item.exists(itemId)) {
          missing.push(`${mapName}.${key} -> ${itemId} (item not registered)`);
        }
      }
    }
  }
  if (missing.length > 0) {
    console.warn(`[ItemValidation] ${missing.length} problem(s) found:`);
    missing.forEach(m => console.warn(`  - ${m}`));
  } else {
    console.log('[ItemValidation] All item/tag references OK.');
  }
}*/







/*      Bulk and Dictionary Recipe Building- Convert Above to Below Eventually        */


//Document Well!!

/*

//Item Mapping dictionaries
let ore = {};
let raw_ore={};
let raw_ore_block={};
let crushed_ore={};
let crushed_ore_block={};
let grit={};
let nugget={};
let ingot={};
let block={};
let plate={};
let plate_block={};
let rod={};
let wire={};
let spring={};
let small_spring={};
let item={};
//Misc item assignments
item.xp_nug = 'create:experience_nugget';
item.cobble = 'minecraft:cobblestone';
item.deepslate_cobble = 'minecraft:deepslate_cobblestone';
item.gilded_blackstone='minecraft:gilded_blackstone';
item.chain = 'minecraft:chain';
item.incomplete = 'kubejs:incomplete_item';
//Copper Assignments
ore.copper = "minecraft:copper_ore";
ore.dscopper = "minecraft:deepslate_copper_ore";
ore.anycopper = "#forge:ores/copper";
raw_ore.copper = 'minecraft:raw_copper'
raw_ore_block.copper = 'minecraft:raw_copper_block'
crushed_ore.copper = 'create:crushed_raw_copper'
crushed_ore_block.copper = 'create_compressed:crushed_copper_pile'
grit.copper = 'immersiveengineering:dust_copper'
nugget.copper = 'immersiveengineering:nugget_copper'
ingot.copper = 'minecraft:copper_ingot'
block.copper = 'minecraft:copper_block'
plate.copper = 'immersiveengineering:plate_copper'
plate_block.copper = 'create_compressed:copper_sheet_block'
rod.copper = 'immersiveposts:stick_copper'
wire.copper = 'immersiveengineering:wire_copper'
spring.copper = 'vintageimprovements:copper_spring'
small_spring.copper = 'vintageimprovements:small_copper_spring'
//Iron Assignments
ore.iron = "minecraft:iron_ore";
ore.dsiron = "minecraft:deepslate_iron_ore";
ore.anyiron = "#forge:ores/iron";
raw_ore.iron = 'minecraft:raw_iron'
raw_ore_block.iron = 'minecraft:raw_iron_block'
crushed_ore.iron = 'create:crushed_raw_iron'
crushed_ore_block.iron = 'create_compressed:crushed_iron_pile'
grit.iron = 'immersiveengineering:dust_iron'
nugget.iron = 'minecraft:iron_nugget'
ingot.iron = 'minecraft:iron_ingot'
block.iron = 'minecraft:iron_block'
plate.iron = 'immersiveengineering:plate_iron'
plate_block.iron = 'create_compressed:iron_sheet_block'
rod.iron = 'immersiveengineering:stick_iron'
wire.iron = 'creativeengineering:iron_wire'
spring.iron = 'vintageimprovements:iron_spring'
small_spring.iron = 'vintageimprovements:small_iron_spring'
//Gold Assignments
ore.gold = "minecraft:gold_ore";
ore.dsgold = "minecraft:deepslate_gold_ore";
ore.anygold = "#forge:ores/gold";
raw_ore.gold = 'minecraft:raw_gold'
raw_ore_block.gold = 'minecraft:raw_gold_block'
crushed_ore.gold = 'create:crushed_raw_gold'
crushed_ore_block.gold = 'create_compressed:crushed_gold_pile'
grit.gold = 'immersiveengineering:dust_gold'
nugget.gold = 'minecraft:gold_nugget'
ingot.gold = 'minecraft:gold_ingot'
block.gold = 'minecraft:gold_block'
plate.gold = 'immersiveengineering:plate_gold'
plate_block.gold = 'create_compressed:gold_sheet_block'
rod.gold = 'immersiveposts:stick_gold'
wire.gold = 'creativeengineering:gold_wire'
spring.gold = 'vintageimprovements:golden_spring'
small_spring.gold = 'vintageimprovements:small_golden_spring'
//Rose Gold Assignments
grit.anygold = 'mca:rose_gold_dust'
nugget.anygold = 'creativeengineering:rose_gold_nugget'
ingot.anygold = 'mca:rose_gold_ingot'
block.anygold = 'mca:rose_gold_block'
rod.anygold = 'creativeengineering:rose_gold_rod'
//Zinc Assignments
ore.zinc = "create:zinc_ore";
ore.dszinc = "create:deepslate_zinc_ore";
ore.anyzinc = "#forge:ores/zinc";
raw_ore.zinc = 'create:raw_zinc'
raw_ore_block.zinc = 'create:raw_zinc_block'
crushed_ore.zinc = 'create:crushed_raw_zinc'
crushed_ore_block.zinc = 'create_compressed:crushed_zinc_pile'
grit.zinc = 'creativeengineering:zinc_grit'
nugget.zinc = 'create:zinc_nugget'
ingot.zinc = 'create:zinc_ingot'
block.zinc = 'create:zinc_block'
plate.zinc = 'creativeengineering:zinc_plate'
plate_block.zinc = 'creativeengineering:zinc_plate_block'
rod.zinc = 'vintageimprovements:zinc_rod'
wire.zinc = 'creativeengineering:zinc_wire'
spring.zinc = 'vintageimprovements:zinc_spring'
small_spring.zinc = 'vintageimprovements:small_zinc_spring'

// Run validation func to ensure all items resolve correctly
validateItemMaps({
  ore: ore,
  raw_ore: raw_ore,
  raw_ore_block: raw_ore_block,
  crushed_ore: crushed_ore,
  crushed_ore_block: crushed_ore_block,
  grit: grit,
  nugget: nugget,
  ingot: ingot,
  block: block,
  plate: plate,
  plate_block: plate_block,
  rod: rod,
  wire: wire,
  spring: spring,
  small_spring: small_spring
});

//Batching IE ore related crushing
//Remove existing grit crushing recipes
CE.bulk('remove', [
	['immersiveengineering:crusher/ore_copper', e],
	['immersiveengineering:crusher/raw_ore_copper', e],
	['immersiveengineering:crusher/raw_block_copper', e],
	['immersiveengineering:crafting/hammercrushing_copper', e],
	['immersiveengineering:crafting/raw_hammercrushing_copper', e],
	['create:crushing/copper_ore', e],
	['create:crushing/deepslate_copper_ore', e],
	['create_compressed:copper_sheet', e],
	
	['immersiveengineering:crusher/ore_iron', e],
	['immersiveengineering:crusher/raw_ore_iron', e],
	['immersiveengineering:crusher/raw_block_iron', e],
	['immersiveengineering:crafting/hammercrushing_iron', e],
	['immersiveengineering:crafting/raw_hammercrushing_iron', e],
	['create:crushing/iron_ore', e],
	['create:crushing/deepslate_iron_ore', e],
	
	['immersiveengineering:crusher/ore_gold', e],
	['immersiveengineering:crusher/raw_ore_gold', e],
	['immersiveengineering:crusher/raw_block_gold', e],
	['immersiveengineering:crafting/hammercrushing_gold', e],
	['immersiveengineering:crafting/raw_hammercrushing_gold', e],
	['create:crushing/gold_ore', e],
	['create:crushing/deepslate_gold_ore', e],
	['immersiveengineering:crusher/ore_gold', e],
	['immersiveengineering:crusher/ingot_gold', e],
	['immersiveengineering:crusher/nether_gold', e],
	['create:crushing/misc/scrap_gilded_blackstone', e],
	['immersiveengineering:arcfurnace/dust_gold', e],
	['create_netherless:gilded_blackstone_from_deploying', e],
	
	['vintageimprovements:coiling/andesite_rod', e],
	['vintageimprovements:coiling/andesite_wire', e],
	['vintageimprovements:coiling/vanadium_rod', e],
	['vintageimprovements:coiling/vanadium_wire', e],
])*/

/*
	
	
	['immersiveengineering:crusher/ore_iron', e],
	['immersiveengineering:crusher/raw_ore_iron', e],
	['immersiveengineering:crusher/raw_block_iron', e],
	['immersiveengineering:crafting/hammercrushing_iron', e],
	['immersiveengineering:crafting/raw_hammercrushing_iron', e],
	['create:crushing/iron_ore', e],
	['create:crushing/deepslate_iron_ore', e],
	
*//*
//Add IE crushing recipes for ore blocks, raw ore, raw ore blocks and grit with correct ratios
CE.bulk('ieCrushing',[
[Item.of(crushed_ore.copper, 2),ore.anycopper, e],
[crushed_ore.copper,raw_ore.copper,e,[{chance: 0.333,output:crushed_ore.copper}],6000],
[Item.of(crushed_ore.copper, 12),raw_ore_block.copper,e,undefined,24000],
[grit.copper,crushed_ore.copper,e,[{chance: 0.333,output:grit.copper}],6000],
[Item.of(grit.copper, 12),crushed_ore_block.copper,e,undefined,24000],

[Item.of(crushed_ore.iron, 2),ore.anyiron, e],
[crushed_ore.iron,raw_ore.iron,e,[{chance: 0.333,output:crushed_ore.iron}],6000],
[Item.of(crushed_ore.iron, 12),raw_ore_block.iron,e,undefined,24000],
[grit.iron,crushed_ore.iron,e,[{chance: 0.333,output:grit.iron}],6000],
[Item.of(grit.iron, 12),crushed_ore_block.iron,e,undefined,24000],

[Item.of(crushed_ore.gold, 2),ore.anygold, e],
[crushed_ore.gold,raw_ore.gold,e,[{chance: 0.333,output:crushed_ore.gold}],6000],
[Item.of(crushed_ore.gold, 12),raw_ore_block.gold,e,undefined,24000],
[grit.gold,crushed_ore.gold,e,[{chance: 0.333,output:grit.gold}],6000],
[Item.of(grit.gold, 12),crushed_ore_block.gold,e,undefined,24000],
[grit.gold,ingot.gold,e,undefined,6000],
[Item.of(nugget.gold, 18),item.gilded_blackstone,e,undefined,6000],
])

//Add create crishing recipes for ore and deepslate ore
CE.bulk('createCrushing',[
[[Item.of(crushed_ore.copper, 1),Item.of(crushed_ore.copper, 1).withChance(0.5),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.cobble, 1).withChance(0.12)],ore.copper,e],
[[Item.of(crushed_ore.copper, 2),Item.of(crushed_ore.copper, 1).withChance(0.25),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.deepslate_cobble, 1).withChance(0.12)],ore.dscopper,e],

[[Item.of(crushed_ore.iron, 1),Item.of(crushed_ore.iron, 1).withChance(0.5),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.cobble, 1).withChance(0.12)],ore.iron,e],
[[Item.of(crushed_ore.iron, 2),Item.of(crushed_ore.iron, 1).withChance(0.25),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.deepslate_cobble, 1).withChance(0.12)],ore.dsiron,e],

[[Item.of(crushed_ore.gold, 1),Item.of(crushed_ore.gold, 1).withChance(0.5),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.cobble, 1).withChance(0.12)],ore.gold,e],
[[Item.of(crushed_ore.gold, 2),Item.of(crushed_ore.gold, 1).withChance(0.25),Item.of(item.xp_nug, 1).withChance(0.75),Item.of(item.deepslate_cobble, 1).withChance(0.12)],ore.dsgold,e],
])

CE.bulk('ieArcFurnace', [
[ingot.copper,crushed_ore.copper,e,[{chance: 0.5,output:ingot.copper}]],
[Item.of(ingot.copper, 13),crushed_ore_block.copper,e,[{chance: 0.5,output:ingot.copper}]],
[ingot.iron,crushed_ore.iron,e,[{chance: 0.5,output:ingot.iron}]],
[Item.of(ingot.iron, 13),crushed_ore_block.iron,e,[{chance: 0.5,output:ingot.iron}]],
[ingot.gold,crushed_ore.gold,e,[{chance: 0.5,output:ingot.gold}]],
[Item.of(ingot.gold, 13),crushed_ore_block.gold,e,[{chance: 0.5,output:ingot.gold}]],
[ingot.gold,grit.gold, e],
])

// Copper plate recipes
CE.bulk('createPressing',[
[plate.copper,ingot.copper,e],
])
CE.bulk('shapeless',[
[Item.of(plate.copper, 9),plate_block.copper,e],
])

// Guilded blackstone expensive craft
CE.createDeploying(item.gilded_blackstone, ['minecraft:blackstone', 'minecraft:gold_block'], e)

//Chain sequence assembly
CE.createSequencedAssembly(Item.of(item.chain, 4), ingot.iron, [
	e.recipes.create.deploying(item.incomplete, [item.incomplete, nugget.iron])
		.id(`createdeploying/chain_step1_nugget`),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, ingot.iron])
		.id(`createdeploying/chain_step2_ingot`),
	e.recipes.create.deploying(item.incomplete, [item.incomplete, nugget.iron])
		.id(`createdeploying/chain_step3_nugget`)
], item.incomplete, 1, e, 'chain_sequenced_assembly')
*/
console.log('The metal event has finished!')
})
