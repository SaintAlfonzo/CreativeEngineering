// requires: fungidelight
// Fungi Delight compost values: Creative Delight QoL-4 (creative_delight.md 8). Moved 2026-10-25 from the pack's
// startup_scripts/compost/compost_values.js, same rows and chances (19 items); the engine is patcherMod's
// (global.compost, func/compost.js), tiers and rules are creativeengineering/compost_rules.js here. Startup script in the CD jar: an edit needs a CD rebuild and restart.
// The rows count only while [qol] compost is on (checked at every table rebuild, after the configs load); off, these
// foods fall to the rules.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => !!global.CD && global.CD.qol('compost') && global.CD.on('fungidelight') }

	C.values('mushrooms', [   // upstream values, fungi_delight.md step 13
		[i.inky_cap_mushroom, 0.65],
		[i.morel_mushroom,    0.65],
		[i.truffle,           1.0],
	], ON)
	C.values('plant_matter', [
		[i.inky_cap_mushroom_block,          0.85],
		[i.inky_cap_mushroom_edge,           0.85],
		[i.inky_cap_mushroom_stem,           0.65],
		[i.morel_mushroom_block,             0.85],
		[i.morel_mushroom_stem,              0.65],
		[i.inky_goo_veil,                    0.65],
	], ON)
	C.values('plants', [
		[i.inky_cap_mushroom_colony, 1.0],
		[i.morel_mushroom_colony,    1.0],
	], ON)
	C.values('crates', [
		[i.brown_mushroom_crate,                1.0],
		[i.red_mushroom_crate,                  1.0],
		[i.inky_cap_mushroom_crate,             1.0],
		[i.morel_mushroom_crate,                1.0],
		[i.truffle_crate,                       1.0],
	], ON)
	C.values('other', [   // prepared ingredients
		[i.cleaned_cap,                        0.5],
		[i.cooked_cleaned_cap,                 0.65],
		[i.truffle_slice,                      0.5],
	], ON)
})()
