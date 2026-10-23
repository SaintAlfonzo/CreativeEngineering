// Mortar and pestle (Old Guns) grinding for other mods' food recipes (TODO cooking/kitchen #10): one shapeless craft
// per entry of kubejs/generation_scripts/food/mortar_catalog.json, written by tools/gen_mortar_catalog.py from the
// /dumpfood recipe export (Create milling and crushing whose main output is a food ingredient). The pack's own
// grinding gets its mortar craft from R.foodGrinding (cooking.js), so it isn't in the catalog. Regenerate the
// catalog after adding mods or changing grinding recipes, then /reload.
ServerEvents.recipes(e => {
	var mortarItems = global.item
	var mortarCatalog = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/mortar_catalog.json')))
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
