// requires: creativecompat
// Creative Compat pass-through blocks (feature_writeup/plan_passthrough.md, surface/PassThrough): the eleven block tiers
// creativecompat:passthrough/<tier> (the layer tier's tag is creativecompat:passthrough_layers), the per-block growth override, and the entity-type tags (exempt mobs per tier,
// exempt from everything, projectile web groups). Values per tier: config/creativecompat-common.toml
// [surface.passthrough.<tier>]. Offline check: node tools/passthrough_tag_sim.js (members, overlaps, unknown ids).
// The exempt tags ship in this same file as the cobweb tier: without them spiders and modded bullets get web slowdown.

var PASS = {
	dense_crop: 'creativecompat:passthrough/dense_crop', // 0.75, 3+ of the 4 side neighbors 0.50, tall_half
	crop:       'creativecompat:passthrough/crop',       // 0.75 from half grown
	wild_crop:  'creativecompat:passthrough/wild_crop',  // 0.75
	vine:       'creativecompat:passthrough/vine',       // 0.75 while moving through
	tall_plant: 'creativecompat:passthrough/tall_plant', // 0.75
	cane:       'creativecompat:passthrough/cane',       // 0.50
	rope:       'creativecompat:passthrough/rope',       // 0.75, not while climbing
	cobweb:     'creativecompat:passthrough/cobweb',     // 0.50 / fall 0.85, 3+ of the 6 face neighbors 0.35 / 0.20, 6 of 6 surrounded
	bush:       'creativecompat:passthrough/bush',       // 0.36
	layers:     'creativecompat:passthrough_layers',     // by height: snow 0.4 at 7 of 8, leaf layers 0.6 at full
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
	t.add(e, PASS.cobweb, [i.cobweb, b.reinforced_cobweb, b.spider_web_small,
		b.spider_web_wide, i.barbed_wire, i.razor_wire])  // razor wire: owner

	// Dense crops; tomatoes on ropes and sticks sit here, not in rope. Rice panicles count as rice's neighbours.
	t.add(e, PASS.dense_crop, [b.corn_crop, b.hemp, i.farmersdelight_rice,
		b.rice_panicles, b.oat_crop, b.wheat_crop, b.tomatoes,
		b.budding_tomatoes, b.rope_tomatoes, b.stick_tomatoes,
		b.treated_stick_tomatoes])

	// Ropes (Beautify's rope is a solid chain: out)
	t.add(e, PASS.rope, [i.fd_rope, i.supplementaries_rope, i.quark_rope])

	// Canes: sugar cane and the four Nether's Delight propelplant blocks (walking in unsneaked still makes them explode)
	t.add(e, PASS.cane, [i.sugar_cane, b.propelplant_stem, b.propelplant_berry_stem,
		i.propelplant_cane, b.propelplant_berry_cane])

	// Berry bushes: sweet berry, the Hearth and Harvest raspberry, every Vinery grape bush (the taiga bushes slow newly)
	t.add(e, PASS.bush, [b.sweet_berry_bush, b.raspberry_bush, b.red_grape_bush,
		b.white_grape_bush, b.savanna_grape_bush_red, b.savanna_grape_bush_white,
		b.taiga_grape_bush_red, b.taiga_grape_bush_white,
		b.salmonberry_bush, b.cranberry_bush])  // salmonberry, cranberry: owner

	// Layer height blocks (plan_post_scratch_1115b.md D1, F1; plan_post_scratch_1115.md G7.4): snow layers, every
	// Immersive Weathering leaf pile (azalea flower pile included) and Twigs bamboo leaves. Sand, ash, fodder, sawdust
	// and salt layers are dust/soft: no walk-through, their BlockRunner surface speed only.
	t.addMatching(e, PASS.layers, [b.snow, '/^immersive_weathering:([a-z_]+\\/)?[a-z_]*(leaf|flower)_pile$/', i.bamboo_leaves,
		b.bamboo_leaves_full])  // full bamboo leaves: Twigs bamboo leaves past 4 layers (G7.9)
	// Leaf layers: walk-through at every height, leafMaxSlow at full. Snow layers: walk-through 1 to 7 (a full layer
	// becomes a snow block), snowMaxSlow at 7.
	// Snow! Real Magic's snowy plant is a snow layer too (snow_real_magic.md 4 step 5); the plant inside adds its own
	// tier's walk (surface/PassThrough, lowest walk wins)
	if (global.has('snowrealmagic')) t.add(e, PASS.layers, b.srm_snow)
	t.addMatching(e, 'creativecompat:passthrough_layers_open', ['#' + PASS.layers], { except: global.has('snowrealmagic') ? [b.snow, b.srm_snow] : [b.snow] })
	// Layers that fall and stack (plan_post_scratch_1115.md G7.5, surface/FallingLayers): the layer blocks with no gravity
	// of their own. Immersive Weathering's layers and Supplementaries ash already fall by their own code.
	t.add(e, 'creativecompat:falling_layers', [b.snow, i.vintagedelight_salt, i.bamboo_leaves,
		b.bamboo_leaves_full, i.immersiveengineering_sawdust, i.fodder])
	// Snow! Real Magic's snowy plant: listed for the pack fall, which leaves snow to SRM while it is loaded (R3)
	if (global.has('snowrealmagic')) t.add(e, 'creativecompat:falling_layers', b.srm_snow)

	// Vines: vanilla, Vinery jungle grape vines, Hearth hops, Floral Enchantment, Quark, Botania, Immersive Weathering
	// ivy, Upgrade Aquatic mulberry, Neapolitan vanilla, Biomes O' Plenty strands (flesh tendons, and hanging cobweb,
	// spanish moss and glowworm silk where Biomes O' Plenty registers them)
	t.addMatching(e, PASS.vine, [i.vine, i.twisting_vines, b.twisting_vines_plant,
		i.weeping_vines, b.weeping_vines_plant, b.cave_vines, b.cave_vines_plant,
		b.jungle_grape_bush_red, b.jungle_grape_bush_white, b.hops_vine,
		'floralench:*_vine', i.cut_vine, b.solid_vine, i.ivy,
		b.mulberry_vine, b.vanilla_vine, b.vanilla_vine_plant,
		'/^biomesoplenty:(flesh_tendons|hanging_cobweb|spanish_moss|glowworm_silk)(_strand|_plant)?$/'])

	// Wild crops: Farmer's Delight's tag plus the wild plants it misses (Supplementaries wild flax stays out with flax)
	t.addMatching(e, PASS.wild_crop, [i.t_wild_crops, i.wild_oats,
		i.wild_cucumbers, i.wild_ghost_peppers, i.wild_peanuts,
		i.wild_coffee_bush, i.wild_tea_bush, i.wild_pineapple,
		i.wild_hops], { except: tiered().filter(x => x !== '#' + PASS.wild_crop) })

	// Planted crops: #minecraft:crops misses many modded ones. Supplementaries flax is out (owner). Farmer's
	// Respite coffee: 2+ blocks from planting, growth `any` (passthrough_growth_any below).
	t.addMatching(e, PASS.crop, ['#minecraft:crops', b.attached_melon_stem, b.attached_pumpkin_stem,
		b.nether_wart_crop, b.coffee_bush, b.coffee_bush_top,
		b.coffee_stem, b.coffee_stem_middle, b.coffee_stem_double,
		b.tea_bush, b.small_tea_bush, b.cantaloupe_plant,
		b.sunflower_stem, b.strawberry_bush, b.mint_plant, b.adzuki_sprouts,
		i.pineapple],
		{ except: [b.flax_crop].concat(tiered().filter(x => x !== '#' + PASS.crop)) })

	// 2-tall plants. Upgrade Aquatic tall pickerelweed keeps its own slowdown (unlisted stuck plants stay outside).
	t.addMatching(e, PASS.tall_plant, ['#minecraft:tall_flowers', i.tall_grass, i.large_fern,
		i.small_dripleaf, b.eyebulb, i.tall_beachgrass],
		{ except: [b.tall_blue_pickerelweed, b.tall_purple_pickerelweed]
			.concat(tiered().filter(x => x !== '#' + PASS.tall_plant)) })

	// Short plants at 1.00. Left out on purpose (they keep their own slowdown, owner): Floral Enchantment bushes,
	// Farmer's Respite wither roots, Neapolitan beanstalk thorns, Upgrade Aquatic pickerelweed.
	t.addMatching(e, PASS.grass, [i.grass, i.fern, i.dead_bush, i.nether_sprouts,
		i.crimson_roots, i.warped_roots, i.hanging_roots, b.hanging_roots_wall,
		i.frosty_grass, i.frosty_fern, i.weeds,
		i.beachgrass, i.sandy_shrub, i.t_small_flowers, i.t_saplings,
		'flowerpatch:*_patch', i.gossypium,
		// Botania flowers and mushrooms (owner); floating flowers are solid, left out
		'#botania:mystical_flowers', '#botania:shiny_flowers', i.t_botania_special_flowers, '/^botania:[a-z_]+_mushroom$/'],
		{ except: [i.azalea, i.flowering_azalea, 'floralench:*_bush', b.wither_roots, b.wither_roots_plant,
			i.beanstalk_thorns, i.blue_pickerelweed, i.purple_pickerelweed]
			.concat(tiered().filter(x => x !== '#' + PASS.grass)) })

	// Blocks whose growth never gates the tier (plan 1.1): a tomato segment's age is fruit ripeness and resets on
	// harvest; coffee is 2+ blocks from planting and its stems' age is berry ripeness. Budding tomatoes keep `half`.
	t.add(e, 'creativecompat:passthrough_growth_any', [b.tomatoes, b.rope_tomatoes,
		b.stick_tomatoes, b.treated_stick_tomatoes, b.coffee_bush,
		b.coffee_bush_top, b.coffee_stem, b.coffee_stem_middle,
		b.coffee_stem_double])

	t.overlaps(e, tiers, 'Pass-through tier')
})

ServerEvents.tags('entity_type', e => {
	var t = global.tags
	var P = 'creativecompat:passthrough/projectile/'

	// Mobs a tier never slows (vanilla and Immersive Weathering rules, kept tier-wide: spiders now pass reinforced and
	// Fantasy Furniture webs and barbed wire too; barbed wire still hurts them)
	t.add(e, 'creativecompat:passthrough_exempt/cobweb', ['minecraft:spider', 'minecraft:cave_spider'])
	t.add(e, 'creativecompat:passthrough_exempt/bush', ['minecraft:fox', 'minecraft:bee'])
	// rabbits and cats: G7.14 (exempt from the slowdown and the thorns; the deep-pile path block still applies)
	t.add(e, 'creativecompat:passthrough_exempt/layers', ['minecraft:rabbit', 'minecraft:cat'])
	// foxes and bees: Immersive Weathering's leaf pile rule, leaf layers only (plan_misc_1117.md 1): they move through
	// leaf piles and bamboo leaves unslowed but never plan a path through a deep pile (surface/LeafThorns.pathType);
	// snow layers slow them as any other mob
	t.add(e, 'creativecompat:passthrough_exempt/leaf_layers', ['minecraft:fox', 'minecraft:bee'])
	// Light mobs stand on top of snow layers whatever their size (plan_post_scratch_1115.md G11; surface/SnowLayers).
	// Mobs with a hitbox under [surface.passthrough.layers] snowLightVolume count without the tag; vanilla's own
	// powder snow walkers are listed so they still stand on top with the size rule off (snowLightVolume = 0).
	// Light mobs stand on snow layers (G11). Owner ruling: cats, spiders, every bird (Alex's Mobs included);
	// the rest are small modded mobs picked from the entity dump. Mobs of an absent mod are skipped by the helper.
	t.add(e, 'creativecompat:snow_layer_light', ['minecraft:fox', 'minecraft:rabbit', 'minecraft:endermite',
		'minecraft:silverfish', 'minecraft:cat', 'minecraft:ocelot', 'minecraft:spider', 'minecraft:cave_spider',
		'minecraft:chicken', 'minecraft:parrot', 'minecraft:frog', 'minecraft:bat', 'minecraft:bee', 'minecraft:allay',
		'neapolitan:plantain_spider',
		'alexsmobs:bald_eagle', 'alexsmobs:blue_jay', 'alexsmobs:crow', 'alexsmobs:hummingbird',
		'alexsmobs:potoo', 'alexsmobs:roadrunner', 'alexsmobs:seagull', 'alexsmobs:soul_vulture',
		'alexsmobs:sunbird', 'alexsmobs:toucan',
		'untitledduckmod:duck', 'untitledduckmod:goose', 'upgrade_aquatic:goose',
		'alexsmobs:banana_slug', 'alexsmobs:capuchin_monkey', 'alexsmobs:cockroach', 'alexsmobs:crimson_mosquito',
		'alexsmobs:fly', 'alexsmobs:jerboa', 'alexsmobs:leafcutter_ant', 'alexsmobs:mudskipper', 'alexsmobs:platypus',
		'alexsmobs:raccoon', 'alexsmobs:rain_frog', 'alexsmobs:rattlesnake', 'alexsmobs:skunk', 'alexsmobs:sugar_glider',
		'alexsmobs:tarantula_hawk', 'alexsmobs:terrapin', 'alexsmobs:tasmanian_devil',
		'quark:crab', 'quark:stoneling', 'create_confectionery:little_gingerbread_man'])

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
