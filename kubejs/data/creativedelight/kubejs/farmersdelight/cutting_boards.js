// requires: farmersdelight
// Extra cutting boards, registered by the repackedFarmers member (FDBackportRegistry). They work like FD's board (same
// block class and block entity). Recipe: FD's board shape (2 sticks, 4 of the material).
// - Stone (TODO cooking/kitchen #25, 2026-11-02; kitchen.md 2.8): granite, andesite, diorite; the polished stone for the
//   planks. Pickaxe.
// - Wood (TODO cooking/kitchen #38, 2026-11-04; kitchen.md 2.11): the 8 vanilla woods other than spruce; that wood's
//   planks. Axe. FD's own board is the spruce one: its recipe takes #creativedelight:cutting_board_planks, every plank
//   except the 8 woods with their own board (modded and vertical planks included), instead of #minecraft:planks.
// Both sets get the wooden board's counter-topper and hammer tags.
ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var t = global.tags
	var b = global.blocks

	t.mineable(e, 'pickaxe', null, b.stone_cutting_boards)
	t.mineable(e, 'axe', null, b.wood_cutting_boards)
	t.add(e, b.t_hammer_no_rotate, b.stone_cutting_boards.concat(b.wood_cutting_boards))
	if (global.CD.on('blockheads_additions')) {
		t.add(e, b.t_counter_toppers, b.stone_cutting_boards.concat(b.wood_cutting_boards))
	}
})

ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var i = global.item
	global.tags.addMatching(e, i.t_cutting_board_planks, i.planks, { except: woodBoards().map(p => p[1]) })
})

ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		;[[i.granite_cutting_board, i.polished_granite], [i.andesite_cutting_board, i.polished_andesite],
			[i.diorite_cutting_board, i.polished_diorite]].concat(woodBoards()).forEach(p => {
			R.shaped(p[0], ['/##', '/##'], { '/': i.stick, '#': p[1] }, e, String(p[0]).split(':')[1])
		})
		R.remove({ id: 'farmersdelight:cutting_board' }, e)
		R.shaped(i.cutting_board, ['/##', '/##'], { '/': i.stick, '#': i.t_cutting_board_planks }, e, 'cutting_board')
	})
})

// [board, planks] for each wood board
function woodBoards() {
	var i = global.item
	return ['oak', 'birch', 'jungle', 'acacia', 'dark_oak', 'mangrove', 'crimson', 'warped']
		.map(w => [i[w + '_cutting_board'], i['planks_' + w]])
}
