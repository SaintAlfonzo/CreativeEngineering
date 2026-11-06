// priority: 10
// requires: incubation
// Hand-written compost rows for Creative Delight's Incubation compartment: the nests (were incubation_additions:*,
// generated into kubejs_startup/incubation_additions/compost_values.js until M8, plan_cb_publish.md 3b; the rows are
// unchanged). They are creativedelight ids now, so tools/gen_compost_values.py skips them through this file. Row:
// [i.<item_dict key>, chance]; engine global.compost (Creative Common's func/compost.js). Startup script in the CD jar:
// CD rebuild + restart.

;(function () {
	if (!global.CD || !global.CD.on('incubation')) return
	var i = global.item
	var C = global.compost

	if (global.has('alexsmobs')) C.values('other', [
		[i.emu_nest,   0.65],
	])
	if (global.has('untitledduckmod')) C.values('other', [
		[i.duck_nest,  0.65],
		[i.goose_nest, 0.65],
	])
})()
