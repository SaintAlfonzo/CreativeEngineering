// priority: 999
// Creative Delight food family crafts: global.CD.food(e) (food_families.md 3.1, creative_delight.md 30). One call per
// food builds every recipe its family's rule asks for, so a rule change here reaches every member. Built on the
// global.NCH station helpers; call it inside global.CD.recipes so the ids are creative_delight:<type>/<suffix>.
//
//   var F = global.CD.food(e)
//   F.veggieSlice(i.cabbage, i.cabbage_leaf, 3)      every call is (input, output, ...): the input first
//
// A family call is authoritative for its food: before adding, it removes the recipes of the same stations that make
// that output from that input (cooking trio, cutting board), or every other recipe of the output (cakes, pies,
// cookies; muffins their pot recipes; tarts keep theirs), so calling it on a mod's food replaces the mod's own
// version instead of adding a duplicate. Recipes the pack and CD add themselves are never removed.
//
// Families and their calls:
//   cutting board   fruitSlice, veggieSlice, meatCut, fishSlice (input, output, count): knife, FAMILY count unless given
//                   pieSlice (whole, slice, count): board cut plus slices -> whole by hand (`<whole>_from_slices`)
//   cooking trio    fruitCook, veggieCook, meatCook, fishCook, eggCook (input, output): furnace, smoker and campfire
//   oven            cakeBake (flavour, cake)          baked base + frosting + 2 flavour by hand; raw base in the oven
//                   pieBake (flavour, pie, can)       crust + 2 flavour + 2 sweet; with a filling can key also the pot
//                                                     filling and crust + full can
//                   cookieBake (flavour, cookie, second)  cookie dough + 2 flavour (or flavour + second) -> 12
//                   muffinBake (fruit, muffin)        oven 4 on paper; Cooking Pot 2 on dough
//                   tartBake (fruit, tart)            crust + fruit + egg + sugar + milk -> 2, baking sheet
//   heatless grid   mealCraft (inputs, meal)          a pot meal's grid route from cooked forms (`_cooked_manual_only`)
//                   soupCraft (inputs, soup)          the same with the bone broth added (it brings the bowl)
//                   saladCraft (inputs, salad)        raw parts with the bowl added (`_manual_only`)
//   hand food       wrapAssemble (fillings, wrap)     tortilla + fillings by hand; assembly on a tortilla, fillings in order
//                   burgerAssemble / skewerAssemble / sandwichAssemble (pattern, keys, food, steps, base): shaped by hand
//                   (`_manual_only`) plus one assembly on base (bread, stick, bread unless given)
//                   assemble (base, steps, food, loops): the sequenced assembly alone. A step is an ingredient
//                   (deploy), { fill: Fluid.of(...) } (spout) or { press: true } (press)
//   golden          goldenGild (food, golden, gold)   food in a ring of 8 gold; heated mixer + 6; Arc Furnace + 5
//                   goldenEnchant (golden, enchanted) assembly: gold block, 250 mB liquid XP, press, 4 loops
//   oven parts      F.slot(id, count), F.ware.sheet / pie / cake / muffin, F.SWEET for one-off oven recipes

global.CD.FOOD = {
	// Cutting board: pieces per whole when the call gives no count
	slice: { fruit: 4, veggie: 2, meat: 2, fish: 6, pie: 4 },
	// Fish slices also give a bone meal
	fishBone: true,
	// Cooking trio per family: xp, furnace / smoker / campfire ticks (vanilla's cooked food)
	cook: {
		fruit: { xp: 0.35, furnace: 200, smoker: 100, campfire: 600 },
		veggie: { xp: 0.35, furnace: 200, smoker: 100, campfire: 600 },
		meat: { xp: 0.35, furnace: 200, smoker: 100, campfire: 600 },
		fish: { xp: 0.35, furnace: 200, smoker: 100, campfire: 600 },
		egg: { xp: 0.35, furnace: 200, smoker: 100, campfire: 600 },
	},
	// Oven: xp and ticks for every baked family; output counts
	bake: { exp: 0.1, time: 200 },
	cookies: 12, muffinsOven: 4, muffinsPot: 2, tarts: 2,
	// Golden food: gold in the heated mixer and the Arc Furnace (the grid is a ring of 8)
	gold: { mixer: 6, arc: 5 },
}

global.CD.food = function (e) {
	var R = global.NCH
	var i = global.item
	var V = global.CD.FOOD
	var n = (id, count) => ({ id: id, count: count })
	var ware = {
		sheet: { exp: V.bake.exp, time: V.bake.time, tool: i.t_bakeware_baking_sheet },
		pie: { exp: V.bake.exp, time: V.bake.time, tool: i.t_bakeware_pie_tin },
		cake: { exp: V.bake.exp, time: V.bake.time, tool: i.t_bakeware_cake_pan },
		muffin: { exp: V.bake.exp, time: V.bake.time, tool: i.t_bakeware_muffin_tin, container: i.paper },
	}
	var SWEET = i.any_sweetener   // one sweet slot: any sweetener (sugar, powdered sugar, glycerol, honey bottle, honeycomb)
	// Recipes the pack and CD add themselves are never swept by a family's removal
	var OURS = [{ mod: 'creativeengineering' }, { mod: 'creative_delight' }]

	var idOf = v => String(R._resolveIdCount(v).id || '').replace(/^#/, '')
	var pathOf = v => { var id = idOf(v); return id.indexOf(':') >= 0 ? id.split(':')[1] : id }
	// Id suffix from the output's path; a second food with the same path (another mod's) gets its namespace in front
	// (kept across reloads: the same path maps to the same id every time)
	var suffix = (output, tail) => {
		if (!global.CD._foodIds) global.CD._foodIds = {}
		var id = idOf(output), s = pathOf(output) + tail
		var seen = global.CD._foodIds[s]
		if (seen && seen !== id) s = id.split(':')[0] + '_' + s
		global.CD._foodIds[s] = id
		return s
	}
	// Incomplete item key: the global.item key whose id is the output's (transition_items.js keys by it)
	var incKey = output => {
		var id = idOf(output), path = pathOf(output)
		var tries = [path, path.replace(/_block$/, '')]
		for (var t = 0; t < tries.length; t++) if (R.INCOMPLETE[tries[t]]) return tries[t]
		for (var k in R.INCOMPLETE) if (String(i[k]) === id) return k
		return path
	}

	var F = {
		slot: n, ware: ware, SWEET: SWEET, FAMILY: V,

		//######################### Cutting board #########################
		_board(family, input, output, count, extra) {
			var outs = [Item.of(output, count == null ? V.slice[family] : count)].concat(extra || [])
			R.remove({ type: 'farmersdelight:cutting', input: input, output: output }, e)
			R.fdCutting(input, i.any_knife, outs, e, suffix(output, '_from_' + pathOf(input)))
		},
		fruitSlice(input, output, count) { F._board('fruit', input, output, count) },
		veggieSlice(input, output, count) { F._board('veggie', input, output, count) },
		meatCut(input, output, count) { F._board('meat', input, output, count) },
		fishSlice(input, output, count) { F._board('fish', input, output, count, V.fishBone ? [i.bonemeal] : []) },
		pieSlice(whole, slice, count) {
			var c = count == null ? V.slice.pie : count
			F._board('pie', whole, slice, c)
			R.remove({ type: 'minecraft:crafting_shapeless', input: slice, output: whole, not: OURS }, e)
			var slices = []
			for (var s = 0; s < c; s++) slices.push(slice)
			R.shapeless(whole, slices, e, suffix(whole, '_from_slices'))
		},

		//######################### Cooking trio #########################
		_cook(family, input, output) {
			var c = V.cook[family]
			;['minecraft:smelting', 'minecraft:smoking', 'minecraft:campfire_cooking'].forEach(t =>
				R.remove({ type: t, input: input, output: output, not: OURS }, e))
			var s = suffix(output, '_from_' + pathOf(input))
			R.smelting(output, input, e, s, c.xp, c.furnace)
			R.smoking(output, input, e, s, c.xp, c.smoker)
			R.campfireCooking(output, input, e, s, c.xp, c.campfire)
		},
		fruitCook(input, output) { F._cook('fruit', input, output) },
		veggieCook(input, output) { F._cook('veggie', input, output) },
		meatCook(input, output) { F._cook('meat', input, output) },
		fishCook(input, output) { F._cook('fish', input, output) },
		eggCook(input, output) { F._cook('egg', input, output) },

		//######################### Oven #########################
		// Every other recipe of the output goes; slices -> whole stays
		_sweep(output) { R.remove({ output: output, not: [{ id: /_from_slices$/ }].concat(OURS) }, e) },
		cakeBake(flavour, output) {
			F._sweep(output)
			var s = suffix(output, '')
			R.shapeless(output, [i.cake_base_baked, i.frosting, Item.of(flavour, 2)], e, s + '_manual_only')
			R.ceBaking([i.cake_base, i.frosting, n(flavour, 2)], Item.of(output, 1), e, s, ware.cake)
		},
		// can: the filling can's global.item key (its can json's "pie"), or nothing for a pie with no can
		pieBake(flavour, output, can) {
			F._sweep(output)
			var s = suffix(output, '')
			R.ceBaking([i.pie_crust, n(flavour, 2), SWEET, SWEET], Item.of(output, 1), e, s, ware.pie)
			if (!can) return
			R.fdCooking([flavour, flavour, SWEET, SWEET], Item.of(i[can], 1), e, can + '_pot', V.bake.exp, V.bake.time, i.empty_can)
			R.ceBaking([i.pie_crust, i[can]], Item.of(output, 1), e, s + '_from_filling', ware.pie)
		},
		cookieBake(flavour, output, second) {
			F._sweep(output)
			var flavours = second ? [flavour, second] : [n(flavour, 2)]
			R.ceBaking([i.cookie_dough].concat(flavours), Item.of(output, V.cookies), e, suffix(output, ''), ware.sheet)
		},
		// The pot route replaces the mod's own (#forge:dough, not only wheat dough)
		muffinBake(fruit, output) {
			var s = suffix(output, '')
			R.remove({ type: 'farmersdelight:cooking', output: output, not: OURS }, e)
			R.ceBaking([n(fruit, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sweetener], Item.of(output, V.muffinsOven), e, s, ware.muffin)
			R.fdCooking([fruit, fruit, i.any_dough, i.t_milk_bottle, i.eggs, i.any_sweetener], Item.of(output, V.muffinsPot), e, s,
				V.bake.exp, V.bake.time)
		},
		tartBake(fruit, output) {
			R.ceBaking([fruit, i.pie_crust, i.eggs, i.any_sweetener, i.t_milk_bottle], Item.of(output, V.tarts), e,
				suffix(output, ''), ware.sheet)
		},

		//######################### Heatless grid meals #########################
		// Heatless meal rule (farmersdelight/recipes.js "Meals"): the grid route from cooked forms, at least one more
		// main ingredient than the pot. _manual_only keeps Create's basin and mechanical crafter off it.
		mealCraft(inputs, output) { R.shapeless(output, inputs, e, suffix(output, '_cooked_manual_only')) },
		soupCraft(inputs, output) { R.shapeless(output, [i.bone_broth].concat(inputs), e, suffix(output, '_cooked_manual_only')) },
		saladCraft(inputs, output) { R.shapeless(output, inputs.concat([i.bowl]), e, suffix(output, '_manual_only')) },

		//######################### Hand food #########################
		assemble(base, steps, output, loops) {
			if (!Platform.isLoaded('create')) return
			var key = incKey(output)
			var inc = R.incomplete(key)
			var seq = steps.map(s => {
				if (s && s.press) return e.recipes.create.pressing(inc, inc)
				if (s && s.fill) return e.recipes.create.filling(inc, [inc, s.fill])
				return e.recipes.create.deploying(inc, [inc, Ingredient.of(s)])
			})
			R.createSequencedAssembly(output, base, seq, [key], e, suffix(output, '_sequenced_assembly'), loops || 1)
		},
		_shapedAssemble(pattern, keys, output, steps, base) {
			R.shaped(output, pattern, keys, e, suffix(output, '_manual_only'))
			F.assemble(base, steps, output)
		},
		burgerAssemble(pattern, keys, output, steps, base) { F._shapedAssemble(pattern, keys, output, steps, base || i.t_bread) },
		skewerAssemble(pattern, keys, output, steps, base) { F._shapedAssemble(pattern, keys, output, steps, base || i.stick) },
		sandwichAssemble(pattern, keys, output, steps, base) { F._shapedAssemble(pattern, keys, output, steps, base || i.t_bread) },
		wrapAssemble(fillings, output) {
			R.shapeless(output, [i.t_tortilla].concat(fillings), e, suffix(output, '_manual_only'))
			F.assemble(i.t_tortilla, fillings, output)
		},

		//######################### Golden #########################
		// gold: the gold item or tag for this food (nuggets for a carrot, ingots for an apple)
		goldenGild(input, output, gold) {
			var s = pathOf(output)
			R.remove({ type: 'minecraft:crafting_shaped', output: output, not: OURS }, e)
			R.shaped(output, ['NNN', 'NCN', 'NNN'], { C: input, N: gold }, e, s)
			if (Platform.isLoaded('create'))
				R.createMixing(output, [input, Ingredient.of([gold]).withCount(V.gold.mixer)], e, s + '_gilding', 'heated')
			if (Platform.isLoaded('immersiveengineering')) {
				var add = []
				for (var g = 0; g < V.gold.arc; g++) add.push(gold)
				R.ieArcFurnace(output, input, e, s + '_arc_gilding', [], add)
			}
		},
		goldenEnchant(input, output) {
			F.assemble(input, [i.any_block_gold, { fill: Fluid.of(global.fluid.xp, 250) }, { press: true }], output, 4)
		},
	}
	return F
}
