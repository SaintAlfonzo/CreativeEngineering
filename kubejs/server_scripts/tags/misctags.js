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

	// Flower box, planter box, chalk dye and cbmicroblock tags: Creative Compat (creativecompat/kubejs/tags/misctags.js).

	// Seeds missing from forge:seeds (so also from Quark's pouch tag, which nests it)
	t.add(e, 'forge:seeds', [i.tobacco_seeds, i.hamimelon_seeds, i.lemon_seeds])

	t.add(e, '#forge:animal_fat', [i.animal_fat, i.tallow, i.animal_oil_bottle])
})

console.log('misc tags finished')