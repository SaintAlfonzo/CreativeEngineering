// requires: creativedelight
// requires: hearthandharvest
// Tags of the Hearth and Harvest content ported into the Creative Delight member hearthandharvest
// (hearth_and_harvest.md 4.1, 4.2 step 3). 1.4.1's c: tags as forge: tags; the pack's plural #forge:fruits/raspberries is
// the fruit tag (it already existed, empty). Seasons: kubejs/generation_scripts/worldgen/crop_seasons.json, not Serene
// Seasons tags.
var HH_MUMS = ['white', 'red', 'orange', 'yellow', 'light_blue', 'blue', 'purple', 'pink']
var HH_WOODS = ['oak', 'spruce', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item

	// addTag: also the parent tag (#forge:fruits, #forge:crops, #forge:seeds)
	t.addTag(e, i.t_fruits_raspberries, i.raspberry)
	t.addTag(e, i.t_crops_raspberry, i.raspberry)
	t.addTag(e, i.t_seeds_raspberry, i.raspberry_pips)
	t.add(e, [i.t_berries, i.t_fruits_berries, i.t_fox_food], i.raspberry)

	// Watering can fertiliser, loaded from the off hand (4.0). 1.4.1 also lists its Manure (not ported yet).
	t.add(e, i.t_bonemeal_substitutes, [i.bonemeal, i.manure])
	var cleavers = [i.flint_cleaver, i.iron_cleaver, i.golden_cleaver, i.diamond_cleaver, i.netherite_cleaver]
	t.add(e, i.t_cleavers, cleavers)
	t.add(e, i.any_knife, cleavers)

	// Dairy. Goat milk counts as milk except in cow-specific dairy, which asks for #creativedelight:cow_milk (N24-2).
	t.add(e, i.t_butter, i.butter)
	t.add(e, i.any_cheese, i.goat_cheese_slice)
	t.add(e, i.milk, [i.goat_milk_bucket, i.goat_milk_bottle])
	t.add(e, i.t_milk_bottle, i.goat_milk_bottle)
	t.add(e, i.t_cow_milk, [i.minecraft_milk_bucket, i.farmersdelight_milk_bottle, i.neapolitan_milk_bottle])

	t.addTag(e, i.t_crops_hops, i.hops)
	// Crow feather: arrows, brush and book and quill come through the tag (crow plan C8).
	t.add(e, '#forge:feathers', i.crow_feather)
	t.add(e, i.t_flowers, HH_MUMS.map(c => i[c + '_mum']))
	// Farmer's Hat XP and the scarecrow's hat slot (N21, N23-18): the four pack hats and H&H's.
	t.add(e, i.t_farmers_hats, [i.farmers_hat, i.farmers_hat_helmet, i.straw_hat, i.farmerbrim, i.summerhat])
	t.addTag(e, i.t_seeds_blueberry, i.blueberry_pips)
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	var i = global.item
	var b = global.blocks

	t.add(e, '#minecraft:small_flowers', HH_MUMS.map(c => i[c + '_mum']))
	t.add(e, '#minecraft:mineable/axe', HH_WOODS.map(w => i[w + '_half_cabinet']).concat([
		i.crimson_fungus_crate, i.warped_fungus_crate, i.glistering_melon_crate, i.poisonous_potato_crate, i.rotten_tomato_crate]))
	t.add(e, '#minecraft:mineable/shovel', [i.manure_block])
	t.add(e, '#minecraft:mineable/pickaxe', [i.manure_bricks, i.manure_brick_slab, i.manure_brick_stairs, i.manure_brick_wall,
		i.polished_manure, i.polished_manure_slab, i.polished_manure_stairs, i.polished_manure_wall])
	t.add(e, '#minecraft:slabs', [i.manure_brick_slab, i.polished_manure_slab])
	t.add(e, '#minecraft:stairs', [i.manure_brick_stairs, i.polished_manure_stairs])
	t.add(e, '#minecraft:walls', [i.manure_brick_wall, i.polished_manure_wall])
	t.add(e, '#minecraft:wooden_slabs', [i.crimson_fungus_crate, i.warped_fungus_crate])
	t.add(e, '#minecraft:mineable/axe', [i.tree_tapper])
	t.add(e, '#minecraft:mineable/pickaxe', [i.tilled_end_stone])
	t.add(e, '#minecraft:mineable/axe', [i.hops_crop])

	// Sap logs: 1.4.1's spruce and dark oak, plus birch (real-world syrup, 3.1).
	t.add(e, i.t_tappable, [i.log_spruce, i.log_dark_oak, i.log_birch])
	// Pitchfork: instant on hay, straw and rugs; straw from grass and ferns; straw on top of mature crops (N23-17).
	t.add(e, i.t_mineable_with_pitchfork, ['#minecraft:crops', '#minecraft:leaves', i.hay_bale, i.straw_bale, i.hay_rug, i.straw_rug, i.manure_block])
	t.add(e, i.t_pitchfork_grass, [i.grass, i.tall_grass, i.fern, i.large_fern])
	t.add(e, i.t_pitchfork_crops, [b.wheat_crop, b.rice_panicles, b.oat_crop, b.hemp])
})

ServerEvents.tags('entity_type', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	var t = global.tags
	// Cleaver butchering (1.4.1's list); manure not from chickens or crows; Pinned exemptions the class checks miss (N25).
	t.add(e, 'hearthandharvest:can_be_butchered', ['minecraft:chicken', 'minecraft:cow', 'minecraft:pig', 'minecraft:sheep', 'minecraft:rabbit'])
	t.add(e, 'hearthandharvest:does_not_poop', ['minecraft:chicken', 'alexsmobs:crow'])
	t.add(e, 'hearthandharvest:pinned_immune', ['minecraft:bat', 'minecraft:vex', 'minecraft:blaze', 'minecraft:allay'])
})

ServerEvents.tags('fluid', e => {
	if (!global.CD || !global.CD.on('hearthandharvest')) return
	global.tags.addFluid(e, 'forge:milk', global.fluid.goat_milk)
})
