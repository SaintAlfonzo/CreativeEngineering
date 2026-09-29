// /dumpfood   ->  writes kubejs/exported/food/  (and refreshes kubejs/exported/tags/ for items)
//
//   foods.json         every edible item as the game sees it right now (after any KubeJS edits):
//                      nutrition, saturation modifier, saturation points, effects with chances, meat /
//                      always-eat / fast flags, eat or drink animation and time, what it leaves behind,
//                      item class chain, tags, and its Spice of Life complexity
//   food_blocks.json   placed food: cakes, pies, feasts (the block item is not edible itself)
//   drinks.json        drinkable items with no food value (potions, milk, some mod drinks)
//   recipes.json       every recipe with at least one item output: type, raw ingredient JSON (keeps
//                      tags), item and fluid outputs with chances, fluid inputs, heat, cook time
//
// tools/food_catalog.py reads all of it and builds feature_writeup/food_catalog.md.
// Everything that touches a mod class goes through guardThrowable (java_guard.js): a java.lang.Error
// from one odd item or recipe would otherwise end the command silently.

ServerEvents.commandRegistry(event => {
	var { commands: Commands } = event
	event.register(
		Commands.literal('dumpfood')
			.requires(src => src.hasPermission(2))
			.executes(ctx => {
				console.log('[dumpfood] started')
				var g = guardThrowable(() => dumpFood())
				if (g.ok) {
					var r = g.value
					ctx.source.sendSuccess(Text.green(`Dumped ${r.foods} foods, ${r.blocks} food blocks, ${r.drinks} drinks, `
						+ `${r.recipes} recipes to kubejs/exported/food/`), false)
				} else {
					console.error('[dumpfood] failed: ' + g.error)
					ctx.source.sendFailure(Text.red('dumpfood failed - see log: ' + g.error))
				}
				return 1
			})
	)
})

function dumpFood() {
	var $Reg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var $BlockItem = Java.loadClass('net.minecraft.world.item.BlockItem')
	var $Hooks = Java.loadClass('net.minecraftforge.server.ServerLifecycleHooks')

	var round = (n, d) => { var p = Math.pow(10, d == null ? 3 : d); return Math.round(n * p) / p }
	var keyOf = (reg, obj) => { try { var k = reg.getKey(obj); return k ? String(k) : null } catch (e) { return null } }
	var each = (javaIterable, fn) => { var it = javaIterable.iterator(); while (it.hasNext()) fn(it.next()) }
	var classChain = obj => {
		var out = []
		try {
			var c = obj.getClass()
			while (c && out.length < 8) {
				var n = String(c.getName())
				if (n === 'java.lang.Object') break
				out.push(n)
				c = c.getSuperclass()
			}
		} catch (e) {}
		return out
	}

	// Spice of Life: Sweet Potato (the jar is "Apple Pie Edition"). Complexity is what one eat adds to
	// food diversity before the age penalty: the config map value if listed, else its own formula.
	var sol = null
	try {
		sol = {
			FoodList: Java.loadClass('com.tarinoita.solsweetpotato.tracking.FoodList'),
			FoodInstance: Java.loadClass('com.tarinoita.solsweetpotato.tracking.FoodInstance'),
			ConfigHandler: Java.loadClass('com.tarinoita.solsweetpotato.ConfigHandler'),
		}
	} catch (e) { console.warn('[dumpfood] Spice of Life classes not reachable: ' + e) }
	var solOf = item => {
		if (!sol) return null
		var g = guardThrowable(() => {
			var fi = new sol.FoodInstance(item)
			return { complexity: round(sol.FoodList.getComplexity(fi), 4), fromConfig: !!sol.ConfigHandler.complexityMap.containsKey(fi) }
		})
		return g.ok ? g.value : null
	}

	// item -> tags, from the live tag manager (ItemStack#getTags is shadowed by KubeJS here)
	var tagsByItem = {}
	guardThrowable(() => {
		var tm = $Reg.ITEMS.tags()
		each(tm.getTagNames(), key => {
			var tagId = String(key.location())
			each(tm.getTag(key), it => {
				var iid = keyOf($Reg.ITEMS, it)
				if (iid) (tagsByItem[iid] = tagsByItem[iid] || []).push(tagId)
			})
		})
	})

	var effectsOf = props => {
		var out = []
		try {
			each(props.getEffects(), pair => {
				var inst = pair.getFirst()
				out.push({
					effect: keyOf($Reg.MOB_EFFECTS, inst.getEffect()),
					amplifier: inst.getAmplifier(),
					seconds: round(inst.getDuration() / 20, 2),
					chance: round(pair.getSecond(), 3),
				})
			})
		} catch (e) {}
		return out
	}

	var propsRow = props => ({
		nutrition: props.getNutrition(),
		saturationModifier: round(props.getSaturationModifier(), 4),
		saturation: round(props.getNutrition() * props.getSaturationModifier() * 2, 2),
		meat: !!props.isMeat(),
		alwaysEat: !!props.canAlwaysEat(),
		fast: !!props.isFastFood(),
		effects: effectsOf(props),
	})

	var remainderOf = (item, stack) => {
		var g = guardThrowable(() => {
			var r = item.getCraftingRemainingItem(stack)
			return (r && !r.isEmpty()) ? keyOf($Reg.ITEMS, r.getItem()) : null
		})
		if (g.ok && g.value) return g.value
		g = guardThrowable(() => { var r = item.getCraftingRemainingItem(); return r ? keyOf($Reg.ITEMS, r) : null })
		return (g.ok && g.value && g.value !== 'minecraft:air') ? g.value : null
	}

	var foods = [], blocks = [], drinks = []
	var FOOD_BLOCK = /(Cake|Feast|Pie|Pizza|Pudding|Sandwich|Roast|Platter|Food)/

	var keys = []
	$Reg.ITEMS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0)

	keys.forEach(rl => {
		var id = String(rl)
		if (id === 'minecraft:air') return
		var g = guardThrowable(() => {
			var item = $Reg.ITEMS.getValue(rl)
			var stack = Item.of(id)
			var mod = id.substring(0, id.indexOf(':'))
			var name = id
			try { name = String(stack.getHoverName().getString()) } catch (e) {}
			var useAnim = null, useTicks = 0
			try { useAnim = String(item.getUseAnimation(stack)) } catch (e) {}
			try { useTicks = item.getUseDuration(stack) } catch (e) {}

			var edible = false
			try { edible = !!item.isEdible() } catch (e) {}

			if (edible) {
				var props = item.getFoodProperties()
				var row = { id: id, mod: mod, name: name }
				var base = propsRow(props)
				Object.keys(base).forEach(k => { row[k] = base[k] })
				// Forge's stack-aware hook: a few items override it and differ from the plain properties
				var aware = guardThrowable(() => item.getFoodProperties(stack, null))
				if (aware.ok && aware.value && aware.value !== props) {
					var a = propsRow(aware.value)
					if (a.nutrition !== row.nutrition || a.saturationModifier !== row.saturationModifier) row.stackAware = a
				}
				row.useAnim = useAnim
				row.useTicks = useTicks
				row.stackSize = stack.getMaxStackSize()
				row.remainder = remainderOf(item, stack)
				row.classes = classChain(item)
				if (item instanceof $BlockItem) row.blockClasses = classChain(item.getBlock())
				row.tags = (tagsByItem[id] || []).sort()
				row.sol = solOf(item)
				foods.push(row)
				return
			}

			if (item instanceof $BlockItem) {
				var block = item.getBlock()
				var chain = classChain(block)
				if (chain.some(c => FOOD_BLOCK.test(c.substring(c.lastIndexOf('.') + 1)))) {
					var b = { id: id, mod: mod, name: name, blockClasses: chain, tags: (tagsByItem[id] || []).sort() }
					var state = null
					try { state = block.defaultBlockState() } catch (e) {}
					var serving = guardThrowable(() => block.getServingItem(state))           // FD FeastBlock
					if (serving.ok && serving.value && !serving.value.isEmpty()) b.serving = keyOf($Reg.ITEMS, serving.value.getItem())
					var slice = guardThrowable(() => block.getPieSliceItem())                  // FD PieBlock
					if (slice.ok && slice.value && !slice.value.isEmpty()) b.slice = keyOf($Reg.ITEMS, slice.value.getItem())
					var maxServ = guardThrowable(() => block.getMaxServings())
					if (maxServ.ok && maxServ.value != null) b.servings = maxServ.value
					blocks.push(b)
				}
				return
			}

			if (useAnim === 'DRINK') {
				drinks.push({ id: id, mod: mod, name: name, useTicks: useTicks, stackSize: stack.getMaxStackSize(),
					remainder: remainderOf(item, stack), classes: classChain(item), tags: (tagsByItem[id] || []).sort() })
			}
		})
		if (!g.ok) console.warn(`[dumpfood] skipped ${id}: ${g.error}`)
	})
	console.log(`[dumpfood] ${foods.length} foods, ${blocks.length} food blocks, ${drinks.length} drinks`)

	// ── recipes ───────────────────────────────────────────────────────────────
	var server = $Hooks.getCurrentServer()
	var manager = server ? server.getRecipeManager() : null
	var recipes = []
	var failed = 0
	var stackRow = (st, chance) => {
		var o = { id: keyOf($Reg.ITEMS, st.getItem()), n: st.getCount() }
		if (chance != null && chance < 1) o.p = round(chance, 3)
		return o
	}
	var fluidRow = fs => ({ f: keyOf($Reg.FLUIDS, fs.getFluid()), mb: fs.getAmount() })

	if (manager) {
		var rit = manager.getRecipeIds().iterator()
		while (rit.hasNext()) {
			let rid = rit.next()
			let recipe = manager.byKey(rid).orElse(null)
			if (!recipe) continue
			let rg = guardThrowable(() => {
				var row = { id: String(rid), type: String(recipe.getType()), in: [], out: [] }
				// item outputs: Create / FD chance lists first, then the vanilla single result
				var rolled = guardThrowable(() => recipe.getRollableResults())
				if (rolled.ok && rolled.value) each(rolled.value, o => { if (!o.getStack().isEmpty()) row.out.push(stackRow(o.getStack(), o.getChance())) })
				if (!row.out.length) {
					var res = guardThrowable(() => recipe.getResultItem())
					if (res.ok && res.value && !res.value.isEmpty()) row.out.push(stackRow(res.value, null))
				}
				var fres = guardThrowable(() => recipe.getFluidResults())
				if (fres.ok && fres.value) { row.fout = []; each(fres.value, fs => { if (!fs.isEmpty()) row.fout.push(fluidRow(fs)) }) }
				if (!row.out.length && !(row.fout && row.fout.length)) return null

				var ings = guardThrowable(() => recipe.getIngredients())
				if (ings.ok && ings.value) each(ings.value, ing => {
					var blank = false
					try { blank = !!ing.isEmpty() } catch (e) {}
					if (blank) return
					var j = guardThrowable(() => String(ing.toJson()))
					row.in.push(j.ok ? j.value : '?')
				})
				var fin = guardThrowable(() => recipe.getFluidIngredients())
				if (fin.ok && fin.value) {
					row.fin = []
					each(fin.value, fi => {
						var alts = []
						try { each(fi.getMatchingFluidStacks(), fs => { var k = keyOf($Reg.FLUIDS, fs.getFluid()); if (k && alts.indexOf(k) < 0) alts.push(k) }) } catch (e) {}
						var mb = 0
						try { mb = fi.getRequiredAmount() } catch (e) {}
						row.fin.push({ f: alts.slice(0, 6), mb: mb })
					})
					if (!row.fin.length) delete row.fin
				}
				if (row.fout && !row.fout.length) delete row.fout
				var heat = guardThrowable(() => String(recipe.getRequiredHeat()))
				if (heat.ok && heat.value && heat.value !== 'NONE') row.heat = heat.value
				var time = guardThrowable(() => recipe.getCookingTime())
				if (time.ok && time.value) row.ticks = time.value
				var cont = guardThrowable(() => recipe.getOutputContainer())
				if (cont.ok && cont.value && !cont.value.isEmpty()) row.container = keyOf($Reg.ITEMS, cont.value.getItem())
				return row
			})
			if (rg.ok) { if (rg.value) recipes.push(rg.value) }
			else failed++
		}
	}
	recipes.sort((a, b) => a.id < b.id ? -1 : a.id > b.id ? 1 : 0)
	console.log(`[dumpfood] ${recipes.length} recipes with an item or fluid output, ${failed} unreadable`)

	writeExportJson('kubejs/exported/food/foods.json', { count: foods.length, foods: foods })
	writeExportJson('kubejs/exported/food/food_blocks.json', { count: blocks.length, blocks: blocks })
	writeExportJson('kubejs/exported/food/drinks.json', { count: drinks.length, drinks: drinks })
	writeExportJson('kubejs/exported/food/recipes.json', { count: recipes.length, unreadable: failed, recipes: recipes })
	guardThrowable(() => dumpTags(['item']))

	return { foods: foods.length, blocks: blocks.length, drinks: drinks.length, recipes: recipes.length }
}
