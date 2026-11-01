console.log('misc tags starting')

var t = global.tags
var i = global.item
var b = global.blocks

// ── Glass tag rollup ────────────────────────────────────────────────────────
// Some mods (createframed, design_decor, …) register their glass ONLY under a
// colour/variant subtag — forge:glass/white, forge:glass/tinted — and never into
// the parent forge:glass, so #forge:glass reads short. Nest every subtag back
// into its parent (both glass and glass_panes, item + block) so the parent stays
// complete; nested refs self-heal when a mod adds more to a subtag later.
// An unused suffix (e.g. glass_panes/silica) just resolves to nothing — harmless.
var GLASS_SUB = [
	'white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
	'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black',
	'colorless', 'silica', 'tinted',
]
function rollupGlass(e) {
	GLASS_SUB.forEach(function (s) {
		t.add(e, 'forge:glass',       '#forge:glass/' + s)
		t.add(e, 'forge:glass_panes', '#forge:glass_panes/' + s)
	})
}

ServerEvents.tags('block', e => {
	rollupGlass(e)
})

ServerEvents.tags('item', e => {
	rollupGlass(e)

	// Limestone
	t.add(e, 'create:stone_types/limestone', [
		i.limestone_castel_brick_stairs, i.limestone_castel_brick_wall, i.limestone_castel_bricks,
		i.limestone_castel_tile_stairs, i.limestone_castel_tile_wall, i.limestone_castel_tiles,
		i.limestone_brick_stairs_small, i.limestone_brick_wall_small, i.limestone_bricks_small,
		i.limestone_polished_cut, i.limestone_polished_cut_stairs, i.limestone_polished_cut_wall,
		i.limestone, i.limestone_pillar, i.limestone_layered, i.limestone_cut,
		i.limestone_cut_brick_stairs, i.limestone_cut_brick_wall, i.limestone_cut_bricks,
		i.limestone_cut_stairs, i.limestone_cut_wall,
	])

	
	// Flower boxes (feature_writeup/flower_pots.md 3.2). Azalea bushes go in no box; Another Furniture's planter box
	// also takes these plants, drawn with Supplementaries' flower box models (patcherMod PlantBoxModels).
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

	// Seeds missing from forge:seeds (so also from Quark's pouch tag, which nests it)
	t.add(e, 'forge:seeds', [i.tobacco_seeds, i.hamimelon_seeds, i.lemon_seeds])

	t.add(e, '#forge:animal_fat', [i.animal_fat, i.tallow, i.animal_oil_bottle])

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

console.log('misc tags finished')