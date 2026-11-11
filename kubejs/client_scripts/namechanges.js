console.log('Rename Started')

ClientEvents.lang('en_us', e => {
	e.renameItem('create:brass_hand', 'Gold Hand')
	e.renameItem('oldguns:mortar_and_pestle', 'Powder Pestle')
	e.renameItem('oceansdelight:elder_guardian_roll', 'Elder Guardian Roll')
	e.add('block.salt.salt_lamp', 'Large Salt Lamp')  // The Salt's lamp next to Vintage Delight's (salt merge)
	// Flower pot names: Creative Compat (assets/creativecompat/kubejs/names.js).
	// JEI info: Salt Blocks no longer grow salt since the salt merge (patcherMod block.SaltBlockTicks)
	e.add('vintagedelight.jei.info.evaporator', 'Place next to water to grow Salt Layers on top. Salt Blocks do not grow salt.')
	e.add('item.creativeengineering.precision_sawblade', 'Precision Sawblade')
  e.add('item.creativeengineering.creosote_soaked_sawdust', 'Creosote Soaked Sawdust')
  e.add('item.creativeengineering.zinc_grit', 'Zinc Grit')
  e.add('item.creativeengineering.brass_grit', 'Brass Grit')
  e.add('item.creativeengineering.cast_iron_grit', 'Cast Iron Grit')
  e.add('item.creativeengineering.rose_gold_nugget', 'Rose Gold Nugget')
  e.add('item.creativeengineering.rose_gold_rod', 'Rose Gold Rod')
  e.add('item.creativeengineering.silver_wire', 'Silver Wire')
  e.add('item.creativeengineering.nickel_wire', 'Nickel Wire')
  e.add('item.creativeengineering.iron_wire', 'Iron Wire')
  e.add('item.creativeengineering.zinc_wire', 'Zinc Wire')
  e.add('item.creativeengineering.brass_wire', 'Brass Wire')
  e.add('item.creativeengineering.gold_wire', 'Gold Wire')
  e.add('item.creativeengineering.spool', 'Spool')
  e.add('item.creativeengineering.zinc_plate', 'Zinc Plate')
  e.add('item.creativeequipment.arrow_fletching', 'Arrow Fletching')
  // Flint and Terrasteel, Dark Fire: Creative Magic (assets/creativemagic/kubejs/names.js).
  e.add('block.creativeengineering.aluminum_plate_block', 'Aluminum Plate Block')
  e.add('block.creativeengineering.cast_iron_plate_block', 'Cast Iron Plate Block')
  e.add('block.creativeengineering.constantan_plate_block', 'Constantan Plate Block')
  e.add('block.creativeengineering.electrum_plate_block', 'Electrum Plate Block')
  e.add('block.creativeengineering.lead_plate_block', 'Lead Plate Block')
  e.add('block.creativeengineering.netherite_plate_block', 'Netherite Plate Block')
  e.add('block.creativeengineering.nickel_plate_block', 'Nickel Plate Block')
  e.add('block.creativeengineering.silver_plate_block', 'Silver Plate Block')
  e.add('block.creativeengineering.steel_plate_block', 'Steel Plate Block')
  e.add('block.creativeengineering.uranium_plate_block', 'Uranium Plate Block')
  e.add('block.creativeengineering.zinc_plate_block', 'Zinc Plate Block')
  e.add('block.creativeengineering.prepared_flesh_block', 'Prepared Flesh Block')
  
  e.add('block.creativeengineering.copper_dust_pile', 'Copper Grit Pile')
  e.add('block.creativeengineering.iron_dust_pile', 'Iron Grit Pile')
	e.add('block.creativeengineering.gold_dust_pile', 'Gold Grit Pile')	
	e.add('block.creativeengineering.zinc_dust_pile', 'Zinc Grit Pile')	
	e.add('block.creativeengineering.aluminum_dust_pile', 'Aluminum Grit Pile')	
	e.add('block.creativeengineering.lead_dust_pile', 'Lead Grit Pile')	
	e.add('block.creativeengineering.silver_dust_pile', 'Silver Grit Pile')	
	e.add('block.creativeengineering.nickel_dust_pile', 'Nickel Grit Pile')	
	e.add('block.creativeengineering.uranium_dust_pile', 'Uranium Grit Pile')	
	e.add('block.creativeengineering.diamond_dust_pile', 'Diamond Grit Pile')
	e.add('block.creativeengineering.rose_gold_dust_pile', 'Rose Gold Grit Pile')
	e.add('block.creativeengineering.cast_iron_dust_pile', 'Cast Iron Grit Pile')
	e.add('block.creativeengineering.electrum_dust_pile', 'Electrum Grit Pile')
	e.add('block.creativeengineering.steel_dust_pile', 'Steel Grit Pile')
	e.add('block.creativeengineering.constantan_dust_pile', 'Constantan Grit Pile')
	e.add('block.creativeengineering.brass_dust_pile', 'Brass Grit Pile')

  // Food and Delight-member names (Rustic Delight, Canned Goods, Neapolitan, Delightful, Farmer's Delight, PneumaticCraft
  // fried food) moved to Creative Delight 2026-10-27: kubejs/assets/creativedelight/kubejs/<mod>/client.js
  // Brewin' and Chewin' pizza: the many-topping recipe in cooking/cooking.js
  e.add('block.brewinandchewin.pizza', 'Supreme Pizza')
  e.add('item.brewinandchewin.pizza_slice', 'Supreme Pizza Slice')

  e.renameItem('creativeengineering:transmitter', 'Transmitter')
  

	e.renameItem('create_compressed:copper_sheet_block', 'Copper Plate Block')
  e.renameItem('create_compressed:iron_sheet_block', 'Iron Plate Block')
  e.renameItem('create_compressed:gold_sheet_block', 'Gold Plate Block')
  e.renameItem('create_compressed:brass_sheet_block', 'Brass Plate Block')
  e.renameItem('create_compressed:sturdy_sheet_block', 'Sturdy Plate Block')
  e.renameItem('vintageimprovements:netherite_sheet', 'Netherite Plate')
  e.renameItem('createdeco:cast_iron_sheet', 'Cast Iron Plate')
  e.renameItem('createdeco:cast_iron_block', 'Cast Iron Plating')  // Dustrial Decor's Cast Iron Block keeps the plain name
  e.renameItem('create:brass_sheet', 'Brass Plate')
  e.renameItem('create:sturdy_sheet', 'Sturdy Plate')
  e.renameItem('create_things_and_misc:rose_quartz_sheet', 'Rose Quartz Plate')
  e.renameItem('create_things_and_misc:experience_sheet', 'Experience Plate')
  e.renameItem('create_compressed:crushed_silver_pile', 'Crushed Raw Silver Pile')
  e.renameItem('create_compressed:crushed_uranium_pile', 'Crushed Raw Uranium Pile')
  
  e.renameItem('createdeco:zinc_coin', 'Decorative Zinc Coin')
  e.renameItem('createdeco:zinc_coinstack', 'Decorative Zinc Coinstack')
  e.renameItem('createdeco:copper_coin', 'Decorative Copper Coin')
  e.renameItem('createdeco:copper_coinstack', 'Decorative Copper Coinstack')
  e.renameItem('createdeco:iron_coin', 'Decorative Iron Coin')
  e.renameItem('createdeco:iron_coinstack', 'Decorative Iron Coinstack')
  e.renameItem('createdeco:gold_coin', 'Decorative Gold Coin')
  e.renameItem('createdeco:gold_coinstack', 'Decorative Gold Coinstack')
  e.renameItem('createdeco:netherite_coin', 'Decorative Netherite Coin')
  e.renameItem('createdeco:netherite_coinstack', 'Decorative Netherite Coinstack')
  e.renameItem('createdeco:brass_coin', 'Decorative Brass Coin')
  e.renameItem('createdeco:brass_coinstack', 'Decorative Brass Coinstack')
  e.renameItem('createdeco:cast_iron_coin', 'Decorative Cast Iron Coin')
  e.renameItem('createdeco:cast_iron_coinstack', 'Decorative Cast Iron Coinstack')
  e.renameItem('mca:blueprint', 'Village Blueprint')
  e.renameItem('create_questing:blueprint', 'Quest Blueprint')
  e.renameItem('createaddition:modular_accumulator', 'Modular Accumulator')
  e.renameItem('controlengineering:crt_tube', 'Cathode-Ray Tube')
  
  e.renameItem('creategbd:guardian_beam_capacitor', 'Guardian Charge Cell')
  e.renameItem('creategbd:elder_guardian_beam_capacitor', 'Elder Guardian Charge Cell')
  
  e.renameItem('design_decor:cardboard_box', 'Old Cardboard Box')  // cardboard merge: Design and Decor's box
  e.renameItem('dustrial_decor:large_chain', 'Huge Chain')
  e.renameItem('dustrial_decor:large_gold_chain', 'Huge Gold Chain')
  
  e.renameItem('projectred_core:red_ingot', 'Red Electrum Ingot')
  e.renameItem('projectred_transmission:red_alloy_wire', 'Red Electrum Wire')
  e.renameItem('vintageimprovements:redstone_module', 'Redstone Mechanism')
  
  e.renameItem('pneumaticcraft:compressed_iron_gear', 'Steel Gear')
  e.renameItem('pneumaticcraft:compressed_iron_block', 'Steel Block')
  e.renameItem('compressedcreativity:compressed_iron_casing', 'Steel Casing')
  e.renameItem('pneumaticcraft:compressed_iron_helmet', 'Steel Helmet')
  e.renameItem('pneumaticcraft:compressed_iron_chestplate', 'Steel Chestplate')
  e.renameItem('pneumaticcraft:compressed_iron_leggings', 'Steel Leggings')
  e.renameItem('pneumaticcraft:compressed_iron_boots', 'Steel Boots')
  e.renameItem('pneumaticcraft:drill_bit_compressed_iron', 'Steel Drill Bit')
  e.renameItem('pneumaticcraft:creative_compressed_iron_block', 'Creative Steel Block')
  e.renameItem('pneumaticcraft:capacitor', 'Inductor')
 	e.renameItem('quark:iron_rod', 'Destructive Rod')
	e.renameItem('computercraft:speaker', 'Machine Speaker')
	e.renameItem('supplementaries:speaker_block', 'Brass Speaker')
  
  e.renameItem('immersiveengineering:armor_steel_head', 'Steel Plated Helmet')
  e.renameItem('immersiveengineering:armor_steel_chest', 'Steel Plated Chestplate')
  e.renameItem('immersiveengineering:armor_steel_legs', 'Steel Plated Leggings')
  e.renameItem('immersiveengineering:armor_steel_feet', 'Steel Plated Boots')
  
  e.renameItem('everythingcopper:copper_bars', 'Raw Copper Bars')
  e.renameItem('create:copper_bars', 'Ornate Copper Bars')
  e.renameItem('create:andesite_bars', 'Ornate Andesite Bars')
  e.renameItem('create:brass_bars', 'Ornate Brass Bars')
  e.renameItem('quark:gold_bars', 'Ornate Gold Bars')
  
  e.renameItem('quark:polished_limestone', 'Polished Gabbro')
	e.renameItem('quark:polished_limestone_slab', 'Polished Gabbro Slab')
	e.renameItem('quark:polished_limestone_stairs', 'Polished Gabbro Stairs')
	e.renameItem('quark:polished_limestone_vertical_slab', 'Polished Gabbro Vertical Slab')
	e.renameItem('quark:limestone', 'Gabbro')
	e.renameItem('quark:limestone_bricks', 'Gabbro Bricks')
	e.renameItem('quark:limestone_bricks_slab', 'Gabbro Bricks Slab')
	e.renameItem('quark:limestone_bricks_stairs', 'Gabbro Bricks Stairs')
	e.renameItem('quark:limestone_bricks_vertical_slab', 'Gabbro Bricks Vertical Slab')
	e.renameItem('quark:limestone_bricks_wall', 'Gabbro Bricks Wall')
	e.renameItem('quark:limestone_pillar', 'Gabbro Pillar')
	e.renameItem('quark:limestone_slab', 'Gabbro Slab')
	e.renameItem('quark:limestone_stairs', 'Gabbro Stairs')
	e.renameItem('quark:limestone_vertical_slab', 'Gabbro Vertical Slab')
	e.renameItem('quark:limestone_wall', 'Gabbro Wall')

	// Quark's permafrost is Froststone (crafted only); Immersive Weathering's is the world's permafrost (TODO biomes #5)
	;[
		['froststone', 'Froststone'], ['froststone_slab', 'Froststone Slab'], ['froststone_stairs', 'Froststone Stairs'],
		['froststone_vertical_slab', 'Froststone Vertical Slab'], ['froststone_wall', 'Froststone Wall'],
		['froststone_bricks', 'Froststone Bricks'], ['froststone_bricks_slab', 'Froststone Brick Slab'],
		['froststone_bricks_stairs', 'Froststone Brick Stairs'], ['froststone_bricks_vertical_slab', 'Froststone Brick Vertical Slab'],
		['froststone_bricks_wall', 'Froststone Brick Wall'],
	].forEach(([key, name]) => e.renameItem(global.item[key], name))
	
	e.renameItem('immersive_weathering:mulch_block', 'Fertile Soil')

  // Iron Shield (the vanilla shield as the iron tier): Creative Equipment (assets/creativeequipment/kubejs/names.js).
  
  e.add('entity.minecraft.villager.botanist', 'Botanist')
  e.add('entity.minecraft.villager.animal_tamer', 'Animal Tamer')
  
  e.add('entity.minecraft.villager.immersiveengineering:engineer', 'Engineer')
  e.add('entity.minecraft.villager.immersiveengineering:machinist', 'Machinist')
  e.add('entity.minecraft.villager.immersiveengineering:electrician', 'Electrician')
  e.add('entity.minecraft.villager.immersiveengineering:outfitter', 'Outfitter')
  e.add('entity.minecraft.villager.immersiveengineering:gunsmith', 'Gunsmith') 
  
  e.add('desc.immersiveengineering.info.blueprint.redstonecomponents', 'Redstone Components')
  e.add('desc.immersiveengineering.info.blueprint.electricalcomponents', 'Electrical Components')
  e.add('desc.immersiveengineering.info.blueprint.railtransport', 'Rail Transportation')
  e.add('desc.immersiveengineering.info.blueprint.archery', 'Archery')
  e.add('desc.immersiveengineering.info.blueprint.precision_sawblades', 'Precision Sawblades')
  e.add('desc.immersiveengineering.info.blueprint.disassembly_wire_coils', 'Disassembly: Wire Coils')
  e.add('desc.immersiveengineering.info.blueprint.disassembly_coil_blocks', 'Disassembly: Coil Blocks')
  e.add('desc.immersiveengineering.info.blueprint.disassembly_components', 'Disassembly: Components')
  e.add('desc.immersiveengineering.info.blueprint.disassembly_springs', 'Disassembly: Springs')
  e.add('desc.immersiveengineering.info.blueprint.disassembly_small_springs', 'Disassembly: Small Springs')
  e.add('desc.immersiveengineering.info.blueprint.controlengineering:scope_components', 'Oscilloscope Components')
  
  e.add('entity.minecraft.villager.mechanical_engineer', 'Mechanical Engineer')
  e.add('entity.minecraft.villager.hydraulic_engineer', 'Hydraulic Engineer')
  e.add('entity.minecraft.villager.train_mechanic', 'Train Mechanic')
  e.add('entity.minecraft.villager.miner', 'Miner')
  
  e.add('entity.minecraft.villager.etched:bard', 'Bard')
  e.add('entity.minecraft.villager.pneumaticcraft:mechanic', 'Pneumatic Mechanic')
  e.add('entity.minecraft.villager.advancedperipherals:computer_scientist', 'Computer Scientist')
  e.add('entity.minecraft.villager.banker', 'Banker')
  e.add('entity.minecraft.villager.cashier', 'Cashier')
  
  e.add('block.chalk.red_chalk_mark', 'Red Chalk Mark')
  e.add('block.chalk.orange_chalk_mark', 'Orange Chalk Mark')
  e.add('block.chalk.yellow_chalk_mark', 'Yellow Chalk Mark')
  e.add('block.chalk.green_chalk_mark', 'Green Chalk Mark')
  e.add('block.chalk.lime_chalk_mark', 'Lime Chalk Mark')
  e.add('block.chalk.blue_chalk_mark', 'Blue Chalk Mark')
  e.add('block.chalk.light_blue_chalk_mark', 'Light Blue Chalk Mark')
  e.add('block.chalk.cyan_chalk_mark', 'Cyan Chalk Mark')
  e.add('block.chalk.purple_chalk_mark', 'Purple Chalk Mark')
  e.add('block.chalk.magenta_chalk_mark', 'Magenta Chalk Mark')
  e.add('block.chalk.pink_chalk_mark', 'Pink Chalk Mark')
  e.add('block.chalk.white_chalk_mark', 'White Chalk Mark')
  e.add('block.chalk.light_gray_chalk_mark', 'Light Gray Chalk Mark')
  e.add('block.chalk.gray_chalk_mark', 'Gray Chalk Mark')
  e.add('block.chalk.black_chalk_mark', 'Black Chalk Mark')
  e.add('block.chalk.brown_chalk_mark', 'Brown Chalk Mark')
  
  e.renameItem('create_confectionery:black_chocolate_bucket', 'Dark Chocolate Bucket')
  e.add('fluid_type.create_confectionery.black_chocolate', 'Molten Dark Chocolate')
  e.add('fluid_type.create_confectionery.white_chocolate', 'Molten White Chocolate')
  e.add('fluid_type.create_confectionery.ruby_chocolate', 'Molten Ruby Chocolate')
  e.add('fluid_type.create_confectionery.hot_chocolate', 'Hot Chocolate')
  e.add('fluid_type.create_confectionery.caramel', 'Molten Caramel')
  e.add('fluid_type.create_things_and_misc.diluted_bonemeal', 'Diluted Bonemeal')
  e.add('fluid_type.create_things_and_misc.slime', 'Liquid Slime')

  // Vinery names (wood, cider, the Winemaker profession): Creative Brewing (assets/creativebrewing/kubejs/vinery/names.js)
  // Dave's rocky dirt is Pebble Dirt; Twigs' keeps "Rocky Dirt" (item_merges.md 2.5)
  e.renameItem(global.item.rocky_dirt, 'Pebble Dirt')
  e.renameItem(global.item.rocky_dirt_slab, 'Pebble Dirt Slab')
  // Upgrade Aquatic's river tree is the Mulberry tree: oak trunk, its wood merged into oak (recipes_wood.md 2.6). The
  // potted sapling is a block-only key, named in name_rules.json.
  e.renameItem(global.item.river_leaves, 'Mulberry Leaves')
  e.renameItem(global.item.river_sapling, 'Mulberry Sapling')
  e.renameItem(global.item.river_leaf_pile, 'Pile of Mulberry Leaves')
  e.renameItem(global.item.river_leaf_carpet, 'Mulberry Leaf Carpet')
  e.renameItem(global.item.river_hedge, 'Mulberry Hedge')
  e.renameItem(global.item.upgrade_aquatic_river_leaf_pile, 'Mulberry Leaf Pile')
  // Immersive Weathering's nulch is the Nether counterpart of Fertile Soil (crops.md 2.4); the ghost pepper seed
  // tooltip names it too
  e.renameItem(global.item.nulch, 'Fertile Soul Soil')
  e.add('item.vintagedelight.ghost_pepper.planting_soil', 'Requires a soul soil and the heat of the nether to grow')
  // Delight dish, crop and fluid names (Oceanic, Fruits, Veggies, Vintage, Ender's, Rustic, Delightful, The Dirty
  // Stuff, dish renames) and the lunchbox tier names: Creative Delight
  // (assets/creativedelight/kubejs/creativedelight/names.js, moved 2026-11-01).
  // Create D&D asphalt is painted Immersive Petroleum asphalt (server_scripts/engineering/dreams_desires.js)
  ;['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple',
    'blue', 'brown', 'green', 'red', 'black'].forEach(c => {
    var name = c.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
    e.add('create_dd', 'block.create_dd.' + c + '_asphalt_block', name + ' Painted Asphalt')
  })
})

console.log('Rename Finished')
