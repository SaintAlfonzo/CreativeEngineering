// requires: creativedelight
// Mortar and pestle (Old Guns) grinding for other mods' food recipes (TODO cooking/kitchen #10): one shapeless craft
// per catalog entry, written by tools/gen_mortar_catalog.py from the /dumpfood recipe export (Create milling and
// crushing whose main output is a food ingredient) into one <mod>/mortar_catalog.js member script per source recipe's
// mod (`// requires: <mod>`, framework #7 2026-11-01; was the JSON read by instance path). The pack's own grinding gets
// its mortar craft from R.foodGrinding (cooking.js), so it isn't in the catalog. Regenerate the catalog after adding
// mods or changing grinding recipes, then /reload.
// Moved from the pack's kubejs/server_scripts/cooking/mortar.js 2026-10-25, rows unchanged.
ServerEvents.recipes(e => {
	if (!global.CD) return

	global.CD.recipes(() => {
		var mortarItems = global.item
		var mortarCatalog = global.CD.genMerge('mortar_catalog')
		if (!mortarCatalog.recipes) mortarCatalog.recipes = []
		// The catalog can predate an item merge: an entry naming a purged item copies a recipe the merge removed.
		var mortarPurged = global.removedItems || []
		var mortarCount = 0
		mortarCatalog.recipes.forEach(entry => {
			var input = entry.inputKey ? mortarItems[entry.inputKey] : entry.input
			var output = entry.outputKey ? mortarItems[entry.outputKey] : entry.output
			if (mortarPurged.indexOf(String(input)) >= 0 || mortarPurged.indexOf(String(output)) >= 0) return
			global.NCH.mortar(Item.of(output, Math.round(entry.count)), input, e, entry.id, Math.round(entry.inputs))
			mortarCount++
		})
		console.log('[mortar] ' + mortarCount + ' of ' + mortarCatalog.recipes.length + ' catalog crafts')
	})
})
