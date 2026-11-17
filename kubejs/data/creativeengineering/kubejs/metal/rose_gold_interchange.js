// requires: creativeengineering
// Rose gold counts as gold (TODO recipes/metal #3, owner 2026-11-07; #7 closed as a decision 2026-11-14, plan_bugfix_1114.md
// 4.2). The ingot, nugget, dust, block and rod are already in the forge gold tags (MCA's own tags, tags/metaltags.js); the
// plate and wire join them there. Here every recipe that still names a literal gold item takes the gold tag instead, so rose
// gold fits all of them except the recipes that make the raw gold forms themselves (gold or rose gold ore, raw, crushed,
// dust, nugget, ingot, block, bars, plate, rod, wire and their storage blocks and coils), which would turn one metal into
// the other. Everything else takes either: golden tools, armor and food, components, equipment, shields and magic.
// Pack scripts that name a literal gold item are edited where they are (components, redstone, process).
ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH.forPack('creativeengineering')

	var goldOnly = []
	var forms = ['ore', 'ds_ore', 'raw', 'raw_block', 'crushed', 'crushed_block', 'dust', 'dust_block', 'nugget', 'ingot',
		'block', 'plate', 'plate_block', 'rod', 'wire', 'wire_coil', 'coil_block']
	var keys = ['gold_bars', 'ornate_gold_bars']
	forms.forEach(f => { keys.push(f + '_gold'); keys.push(f + '_rose_gold') })
	keys.forEach(key => {
		var id = i[key]
		if (typeof id !== 'string' || id == i.na || id == i.none || !/^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(id)) return
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
