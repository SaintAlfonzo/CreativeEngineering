// requires: creativedelight
// requires: hearthandharvest
// Tags of the salt lick, the scarecrow and the crow mechanics on alexsmobs:crow (plan_hearth_crow_salt.md; hearth_and_harvest.md
// 5.8 N24-7 and N24-8). Read by the member's hearth/crow, hearth/salt and hearth/nest packages.

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// Crow food: AM's seeds (not its eggs and sugar) plus the pack's corn and corn seeds (N24-7). Taming, tempting, dropped food.
	t.add(e, 'creativedelight:crow_food', ['#forge:seeds', i.t_crops_corn, i.t_seeds_corn])

	// N24-8 list. Bricks sit in #forge:ingots in this pack and are not shiny; netherite and the end-tier metals are cut.
	var cut = [i.ingot_netherite, 'minecraft:netherite_scrap', 'create_crush_everything:netherite_shard', 'botania:terrasteel_ingot', 'botania:terrasteel_nugget',
		'create:refined_radiance', 'create:shadow_steel', 'create:experience_nugget',
		'*:*brick*']
	t.addMatching(e, 'creativedelight:crow_shiny_items', ['#forge:gems', '#forge:ingots', '#forge:nuggets', '#forge:raw_materials'], { except: cut })
	t.add(e, 'creativedelight:crow_shiny_items', ['minecraft:glowstone_dust', 'minecraft:ender_pearl', 'minecraft:ender_eye'])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// Salt lick: the worn stages mine like The Salt's block.
	var licks = [i.lightly_licked_salt_block, i.well_licked_salt_block, i.heavily_licked_salt_block, i.waxed_salt_block,
		i.waxed_lightly_licked_salt_block, i.waxed_well_licked_salt_block, i.waxed_heavily_licked_salt_block]
	t.add(e, '#minecraft:mineable/pickaxe', licks)

	// Crows
	t.add(e, 'creativedelight:repels_crows', ['minecraft:carved_pumpkin', 'minecraft:jack_o_lantern', i.scarecrow])
	t.add(e, 'alexsmobs:crow_fears', i.scarecrow)
	t.add(e, 'creativedelight:nests', i.nest)
	t.add(e, '#minecraft:mineable/hoe', [i.nest, i.scarecrow])
	t.add(e, 'creativedelight:crow_edible_crops', [
		// Upstream's list, retargeted to the pack: wheat, stems, berries, Farmer's Delight, raspberry, trellis crops, peanuts, corn
		'minecraft:wheat', 'minecraft:pumpkin_stem', 'minecraft:melon_stem', 'minecraft:sweet_berry_bush',
		'farmersdelight:cabbages', 'farmersdelight:tomatoes', 'farmersdelight:budding_tomatoes',
		'hearthandharvest:raspberry_bush', 'fruitsdelight:blueberry_bush', 'fruitsdelight:cranberry_bush',
		'delightful:salmonberry_bush', 'neapolitan:strawberry_bush',
		'supplementaries:stick_tomatoes', 'supplementaries:rope_tomatoes', 'creativedelight:treated_stick_tomatoes',
		'vintagedelight:peanut_crop', 'corn_delight:corn_crop'])
})

ServerEvents.tags('entity_type', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags

	// Allowlist of salt lickers (S4); the AM grazers are the pack's moose, bison and gazelle.
	t.add(e, 'creativedelight:salt_lickers', ['minecraft:cow', 'minecraft:sheep', 'minecraft:goat', 'minecraft:pig', 'minecraft:horse', 'minecraft:donkey',
		'minecraft:mule', 'minecraft:llama', 'minecraft:rabbit', 'alexsmobs:moose', 'alexsmobs:bison', 'alexsmobs:gazelle'])

	t.add(e, 'creativedelight:scary_for_crow', ['minecraft:player', 'minecraft:villager', 'mca:male_villager', 'mca:female_villager'])
})

// Ground nests: the grass biomes of the crow spawn list (spawns.json); the snow, rock and sky biomes on that list are left out.
ServerEvents.tags('worldgen/biome', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	global.tags.add(e, 'creativedelight:has_crows', ['minecraft:birch_forest', 'minecraft:dark_forest', 'minecraft:flower_forest', 'minecraft:forest',
		'minecraft:meadow', 'minecraft:old_growth_birch_forest', 'minecraft:old_growth_pine_taiga', 'minecraft:old_growth_spruce_taiga', 'minecraft:plains',
		'minecraft:sunflower_plains', 'minecraft:taiga', 'terralith:birch_taiga', 'terralith:blooming_valley', 'terralith:cloud_forest', 'terralith:cold_shrubland',
		'terralith:forested_highlands', 'terralith:highlands', 'terralith:hot_shrubland', 'terralith:lavender_forest', 'terralith:lavender_valley', 'terralith:lush_valley',
		'terralith:moonlight_grove', 'terralith:moonlight_valley', 'terralith:rocky_shrubland', 'terralith:sakura_grove', 'terralith:sakura_valley', 'terralith:shield',
		'terralith:shield_clearing', 'terralith:steppe', 'terralith:temperate_highlands', 'terralith:valley_clearing', 'terralith:yosemite_lowlands'])
})
