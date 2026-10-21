var NCH_MCRegistry = Java.loadClass('net.minecraft.core.Registry');

function buildTooltipLine(line) {
    var data = { text: line[0], color: line[1] };
    var modifiers = line[2];
    if (modifiers) {
        if (modifiers.bold !== undefined) data.bold = modifiers.bold;
        if (modifiers.italic !== undefined) data.italic = modifiers.italic;
        if (modifiers.underlined !== undefined) data.underlined = modifiers.underlined;
        if (modifiers.strikethrough !== undefined) data.strikethrough = modifiers.strikethrough;
        if (modifiers.obfuscated !== undefined) data.obfuscated = modifiers.obfuscated;}
    return data;
}

function addTooltips(e) {
    var entries = Array.prototype.slice.call(arguments, 1);
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        var id = entry[0];
        var lines = entry.slice(1);
        if (lines.length === 1) {
            e.add(id, buildTooltipLine(lines[0]));
        } else {
            var tipLines = [];
            for (var j = 0; j < lines.length; j++) {
                tipLines.push(buildTooltipLine(lines[j]));}
            e.add(id, tipLines);}}      
}

function addTooltipPattern(e, pattern) {
    var idx = 2;
    var excludeIds = [];
    if (arguments.length > 2 && arguments[2] && arguments[2].exclude) {
        excludeIds = arguments[2].exclude;
        idx = 3;}
    var lines = Array.prototype.slice.call(arguments, idx);
    var data;
    if (lines.length === 1) {
        data = buildTooltipLine(lines[0]);
    } else {
        data = [];
        for (var j = 0; j < lines.length; j++) {
            data.push(buildTooltipLine(lines[j]));}}
    var regex = new RegExp('^' + pattern.replace(/[.+^${}()|[\]\\?]/g, '\\$&').replace(/\*/g, '.*') + '$');
    var keys = NCH_MCRegistry.ITEM.keySet();
    var it = keys.iterator();
    var matchCount = 0;
    while (it.hasNext()) {
        var rl = it.next();
        var id = rl.toString();
        if (regex.test(id) && excludeIds.indexOf(id) === -1) {
            e.add(id, data);
            matchCount++;}}
    console.log('[addTooltipPattern] ' + pattern + ' -> ' + matchCount + ' matches (excluded ' + excludeIds.length + ')');
}

function removeTooltips(e) {
    var ids = Array.prototype.slice.call(arguments, 1);
    for (var i = 0; i < ids.length; i++) {
        e.addAdvanced(ids[i], function(item, advanced, text) {
            // Keep index 0 (display name), strip every line after it
            while (text.size() > 1) {
                text.remove(1);
            }
        });
    }
}

// Strip only the lines built from one translation key (a mod's own flavour text), leaving the name
// and every other line alone - unlike removeTooltips, BlockRunner's "Speed Multiplier" line survives.
// A key ending in '*' matches every key with that prefix (mods that build the key from the item id).
//   removeTooltipKey(e, 'desc.somemod.flavour.thing', i.thing, i.thing_slab)
//   removeTooltipKey(e, 'item.somemod.fruit.tooltip.*', i.apple, i.pear)
function removeTooltipKey(e, key) {
    var ids = Array.prototype.slice.call(arguments, 2);
    var isPrefix = key.charAt(key.length - 1) === '*';
    var prefix = isPrefix ? key.substring(0, key.length - 1) : key;
    for (var k = 0; k < ids.length; k++) {
        e.addAdvanced(ids[k], function(item, advanced, text) {
            for (var n = text.size() - 1; n >= 1; n--) {
                var lineKey = null;
                try { lineKey = String(text.get(n).getContents().getKey()); } catch (err) {}
                if (lineKey !== null && (isPrefix ? lineKey.indexOf(prefix) === 0 : lineKey === key)) {
                    text.remove(n);
                }
            }
        });
    }
}

ItemEvents.tooltip(e => {
// ############################## Global ##############################
var i = global.item;

// ############################## Color ##############################
	var col = {};
	
	col.black = 'black';
	col.dark_blue = 'dark_blue';
		col.dblue = 'dark_blue';
	col.dark_green = 'dark_green';
		col.dgreen = 'dark_green';
	col.dark_aqua = 'dark_aqua';
		col.daqua = 'dark_aqua';
	col.dark_red = 'dark_red';
		col.dred = 'dark_red';
	col.dark_purple = 'dark_purple';
		col.dpurple = 'dark_purple';
	col.gold = 'gold';
	col.gray = 'gray';
	col.dark_gray = 'dark_gray';
		col.dgray = 'dark_gray';
	col.blue = 'blue';
	col.green = 'green';
	col.aqua = 'aqua';
	col.red = 'red';
	col.light_purple = 'light_purple';
		col.lpurple = 'light_purple';
	col.yellow = 'yellow';
	col.white = 'white';
	
	col.norm = 'gray';
	col.uncm = 'green';
	col.rare = 'dark_blue';
	col.epic = 'dark_purple';
	col.leg = 'aqua';
	col.warn = 'dark_red';
	col.good = 'dark_green';
	col.eng = 'gold';

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

		[i.coil_copper,
			['Electrical Conductivity: 59.6 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0168 Ω·mm²/m', col.dark_gray]],
		[i.coil_iron,
			['Electrical Conductivity: 10.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0971 Ω·mm²/m', col.dark_gray]],
		[i.coil_gold,
			['Electrical Conductivity: 45.2 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0221 Ω·mm²/m', col.dark_gray]],
		[i.coil_zinc,
			['Electrical Conductivity: 16.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0592 Ω·mm²/m', col.dark_gray]],
		[i.coil_aluminum,
			['Electrical Conductivity: 37.7 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0265 Ω·mm²/m', col.dark_gray]],
		[i.coil_lead,
			['Electrical Conductivity: 4.5 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.2222 Ω·mm²/m', col.dark_gray]],
		[i.coil_nickel,
			['Electrical Conductivity: 14.3 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0699 Ω·mm²/m', col.dark_gray]],
		[i.coil_silver,
			['Electrical Conductivity: 63.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0159 Ω·mm²/m', col.dark_gray]],
		[i.coil_steel,
			['Electrical Conductivity: 7.0 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.1429 Ω·mm²/m', col.dark_gray]],
		[i.coil_brass,
			['Electrical Conductivity: 15.9 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0629 Ω·mm²/m', col.dark_gray]],
		[i.coil_constantan,
			['Electrical Conductivity: 2.04 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.4902 Ω·mm²/m', col.dark_gray]],
		[i.coil_electrum,
			['Electrical Conductivity: 11 MS/m', col.dark_gray],
			['Electrical Resistivity: 0.0909 Ω·mm²/m', col.dark_gray]],

		[i.coil_hv,
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
		[i.brass_speaker, ['Make announcements in chat, display message on screen, or speak through narrator', col.rare]],
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
    
    addTooltips(e,
		[i.lead_knife, ['Extra nutrients in your food...', col.warn]],
		[i.bowl, ['Warning: Sometimes Consumed', col.warn]],
		[i.sugar_cube, ['Feed to your horse, dissolves on contact w/ water', col.uncm]],
		[i.fd_basket, ['Collects items from the block in front of opening, can be placed sideways', col.uncm]],
		[i.rich_soil, ['Crops grow slightly faster and can\'t be trampled!', col.uncm],['Can cultivate mushroom colonies from mushrooms', col.uncm]],
		[i.rich_farmland, ['Crops grow slightly faster and can\'t be trampled!', col.uncm],['Can cultivate mushroom colonies from mushrooms', col.uncm]],
		[i.fertile_soil, ['Crops grow significantly faster and can\'t be trampled!', col.rare],['Doesn\'t need to be tilled, can be soaked w/ water to increase plant growth', col.rare]],
		[i.nulch, ['Grows nether crops faster', col.rare],['Can be filled w/ lava, but watch your feet!', col.rare]]
	);

	// Brewin' and Chewin' keg temperature blocks (alcohol.md 8.3): each source within 2 blocks of a keg counts 1
	addTooltips(e,
		[i.heating_cask,
			['Keg heat source within 2 blocks: on its own, one makes a keg Warm and two make it Hot', col.uncm],
			['Heats a cooking pot like a stove', col.norm],
			['Use a water bucket or bottle to vent steam', col.norm]],
		[i.ice_crate,
			['Keg cold source within 2 blocks: on its own, one makes a keg Chilly and two make it Cold', col.uncm]]
	);
		
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
  	
// ############################## Misc Mods ##############################
  	
  	// Chimes
  	addTooltipPattern(e, 'chimes:*_chimes', ['They sound nice', 'gray']);
  	
  	//Comforts 
  	addTooltips(e,[i.rope_nail, ['Use to hang hammocks', col.norm]]);
  	addTooltipPattern(e, 'comforts:sleeping_bag*', ['Sleep anywhere without setting a spawnpoint', col.norm]);
		addTooltipPattern(e, 'comforts:hammock*', ['Use with rope and nail', col.norm]);
  	
  	//Compass
  	addTooltips(e,
		[i.explorers_compass, ['Points to any structure!', col.leg]],
		[i.natures_compass, ['Points to any biome!', col.epic]]
	);
  	
  	//Domestic Innovation
  	addTooltips(e,
		[i.wayward_lantern, ['Allows following pets in unloaded chunks to wander home', col.good]],
		[i.feather_on_a_stick, ['Play with your pets, herd them effectively', col.norm]],
		[i.rotten_apple, ['Turns a horse into a zombie horse', col.epic]],
		[i.sinister_carrot, ['Turns a zombie horse into a skeleton horse', col.epic]],
		[i.deed_of_ownership, ['Allows transferring ownership of a pet to another player', col.rare]],
		[i.pet_drum, ['Set behavior of all pets in hearing range, can be activated w/ redstone', col.uncm]],
		[i.collar_tag, ['Usable as a nametag, can be enchanted', col.uncm]]
	);
	addTooltipPattern(e, 'domesticationinnovation:pet_bed*', ['Allows pets to respawn if killed', col.good]);
	
	//Everything copper
	addTooltips(e,
		[i.copper_anvil, ['Remember to wax your anvils', col.norm]]
	);
		
  	//Minecraft comes alive
  	addTooltips(e,
		[i.matchmaker_ring, ['Arrange a marriage...', col.norm]],
		[i.baby_boy, ['Don\'t leave in the furnace unattended!', col.norm]],
		[i.baby_girl, ['Don\'t leave in the furnace unattended!', col.norm]]
	);
  	
  	//Immersive weathering
  	addTooltips(e,
		[i.tallow, ['Can be used to wax copper and iron blocks', col.norm]],
		[i.steel_wool, ['Removes oxidation and rust from copper and iron blocks respectively', col.norm]]
	);

  	//Waystone
  	addTooltips(e,
		[i.warpstone, ['On-demand waystone', col.epic]]
	);
		
	//Archery
	addTooltips(e,
		[i.spectral_arrow, ['Highlights target on hit', col.uncm]],
		[i.balloon_arrow, ['Not affected by gravity, attachs balloon to target on hit', col.uncm]],
		[i.blossom_arrow, ['Bone meal from a distance', col.uncm]],
		[i.cupid_arrow, ['Makes entities fall in love', col.rare], ['Transported from distant lands by wandering traders', col.norm]],
		[i.dousing_arrow, ['Removes fire and douses lava', col.norm]],
		[i.drill_arrow, ['Breaks a block at iron level', col.uncm]],
		[i.echoing_arrow, ['Highlights nearby entities on hit', col.rare]],
		[i.ethereal_arrow, ['Phases through a wall when fired', col.epic], ['Transported from distant lands by wandering traders', col.norm]],
		[i.explosive_arrow, ['Explodes on impact', col.rare]],
		[i.growing_arrow, ['Bigger and stronger the longer it flies', col.epic], ['Transported from distant lands by wandering traders', col.norm]],
		[i.hookshot_arrow, ['Attaches a lead to target on hit', col.uncm]],
		[i.ink_arrow, ['Creates a blinding ink cloud on impact', col.uncm]],
		[i.luminous_arrow, ['Glows when fired', col.norm]],
		[i.message_arrow, ['Rename and fire to send a message', col.norm]],
		[i.paint_arrow, ['Recolor block or entity, can be dyed', col.norm]],
		[i.party_arrow, ['Explodes, but in a fun way', col.uncm]],
		[i.prismarine_arrow, ['Weak in air, strong in water', col.uncm]],
		[i.pufferfish_arrow, ['Spawns a pufferfish', col.uncm]],
		[i.redstone_torch_arrow, ['Places a redstone torch where it lands', col.norm]],
		[i.reinforced_arrow, ['Stronger, pierces armor', col.rare]],
		[i.repulsive_arrow, ['Makes entities run away', col.uncm]],
		[i.seeker_arrow, ['Homes in on entities', col.epic]],
		[i.slime_arrow, ['Bounces thrice!', col.uncm]],
		[i.soul_torch_arrow, ['Places a soul torch where it lands', col.norm]],
		[i.split_arrow, ['Splits into 8 arrows on contact', col.rare]],
		[i.teleportation_arrow, ['Teleport to arrow destination', col.epic]],
		[i.whistling_arrow, ['Creates noise, distracts enemies', col.norm]]
	);

// ############################## Vinery ##############################
	// Vinery port (alcohol/vineryport #16). Wine sizes, rack and Wine Box capacity and the aging hint come from the port
	// itself (DrinkBlockItem.BottleSize), since the sizes live in its code; these are the lines it doesn't write.
	addTooltips(e,
		// Processing
		[i.grapevine_pot, ['Fill with grapes of one type, then jump on it to press juice (counts in JEI)', col.norm]],
		[i.wine_press, ['Mashes apples, then turns the mash and a wine bottle into apple juice', col.norm]],
		[i.fermentation_barrel, ['Pour in juice (one type at a time) and add the ingredients and a wine bottle', col.norm],
			['Sneak right-click with an empty hand to pour the juice out', col.norm]],
		// Growing
		[i.grapevine_stem, ['Place two high and plant grape seeds on the top one; jungle grapes need a lattice', col.norm],
			['Shears give the seed back; breaking a planted stem loses it', col.norm]],
		[i.grapevine_lattice, ['Plant jungle grape seeds on it', col.norm],
			['Shears give the seed back; breaking a planted lattice loses it', col.norm]],
		[i.apple_leaves, ['Some leaves grow apples; pick ripe ones with an empty hand', col.norm]],
		[i.cherry_leaves, ['Some leaves grow cherries; pick ripe ones with an empty hand', col.norm]],
		[i.apple_tree_sapling, ['Grows an apple tree', col.norm]],
		[i.cherry_sapling, ['Grows a cherry tree', col.norm]],
		// Utility
		[i.stackable_log, ['A campfire: cooks up to 4 foods and burns 10 minutes; rain can put it out', col.norm],
			['Add logs to refuel; a shovel or a water bottle puts it out', col.norm]],
		// Wines
		[i.chorus_wine, ['Makes you wake up in strange places', col.lpurple]]
	);

// ############################## Quark ##############################

	//addTooltipPattern(e, 'quark:*_rune', {text: 'Recolor enchantment shine!',color: 'white'});
	//addTooltipPattern(e, 'quark:*_rune', ['Recolor enchantment shine!', col.norm]);
	addTooltipPattern(e, 'quark:*_rune', {exclude: ['quark:rainbow_rune']}, ['Recolor enchantment shine!', col.norm]);
	addTooltipPattern(e, 'quark:hollow_*_log', ['Can be crawled through', col.norm]);
	addTooltipPattern(e, 'quark:*_crystal_lamp', ['Recolors beacon beam when powered', col.norm]);
	addTooltipPattern(e, 'quark:*_corundum_cluster', ['Redirects and recolors beacon beam', col.norm]);
	addTooltipPattern(e, 'minecraft:*_coral_fan', {exclude: [
		'minecraft:dead_tube_coral_fan',
		'minecraft:dead_brain_coral_fan',
		'minecraft:dead_bubble_coral_fan',
		'minecraft:dead_fire_coral_fan',
		'minecraft:dead_horn_coral_fan'
	]}, ['Can be placed on cacti', col.norm]);
	addTooltipPattern(e, 'everycomp:*_ladder', ['Right click to extend ladder, can hang below a supported ladder', col.norm]);
	addTooltipPattern(e, 'copycats:*_ladder', ['Right click to extend ladder, can hang below a supported ladder', col.norm]);
	addTooltipPattern(e, 'createdeco:*_ladder', ['Right click to extend ladder, can hang below a supported ladder', col.norm]);
	addTooltipPattern(e, 'create:*_ladder', ['Right click to extend ladder, can hang below a supported ladder', col.norm]);
	addTooltipPattern(e, 'quark:*_ladder', ['Right click to extend ladder, can hang below a supported ladder', col.norm]);
	addTooltips(e,
		[i.ladder, ['Right click to extend ladder, can hang below a supported ladder', col.norm]],
		[i.rainbow_rune, ['Recolor enchantment shine!', col.epic]],
		[i.bottled_cloud, ['Place a temporary block midair!', col.rare]],
		[i.slime_in_a_bucket, ['Detects slime chunks!', col.rare]],
		[i.soul_bead, ['Seeks out the nearest Nether Fortress', col.rare]],
		[i.seed_pouch, ['Pick up and place seeds faster', col.uncm]],
		[i.torch_arrow, ['Places a torch where it lands', col.uncm]],
		[i.pathfinders_quill, ['Find distant biomes', col.uncm]],
		[i.mud_brick_lattice, ['Can be waterlogged without letting water flow out', col.norm]],
		[i.crate, ['Stores 640 items, regardless of stack size', col.rare]],
		[i.destructive_rod, ['Breaks blocks when moved by piston', col.epic]],
		[i.iron_button, ['Very long pulse', col.norm]],
		[i.gold_button, ['Very short pulse', col.norm]],
		[i.grate, ['Unleashed animals won\'t cross, but items fall right through', col.norm]],
		[i.sturdy_stone, ['Cannot be pushed by pistons', col.norm]],
		[i.obsidian_pressure_plate, ['Only activates for players', col.rare]],
		[i.magnet, ['Push and pull magnetic blocks', col.epic]],
		[i.gravisand, ['Falls when it receives redstone signal, floats if it cannot fall, propagates gravity state to adjacent gravisand', col.epic]],
		[i.redstone_randomizer, ['Random* output for each input pulse', col.rare]],
		[i.chute, ['Drops items directly below', col.norm]],
		[i.ender_watcher, ['Outputs signal based on player looking at center of block', col.epic]],
		[i.enderman_head, ['Outputs signal based on length of player eye contact', col.epic]],
		[i.feeding_trough, ['Automatically feeds and breeds animals if filled with food', col.uncm]],
		[i.dispenser, ['Can place blocks', col.norm]],
		[i.piston, ['Can move tile entities like chests', col.norm]],
		[i.sticky_piston, ['Can move tile entities like chests', col.norm]],
		[i.tinted_glass, ['Can dim beacon beams', col.norm]],
		[i.item_frame, ['Can be dyed, allows custom colors', col.norm]],
		[i.glow_item_frame, ['Can be dyed, allows custom colors', col.norm]],
		[i.poisonous_potato, ['Can be fed to a baby animal for a chance to stunt their growth', col.norm]],
		[i.spore_blossom, ['Can be duplicated w/ bone meal', col.norm]],
		[i.dragon_scale, ['Drops from ender dragon when killed, can be crafted w/ elytra to duplicate', col.epic]]
	);
  	
// ############################## Supplementaries ##############################

	addTooltipPattern(e, 'supplementaries:present*', ['Pack items for player w/ message, only recipient can open', col.norm]);
	addTooltipPattern(e, 'supplementaries:trapped_present*', ['Pack items for player w/ message, only recipient can open', col.norm]);
	addTooltipPattern(e, 'supplementaries:sack*', ['Retains 9-slot inventory when picked up, 3+ sacks will overencumber the player, can be dyed', col.norm]);
	
	addTooltips(e,
		[i.faucet, ['Drains blocks containing fluid into inventory below, waterlogged blocks act as infinite source', col.uncm]],
		[i.bellows, ['Speeds up furnaces, sustains fire blocks, pushes entities, continuously active when powered', col.norm]],
		[i.notice_board, ['Displays banners maps and books, wax to lock, redstone pulse turns page', col.norm]],
		[i.soap, ['Cleaning items will remove dye', col.uncm]],
		[i.soap_block, ['Unpredictably slippery, bellowing an adjacent wet sponge creates bubbles', col.rare]],
		[i.bubble_blower, ['Stasis enchantment allows placing bubble block mid-air', col.epic]],
		[i.relayer, ['Retransmits analog redstone signal', col.norm]],
		[i.cog_block, ['Transmits analog redstone signals, allows transmitting signals vertically', col.uncm]],
		[i.clock_block, ['Displays game time, provides comparator output', col.uncm]],
		[i.turn_table, ['Rotates block on face, delay based on signal strength', col.norm]],
		[i.crystal_display, ['Displays a character based on signal strength', col.uncm]],
		[i.gold_door, ['Disables interaction when powered', col.norm]],
		[i.gold_trapdoor, ['Disables interaction when powered', col.norm]],
		[i.blackboard, ['Can be drawn on with chalk, quartz, or dye', col.norm]],
		[i.bamboo_spikes, ['Can be tipped w/ lingering potions', col.norm]],
		[i.flute, ['Toot Toot', col.uncm],['Beckons nearby standing pets, can be bound to specific pet (or horse)', col.norm]],
		[i.redstone_illuminator, ['Dims based on signal strength', col.norm]],
		[i.globe, ['Displays current coordinates, provides comparator output, floats on pedestal', col.rare]],
		[i.globe_sepia, ['Displays current coordinates, provides comparator output, floats on pedestal', col.rare]],
		[i.altimeter, ['Displays current altitude', col.uncm]],
		[i.fodder, ['Automatically eaten by animals, can be placed by dispensers, clear 1 layer w/ hoe', col.norm]],
		[i.pedestal, ['Displays items, swap item w/ sack to avoid block update, end crystal placed on top provides 3 enchantment power', col.norm]],
		[i.feather_block, ['Negates fall damage', col.norm]],
		[i.flint_block, ['Strikes fire when moved past iron blocks', col.norm]],
		[i.hourglass, ['Can be flipped w/ turn table, also accepts slime and honey', col.uncm]],
		[i.cage, ['Captures small animals', col.uncm]],
		[i.doormat, ['Conceals an item below', col.norm]],
		[i.key, ['Rename to set code, lock safes, netherite doors and lock blocks', col.uncm]],
		[i.heavy_key, ['Unique key that cannot be copied', col.epic]],
		[i.lock_block, ['Emits a pulse when unlocked', col.norm]],
		[i.safe,
			['Acts as a fireproof shulker box', col.epic],
			['Key Lockable', col.norm, {italic: true}]],
		[i.tall_netherite_door, ['Key Lockable', col.norm, {italic: true}]],
		[i.short_netherite_door, ['Key Lockable', col.norm, {italic: true}]],
		[i.statue, ['Holds items, waves when powered', col.norm]],
		[i.goblet, ['Holds most fluids, right-click to drink', col.norm]],
		[i.quiver, 
			['Swap between up to 7 different arrows!', col.uncm],
			['Hold [R] and scroll to swap', col.norm]],
		[i.slice_map, ['Map of a specific altitude', col.norm]],
		[i.bomb, ['Throwable explosive, doesn\'t damage blocks', col.uncm]],
		[i.blue_bomb,
			['Powerful throwable explosive, doesn\'t damage blocks', col.rare],
			['Detonates nearby creepers', col.norm]],
		[i.spiky_bomb,
			['Powerful throwable explosive, doesn\'t damage blocks', col.rare],
			['Shoots shrapnel projectiles', col.norm]],
		[i.rope_arrow, ['Deploys a rope coil where it lands', col.uncm]],
		[i.antique_ink, ['Age your signs, maps, globes, and written books', col.rare]],
		[i.candy, ['Don\'t eat too many, they\'ll rot your teeth!', col.uncm]],
		[i.pancake, ['Can be covered in honey or chocolate', col.uncm]],
		[i.creeper_head, 
			['Can be stacked, candles can be placed on top', col.uncm],
			['Can be used w/ fence to create skull pike, which scares hostile mobs away', col.norm]],
		[i.zombie_head, 
			['Can be stacked, candles can be placed on top', col.uncm],
			['Can be used w/ fence to create skull pike, which scares hostile mobs away', col.norm]],
		[i.skeleton_head, 
			['Can be stacked, candles can be placed on top', col.uncm],
			['Can be used w/ fence to create skull pike, which scares hostile mobs away', col.norm]],
		[i.wither_head, ['Can be stacked, candles can be placed on top', col.uncm]]
	);
		
// ############################## Framed / Copycat Blocks ##############################

	// Create schematics save a copycat's material, but not a FramedBlocks camo (feature_writeup/tooltips.md)
	addTooltipPattern(e, 'framedblocks:framed_*', {exclude: [i.framed_blueprint, i.framed_hammer, i.framed_key,
		i.framed_reinforcement, i.framed_screwdriver, i.framed_wrench, i.framed_item_frame, i.framed_glowing_item_frame]},
		['Create schematics won\'t save the block inside it', col.norm]);
	addTooltipPattern(e, 'copycats:copycat_*', ['Create schematics save the block inside it', col.norm]);
	addTooltips(e,
		[i.copycat_panel, ['Create schematics save the block inside it', col.norm]],
		[i.copycat_step, ['Create schematics save the block inside it', col.norm]]
	);

	// Hybrid blocks: one camo per part; the newest camo sets the walk speed (feature_writeup/blockrunner.md 3.3)
	addTooltips(e,
		[i.framed_slab, ['Place a second one in the same space to make a hybrid block, one camo per half', col.norm]],
		[i.framed_panel, ['Place a second one in the same space to make a hybrid block, one camo per half', col.norm]]
	);
	addTooltipPattern(e, 'framedblocks:*double*', ['Hybrid block: each part holds its own camo', col.norm]);

// ############################## Botania ##############################	 
   
   	addTooltips(e,
		[i.vine_ball, ['Throw to drop vines on target block', col.norm]]
	);

// ############################## End Gear ##############################

	//removeTooltips(e, i.echo_helmet, i.echo_chestplate, i.echo_leggings, i.echo_boots);

	addTooltips(e,
		[i.felsic_helmet, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_chestplate, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_leggings, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_boots, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_sword, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_pickaxe, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_axe, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_shovel, ['Capable of binding to dragon scale', col.epic]],
		[i.felsic_hoe, ['Capable of binding to dragon scale', col.epic]],
		[i.elytra, ['Craft with dyes or a banner to customize', col.epic], ['Wings can be split, customized individually, and recombined', col.epic],],
		[i.custom_elytra, ['Craft with dyes or a banner to customize', col.epic], ['Wings can be split, customized individually, and recombined', col.epic],],
		[i.elytra_wing, ['Craft with dyes or a banner to customize', col.epic], ['Craft with another wing to recombine', col.epic],],
		[i.air_brake, ['Slows horizontal and upward elytra flight, causes stalling at low speeds', col.epic]],
		[i.dragon_anvil, ['Unbreakable!', col.epic]],
		[i.dragon_sword, ['Weaken and bleed your target', col.epic], ['Steals life-force from kills', col.epic]],
		[i.dragon_axe, ['Levitate and bleed your target', col.epic], ['Jump slightly higher while held', col.epic]],
		[i.dragon_shovel, ['Discombobulate your opponent', col.epic], ['Reduces knockback when held', col.epic]],
		[i.dragon_trident, ['Inflict mining fatigue on hit', col.epic], ['Fall slower when held', col.epic]],
		[i.dragon_hoe, ['Chance to refund bone meal held in the other hand', col.epic]],
	);

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
