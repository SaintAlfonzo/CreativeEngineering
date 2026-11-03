// requires: farmersdelight
// Stone cutting boards (TODO cooking/kitchen #25, 2026-11-02; kitchen.md 2.8): Farmer's Delight's cutting board in
// granite, andesite and diorite, registered by the repackedFarmers member (FDBackportRegistry). They work like the
// wooden board (same block entity). Recipe: the wooden board's shape with the polished stone for the planks. Block
// tags: pickaxe, and the wooden board's counter-topper and hammer tags.
ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var t = global.tags
	var b = global.blocks

	t.mineable(e, 'pickaxe', null, b.stone_cutting_boards)
	t.add(e, b.t_hammer_no_rotate, b.stone_cutting_boards)
	if (global.CD.on('blockheads_additions')) t.add(e, b.t_counter_toppers, b.stone_cutting_boards)
})

ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		;[[i.granite_cutting_board, i.polished_granite], [i.andesite_cutting_board, i.polished_andesite],
			[i.diorite_cutting_board, i.polished_diorite]].forEach(p => {
			R.shaped(p[0], ['/##', '/##'], { '/': i.stick, '#': p[1] }, e, String(p[0]).split(':')[1])
		})
	})
})
