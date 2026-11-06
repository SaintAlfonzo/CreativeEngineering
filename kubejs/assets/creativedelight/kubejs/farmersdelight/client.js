// requires: farmersdelight
// Farmer's Delight client rows, moved from the pack's client_scripts/ 2026-10-27, rows unchanged.

// Bowl, basket and soil lines (moved from the pack's client_scripts/tooltips.js 2026-11-01, rows unchanged; the
// helpers and palette come through global.tooltips), then the farmer villager line: Creative Delight's Farmer's
// Delight repack, config/farmersdelight-ce-common.toml [villagers] farmlandBlocks (feature_writeup/farmers_delight.md
// 3), after the soil lines as before.
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var i = global.item
	var T = global.tooltips
	if (!T) { console.error('[creativedelight] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run') }
	else {
		var addTooltips = T.addTooltips, col = T.col
		addTooltips(e,
			[i.bowl, ['Warning: Sometimes Consumed', col.warn]],
			[i.fd_basket, ['Collects items from the block in front of opening, can be placed sideways', col.uncm]],
			[i.rich_soil, ['Crops grow slightly faster; till it into Rich Farmland', col.uncm],['Can cultivate mushroom colonies from mushrooms', col.uncm]],
			[i.rich_farmland, ['Crops grow slightly faster and it can\'t be trampled!', col.uncm],['Can cultivate mushroom colonies from mushrooms', col.uncm]],
			[i.fertile_soil, ['Crops grow significantly faster and can\'t be trampled!', col.rare],['Doesn\'t need to be tilled, can be soaked w/ water to increase plant growth', col.rare]],
			[i.nulch, ['Grows nether crops faster', col.rare],['Can be filled w/ lava, but watch your feet!', col.rare]]
		);
	}
	e.add(i.rich_farmland, Text.green('Farmer villagers work it like Farmland'))
	if (global.has(i.fertile_soil)) e.add(i.fertile_soil, Text.darkBlue('Farmer villagers work it like Farmland'))
})

ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	// Farmer's Delight's cheesecake is the pack's only sweet berry pie (Canned Sweet Berry Pie Filling); the Display
	// Delight plates are renamed in kubejs/assets/displaydelight/lang
	e.renameItem(i.sweet_berry_cheesecake, 'Sweet Berry Pie')
	e.renameItem(i.sweet_berry_cheesecake_slice, 'Slice of Sweet Berry Pie')
	if (global.has(i.incomplete_sweet_berry_cheesecake)) e.renameItem(i.incomplete_sweet_berry_cheesecake, 'Incomplete Sweet Berry Pie')
	// FD's fried rice is made with eggs; Display Delight's placed copy is renamed in kubejs/assets/displaydelight/lang
	e.renameItem(i.fried_rice, 'Egg Fried Rice')
})
