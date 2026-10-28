// requires: farmersdelight
// Farmer's Delight compost values: Creative Delight QoL-4 (creative_delight.md 8). Moved 2026-10-25 from the pack's
// startup_scripts/compost/compost_values.js, same rows and chances (48 items); the engine is patcherMod's
// (global.compost, func/compost.js), tiers and rules are creativeengineering/compost_rules.js here. Startup script in the CD jar: an edit needs a CD rebuild and restart.
// The rows count only while [qol] compost is on (checked at every table rebuild, after the configs load); off, these
// foods fall to the rules.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => global.CD.qol('compost') && global.CD.on('farmersdelight') }

	C.values('seeds', [
		[i.cabbage_seeds,             0.3],
		[i.farmersdelight_rice,       0.3],
		[i.tomato_seeds,              0.3],
	], ON)
	C.values('crops', [
		[i.farmersdelight_cabbage, 0.65],
		[i.cabbage_leaf,           0.5],
		[i.onion_farmersdelight,   0.65],
		[i.pumpkin_slice,          0.5],
		[i.farmersdelight_tomato,  0.65],
		[i.wild_beetroots,         0.65],
		[i.wild_cabbages,          0.65],
		[i.wild_carrots,           0.65],
		[i.wild_onions,            0.65],
		[i.wild_potatoes,          0.65],
		[i.wild_rice,              0.65],
		[i.wild_tomatoes,          0.65],
	], ON)
	C.values('baked', [
		[i.honey_cookie,               0.85],
		[i.sweet_berry_cookie,         0.85],
	], ON)
	C.values('cakes_pies', [
		[i.apple_pie,                              1.0],
		[i.apple_pie_slice,                        0.85],
		[i.cake_slice,                             0.85],
		[i.chocolate_pie,                          1.0],
		[i.chocolate_pie_slice,                    0.85],
		[i.pie_crust,                              0.65],
	], ON)
	C.values('plant_matter', [
		[i.straw,                            0.3],
		[i.tree_bark,                        0.3],
	], ON)
	C.values('plants', [
		[i.brown_mushroom_colony, 1.0],
		[i.red_mushroom_colony,   1.0],
		[i.sandy_shrub,           0.3],   // was under the Fungi Delight rows
	], ON)
	C.values('crates', [
		[i.farmersdelight_beetroot_crate,       1.0],
		[i.cabbage_crate,                       1.0],
		[i.farmersdelight_carrot_crate,         1.0],
		[i.onion_crate,                         1.0],
		[i.farmersdelight_potato_crate,         1.0],
		[i.rice_bag,                            1.0],
		[i.rice_bale,                           0.85],
		[i.tomato_crate,                        1.0],
	], ON)
	C.values('other', [
		[i.cabbage_rolls,                      0.85],
		[i.dumplings,                          1.0],
		[i.kelp_roll,                          0.85],
		[i.kelp_roll_slice,                    0.5],
		[i.raw_pasta,                          0.85],
		[i.rice_panicle,                       0.3],
		[i.rice_medley,                        1.0],
		[i.rotten_tomato,                      0.85],
		[i.stuffed_pumpkin_block,              1.0],
		[i.sweet_berry_cheesecake,             1.0],
		[i.sweet_berry_cheesecake_slice,       0.85],
		[i.out_dough,                          0.85],
	], ON)
})()
