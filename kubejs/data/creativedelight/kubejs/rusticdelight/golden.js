// requires: creativedelight
// requires: rusticdelight
// Rustic Delight's golden food on the golden family rule (CD.food goldenGild, creative_delight.md 30; TODO
// creativedelight/framework #12): Golden Coffee Beans, a coffee bean (raw or roasted, as upstream's grid recipe) in a
// ring of 8 gold nuggets (any gold nugget; was the vanilla nugget), plus a heated mixer route (6 nuggets) and an Arc
// Furnace route (5 nuggets). goldenGild replaces upstream's shaped recipe. Creative Delight member script: recipe ids
// are creativedelight:<type>/<suffix>.
ServerEvents.recipes(e => {
	if (!global.CD || !global.CD.on('rusticdelight')) return

	var i = global.item
	var F = global.CD.food(e)

	global.CD.recipes(() => {
		F.goldenGild(i.t_coffee_ingredients, i.golden_coffee_beans, i.any_nugget_gold)
	})
})
