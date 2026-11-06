// requires: creativedevtools
// priority: -10000
// ============================================================
// Diagnostic: recipes that still use or make a removed item
// ============================================================
//
// Reads the live RecipeManager, so it sees the final recipe set: every script's removals, replaceInput /
// replaceOutput and the item merges are already applied. (Inside ServerEvents.recipes they are not: KubeJS
// applies them only after every handler has run. ServerEvents.afterRecipes never fires in this pack, see
// dump_recipes.js.) Merged items are therefore not skipped: one still named here is a real leftover.
//
// One pass over all recipes against a set of removed ids. Per recipe it reads the ingredients
// (getIngredients, tags resolved, and removed items have their tags stripped below), the result
// (getResultItem) and the extra outputs of Create (getRollableResults) and IE (getItemOutputs). Outputs other
// mods keep in their own fields are not seen.
//
// Creative Devtools (creative_devtools.md 2); the removed-item tag strip it used to share a file with stays in the
// pack (server_scripts/_lib/removed_item_tags.js).
//
// Off by default. creativedevtools-common.toml [debug.recipe_scan] runAtLoad = true runs it once per session when the server has
// loaded; /recipescan runs it on demand (op 2), e.g. after a /reload. Output: logs/kubejs/server.log,
// "[jeiRemove] <id> -> N recipe(s):" with the recipe ids, then "[recipe_scan] Scan complete. ...".

function scanRemovedItemRecipes(server) {
	var removed = {}
	global.removedItems.forEach(id => { removed[String(id)] = true })

	var hits = {}
	var unreadable = 0
	var add = (stack, recipeId, side) => {
		if (!stack || stack.isEmpty()) return
		var id = String(stack.id)
		if (!removed[id]) return
		var list = hits[id] = hits[id] || []
		var entry = recipeId + ' (' + side + ')'
		if (list.indexOf(entry) < 0) list.push(entry)
	}

	var manager = server.getRecipeManager()
	var it = manager.getRecipeIds().iterator()
	var total = 0
	while (it.hasNext()) {
		var rl = it.next()
		var recipe = manager.byKey(rl).orElse(null)
		if (!recipe) continue
		total++
		var recipeId = String(rl)
		// Some mod recipe classes throw a java.lang.Error that plain try/catch misses (java_guard.js)
		var g = guardThrowable(() => {
			// getStacks, not getItems: KubeJS hides Ingredient.getItems, which threw for every recipe with inputs
			recipe.getIngredients().forEach(ing => {
				ing.getStacks().forEach(s => add(s, recipeId, 'input'))
			})
			add(recipe.getResultItem(), recipeId, 'output')
			if (typeof recipe.getRollableResults === 'function') {
				recipe.getRollableResults().forEach(o => add(o.getStack(), recipeId, 'output'))
			}
			if (typeof recipe.getItemOutputs === 'function') {
				recipe.getItemOutputs().forEach(s => add(s, recipeId, 'output'))
			}
		})
		if (!g.ok) unreadable++
	}

	var totalHits = 0
	Object.keys(hits).sort().forEach(id => {
		totalHits += hits[id].length
		console.log(`[jeiRemove] ${id} -> ${hits[id].length} recipe(s):`)
		hits[id].sort().forEach(m => console.log(`    ${m}`))
	})

	console.log(`[recipe_scan] Scan complete. ${totalHits} total recipe references found across `
		+ `${global.removedItems.length} removed items (${Object.keys(hits).length} still referenced, `
		+ `${total} recipes read, ${unreadable} unreadable).`)
	return { hits: totalHits, items: Object.keys(hits).length, unreadable: unreadable }
}

ServerEvents.loaded(event => {
	if (global.hasScannedRemovedItemRecipes) return
	if (!global.CDV || !global.CDV.recipeScanAtLoad()) return
	global.hasScannedRemovedItemRecipes = true
	scanRemovedItemRecipes(event.server)
})

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('recipescan')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				var r = scanRemovedItemRecipes(ctx.source.server)
				ctx.source.sendSuccess(Text.of(`Recipe scan: ${r.hits} references to ${r.items} removed items`
					+ (r.unreadable ? `, ${r.unreadable} recipes unreadable` : '') + ' - see logs/kubejs/server.log'), false)
				return 1
			})
	)
})
