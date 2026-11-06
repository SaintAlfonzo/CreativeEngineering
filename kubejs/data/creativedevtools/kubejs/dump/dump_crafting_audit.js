// requires: creativedevtools
// /dumpcraftingaudit  ->  writes kubejs/exported/recipes/crafting_audit.json
//
// Facts JEI uses to decide whether a crafting-table recipe gets a page, one row per recipe whose
// RecipeType is minecraft:crafting. tools/jei_coverage.py turns this into the uncovered list.
//
// JEI 11.8 (VanillaRecipes + CategoryRecipeValidator + VanillaPlugin) shows a crafting recipe when:
//   - isSpecial() is false, the result is not empty, getIngredients() is not empty and has <= 9
//     entries (the default CraftingRecipe extension skips isSpecial() recipes), or
//   - isSpecial() is true and some plugin registered a category extension for its class, or
//   - JEI or a plugin adds stand-in display recipes for it (vanilla: tipped arrow, shulker box
//     coloring, suspicious stew, shield decoration).
// So this records: class + its supertypes (extensions match by class), serializer, isSpecial,
// result, ingredient count, and slots whose ingredient has no items (an empty tag still shows a
// page, but the recipe cannot be made).
//
// Also writes a per-RecipeType census (recipe_types.json) for the non-crafting half of the audit.
//
// Rhino here only sees Mojang method names (SRG names like m_6671_ are "not a function"), and
// Recipe.getType() is shadowed by KubeJS's kjs$getType (it returns the serializer id). So each
// recipe's real RecipeType comes from RecipeManager.getAllRecipesFor(type) over the type registry.

;(function () {
	var $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $Ingredient = Java.loadClass('net.minecraft.world.item.crafting.Ingredient')

	function server() {
		return Java.loadClass('net.minecraftforge.server.ServerLifecycleHooks').getCurrentServer()
	}

	function supertypes(cls) {
		var out = []
		var seen = {}
		var queue = [cls]
		while (queue.length) {
			var c = queue.shift()
			if (!c) continue
			var name = String(c.getName())
			if (seen[name]) continue
			seen[name] = true
			out.push(name)
			queue.push(c.getSuperclass())
			var ifaces = c.getInterfaces()
			for (var k = 0; k < ifaces.length; k++) queue.push(ifaces[k])
		}
		return out.filter(n => n !== 'java.lang.Object')
	}

	function stackId(stack) {
		if (!stack || stack.isEmpty()) return ''
		var key = $ForgeRegistries.ITEMS.getKey(stack.getItem())
		return String(key) + (stack.getCount() > 1 ? ' x' + stack.getCount() : '')
	}

	function auditRecipe(recipe, id) {
		var row = { id: id }
		row.cls = String(recipe.getClass().getName())
		var ser = guardThrowable(() => String($ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipe.getSerializer())))
		row.serializer = ser.ok ? ser.value : 'error'
		var sp = guardThrowable(() => recipe.isSpecial() == true)
		row.special = sp.ok ? sp.value : 'error'
		var out = guardThrowable(() => stackId(recipe.getResultItem()))
		row.result = out.ok ? out.value : 'error: ' + out.error
		var ing = guardThrowable(() => {
			var list = recipe.getIngredients()
			var n = list.size()
			var blank = 0
			var dead = []
			for (var k = 0; k < n; k++) {
				var ingr = list.get(k)
				// Ingredient.getItems() is hidden behind KubeJS's IngredientKJS; getStacks() is its view.
				// Shaped-recipe gaps are the Ingredient.EMPTY singleton (Ingredient has no equals override).
				if (ingr.equals($Ingredient.EMPTY)) { blank++; continue }
				if (ingr.getStacks().size() > 0) continue
				dead.push(String(ingr))
			}
			return { n: n, blank: blank, dead: dead }
		})
		if (ing.ok) {
			row.ingredients = ing.value.n
			row.blank = ing.value.blank
			if (ing.value.dead.length) row.dead = ing.value.dead
		} else {
			row.ingredients = 'error: ' + ing.error
		}
		return row
	}

	function dumpCraftingAudit() {
		var manager = server().getRecipeManager()
		var rows = []
		var classes = {}
		var types = {}
		var typeOf = {}
		var typeIt = $ForgeRegistries.RECIPE_TYPES.getEntries().iterator()
		while (typeIt.hasNext()) {
			var entry = typeIt.next()
			var typeId = String(entry.getKey().location())
			var g0 = guardThrowable(() => manager.getAllRecipesFor(entry.getValue()))
			if (!g0.ok) { console.error(`[dumpcraftingaudit] getAllRecipesFor(${typeId}) threw: ${g0.error}`); continue }
			var listIt = g0.value.iterator()
			while (listIt.hasNext()) typeOf[String(listIt.next().getId())] = typeId
		}
		var it = manager.getRecipeIds().iterator()
		while (it.hasNext()) {
			var rl = it.next()
			var id = String(rl)
			var recipe = manager.byKey(rl).orElse(null)
			if (!recipe) continue
			var type = typeOf[id] || 'unknown'
			var cls = String(recipe.getClass().getName())
			var ty = types[type] = types[type] || { count: 0, classes: {}, serializers: {} }
			ty.count++
			ty.classes[cls] = (ty.classes[cls] || 0) + 1
			var s = guardThrowable(() => String($ForgeRegistries.RECIPE_SERIALIZERS.getKey(recipe.getSerializer())))
			var sk = s.ok ? s.value : 'error'
			ty.serializers[sk] = (ty.serializers[sk] || 0) + 1
			if (type !== 'minecraft:crafting') continue
			var g = guardThrowable(() => auditRecipe(recipe, id))
			rows.push(g.ok ? g.value : { id: id, cls: cls, error: String(g.error) })
			if (!classes[cls]) {
				var h = guardThrowable(() => supertypes(recipe.getClass()))
				classes[cls] = h.ok ? h.value : ['error: ' + h.error]
			}
		}
		rows.sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
		writeExportJson('kubejs/exported/recipes/crafting_audit.json', { count: rows.length, classes: classes, recipes: rows })
		writeExportJson('kubejs/exported/recipes/recipe_types.json', types)
		console.log(`[dumpcraftingaudit] ${rows.length} crafting recipes, ${Object.keys(classes).length} classes, ${Object.keys(types).length} recipe types`)
		return rows.length
	}

	ServerEvents.commandRegistry(event => {
		var { commands: Commands } = event
		event.register(
			Commands.literal('dumpcraftingaudit')
				.requires(src => src.hasPermission(2))
				.executes(ctx => {
					var g = guardThrowable(() => dumpCraftingAudit())
					if (g.ok) ctx.source.sendSuccess(Text.green(`Audited ${g.value} crafting recipes -> kubejs/exported/recipes/crafting_audit.json`), false)
					else {
						console.error('[dumpcraftingaudit] failed: ' + g.error)
						ctx.source.sendFailure(Text.red('dumpcraftingaudit failed - see log: ' + g.error))
					}
					return 1
				})
		)
	})
})()
