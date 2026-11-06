// Creative Compat: item tooltips for the mod tweaks (misc mods, Quark, Supplementaries, Framed and copycat blocks,
// flower pots); moved from kubejs/client_scripts/tooltips.js on 2026-11-01, rows unchanged. The helpers and the colour
// palette stay in the pack's tooltips.js and are read here through global.tooltips at event time (each script pack has
// its own scope).
ItemEvents.tooltip(e => {
	var T = global.tooltips
	if (!T) { console.error('[creativecompat] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	var addTooltips = T.addTooltips, addTooltipPattern = T.addTooltipPattern, removeTooltipKey = T.removeTooltipKey
	var col = T.col
	var i = global.item

// ############################## Misc Mods ##############################
  	
  	// Chimes
  	addTooltipPattern(e, 'chimes:*_chimes', ['They sound nice', 'gray']);
  	
  	//Comforts 
  	addTooltips(e,[i.rope_nail, ['Use to hang hammocks', col.norm]]);
  	addTooltipPattern(e, 'comforts:sleeping_bag*', ['Sleep anywhere without setting a spawnpoint', col.norm]);
		addTooltipPattern(e, 'comforts:hammock*', ['Use with rope and nail', col.norm]);
  	
  	//Compass
  	addTooltips(e,
		[i.explorers_compass, ['Points to any structure!', col.leg], ['Not craftable', col.warn]],
		[i.natures_compass, ['Points to any biome!', col.epic]]
	);
  	
  	//Domestic Innovation
  	addTooltips(e,
		[i.wayward_lantern, ['Allows following pets in unloaded chunks to wander home', col.good]],
		[i.feather_on_a_stick, ['Play with your pets, herd them effectively', col.norm], ['Sticks to hostile mobs: your cats attack them', col.good]],
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
		[i.ladder_oak, ['Right click to extend ladder, can hang below a supported ladder', col.norm]],
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

// ############################## Flower Pots ##############################

	// Wall-mountable plant boxes (feature_writeup/flower_pots.md 3.4)
	addTooltips(e,
		[i.flower_box, ['Can be placed on walls', col.norm]]
	);
	addTooltipPattern(e, '*:*_planter_box', ['Can be placed on walls', col.norm]);
	// Silt pots are soil for any plant once filled with rooted dirt (SiltPotSoilMixin)
	addTooltipPattern(e, 'twigs:*silt_pot', ['Use Rooted Dirt on it to fill it; plants then grow on top', col.norm]);
})
