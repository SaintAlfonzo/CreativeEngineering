// Creative Engineering; moved from kubejs/server_scripts/engineering/dreams_desires.js on 2026-11-04 (creative_split.md 7.13, wave 8), rows unchanged.
ServerEvents.recipes(e => {
	console.log('Dreams and Desires starting')

	const R = global.NCH
	var i = global.item

	// ── Painted Asphalt (TODO engineering/dreams-and-desires #6) ──
	// D&D's coloured asphalt is painted Immersive Petroleum asphalt: 8 asphalt + 1 dye -> 8. Painted asphalt takes
	// the dye as well (repaint), and 8 painted + 1 soap washes it back to plain asphalt. Right-clicking painted
	// asphalt with soap still gives white painted asphalt (Supplementaries' colour set). Run speed is the
	// BlockRunner asphalt tier (tags/blocktags.js).
	var colors = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
		'purple', 'blue', 'brown', 'green', 'red', 'black']

	R.bulk('remove', colors.map(c => [{ id: 'create_dd:crafting/decor/' + c + '_asphalt_block' }, e]))
	colors.forEach(c => R.shaped(Item.of(i[c + '_asphalt_block'], 8), ['AAA', 'ADA', 'AAA'],
		{ A: [i.asphalt, i.t_painted_asphalt], D: i['any_dye_' + c] }, e, c + '_painted_asphalt'))
	R.shaped(Item.of(i.asphalt, 8), ['AAA', 'ASA', 'AAA'], { A: i.t_painted_asphalt, S: i.soap }, e,
		'asphalt_from_painted_asphalt_soap')
})
