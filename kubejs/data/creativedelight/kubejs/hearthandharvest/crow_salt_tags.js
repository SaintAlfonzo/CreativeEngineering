// requires: creativedelight
// requires: hearthandharvest
// Tags of the salt lick, the scarecrow, the crow (hearthandharvest:crow) and its nests (plan_hearth_crow_salt.md,
// plan_hearth_crow_swap.md; hearth_and_harvest.md 5.8 N24-7 and N24-8). Read by the member's hearth/crow, hearth/salt and hearth/nest packages.

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// Crow food: seeds plus the pack's corn and corn seeds (N24-7). Taming, tempting, dropped food.
	t.add(e, 'creativedelight:crow_food', [i.t_seeds, i.t_crops_corn, i.t_seeds_corn])

	// N24-8 list. Bricks sit in #forge:ingots in this pack and are not shiny; netherite and the end-tier metals are cut.
	// G5 begin: the except list of the tags helper
	var cut = [i.ingot_netherite, i.raw_netherite, i.nugget_netherite, i.terrasteel_ingot, i.terrasteel_nugget,
		i.refined_radiance, i.shadow_steel, i.xp_nugget,
		'*:*brick*']
	// G5 end
	t.addMatching(e, 'creativedelight:crow_shiny_items', ['#forge:gems', i.t_ingots, i.t_nuggets, i.t_raw_materials], { except: cut })
	t.add(e, 'creativedelight:crow_shiny_items', [i.glowstone, i.ender_pearl, i.ender_eye])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// Salt lick: the worn stages mine like The Salt's block.
	var licks = [i.lightly_licked_salt_block, i.well_licked_salt_block, i.heavily_licked_salt_block, i.waxed_salt_block,
		i.waxed_lightly_licked_salt_block, i.waxed_well_licked_salt_block, i.waxed_heavily_licked_salt_block]
	t.add(e, global.blocks.t_mineable_pickaxe, licks)

	// Crows
	t.add(e, 'creativedelight:repels_crows', [i.carved_pumpkin, i.jack_o_lantern, i.scarecrow])
	t.add(e, 'creativedelight:nests', i.nest)
	t.add(e, '#minecraft:mineable/hoe', [i.nest, i.scarecrow])
	t.add(e, 'creativedelight:crow_edible_crops', [
		// Upstream's list, retargeted to the pack: wheat, stems, berries, Farmer's Delight, raspberry, trellis crops, peanuts, corn
		i.wheat, 'minecraft:pumpkin_stem', 'minecraft:melon_stem', global.blocks.sweet_berry_bush,
		global.blocks.cabbages, global.blocks.tomatoes, 'farmersdelight:budding_tomatoes',
		'hearthandharvest:raspberry_bush', i.blueberry_bush, global.blocks.cranberry_bush,
		'delightful:salmonberry_bush', global.blocks.strawberry_bush,
		global.blocks.stick_tomatoes, global.blocks.rope_tomatoes, global.blocks.treated_stick_tomatoes,
		global.blocks.peanut_crop, global.blocks.corn_crop])
})

ServerEvents.tags('entity_type', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags

	// Allowlist of salt lickers (S4); the AM grazers are the pack's moose, bison and gazelle.
	t.add(e, 'creativedelight:salt_lickers', ['minecraft:cow', 'minecraft:sheep', 'minecraft:goat', 'minecraft:pig', 'minecraft:horse', 'minecraft:donkey',
		'minecraft:mule', 'minecraft:llama', 'minecraft:rabbit', 'alexsmobs:moose', 'alexsmobs:bison', 'alexsmobs:gazelle'])

	t.add(e, 'creativedelight:scary_for_crow', ['minecraft:player', 'minecraft:villager', 'mca:male_villager', 'mca:female_villager'])
})

// Tree-top nests (plan_hearth_crow_swap.md 5.1): the temperate forests (in no non-temperate list of season_types.json).
// Crows have no biome spawn entries; they live around these nests and village roof nests.
ServerEvents.tags('worldgen/biome', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	global.tags.add(e, 'creativedelight:crow_nest_forests', ['minecraft:forest', 'minecraft:birch_forest', 'minecraft:old_growth_birch_forest',
		'minecraft:dark_forest', 'minecraft:flower_forest', 'terralith:lavender_forest', 'terralith:moonlight_grove', 'terralith:sakura_grove'])
})
