// requires: creativedelight
// requires: ends_delight
// End's Delight tags (Creative Delight member, ends_delight.md 3.1; TODO largecontent/mod-additions #4, #15). The
// shared End food tags get their End's entries here and their Ender's entries in endersdelight/tags.js.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('ends_delight')) return
	var t = global.tags
	var i = global.item

	// chorus crates: one unpack route for both (2.6.1's forge:storage_blocks/chorus_fruit)
	t.storageBlock(e, 'chorus_fruit', i.chorus_fruit_crate)
	// chorus pie slices of both mods, for whichever recipe picks a chorus pie
	t.add(e, i.t_chorus_pie_slice, i.chorus_fruit_pie_slice)
	// shulker meat: End's meat may stand in for Ender's in the pack's Ender's recipes (#endersdelight:shulker_loot)
	t.add(e, i.shulker_loot, [i.shulker_meat, i.shulker_meat_slice])
	// fungi End's crimson fungus inputs accept (mod-additions #15): crimson, warped, Fungi Delight's edible ones
	t.add(e, i.t_cookable_fungi, [i.crimson_fungus, i.warped_fungus])
	if (Platform.isLoaded('fungidelight')) t.add(e, i.t_cookable_fungi, [i.inky_cap_mushroom, i.morel_mushroom, i.truffle])
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('ends_delight')) return
	global.tags.storageBlock(e, 'chorus_fruit', global.blocks.chorus_fruit_crate, { tool: 'axe' })
})
