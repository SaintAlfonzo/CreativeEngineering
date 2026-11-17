// requires: creativedelight
// requires: incubation
// Incubation item tags.
ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item

	// redundant egg
	t.clearAll(e, i.fried_egg_incubation)
})
