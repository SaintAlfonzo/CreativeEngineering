// requires: creativedelight
// Smoothies (TODO sweets #8, feature_writeup/smoothies.md section 2): the grid recipe of each, and for every smoothie
// but Mixed Berry its fluid's routes: Create mixing of the same fruits -> 250 mB (one bottle), Spout and Item Drain
// (R.createBottle). Items, fluids and the table (global.smoothies): CD startup creativedelight/smoothies.js. Tags:
// creativedelight/smoothie_tags.js. A smoothie whose fruit mods are absent gets no recipe.
//
// Mixed Berry: 2 red berries + 1 blue or purple berry. The result stores the effects of the berries used
// (global.smoothies.berryEffects: each berry at half its base length, the same effect twice adds up) as
// "effect id|ticks|amplifier" entries joined with ';' under SmoothieEffects, so different mixes do not stack together.
ServerEvents.recipes(e => {
	if (!global.CD || !global.smoothies) return
	var i = global.item
	var f = global.fluid
	var S = global.smoothies
	var R = global.NCH.forPack('creativedelight')

	// the stored effects of one Mixed Berry craft, in the order the berries add them
	var berryNbt = stacks => {
		var order = []
		var sum = {}
		stacks.forEach(st => {
			;(S.berryEffects[String(st.getItem().getId())] || []).forEach(x => {
				var id = String(x[0])
				if (!S.effectOn(id)) return
				if (!sum[id]) { sum[id] = { ticks: 0, amp: x[2] }; order.push(id) }
				sum[id].ticks += x[1] * 20
			})
		})
		return order.map(id => id + '|' + sum[id].ticks + '|' + sum[id].amp).join(';')
	}
	var list = (javaList) => {
		var out = []
		for (var n = 0; n < javaList.size(); n++) out.push(javaList.get(n))
		return out
	}

	global.CD.recipes(() => {
		S.list.forEach(s => {
			if (!s.mods.every(m => Platform.isLoaded(m))) return
			var id = s.key.replace(/_smoothie$/, '')
			var fruits = s.b ? [s.a, s.a, s.b] : [s.a, s.a, s.a]
			var recipe = R.shapeless(i[s.key], fruits.concat([i.glass_bottle]), e, 'smoothie/' + id)
			if (s.special === 'berries') {
				recipe.modifyResult((grid, result) => {
					var berries = list(grid.findAll(Ingredient.of(i.t_berries_red)))
						.concat(list(grid.findAll(Ingredient.of(i.t_berries_blue_or_purple))))
					var out = result.copy()
					out.getOrCreateTag().putString(S.nbtKey, berryNbt(berries))
					return out
				})
				return
			}
			var fluid = f[s.key]
			R.createMixing(Fluid.of(fluid, 250), fruits, e, 'smoothie/' + id)
			R.createBottle(i[s.key], fluid, e, 'smoothie/' + id)
		})
	})
})
