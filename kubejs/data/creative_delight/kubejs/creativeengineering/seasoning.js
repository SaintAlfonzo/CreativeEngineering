// The pack's seasoning crafts on foods. Moved from the pack's kubejs/server_scripts/cooking/seasoning.js 2026-10-25,
// rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	const R = global.NCH
	var i = global.item

	global.CD.recipes(() => {

		// ============================================================
		// Pack seasonings on foods (development/creativeDelight/src/main/resources/kubejs_startup/creativeengineering/seasonings.js holds the seasonings themselves)
		// ============================================================
		// Each craft is food + the seasoning item(s) in the grid -> the same food carrying those seasonings. A food takes a
		// seasoning once; seasonings that don't combine can't share a food. Onion and garlic combine, so garlic popcorn +
		// onion, onion popcorn + garlic, or popcorn + both at once all give the same onion and garlic popcorn.

		// Test set: popcorn (food/seasoning #3)
		R.season(i.popcorn, 'cheese', e)
		R.season(i.popcorn, 'onion', e)
		R.season(i.popcorn, 'garlic', e)
		R.season(i.popcorn, ['garlic', 'onion'], e)

		// Sugar (sugar or powdered sugar; food/seasoning #5, dishes/ingredients #16): test food only, which foods take it is open
		R.season(i.waffle, 'sugar', e)
	})
})
