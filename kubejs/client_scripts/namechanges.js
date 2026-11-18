console.log('Rename Started')

ClientEvents.lang('en_us', e => {
	e.renameItem(global.item.gold_hand, 'Gold Hand')
	e.renameItem(global.item.powder_pestle, 'Powder Pestle')
	e.renameItem(global.item.elder_guardian_roll, 'Elder Guardian Roll')
	e.add('block.salt.salt_lamp', 'Large Salt Lamp')  // The Salt's lamp next to Vintage Delight's (salt merge)
	// Flower pot names: Creative Compat (assets/creativecompat/kubejs/names.js).
	// JEI info: Salt Blocks do not grow salt (Creative Common block.SaltBlockTicks)
	e.add('vintagedelight.jei.info.evaporator', 'Place next to water to grow Salt Layers on top. Salt Blocks do not grow salt.')
	// Flint and Terrasteel, Dark Fire: Creative Magic (assets/creativemagic/kubejs/names.js).


	// Food and Delight-member names (Rustic Delight, Canned Goods, Neapolitan, Delightful, Farmer's Delight, PneumaticCraft
	// fried food) are Creative Delight's: kubejs/assets/creativedelight/kubejs/<mod>/client.js
	// Brewin' and Chewin' pizza: the many-topping recipe in cooking/cooking.js
	e.add('block.brewinandchewin.pizza', 'Supreme Pizza')
	e.add('item.brewinandchewin.pizza_slice', 'Supreme Pizza Slice')



	e.renameItem(global.item.plate_block_copper, 'Copper Plate Block')
	e.renameItem(global.item.plate_block_iron, 'Iron Plate Block')
	e.renameItem(global.item.plate_block_gold, 'Gold Plate Block')
	e.renameItem(global.item.plate_block_brass, 'Brass Plate Block')
	e.renameItem(global.item.sturdy_plate_block, 'Sturdy Plate Block')
	e.renameItem(global.item.plate_netherite, 'Netherite Plate')
	e.renameItem(global.item.plate_cast_iron, 'Cast Iron Plate')
	e.renameItem(global.item.block_cast_iron, 'Cast Iron Plating')  // Dustrial Decor's Cast Iron Block keeps the plain name
	e.renameItem(global.item.plate_brass, 'Brass Plate')
	e.renameItem(global.item.sturdy_plate, 'Sturdy Plate')
	e.renameItem(global.item.rose_quartz_plate, 'Rose Quartz Plate')
	e.renameItem(global.item.experience_sheet, 'Experience Plate')
	e.renameItem(global.item.crushed_block_silver, 'Crushed Raw Silver Pile')
	e.renameItem(global.item.crushed_block_uranium, 'Crushed Raw Uranium Pile')

	e.renameItem(global.item.zinc_coin, 'Decorative Zinc Coin')
	e.renameItem(global.item.zinc_coinstack, 'Decorative Zinc Coinstack')
	e.renameItem(global.item.copper_coin, 'Decorative Copper Coin')
	e.renameItem(global.item.copper_coinstack, 'Decorative Copper Coinstack')
	e.renameItem(global.item.iron_coin, 'Decorative Iron Coin')
	e.renameItem(global.item.iron_coinstack, 'Decorative Iron Coinstack')
	e.renameItem(global.item.gold_coin, 'Decorative Gold Coin')
	e.renameItem(global.item.gold_coinstack, 'Decorative Gold Coinstack')
	e.renameItem(global.item.netherite_coin, 'Decorative Netherite Coin')
	e.renameItem(global.item.netherite_coinstack, 'Decorative Netherite Coinstack')
	e.renameItem(global.item.brass_coin, 'Decorative Brass Coin')
	e.renameItem(global.item.brass_coinstack, 'Decorative Brass Coinstack')
	e.renameItem(global.item.cast_iron_coin, 'Decorative Cast Iron Coin')
	e.renameItem(global.item.cast_iron_coinstack, 'Decorative Cast Iron Coinstack')
	e.renameItem(global.item.mca_blueprint, 'Village Blueprint')
	e.renameItem('create_questing:blueprint', 'Quest Blueprint')
	e.renameItem(global.item.modular_accumulator, 'Modular Accumulator')
	e.renameItem(global.item.crt_tube, 'Cathode-Ray Tube')

	e.renameItem(global.item.guardian_beam_capacitor, 'Guardian Charge Cell')
	e.renameItem(global.item.elder_guardian_beam_capacitor, 'Elder Guardian Charge Cell')

	e.renameItem(global.item.cardboard_box, 'Old Cardboard Box')  // cardboard merge: Design and Decor's box
	e.renameItem(global.item.large_chain, 'Huge Chain')
	e.renameItem(global.item.large_gold_chain, 'Huge Gold Chain')

	e.renameItem(global.item.red_electrum_ingot, 'Red Electrum Ingot')
	e.renameItem(global.item.red_alloy_wire, 'Red Electrum Wire')
	e.renameItem(global.item.redstone_mechanism, 'Redstone Mechanism')

	e.renameItem(global.item.compressed_gear, 'Steel Gear')
	e.renameItem(global.item.block_steel, 'Steel Block')
	e.renameItem(global.item.compressed_iron_casing, 'Steel Casing')
	e.renameItem(global.item.compressed_iron_helmet, 'Steel Helmet')
	e.renameItem(global.item.compressed_iron_chestplate, 'Steel Chestplate')
	e.renameItem(global.item.compressed_iron_leggings, 'Steel Leggings')
	e.renameItem(global.item.compressed_iron_boots, 'Steel Boots')
	e.renameItem(global.item.drill_bit_compressed_iron, 'Steel Drill Bit')
	e.renameItem(global.item.creative_compressed_iron_block, 'Creative Steel Block')
	e.renameItem(global.item.pneumaticcraft_capacitor, 'Inductor')
	e.renameItem(global.item.destructive_rod, 'Destructive Rod')
	e.renameItem('computercraft:speaker', 'Machine Speaker')
	e.renameItem(global.item.speaker, 'Brass Speaker')

	e.renameItem(global.item.armor_steel_head, 'Steel Plated Helmet')
	e.renameItem(global.item.armor_steel_chest, 'Steel Plated Chestplate')
	e.renameItem(global.item.armor_steel_legs, 'Steel Plated Leggings')
	e.renameItem(global.item.armor_steel_feet, 'Steel Plated Boots')

	e.renameItem(global.item.raw_copper_bars, 'Raw Copper Bars')
	e.renameItem(global.item.ornate_copper_bars, 'Ornate Copper Bars')
	e.renameItem(global.item.ornate_andesite_bars, 'Ornate Andesite Bars')
	e.renameItem(global.item.ornate_brass_bars, 'Ornate Brass Bars')
	e.renameItem(global.item.ornate_gold_bars, 'Ornate Gold Bars')

	e.renameItem(global.item.polished_limestone, 'Polished Gabbro')
	e.renameItem(global.item.polished_limestone_slab, 'Polished Gabbro Slab')
	e.renameItem(global.item.polished_limestone_stairs, 'Polished Gabbro Stairs')
	e.renameItem(global.item.polished_limestone_vertical_slab, 'Polished Gabbro Vertical Slab')
	e.renameItem(global.item.quark_limestone, 'Gabbro')
	e.renameItem(global.item.limestone_bricks, 'Gabbro Bricks')
	e.renameItem(global.item.limestone_bricks_slab, 'Gabbro Bricks Slab')
	e.renameItem(global.item.limestone_bricks_stairs, 'Gabbro Bricks Stairs')
	e.renameItem(global.item.limestone_bricks_vertical_slab, 'Gabbro Bricks Vertical Slab')
	e.renameItem(global.item.limestone_bricks_wall, 'Gabbro Bricks Wall')
	e.renameItem(global.item.quark_limestone_pillar, 'Gabbro Pillar')
	e.renameItem(global.item.limestone_slab, 'Gabbro Slab')
	e.renameItem(global.item.limestone_stairs, 'Gabbro Stairs')
	e.renameItem(global.item.limestone_vertical_slab, 'Gabbro Vertical Slab')
	e.renameItem(global.item.limestone_wall, 'Gabbro Wall')

	// Quark's permafrost is Froststone (crafted only); Immersive Weathering's is the world's permafrost (TODO biomes #5)
	;[
		['froststone', 'Froststone'], ['froststone_slab', 'Froststone Slab'], ['froststone_stairs', 'Froststone Stairs'],
		['froststone_vertical_slab', 'Froststone Vertical Slab'], ['froststone_wall', 'Froststone Wall'],
		['froststone_bricks', 'Froststone Bricks'], ['froststone_bricks_slab', 'Froststone Brick Slab'],
		['froststone_bricks_stairs', 'Froststone Brick Stairs'], ['froststone_bricks_vertical_slab', 'Froststone Brick Vertical Slab'],
		['froststone_bricks_wall', 'Froststone Brick Wall'],
	].forEach(([key, name]) => e.renameItem(global.item[key], name))

	e.renameItem(global.item.fertile_soil, 'Fertile Soil')

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

	e.renameItem(global.item.black_chocolate_bucket, 'Dark Chocolate Bucket')
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
	// Stuff, dish renames) and the lunchbox tier names: Creative Delight. Create D&D asphalt is painted Immersive
	// Petroleum asphalt (server_scripts/engineering/dreams_desires.js)
	;['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple',
		'blue', 'brown', 'green', 'red', 'black'].forEach(c => {
		var name = c.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
		e.add('create_dd', 'block.create_dd.' + c + '_asphalt_block', name + ' Painted Asphalt')
	})
})

console.log('Rename Finished')
