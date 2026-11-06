// requires: largemeals
// Large Meals client rows (feature_writeup/large_meals.md 4 steps 5, 18): where the Mutton Rack comes from. The sheep drop
// is a Farmer's Delight loot modifier (needs a knife in the main hand; burning gives the cooked rack), so the item says so (jei_info.js adds the JEI page).
// The Knife and Machete Drops tab lists the two modifiers by itself.
ItemEvents.tooltip(e => {
	if (!global.CD || !global.CD.on('largemeals')) return
	var i = global.item
	var T = global.tooltips
	if (!T) { console.error('[creativedelight] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	T.addTooltips(e,
		[i.raw_mutton_rack, ['Obtained by killing a Sheep with a Knife', T.col.uncm]],
		[i.cooked_mutton_rack, ['Obtained by killing a burning Sheep with a Knife', T.col.uncm]]
	)
})
