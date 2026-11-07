// requires: creativebrewing
// requires: vinery
// Vinery item tooltips (port lines and the flower box). Moved from the pack's client_scripts/tooltips.js 2026-11-01
// (creative_brewing.md 4.5), rows unchanged; the helpers and palette are read through global.tooltips at event time.
ItemEvents.tooltip(e => {
	if (!global.CB || !global.CB.on('vinery')) return
	var T = global.tooltips
	if (!T) { console.error('[creativebrewing] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	var addTooltips = T.addTooltips, removeTooltipKey = T.removeTooltipKey
	var col = T.col
	var i = global.item

	// Vinery port (alcohol/vineryport #16). Wine sizes, rack and Wine Box capacity and the aging hint come from the port
	// itself (DrinkBlockItem.BottleSize), since the sizes live in its code; these are the lines it doesn't write.
	addTooltips(e,
		// Processing
		[i.grapevine_pot, ['Fill with grapes of one type, then jump on it to press juice (counts in JEI)', col.norm]],
		[i.wine_press, ['Mashes apples, then turns the mash and a wine bottle into apple juice', col.norm]],
		[i.fermentation_barrel, ['Pour in juice (one type at a time) and add the ingredients and a wine bottle', col.norm],
			['Sneak right-click with an empty hand to pour the juice out', col.norm]],
		// Growing
		[i.grapevine_stem, ['Place two high and plant grape seeds on the top one; jungle grapes need a lattice', col.norm],
			['Shears give the seed back; breaking a planted stem loses it', col.norm]],
		[i.grapevine_lattice, ['Plant jungle grape seeds on it', col.norm],
			['Shears give the seed back; breaking a planted lattice loses it', col.norm]],
		[i.apple_leaves, ['Some leaves grow apples; pick ripe ones with an empty hand', col.norm]],
		[i.cherry_leaves, ['Some leaves grow cherries; pick ripe ones with an empty hand', col.norm]],
		[i.apple_tree_sapling, ['Grows an apple tree', col.norm]],
		[i.cherry_sapling, ['Grows a cherry tree', col.norm]],
		// Utility
		[i.stackable_log, ['A campfire: cooks up to 4 foods and burns 10 minutes; rain can put it out', col.norm],
			['Add logs to refuel; a shovel or a water bottle puts it out', col.norm]],
		// Wines
		[i.chorus_wine, ['Makes you wake up in strange places', col.lpurple]]
	);

	// Vinery flower box rows (feature_writeup/flower_pots.md 3.4)
	addTooltips(e,
		[i.vinery_flower_box, ['Can be placed on walls', col.norm]]
	);
	// Vinery's own "Can be placed" line on the box (the new line says it)
	removeTooltipKey(e, 'block.vinery.canbeplaced.tooltip', i.vinery_flower_box);
})
