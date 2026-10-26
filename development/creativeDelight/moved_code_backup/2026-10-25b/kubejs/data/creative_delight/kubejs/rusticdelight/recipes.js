// requires: rusticdelight
// Rustic Delight 1.7.0 recipe changes (feature_writeup/rustic_delight.md 5.2). Creative Delight member script: recipe
// ids are creative_delight:<type>/<suffix> (global.CD.recipes). The port's data is upstream's 1.7.0 data; each change
// removes the upstream recipe and adds the pack recipe here.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return

	var i = global.item
	var R = global.NCH
	var n = (id, count) => ({ id: id, count: count })
	// as cooking/baking.js
	var SHEET = { exp: 0.1, time: 200, tool: i.t_bakeware_baking_sheet }
	var PIE = { exp: 0.1, time: 200, tool: i.t_bakeware_pie_tin }
	var SWEET = [i.any_sugar, i.honey_bottle]

	global.CD.recipes(() => {

		//######################### Baking (oven.md 2.6) #########################
		// The new cookies and cheesecakes join the pack's baking overhaul: upstream's grid recipes out, oven only.
		// Cookies: cookie dough + 2 flavour -> 12. Maple Cookie (Abnormals Delight, its Autumnity syrup is not in the
		// pack) takes one syrup and one honey, so it does not clash with the Syrup Cookie.
		var COOKIES = [
			[i.syrup_cookie, [n(i.syrup, 2)]],
			[i.coffee_cookie, [n(i.roasted_coffee_beans, 2)]],
			[i.cherry_blossom_cookie, [n(i.t_cherry_blossom, 2)]],
			[i.maple_cookie, [i.syrup, i.honey_bottle]],
		]
		COOKIES.forEach(c => {
			R.remove({ output: c[0], not: { mod: 'creativeengineering' } }, e)
			R.ceBaking([i.cookie_dough].concat(c[1]), Item.of(c[0], 12), e, c[0].split(':')[1], SHEET)
		})
		// Cheesecakes are pies here (as Sweet Berry Cheesecake): pie crust + 2 flavour + 2 sweet. Slices back to a
		// whole cheesecake stay.
		var CHEESECAKES = [
			[i.syrup_cheesecake, i.syrup],
			[i.coffee_cheesecake, i.roasted_coffee_beans],
			[i.cherry_blossom_cheesecake, i.t_cherry_blossom],
		]
		CHEESECAKES.forEach(c => {
			R.remove({ output: c[0], not: [{ id: /_from_slices$/ }, { mod: 'creativeengineering' }] }, e)
			R.ceBaking([i.pie_crust, n(c[1], 2), SWEET, SWEET], Item.of(c[0], 1), e, c[0].split(':')[1], PIE)
		})

		//######################### Maple foods #########################
		// Abnormals Delight's maple foods stay; Rustic's syrup stands in for Autumnity's (owner 2026-10-25).
		R.shapeless(i.maple_glazed_bacon, [i.cooked_bacon, i.syrup], e, 'maple_glazed_bacon')
	})
})
