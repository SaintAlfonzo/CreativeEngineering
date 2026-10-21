ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	console.log('metaltagremoval starting')

	// ─────────────────────────────────────────────────────────────────────
	// Add canonical parts from the dict [ type, metal, itemId ]
	// (copper rod & IE iron wire normalized to the full trio)
	// ─────────────────────────────────────────────────────────────────────
	;[
		['rods', 'copper', i.rod_copper],
		['rods', 'iron', i.rod_iron],
		['rods', 'gold', i.rod_gold],
		['rods', 'zinc', i.rod_zinc],
		['rods', 'gold', i.rod_rose_gold],
		['wires', 'iron', i.wire_iron],
		['wires', 'gold', i.wire_gold],
		['wires', 'zinc', i.wire_zinc],
		['wires', 'silver', i.wire_silver],
		['wires', 'nickel', i.wire_nickel],
		['wires', 'brass', i.wire_brass],
		['plates', 'zinc', i.plate_zinc],
	].forEach(([type, metal, id]) => {
		t.addTag(e, `forge:${type}/${metal}`, id) // -> forge:<type>/<metal>, forge:<type>
		t.add(e, `forge:${type}/all_metal`, id)
	})

	// any metal dust or grit (Steel Toe Stout, alcohol.md 8.6)
	t.add(e, i.t_metal_dusts, [
		i.dust_copper, i.dust_iron, i.dust_gold, i.dust_zinc, i.dust_aluminum, i.dust_lead, i.dust_nickel,
		i.dust_silver, i.dust_uranium, i.dust_cast_iron, i.dust_rose_gold, i.dust_brass, i.dust_steel,
		i.dust_constantan, i.dust_electrum,
	])

	// spool
	t.addTag(e, 'forge:spool/iron', i.spool)
	t.add(e, 'forge:spool/all_metal', i.spool)

	// nuggets / storage blocks (no /all_metal)
	t.addTag(e, 'forge:nuggets/gold', i.nugget_rose_gold)
	t.addTag(e, 'forge:nuggets/netherite', i.nugget_netherite)
	t.addTag(e, 'forge:storage_blocks/gold', i.block_rose_gold)

	// ─────────────────────────────────────────────────────────────────────
	// Compressed iron ⇄ steel bridging + alt spellings
	// ─────────────────────────────────────────────────────────────────────
	t.add(e, 'forge:gears/steel', i.compressed_gear)
	t.add(e, 'forge:storage_blocks/steel', i.block_steel)
	t.add(e, 'forge:ingots/compressed_iron', i.ingot_steel)
	t.add(e, 'forge:storage_blocks/aluminium', i.block_aluminum)
	t.add(e, 'forge:ingots/aluminium', i.ingot_aluminum)

	// ─────────────────────────────────────────────────────────────────────
	// VI springs -> forge:springs family (IE arc recycling) + stiffness
	// ─────────────────────────────────────────────────────────────────────
	const SPRINGS = {
		stiff: ['copper', 'iron', 'zinc', 'nickel', 'constantan', 'steel'],
		weak: ['gold', 'aluminum', 'silver', 'brass', 'electrum'],
	}
	for (const stiff in SPRINGS) {
		SPRINGS[stiff].forEach(metal => {
			const s = i['spring_' + metal]
			const ss = i['small_spring_' + metal]
			t.addTag(e, 'forge:springs/' + metal, s) // -> forge:springs/<metal>, forge:springs
			t.addTag(e, 'forge:springs_' + stiff, s)
			t.addTag(e, 'forge:small_springs_' + stiff, ss)
			t.add(e, 'forge:small_springs', ss) // unified parent (stiff/weak split has none by itself)
		})
	}
	// lead/rose_gold springs sit outside the stiff/weak split but still belong in forge:springs
	;['lead', 'rose_gold'].forEach(metal => {
		t.addTag(e, 'forge:springs/' + metal, i['spring_' + metal])
		t.add(e, 'forge:small_springs', i['small_spring_' + metal])
	})

	// ─────────────────────────────────────────────────────────────────────
	// Disassembly-blueprint ingredient tags: wire coils / coil blocks / components,
	// one tag per family so the blueprint crafting recipes can accept "any" of them.
	// ─────────────────────────────────────────────────────────────────────
	const COIL_METALS = ['copper', 'iron', 'gold', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'brass', 'constantan', 'steel', 'electrum']
	COIL_METALS.forEach(metal => {
		t.addTag(e, 'forge:wire_coils/' + metal, i['wire_coil_' + metal])
		t.addTag(e, 'forge:coil_blocks/' + metal, i['coil_block_' + metal])
	})
	t.add(e, 'forge:wire_coils', i.wirecoil_hv)
	t.add(e, 'forge:coil_blocks', i.coil_block_hv)

	const COMPONENT_METALS = ['copper', 'iron', 'gold', 'netherite', 'zinc', 'aluminum', 'lead', 'nickel', 'silver', 'uranium', 'brass', 'constantan', 'steel', 'electrum']
	COMPONENT_METALS.forEach(metal => {
		t.addTag(e, 'forge:components/' + metal, i['component_' + metal])
	})
	t.add(e, 'forge:components', i.component_diamond)

	t.addTag(e, 'forge:components/light', i.component_aluminum)
	t.addTag(e, 'forge:components/light', i.component_nickel)
	t.addTag(e, 'forge:components/light', i.component_silver)

	t.addTag(e, 'forge:components/heavy', i.component_lead)
	t.addTag(e, 'forge:components/heavy', i.component_uranium)

	console.log('tagremoval finished')
})
