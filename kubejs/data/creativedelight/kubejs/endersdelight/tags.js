// requires: endersdelight
// Ender's Delight tags (non-member; ends_delight.md 3.1). Ender's entries of the shared End food tags (End's entries:
// ends_delight/tags.js), the mining tags its blocks lack, and the soils and spice of the enders_additions member.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.storageBlock(e, 'chorus_fruit', i.chorus_crate)
	t.add(e, i.t_chorus_pie_slice, i.chorus_pie_slice)
	// Shulker meat: Ender's mollusk and filet merge into End's meat and slice (CD startup endersdelight/item_merges.js)

	// Void Pepper is the first spice (briefs/food_seasoning.md); saffron is not (owner, 2026-10-25)
	if (global.CD.on('enders_additions')) t.add(e, i.t_spices, [i.voidpepper, i.crushed_voidpepper])
})

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var t = global.tags
	var b = global.blocks

	// The Chorus Crate and End Stone Stove had no mining tag in 1.2.2 (1.1.4 tags them axe / pickaxe)
	t.storageBlock(e, 'chorus_fruit', b.chorus_crate, { tool: 'axe' })
	t.add(e, b.t_mineable_pickaxe, b.endstone_stove)

	// Soils of the End plants (enders_additions): survive on both, grow slowly on end stone, faster when tilled.
	// Rates: config/enders_additions-common.toml [growth].
	if (global.CD.on('enders_additions')) {
		t.add(e, b.t_end_plant_soil_slow, b.end_stone)
		if (Platform.isLoaded('endercrop')) t.add(e, b.t_end_plant_soil_fast, b.tilled_end_stone)
	}
})
