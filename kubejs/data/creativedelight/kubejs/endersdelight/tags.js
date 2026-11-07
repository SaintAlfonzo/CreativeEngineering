// requires: creativedelight
// requires: endersdelight
// Ender's Delight tags (non-member; ends_delight.md 3.1). Ender's entries of the shared End food tags (End's entries:
// ends_delight/tags.js) and the mining tags its blocks lack. The soils and spice of enders_additions (1.1.4 content,
// loaded without 1.2.2) are in additions.js.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	t.storageBlock(e, 'chorus_fruit', i.chorus_crate)
	t.add(e, i.t_chorus_pie_slice, i.chorus_pie_slice)
	// Shulker meat: Ender's mollusk and filet merge into End's meat and slice (CD startup endersdelight/item_merges.js)
})

ServerEvents.tags('block', e => {
	if (!global.CD) return
	var t = global.tags
	var b = global.blocks

	// The Chorus Crate and End Stone Stove had no mining tag in 1.2.2 (1.1.4 tags them axe / pickaxe)
	t.storageBlock(e, 'chorus_fruit', b.chorus_crate, { tool: 'axe' })
	t.add(e, b.t_mineable_pickaxe, b.endstone_stove)
})
