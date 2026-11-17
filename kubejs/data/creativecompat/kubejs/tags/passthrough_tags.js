// requires: creativecompat
// Creative Compat pass-through blocks (feature_writeup/plan_passthrough.md, surface/PassThrough): the eleven block tiers
// creativecompat:passthrough/<tier> (the layer tier's tag is creativecompat:passthrough_layers), the per-block growth override, and the entity-type tags (exempt mobs per tier,
// exempt from everything, projectile web groups). Values per tier: config/creativecompat-common.toml
// [surface.passthrough.<tier>]. Offline check: node tools/passthrough_tag_sim.js (members, overlaps, unknown ids).
// The exempt tags ship in this same file as the cobweb tier: without them spiders and modded bullets get web slowdown.

var PASS = {
	dense_crop: 'creativecompat:passthrough/dense_crop', // 0.75, 3+ of the 4 side neighbours 0.50, tall_half
	crop:       'creativecompat:passthrough/crop',       // 0.75 from half grown
	wild_crop:  'creativecompat:passthrough/wild_crop',  // 0.75
	vine:       'creativecompat:passthrough/vine',       // 0.75 while moving through
	tall_plant: 'creativecompat:passthrough/tall_plant', // 0.75
	cane:       'creativecompat:passthrough/cane',       // 0.50
	rope:       'creativecompat:passthrough/rope',       // 0.75, not while climbing
	cobweb:     'creativecompat:passthrough/cobweb',     // 0.50 / fall 0.85, 3+ of the 6 face neighbours 0.35 / 0.20, 6 of 6 surrounded
	bush:       'creativecompat:passthrough/bush',       // 0.36
	layers:     'creativecompat:passthrough_layers',     // by height: 0.5 at the highest walk-through layer (5 of 8)
	grass:      'creativecompat:passthrough/grass',      // 1.00 (listed so it can be tuned)
}

ServerEvents.tags('block', e => {
	var t = global.tags
	var b = global.blocks
	var i = global.item
	var tiers = Object.keys(PASS).map(k => PASS[k])
	// Pass-through tiers are a set of their own: a block can sit in a BlockRunner surface tier and a pass-through tier.
	// One pass-through tier per block (overlaps below). The more specific tiers are filled first; the broad ones
	// (crop, tall_plant, grass) take out everything already tiered.
	var tiered = () => tiers.map(tag => '#' + tag)

	// Cobwebs and barbed wire, by id: none of the modded webs extends WebBlock (phase 0 brief 3)
	t.add(e, PASS.cobweb, [i.cobweb, 'securitycraft:reinforced_cobweb', 'fantasyfurniture:decorations/spider_web_small',
		'fantasyfurniture:decorations/spider_web_wide', i.barbed_wire, i.razor_wire])  // razor wire: owner 2026-11-15

	// Dense crops; tomatoes on ropes and sticks sit here, not in rope. Rice panicles count as rice's neighbours.
	t.add(e, PASS.dense_crop, [b.corn_crop, b.hemp, i.farmersdelight_rice,
		b.rice_panicles, b.oat_crop, b.wheat_crop, b.tomatoes,
		'farmersdelight:budding_tomatoes', b.rope_tomatoes, b.stick_tomatoes,
		b.treated_stick_tomatoes])

	// Ropes (Beautify's rope is a solid chain: out)
	t.add(e, PASS.rope, [i.fd_rope, i.supplementaries_rope, i.quark_rope])

	// Canes: sugar cane and the four Nether's Delight propelplant blocks (walking in unsneaked still makes them explode)
	t.add(e, PASS.cane, [i.sugar_cane, 'nethersdelight:propelplant_stem', 'nethersdelight:propelplant_berry_stem',
		i.propelplant_cane, 'nethersdelight:propelplant_berry_cane'])

	// Berry bushes: sweet berry, the Hearth and Harvest raspberry, every Vinery grape bush (the taiga bushes slow newly)
	t.add(e, PASS.bush, [b.sweet_berry_bush, 'hearthandharvest:raspberry_bush', 'vinery:red_grape_bush',
		'vinery:white_grape_bush', 'vinery:savanna_grape_bush_red', 'vinery:savanna_grape_bush_white',
		'vinery:taiga_grape_bush_red', 'vinery:taiga_grape_bush_white',
		'delightful:salmonberry_bush', b.cranberry_bush])  // salmonberry, cranberry: owner 2026-11-15

	// Layer height blocks (plan_post_scratch_1115b.md D1, 2026-11-15): one rule by height for every stacked-layer block.
	// Layer height blocks (owner 2026-11-16): snow layers, every Immersive Weathering leaf pile (azalea flower pile
	// included) and Twigs bamboo leaves. Sand, ash, fodder, sawdust and salt layers are dust/soft: no walk-through.
	t.addMatching(e, PASS.layers, [b.snow, '/^immersive_weathering:([a-z_]+\\/)?[a-z_]*(leaf|flower)_pile$/', i.bamboo_leaves])
	// Leaf layers never act whole (owner 2026-11-16): no solidBelowFull for them. Snow layers keep it.
	t.addMatching(e, 'creativecompat:passthrough_layers_open', ['#' + PASS.layers], { except: [b.snow] })

	// Vines: vanilla, Vinery jungle grape vines, Hearth hops, Floral Enchantment, Quark, Botania, Immersive Weathering
	// ivy, Upgrade Aquatic mulberry, Neapolitan vanilla, Biomes O' Plenty strands (flesh tendons, and hanging cobweb,
	// spanish moss and glowworm silk where Biomes O' Plenty registers them)
	t.addMatching(e, PASS.vine, [i.vine, i.twisting_vines, 'minecraft:twisting_vines_plant',
		i.weeping_vines, 'minecraft:weeping_vines_plant', 'minecraft:cave_vines', 'minecraft:cave_vines_plant',
		b.jungle_grape_bush_red, b.jungle_grape_bush_white, b.hops_vine,
		'floralench:*_vine', i.cut_vine, 'botania:solid_vine', i.ivy,
		b.mulberry_vine, b.vanilla_vine, b.vanilla_vine_plant,
		'/^biomesoplenty:(flesh_tendons|hanging_cobweb|spanish_moss|glowworm_silk)(_strand|_plant)?$/'])

	// Wild crops: Farmer's Delight's tag plus the wild plants it misses (Supplementaries wild flax stays out with flax)
	t.addMatching(e, PASS.wild_crop, [i.t_wild_crops, i.wild_oats,
		i.wild_cucumbers, i.wild_ghost_peppers, i.wild_peanuts,
		i.wild_coffee_bush, i.wild_tea_bush, i.wild_pineapple,
		i.wild_hops], { except: tiered().filter(x => x !== '#' + PASS.wild_crop) })

	// Planted crops: #minecraft:crops misses many modded ones. Supplementaries flax is out (owner, 2026-11-14).
	// Farmer's Respite coffee: 2+ blocks from planting, growth `any` (passthrough_growth_any below).
	t.addMatching(e, PASS.crop, ['#minecraft:crops', 'minecraft:attached_melon_stem', 'minecraft:attached_pumpkin_stem',
		b.nether_wart_crop, 'farmersrespite:coffee_bush', 'farmersrespite:coffee_bush_top',
		'farmersrespite:coffee_stem', 'farmersrespite:coffee_stem_middle', 'farmersrespite:coffee_stem_double',
		'farmersrespite:tea_bush', 'farmersrespite:small_tea_bush', 'delightful:cantaloupe_plant',
		b.sunflower_stem, b.strawberry_bush, 'neapolitan:mint', b.adzuki_sprouts,
		i.pineapple],
		{ except: [b.flax_crop].concat(tiered().filter(x => x !== '#' + PASS.crop)) })

	// 2-tall plants. Upgrade Aquatic tall pickerelweed keeps its own slowdown (unlisted stuck plants stay outside).
	t.addMatching(e, PASS.tall_plant, ['#minecraft:tall_flowers', i.tall_grass, i.large_fern,
		i.small_dripleaf, b.eyebulb, i.tall_beachgrass],
		{ except: [b.tall_blue_pickerelweed, b.tall_purple_pickerelweed]
			.concat(tiered().filter(x => x !== '#' + PASS.tall_plant)) })

	// Short plants at 1.00. Left out on purpose (they keep their own slowdown, owner 2026-11-14): Floral Enchantment
	// bushes, Farmer's Respite wither roots, Neapolitan beanstalk thorns, Upgrade Aquatic pickerelweed.
	t.addMatching(e, PASS.grass, [i.grass, i.fern, i.dead_bush, i.nether_sprouts,
		i.crimson_roots, i.warped_roots, i.hanging_roots, 'immersive_weathering:hanging_roots_wall',
		i.frosty_grass, i.frosty_fern, i.weeds,
		i.beachgrass, i.sandy_shrub, i.t_small_flowers, i.t_saplings,
		'flowerpatch:*_patch', i.gossypium,
		// Botania flowers and mushrooms (owner 2026-11-15); floating flowers are solid, left out
		'#botania:mystical_flowers', '#botania:shiny_flowers', i.t_botania_special_flowers, '/^botania:[a-z_]+_mushroom$/'],
		{ except: [i.azalea, i.flowering_azalea, 'floralench:*_bush', 'farmersrespite:wither_roots', 'farmersrespite:wither_roots_plant',
			i.beanstalk_thorns, i.blue_pickerelweed, i.purple_pickerelweed]
			.concat(tiered().filter(x => x !== '#' + PASS.grass)) })

	// Blocks whose growth never gates the tier (plan 1.1): a tomato segment's age is fruit ripeness and resets on
	// harvest; coffee is 2+ blocks from planting and its stems' age is berry ripeness. Budding tomatoes keep `half`.
	t.add(e, 'creativecompat:passthrough_growth_any', [b.tomatoes, b.rope_tomatoes,
		b.stick_tomatoes, b.treated_stick_tomatoes, 'farmersrespite:coffee_bush',
		'farmersrespite:coffee_bush_top', 'farmersrespite:coffee_stem', 'farmersrespite:coffee_stem_middle',
		'farmersrespite:coffee_stem_double'])

	t.overlaps(e, tiers, 'Pass-through tier')
})

ServerEvents.tags('entity_type', e => {
	var t = global.tags
	var P = 'creativecompat:passthrough/projectile/'

	// Mobs a tier never slows (vanilla and Immersive Weathering rules, kept tier-wide: spiders now pass reinforced and
	// Fantasy Furniture webs and barbed wire too; barbed wire still hurts them)
	t.add(e, 'creativecompat:passthrough_exempt/cobweb', ['minecraft:spider', 'minecraft:cave_spider'])
	t.add(e, 'creativecompat:passthrough_exempt/bush', ['minecraft:fox', 'minecraft:bee'])
	// foxes and bees: Immersive Weathering's leaf pile rule, kept tier-wide (snow and the other layers too)
	t.add(e, 'creativecompat:passthrough_exempt/layers', ['minecraft:fox', 'minecraft:bee'])

	// Projectile web groups (plan 5.1). Without a tag a projectile runs on its base-class default (surface/PassGroup);
	// tools/projectile_scan.py lists the ones with no tag. Vanilla rows are listed too, so the tags are the full table.
	t.add(e, P + 'break', ['minecraft:snowball', 'minecraft:fireball', 'minecraft:small_fireball',
		'minecraft:dragon_fireball', 'minecraft:wither_skull', 'minecraft:llama_spit', 'minecraft:shulker_bullet',
		'minecraft:ender_pearl', 'minecraft:egg', 'untitledduckmod:duck_egg', 'untitledduckmod:goose_egg',
		'alexsmobs:emu_egg', 'alexsmobs:cockroach_egg', 'alexsmobs:tossed_item', 'alexsmobs:enderiophage_rocket',
		'quark:parrot_egg', 'twigs:pebble', 'supplementaries:bomb', 'supplementaries:brick_projectile',
		'supplementaries:slingshot_projectile', 'botania:vine_ball', 'botania:magic_missile',
		'pneumaticcraft:micromissile', 'pneumaticcraft:vortex', 'immersivepetroleum:molotov', 'creativecompat:shrapnel',
		'farmersdelight:rotten_tomato', 'hearthandharvest:manure_projectile', 'canned_goods:projectile_mini_fizz',
		'create_things_and_misc:projectile_blaze_balloon_fire'])
	t.add(e, P + 'settle', ['minecraft:potion', 'minecraft:experience_bottle',
		'create_enchantment_industry:hyper_experience_bottle', 'splash_milk:milk_bottle', 'botania:ender_air_bottle'])
	t.add(e, P + 'snag', ['minecraft:fishing_bobber', 'domesticationinnovation:feather', 'quark:pickarang',
		'quark:flamerang', 'quark:echorang', 'botania:thorn_chakram'])
	t.add(e, P + 'linger', ['minecraft:firework_rocket'])
	t.add(e, P + 'hang', ['minecraft:arrow', 'minecraft:spectral_arrow', 'minecraft:trident', 'dragonloot:dragon_trident',
		'neapolitan:bananarrow', 'alexsmobs:shark_tooth_arrow', 'supplementaries:rope_arrow', 'quark:torch_arrow',
		'hearthandharvest:thrown_cleaver', 'hearthandharvest:thrown_pitchfork', 'hearthandharvest:thrown_horseshoe',
		'create_things_and_misc:projectile_neon_tube'])
	// Nock Enough Arrows: every arrow type
	t.add(e, P + 'hang', [
		'blossom_arrow', 'cupid_arrow', 'dousing_arrow', 'drill_arrow', 'echoing_arrow', 'ethereal_arrow',
		'explosive_arrow', 'growing_arrow', 'hookshot_arrow', 'ink_arrow', 'luminous_arrow', 'message_arrow',
		'paint_arrow', 'party_arrow', 'prismarine_arrow', 'pufferfish_arrow', 'redstone_torch_arrow',
		'reinforced_arrow', 'repulsive_arrow', 'seeker_arrow', 'slime_arrow', 'soul_torch_arrow',
		'split_arrow', 'teleportation_arrow', 'torch_arrow', 'whistling_arrow',
	].map(n => 'nock_enough_arrows:' + n))
	// Botania's throwable copy is abstract (never registered): nothing is `slow` by tag today
	t.add(e, P + 'slow', [])
	// Modded bullets and munitions: never slowed (owner). IE shots extend AbstractArrow and Botania's bursts are
	// throwables, so without this tag they would hang or break.
	t.add(e, P + 'exempt', ['oldguns:bullet_projectile', 'oldguns:rocket_projectile',
		'immersiveengineering:revolver_shot', 'immersiveengineering:revolver_shot_flare',
		'immersiveengineering:revolver_shot_homing', 'immersiveengineering:revolver_shot_wolfpack',
		'immersiveengineering:railgun_shot', 'immersiveengineering:chemthrower_shot', 'immersiveengineering:sawblade',
		'securitycraft:bullet', 'securitycraft:imsbomb', 'create:potato_projectile',
		'create_things_and_misc:projectile_spout_gun_chocolate', 'create_things_and_misc:projectile_spout_gun_honey',
		'create_things_and_misc:projectile_spout_gun_lava', 'create_things_and_misc:projectile_spout_gun_slime',
		'create_things_and_misc:projectile_spout_gun_water', 'powergrid:zap_projectile', 'botania:mana_burst',
		'botania:babylon_weapon', 'botania:falling_star', 'pneumaticcraft:tumbling_block'])
	// Blocky Siege munitions (all 13; the siege engines themselves are not projectiles and stay slowed)
	t.add(e, P + 'exempt', [
		'projectile_barrel_of_gunpowder_projectile', 'projectile_cannon_ball_projectile_projectile',
		'projectile_fire_bomb', 'projectile_frost_bomb', 'projectile_giant_cannon_ball_projectile',
		'projectile_giant_fire_bomb_projectile', 'projectile_giant_frost_bomb_projectile',
		'projectile_giant_sculk_bomb_projectile', 'projectile_ice_drop', 'projectile_keg_of_gunpowder',
		'projectile_lava_drop', 'projectile_sculk_bomb', 'projectile_wooden_shrapnel',
	].map(n => 'blocky_siege:' + n))
	t.add(e, 'creativecompat:passthrough_exempt/all', '#' + P + 'exempt')

	t.overlaps(e, ['break', 'settle', 'snag', 'linger', 'hang', 'slow', 'exempt'].map(g => P + g), 'Projectile group')
})
