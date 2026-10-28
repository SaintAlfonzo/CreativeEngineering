// requires: farmersdelight
// Farmer's Delight client rows, moved from the pack's client_scripts/ 2026-10-27, rows unchanged.

// Farmer villagers work these soils: Creative Delight's Farmer's Delight repack, config/farmersdelight-ce-common.toml
// [villagers] farmlandBlocks (feature_writeup/farmers_delight.md 3). Added after the pack's soil lines (tooltips.js).
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('farmersdelight')) return
	var i = global.item
	e.add(i.rich_farmland, Text.green('Farmer villagers work it like Farmland'))
	e.add(i.fertile_soil, Text.darkBlue('Farmer villagers work it like Farmland'))
})

ClientEvents.lang('en_us', e => {
	if (!global.CD) return
	var i = global.item
	// Farmer's Delight's cheesecake is the pack's only sweet berry pie (Canned Sweet Berry Pie Filling); the Display
	// Delight plates are renamed in kubejs/assets/displaydelight/lang
	e.renameItem(i.sweet_berry_cheesecake, 'Sweet Berry Pie')
	e.renameItem(i.sweet_berry_cheesecake_slice, 'Slice of Sweet Berry Pie')
	e.renameItem(i.incomplete_sweet_berry_cheesecake, 'Incomplete Sweet Berry Pie')
	// FD's fried rice is made with eggs; Display Delight's placed copy is renamed in kubejs/assets/displaydelight/lang
	e.renameItem(i.fried_rice, 'Egg Fried Rice')
})
