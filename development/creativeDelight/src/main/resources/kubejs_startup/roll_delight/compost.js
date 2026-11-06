// requires: roll_delight
// Roll Delight compost values (feature_writeup/roll_delight.md 4 step 14). Hand-written rows (gen_compost_values.py skips
// items listed here). Upstream registered the Ancient Roll and its slice at 100 %; the pack drops that and rates the
// plant-only rolls as Farmer's Delight's Kelp Roll (whole 0.85, slice 0.5, farmersdelight/compost.js). Out: the Salmon
// Maki, Nether and Tobiko rolls (fish, meat and roe never compost by rule), the Golden Roll (gold is excluded) and Cooked
// Nether Wart (a bowl food, as Cooked Rice). Engine: global.compost (patcherMod func/compost.js), tiers and rules in
// creativeengineering/compost_rules.js. Startup script in the CD jar: an edit needs a CD rebuild and restart.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => global.CD.on('roll_delight') }

	C.values('other', [
		[i.rice_roll,           0.85],
		[i.rice_roll_slice,     0.5],
		[i.mushroom_roll,       0.85],
		[i.mushroom_roll_slice, 0.5],
		[i.ancient_roll,        0.85],
		[i.ancient_roll_slice,  0.5],
	], ON)
})()
