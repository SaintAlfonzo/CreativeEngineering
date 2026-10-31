// JEI ingredient order — the pack's definition (helper and shape: startup_scripts/func/jei_order.js,
// reference + tests: feature_writeup/jei_ordering.md). Order of every array below = the order in JEI.
// Tags made from this: server_scripts/tags/jei_order_tags.js. Sorting: patcherMod PackIngredientOrder
// (CEConfig [jei] packOrder). Apply a change: /kubejs reload startup_scripts, /kubejs reload client_scripts,
// then /reload (tags + JEI restart).
//
// Items: Equipment first (writeup 12.5): one node per material family (tools, then armor, then that material's
// copies of misc gear), then misc (vanilla, modded, knives, arrows, guns, then the tech / magic / hats
// catch-alls). Also the IE multiblocks, vanilla fluids, vanilla beneficial effects. Everything else is unplaced
// and keeps JEI's own order after the placed entries of its type, until it gets a node.
const jeiI = global.item
const jeiF = global.fluid
const jeiE = global.effect
const jeiMb = global.multiblock

// Storage Delight (repackedStorage): wood by wood in the vanilla wood order, bamboo last, each wood's pieces in one fixed
// order (creativedelight/members #23). The ids are the mod's own pattern, so they are built here, not listed in the dict.
const jeiStorageWoods = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped', 'bamboo']
const jeiStorageKinds = [
	w => w + '_drawer', w => w + '_drawer_with_door', w => w + '_drawer_with_books', w => 'small_' + w + '_drawers',
	w => w + '_single_door_cabinet', w => w + '_cabinet_with_glass_doors', w => 'glass_' + w + '_cabinet',
	w => w + '_cabinet_with_polished_andesite_countertop', w => w + '_cabinet_with_polished_diorite_countertop',
	w => w + '_cabinet_with_polished_deepslate_countertop', w => w + '_cabinet_with_polished_blackstone_countertop',
	w => w + '_cabinet_with_stone_bricks_countertop', w => w + '_cabinet_with_polished_tuff_countertop',
	w => w + '_bookshelf_with_door',
]
const jeiStorageList = []
jeiStorageWoods.forEach(w => jeiStorageKinds.forEach(k => jeiStorageList.push('storagedelight:' + k(w))))

global.jeiOrder.define({
	types: ['item', 'multiblock', 'fluid', 'effect'],

	modOrder: [
		'minecraft',
		'create',
		'immersiveengineering',
		'pneumaticcraft',
		'creativeengineering',
		'farmersdelight',
		'everythingcopper',
		'delightful',
		'create_things_and_misc',
	],

	trees: {
		item: [
			{ id: 'equipment', children: [
				{ id: 'wood', sort: ['list', 'mod', 'creative'], list: [
					jeiI.wooden_sword, jeiI.wooden_pickaxe, jeiI.wooden_axe, jeiI.wooden_shovel, jeiI.wooden_hoe, jeiI.wooden_knife,
					jeiI.ia_wooden_helmet, jeiI.ia_wooden_chestplate, jeiI.ia_wooden_leggings, jeiI.ia_wooden_boots,
				] },
				{ id: 'leather', sort: ['list', 'mod', 'creative'], list: [
					jeiI.leather_helmet, jeiI.leather_chestplate, jeiI.leather_leggings, jeiI.leather_boots,
					jeiI.goggle_leather_helmet, jeiI.leather_backtank, jeiI.leather_horse_armor,
				] },
				{ id: 'stone', sort: ['list', 'mod', 'creative'], list: [
					jeiI.stone_sword, jeiI.stone_pickaxe, jeiI.stone_axe, jeiI.stone_shovel, jeiI.stone_hoe, jeiI.stone_knife,
				] },
				{ id: 'copper', sort: ['list', 'mod', 'creative'], list: [
					jeiI.copper_sword, jeiI.copper_pickaxe, jeiI.copper_axe, jeiI.copper_shovel, jeiI.copper_hoe, jeiI.copper_knife,
					jeiI.delightful_copper_knife, jeiI.copper_helmet, jeiI.copper_chestplate, jeiI.copper_leggings,
					jeiI.copper_boots, jeiI.goggle_diving_helmet, jeiI.copper_diving_helmet, jeiI.copper_backtank,
					jeiI.copper_diving_boots, jeiI.ce_copper_shield, jeiI.copper_shears, jeiI.copper_horse_armor,
					jeiI.copper_backpack,
				] },
				{ id: 'chainmail', sort: ['list', 'mod', 'creative'], list: [
					jeiI.chainmail_helmet, jeiI.chainmail_chestplate, jeiI.chainmail_leggings, jeiI.chainmail_boots,
					jeiI.goggle_chainmail_helmet, jeiI.chainmail_backtank,
				] },
				{ id: 'iron', sort: ['list', 'mod', 'creative'], list: [
					jeiI.iron_sword, jeiI.iron_pickaxe, jeiI.iron_axe, jeiI.iron_shovel, jeiI.iron_hoe, jeiI.iron_knife,
					jeiI.iron_machete, jeiI.iron_helmet, jeiI.iron_chestplate, jeiI.iron_leggings, jeiI.iron_boots,
					jeiI.goggle_iron_helmet, jeiI.iron_backtank, jeiI.shears, jeiI.iron_horse_armor, jeiI.iron_fishing_rod,
					jeiI.iron_fishing_rod_upgrade, jeiI.iron_backpack,
				] },
				{ id: 'gold', sort: ['list', 'mod', 'creative'], list: [
					jeiI.golden_sword, jeiI.golden_pickaxe, jeiI.golden_axe, jeiI.golden_shovel, jeiI.golden_hoe, jeiI.golden_knife,
					jeiI.golden_machete, jeiI.scythe, jeiI.golden_helmet, jeiI.golden_chestplate, jeiI.golden_leggings,
					jeiI.golden_boots, jeiI.goggle_golden_helmet, jeiI.golden_backtank, jeiI.golden_horse_armor,
					jeiI.ce_gold_shield, jeiI.gold_fishing_rod, jeiI.gold_fishing_rod_upgrade, jeiI.gold_backpack,
				] },
				{ id: 'diamond', sort: ['list', 'mod', 'creative'], list: [
					jeiI.diamond_sword, jeiI.diamond_pickaxe, jeiI.diamond_axe, jeiI.diamond_shovel, jeiI.diamond_hoe,
					jeiI.diamond_knife, jeiI.diamond_machete, jeiI.diamond_helmet, jeiI.diamond_chestplate, jeiI.diamond_leggings,
					jeiI.diamond_boots, jeiI.goggle_diamond_helmet, jeiI.diamond_backtank, jeiI.diamond_horse_armor,
					jeiI.ce_diamond_shield, jeiI.diamond_fishing_rod, jeiI.diamond_fishing_rod_upgrade, jeiI.diamond_backpack,
				] },
				{ id: 'netherite', sort: ['list', 'mod', 'creative'], list: [
					jeiI.netherite_sword, jeiI.netherite_pickaxe, jeiI.netherite_axe, jeiI.netherite_shovel, jeiI.netherite_hoe,
					jeiI.netherite_knife, jeiI.netherite_machete, jeiI.netherite_helmet, jeiI.netherite_chestplate,
					jeiI.netherite_leggings, jeiI.netherite_boots, jeiI.goggle_netherite_helmet,
					jeiI.goggle_netherite_diving_helmet, jeiI.netherite_diving_helmet, jeiI.netherite_backtank,
					jeiI.netherite_diving_boots, jeiI.netherite_horse_armor, jeiI.ce_netherite_shield, jeiI.netherite_fishing_rod,
					jeiI.netherite_backpack,
				] },
				{ id: 'echo', sort: ['list', 'mod', 'creative'], list: [
					jeiI.echo_sword, jeiI.echo_pickaxe, jeiI.echo_axe, jeiI.echo_shovel, jeiI.echo_hoe, jeiI.echo_helmet,
					jeiI.echo_chestplate, jeiI.echo_leggings, jeiI.echo_boots, jeiI.ce_echo_shield,
				] },
				{ id: 'felsic', sort: ['list', 'mod', 'creative'], list: [
					jeiI.felsic_sword, jeiI.felsic_pickaxe, jeiI.felsic_axe, jeiI.felsic_shovel, jeiI.felsic_hoe,
					jeiI.felsic_helmet, jeiI.felsic_chestplate, jeiI.felsic_leggings, jeiI.felsic_boots, jeiI.ce_felsic_shield,
				] },
				{ id: 'dragon', sort: ['list', 'mod', 'creative'], list: [
					jeiI.dragon_sword, jeiI.dragon_pickaxe, jeiI.dragon_axe, jeiI.dragon_shovel, jeiI.dragon_hoe,
					jeiI.dragon_helmet, jeiI.dragon_chestplate, jeiI.upgraded_dragon_chestplate, jeiI.dragon_leggings,
					jeiI.dragon_boots, jeiI.ce_dragon_shield, jeiI.dragon_bow, jeiI.dragon_crossbow, jeiI.dragon_trident,
					jeiI.dragon_horse_armor,
				] },
				{ id: 'turtle', sort: ['list', 'mod', 'creative'], list: [
					jeiI.turtle_helmet, jeiI.goggle_turtle_helmet,
				] },
				{ id: 'shulker', sort: ['list', 'mod', 'creative'], list: [
					jeiI.shulker_shell,
				] },
				{ id: 'immersive_armors', sort: ['list', 'mod', 'creative'], list: [
					jeiI.ia_bone_helmet, jeiI.ia_bone_chestplate, jeiI.ia_bone_leggings, jeiI.ia_bone_boots, jeiI.ia_slime_helmet,
					jeiI.ia_slime_chestplate, jeiI.ia_slime_leggings, jeiI.ia_slime_boots, jeiI.ia_robe_helmet,
					jeiI.ia_robe_chestplate, jeiI.ia_robe_leggings, jeiI.ia_robe_boots, jeiI.ia_prismarine_helmet,
					jeiI.ia_prismarine_chestplate, jeiI.ia_prismarine_leggings, jeiI.ia_prismarine_boots, jeiI.ia_warrior_helmet,
					jeiI.ia_warrior_chestplate, jeiI.ia_warrior_leggings, jeiI.ia_warrior_boots, jeiI.ia_heavy_helmet,
					jeiI.ia_heavy_chestplate, jeiI.ia_heavy_leggings, jeiI.ia_heavy_boots, jeiI.ia_steampunk_helmet,
					jeiI.ia_steampunk_chestplate, jeiI.ia_steampunk_leggings, jeiI.ia_steampunk_boots, jeiI.ia_divine_helmet,
					jeiI.ia_divine_chestplate, jeiI.ia_divine_leggings, jeiI.ia_divine_boots, jeiI.ia_wither_helmet,
					jeiI.ia_wither_chestplate, jeiI.ia_wither_leggings, jeiI.ia_wither_boots,
				] },
				{ id: 'steel', sort: ['list', 'mod', 'creative'], list: [
					jeiI.sword_steel, jeiI.pickaxe_steel, jeiI.axe_steel, jeiI.shovel_steel, jeiI.hoe_steel, jeiI.steel_knife,
					jeiI.armor_steel_head, jeiI.armor_steel_chest, jeiI.armor_steel_legs, jeiI.armor_steel_feet,
				] },
				{ id: 'faraday', sort: ['list', 'mod', 'creative'], list: [
					jeiI.armor_faraday_head, jeiI.armor_faraday_chest, jeiI.armor_faraday_legs, jeiI.armor_faraday_feet,
				] },
				{ id: 'compressed_iron', sort: ['list', 'mod', 'creative'], list: [
					jeiI.compressed_iron_helmet, jeiI.compressed_iron_chestplate, jeiI.compressed_iron_leggings,
					jeiI.compressed_iron_boots, jeiI.pneumatic_helmet, jeiI.pneumatic_chestplate, jeiI.pneumatic_leggings,
					jeiI.pneumatic_boots,
				] },
				{ id: 'quartz', sort: ['list', 'mod', 'creative'], list: [
					jeiI.certus_quartz_sword, jeiI.certus_quartz_pickaxe, jeiI.certus_quartz_axe, jeiI.certus_quartz_shovel,
					jeiI.certus_quartz_hoe, jeiI.certus_quartz_cutting_knife, jeiI.nether_quartz_sword, jeiI.nether_quartz_pickaxe,
					jeiI.nether_quartz_axe, jeiI.nether_quartz_shovel, jeiI.nether_quartz_hoe, jeiI.nether_quartz_cutting_knife,
				] },
				{ id: 'fluix', sort: ['list', 'mod', 'creative'], list: [
					jeiI.fluix_sword, jeiI.fluix_pickaxe, jeiI.fluix_axe, jeiI.fluix_shovel, jeiI.fluix_hoe,
				] },
				{ id: 'botania', sort: ['list', 'mod', 'creative'], list: [
					jeiI.glass_pickaxe, jeiI.manasteel_sword, jeiI.manasteel_pick, jeiI.manasteel_axe, jeiI.manasteel_shovel,
					jeiI.manasteel_hoe, jeiI.manasteel_knife, jeiI.ender_dagger, jeiI.manasteel_helmet, jeiI.manasteel_chestplate,
					jeiI.manasteel_leggings, jeiI.manasteel_boots, jeiI.manasteel_shears, jeiI.manaweave_helmet,
					jeiI.manaweave_chestplate, jeiI.manaweave_leggings, jeiI.manaweave_boots, jeiI.elementium_sword,
					jeiI.elementium_pickaxe, jeiI.elementium_axe, jeiI.elementium_shovel, jeiI.elementium_hoe,
					jeiI.elementium_knife, jeiI.elementium_helmet, jeiI.elementium_chestplate, jeiI.elementium_leggings,
					jeiI.elementium_boots, jeiI.elementium_shears, jeiI.terra_sword, jeiI.terra_pick, jeiI.terra_axe,
					jeiI.terra_knife, jeiI.terrasteel_helmet, jeiI.terrasteel_chestplate, jeiI.terrasteel_leggings,
					jeiI.terrasteel_boots, jeiI.star_sword, jeiI.thunder_sword, jeiI.alf_knife,
				] },
				{ id: 'wardrobe', sort: ['list', 'mod', 'creative'], list: [
					jeiI.desert_helmet, jeiI.desert_chestplate, jeiI.desert_leggings, jeiI.desert_boots, jeiI.snowy_helmet,
					jeiI.snowy_chestplate, jeiI.snowy_leggings, jeiI.snowy_boots, jeiI.taiga_helmet, jeiI.taiga_chestplate,
					jeiI.taiga_leggings, jeiI.taiga_boots, jeiI.jungle_chestplate, jeiI.jungle_leggings, jeiI.jungle_boots,
					jeiI.savanna_chestplate, jeiI.savanna_leggings, jeiI.savanna_boots, jeiI.snowy_hood_covered_helmet,
					jeiI.hat_helmet, jeiI.leather_top_hat_helmet, jeiI.newsboy_cap_helmet, jeiI.farmers_hat_helmet,
					jeiI.fletchers_hat_helmet, jeiI.fox_hat_helmet, jeiI.snow_fox_hat_helmet, jeiI.cake_hat_helmet, jeiI.book_hat,
					jeiI.book_hat_2, jeiI.book_hat_3, jeiI.wool_vest_chestplate, jeiI.broken_barrel_chestplate, jeiI.chiton,
					jeiI.leather_apron, jeiI.brewing_apron, jeiI.fishing_apron, jeiI.breathing_reed, jeiI.lantern_on_a_stick,
					jeiI.soul_lantern_on_a_stick, jeiI.wardrobe_backpack, jeiI.large_backpack, jeiI.travel_backpack,
					jeiI.white_scarf, jeiI.light_gray_scarf, jeiI.gray_scarf, jeiI.black_scarf, jeiI.brown_scarf, jeiI.red_scarf,
					jeiI.orange_scarf, jeiI.yellow_scarf, jeiI.lime_scarf, jeiI.green_scarf, jeiI.cyan_scarf, jeiI.light_blue_scarf,
					jeiI.blue_scarf, jeiI.purple_scarf, jeiI.magenta_scarf, jeiI.pink_scarf,
				] },
				{ id: 'cardboard', sort: ['list', 'mod', 'creative'], list: [
					jeiI.cardboard_helmet, jeiI.cardboard_chestplate, jeiI.cardboard_leggings, jeiI.cardboard_boots,
				] },
				{ id: 'vinery', sort: ['list', 'mod', 'creative'], list: [
					jeiI.straw_hat, jeiI.vinemaker_apron, jeiI.vinemaker_leggings, jeiI.vinemaker_boots,
				] },
				{ id: 'floral', sort: ['list', 'mod', 'creative'], list: [
					jeiI.white_crown, jeiI.yellow_crown, jeiI.red_crown, jeiI.pink_crown, jeiI.blue_crown, jeiI.black_crown,
					jeiI.tulips_crown,
				] },
				{ id: 'alexsmobs', sort: ['list', 'mod', 'creative'], list: [
					jeiI.skelewag_sword, jeiI.tendon_whip, jeiI.ghostly_pickaxe, jeiI.froststalker_helmet, jeiI.moose_headgear,
					jeiI.frontier_cap, jeiI.fedora, jeiI.sombrero, jeiI.novelty_hat, jeiI.spiked_turtle_shell,
					jeiI.crocodile_chestplate, jeiI.rocky_chestplate, jeiI.unsettling_kimono, jeiI.tarantula_hawk_elytra,
					jeiI.centipede_leggings, jeiI.emu_leggings, jeiI.flying_fish_boots, jeiI.roadrunner_boots, jeiI.pigshoes,
					jeiI.shield_of_the_deep, jeiI.straddle_helmet, jeiI.straddle_saddle, jeiI.squid_grapple, jeiI.vine_lasso,
					jeiI.falconry_glove, jeiI.blood_sprayer, jeiI.hemolymph_blaster, jeiI.stink_ray, jeiI.pocket_sand,
					jeiI.ancient_dart, jeiI.shattered_dimensional_carver,
				] },
				{ id: 'misc', children: [
					{ id: 'vanilla', sort: ['list', 'mod', 'creative'], list: [
						jeiI.bow, jeiI.crossbow, jeiI.trident, jeiI.shield, jeiI.fishing_rod, jeiI.carrot_on_a_stick,
						jeiI.warped_fungus_on_a_stick, jeiI.flint_and_steel, jeiI.compass, jeiI.recovery_compass, jeiI.clock,
						jeiI.spyglass, jeiI.bundle, jeiI.lead, jeiI.saddle, jeiI.elytra, jeiI.totem_of_undying,
					] },
					{ id: 'modded', sort: ['list', 'mod', 'creative'], list: [
						jeiI.hammer, jeiI.pickarang, jeiI.trowel, jeiI.abacus, jeiI.backpack, jeiI.forgotten_hat, jeiI.quiver,
						jeiI.slingshot, jeiI.bomb, jeiI.blue_bomb, jeiI.spiky_bomb, jeiI.spring_launcher, jeiI.bubble_blower,
						jeiI.flute, jeiI.altimeter, jeiI.key, jeiI.sup_wrench, jeiI.custom_elytra, jeiI.elytra_wing,
						jeiI.deflection_shield, jeiI.domesticationinnovation_magnet, jeiI.ice_sickle,
						jeiI.sophisticatedbackpacks_backpack,
					] },
					{ id: 'knives', sort: ['list', 'mod', 'name'],
						from: ['delightful:*_knife'],
						except: [jeiI.delightful_copper_knife, jeiI.steel_knife, jeiI.manasteel_knife, jeiI.elementium_knife, jeiI.terra_knife, jeiI.alf_knife], list: [
						jeiI.flint_knife, jeiI.end_stone_knife, jeiI.purpur_knife, jeiI.dragon_egg_shell_knife,
						jeiI.dragon_tooth_knife, jeiI.abnormals_delight_silver_knife, jeiI.necromium_knife,
					] },
					{ id: 'arrows', sort: ['list', 'mod', 'name'],
						from: ['@nock_enough_arrows'], list: [
						jeiI.arrow, jeiI.spectral_arrow, jeiI.tipped_arrow, jeiI.arrow_fletching, jeiI.torch_arrow, jeiI.rope_arrow,
						jeiI.shark_tooth_arrow, jeiI.balloon_arrow,
					] },
					{ id: 'guns', sort: ['mod', 'name'], modOrder: ['oldguns', 'immersiveengineering', 'pneumaticcraft'],
						from: ['oldguns:matchlock_*', 'oldguns:wheellock_*', 'oldguns:flintlock_*', 'oldguns:caplock_*', 'oldguns:*_musket_ball*', 'oldguns:*shot', 'oldguns:*_cannonball', 'oldguns:*_shell', 'oldguns:*_rocket', 'oldguns:*_cannon_barrel'],
						except: ['oldguns:*_mechanism', 'oldguns:*_repair_parts'], list: [
						jeiI.musketeer_hat, jeiI.horsemans_pot_helm, jeiI.powder_horn, jeiI.ram_rod, jeiI.gunners_quadrant,
						jeiI.bombard, jeiI.medium_naval_cannon, jeiI.revolver, jeiI.speedloader, jeiI.casull, jeiI.armor_piercing,
						jeiI.buckshot, jeiI.he, jeiI.dragons_breath, jeiI.silver, jeiI.immersiveengineering_potion, jeiI.flare,
						jeiI.immersiveengineering_firework, jeiI.wolfpack, jeiI.toolupgrade_revolver_bayonet,
						jeiI.toolupgrade_revolver_magazine, jeiI.toolupgrade_revolver_electro, jeiI.railgun,
						jeiI.toolupgrade_railgun_scope, jeiI.toolupgrade_railgun_capacitors, jeiI.chemthrower,
						jeiI.toolupgrade_chemthrower_focus, jeiI.toolupgrade_chemthrower_multitank, jeiI.minigun, jeiI.gun_ammo,
						jeiI.gun_ammo_ap, jeiI.gun_ammo_explosive, jeiI.gun_ammo_freezing, jeiI.gun_ammo_incendiary,
						jeiI.gun_ammo_weighted,
					] },
					{ id: 'tech', sort: ['list', 'mod', 'creative'], list: [
						jeiI.wrench, jeiI.goggles, jeiI.extendo_grip, jeiI.wand_of_symmetry, jeiI.handheld_worldshaper,
						jeiI.potato_cannon, jeiI.linked_controller, jeiI.sand_paper, jeiI.red_sand_paper, jeiI.super_glue,
						jeiI.minecart_coupling, jeiI.sticky_launcher, jeiI.brass_sticky_launcher, jeiI.spout_gun, jeiI.spout_gun_water,
						jeiI.spout_gun_lava, jeiI.spout_gun_honey, jeiI.spout_gun_chocolate, jeiI.spout_gun_slime,
						jeiI.sticky_boots_boots, jeiI.portable_whistle, jeiI.netherite_portable_whistle, jeiI.deforester_saw,
						jeiI.excavation_drill, jeiI.gilded_rose_sword, jeiI.gilded_rose_pickaxe, jeiI.gilded_rose_axe,
						jeiI.gilded_rose_shovel, jeiI.gilded_rose_hoe, jeiI.modular_drill, jeiI.drill_crusher_head,
						jeiI.drill_enchantable_head, jeiI.drill_smelting_head, jeiI.drill_vein_head, jeiI.visor_helmet, jeiI.jetpack,
						jeiI.clockwork_crossbow, jeiI.beam_reactor_helmet, jeiI.electrum_amulet, jeiI.ie_hammer, jeiI.wirecutter,
						jeiI.screwdriver, jeiI.voltmeter, jeiI.survey_tools, jeiI.drill, jeiI.drillhead_iron, jeiI.drillhead_steel,
						jeiI.toolupgrade_drill_capacity, jeiI.toolupgrade_drill_damage, jeiI.toolupgrade_drill_fortune,
						jeiI.toolupgrade_drill_lube, jeiI.toolupgrade_drill_waterproof, jeiI.buzzsaw, jeiI.sawblade, jeiI.rockcutter,
						jeiI.toolupgrade_buzzsaw_spareblades, jeiI.immersiveengineering_shield, jeiI.toolupgrade_shield_flash,
						jeiI.toolupgrade_shield_magnet, jeiI.toolupgrade_shield_shock, jeiI.skyhook, jeiI.glider, jeiI.earmuffs,
						jeiI.powerpack, jeiI.toolupgrade_powerpack_antenna, jeiI.toolupgrade_powerpack_induction,
						jeiI.toolupgrade_powerpack_tesla, jeiI.maintenance_kit, jeiI.pneumatic_wrench, jeiI.manometer,
						jeiI.logistics_configurator, jeiI.camo_applicator, jeiI.gps_tool, jeiI.gps_area_tool, jeiI.amadron_tablet,
						jeiI.jackhammer, jeiI.drill_bit_iron, jeiI.drill_bit_compressed_iron, jeiI.drill_bit_diamond,
						jeiI.drill_bit_netherite, jeiI.air_cannon, jeiI.vortex_cannon, jeiI.elytra_upgrade, jeiI.jet_boots_upgrade_1,
						jeiI.jet_boots_upgrade_2, jeiI.jet_boots_upgrade_3, jeiI.jet_boots_upgrade_4, jeiI.jet_boots_upgrade_5,
						jeiI.certus_quartz_wrench, jeiI.nether_quartz_wrench, jeiI.network_tool, jeiI.meteorite_compass,
						jeiI.charged_staff, jeiI.matter_cannon, jeiI.stone_wand, jeiI.iron_wand, jeiI.diamond_wand, jeiI.infinity_wand,
						jeiI.physics_gun, jeiI.supercharged_gravity_gun, jeiI.displacer_cannon, jeiI.tool_gun, jeiI.portal_gun,
						jeiI.golden_portal_gun, jeiI.prime_portal_gun,
					] },
					{ id: 'magic', sort: ['list', 'mod', 'creative'], list: [
						jeiI.twig_wand, jeiI.dreamwood_wand, jeiI.livingwood_bow, jeiI.crystal_bow, jeiI.botania_slingshot,
						jeiI.mana_gun, jeiI.light_launcher, jeiI.mana_bomb, jeiI.divining_rod, jeiI.dirt_rod, jeiI.skydirt_rod,
						jeiI.cobble_rod, jeiI.rainbow_rod, jeiI.water_rod, jeiI.fire_rod, jeiI.smelt_rod, jeiI.terraform_rod,
						jeiI.tornado_rod, jeiI.gravity_rod, jeiI.missile_rod, jeiI.exchange_rod, jeiI.magnet_ring,
						jeiI.magnet_ring_greater, jeiI.balance_cloak, jeiI.holy_cloak, jeiI.unholy_cloak, jeiI.invisibility_cloak,
						jeiI.diva_charm, jeiI.goddess_charm, jeiI.staff_of_life, jeiI.whistle, jeiI.apprentice_witch_hat,
						jeiI.gold_witch_hat, jeiI.diamond_witch_hat, jeiI.amethyst_witch_hat, jeiI.witch_suit, jeiI.basic_broom_item,
						jeiI.woodshade_broom_item, jeiI.leafshade_broom_item, jeiI.goldenwood_broom_item, jeiI.swiftstick_broom_item,
						jeiI.snowair_broom_item, jeiI.blue_bottle_broom_item, jeiI.firebolt_broom_item, jeiI.wings_broom_item,
						jeiI.silver_arrow_broom_item,
					] },
					{ id: 'hats', sort: ['list', 'mod', 'creative'],
						from: ['@simplehats'], list: [
						jeiI.flower_crown, jeiI.durian_helmet, jeiI.propeller_hat, jeiI.mysterious_beanie_helmet,
						jeiI.white_conductor_cap, jeiI.light_gray_conductor_cap, jeiI.gray_conductor_cap, jeiI.black_conductor_cap,
						jeiI.brown_conductor_cap, jeiI.red_conductor_cap, jeiI.orange_conductor_cap, jeiI.yellow_conductor_cap,
						jeiI.lime_conductor_cap, jeiI.green_conductor_cap, jeiI.cyan_conductor_cap, jeiI.light_blue_conductor_cap,
						jeiI.blue_conductor_cap, jeiI.purple_conductor_cap, jeiI.magenta_conductor_cap, jeiI.pink_conductor_cap,
						jeiI.cosmetic_ancient_mask, jeiI.cosmetic_black_bowtie, jeiI.cosmetic_clock_eye, jeiI.cosmetic_eerie_mask,
						jeiI.cosmetic_engineer_goggles, jeiI.cosmetic_lusitanic_shield, jeiI.cosmetic_polka_dotted_bows,
						jeiI.cosmetic_puffy_scarf, jeiI.cosmetic_tiny_potato_mask,
					] },
				] },
			] },
			{ id: 'storage_delight', sort: ['list', 'mod', 'creative'], list: jeiStorageList },
		],

		multiblock: [
			{ id: 'immersiveengineering', children: [
				{ id: 'stone', sort: ['list'], list: [
					jeiMb.coke_oven, jeiMb.alloy_smelter, jeiMb.blast_furnace, jeiMb.improved_blast_furnace,
				] },
				{ id: 'machines', sort: ['list', 'name'], list: [
					jeiMb.crusher, jeiMb.sawmill, jeiMb.metal_press, jeiMb.assembler, jeiMb.auto_workbench,
					jeiMb.bottling_machine, jeiMb.squeezer, jeiMb.fermenter, jeiMb.refinery, jeiMb.mixer,
					jeiMb.arc_furnace, jeiMb.excavator, jeiMb.bucket_wheel,
				] },
				{ id: 'power_storage', sort: ['list', 'name'], list: [
					jeiMb.diesel_generator, jeiMb.lightning_rod, jeiMb.silo, jeiMb.sheetmetal_tank,
				] },
			] },
		],

		fluid: [
			{ id: 'vanilla', sort: ['list', 'mod', 'creative'], list: [jeiF.water, jeiF.lava, jeiF.milk] },
		],

		effect: [
			{ id: 'beneficial', sort: ['list', 'name'], list: [
				jeiE.speed, jeiE.haste, jeiE.strength, jeiE.jump_boost, jeiE.regeneration, jeiE.resistance,
				jeiE.fire_resistance, jeiE.water_breathing, jeiE.night_vision, jeiE.invisibility,
				jeiE.health_boost, jeiE.absorption, jeiE.saturation, jeiE.instant_health, jeiE.luck,
				jeiE.slow_falling, jeiE.conduit_power, jeiE.dolphins_grace, jeiE.hero_of_the_village,
			] },
		],
	},

	packHidden: {
		// dead placeholder fluids (server_scripts/tags/fluidtags.js strips their tags); fluid_jei.js skips them
		fluid: [jeiF.tmted_melon_juice, jeiF.tmted_tomato_sauce, jeiF.tmted_apple_cider, jeiF.thatch],
	},
})
