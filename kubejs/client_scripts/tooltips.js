// The pack's own tooltip rows. The helpers and palette (global.tooltips) live in Creative Common's client scripts
// (assets/creativecommon/kubejs/_lib/tooltips.js); they are bound inside the event, since that pack may load after this one.

ItemEvents.tooltip(e => {
	var tt = global.tooltips
	if (!tt) { console.error('[tooltips] global.tooltips missing: Creative Common\'s client scripts did not load'); return }
	var addTooltips = tt.addTooltips, addTooltipPattern = tt.addTooltipPattern, removeTooltips = tt.removeTooltips,
		removeTooltipKey = tt.removeTooltipKey, buildTooltipLine = tt.buildTooltipLine, col = tt.col
// ############################## Global ##############################
var i = global.item;

	// col: the palette, from global.tooltips (Creative Common)

	//Test item
	addTooltips(e,
			[i.white_wool,['Normal', col.norm],['Uncommon', col.uncm],['Rare', col.rare],['Epic', col.epic],['Legendary', col.leg],['Good', col.good],['Warning', col.warn],['Engineering', col.eng]],
			[i.white_concrete,
	['Black', col.black],
	['Dark Blue', col.dblue],
	['Dark Green', col.dgreen],
	['Dark Aqua', col.daqua],
	['Dark Red', col.dred],
	['Dark Purple', col.dpurple],
	['Gold', col.gold],
	['Gray', col.gray],
	['Dark Gray', col.dgray],
	['Blue', col.blue],
	['Green', col.green],
	['Aqua', col.aqua],
	['Red', col.red],
	['Light Purple', col.lpurple],
	['Yellow', col.yellow],
	['White', col.white]]
	);
		
// ############################## Material Information ##############################

	addTooltips(e,
		[i.ingot_copper,
			['Density: 8,960 kg/m³', col.dark_gray],
			['Weight: 19,753 lb/m³', col.dark_gray],
			['Melting Point: 1085°C / 1985°F', col.dark_gray],
			['Thermal Conductivity: 401 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 59.6 MS/m', col.dark_gray]],

		[i.ingot_iron,
			['Density: 7,874 kg/m³', col.dark_gray],
			['Weight: 17,359 lb/m³', col.dark_gray],
			['Melting Point: 1538°C / 2800°F', col.dark_gray],
			['Thermal Conductivity: 80.4 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 10.3 MS/m', col.dark_gray]],

		[i.ingot_gold,
			['Density: 19,300 kg/m³', col.dark_gray],
			['Weight: 42,549 lb/m³', col.dark_gray],
			['Melting Point: 1064°C / 1947°F', col.dark_gray],
			['Thermal Conductivity: 318 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 45.2 MS/m', col.dark_gray]],

		[i.ingot_zinc,
			['Density: 7,140 kg/m³', col.dark_gray],
			['Weight: 15,741 lb/m³', col.dark_gray],
			['Melting Point: 420°C / 788°F', col.dark_gray],
			['Thermal Conductivity: 116 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 16.9 MS/m', col.dark_gray]],

		[i.ingot_aluminum,
			['Density: 2,700 kg/m³', col.dark_gray],
			['Weight: 5,953 lb/m³', col.dark_gray],
			['Melting Point: 660°C / 1220°F', col.dark_gray],
			['Thermal Conductivity: 237 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 37.7 MS/m', col.dark_gray]],

		[i.ingot_lead,
			['Density: 11,340 kg/m³', col.dark_gray],
			['Weight: 25,000 lb/m³', col.dark_gray],
			['Melting Point: 328°C / 622°F', col.dark_gray],
			['Thermal Conductivity: 35.3 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 4.5 MS/m', col.dark_gray]],

		[i.ingot_nickel,
			['Density: 8,908 kg/m³', col.dark_gray],
			['Weight: 19,639 lb/m³', col.dark_gray],
			['Melting Point: 1455°C / 2651°F', col.dark_gray],
			['Thermal Conductivity: 90.9 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 14.3 MS/m', col.dark_gray]],

		[i.ingot_silver,
			['Density: 10,490 kg/m³', col.dark_gray],
			['Weight: 23,127 lb/m³', col.dark_gray],
			['Melting Point: 962°C / 1764°F', col.dark_gray],
			['Thermal Conductivity: 429 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 63.0 MS/m', col.dark_gray]],

		[i.ingot_uranium,
			['Density: 19,050 kg/m³', col.dark_gray],
			['Weight: 41,998 lb/m³', col.dark_gray],
			['Melting Point: 1132°C / 2070°F', col.dark_gray],
			['Thermal Conductivity: 27.5 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 3.6 MS/m', col.dark_gray]],

		[i.ingot_cast_iron,
			['Density: 7,200 kg/m³', col.dark_gray],
			['Weight: 15,873 lb/m³', col.dark_gray],
			['Melting Point: 1200°C / 2192°F', col.dark_gray],
			['Thermal Conductivity: 55 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 1.4 MS/m', col.dark_gray]],

		[i.ingot_steel,
			['Density: 7,850 kg/m³', col.dark_gray],
			['Weight: 17,306 lb/m³', col.dark_gray],
			['Melting Point: 1450°C / 2642°F', col.dark_gray],
			['Thermal Conductivity: 50 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 7.0 MS/m', col.dark_gray]],

		[i.ingot_brass,
			['Density: 8,530 kg/m³', col.dark_gray],
			['Weight: 18,805 lb/m³', col.dark_gray],
			['Melting Point: 930°C / 1706°F', col.dark_gray],
			['Thermal Conductivity: 120 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 15.9 MS/m', col.dark_gray]],

		[i.ingot_constantan,
			['Density: 8,900 kg/m³', col.dark_gray],
			['Weight: 19,621 lb/m³', col.dark_gray],
			['Melting Point: 1270°C / 2318°F', col.dark_gray],
			['Thermal Conductivity: 21.5 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 2.04 MS/m', col.dark_gray]],

		[i.ingot_rose_gold,
			['Density: 15,200 kg/m³', col.dark_gray],
			['Weight: 33,510 lb/m³', col.dark_gray],
			['Melting Point: 900°C / 1652°F', col.dark_gray],
			['Thermal Conductivity: 170 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 12 MS/m', col.dark_gray]],

		[i.ingot_electrum,
			['Density: 14,900 kg/m³', col.dark_gray],
			['Weight: 32,849 lb/m³', col.dark_gray],
			['Melting Point: 1020°C / 1868°F', col.dark_gray],
			['Thermal Conductivity: 175 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 11 MS/m', col.dark_gray]],

		[i.ingot_netherite,
			['Density: 23,000 kg/m³', col.dark_gray],
			['Weight: 50,706 lb/m³', col.dark_gray],
			['Melting Point: 3600°C / 6512°F', col.dark_gray],
			['Thermal Conductivity: 6 W/(m·K)', col.dark_gray],
			['Electrical Conductivity: 0.4 MS/m', col.dark_gray]],

		[i.wire_copper,
			['Electrical Conductivity: 59.6 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0168 Ω·mm²/m', col.dark_gray]],
		[i.wire_iron,
			['Electrical Conductivity: 10.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0971 Ω·mm²/m', col.dark_gray]],
		[i.wire_gold,
			['Electrical Conductivity: 45.2 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0221 Ω·mm²/m', col.dark_gray]],
		[i.wire_zinc,
			['Electrical Conductivity: 16.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0592 Ω·mm²/m', col.dark_gray]],
		[i.wire_aluminum,
			['Electrical Conductivity: 37.7 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0265 Ω·mm²/m', col.dark_gray]],
		[i.wire_lead,
			['Electrical Conductivity: 4.5 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.2222 Ω·mm²/m', col.dark_gray]],
		[i.wire_nickel,
			['Electrical Conductivity: 14.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0699 Ω·mm²/m', col.dark_gray]],
		[i.wire_silver,
			['Electrical Conductivity: 63.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0159 Ω·mm²/m', col.dark_gray]],
		[i.wire_steel,
			['Electrical Conductivity: 7.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.1429 Ω·mm²/m', col.dark_gray]],
		[i.wire_brass,
			['Electrical Conductivity: 15.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0629 Ω·mm²/m', col.dark_gray]],
		[i.wire_constantan,
			['Electrical Conductivity: 2.04 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.4902 Ω·mm²/m', col.dark_gray]],
		[i.wire_electrum,
			['Electrical Conductivity: 11 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0909 Ω·mm²/m', col.dark_gray]],

		[i.wire_coil_copper,
			['Electrical Conductivity: 59.6 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0168 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_iron,
			['Electrical Conductivity: 10.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0971 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_gold,
			['Electrical Conductivity: 45.2 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0221 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_zinc,
			['Electrical Conductivity: 16.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0592 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_aluminum,
			['Electrical Conductivity: 37.7 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0265 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_lead,
			['Electrical Conductivity: 4.5 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.2222 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_nickel,
			['Electrical Conductivity: 14.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0699 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_silver,
			['Electrical Conductivity: 63.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0159 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_steel,
			['Electrical Conductivity: 7.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.1429 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_brass,
			['Electrical Conductivity: 15.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0629 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_constantan,
			['Electrical Conductivity: 2.04 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.4902 Ω·mm²/m', col.dark_gray]],
		[i.wire_coil_electrum,
			['Electrical Conductivity: 11 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0909 Ω·mm²/m', col.dark_gray]],

		[i.wirecoil_hv,
			['Electrical Conductivity: 22.35 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0447 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_hv,
			['Electrical Conductivity: 22.35 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0447 Ω·mm²/m', col.dark_gray]],

		[i.coil_block_copper,
			['Electrical Conductivity: 59.6 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0168 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_iron,
			['Electrical Conductivity: 10.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0971 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_gold,
			['Electrical Conductivity: 45.2 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0221 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_zinc,
			['Electrical Conductivity: 16.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0592 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_aluminum,
			['Electrical Conductivity: 37.7 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0265 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_lead,
			['Electrical Conductivity: 4.5 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.2222 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_nickel,
			['Electrical Conductivity: 14.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0699 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_silver,
			['Electrical Conductivity: 63.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0159 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_steel,
			['Electrical Conductivity: 7.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.1429 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_brass,
			['Electrical Conductivity: 15.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0629 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_constantan,
			['Electrical Conductivity: 2.04 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.4902 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_electrum,
			['Electrical Conductivity: 11 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0909 Ω·mm²/m', col.dark_gray]],
		[i.coil_block_hv,
			['Electrical Conductivity: 22.35 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0447 Ω·mm²/m', col.dark_gray]],
	);

// ############################## Tools ##############################

	//Basic tools
	addTooltips(e,
		[i.sup_wrench,['Rotates blocks around the face clicked', col.uncm],['Crouch-click to rotate the opposite direction', col.norm]],
		[i.trowel,['Places a random block from hotbar', col.uncm]],
		[i.abacus,['Measures block distances', col.uncm]],
		[i.slingshot, ['Place blocks from a distance', col.uncm], ['Stasis enchantment removes gravity and highlights target', col.norm]]
	);

	// Stepstools: Creative Equipment (assets/creativeequipment/kubejs/tooltips.js).
				
// ############################## Crafting Materials ##############################	

	//Mineral chain
	// Netherite
	addTooltips(e,
			[i.netherite_shard, ['Not craftable by hand', col.warn]]
	);
	
	//Basic materials
	addTooltips(e,
		[i.gunpowder, ['Usable as flammable fuse, speeds up TNT explosion', col.norm]],
		[i.book, ['Four placed books provide the same enchantment power as a bookshelf', col.norm]],
		[i.enchanted_book, ['Three placed enchanted books provide the same enchantment power as a bookshelf', col.uncm]],
		[i.stick, ['Placeable', col.norm, {italic: true}]],
		[i.blaze_rod, ['Placeable', col.norm, {italic: true}]],
		[i.shulker_shell, ['Can be worn, hold in both hands and shift + right click to pack a chest', col.norm]]
	);

// ############################## Blocks ##############################	
    
    addTooltips(e,
		[i.hay_bale, ['Reduces fall damage', col.good]],
		[i.crying_obsidian, ['Ignite to light permanent soul fire', col.rare]],
		[i.chain, ["Ends of chain stick to blocks w/o glue, and sides won't stick to slime blocks", col.uncm]]
	);
	// Botany Pots growth is 8x slower for crops, 2x for flowers (server_scripts/other/botany_pots.js)
	if (Platform.isLoaded('botanypots')) addTooltipPattern(e, 'botanypots:*botany_pot', ['Useful for flowers, not great for crops', col.norm]);
		
// ############################## Decor ##############################	

	addTooltipPattern(e, 'minecraft:*_banner', ['Placeable underneath blocks and rope', col.norm]);
	addTooltips(e,
		[i.vine, ['Can be trimmed w/ shears to prevent growth', col.norm]],
		[i.campfire, ['Boosts elytra users upward when placed on hay bale', col.norm]],
		[i.soul_campfire, ['Pulls elytra users downward when placed on hay bale', col.norm]],
		[i.sponge, ['Can be placed on top of water', col.norm]]
	);

// ############################## Immersive Engineering ##############################	

		// Misc tips
    addTooltips(e,
		[i.sawdust, ['Place in coke oven to soak up creosote oil', col.eng]],
		[i.lead_wire, ['Usable as solder in Engineer\'s Circuit Table', col.eng]],
		[i.copper_wire, ['Usable as solder in Engineer\'s Circuit Table', col.eng]]
	);

	// Precision Sawblade — show the programmed profile (NBT-dependent)
	e.addAdvanced('creativeengineering:precision_sawblade', (item, advanced, text) => {
		var profile = item.nbt ? item.nbt.Profile : null;
		if (profile) {
			var pretty = String(profile).replace(/_/g, ' ').replace(/\b\w/g, function (c) { return c.toUpperCase(); });
			text.add(Text.gray('Sawmill profile: ').append(Text.gold(pretty)));
		} else {
			text.add(Text.darkGray('Unprogrammed — blueprint with a shape to set a profile'));
		}
	});
		
// ############################## Create ##############################	

	addTooltips(e,
		[i.cr_filter, ['Craftable with tough fabric', col.eng]],
		[i.cr_attribute_filter, ['Craftable with tough fabric', col.eng]],
		[i.cr_nozzle, ['Craftable with tough fabric', col.eng]],
		[i.cr_pulley, ['Craftable with tough fabric', col.eng]],
		[i.cr_sail, ['Craftable with tough fabric', col.eng]],
		[i.speaker, ['Make announcements in chat, display message on screen, or speak through narrator', col.rare]],
		[i.woven_mesh, ['Craftable with tough fabric', col.eng]],
		[i.mechanical_crafter, ['Crafted with gold', col.eng]],
		[i.cinder_flour, ['A powerful reagent, changes form when mixed with potions', col.eng]],
		[i.cinder_flour_pile, ['A powerful reagent, changes form when mixed with potions', col.eng]]
	);
	
	//Remove create compress compatability tooltip
	removeTooltips(e, i.crushed_block_aluminum, i.crushed_block_lead, i.crushed_block_silver, i.crushed_block_nickel, i.crushed_block_uranium);

	// Remove Immersive Petroleum's "Increases walking speed by 20.0%": BlockRunner's asphalt tier sets
	// the real speed and shows it in its own "Speed Multiplier" line (feature_writeup/tooltips.md)
	removeTooltipKey(e, 'desc.immersivepetroleum.flavour.asphalt', i.asphalt, i.asphalt_slab, i.asphalt_stair);
	
// ############################## Transport ##############################	 
   
   	addTooltips(e,
		[i.ghost_rail, ['Launches minecarts and allows them to pass through blocks, effect stopped by dreamwood', col.epic]]
	);
    
// ############################## Food / Farmers Delight ##############################
    
	// Bowl, basket and soil lines: Creative Delight (assets/creativedelight/kubejs/farmersdelight/client.js, 2026-11-01).
	// Food tooltips (Farmer's Delight villager soil line, foods, kitchen blocks) moved to Creative Delight 2026-10-27:
	// kubejs/assets/creativedelight/kubejs/<mod>/client.js

	// Fantasy Furniture's seven ovens run smoking recipes with normal fuel (their OvenBlockEntity); expect 7 matches.
	addTooltipPattern(e, 'fantasyfurniture:*/oven', ['Works like a smoker', col.norm]);

	// Brewin' and Chewin' keg temperature blocks: Creative Brewing (assets/creativebrewing/kubejs/brewinandchewin/tooltips.js).
		
// ############################## Archery ##############################

		/*addTooltips(e,
				[i.ghost_rail, ['Launches minecarts and allows them to pass through blocks, effect stopped by dreamwood', col.epic]]
		);*/
  	
// ############################## Etched/music ##############################
  	
  	addTooltips(e,
		[i.jukebox,
			['Please stop inserting pancakes. -Management', col.norm]],
		[i.radio,
			['Picks up signals from a distant communication network...', col.epic],
			['(Loops a provided MP3 URL, power w/ redstone to deactivate)', col.norm]],
		[i.boombox,
			['Portable jukebox, loops the disc inserted while in inventory', col.rare],
			['Audible to others while in hand or off-hand', col.norm]],
		[i.etching_table,
			['Create custom music discs!', col.epic],
			['Uses blank music discs and music labels', col.norm]],
		[i.album_jukebox,
			['Store nine discs, play them in order', col.rare],
			['Power w/ redstone to deactivate', col.norm]],
		[i.music_label,
			['Add music author and title, dyeable', col.norm],
			['Use multiple dyes on a label, combine two dyed labels into a hybrid label', col.norm]],
		[i.album_cover,
			['Holds up to nine music discs like a bundle', col.norm],
			['Autogenerates cover art from BandCamp or SoundCloud URLs', col.norm],
			['First disc inserted determines album art, use grindstone to remove', col.norm]]
	);
  	
// ############################## Misc Mods, Quark, Supplementaries, Framed ##############################

	// Creative Compat (assets/creativecompat/kubejs/tooltips.js).

// ############################## Vinery ##############################
	// Vinery port lines: Creative Brewing (assets/creativebrewing/kubejs/vinery/tooltips.js).

// ############################## Flower Pots ##############################

	// Vinery flower box rows: Creative Brewing (assets/creativebrewing/kubejs/vinery/tooltips.js). The Supplementaries
	// box, planter boxes and silt pots: Creative Compat (assets/creativecompat/kubejs/tooltips.js).

// ############################## Botania ##############################
	// Vine ball and Trinket Case lines: Creative Magic (assets/creativemagic/kubejs/tooltips.js).

// ############################## End Gear ##############################

	// Felsic, elytra, air brake and dragon gear lines: Creative Equipment (assets/creativeequipment/kubejs/tooltips.js).

	addTooltips(e,
		[i.wrappist_shard, ['Brewable into potion of long reach', col.rare]]
	);

	addTooltipPattern(e, 'geode:wrappist*', { exclude: [i.wrappist_goat_horn, i.wrappist_pedestal] }, ['Found within geodes in the outer end lands', col.rare]);

	addTooltips(e,
		[i.budding_wrappist, ['Found within geodes in the outer end lands', col.rare]],
		[i.small_wrappist_bud, ['Capable of binding to dragon scale', col.rare]],
		[i.medium_wrappist_bud, ['Capable of binding to dragon scale', col.rare]],
		[i.large_wrappist_bud, ['Capable of binding to dragon scale', col.rare]],
	);
})
