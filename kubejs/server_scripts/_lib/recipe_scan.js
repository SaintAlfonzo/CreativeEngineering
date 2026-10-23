// priority: -10000
// ============================================================
// Diagnostic: print recipes referencing removed items (once per session)
// ============================================================
ServerEvents.recipes(event => {
	if (global.hasScannedRemovedItemRecipes) {
		console.log('[recipe_scan] Skipping recipe scan (already ran this session)')
		return
	}
	global.hasScannedRemovedItemRecipes = true

	function getRecipeId(recipe) {
		try { return recipe.getId().toString() } catch (err) {}
		try { return recipe.json.id.toString() } catch (err) {}
		try { return recipe.id.toString() } catch (err) {}
		return recipe.toString()
	}

	let totalHits = 0
	// Merged items (item_merges.js) are swapped with replaceInput / replaceOutput in this same event, which KubeJS
	// applies only after every script has run, so the json here still names them. Not orphans: skip them.
	const merged = (global.itemMerges || []).map(m => m[0])

	global.removedItems.forEach(itemId => {
		if (merged.indexOf(itemId) >= 0) return
		let matches = []

		event.forEachRecipe({}, recipe => {
			let jsonStr
			try {
				jsonStr = recipe.json.toString()
			} catch (err) {
				return
			}

			if (jsonStr.indexOf(itemId) !== -1) {
				matches.push(getRecipeId(recipe))
			}
		})

		if (matches.length > 0) {
			totalHits += matches.length
			console.log(`[jeiRemove] ${itemId} -> ${matches.length} recipe(s):`)
			matches.forEach(m => console.log(`    ${m}`))
		}
	})

	console.log(`[recipe_scan] Scan complete. ${totalHits} total recipe references found across ${global.removedItems.length} removed items.`)
})

// ============================================================
// Tag stripping
// ============================================================
ServerEvents.tags('item', event => {
	global.removedItems.forEach(id => {
		event.removeAllTagsFrom(id)
	})
})
