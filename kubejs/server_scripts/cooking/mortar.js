// Mortar and pestle (Old Guns) grinding for other mods' food recipes (TODO food/kitchen #10): one shapeless craft
// per entry of kubejs/generation_scripts/food/mortar_catalog.json, written by tools/gen_mortar_catalog.py from the
// /dumpfood recipe export (Create milling and crushing whose main output is a food ingredient). The pack's own
// grinding gets its mortar craft from R.foodGrinding (cooking.js), so it isn't in the catalog. Regenerate the
// catalog after adding mods or changing grinding recipes, then /reload.
ServerEvents.recipes(e => {
	var mortarItems = global.item
	var mortarCatalog = JSON.parse(String(JsonIO.readString('kubejs/generation_scripts/food/mortar_catalog.json')))
	mortarCatalog.recipes.forEach(entry => {
		var input = entry.inputKey ? mortarItems[entry.inputKey] : entry.input
		var output = entry.outputKey ? mortarItems[entry.outputKey] : entry.output
		global.NCH.mortar(Item.of(output, Math.round(entry.count)), input, e, entry.id, Math.round(entry.inputs))
	})
	console.log('[mortar] ' + mortarCatalog.recipes.length + ' catalog crafts')
})
