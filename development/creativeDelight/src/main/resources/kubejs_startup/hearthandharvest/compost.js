// requires: hearthandharvest
// Hearth and Harvest compost values (hearth_and_harvest.md 4.2 step 13, N9). Engine: global.compost; tiers and rules are
// creativedelight/compost_rules.js. Startup script in the CD jar: an edit needs a CD rebuild and a restart.
;(function () {
	var i = global.item
	var C = global.compost
	var ON = { when: () => global.CD.qol('compost') && global.CD.on('hearthandharvest') }

	C.values('crates', [
		[i.hops_bag,         1.0],
		[i.manure_bag,       1.0],
		[i.corn_husk_bundle, 1.0],
	], ON)
	C.values('other', [
		[i.manure,          0.85],
		[i.hops,            0.3],
		[i.corn_husk,       0.3],
		[i.sunflower_seeds, 0.3],
	], ON)
})()
