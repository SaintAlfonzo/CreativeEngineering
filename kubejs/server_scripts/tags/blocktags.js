console.log('block tags starting')

var t = global.tags
var b = global.blocks
var item = global.item

ServerEvents.tags('block', e => {
	// Create D&D 1.2c Mid-Dev ships data/minecraft/tags/blocks/mineable/axe.json naming 7 creative_*
	// blocks that jar never registers. One missing required entry makes vanilla drop the WHOLE merged
	// tag, so no axe was effective on anything (wood mined at fist speed, WTHIT showed no tool).
	// KubeJS edits the raw entry lists before vanilla resolves them, so stripping the ghosts here works.
	// Literal ids on purpose: these blocks don't exist, so they have no item_dict/block_dict key.
	t.remove(e, 'minecraft:mineable/axe', [
		'create_dd:creative_encased_shaft', 'create_dd:creative_encased_cogwheel',
		'create_dd:creative_encased_large_cogwheel', 'create_dd:creative_encased_fluid_pipe',
		'create_dd:creative_depot', 'create_dd:creative_mixer', 'create_dd:creative_press',
	])

	t.add(e, 'minecraft:fire', b.dark_fire)

	// Layered Obsidian (startup_scripts/items/decor_blocks.js): mined like obsidian
	t.mineable(e, 'pickaxe', 'diamond', item.layered_obsidian)


	// Oven heat sources and the oven's stove heat and pickaxe tags: Creative Delight
	// (creativeengineering/kitchen_tags.js). Monster box, Rubidium blendable, campfire rain and no_world_dye tags:
	// Creative Compat (creativecompat/kubejs/tags/blocktags.js). Bakeware hammering (cutting board): Creative Delight
	// (creativeengineering/kitchen_tags.js). Counter supports / toppers (Cooking for Blockheads): Creative Delight
	// cookingforblockheads/kitchen.js. Villager ladders, structure ground match, earth fill, template trim and wild plant
	// soil tags: Creative Worldgen (creativeworldgen/kubejs/tags/blocktags.js).

	// Keg temperature + Vinery cold storage (Brewin port BnCHeatSources; Vinery WineYears.storageKind). The keg tag
	// already holds FD heat_sources; these only warm kegs, they don't let FD pots cook. Running gates are in
	// BnCHeatSources: lit/active/on must be true, blaze heaters (burner, liquid burner, enchanter) need fuel. Skipped
	// for now: machines whose running state lives only in the block entity (IE arc furnace, advanced blast furnace,
	// preheater; PneumaticCraft heat blocks; Create steam engines/boilers; Compressed Creativity heater, D&D furnace
	// engine). The Cooking for Blockheads oven gets a `lit` state from Creative Delight and comes in through FD heat_sources.
	t.add(e, b.t_keg_heat_sources, [
		b.furnace, b.blast_furnace, b.smoker, b.quark_deepslate_furnace, b.quark_blackstone_furnace,
		b.keypad_furnace, b.keypad_blast_furnace, b.keypad_smoker,
		b.bonfire, b.soul_bonfire, b.brazier, b.soul_brazier, b.siege_brazier_on,
		b.ie_coke_oven, b.ie_blast_furnace, b.ie_alloy_smelter, b.ie_furnace_heater,
		b.small_lab_furnace, b.small_electrical_furnace, b.small_waste_incinerator, b.clayworks_kiln,
		b.air_compressor, b.advanced_air_compressor, b.vibration_chamber,
		b.liquid_blaze_burner, b.blaze_enchanter, b.botania_blaze_block,
	])
	// #minecraft:ice nests the port's ice / packed / blue and adds frosted and rare ice
	t.add(e, b.t_keg_freeze_sources, [
		b.snow, b.snow_block, b.powder_snow, b.thin_ice, b.iw_permafrost, b.iw_cryosol,
		b.t_ice, b.polished_packed_ice, b.chiseled_packed_ice,
	])

	// Ore Excavation undo (Creative Common excavation/VeinUndo.java, CEConfig [vein_undo]). An undo whose vein mine broke
	// any of these is refused; blocks with a block entity are refused in code (a restored chest rerolls its loot).
	t.add(e, b.t_vein_undo_denied, [b.t_ores, b.t_storage_blocks, item.ore_netherite, item.spawner])
	t.addMatching(e, b.t_vein_undo_denied, '*:budding_*')

	// Emu, duck and goose nests: Incubation Additions tags them into incubation:bird_nests itself

	// minecraft:concrete isn't a vanilla tag in 1.19.2 — the 16 solid concretes (not powder)
	t.addMatching(e, 'forge:concrete', 'minecraft:*_concrete')
	t.addMatching(e, 'forge:bricks', [item.bricks, 'minecraft:brick_*'])
	t.addMatching(e, 'forge:stone_bricks', [item.stone_bricks, 'minecraft:stone_brick_*'])

	// ── BlockRunner walk speed ──────────────────────────────────────────────
	// Multipliers live in config/creativeengineering-common.toml [surface.speed], one key per tier
	// (the numbers below are the defaults, fastest -> slowest). Creative Common's SurfaceTier enum lists the
	// tiers for that config, so a new tier needs an entry there too; blockrunner.json only keeps one-offs.
	// BlockRunner reads the block ~0.2 below your feet (vanilla getOnPos), so carpets, 1–2 layer
	// snow, pressure plates and IW leaf piles never register (the block under them does).
	// A tiered block's native speed factor is replaced (IP asphalt's 1.2, honey's 0.4); soul sand/soil
	// are left out on purpose: tiering them would stop Soul Speed from canceling the slowdown.
	// One tier per block — BlockRunner picks a random one on each reload otherwise (see overlaps below).
	var runner = {
		asphalt:            'blockrunner:surface/asphalt',            // 1.50
		concrete:           'blockrunner:surface/concrete',           // 1.40
		grating:            'blockrunner:surface/grating',            // 1.30
		metal:              'blockrunner:surface/metal',              // 1.25
		slippery:           'blockrunner:surface/slippery',           // 1.25
		constructed_smooth: 'blockrunner:surface/constructed_smooth', // 1.25
		constructed_brick:  'blockrunner:surface/constructed_brick',  // 1.25
		natural_path:       'blockrunner:surface/natural_path',       // 1.15
		wood:               'blockrunner:surface/wood',               // 1.10
		natural_smooth:     'blockrunner:surface/natural_smooth',     // 1.05
		machinery:          'blockrunner:surface/machinery',          // 1.00
		functional:         'blockrunner:surface/functional',         // 1.00
		decorative:         'blockrunner:surface/decorative',         // 1.00
		furniture:          'blockrunner:surface/furniture',          // 1.00
		flora:              'blockrunner:surface/flora',              // 1.00
		camouflage:         'blockrunner:surface/camouflage',         // 1.00
		glass:              'blockrunner:surface/glass',              // 1.00
		dirt:               'blockrunner:surface/dirt',               // 1.00
		natural_rough:      'blockrunner:surface/natural_rough',      // 0.92
		soft:               'blockrunner:surface/soft',               // 0.88
		dust:               'blockrunner:surface/dust',               // 0.85
		confectionery:      'blockrunner:surface/confectionery',      // 0.85
		tilled:             'blockrunner:surface/tilled',             // 0.80
		leaves:             'blockrunner:surface/leaves',             // 0.78
		snow:               'blockrunner:surface/snow',               // 0.75 (snow block, powder snow; layers: pass-through)
		sticky:             'blockrunner:surface/sticky',             // 0.65
		spongy:             'blockrunner:surface/spongy',             // 0.60
		very_sticky:        'blockrunner:surface/very_sticky',        // 0.45
	}
	// BlockRunner's basic tiers stay empty for one-off overrides; wipe the jar's seeds
	// (dirt_path, #minecraft:stone_bricks) — both now live in a surface tag.
	var basicTiers = ['very_quick', 'quick', 'slightly_quick', 'slightly_slow', 'slow', 'very_slow']
		.map(n => 'blockrunner:' + n + '_blocks')
	t.wipeTag(e, basicTiers)

	// Create/D&D palette stones: <stone>, cut_<stone>, polished_cut_<stone>, layered_<stone>,
	// <stone>_pillar, cut_<stone>_bricks, small_<stone>_bricks (+ _slab/_stairs/_wall).
	var palette = '(create|create_dd):'

	t.addMatching(e, runner.asphalt, [
		'#forge:asphalt', 'immersivepetroleum:asphalt_*', 'create_dd:*_asphalt_block',
	])

	t.addMatching(e, runner.concrete, [
		item.t_concrete, 'immersiveengineering:*concrete*', 'engineersdecor:*rebar_concrete*',
		'immersiveengineering:*hempcrete*', 'dustrial_decor:*cinder*',
	])

	t.addMatching(e, runner.slippery, [
		b.t_ice, item.soap_block, 'architects_palette:*packed_ice*',
		item.thin_ice,
	])

	t.addMatching(e, runner.constructed_smooth, [
		// vanilla: polished / smooth / cut stones, quartz, glazed terracotta, dark prismarine
		'minecraft:polished_*', 'minecraft:smooth_*', 'minecraft:cut_*sandstone*', 'minecraft:chiseled_*sandstone',
		item.chiseled_polished_blackstone, 'minecraft:*_glazed_terracotta', 'minecraft:dark_prismarine*',
		'/^minecraft:(chiseled_)?quartz_(block|pillar|slab|stairs)$/',
		item.smooth_salt_block, item.salt_pillar,   // Vintage Delight salt decor
		item.t_storage_blocks_diamond, '#forge:storage_blocks/emerald', '#forge:storage_blocks/lapis',
		'#forge:storage_blocks/redstone', '#forge:storage_blocks/quartz',
		// quark
		'quark:polished_*', 'quark:smooth_*', 'quark:cut_*sandstone*', item.chiseled_soul_sandstone, 'quark:*_pillar',
		item.quartz_vertical_slab, item.dark_prismarine_vertical_slab, 'quark:midori_block*',
		'quark:duskbound_block*', item.sturdy_stone,
		// supplementaries: checkered tiles, wattle-and-daub plaster
		'supplementaries:checker_*', 'supplementaries:daub*', item.flint_block,
		'/^suppsquared:daub(_frame)?_[a-z_]+$/',
		// Create/D&D palette
		'/^' + palette + '(polished_cut|layered)_[a-z_]+$/', '/^' + palette + 'cut_(?!.*brick)[a-z_]+$/',
		'/^' + palette + '[a-z_]+_pillar$/', item.rose_quartz_block, 'create_compressed:rose_quartz_*_block',
		item.create_dd_hazard_block, item.layered_obsidian, item.echo_block,
		// architect's palette: crafted stones, chiseled/pillar forms, polished glowstone and nebulite
		'/^architects_palette:(abyssaline|hadaline)(_pillar|_plating)?$/',
		'/^architects_palette:(oracle|cerebral|entwine|wardstone|hazard)(_block|_pillar|_slab|_stairs|_vertical_slab|_wall)?$/',
		'/^architects_palette:gilded_sandstone(_pillar|_slab|_stairs|_vertical_slab)?$/',
		'/^architects_palette:polished_(glowstone|nebulite)(_slab|_stairs|_vertical_slab|_wall)?$/',
		'/^architects_palette:chiseled_(calcite|dripstone|entwine|gilded_sandstone|moonshale|olivestone|tuff|wardstone)$/',
		'/^architects_palette:(calcite|dripstone|tuff|esoterrack|onyx|olivestone|osseous|withered_osseous|flint)_pillar$/',
		'/^architects_palette:(flint_block|bordered_moonshale|illuminated_olivestone|runic_glowstone|ender_pearl_block|framed_oracle_block|sunstone|moonstone)$/',
		// botania: quartz family, pavement, azulejo, polished livingrock, shimmerrock, corporea, gem blocks
		'/^botania:(chiseled_)?[a-z]+_quartz(_pillar|_slab|_stairs)?$/', 'botania:*_pavement*', 'botania:azulejo_*',
		'/^botania:polished_livingrock(_slab|_stairs|_wall)?$/', '/^botania:shimmerrock(_slab|_stairs)?$/',
		'/^botania:corporea_(block|slab|stairs)$/', item.blaze_block, item.mana_diamond_block, item.dragonstone_block,
		// AE2 quartz / fluix / smooth sky stone (not the quartz glass: that's glass)
		'/^ae2:(chiseled_|cut_|smooth_)?quartz_(block|slab|stairs|wall)$/', '/^ae2:quartz_pillar(_slab|_stairs|_wall)?$/',
		'/^ae2:fluix_(block|slab|stairs|wall)$/', '/^ae2:smooth_sky_stone_(block|slab|stairs|wall)$/',
		// the rest: crystal quartz, polished/cut/column stones, tiled floors, lab blocks
		'/^securitycraft:(reinforced_)?(chiseled_|smooth_)?crystal_quartz(_block|_pillar|_slab|_stairs)?$/',
		'/^twigs:polished_(?![a-z_]*brick)[a-z_]+$/', item.cut_amethyst, 'twigs:*_column',
		'/^pneumaticcraft:(compressed|reinforced)_stone(_slab)?$/', 'geode:smooth_end_stone*', item.clayworks_glazed_terracotta,
		'cookingforblockheads:*_kitchen_floor', item.station_platform, item.stone_pillar,
		item.dark_prismarine_wall, item.frozen_dark_prismarine, item.redstone_block_off,
		'/^davebuildingmod:(cut_)?laboratory_block$/', item.duroplast,
	], { except: ['minecraft:polished_*brick*', item.iron_pillar, 'quark:polished_*brick*'] })

	// grating: see-through walkway floors (catwalks, grates, metal scaffolding tops)
	t.addMatching(e, runner.grating, [
		item.grate, 'everythingcopper:*copper_grate', 'design_decor:*_catwalk', item.ornate_grate,
		'createdeco:*_catwalk', 'createdeco:*_catwalk_stairs', 'create:*_scaffolding', item.steel_mesh_trapdoor,
		'/^immersiveengineering:(slab_|stairs_)?(alu|steel)_scaffolding_(grate_top|standard)$/',
		'engineersdecor:steel_catwalk*', item.steel_floor_grating,
		'/^dustrial_decor:(chain|iron_bar)_trapdoor$/',
	])

	t.addMatching(e, runner.metal, [
		'#forge:storage_blocks/iron', item.any_block_gold, '#forge:storage_blocks/copper',
		item.t_storage_blocks_netherite, '#forge:storage_blocks/steel', '#forge:storage_blocks/aluminum',
		'#forge:storage_blocks/lead', '#forge:storage_blocks/silver', '#forge:storage_blocks/nickel',
		'#forge:storage_blocks/constantan', '#forge:storage_blocks/electrum', '#forge:storage_blocks/uranium',
		'#forge:storage_blocks/brass', '#forge:storage_blocks/zinc', '#forge:storage_blocks/compressed_iron',
		'#forge:storage_blocks/andesite_alloy', '#forge:sheetmetals', item.sheetmetal_slabs,
		// every oxidation / waxed stage of copper and cut copper
		'minecraft:*copper', 'minecraft:*copper_block', 'minecraft:*cut_copper*', item.iron_trapdoor,
		'quark:*cut_copper*', 'quark:*iron_plate*', item.iron_pillar,
		'immersive_weathering:*plate_iron*', 'immersive_weathering:*cut_iron*', 'immersive_weathering:*iron_trapdoor',
		item.block_industrial_iron, '/^create:(waxed_)?((exposed|weathered|oxidized)_)?copper_(shingle|tile)(s|_slab|_stairs)$/',
		'/^everythingcopper:(waxed_)?((exposed|weathered|oxidized)_)?(chiseled_copper|copper_trapdoor)$/',
		'/^twigs:(waxed_)?((exposed|weathered|oxidized)_)?copper_pillar$/',
		// modded metal blocks, plates, sheets, bricks and fences
		'creativeengineering:*_plate_block', 'creativeengineering:coil_*', 'immersiveengineering:coil_*',
		// a sealed tin; the opened one shows dirt on top and stays untiered (1.00, like dirt)
		'canned_goods:canned_dirt',
		'/^immersiveengineering:slab_storage_[a-z]+$/', '/^immersiveengineering:(alu|steel)_fence$/',
		'/^immersiveposts:fence_[a-z]+$/', '@metalfences', '/^gates:iron_fence(_gate)?$/',
		'/^supplementaries:((gold|lead|netherite|silver)_trapdoor|(gold|iron)_gate)$/',
		'/^architects_palette:(chiseled_|cut_|smooth_)?(nether_brass|sunmetal|ancient_plating|plating|sheet_metal|tread_plate)(_block|_pillar|_fence|_slab|_stairs|_vertical_slab|_wall)?$/',
		item.unobtanium_block, item.block_manasteel, item.block_terrasteel, item.block_elementium,
		item.vanadium_block, item.block_felsic, item.block_rose_gold, 'create_dd:dark_metal_*',
		'createdeco:*_sheet_*', 'createdeco:*_hull', item.block_cast_iron, 'createdeco:*_mesh_fence', 'createdeco:*_trapdoor',
		'/^create_things_and_misc:(brass_(brick|tile)[a-z_]*|sturdy_sheet_[a-z_]+)$/',
		'create_compressed:*_sheet_block', '/^create_compressed:(shaft_bundle|mechanism_block)$/',
		'/^design_decor:([a-z_]+_)?metal_(sheet|plate)[a-z_]*$/', 'design_decor:*_floor', 'design_decor:*_railing',
		'design_decor:*_boiler*', '/^design_decor:(industrial_gold_block|industrial_plating_block|capitalism_block|zinc_checker_tiles)$/',
		'/^dustrial_decor:(bolted_industrial_iron|cast_iron|engraved_cast_iron|industrial_iron|rusty_iron|(rusty_)?sheet_metal)_(?!door$)[a-z_]+$/',
		'/^dustrial_decor:(wrapped_(gold_)?chains|chain_link_fence)$/',
		'/^davebuildingmod:([a-z_]+_)?steel_(block|plating|seamless|slab|stairs|wall|trapdoor|pillar|boiler|buffer|dark_buffer|funnel|pilot|steam_exhaust|tank)$/',
		'/^davebuildingmod:[a-z_]*boiler$/',
		'engineersdecor:steel_mesh_fence*', item.steel_railing, item.iron_hatch,
		'railways:*locometal*', '/^bellsandwhistles:((corrugated_)?metro_casing|(metro|ornate_iron)_trapdoor)$/',
		'/^sootychimneys:(dirty_)?(copper|iron)_chimney$/', '/^securitycraft:(reinforced_fence_gate|electrified_iron_fence)$/',
	], { except: [item.raw_block_copper] })

	t.addMatching(e, runner.constructed_brick, [
		'#minecraft:stone_bricks', '#forge:bricks', '#forge:stone_bricks',
		// vanilla: every *brick* family, deepslate tiles, prismarine, purpur
		'minecraft:*brick*', 'minecraft:*deepslate_tile*', item.chiseled_deepslate,
		'/^minecraft:prismarine(_slab|_stairs|_wall)?$/', 'minecraft:purpur_*',
		// quark: bricks (incl. raw ore/dirt/cobblestone bricks), roof shingles, vertical slabs
		'quark:*brick*', 'quark:*shingles*', 'quark:*deepslate_tile*', item.prismarine_vertical_slab,
		item.purpur_vertical_slab,
		'supplementaries:*brick*', 'supplementaries:*_tile*', 'immersive_weathering:*brick*',
		'immersive_weathering:*deepslate_tile*',
		'/^' + palette + '(cut|small)_[a-z_]+_brick(s|_slab|_stairs|_wall)$/', 'create:*rose_quartz_tiles',
		// modded bricks, tiles and roof shingles
		'architects_palette:*brick*', 'architects_palette:*_tile*', item.moonshale_flagstone,
		'botania:*brick*', 'ae2:*brick*', 'clayworks:*brick*', 'createdeco:*brick*', 'pneumaticcraft:*brick*',
		'twigs:*brick*', 'twigs:*shingle*', 'design_decor:*castel*', 'design_decor:*stone_tiles', '@mosaic_masonry',
		'vintagedelight:*brick*',   // Vintage Delight salt bricks and mixed salt bricks
		'/^immersiveengineering:(slab_)?(alloybrick|blastbrick|blastbrick_reinforced|cokebrick)$/',
		'engineersdecor:clinker_brick_*', 'engineersdecor:slag_brick_*', 'engineersdecor:dark_shingle_roof*',
		'/^buzzier_bees:(chiseled_)?honeycomb_(brick|tile)[a-z_]*$/', '/^securitycraft:(reinforced_)?crystal_quartz_bricks$/',
		'philipsruins:*brick*', 'philipsruins:*_tiles', item.frozen_prismarine,
		'/^sootychimneys:(dirty_)?[a-z_]*brick_chimney$/',
	])

	t.addMatching(e, runner.natural_path, [
		item.dirt_path, item.coarse_dirt, item.t_gravel, item.t_cobblestone,
		'minecraft:*cobble*', 'quark:*cobble*_vertical_slab', item.raked_gravel,
		'immersive_weathering:*cobblestone*', 'botania:metamorphic_*_cobblestone*',
		'/^davebuildingmod:(ballast|gravel|rocky_dirt)(_slab)?$/', item.twigs_rocky_dirt, item.coarse_dirt_slab, item.dirt_path_slab,
		'/^engineersdecor:dense_grit_(dirt|sand)_block$/', '/^sootychimneys:(dirty_)?cobblestone_chimney$/',
	], { except: ['quark:*cobblestone_bricks*'] })

	t.addMatching(e, runner.wood, [
		item.planks, item.wood_slab, item.any_wooden_stairs, item.any_log,
		'#forge:treated_wood', '#forge:treated_wood_slab',
		// fence and gate tops count; a trapdoor only when it sits in the top half of its block
		item.any_wooden_fences, item.t_fence_gates_wooden, item.any_wooden_trapdoors, item.scaffolding,
		'/^(architects_palette:twisted|botania:(dream|living)wood|immersive_weathering:charred|vinery:cherry)_fence_gate$/',
		item.any_wooden_vertical_slabs, 'quark:vertical_*_planks', '#quark:hollow_logs',
		item.log_bamboo, item.bamboo_mosaic, item.bamboo_mat, 'quark:*crate', item.cactus_block,
		item.log_bamboo_stripped, item.stick_block, item.sugar_cane_block, item.petrified_oak_slab,
		'supplementaries:timber_*',
		// modded boards, beams, palisades, trims, floorboards, log piles and wooden crates
		'architects_palette:*_board*', 'architects_palette:*_railing', item.vslab_twisted,
		'everycomp:ap/*_board*', 'everycomp:ap/*_railing', 'everycomp:db/*_beam', 'everycomp:db/*_palisade', 'everycomp:sd/*_trim',
		'decorative_blocks:*_beam', 'decorative_blocks:*_palisade', 'storagedrawers:*_trim',
		'/^botania:(stripped_)?(dream|living)wood_(fence|wall)$/', 'immersive_weathering:charred_*',
		'/^immersiveengineering:((slab_|stairs_)?(alu|steel)_scaffolding_wooden_top|treated_scaffold)$/',
		'/^engineersdecor:old_industrial_wood_(planks|slab|slabslice|stairs)$/',
		'/^davebuildingmod:(full_log_pile|log_palisade|log_pile_[12]_(bottom|top)|half_lumber_[a-z_]+)$/',
		'/^vinery:(cherry_(beam|floorboard)|stackable_log|[a-z_]*crate|[a-z_]*_wine_rack_(big|mid|small)|[a-z_]*_lattice)$/', '/^create_compressed:(large_)?cogwheel_block$/',
		'farmersdelight:*_crate', 'rusticdelight:*crate', 'delightful:*_crate', 'quarkdelight:*_fruit_crate',
		'fruitsdelight:*_crate', item.chorus_crate, item.corn_crate,
		'vintagedelight:*_crate',   // Vintage Delight cucumber, ghost pepper and peanut crates
		// Brewin' port: both copy oak planks. The coaster stays untiered: it is 1 px thin, so the block under it counts
		item.heating_cask, item.ice_crate,
	], { except: ['/^(copycats|framedblocks):/'] }) // their copycat/framed trapdoors are camouflage

	t.addMatching(e, runner.natural_smooth, [
		// raw stone + its slab/stairs/wall (vanilla, then quark's stones and vertical slabs)
		'/^minecraft:(stone|granite|diorite|andesite|tuff|calcite|deepslate|blackstone|sandstone|red_sandstone|end_stone|dripstone_block)(_slab|_stairs|_wall)?$/',
		'/^quark:(stone|granite|diorite|andesite|tuff|calcite|dripstone_block|blackstone|sandstone|red_sandstone|jasper|limestone|shale|myalite|permafrost|soul_sandstone)(_slab|_stairs|_wall|_vertical_slab)?$/',
		'/^minecraft:infested_(stone|deepslate)$/', item.gilded_blackstone, item.reinforced_deepslate,
		item.t_terracotta, 'minecraft:*_ore', b.t_ores, item.obsidian, item.crying_obsidian,
		item.amethyst_block, item.budding_amethyst, item.packed_mud, item.bone_block,
		item.bedrock, item.dusky_myalite, 'quark:*corundum',
		'/^create:(asurine|crimsite|limestone|ochrum|scorchia|scoria|veridium)$/',
		'/^create_dd:(breccia|dolomite|gabbro|weathered_limestone)$/',
		// modded raw stones, crystal blocks, terracotta shapes and bone blocks
		'/^architects_palette:(myonite|moonshale|craterstone|esoterrack|onyx|nebulite|warpstone)(_slab|_stairs|_vertical_slab|_wall)?$/',
		'/^architects_palette:(twisting|weeping)_blackstone$/', item.withered_bone_block,
		'/^botania:livingrock(_slab|_stairs|_wall)?$/', 'botania:metamorphic_*_stone*',
		'/^ae2:sky_stone_(block|slab|stairs|wall)$/', 'ae2:*budding_quartz',
		'/^twigs:(bloodstone|rhyolite|schist|calcite|tuff)(_slab|_stairs|_wall)?$/', item.compacted_dripstone, 'twigs:*packed_silt',
		'/^immersive_weathering:(mossy_|sandy_|snowy_)?stone(_slab|_stairs|_wall|_vertical_slab)?$/',
		'/^clayworks:([a-z_]+_)?terracotta(_slab|_stairs|_wall|_vertical_slab)$/', '/^sootychimneys:(dirty_)?terracotta_chimney$/',
		'/^geode:([a-z_]+_block|budding_[a-z_]+|galcite)$/',
		'/^philipsruins:(bone_chunk_block|cracked_bone|sculk_bone_block)$/',
	], { except: ['minecraft:nether_*_ore', item.ore_netherite] })

	// camouflage: blocks that copy another block's look, so a speed of their own would be arbitrary
	t.addMatching(e, runner.camouflage, [
		'@framedblocks', '@copycats', item.copycat_panel, item.copycat_step,
	], { except: [item.framing_saw, '/^framedblocks:framed_(glowing_)?item_frame$/'] })

	t.addMatching(e, runner.glass, [
		item.any_glass, item.tinted_glass, 'ae2:quartz_glass', 'ae2:quartz_vibrant_glass', item.rainbow_glass,
		'create_crystal_clear:*_glass_casing', item.reinforced_glass, 'engineersdecor:panzerglass_*',
		'/^immersiveengineering:(slab_)?insulating_glass$/', item.slag_glass,
		'securitycraft:reinforced_*glass',
		// full-block windows, glass trapdoors (top half only), glassy crystal blocks
		item.sea_lantern, 'create:*_window', 'everycomp:c/*_window', item.framed_glass_trapdoor,
		'createframed:*_glass_trapdoor', 'create_crystal_clear:*glass_encased*', item.steel_framed_window,
		item.metro_window, item.window, item.window_block, '/^immersive_weathering:(vitrified_sand|frosty_glass)$/',
		'/^buzzier_bees:crystallized_(clover_)?honey_block$/', item.crystalized_banana_slug_mucus,
	])

	t.addMatching(e, runner.natural_rough, [
		b.raw_rock_salt_block,   // raw salt crystals, like raw stone (salt.md 6 S3)
		item.podzol, item.mycelium, item.crimson_nylium, item.warped_nylium,
		item.netherrack, item.basalt, 'minecraft:nether_*_ore', 'supplementaries:ashen_basalt',
		'minecraft:*coral_block', item.mangrove_roots,
		// raw ore chunks, ancient debris, and crumbly fuel / mineral blocks
		'minecraft:raw_*_block', item.raw_block_zinc, 'immersiveengineering:raw_block_*', item.ore_netherite,
		item.coal_block, item.glowstone_block, item.charcoal_block, item.architects_palette_charcoal_block,
		'/^immersiveengineering:(slab_)?coke$/', item.petcoke_block, item.sulfur_block, item.biomass_pellet_block,
		// Immersive Weathering soils
		'/^immersive_weathering:(silt|humus|fluvisol|vertisol|cryosol|permafrost|mulch_block|nulch_block|cracked_mud)$/',
		// other loose ground: silt, lichen, compost, anthills, niter beds
		item.silt, item.petrified_lichen, item.compost, item.soul_compost,
		item.leafcutter_anthill, item.niter_bedding,
	])

	t.addMatching(e, runner.soft, [
		item.any_wool, '#forge:mushroom_caps', '#forge:mushroom_stems', '#minecraft:wart_blocks',
		item.sponge, item.hay_bale, item.target, item.moss,
		item.dried_kelp_block, item.shroomlight, 'minecraft:*_froglight', item.honeycomb_block,
		'/^minecraft:sculk(_catalyst|_sensor|_shrieker)?$/', // not sculk_vein: no collision
		'quark:thatch*', 'quark:bonded_*', 'quark:*_sack', item.glow_shroom_block, item.glow_shroom_stem,
		item.feather_block, item.flax_block, item.fodder, item.sack,
		// beds (not full blocks, so no visual sink)
		item.t_beds, 'handcrafted:*_fancy_bed', 'everycomp:hc/*_fancy_bed', 'domesticationinnovation:pet_bed_*',
		// fabric, fur, padding, rubber, petals, thatch, wax, cardboard
		'/^create_dd:([a-z_]+_)?(velvet_block|padded_[a-z_]+|rubber_block)$/', item.belt_block,
		'/^dustrial_decor:(mini_)?padded_(?!door$)[a-z_]+$/', '/^dustrial_decor:(smooth_cardboard[a-z_]*|folded_cardboard|cardboard_box|cardboard_trapdoor)$/',
		'davebuildingmod:*_quilted_wool', item.bison_fur_block, 'botania:*_petal_block', 'twigs:bamboo_thatch*',
		item.paraffin_wax_block, item.weathered_cardboard_box,
		// sacks, bags and bales
		'suppsquared:sack_*', 'delightful:*_sack', 'rusticdelight:*_bag', 'vinery:*_bag', 'farmersrespite:*_sack', item.corn_kernel_bag,
		'/^farmersdelight:(rice_bag|rice_bale|straw_bale|tatami)$/',
		'/^vintagedelight:oat_(bag|bale)$/',
		// the one salt block (packed salt dust, food/seasoning #6) and Vintage's own; Raw Rock Salt Block is natural_rough
		item.salt_salt_block, item.salt_block,
	])

	t.addMatching(e, runner.dust, [
		item.t_sand, 'minecraft:*_concrete_powder', item.gravisand,
		item.ash,
		'/^immersive_weathering:(red_)?sand_layer_block$/', 'immersive_weathering:ash*',
		'creativeengineering:*_dust_pile', 'create_compressed:*_flour_pile', item.immersiveengineering_sawdust,
		'create_compressed:crushed_*_pile', item.powdered_obsidian_block,
		item.create_things_and_misc_powdered_obsidian_block, 'oldguns:*_powder_block',
	])

	// confectionery: edible building blocks and placed food
	t.addMatching(e, runner.confectionery, [
		'@create_confectionery', item.cake, item.sugar_cube,
		'/^architects_palette:(crustless_)?bread_(block|slab)$/', 'delightful:*_ice_cream_block', 'delightful:*_pie',
		'create_central_kitchen:*_pie', 'farmersdelight:*_pie', item.sweet_berry_cheesecake,
		'/^farmersdelight:(honey_glazed_ham|rice_roll_medley|roast_chicken|shepherds_pie|stuffed_pumpkin)_block$/',
		// gourds and fruit blocks (a hard rind: no visual sink), feasts and cakes
		item.melon_block, item.pumpkin, item.carved_pumpkin, item.jack_o_lantern, item.chorus_fruit_block,
		'/^delightful:(sliced_)?(cantaloupe|melon|mini_melon|pumpkin)$/', '/^brewinandchewin:(pizza|quiche)$/',
		'/^vintagedelight:(cheese|meat)_pizza$/', item.cheese_wheel,
		'/^createaddition:(chocolate|honey)_cake$/', '/^rusticdelight:[a-z_]*(pancakes|cheesecake)$/', item.chorus_pie,
		'/^goldenfood:(enchanted_)?golden_cake$/', item.hamimelon, item.coffee_cake, item.rose_hip_pie,
		item.fig_pudding, item.mangosteen_cake, item.pineapple_fried_rice, item.nachos_block, item.popcorn_box,
	])

	t.addMatching(e, runner.tilled, [item.farmland, item.rich_farmland, b.tilled_end_stone])

	t.addMatching(e, runner.leaves, [item.t_leaves, '#quark:hedges'])  // bamboo leaves: layered pass-through tier only

	// snow layers left out (plan_post_scratch_1115b.md D2): they slow by height in creativecompat:passthrough_layers
	t.addMatching(e, runner.snow, [b.snow_block, b.powder_snow, item.coarse_snow])

	t.addMatching(e, runner.spongy, [
		item.mud, item.muddy_mangrove_roots, item.wet_sponge, item.clay,
		item.dough_block, item.prepared_flesh_block,
		item.gingerdough_block, item.cookie_dough_block, item.sweet_potato_dough_block, item.oat_dough_block,
		item.cucumber_dough_block, item.sourdough_block,
		'creativedelight:*_culture_block',  // meat culture blocks (reserved for Creative Cultivation, creative_delight.md 29.5)
		'/^architects_palette:(rotten_flesh_block|entrails[a-z_]*)$/',
		b.flesh, // Visceral Heap (only registered with Creative Worldgen + Biomes O' Plenty)
	])

	// sticky: blocks with one sticky face. Piston heads are shared by plain and sticky pistons, so
	// Creative Common (StickyFacing) drops the speed unless facing=up and, for heads, type=sticky —
	// for everything in #creativecompat:blockrunner_face_up below.
	t.addMatching(e, runner.sticky, [
		item.sticky_piston, 'minecraft:piston_head', item.sticky_mechanical_piston,
		'create:mechanical_piston_head', 'create_things_and_misc:*sticky_launcher',
	])

	t.addMatching(e, runner.very_sticky, [
		item.honey_block, item.slime_block, item.banana_slug_slime_block,
		'fruitsdelight:*_jelly_block', 'fruitsdelight:*_jello_block', // extend HoneyBlock / SlimeBlock
		b.porous_flesh, // Visceral Heap; honey's jump factor (block_properties.js), wall slide (creativecompat:wall_slide below)
	])

	// ── Audit (plan_blockrunner_audit.md): every walkable block gets a tier ────────── Part 1, real
	// materials: families that walk like a tier above. Part 2, the 1.00 tiers: what is left of each
	// mod goes in whole ('@mod'), minus blocks a tier already holds (auditLeft). Blocks with no
	// collision ignore their tag, so whole-mod entries are safe for plants and pipes. Never tiered (a
	// tier replaces the block's own speed): soul sand and soil, magma, fluids.
	var auditSkip = [
		item.soul_sand, item.soul_soil, 'securitycraft:reinforced_soul_soil', item.magma_block,
		global.fluid.water, global.fluid.lava, 'minecraft:bubble_column', 'minecraft:moving_piston',
		item.structure_void, 'minecraft:end_gateway', 'minecraft:end_portal', 'minecraft:nether_portal',
		'securitycraft:fake_water_block', 'securitycraft:fake_lava_block', global.fluid.blood,
	]

	// Part 1: real materials
	t.addMatching(e, runner.metal, [
		'/^blocky_siege:(block_of_cast_iron[a-z_]*|cast_iron_fence)$/',   // leaves with Blocky Siege cast iron (plan note)
		'/^lightmanscurrency:coinblock_(copper|iron|gold|emerald|diamond|netherite)$/',
	])
	t.addMatching(e, runner.constructed_brick, [
		// Upgrade Aquatic kelpy and tooth bricks, scute shingles, prismarine; Alex's Abnormalities tooth bricks and tiles
		'/^upgrade_aquatic:([a-z]+_)?kelpy_stone_brick[a-z_]*$/', '/^upgrade_aquatic:(chiseled_)?tooth_(brick|tile)[a-z_]*$/',
		'/^upgrade_aquatic:tooth_(slab|stairs|vertical_slab|wall)$/',
		'/^upgrade_aquatic:(chiseled_)?scute_shingle[a-z_]*$/', 'upgrade_aquatic:prismarine_rod_bundle',
		'upgrade_aquatic:luminous_prismarine*',
		'/^alexsabnormalities:(chiseled_)?[a-z_]+_tooth_(brick|tile)[a-z_]*$/',
		'/^alexsabnormalities:(chiseled_)?[a-z_]*scute_shingle[a-z_]*$/',
		'floralench:*_mossy_stone_brick', '/^hearthandharvest:manure_brick[a-z_]*$/',
	])
	t.addMatching(e, runner.constructed_smooth, [
		'/^upgrade_aquatic:scute_pavement[a-z_]*$/', '/^alexsabnormalities:[a-z_]*scute_pavement[a-z_]*$/',
		'upgrade_aquatic:chiseled_*coralstone', '/^hearthandharvest:polished_manure[a-z_]*$/',
	])
	t.addMatching(e, runner.natural_path, ['/^upgrade_aquatic:([a-z]+_)?kelpy_cobblestone[a-z_]*$/', 'floralench:*_mossy_cobblestone'])
	t.addMatching(e, runner.wood, [
		'upgrade_aquatic:*_boards', 'upgrade_aquatic:*_beehive', item.beehive, item.bee_nest,
		'/^architects_palette:[a-z_]*_totem(_wing)?$/', '/^neapolitan:(carved_)?banana_stalk$/',
		// crates of produce and eggs (Fruits Delight crate precedent)
		'neapolitan:*_crate', 'veggiesdelight:*_crate', 'fungidelight:*_crate', item.chorus_fruit_crate,
		item.cave_carrot_crate, 'hearthandharvest:*crate', item.goose_egg_crate,
		'alexsabnormalities:*_crate', 'incubation:*_crate', 'villagersellanimals:crate_*',
	])
	t.addMatching(e, runner.natural_smooth, [
		// coralstone is stone with coral in it; bone-like tooth and scute blocks follow the bone block
		'/^upgrade_aquatic:(?!chiseled_)([a-z_]+_)?coralstone(_slab|_stairs|_vertical_slab|_wall)?$/', 'upgrade_aquatic:embedded_ammonite',
		'upgrade_aquatic:tooth_block', 'upgrade_aquatic:scute_block', item.scute_block,
		'alexsabnormalities:*_tooth_block', 'alexsabnormalities:*scute_block',
		// crystal clusters and buds, like amethyst
		'minecraft:*amethyst_bud', item.amethyst_cluster, item.pointed_dripstone, '/^geode:[a-z_]*(cluster|bud|crystal)$/',
		'ae2:*quartz_bud', 'ae2:quartz_cluster', 'salt:*salt_bud', 'salt:salt_cluster', 'salt:*rock_salt_ore',
		'diamethysts:budding_diamethyst_block', item.straddlite_block,
	])
	t.addMatching(e, runner.natural_rough, [
		'/^upgrade_aquatic:(dead_)?[a-z_]+_coral_block$/', 'fortuneonneterite_i_ig_z:*_block',
		item.manure_block, 'blocky_siege:permafrost', item.leafcutter_ant_chamber,
		item.fulgurite,
	])
	t.addMatching(e, runner.soft, [
		// beds, bedrolls, sleeping bags, hammocks, pet beds
		'upgrade_aquatic:*bedroll', 'comforts:*', 'redeco:*_pet_bed', item.cat_bed,
		// thatch, kelp and reed blocks, pods
		'upgrade_aquatic:beachgrass_thatch*', 'neapolitan:frond_thatch*', '/^upgrade_aquatic:[a-z_]*kelp_block$/',
		'/^upgrade_aquatic:(boiled_)?[a-z_]+_pickerelweed_block$/', 'neapolitan:*vanilla_pod_block',
		// bags, bundles, cloth, cushioning, cardboard
		'/^hearthandharvest:(corn_husk_bundle|hops_bag|manure_bag)$/', item.cushion, item.immersiveengineering_balloon,
		item.cocoon, 'fantasyfurniture:decorations/bolts_of_cloth', '/^wares:(cardboard_box|package)$/',
		item.cardboard_box, item.cardboard_scrap,
		// the salt blocks in their licked stages (salt:salt_block precedent)
		'/^hearthandharvest:(waxed_)?([a-z]+_licked_)?salt_block$/',
		// nests and mushroom caps (#forge:mushroom_caps precedent)
		'/^(incubation|hearthandharvest|creativedelight):[a-z_]*nest$/', '/^fungidelight:[a-z_]+_mushroom_block$/',
		'/^farmersdelight:(full|half)_tatami_mat$/', '/^hearthandharvest:(hay|straw)_rug$/',
	])
	t.addMatching(e, runner.tilled, [b.tilled_end_stone, 'neapolitan:adzuki_soil'])
	t.addMatching(e, runner.very_sticky, [
		// jam blocks extend HoneyBlock, jello blocks SlimeBlock (Fruits Delight precedent)
		'vintagedelight:*_jam_block', 'vintagedelight:*_jello_block', item.mulberry_jam_block,
	])
	// confectionery: every placed dish, plate, jar and bottle (decided), food blocks, chocolate
	t.addMatching(e, runner.confectionery, [
		'@displaydelight', '@neapolitan',
		'/^lightmanscurrency:coin(block|pile)_chocolate_[a-z]+$/',
		'fruitsdelight:*_jelly', item.durian, item.pineapple,
		'/^vintagedelight:[a-z_]+_jar$/', item.golden_egg,
		'/^brewinandchewin:(?!keg$|coaster$)[a-z_]+$/',
		'/^hearthandharvest:([a-z_]+_pie|waffle|jug|(unripe_)?goat_cheese_wheel)$/',
		'/^veggiesdelight:(beetroot_brownie_tray|carrot_cake|lasagna_pan|stuffed_bellpeppers_block|sweet_potato_pie|vegan_pizza|zucchini_quiche)$/',
		'/^vinery:[a-z_]+_wine$/', '/^ends_delight:(chorus_fruit_pie|[a-z_]+_block)$/', item.stuffed_hoglin,
		'/^miners_delight:(fake_meatloaf|glazed_arachnid_limbs|stuffed_squid)$/', item.baklava,
		item.stuffed_cantaloupe_block, item.sponge_cake, item.guardian_soup,
		item.wrapped_chopsticks, item.gleaming_salad_block, 'hostiledelight:candiedslimeblock',
		'/^rusticdelight:bell_pepper_[a-z_]+_block$/', item.mulberry_punnet,
		'fantasyfurniture:decorations/muffins_*', 'fantasyfurniture:decorations/nordic_sweetrolls',
		'fantasyfurniture:decorations/nordic_boiled_creme_treats',
	], { except: [
		'#' + runner.wood, '#' + runner.soft, '#' + runner.tilled,   // Neapolitan crates, pods, thatch, soil
		'/^neapolitan:(banana_frond|large_banana_frond|small_banana_frond|beanstalk|beanstalk_thorns|mint|[a-z_]*milkshake_cauldron|milk_cauldron|[a-z_]*_basket)$/',
		item.chimpanzee_head,
	] })

	// Part 2: the 1.00 tiers, whole mods last. auditLeft(tier) keeps what no tier holds yet.
	var auditTiers = Object.keys(runner).map(k => runner[k])
	var auditLeft = function (tier) { return { except: auditTiers.filter(x => x !== tier).map(x => "#" + x)
		.concat([item.t_dirt, item.t_flowers, "#minecraft:crops"], auditSkip) } }
	t.addMatching(e, runner.flora, [
		item.cactus, item.chorus_plant, item.chorus_flower, item.big_dripleaf, 'minecraft:cocoa',
		'botania:floating_*', '/^botania:[a-z_]+_floating_flower$/', 'neapolitan:*banana_frond', 'neapolitan:beanstalk*',
		'neapolitan:mint', 'fungidelight:*_colony', 'farmersdelight:*_colony', item.chorus_succulent,
		'nethersdelight:mimicarnation', item.lemon_tree, 'upgrade_aquatic:*_kelp_plant',
		'@flowerpatch', '@bushierflowers', '/^farmersrespite:coffee_stem[a-z_]*$/',
	], auditLeft(runner.flora))
	t.addMatching(e, runner.machinery, [
		'@create', '@create_dd', '@createaddition', '@vintageimprovements', '@extendedgears', '@create_things_and_misc',
		'@create_enchantment_industry', '@create_power_loader', '@creategoggles', '@sliceanddice', '@create_mechanical_extruder',
		'@railways', '@littlecontraptions', '@littlelogistics', '@nocubescreateexp', '@compressedcreativity',
		'@immersiveengineering', '@immersivepetroleum', '@controlengineering', '@more_immersive_wires', '@pneumaticcraft',
		'@ae2', '@appbot', '@fulleng', '@createappliedkinetics', '@advancedperipherals', '@computercraft', '@cccbridge',
		'@prettypipes', '@ppfluids', '@sculktransporting', '@trashcans', '@creategbd', '@gates', '@bobberdetector',
		'@brickhopper', '@projectred_core', '@projectred_fabrication', '@projectred_illumination',
		'/^securitycraft:(?!reinforced_|fake_)(?![a-z_]+_mine$)[a-z_]+$/',   // reinforced blocks and mines: the mirror below
		'@engineersdecor', '@enderstorage',
		item.piston, item.sticky_piston, item.observer, item.dispenser, item.dropper,
		item.hopper, item.daylight_sensor, item.redstone_lamp, item.target,
		'/^everythingcopper:[a-z_]*copper_hopper$/', item.magnet, item.pipe, item.encased_pipe, item.q_chute,
		item.ender_watcher, item.redstone_randomizer, item.magnetized_block, 'sereneseasons:season_sensor',
		'create_central_kitchen:blaze_stove', item.oven, 'immersive_weathering:*',
	], auditLeft(runner.machinery))
	t.addMatching(e, runner.functional, [
		'@botania', '@waystones', '@ftbquests', '@questsadditions', '@lootr', '@tempad', '@well', '@colored_water',
		'@lightmanscurrency', '@monobank', '@etched', '@clayworks', '@appliedcooking', '@the_dirty_stuff', '@dragonloot',
		'@myrtrees', '@cloudstorage', '@botanypots',
		'minecraft:*crafting_table', 'minecraft:*_table', 'minecraft:*furnace', item.smoker, 'minecraft:*anvil',
		item.grindstone, item.stonecutter, item.loom, item.lectern, item.brewing_stand,
		'minecraft:*cauldron', item.composter, item.beacon, item.conduit, item.respawn_anchor,
		item.lodestone, item.jukebox, item.noteblock, item.bell, item.spawner,
		'minecraft:*command_block', item.structure_block, item.jigsaw, 'minecraft:barrier',
		item.end_portal_frame, item.tnt, item.frogspawn, item.turtle_egg,
		'/^everythingcopper:[a-z_]*copper_anvil$/', item.weathering_station,
		'*:*_cauldron', '*:*stove', '*:*_oven', '/^[a-z_]+:[a-z_]*(cooking_pot|copper_pot|skillet|kettle|keg)$/',
		'farmersdelight:*cutting_board', item.safety_net, item.cheese_mold, item.evaporator,
		item.fermenting_jar, '/^hearthandharvest:(sprinkler|stomping_basin|tree_tapper|trough)$/',
		'/^nethersdelight:[a-z_]*(furnace|smoker)$/', '/^vinery:(fermentation_barrel|wine_press)$/',
		item.matrix_enchanter, item.monster_box, item.feeding_trough, 'quark:*_furnace',
		item.capsid, item.gustmaker, item.transmutation_table, 'oldguns:*', 'blocky_siege:*',
		'supplementaries:*', item.pet_drum, item.jeweler_workbench, 'wares:delivery_table',
		item.lightroom, 'projectred_fabrication:*_table', 'creativelabels:*', 'cookingforblockheads:cooking_table',
		'neapolitan:*milk*_cauldron', 'myrtrees:tree_tap', item.framing_saw,
	], auditLeft(runner.functional))
	t.addMatching(e, runner.furniture, [
		'@handcrafted', '@fantasyfurniture', '@redeco', '@another_furniture', '@storagedelight', '@storagedrawers',
		'@abnormals_delight', '@cookingforblockheads', '@interiors', '@wardrobe', '@sophisticatedbackpacks', '@berry_good',
		'@everycomp',
		'minecraft:*chest', item.barrel, 'minecraft:*shulker_box', item.bookshelf, 'quark:*chest',
		'quark:*bookshelf', 'upgrade_aquatic:*chest', 'upgrade_aquatic:*_bookshelf', 'twigs:*_table',
		'farmersdelight:*_cabinet', item.fd_basket, 'creativedelight:*', 'delightful:*_cabinet', 'vinery:*',
		'neapolitan:*_basket', 'hearthandharvest:*_cabinet', item.sticky_basket, 'decorative_blocks:*_seat',
		'immersiveengineering:*crate', 'immersiveengineering:*barrel', 'securitycraft:keypad_chest', 'securitycraft:keypad_barrel',
		'securitycraft:reinforced_bookshelf', 'pneumaticcraft:*chest', 'ae2:*chest',
	], auditLeft(runner.furniture))
	t.addMatching(e, runner.decorative, [
		'@davebuildingmod', '@design_decor', '@createdeco', '@bellsandwhistles', '@dustrial_decor', '@suppsquared',
		'@immersiveposts', '@chimes', '@mca', '@moyai', '@mysticaloaktree', '@yuushya', '@decorative_blocks', '@beautify',
		'@floralench', '@twigs', '@chalk', '@fairylights', '@canned_goods', '@architects_palette', '@quark',
		'@upgrade_aquatic', '@alexsabnormalities', '@alexsmobs', '@hearthandharvest', '@incubation', '@vintagedelight',
		'@fruitsdelight', '@veggiesdelight', '@rusticdelight', '@fungidelight', '@farmersdelight', '@delightful',
		'@ends_delight', '@endersdelight', '@nethersdelight', '@miners_delight', '@oceanic_delight', '@farmersrespite',
		'@corn_delight', '@salt', '@tintedcampfires', '@buzzier_bees', '@biomesoplenty', '@geode', '@diamethysts',
		'@nock_enough_arrows', 'everythingcopper:*copper_lamp',
		item.dragon_egg, 'minecraft:*_head', 'minecraft:*_skull', item.flower_pot, item.end_rod,
		'minecraft:*_stained_glass_pane', item.light, item.sculk_vein, item.glow_lichen,
	], auditLeft(runner.decorative))

	// Last: whatever's left of #minecraft:dirt (grass, dirt, rooted dirt, modded grasses/soils)
	// once the tiers above have claimed coarse dirt, podzol, mud, moss etc.
	var runnerTags = Object.keys(runner).map(k => runner[k])
	t.addMatching(e, runner.dirt, [
		item.t_dirt, item.rooted_grass_block, item.enchanted_soil,
		'/^vinery:(dirt_slab|grass_slab|loam[a-z_]*)$/', item.dirt_slab,
	], {
		except: runnerTags.filter(tag => tag !== runner.dirt).map(tag => '#' + tag),
	})

	// SecurityCraft's reinforced blocks and disguised mines walk like the vanilla block they copy
	// (a mine with a speed of its own would give itself away). Names that don't exist are skipped.
	var scName = {
		terracotta: 'hardened_clay', dirt_path: 'grass_path', nether_quartz_ore: 'quartz',
		stone_slab: 'normal_stone_slab', smooth_stone_slab: 'stone_slab',
	}
	t.mirror(e, runnerTags, id => {
		if (id.indexOf('minecraft:') !== 0) return null
		var name = scName[id.substring(10)] || id.substring(10)
		return ['securitycraft:reinforced_' + name, 'securitycraft:' + name.replace(/_ore$/, '') + '_mine']
	})

	// The Creative Compat hooks on these tiers (facing-aware sticky, camo speed, sinking, wall slide): Creative Compat's
	// tags/blocktags.js

	// wildcards like 'minecraft:polished_*' also catch the family's buttons and pressure plates
	t.remove(e, runnerTags, '/_(button|pressure_plate)$/')

	t.overlaps(e, runnerTags.concat(basicTiers), 'BlockRunner tier')

	// Crop parity (farmland soils and crops): Creative Delight (creativeengineering/crop_tags.js).
})

console.log('block tags finished')
