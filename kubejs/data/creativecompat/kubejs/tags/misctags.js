// Creative Compat: item tags for the flower box, planter box, chalk and microblock tweaks; moved from
// kubejs/server_scripts/tags/misctags.js on 2026-11-01.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	// Flower boxes (feature_writeup/flower_pots.md 3.2). Azalea bushes go in no box; Another Furniture's planter box
	// also takes these plants, drawn with Supplementaries' flower box models (PlantBoxModels).
	t.add(e, 'creativeengineering:not_in_flower_boxes', [i.azalea, i.flowering_azalea])
	t.add(e, 'another_furniture:planter_box_banned', '#creativeengineering:not_in_flower_boxes')
	t.add(e, 'another_furniture:planter_box_placeables', [
		i.beetroot_seeds, i.cactus, i.minecraft_carrot, i.chorus_flower, i.cocoa_beans, i.crimson_roots, i.warped_roots,
		i.dead_bush, i.grass, i.fern, i.lilac, i.melon_seeds, i.peony, i.minecraft_potato, i.pumpkin_seeds, i.rose_bush, i.sunflower,
		i.nether_sprouts, i.nether_wart, i.glow_berries, i.glow_lichen,
	])
	// The Vinery flower box takes the planter box list (backportedVinery LegacyFlowerBoxBlock)
	t.addMatching(e, 'creativeengineering:flower_box_plants', '#another_furniture:planter_box_placeables',
		{ except: '#creativeengineering:not_in_flower_boxes' })

	// Remove dye tags from chalk (no accidental crafts)
	;['red','orange','yellow','green','lime','blue','light_blue','cyan','purple',
	  'magenta','pink','brown','white','light_gray','gray','black'].forEach(c =>
		t.removeTags(e, `forge:dyes/${c}`, i['chalk_' + c], { parents: true }))

	// Only white chalk used for blackboard
	t.remove(e, 'supplementaries:blackboard_white', [i.dye_white, i.quartz])

	// Remove cbmicroblock tags
	t.clearAll(e, [
		i.microblock_stone_rod, i.microblock_stone_saw,
		i.microblock_iron_saw, i.microblock_diamond_saw,
	])
})
