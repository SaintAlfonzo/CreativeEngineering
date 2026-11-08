// requires: creativeengineering
// Rose gold counts as gold (TODO recipes/metal #3, owner 2026-11-07). The ingot, nugget, dust, block and rod are already in
// the forge gold tags (MCA's own tags, tags/metaltags.js); the plate and wire join them there. Here every recipe that still
// names a literal gold item takes the gold tag instead, so rose gold fits all of them except the recipes that make gold itself:
// gold blocks, ingots, nuggets, bars, dust, plates, rods and wires, and the golden armor, tools and foods, i.e. every dict item
// with gold or golden in its key. Components, equipment, shields and magic are not on that list and take either.
// Pack scripts that name a literal gold item are edited where they are (components, redstone, process).
ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH

	var goldOnly = []
	Object.keys(i).forEach(key => {
		if (!/gold|gilded/.test(key)) return
		if (/^(component|ce_)|shield|^t_|^any_/.test(key)) return
		var id = i[key]
		if (typeof id !== 'string' || !/^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(id)) return
		if (goldOnly.indexOf(id) < 0) goldOnly.push(id)
	})

	;[
		[i.ingot_gold, i.any_ingot_gold],
		[i.nugget_gold, i.any_nugget_gold],
		[i.block_gold, i.any_block_gold],
		[i.dust_gold, i.any_dust_gold],
		[i.plate_gold, i.any_plate_gold],
		[i.wire_gold, '#forge:wires/gold'],
		[i.rod_gold, i.any_rod_gold],
	].forEach(p => {
		R.replaceInput({ input: p[0], not: { output: goldOnly } }, p[0], p[1], e)
	})
})
