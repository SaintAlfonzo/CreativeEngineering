// Food family recipe helpers: one call per recipe, the family's rule written once here (feature_writeup/food_families.md
// 3.1, "Recipe shape by station"). Member scripts say WHAT (input, output); the family says HOW (station, tool, xp, time,
// counts, bakeware). Changing a rule is a change in this file only.
//
//   ServerEvents.recipes(e => {
//     var F = global.foodFamily(e)             // inside global.CD.recipes(() => { ... }) so the ids are creative_delight:
//     F.veggieSlice(i.pepper, Item.of(i.pepper_slice))    // count from the rule (4 slices)
//     F.veggieSlice(i.zucchini, i.zucchini_slice, 6)      // count override
//     F.meatCook(i.duck_fillet, i.cooked_duck_fillet, 'cooked_duck_fillet')
//     F.pieBake(i.t_fruits_apple, 'apple_pie')            // result key in global.item, as the cake / pie tables
//   })
//
// A cook call takes `only` (which of 'furnace', 'smoker', 'campfire' to add) and `xpOverride` after the id.
// Every helper ends in an optional recipe id suffix (idOverride), as the NCH helpers; leave it out for a generated one.
// Output: an item id / key string gives `count` of it; an Item.of(...) or an array is used as written (a cut that also
// drops bonemeal: [Item.of(i.cod_slice, 6), i.bonemeal]).
global.FOOD_FAMILY = {
	// Cutting Board (knife): whole -> pieces. Count per cut when the caller gives none.
	cut: {
		fruitSlice: { count: 4 },
		veggieSlice: { count: 4 },
		fishSlice: { count: 6 },
		meatCut: { count: 2 },
		pieSlice: { count: 4 },    // whole pie / cake / bake block -> slices
		cakeSlice: { count: 4 },
	},
	// Furnace + Smoker + Campfire (fruit/cooked, veggie/cooked, meat/cooked, fish/cooked, egg/cooked): xp per cook;
	// furnace 200 ticks, smoker 100, campfire 600.
	cook: {
		fruitCook: { xp: 0.1 },
		veggieCook: { xp: 0.35 },
		meatCook: { xp: 0.35 },
		fishCook: { xp: 0.35 },
		eggCook: { xp: 0.35 },
	},
	cookTime: { furnace: 200, smoker: 100, campfire: 600 },
	// Oven bakeware (never used up); same exp / time for every bake.
	bake: { exp: 0.1, time: 200 },
	cookieCount: 12,   // cookie dough + 2 flavour -> 12
	muffinCount: 4,    // oven muffins (the pot makes 2)
	tartCount: 2,
}

global.foodFamily = function (e) {
	if (!e) throw new Error('foodFamily called without event object!')
	var R = global.NCH
	var ff = global.FOOD_FAMILY
	var i = global.item
	var n = (id, count) => ({ id: id, count: count })
	var out = (output, count) => (typeof output === 'string' ? Item.of(output, count) : output)
	var tool = key => ({ exp: ff.bake.exp, time: ff.bake.time, tool: i[key] })
	var item = key => i[key] ?? key   // a global.item key, or a full item id
	var SWEET = () => [i.any_sugar, i.honey_bottle]   // one pie sugar slot

	var F = {}

	// ---- cutting board ----
	Object.keys(ff.cut).forEach(name => {
		F[name] = (input, output, countOverride, idOverride) =>
			R.fdCutting(input, i.any_knife, out(output, countOverride ?? ff.cut[name].count), e, idOverride)
	})

	// ---- cooking trio ----
	// `only`: restrict to some of 'furnace', 'smoker', 'campfire' when the mod already ships the others.
	Object.keys(ff.cook).forEach(name => {
		F[name] = (input, output, idOverride, only, xpOverride) => {
			var xp = xpOverride ?? ff.cook[name].xp
			var t = ff.cookTime
			var want = s => !only || only.indexOf(s) >= 0
			if (want('furnace')) R.smelting(output, input, e, idOverride, xp, t.furnace)
			if (want('smoker')) R.smoking(output, input, e, idOverride, xp, t.smoker)
			if (want('campfire')) R.campfireCooking(output, input, e, idOverride, xp, t.campfire)
		}
	})

	// ---- oven: pies, cakes, cookies, muffins, tarts ----
	// Result: a global.item key or a full item id. Flavour is an item or tag (2 are used).
	// Pie: crust + 2 fruit + 2 sweet in the pie tin. `fillingKey`: the pie's filling can, which also bakes with a crust
	// and is made in the pot (fruit + sugar, empty can in the container slot).
	F.pieBake = (flavour, pieKey, fillingKey, idOverride) => {
		var id = idOverride ?? pieKey
		R.ceBaking([i.pie_crust, n(flavour, 2), SWEET(), SWEET()], Item.of(item(pieKey), 1), e, id, tool('t_bakeware_pie_tin'))
		if (!fillingKey) return
		R.fdCooking([flavour, flavour, SWEET(), SWEET()], Item.of(item(fillingKey), 1), e, fillingKey + '_pot', ff.bake.exp, ff.bake.time, i.empty_can)
		R.ceBaking([i.pie_crust, item(fillingKey)], Item.of(item(pieKey), 1), e, id + '_from_filling', tool('t_bakeware_pie_tin'))
	}
	// Cake: baked base + frosting + 2 flavour by hand, or raw base + frosting + 2 flavour in the cake pan.
	F.cakeBake = (flavour, cakeKey, idOverride) => {
		var id = idOverride ?? cakeKey
		R.shapeless(item(cakeKey), [i.cake_base_baked, i.frosting, Item.of(flavour, 2)], e, id + '_manual_only')
		R.ceBaking([i.cake_base, i.frosting, n(flavour, 2)], Item.of(item(cakeKey), 1), e, id, tool('t_bakeware_cake_pan'))
	}
	// Cookie: cookie dough + 2 flavour -> 12, oven only. `flavour`: one item or tag (2 are used), or an array of oven
	// slots as written (e.g. [i.syrup, i.honey_bottle]).
	F.cookieBake = (flavour, cookieKey, countOverride, idOverride) => {
		var slots = Array.isArray(flavour) ? flavour : [n(flavour, 2)]
		R.ceBaking([i.cookie_dough].concat(slots), Item.of(item(cookieKey), countOverride ?? ff.cookieCount), e, idOverride ?? cookieKey,
			tool('t_bakeware_baking_sheet'))
	}
	// Muffin: 2 berry + flour + milk + egg + sugar, muffin tin with paper.
	F.muffinBake = (berry, muffinKey, countOverride, idOverride) => {
		var opts = tool('t_bakeware_muffin_tin')
		opts.container = i.paper
		R.ceBaking([n(berry, 2), i.flour, i.t_milk_bottle, i.eggs, i.any_sugar], Item.of(item(muffinKey), countOverride ?? ff.muffinCount), e,
			idOverride ?? muffinKey, opts)
	}
	// Tart: fruit + crust + egg + sugar + milk, baking sheet.
	F.tartBake = (fruit, tartKey, countOverride, idOverride) => {
		R.ceBaking([fruit, i.pie_crust, i.eggs, i.any_sugar, i.t_milk_bottle], Item.of(item(tartKey), countOverride ?? ff.tartCount), e,
			idOverride ?? tartKey, tool('t_bakeware_baking_sheet'))
	}

	return F
}
