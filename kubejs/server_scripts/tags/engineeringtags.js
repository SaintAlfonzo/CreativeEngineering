console.log('engineering tags starting')

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
    var b = global.blocks

	// Bulb glass / carbon filament
	t.add(e, 'creativeengineering:bulb_glass', [
		i.glass, i.glass_bottle, i.quark_framed_glass,
		i.framed_glass, i.framed_glass_vertical, i.framed_glass_horizontal, i.framed_glass_tiled,
	])
	t.add(e, 'creativeengineering:carbon_filament', [i.paper, i.bamboo])

	t.add(e, 'creativeengineering:culture_base', [i.mycelium, i.red_mushroom_colony, i.brown_mushroom_colony, i.red_mushroom_block, i.brown_mushroom_block])

	// Precision sawblade blueprint templates — the "shape reference" ingredient for the
	// vertical_planks / railing sawblades. No upstream tag exists for either shape, so the
	// blueprint recipes (kubejs/data/creativeengineering/recipes/blueprint/precision_sawblade_*.json)
	// point at these. Keep in sync with the sawmill outputs generated in generation_scripts/sawmill.js.
	t.add(e, 'creativeengineering:vertical_planks', [
		'quark:vertical_oak_planks', 'quark:vertical_spruce_planks', 'quark:vertical_birch_planks',
		'quark:vertical_jungle_planks', 'quark:vertical_acacia_planks', 'quark:vertical_dark_oak_planks',
		'quark:vertical_mangrove_planks', 'quark:vertical_crimson_planks', 'quark:vertical_warped_planks',
		'quark:vertical_ancient_planks', 'quark:vertical_azalea_planks', 'quark:vertical_blossom_planks',
		'everycomp:q/architects_palette/vertical_twisted_planks',
	])
	t.add(e, 'creativeengineering:railings', [
		'architects_palette:oak_railing', 'architects_palette:spruce_railing', 'architects_palette:birch_railing',
		'architects_palette:jungle_railing', 'architects_palette:acacia_railing', 'architects_palette:dark_oak_railing',
		'architects_palette:mangrove_railing', 'architects_palette:crimson_railing', 'architects_palette:warped_railing',
		'architects_palette:twisted_railing',
		'everycomp:ap/quark/ancient_railing', 'everycomp:ap/quark/azalea_railing', 'everycomp:ap/quark/blossom_railing',
	])
})

console.log('engineering tags finished')
