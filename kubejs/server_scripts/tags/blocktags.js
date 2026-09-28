console.log('block tags starting')

var t = global.tags
var b = global.blocks

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

	// minecraft:concrete isn't a vanilla tag in 1.19.2 — the 16 solid concretes (not powder)
	t.addMatching(e, 'forge:concrete', 'minecraft:*_concrete')
	t.addMatching(e, 'forge:bricks', ['minecraft:bricks', 'minecraft:brick_*'])
	t.addMatching(e, 'forge:stone_bricks', ['minecraft:stone_bricks', 'minecraft:stone_brick_*'])

	// ── BlockRunner walk speed ──────────────────────────────────────────────
	// Multipliers live in config/creativeengineering-common.toml [surface.speed], one key per tier
	// (the numbers below are the defaults, fastest → slowest). patcherMod's SurfaceTier enum lists the
	// tiers for that config, so a new tier needs an entry there too; blockrunner.json only keeps one-offs.
	// BlockRunner reads the block ~0.2 below your feet (vanilla getOnPos), so carpets, 1–2 layer
	// snow, pressure plates and IW leaf piles never register (the block under them does).
	// A tiered block's native speed factor is replaced (IP asphalt's 1.2, honey's 0.4); soul sand/soil
	// are left out on purpose: tiering them would stop Soul Speed from cancelling the slowdown.
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
		machinery:          'blockrunner:surface/machinery',          // 1.00  not filled yet
		functional:         'blockrunner:surface/functional',         // 1.00  not filled yet
		decorative:         'blockrunner:surface/decorative',         // 1.00  not filled yet
		camouflage:         'blockrunner:surface/camouflage',         // 1.00
		glass:              'blockrunner:surface/glass',              // 1.00
		dirt:               'blockrunner:surface/dirt',               // 1.00
		natural_rough:      'blockrunner:surface/natural_rough',      // 0.92
		soft:               'blockrunner:surface/soft',               // 0.88
		dust:               'blockrunner:surface/dust',               // 0.85
		confectionery:      'blockrunner:surface/confectionery',      // 0.85
		tilled:             'blockrunner:surface/tilled',             // 0.80
		leaves:             'blockrunner:surface/leaves',             // 0.78
		snow:               'blockrunner:surface/snow',               // 0.75
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
	var item = global.item

	t.addMatching(e, runner.asphalt, [
		'#forge:asphalt', 'immersivepetroleum:asphalt_*', 'create_dd:*_asphalt_block',
	])

	t.addMatching(e, runner.concrete, [
		'#forge:concrete', 'immersiveengineering:*concrete*', 'engineersdecor:*rebar_concrete*',
		'immersiveengineering:*hempcrete*', 'dustrial_decor:*cinder*',
	])

	t.addMatching(e, runner.slippery, [
		'#minecraft:ice', 'supplementaries:soap_block', 'architects_palette:*packed_ice*',
		item.thin_ice,
	])

	t.addMatching(e, runner.constructed_smooth, [
		// vanilla: polished / smooth / cut stones, quartz, glazed terracotta, dark prismarine
		'minecraft:polished_*', 'minecraft:smooth_*', 'minecraft:cut_*sandstone*', 'minecraft:chiseled_*sandstone',
		'minecraft:chiseled_polished_blackstone', 'minecraft:*_glazed_terracotta', 'minecraft:dark_prismarine*',
		'/^minecraft:(chiseled_)?quartz_(block|pillar|slab|stairs)$/',
		'#forge:storage_blocks/diamond', '#forge:storage_blocks/emerald', '#forge:storage_blocks/lapis',
		'#forge:storage_blocks/redstone', '#forge:storage_blocks/quartz',
		// quark
		'quark:polished_*', 'quark:smooth_*', 'quark:cut_*sandstone*', 'quark:chiseled_soul_sandstone', 'quark:*_pillar',
		'quark:quartz_vertical_slab', 'quark:dark_prismarine_vertical_slab', 'quark:midori_block*',
		'quark:duskbound_block*', 'quark:sturdy_stone',
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
	], { except: ['minecraft:polished_*brick*', 'quark:iron_pillar', 'quark:polished_*brick*'] })

	// grating: see-through walkway floors (catwalks, grates, metal scaffolding tops)
	t.addMatching(e, runner.grating, [
		'quark:grate', 'everythingcopper:*copper_grate', 'design_decor:*_catwalk', 'design_decor:ornate_grate',
		'createdeco:*_catwalk', 'createdeco:*_catwalk_stairs', 'create:*_scaffolding', 'davebuildingmod:steel_mesh_trapdoor',
		'/^immersiveengineering:(slab_|stairs_)?(alu|steel)_scaffolding_(grate_top|standard)$/',
		'engineersdecor:steel_catwalk*', item.steel_floor_grating,
		'/^dustrial_decor:(chain|iron_bar)_trapdoor$/',
	])

	t.addMatching(e, runner.metal, [
		'#forge:storage_blocks/iron', '#forge:storage_blocks/gold', '#forge:storage_blocks/copper',
		'#forge:storage_blocks/netherite', '#forge:storage_blocks/steel', '#forge:storage_blocks/aluminum',
		'#forge:storage_blocks/lead', '#forge:storage_blocks/silver', '#forge:storage_blocks/nickel',
		'#forge:storage_blocks/constantan', '#forge:storage_blocks/electrum', '#forge:storage_blocks/uranium',
		'#forge:storage_blocks/brass', '#forge:storage_blocks/zinc', '#forge:storage_blocks/compressed_iron',
		'#forge:storage_blocks/andesite_alloy', '#forge:sheetmetals', '#forge:sheetmetal_slabs',
		// every oxidation / waxed stage of copper and cut copper
		'minecraft:*copper', 'minecraft:*copper_block', 'minecraft:*cut_copper*', 'minecraft:iron_trapdoor',
		'quark:*cut_copper*', 'quark:*iron_plate*', 'quark:iron_pillar',
		'immersive_weathering:*plate_iron*', 'immersive_weathering:*cut_iron*', 'immersive_weathering:*iron_trapdoor',
		'create:industrial_iron_block', '/^create:(waxed_)?((exposed|weathered|oxidized)_)?copper_(shingle|tile)(s|_slab|_stairs)$/',
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
	], { except: ['minecraft:raw_copper_block'] })

	t.addMatching(e, runner.constructed_brick, [
		'#minecraft:stone_bricks', '#forge:bricks', '#forge:stone_bricks',
		// vanilla: every *brick* family, deepslate tiles, prismarine, purpur
		'minecraft:*brick*', 'minecraft:*deepslate_tile*', 'minecraft:chiseled_deepslate',
		'/^minecraft:prismarine(_slab|_stairs|_wall)?$/', 'minecraft:purpur_*',
		// quark: bricks (incl. raw ore/dirt/cobblestone bricks), roof shingles, vertical slabs
		'quark:*brick*', 'quark:*shingles*', 'quark:*deepslate_tile*', 'quark:prismarine_vertical_slab',
		'quark:purpur_vertical_slab',
		'supplementaries:*brick*', 'supplementaries:*_tile*', 'immersive_weathering:*brick*',
		'immersive_weathering:*deepslate_tile*',
		'/^' + palette + '(cut|small)_[a-z_]+_brick(s|_slab|_stairs|_wall)$/', 'create:*rose_quartz_tiles',
		// modded bricks, tiles and roof shingles
		'architects_palette:*brick*', 'architects_palette:*_tile*', item.moonshale_flagstone,
		'botania:*brick*', 'ae2:*brick*', 'clayworks:*brick*', 'createdeco:*brick*', 'pneumaticcraft:*brick*',
		'twigs:*brick*', 'twigs:*shingle*', 'design_decor:*castel*', 'design_decor:*stone_tiles', '@mosaic_masonry',
		'/^immersiveengineering:(slab_)?(alloybrick|blastbrick|blastbrick_reinforced|cokebrick)$/',
		'engineersdecor:clinker_brick_*', 'engineersdecor:slag_brick_*', 'engineersdecor:dark_shingle_roof*',
		'/^buzzier_bees:(chiseled_)?honeycomb_(brick|tile)[a-z_]*$/', '/^securitycraft:(reinforced_)?crystal_quartz_bricks$/',
		'philipsruins:*brick*', 'philipsruins:*_tiles', item.frozen_prismarine,
		'/^sootychimneys:(dirty_)?[a-z_]*brick_chimney$/',
	])

	t.addMatching(e, runner.natural_path, [
		'minecraft:dirt_path', 'minecraft:coarse_dirt', '#forge:gravel', '#forge:cobblestone',
		'minecraft:*cobble*', 'quark:*cobble*_vertical_slab', 'supplementaries:raked_gravel',
		'immersive_weathering:*cobblestone*', 'botania:metamorphic_*_cobblestone*',
		'/^davebuildingmod:(ballast|gravel|rocky_dirt)(_slab)?$/', item.twigs_rocky_dirt, item.coarse_dirt_slab,
		'/^engineersdecor:dense_grit_(dirt|sand)_block$/', '/^sootychimneys:(dirty_)?cobblestone_chimney$/',
	], { except: ['quark:*cobblestone_bricks*'] })

	t.addMatching(e, runner.wood, [
		'#minecraft:planks', '#minecraft:wooden_slabs', '#minecraft:wooden_stairs', '#minecraft:logs',
		'#forge:treated_wood', '#forge:treated_wood_slab',
		// fence and gate tops count; a trapdoor only when it sits in the top half of its block
		'#minecraft:wooden_fences', '#forge:fence_gates/wooden', '#minecraft:wooden_trapdoors', 'minecraft:scaffolding',
		'/^(architects_palette:twisted|botania:(dream|living)wood|immersive_weathering:charred|vinery:cherry)_fence_gate$/',
		'#quark:wooden_vertical_slabs', 'quark:vertical_*_planks', '#quark:hollow_logs',
		'quark:bamboo_block', 'quark:bamboo_mosaic', 'quark:bamboo_mat', 'quark:*crate', item.cactus_block,
		'quark:stripped_bamboo_block', 'quark:stick_block', 'quark:sugar_cane_block', 'minecraft:petrified_oak_slab',
		'supplementaries:timber_*',
		// modded boards, beams, palisades, trims, floorboards, log piles and wooden crates
		'architects_palette:*_board*', 'architects_palette:*_railing', item.vslab_twisted,
		'everycomp:ap/*_board*', 'everycomp:ap/*_railing', 'everycomp:db/*_beam', 'everycomp:db/*_palisade', 'everycomp:sd/*_trim',
		'decorative_blocks:*_beam', 'decorative_blocks:*_palisade', 'storagedrawers:*_trim',
		'/^botania:(stripped_)?(dream|living)wood_(fence|wall)$/', 'immersive_weathering:charred_*',
		'/^immersiveengineering:((slab_|stairs_)?(alu|steel)_scaffolding_wooden_top|treated_scaffold)$/',
		'/^engineersdecor:old_industrial_wood_(planks|slab|slabslice|stairs)$/',
		'/^davebuildingmod:(full_log_pile|log_palisade|log_pile_[12]_(bottom|top)|half_lumber_[a-z_]+)$/',
		'/^vinery:(cherry_(beam|floorboard)|stackable_log|[a-z_]*crate)$/', '/^create_compressed:(large_)?cogwheel_block$/',
		'farmersdelight:*_crate', 'rusticdelight:*crate', 'delightful:*_crate', 'quarkdelight:*_fruit_crate',
		item.chorus_crate,
	], { except: ['/^(copycats|framedblocks):/'] }) // their copycat/framed trapdoors are camouflage

	t.addMatching(e, runner.natural_smooth, [
		// raw stone + its slab/stairs/wall (vanilla, then quark's stones and vertical slabs)
		'/^minecraft:(stone|granite|diorite|andesite|tuff|calcite|deepslate|blackstone|sandstone|red_sandstone|end_stone|dripstone_block)(_slab|_stairs|_wall)?$/',
		'/^quark:(stone|granite|diorite|andesite|tuff|calcite|dripstone_block|blackstone|sandstone|red_sandstone|jasper|limestone|shale|myalite|permafrost|soul_sandstone)(_slab|_stairs|_wall|_vertical_slab)?$/',
		'/^minecraft:infested_(stone|deepslate)$/', 'minecraft:gilded_blackstone', 'minecraft:reinforced_deepslate',
		'#minecraft:terracotta', 'minecraft:*_ore', '#forge:ores', 'minecraft:obsidian', 'minecraft:crying_obsidian',
		'minecraft:amethyst_block', 'minecraft:budding_amethyst', 'minecraft:packed_mud', 'minecraft:bone_block',
		'minecraft:bedrock', 'quark:dusky_myalite', 'quark:*corundum',
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
	], { except: ['minecraft:nether_*_ore', 'minecraft:ancient_debris'] })

	// camouflage: blocks that copy another block's look, so a speed of their own would be arbitrary
	t.addMatching(e, runner.camouflage, [
		'@framedblocks', '@copycats', 'create:copycat_panel', 'create:copycat_step',
	], { except: ['framedblocks:framing_saw', '/^framedblocks:framed_(glowing_)?item_frame$/'] })

	t.addMatching(e, runner.glass, [
		'#forge:glass', 'minecraft:tinted_glass', 'ae2:quartz_glass', 'ae2:quartz_vibrant_glass', 'alexsmobs:rainbow_glass',
		'create_crystal_clear:*_glass_casing', 'davebuildingmod:reinforced_glass', 'engineersdecor:panzerglass_*',
		'/^immersiveengineering:(slab_)?insulating_glass$/', 'immersiveengineering:slag_glass',
		'securitycraft:reinforced_*glass',
		// full-block windows, glass trapdoors (top half only), glassy crystal blocks
		'minecraft:sea_lantern', 'create:*_window', 'everycomp:c/*_window', item.framed_glass_trapdoor,
		'createframed:*_glass_trapdoor', 'create_crystal_clear:*glass_encased*', item.steel_framed_window,
		item.metro_window, item.window, '/^immersive_weathering:(vitrified_sand|frosty_glass)$/',
		'/^buzzier_bees:crystallized_(clover_)?honey_block$/', item.crystalized_banana_slug_mucus,
	])

	t.addMatching(e, runner.natural_rough, [
		'minecraft:podzol', 'minecraft:mycelium', 'minecraft:crimson_nylium', 'minecraft:warped_nylium',
		'minecraft:netherrack', 'minecraft:basalt', 'minecraft:nether_*_ore', 'supplementaries:ashen_basalt',
		'minecraft:*coral_block', 'minecraft:mangrove_roots',
		// raw ore chunks, ancient debris, and crumbly fuel / mineral blocks
		'minecraft:raw_*_block', 'create:raw_zinc_block', 'immersiveengineering:raw_block_*', 'minecraft:ancient_debris',
		'minecraft:coal_block', 'minecraft:glowstone', item.charcoal_block, item.architects_palette_charcoal_block,
		'/^immersiveengineering:(slab_)?coke$/', item.petcoke_block, item.sulfur_block, item.biomass_pellet_block,
		// Immersive Weathering soils
		'/^immersive_weathering:(silt|humus|fluvisol|vertisol|cryosol|permafrost|mulch_block|nulch_block|cracked_mud)$/',
		// other loose ground: silt, lichen, compost, anthills, niter beds
		item.silt, item.petrified_lichen, item.compost, item.soul_compost,
		item.leafcutter_anthill, item.niter_bedding,
	])

	t.addMatching(e, runner.soft, [
		'#minecraft:wool', '#forge:mushroom_caps', '#forge:mushroom_stems', '#minecraft:wart_blocks',
		'minecraft:sponge', 'minecraft:hay_block', 'minecraft:target', 'minecraft:moss_block',
		'minecraft:dried_kelp_block', 'minecraft:shroomlight', 'minecraft:*_froglight', 'minecraft:honeycomb_block',
		'/^minecraft:sculk(_catalyst|_sensor|_shrieker)?$/', // not sculk_vein: no collision
		'quark:thatch*', 'quark:bonded_*', 'quark:*_sack', 'quark:glow_shroom_block', 'quark:glow_shroom_stem',
		'supplementaries:feather_block', 'supplementaries:flax_block', 'supplementaries:fodder', 'supplementaries:sack',
		// beds (not full blocks, so no visual sink)
		'#minecraft:beds', 'handcrafted:*_fancy_bed', 'everycomp:hc/*_fancy_bed', 'domesticationinnovation:pet_bed_*',
		// fabric, fur, padding, rubber, petals, thatch, wax, cardboard
		'/^create_dd:([a-z_]+_)?(velvet_block|padded_[a-z_]+|rubber_block)$/', item.belt_block,
		'/^dustrial_decor:(mini_)?padded_(?!door$)[a-z_]+$/', '/^dustrial_decor:(smooth_cardboard[a-z_]*|folded_cardboard|cardboard_box|cardboard_trapdoor)$/',
		'davebuildingmod:*_quilted_wool', item.bison_fur_block, 'botania:*_petal_block', 'twigs:bamboo_thatch*',
		item.paraffin_wax_block,
		// sacks, bags and bales
		'suppsquared:sack_*', 'delightful:*_sack', 'rusticdelight:*_bag',
		'/^farmersdelight:(rice_bag|rice_bale|straw_bale|tatami)$/',
	])

	t.addMatching(e, runner.dust, [
		'#forge:sand', 'minecraft:*_concrete_powder', 'quark:gravisand',
		'supplementaries:ash',
		'/^immersive_weathering:(red_)?sand_layer_block$/', 'immersive_weathering:ash*',
		'creativeengineering:*_dust_pile', 'create_compressed:*_flour_pile', 'immersiveengineering:sawdust',
		'create_compressed:crushed_*_pile', item.powdered_obsidian_block,
		item.create_things_and_misc_powdered_obsidian_block, 'oldguns:*_powder_block',
	])

	// confectionery: edible building blocks and placed food
	t.addMatching(e, runner.confectionery, [
		'@create_confectionery', 'minecraft:cake', 'supplementaries:sugar_cube',
		'/^architects_palette:(crustless_)?bread_(block|slab)$/', 'delightful:*_ice_cream_block', 'delightful:*_pie',
		'create_central_kitchen:*_pie', 'farmersdelight:*_pie', 'farmersdelight:sweet_berry_cheesecake',
		'/^farmersdelight:(honey_glazed_ham|rice_roll_medley|roast_chicken|shepherds_pie|stuffed_pumpkin)_block$/',
		// gourds and fruit blocks (a hard rind: no visual sink), feasts and cakes
		'minecraft:melon', 'minecraft:pumpkin', 'minecraft:carved_pumpkin', 'minecraft:jack_o_lantern', item.chorus_fruit_block,
		'/^delightful:(sliced_)?(cantaloupe|melon|mini_melon|pumpkin)$/', '/^brewinandchewin:(pizza|quiche)$/',
		'/^createaddition:(chocolate|honey)_cake$/', '/^rusticdelight:[a-z_]*(pancakes|cheesecake)$/', item.chorus_pie,
		'/^goldenfood:(enchanted_)?golden_cake$/',
	])

	t.addMatching(e, runner.tilled, ['minecraft:farmland', 'farmersdelight:rich_soil_farmland', item.tilled_end_stone])

	t.addMatching(e, runner.leaves, ['#minecraft:leaves', '#quark:hedges', item.bamboo_leaves])

	t.addMatching(e, runner.snow, ['#minecraft:snow', 'architects_palette:coarse_snow'])

	t.addMatching(e, runner.spongy, [
		'minecraft:mud', 'minecraft:muddy_mangrove_roots', 'minecraft:wet_sponge', 'minecraft:clay',
		'create_compressed:dough_block', item.prepared_flesh_block,
		'/^architects_palette:(rotten_flesh_block|entrails[a-z_]*)$/',
	])

	// sticky: blocks with one sticky face. Piston heads are shared by plain and sticky pistons, so
	// patcherMod (StickyFacing) drops the speed unless facing=up and, for heads, type=sticky —
	// for everything in #creativeengineering:blockrunner_face_up below.
	t.addMatching(e, runner.sticky, [
		'minecraft:sticky_piston', 'minecraft:piston_head', 'create:sticky_mechanical_piston',
		'create:mechanical_piston_head', 'create_things_and_misc:*sticky_launcher',
	])

	t.addMatching(e, runner.very_sticky, [
		'minecraft:honey_block', 'minecraft:slime_block', 'alexsmobs:banana_slug_slime_block',
	])

	// Last: whatever's left of #minecraft:dirt (grass, dirt, rooted dirt, modded grasses/soils)
	// once the tiers above have claimed coarse dirt, podzol, mud, moss etc.
	var runnerTags = Object.keys(runner).map(k => runner[k])
	t.addMatching(e, runner.dirt, [
		'#minecraft:dirt', 'immersive_weathering:rooted_grass_block', item.enchanted_soil,
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

	// patcherMod hooks: facing-aware sticky tier, camo-aware camouflage tier, visual sinking
	// (surface/StickyFacing, CamoSpeed, SinkingRender)
	t.add(e, 'creativeengineering:blockrunner_face_up', '#' + runner.sticky)
	t.add(e, 'creativeengineering:camo_speed', '#' + runner.camouflage)
	t.add(e, 'creativeengineering:sinking', [runner.soft, runner.dust, runner.spongy, runner.very_sticky].map(tag => '#' + tag))

	// wildcards like 'minecraft:polished_*' also catch the family's buttons and pressure plates
	t.remove(e, runnerTags, '/_(button|pressure_plate)$/')

	t.overlaps(e, runnerTags.concat(basicTiers), 'BlockRunner tier')

	// ── Structure ground matching (patcherMod worldgen/GroundMatch, CEConfig [ground_match]) ──
	// A base family: these blocks in a structure's bottom layers become the sampled local ground where
	// the ground isn't this family (grass pads on volcanic peaks, dirt in deserts). A new family = a new
	// tag here + its id in [ground_match] families. dirt_path and farmland stay: they're design, not base.
	t.add(e, 'creativeengineering:ground_match/grass', [
		item.grass_block, item.dirt, item.coarse_dirt, item.podzol, item.rooted_dirt, item.mycelium,
	])
	// Dropped when the block under them is swapped (both halves of tall plants).
	t.add(e, 'creativeengineering:ground_match/plants', [
		'#minecraft:flowers', '#minecraft:saplings', item.grass, item.fern, item.tall_grass, item.large_fern,
		item.dead_bush, item.brown_mushroom, item.red_mushroom, b.sweet_berry_bush,
	])
	// Never sampled as ground: tree and crop parts that neighbouring chunks' features spill onto the surface.
	t.add(e, 'creativeengineering:ground_match/not_ground', [
		'#minecraft:leaves', '#minecraft:logs', item.brown_mushroom_block, item.red_mushroom_block,
		item.mushroom_stem, item.cactus, item.bamboo, item.pumpkin, item.melon_block,
	])
})

console.log('block tags finished')
