// Creative Delight scratch test suite: recipe id namespace (Q24), member switch, server load order. SQ-CDT; never
// in the pack or a release jar. `/cdtest ns` checks the ids below.
global.cdtestState = {
	// pack server-script globals exist when a CD script loads (the pack folder runs first, 10.2)
	lootAtLoad: typeof global.LOOT,
	cdAtLoad: typeof global.CD,
}

ServerEvents.recipes(e => {
	var i = global.item
	var R = global.NCH
	global.CD.recipes(() => {
		global.cdtestState.nsInside = R.ns()
		R.shapeless(Item.of(i.wheat, 2), [i.bread], e, 'cdtest/own_shapeless')
		R.withNamespace('cdtest_inner', () => {
			R.shapeless(Item.of(i.wheat, 3), [i.bread, i.bread], e, 'cdtest/nested')
		})
		R.shapeless(Item.of(i.wheat, 4), [i.bread, i.bread, i.bread], e, 'cdtest/after_nested')
		// a member row: only when the member is loaded and its [members] switch is on
		if (global.CD.on('oceanic_delight')) R.shapeless(i.fishing_net, [i.string, i.string, i.stick, i.string], e, 'cdtest/oceanic_member_row')
	})
	global.cdtestState.nsAfter = R.ns()
	R.shapeless(Item.of(i.wheat, 5), [i.bread, i.bread, i.bread, i.bread], e, 'cdtest_ns_restored')
	// an error inside a block: the namespace must still be restored (withNamespace's finally)
	R.withNamespace('creativedelight', () => { throw new Error('cdtest deliberate error, expect one [NCH] withNamespace failed line') })
	global.cdtestState.nsAfterThrow = R.ns()
	global.cdtestState.memberOn = global.CD.on('oceanic_delight')
})
