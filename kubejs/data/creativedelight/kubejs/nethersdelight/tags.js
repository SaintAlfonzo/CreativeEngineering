// requires: nethersdelight
// Nether's Delight tags (Creative Delight member repackedNethers; nethers_delight.md 4.2, 4.9; TODO
// largecontent/mod-additions #16): the Nether's Delight 4.0 tag changes on the 3.1 repack.
ServerEvents.tags('item', e => {
	if (!global.CD || !global.CD.on('nethersdelight')) return
	var i = global.item
	// 4.0: Stuffed Potato counts as a Nether's Delight meal
	global.tags.add(e, i.t_nd_meal_item, i.stuffed_potato)
})

ServerEvents.tags('block', e => {
	if (!global.CD || !global.CD.on('nethersdelight')) return
	var b = global.blocks
	// 4.0: Potted Mimicarnation (registered by the repack's overlay) is a flower pot
	global.tags.add(e, b.t_flower_pots, b.potted_mimicarnation)
	// 4.0 retags hoglin_mount as hoglin_trophy under minecraft:mineable/axe; the id is kept, and 3.1 already tags it
})
