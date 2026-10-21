// Item-physics behaviour tags (patcherMod ItemPhysicSplit). Runs on /reload — dropped
// items pick up membership changes on the next tick, no restart.
//
//   creativeengineering:physics/floats      → floats in water
//   creativeengineering:physics/sinks       → force-sink (wins over floats)
//   creativeengineering:physics/fireproof   → survives open fire, still burns/melts in lava
//   creativeengineering:physics/lavaproof   → immune to BOTH open fire and lava (wins over fireproof)
//
// floats vs sinks is the WATER axis; fireproof vs lavaproof is the FIRE axis — an item can be
// on both (e.g. a wooden boat floats AND nether wood is fireproof). Default for anything
// untagged: sinks + destroyed by fire and lava. patcherMod's jar seeds a small baseline
// (netherite via #forge tags, boats, feathers, paper, buckets, …); entries here merge on top.
//
// ─── GRAY-AREA CALLS (this list is the breakdown; flip any of these freely) ──────────────
//   • The pack already commits the WHOLE stone family (#forge:stone / #forge:cobblestone /
//     every #create:stone_type) to LAVAPROOF. Vanilla stone/andesite/granite/diorite/
//     deepslate/blackstone shapes below follow that. If you'd rather reserve lavaproof for
//     obsidian + deepslate + blackstone + nether + end and drop plain stone to fireproof,
//     move the STONE_LAVA block down into fireproof.
//   • quartz → lavaproof (you asked: yes, real quartz melts ~1670 °C vs lava ~1200 °C).
//   • ice / packed ice / snow → fireproof (melt, don't vanish, under open flame).
//   • sculk family → fireproof (alien growth, non-flammable in MC; not lava-tough).
//   • sugar → sinks with the other powders; bone_meal → floats (you listed it as a floating
//     crafting ingredient). Flip sugar to floats if you want parity.
//   • honey_bottle → fireproof (bottled, glass rule), NOT float. Raw honey/comb float.
//   • hay_block, shield, wooden tools, turtle_helmet, armor_stand → float (organic/wood wins).
//   • nether_bricks / basalt / magma_block / glowstone-less nether blocks → lavaproof.
//   • sea_lantern → fireproof only ("prismarine building, NOT lanterns").
//   • tnt / tnt_minecart / torches / campfires / coal / redstone_torch / redstone_lamp
//     / target / nether_wart → left DEFAULT (burn). Say the word to reclassify.

console.log('item physics tags starting')

// ─────────────────────────────────────────────────────────────────────────────
//  physicsFromTag(event, behaviour, sourceTags[, opts])   — behaviour to every item in
//  another tag. Default adds a NESTED #tag ref (transitive, order-safe). opts.except /
//  opts.expand force expansion to concrete ids. See git history for the long doc.
// ─────────────────────────────────────────────────────────────────────────────
const PHYSICS_TAG = {
	float:      'creativeengineering:physics/floats',
	floats:     'creativeengineering:physics/floats',
	sink:       'creativeengineering:physics/sinks',
	sinks:      'creativeengineering:physics/sinks',
	fireproof:  'creativeengineering:physics/fireproof',
	lavaproof:  'creativeengineering:physics/lavaproof',
	unburnable: 'creativeengineering:physics/lavaproof',
	never:      'creativeengineering:physics/lavaproof',
}

function physicsFromTag(event, behaviour, sourceTags, opts) {
	opts = opts || {}
	const target = PHYSICS_TAG[String(behaviour).toLowerCase()]
	if (!target) {
		console.warn('[item_physics] physicsFromTag: unknown behaviour "' + behaviour + '"')
		return 0
	}

	const strip  = s => String(s).replace(/^#/, '')
	const srcs   = (Array.isArray(sourceTags) ? sourceTags : [sourceTags]).map(strip)
	const except = (opts.except == null ? [] : (Array.isArray(opts.except) ? opts.except : [opts.except])).map(strip)
	const expand = opts.expand === true || except.length > 0

	let n = 0
	srcs.forEach(src => {
		if (!expand) {
			event.add(target, '#' + src)          // nested reference — transitive + order-safe
			n++
			return
		}
		event.get(src).getObjectIds().forEach(rl => {
			const id = rl.toString()
			if (except.indexOf(id) !== -1) return
			event.add(target, id)
			n++
		})
	})

	console.log('[item_physics] ' + target + ' += ' +
		(expand ? n + ' items' : srcs.map(s => '#' + s).join(', ')) +
		' from ' + srcs.join(', ') +
		(except.length ? ' (except ' + except.join(', ') + ')' : ''))
	return n
}

// ── small builders for the tedious vanilla families ─────────────────────────
// (KubeJS' Rhino has NO spread inside array literals — build with .concat(), never [...x])
const COLORS = ['white','orange','magenta','light_blue','yellow','lime','pink','gray',
                'light_gray','cyan','purple','blue','brown','green','red','black']
// copper block/cut/slab/stairs, all 4 oxidation stages × waxed/unwaxed
const COPPER = (() => {
	const ox = ['', 'exposed_', 'weathered_', 'oxidized_']
	const out = []
	;['', 'waxed_'].forEach(w => ox.forEach(o => {
		out.push(w + (o ? o + 'copper' : 'copper_block'))                       // solid block
		;['cut_copper', 'cut_copper_slab', 'cut_copper_stairs'].forEach(c =>
			out.push(w + o + c))
	}))
	return out
})()

ServerEvents.tags('item', e => {
	const t   = global.tags
	const i   = global.item
	const add = (behaviour, entries) => t.add(e, PHYSICS_TAG[behaviour], entries)

	// Resolve a bare id through the pack dict (global.item) when it has a CONCRETE
	// mapping there; otherwise fall back to minecraft:<id>. Strings that already carry
	// a namespace pass straight through. Dict keys that point at a #tag (e.g. i.bread =
	// '#forge:bread/wheat') are skipped so we tag the vanilla item, not the whole tag.
	const mc = arr => arr.map(s => {
		if (String(s).indexOf(':') !== -1) return s
		const d = i[s]
		return (typeof d === 'string' && d && d.charAt(0) !== '#') ? d : 'minecraft:' + s
	})

	// Adjustments
	// Totem sinks, crossbow sinks, armor stand sinks, shields sink, trident is fireproof, 

	// Not done yet (feature_writeup/todo/TODO.md gameplay/item-physics #2, item_physics.md F4):
	// Most create components will sink and be fireproof, belts will float, flour/cinder flour float
		// Behavior carries to compacted items,
	// Treated wood will sink
	// Add items from all other mods!

	// ═══════════════════════════════ FLOATS ════════════════════════════════
	// (a) whole families via nested tags — resolve transitively, survive tag edits
	physicsFromTag(e, 'floats', [
		// wood & wood-constructed
		'#minecraft:planks', '#minecraft:logs', '#minecraft:non_flammable_wood',
		'#minecraft:wooden_slabs', '#minecraft:wooden_stairs', '#minecraft:wooden_fences',
		'#minecraft:wooden_doors', '#minecraft:wooden_trapdoors', '#minecraft:wooden_buttons',
		'#minecraft:wooden_pressure_plates', '#forge:fence_gates/wooden',
		'#minecraft:signs', '#minecraft:boats', '#minecraft:banners',
		'#forge:chests/wooden', '#forge:rods/wooden',
		'#create:scrap_wood_small', '#create:scrap_wood_medium', '#create:scrap_wood_large',
		// plant / organic
		'#minecraft:saplings', '#minecraft:leaves', '#minecraft:flowers',
		// textile / fibre / hide
		'#minecraft:wool', '#forge:wool', '#minecraft:wool_carpets', '#minecraft:beds',
		'#forge:string', '#forge:leather', '#forge:dyes',
		// wax / light
		'#minecraft:candles',
		// food-ish
		'#minecraft:fishes',
	])
	// (b) explicit vanilla items with no clean tag of their own
	add('floats', mc([
		'mangrove_roots', 'sponge',                                          // (pack originals)
		// crafted-wood blocks & tools
		'crafting_table', 'cartography_table', 'fletching_table', 'loom', 'bookshelf', 'lectern',
		'barrel', 'composter', 'bee_nest', 'beehive', 'note_block', 'jukebox',
		'chest', 'trapped_chest', 'ladder', 'scaffolding', 'armor_stand', 'painting',
		'item_frame', 'glow_item_frame', 'flower_banner_pattern', 'creeper_banner_pattern',
		'skull_banner_pattern', 'mojang_banner_pattern', 'globe_banner_pattern', 'piglin_banner_pattern',
		'wooden_sword', 'wooden_pickaxe', 'wooden_axe', 'wooden_shovel', 'wooden_hoe',
		'bow', 'crossbow', 'fishing_rod', 'carrot_on_a_stick', 'warped_fungus_on_a_stick',
		'stick', 'bowl', 'shield', 'turtle_helmet',
		// books & paper
		'book', 'writable_book', 'written_book', 'enchanted_book', 'paper',
		'map', 'filled_map', 'name_tag', 'knowledge_book', 'bundle',
		// leather / hide / keratin / membrane
		'leather', 'leather_helmet', 'leather_chestplate', 'leather_leggings', 'leather_boots',
		'leather_horse_armor', 'saddle', 'lead', 'rabbit_hide', 'rabbit_foot',
		'scute', 'phantom_membrane', 'feather', 'elytra', 'nautilus_shell', 'goat_horn',
		// slime / honey / wax
		'slime_ball', 'slime_block', 'honey_block', 'honeycomb', 'honeycomb_block',
		'ochre_froglight', 'verdant_froglight', 'pearlescent_froglight',
		// crafting ingredients you called out as floaty
		'bone', 'bone_meal', 'bone_block', 'rotten_flesh', 'spider_eye', 'fermented_spider_eye','sugar',
		'string', 'cobweb', 'ink_sac', 'glow_ink_sac',
		// plants / fungus / growths
		'bamboo', 'cactus', 'sugar_cane', 'kelp', 'dried_kelp', 'dried_kelp_block', 'lily_pad',
		'moss_block', 'moss_carpet', 'hay_block', 'seagrass', 'vine', 'weeping_vines', 'twisting_vines',
		'glow_lichen', 'hanging_roots', 'spore_blossom', 'big_dripleaf', 'small_dripleaf',
		'fern', 'large_fern', 'tall_grass', 'grass', 'dead_bush', 'sea_pickle',
		'nether_sprouts', 'crimson_roots', 'warped_roots', 'crimson_fungus', 'warped_fungus',
		'shroomlight', 'nether_wart_block', 'warped_wart_block',
		'brown_mushroom', 'red_mushroom', 'brown_mushroom_block', 'red_mushroom_block', 'mushroom_stem',
		'sweet_berries', 'glow_berries', 'cocoa_beans', 'wheat', 'wheat_seeds',
		'beetroot_seeds', 'melon_seeds', 'pumpkin_seeds',
		// everything chorus
		'chorus_fruit', 'popped_chorus_fruit', 'chorus_flower', 'chorus_plant',
		'purpur_block', 'purpur_pillar', 'purpur_slab', 'purpur_stairs',
		// food (NOT poisonous_potato — see sinks)
		'apple', 'golden_apple', 'enchanted_golden_apple', 'bread',
		'carrot', 'golden_carrot', 'potato', 'baked_potato', 'beetroot', 'beetroot_soup',
		'mushroom_stew', 'rabbit_stew', 'suspicious_stew',
		'melon_block', 'melon_slice', 'glistering_melon_slice', 'pumpkin', 'carved_pumpkin', 'jack_o_lantern',
		'cookie', 'pumpkin_pie', 'cake',
		'beef', 'cooked_beef', 'porkchop', 'cooked_porkchop', 'chicken', 'cooked_chicken',
		'mutton', 'cooked_mutton', 'rabbit', 'cooked_rabbit',
		// eggs
		'egg', 'turtle_egg', 'frogspawn',
		// misc light keepsakes
		'firework_rocket', 'totem_of_undying',
		// mob heads (bone/flesh) — wither_skeleton & dragon go to lavaproof instead
		'skeleton_skull', 'zombie_head', 'creeper_head', 'player_head',
	]))
	// (c) creativeengineering's own items — wood-derived, so floats like the rest of the family
	add('floats', mc([
		'wood_pellets',
	]))

	// ═════════════════════════════ FIREPROOF ═══════════════════════════════
	// survives open fire; still burns / melts in lava
	physicsFromTag(e, 'fireproof', [
		'#forge:sand', '#forge:gravel', '#forge:sandstone',
		'#minecraft:dirt', '#forge:concrete', '#minecraft:terracotta',
		'#forge:raw_materials', '#forge:ingots', '#forge:nuggets',      // netherite excluded (lavaproof wins)
		'#minecraft:rails', '#minecraft:anvil', '#minecraft:music_discs',
		'#create:scrap_copper_blocks', '#create:scrap_copper_blocks_cut',
	])
	add('fireproof', mc([
		// (pack originals)
		'crimson_nylium', 'warped_nylium',
		'lapis_block', 'emerald_block', 'redstone_block',   // (diamond_block -> lavaproof below)
		// glass odds & ends
		'glass_bottle',
		// metal blocks & fittings  (concrete powder + all copper variants appended via .concat below)
		'iron_block', 'gold_block', 'raw_iron_block', 'raw_copper_block', 'raw_gold_block',
		'iron_bars', 'chain', 'iron_door', 'iron_trapdoor', 'lightning_rod',
		'cauldron', 'hopper', 'bell',
		// stone/metal machines & utility blocks
		'stonecutter', 'grindstone', 'smithing_table', 'blast_furnace', 'furnace', 'smoker',
		'brewing_stand', 'lantern', 'soul_lantern', 'sea_lantern', 'flower_pot',
		// minecarts (NOT tnt_minecart)
		'minecart', 'chest_minecart', 'furnace_minecart', 'hopper_minecart',
		// stone / iron / gold tools & armour
		'stone_sword', 'stone_pickaxe', 'stone_axe', 'stone_shovel', 'stone_hoe',
		'iron_sword', 'iron_pickaxe', 'iron_axe', 'iron_shovel', 'iron_hoe',
		'golden_sword', 'golden_pickaxe', 'golden_axe', 'golden_shovel', 'golden_hoe',
		'iron_helmet', 'iron_chestplate', 'iron_leggings', 'iron_boots',
		'golden_helmet', 'golden_chestplate', 'golden_leggings', 'golden_boots',
		'chainmail_helmet', 'chainmail_chestplate', 'chainmail_leggings', 'chainmail_boots',
		'iron_horse_armor', 'golden_horse_armor',
		// metal utility items
		'shears', 'flint_and_steel', 'spyglass', 'clock', 'flint', 'emerald', 'lapis_lazuli',
		// bottles & bottled things
		'potion', 'splash_potion', 'lingering_potion', 'dragon_breath', 'experience_bottle', 'honey_bottle',
		// discs, disc bits
		'disc_fragment_5', 'goat_horn',
		// chorus-family manufactured (end_rod = popped chorus + blaze rod, gray)
		'end_rod',
		// enchanted / written books (also float above)
		'enchanted_book', 'written_book',
		// buckets (also: empty bucket floats via patcherMod)
		'bucket', 'water_bucket', 'lava_bucket', 'milk_bucket', 'powder_snow_bucket',
		'cod_bucket', 'salmon_bucket', 'pufferfish_bucket', 'tropical_fish_bucket',
		'axolotl_bucket', 'tadpole_bucket',
		// sandstone shapes not in #forge:sandstone
		'sandstone_slab', 'sandstone_stairs', 'sandstone_wall',
		'red_sandstone_slab', 'red_sandstone_stairs', 'red_sandstone_wall',
		'smooth_sandstone_slab', 'smooth_sandstone_stairs',
		'smooth_red_sandstone_slab', 'smooth_red_sandstone_stairs',
		'cut_sandstone_slab', 'cut_red_sandstone_slab',
		// fired-clay masonry
		'bricks', 'brick', 'brick_slab', 'brick_stairs', 'brick_wall',
		'clay', 'clay_ball',
		// mud (mud block itself is in #minecraft:dirt)
		'packed_mud', 'mud_bricks', 'mud_brick_slab', 'mud_brick_stairs', 'mud_brick_wall',
		// dirt shapes not in the tag
		'dirt_path', 'farmland',
		// smooth stone (kept OUT of the lavaproof stone bucket — gray)
		'smooth_stone', 'smooth_stone_slab',
		// crystal (amethyst — pack already had the blocks)
		'amethyst_block', 'budding_amethyst', 'amethyst_cluster', 'amethyst_shard',
		'small_amethyst_bud', 'medium_amethyst_bud', 'large_amethyst_bud',
		// (dripstone_block / calcite / tuff -> lavaproof via #create:stone_types/*)
		// ice & snow — melt, don't vanish, in open flame (gray)
		'ice', 'packed_ice', 'blue_ice', 'snow', 'snow_block',
		// stone/metal redstone components
		'comparator', 'repeater', 'observer', 'piston', 'sticky_piston', 'dispenser', 'dropper',
		'lever', 'tripwire_hook', 'daylight_detector',
		'heavy_weighted_pressure_plate', 'light_weighted_pressure_plate',
		// deep-dark growth (gray — non-flammable, not lava-tough)
		'sculk', 'sculk_vein', 'sculk_catalyst', 'sculk_sensor', 'sculk_shrieker',
		// burn-transform end products — survive fire, still melt in lava
		'dirty_glass', 'dirty_glass_pane', 'dirty_shard', 'ash',   // -> quark:* / supplementaries:ash via the dict
	].concat(COLORS.map(c => c + '_concrete_powder'), COPPER)))

	// ═════════════════════════════ LAVAPROOF ═══════════════════════════════
	// immune to BOTH open fire and lava — wins over fireproof
	physicsFromTag(e, 'lavaproof', [
		// (pack originals)
		'#forge:stone', '#forge:cobblestone', '#forge:ores', '#create:sleepers',
		'#create:stone_types/granite', '#create:stone_types/diorite', '#create:stone_types/andesite',
		'#create:stone_types/calcite', '#create:stone_types/dripstone', '#create:stone_types/deepslate',
		'#create:stone_types/tuff', '#create:stone_types/limestone', '#create:stone_types/veridium',
		'#create:stone_types/asurine', '#create:stone_types/crimsite', '#create:stone_types/ochrum',
		'#create:stone_types/scoria', '#create:stone_types/scorchia',
		// anchors
		'#forge:obsidian', '#forge:end_stones', '#forge:netherrack',
		'#forge:gems/diamond', '#forge:storage_blocks/diamond',
		'#forge:ingots/netherite', '#forge:storage_blocks/netherite', '#forge:nuggets/netherite',
		'#minecraft:shulker_boxes', '#minecraft:soul_fire_base_blocks',
	])
	add('lavaproof', mc([
		// (pack originals — modded dragon / felsic endgame gear)
		// (kept exactly as before, see the dragonloot / felsic section further down)

		// ── tough / volcanic / deep stone ──
		'obsidian', 'crying_obsidian', 'respawn_anchor', 'reinforced_deepslate',
		'ancient_debris', 'bedrock', 'magma_block', 'basalt', 'polished_basalt', 'smooth_basalt',
		'calcite', 'tuff', 'dripstone_block', 'pointed_dripstone',            // also via #create:stone_types/*
		'petrified_oak_slab',                                                 // it's a STONE slab
		// vanilla stone-brick / polished shapes (base blocks already in #forge:stone)
		'stone', 'stone_slab', 'stone_stairs', 'stone_button', 'stone_pressure_plate',
		'stone_bricks', 'chiseled_stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks',
		'stone_brick_slab', 'stone_brick_stairs', 'stone_brick_wall',
		'mossy_stone_brick_slab', 'mossy_stone_brick_stairs', 'mossy_stone_brick_wall',
		'cobblestone', 'cobblestone_slab', 'cobblestone_stairs', 'cobblestone_wall',
		'mossy_cobblestone', 'mossy_cobblestone_slab', 'mossy_cobblestone_stairs', 'mossy_cobblestone_wall',
		'infested_stone', 'infested_cobblestone', 'infested_stone_bricks', 'infested_chiseled_stone_bricks',
		'infested_cracked_stone_bricks', 'infested_mossy_stone_bricks', 'infested_deepslate',
		'andesite_slab', 'andesite_stairs', 'andesite_wall',
		'granite_slab', 'granite_stairs', 'granite_wall',
		'diorite_slab', 'diorite_stairs', 'diorite_wall',
		'polished_andesite_slab', 'polished_andesite_stairs',
		'polished_granite_slab', 'polished_granite_stairs',
		'polished_diorite_slab', 'polished_diorite_stairs',
		// deepslate
		'deepslate', 'chiseled_deepslate',
		'cobbled_deepslate_slab', 'cobbled_deepslate_stairs', 'cobbled_deepslate_wall',
		'polished_deepslate_slab', 'polished_deepslate_stairs', 'polished_deepslate_wall',
		'deepslate_brick_slab', 'deepslate_brick_stairs', 'deepslate_brick_wall',
		'deepslate_tile_slab', 'deepslate_tile_stairs', 'deepslate_tile_wall',
		'deepslate_bricks', 'deepslate_tiles', 'cracked_deepslate_bricks', 'cracked_deepslate_tiles',
		// blackstone
		'blackstone', 'gilded_blackstone', 'chiseled_polished_blackstone',
		'polished_blackstone', 'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks',
		'polished_blackstone_button', 'polished_blackstone_pressure_plate',
		'blackstone_slab', 'blackstone_stairs', 'blackstone_wall',
		'polished_blackstone_slab', 'polished_blackstone_stairs', 'polished_blackstone_wall',
		'polished_blackstone_brick_slab', 'polished_blackstone_brick_stairs', 'polished_blackstone_brick_wall',
		// end stone
		'end_stone', 'end_stone_bricks',
		'end_stone_brick_slab', 'end_stone_brick_stairs', 'end_stone_brick_wall',
		// ── nether ──
		'netherrack', 'nether_gold_ore', 'nether_quartz_ore',
		'nether_bricks', 'nether_brick', 'nether_brick_fence',
		'chiseled_nether_bricks', 'cracked_nether_bricks', 'red_nether_bricks',
		'nether_brick_slab', 'nether_brick_stairs', 'nether_brick_wall',
		'red_nether_brick_slab', 'red_nether_brick_stairs', 'red_nether_brick_wall',
		// ── quartz (all of it) ──
		'quartz', 'quartz_block', 'chiseled_quartz_block', 'quartz_pillar', 'quartz_bricks',
		'smooth_quartz', 'quartz_slab', 'quartz_stairs', 'smooth_quartz_slab', 'smooth_quartz_stairs',
		// ── diamond (all of it) ──
		'diamond', 'diamond_block',
		'diamond_sword', 'diamond_pickaxe', 'diamond_axe', 'diamond_shovel', 'diamond_hoe',
		'diamond_helmet', 'diamond_chestplate', 'diamond_leggings', 'diamond_boots', 'diamond_horse_armor',
		// ── netherite (vanilla — patcherMod seeds most of this too) ──
		'netherite_ingot', 'netherite_scrap', 'netherite_block',
		'netherite_sword', 'netherite_pickaxe', 'netherite_axe', 'netherite_shovel', 'netherite_hoe',
		'netherite_helmet', 'netherite_chestplate', 'netherite_leggings', 'netherite_boots',
		// ── prismarine building (NOT sea_lantern) ──
		'prismarine', 'prismarine_bricks', 'dark_prismarine',
		'prismarine_slab', 'prismarine_stairs', 'prismarine_wall',
		'prismarine_brick_slab', 'prismarine_brick_stairs',
		'prismarine_shard', 'prismarine_crystals',
		// ── beacon / lodestone / compass / other end & nether keepsakes ──
		'beacon', 'nether_star', 'end_crystal', 'conduit', 'heart_of_the_sea',
		'dragon_egg', 'dragon_head', 'wither_skeleton_skull',
		'lodestone', 'compass', 'recovery_compass', 'echo_shard',
		'ender_chest', 'enchanting_table',
		// ── fire-immune mob drops ──
		'magma_cream', 'blaze_rod', 'blaze_powder', 'ghast_tear',
		// shulker shell (boxes handled by the tag)
		'shulker_shell',
	]))

	// ── modded dragon / felsic endgame gear (pack originals, unchanged) ──────
	add('lavaproof', [
		i.echo_helmet, i.echo_chestplate, i.echo_leggings, i.echo_boots, i.echo_sword,
		i.echo_pickaxe, i.echo_axe, i.echo_shovel, i.echo_hoe,
		i.ingot_felsic, i.block_felsic, i.scrap_felsic,
		i.felsic_sword, i.felsic_pickaxe, i.felsic_axe, i.felsic_shovel, i.felsic_hoe,
		i.felsic_helmet, i.felsic_chestplate, i.felsic_leggings, i.felsic_boots,
		i.dragon_sword, i.dragon_pickaxe, i.dragon_axe, i.dragon_shovel, i.dragon_hoe,
		i.dragon_bow, i.dragon_crossbow, i.dragon_trident,
		i.dragon_helmet, i.dragon_chestplate, i.dragon_leggings, i.dragon_boots,
		i.upgraded_dragon_chestplate, i.dragon_horse_armor, i.dragon_anvil,
		i.dragonloot_dragon_scale,
		i.netherite_bars, i.ore_netherite, i.debris, i.raw_netherite, i.fragment_netherite,
		i.nugget_netherite, i.ingot_netherite, i.block_netherite,
		i.plate_netherite, i.plate_block_netherite, i.component_netherite,
	])

	// modded fireproof blocks the pack already declared (unchanged)
	add('fireproof', [
		i.block_copper, i.block_iron, i.block_gold, i.block_zinc,
		i.block_aluminum, i.block_lead, i.block_silver, i.block_nickel, i.block_uranium,
		i.block_cast_iron, i.block_rose_gold, i.block_brass, i.block_steel, i.block_electrum, i.block_constantan,
	])

	// ═══════════════════════════════ SINKS ═════════════════════════════════
	// force-sink — only matters where something else floats them; also self-documenting
	add('sinks', mc([
		'poisonous_potato',                                     // you asked for this one specifically
		'gunpowder', 'redstone', 'glowstone_dust', 'blaze_powder',   // heavy powders
		'ender_pearl', 'ender_eye',
		'blaze_rod',
	]))

	// ═══════════════════════ BURN-TRANSFORM BUCKETS ════════════════════════
	// Consumed by patcherMod ItemBurnTransform when fire/lava destroys a dropped item.
	// Membership is reload-able here; the OUTCOME (result item / chance / blast power)
	// is wired in startup_scripts/func/item_physics.js and needs a restart to change.
	//   burn/to_ash     → chance to leave supplementaries:ash (rolled per item in the stack)
	//   burn/explosive  → non-block-breaking explosion (spare bucket; tnt/gunpowder/tnt_minecart
	//                     already have their own per-id rules with tuned power)
	// Glass → quark:dirty_glass is driven straight off #forge:glass / #forge:glass_panes
	// in the startup script, so it needs no bucket here.
	t.add(e, 'creativeengineering:burn/to_ash', [
		'#minecraft:planks', '#minecraft:logs',
		'#minecraft:wooden_slabs', '#minecraft:wooden_stairs', '#minecraft:wooden_fences',
		'#forge:fence_gates/wooden', '#minecraft:wooden_doors', '#minecraft:wooden_trapdoors',
		'#minecraft:wooden_buttons','#minecraft:wooden_pressure_plates','#minecraft:signs',
		'#minecraft:boats','#minecraft:banners','#forge:chests/wooden','#forge:rods/wooden',
		'#create:scrap_wood_small', '#create:scrap_wood_medium', '#create:scrap_wood_large',
		'#minecraft:beds','#create:scrap_wood_and_book'
	])
	// t.add(e, 'creativeengineering:burn/explosive', ['minecraft:fire_charge'])
})

console.log('item physics tags finished')
