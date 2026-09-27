// /dumprecipes [mod ...]   ->  writes kubejs/exported/recipes/
//
// Recipes are read straight off the live server's RecipeManager (ServerLifecycleHooks.getCurrentServer()
// .getRecipeManager().getRecipes()) at the moment the command runs, not cached ahead of time. Grouping
// by mod is the namespace of each recipe's id (same value KubeJS's own recipe.getMod() returns).
//
// NOTE: ServerEvents.recipes (the build-time event) is the wrong place for a full snapshot - its
// RecipeJS wrapper (with the handy .json field) only reflects recipes added/visible by the time
// *this* script's handler runs, which depends on file load order across server_scripts (alphabetical
// by path; "_lib" sorts before basically every other folder). That's why an earlier version of this
// script only saw ~14 static datapack recipes and missed everything NCH generates elsewhere.
//
// A later version instead cached a snapshot on ServerEvents.afterRecipes (KubeJS's post-build
// "recipe manager finished reloading" event) - in theory the fix, but in this pack that event's
// listener (KubeJSReloadListener, a ResourceManagerReloadListener) never actually fired: not on
// initial world load, not on /reload. Querying the RecipeManager directly at command time sidesteps
// that dead event chain entirely and works on demand, no pre-population required.
//
// It still died silently after ~25 recipes: a recipe call threw a java.lang.Error, which JS try/catch
// can't catch in this Rhino fork and the chat-command CompletableFuture swallows unlogged. Ids now come
// from getRecipeIds() and the one remaining recipe call is wrapped in guardThrowable (java_guard.js).
//
//   recipes/_summary.json        per mod: recipe_count, by_type breakdown
//   recipes/by_mod/<mod>.json    every recipe from that mod: {count, recipes: [{id, type}, ...]} sorted by id
//   recipes/all.csv              id,type,mod   flat view for quick grep/sort/diff
//
//   filter tokens (space- or comma-separated). Default (no token) = every mod.
//     <namespace>   only recipes whose id lives in that mod, e.g. immersiveengineering
//
//   Also callable from other scripts: dumpRecipes(['create', 'immersiveengineering'])

function getRecipeManager() {
	var server = Java.loadClass('net.minecraftforge.server.ServerLifecycleHooks').getCurrentServer()
	return server ? server.getRecipeManager() : null
}

function getRecipeType(recipe) {
	try { return recipe.getType().toString() } catch (err) {}
	return 'unknown'
}

ServerEvents.commandRegistry(event => {
	var { commands: Commands, arguments: Arguments } = event

	var run = mods => ctx => {
		console.log('[dumprecipes] command started, filter: ' + (mods.length ? mods.join(',') : '(all mods)'))
		try {
			var r = dumpRecipes(mods)
			ctx.source.sendSuccess(
				Text.green(`Dumped ${r.total} recipes across ${r.mods} mod(s) to kubejs/exported/recipes/`), false)
		} catch (err) {
			console.error('[dumprecipes] command failed: ' + err, err)
			ctx.source.sendFailure(Text.red('dumprecipes failed - see log: ' + err))
		}
		return 1
	}

	event.register(
		Commands.literal('dumprecipes')
			.requires(src => src.hasPermission(2))
			.executes(run([]))
			.then(
				Commands.argument('mods', Arguments.GREEDY_STRING.create(event))
					.executes(ctx => run(
						Arguments.GREEDY_STRING.getResult(ctx, 'mods')
							.split(/[,\s]+/).map(s => s.trim().toLowerCase()).filter(Boolean)
					)(ctx))
			)
	)
})

// mods: array of namespace strings to include. Empty = all.
function dumpRecipes(mods) {
	console.log('[dumprecipes] resolving RecipeManager...')
	var manager = getRecipeManager()
	if (!manager) {
		console.warn('[dumprecipes] no RecipeManager available - is a server/world actually running?')
		return { total: 0, mods: 0 }
	}

	console.log('[dumprecipes] RecipeManager has ' + manager.getRecipes().size() + ' recipes - building cache...')

	// Ids come off the manager's own key set, so id/mod never depend on calling into a recipe. Only the
	// type does, and some mod recipe classes throw a java.lang.Error from getType()/getSerializer() -
	// which plain JS try/catch can't stop (see java_guard.js), so that one call goes through guardThrowable.
	var cache = []
	var failedByClass = {}
	var failed = 0
	var it = manager.getRecipeIds().iterator()
	while (it.hasNext()) {
		var rl = it.next()
		var id = String(rl)
		var mod = String(rl.getNamespace())
		var recipe = manager.byKey(rl).orElse(null)
		if (!recipe) continue
		var g = guardThrowable(() => getRecipeType(recipe))
		if (g.ok) {
			cache.push({ id: id, type: g.value, mod: mod })
			continue
		}
		failed++
		var cls = 'unknown'
		try { cls = String(recipe.getClass().getName()) } catch (e) {}
		if (!failedByClass[cls]) {
			failedByClass[cls] = 0
			console.error(`[dumprecipes] ${cls} threw on getType() (first: ${id}): ${g.error}`)
		}
		failedByClass[cls]++
		cache.push({ id: id, type: 'error', mod: mod })
	}
	console.log(`[dumprecipes] cache built: ${cache.length} recipes, ${failed} with an unreadable type`
		+ (failed ? ' ' + JSON.stringify(failedByClass) : ''))

	var want = (mods && mods.length) ? mods : null

	var byMod = {}
	cache.forEach(r => {
		if (want && want.indexOf(r.mod) === -1) return
		;(byMod[r.mod] = byMod[r.mod] || []).push(r)
	})

	var summary = {}
	var flatRows = []
	var total = 0

	// JSON first: this also creates kubejs/exported/recipes/ (needed by the CSV writer)
	Object.keys(byMod).sort().forEach(ns => {
		var list = byMod[ns].sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
		total += list.length

		var byType = {}
		list.forEach(r => { byType[r.type] = (byType[r.type] || 0) + 1 })
		summary[ns] = { recipe_count: list.length, by_type: byType }

		writeExportJson(`kubejs/exported/recipes/by_mod/${ns}.json`, { count: list.length, recipes: list.map(r => ({ id: r.id, type: r.type })) })
		list.forEach(r => flatRows.push({ id: r.id, type: csvSafe(r.type), mod: ns }))
	})

	writeExportJson('kubejs/exported/recipes/_summary.json', summary)
	writeCsv('all.csv', flatRows)

	console.log(`[dumprecipes] ${total} recipes across ${Object.keys(byMod).length} mod(s) -> kubejs/exported/recipes/`)
	return { total: total, mods: Object.keys(byMod).length }
}

function csvSafe(s) {
	return String(s == null ? '' : s).replace(/[\r\n]+/g, ' ').replace(/,/g, ';')
}

// CsvIO (from the kubejs_csv mod) writes a JsonArray of flat objects; falls back to a JSON
// sidecar if the CSV mod isn't present (same convention as dumpgear).
function writeCsv(name, rows) {
	try {
		CsvIO.write('kubejs/exported/recipes/' + name, rows)
	} catch (err) {
		console.warn(`[dumprecipes] CsvIO unavailable (${err}); writing ${name}.json instead`)
		writeExportJson('kubejs/exported/recipes/' + name.replace(/\.csv$/, '') + '_flat.json', { rows: rows })
	}
}
