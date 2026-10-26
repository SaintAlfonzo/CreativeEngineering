// requires: veggiesdelight
// Veggies Delight compost values: Creative Delight QoL-4 (creative_delight.md 8). Moved 2026-10-25 from the pack's
// startup_scripts/compost/compost_values.js, same rows and chances (12 items); the engine, tiers and rules stay in the pack
// (global.compost, patcherMod's func/compost.js). Startup script in the CD jar: an edit needs a CD rebuild and restart.
// The rows count only while [qol] compost is on (checked at every table rebuild, after the configs load); off, these
// foods fall to the pack's rules.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => !!global.CD && global.CD.qol('compost') && global.CD.on('veggiesdelight') }

	C.values('crops', [
		[i.wild_broccoli,          0.65],
		[i.wild_cauliflowers,      0.65],
		[i.wild_garlic,            0.65],
		[i.wild_sweet_potatoes,    0.65],
		[i.wild_turnips,           0.65],
		[i.wild_zucchinis,         0.65],
	], ON)
	C.values('crates', [
		[i.broccoli_crate,                      1.0],
		[i.cauliflower_crate,                   1.0],
		[i.garlic_crate,                        1.0],
		[i.sweet_potato_crate,                  1.0],
		[i.turnip_crate,                        1.0],
		[i.zucchini_crate,                      1.0],
	], ON)
})()
