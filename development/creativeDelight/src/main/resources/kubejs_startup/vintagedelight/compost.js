// requires: vintagedelight
// Vintage Delight compost values: Creative Delight QoL-4 (creative_delight.md 8). Moved 2026-10-25 from the pack's
// startup_scripts/compost/compost_values.js, same rows and chances (22 items); the engine is patcherMod's
// (global.compost, func/compost.js), tiers and rules are creativeengineering/compost_rules.js here. Startup script in the CD jar: an edit needs a CD rebuild and restart.
// The rows count only while [qol] compost is on (checked at every table rebuild, after the configs load); off, these
// foods fall to the rules.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => !!global.CD && global.CD.qol('compost') && global.CD.on('vintagedelight') }

	C.values('crops', [
		[i.wild_cucumbers,         0.65],
		[i.wild_ghost_peppers,     0.65],
		[i.wild_oats,              0.65],
		[i.wild_peanuts,           0.65],
	], ON)
	C.values('crates', [
		[i.cucumber_crate,                      1.0],
		[i.ghost_pepper_crate,                  1.0],
		[i.peanut_crate,                        1.0],
		[i.oat_bag,                             1.0],
	], ON)
	C.values('other', [   // the port's own chances (VintageDelight.commonSetup); without a row the eggs would not compost
		[i.oat,                                0.3],
		[i.oat_seeds,                          0.3],
		[i.cucumber,                           0.3],
		[i.cucumber_seeds,                     0.3],
		[i.peanut,                             0.4],
		[i.ghost_pepper,                       0.4],
		[i.ghost_pepper_seeds,                 0.3],
		[i.pickle,                             0.4],
		[i.pickled_sea_pickle,                 0.5],   // Brewin's Pickled Pickles' 0.5 (merged into it)
		[i.pickled_pepper,                     0.4],
		[i.pickled_beetroot,                   0.6],
		[i.pickled_onion,                      0.6],
		[i.pickled_egg,                        0.6],
		[i.century_egg,                        0.9],
		[i.magic_peanut,                       0.85],
	], ON)
})()
