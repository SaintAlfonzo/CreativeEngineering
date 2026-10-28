ServerEvents.recipes(e => {
	console.log('Sawdust starting')

	const R = global.NCH
	var i = global.item;
	var f = global.fluid;

	//Removes
	R.bulk('remove', [
		[{ id: 'farmersdelight:organic_compost_from_rotten_flesh' }, e],
		[{ id: 'farmersdelight:organic_compost_from_tree_bark' }, e],
		[{ id: 'immersive_weathering:mulch_block' }, e],
		[{ id: 'create:crushing/wood/scrap_wood_small' }, e],
		[{ id: 'create:crushing/wood/scrap_wood_medium' }, e],
		[{ id: 'create:crushing/wood/scrap_wood_large' }, e],
		[{ id: 'create:crushing/wood/scrap_wood_logs' }, e],
		[{ id: 'create:crushing/wood/scrap_wood_planks' }, e]
	]);

	//Wood to sawdust
	R.createCrushing([Item.of(i.sawdust, 2), Item.of(i.sawdust, 1).withChance(0.50)], i.any_log, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 3), i.any_log, e)

	R.createCrushing([Item.of(i.sawdust, 2), Item.of(i.sawdust, 1).withChance(0.50)], i.scrap_wood_large, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 3), i.scrap_wood_large, e)

	R.createCrushing([Item.of(i.sawdust, 1), Item.of(i.sawdust, 1).withChance(0.50)], i.planks, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 2), i.planks, e)

	R.createCrushing([Item.of(i.sawdust, 1), Item.of(i.sawdust, 1).withChance(0.25)], i.any_wooden_stairs, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 2), i.any_wooden_stairs, e)

	R.createCrushing([Item.of(i.sawdust, 1), Item.of(i.sawdust, 1).withChance(0.25)], i.scrap_wood_medium, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 2), i.scrap_wood_medium, e)

	R.createCrushing([Item.of(i.sawdust, 1), Item.of(i.sawdust, 1).withChance(0.25)], i.any_wooden_fences, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 2), i.any_wooden_fences, e)

	R.createCrushing(Item.of(i.sawdust, 1).withChance(0.75), i.wood_slab, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 1), i.wood_slab, e)

	R.createCrushing([Item.of(i.sawdust, 1), Item.of(i.sawdust, 1).withChance(0.25)], i.scrap_wood_small, e, undefined, true)
	R.ieCrushing(Item.of(i.sawdust, 2), i.scrap_wood_small, e)

	// Ash conversion
	R.smelting(i.ash, i.sawdust, e)
	R.smelting(i.ash, i.creosote_sawdust, e)

	//Mud to dirt
	R.shapeless(Item.of(i.dirt, 4), [Item.of(i.mud, 3), i.sawdust], e)

	//Creosote Soaking
	R.createFilling(i.creosote_sawdust, [Fluid.of(f.creosote, 250), i.sawdust], e)
	R.ieBottling(i.creosote_sawdust, f.t_creosote, i.sawdust, e, undefined, 250)

	//Wood pellet crafting
	R.shapeless(Item.of(i.wood_pellets, 4), [Item.of(i.sawdust, 4), Item.of(i.creosote_sawdust, 4)], e, 'wood_pellets_manual_only')
	R.createMixing(Item.of(i.wood_pellets, 6), [Item.of(i.sawdust, 4), Item.of(i.creosote_sawdust, 4)], e, 'wood_pellets_mixing', 'n/a')

	//Compost & fertile soil crafts
	R.shaped(Item.of(i.compost, 1), ['DWO', 'WOR', 'ORR'], { D: i.dirt, R: i.mulch_brown, O: i.mulch_bone, W: i.mulch_waste }, e, 'compost_manual_only')
	R.shaped(Item.of(i.compost, 1), ['DWO', 'WOB', 'OBB'], { D: i.dirt, B: i.any_bark, O: i.mulch_bone, W: i.mulch_waste }, e, 'compost_bark_manual_only')

	R.createMixing(Item.of(i.compost, 1), [Item.of(i.dirt, 1), i.mulch_brown, i.mulch_brown, i.mulch_bone, i.mulch_bone, i.mulch_waste, i.mulch_waste], e, 'compost_mixing', 'n/a')
	R.createMixing(Item.of(i.compost, 1), [Item.of(i.dirt, 1), i.any_bark, i.any_bark, i.mulch_bone, i.mulch_bone, i.mulch_waste, i.mulch_waste], e, 'compost_mixing_bark', 'n/a')

	R.shaped(Item.of(i.fertile_soil, 6), ['SSS', 'ROW', 'GFZ'], { S: i.rich_soil, R: i.mulch_brown, O: i.mulch_bone, W: i.mulch_waste, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_var_manual_only')
	R.shaped(Item.of(i.fertile_soil, 6), ['SSS', 'ROW', 'GFZ'], { S: i.rich_soil, R: i.any_bark, O: i.mulch_bone, W: i.mulch_waste, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_var_bark_manual_only')

	R.createMixing(Item.of(i.fertile_soil, 6), [Item.of(i.rich_soil, 2), i.mulch_brown, i.mulch_bone, i.mulch_waste, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_mixing_var', 'n/a')
	R.createMixing(Item.of(i.fertile_soil, 6), [Item.of(i.rich_soil, 2), i.any_bark, i.mulch_bone, i.mulch_waste, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_bark_mixing_var', 'n/a')

	R.shaped(Item.of(i.fertile_soil, 3), ['SSS', 'RRR', 'GFZ'], { S: i.rich_soil, R: i.mulch_brown, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_brown_manual_only')
	R.shaped(Item.of(i.fertile_soil, 3), ['SSS', 'RRR', 'GFZ'], { S: i.rich_soil, R: i.any_bark, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_bark_manual_only')
	R.shaped(Item.of(i.fertile_soil, 3), ['SSS', 'OOO', 'GFZ'], { S: i.rich_soil, O: i.mulch_bone, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_bone_manual_only')
	R.shaped(Item.of(i.fertile_soil, 3), ['SSS', 'WWW', 'GFZ'], { S: i.rich_soil, W: i.mulch_waste, G: i.mulch_green, F: i.mulch_fungus, Z: i.mulch_fertilizer }, e, 'fertile_soil_waste_manual_only')

	R.createMixing(Item.of(i.fertile_soil, 3), [Item.of(i.rich_soil, 2), i.mulch_brown, i.mulch_brown, i.mulch_brown, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_mixing_brown', 'n/a')
	R.createMixing(Item.of(i.fertile_soil, 3), [Item.of(i.rich_soil, 2), i.any_bark, i.any_bark, i.any_bark, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_mixing_bark', 'n/a')
	R.createMixing(Item.of(i.fertile_soil, 3), [Item.of(i.rich_soil, 2), i.mulch_bone, i.mulch_bone, i.mulch_bone, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_mixing_bone', 'n/a')
	R.createMixing(Item.of(i.fertile_soil, 3), [Item.of(i.rich_soil, 2), i.mulch_waste, i.mulch_waste, i.mulch_waste, i.mulch_green, i.mulch_fungus, i.mulch_fertilizer], e, 'fertile_soil_mixing_waste', 'n/a')

	//*****************************************************************************************Integrate nulch, move to compost file
	R.remove({ id: 'immersive_weathering:nulch_block' }, e)
	R.shapeless(Item.of(i.nulch, 6), [Item.of(i.netherrack, 3), i.any_scale, i.any_scale, Item.of(i.ash, 1), Item.of(i.magma_cream, 1), Item.of(i.nether_wart, 1), Item.of(i.blaze_powder, 1)], e, 'nulch_manual_only')
	R.createMixing(Item.of(i.nulch, 6), [Item.of(i.netherrack, 2), i.any_scale, Item.of(i.ash, 1), Item.of(i.magma_cream, 1), Item.of(i.nether_wart, 1), Item.of(i.blaze_powder, 1)], e, 'nulch_mixing', 'n/a')

	console.log('Sawdust done')
})
