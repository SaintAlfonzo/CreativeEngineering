// requires: creativemagic
// Creative Magic: Botania tooltips. The helpers and the color palette stay in the pack's tooltips.js and are read here
// through global.tooltips at event time (each script pack has its own scope).
ItemEvents.tooltip(e => {
	var T = global.tooltips
	if (!T) { console.error('[creativemagic] global.tooltips missing: the pack\'s client_scripts/tooltips.js did not run'); return }
	var addTooltips = T.addTooltips
	var col = T.col
	var i = global.item

	addTooltips(e,
		[i.vine_ball, ['Throw to drop vines on target block', col.norm]],
		[i.bauble_box, ['Holds any item; only trinkets equip from it', col.norm]]
	);
})
