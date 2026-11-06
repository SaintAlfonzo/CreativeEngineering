// requires: barbequesdelight
// Barbeque's Delight hand-written compost rows: 1.3.0's neoforge:compostables data map, same chances
// (barbeques_delight.md 3.7). Hand rows win over the category rules (which skip meat and meals) and
// tools/gen_compost_values.py skips this file. Seasonings and the three blocks are not compostable.
// Startup script in the CD jar: an edit needs a CD rebuild and restart.
;(function () {
	var i = global.item
	var C = global.compost

	C.values('other', [
		// 10 raw skewers
		[i.raw_beef_skewer, 0.25], [i.raw_chicken_skewer, 0.25], [i.raw_cod_skewer, 0.25], [i.raw_lamb_skewer, 0.25],
		[i.raw_mushroom_skewer, 0.25], [i.raw_pork_sausage_skewer, 0.25], [i.raw_potato_skewer, 0.25],
		[i.raw_rabbit_skewer, 0.25], [i.raw_salmon_skewer, 0.25], [i.raw_vegetable_skewer, 0.25],
		// 10 cooked skewers
		[i.grilled_beef_skewer, 0.5], [i.grilled_chicken_skewer, 0.5], [i.grilled_cod_skewer, 0.5], [i.grilled_lamb_skewer, 0.5],
		[i.grilled_mushroom_skewer, 0.5], [i.grilled_pork_sausage_skewer, 0.5], [i.grilled_potato_skewer, 0.5],
		[i.grilled_rabbit_skewer, 0.5], [i.grilled_salmon_skewer, 0.5], [i.grilled_vegetable_skewer, 0.5],
		[i.burnt_skewer, 0.25],
		[i.kebab_wrap, 0.5],
		[i.kebab_sandwich, 0.75],
		[i.bibimbap, 1.0],
	])
})()
