// requires: farmersrespite
// requires: rusticdelight
// Coffee merge (feature_writeup/item_merges.md 2.8). Non-member file: recipe ids are creativedelight:<type>/<suffix>
// (global.CD.recipes). Moved from the pack's server_scripts/other/item_merges.js 2026-10-27, rows unchanged; moved from
// farmersrespite/item_merges.js to the Rustic member's tree 2026-11-01 (TODO creativedelight/members #11), rows unchanged.
//
// Rustic Delight's crop and drinks stay; Farmer's Respite's coffee cake, blazing chili and gambler's tea now take
// Rustic's beans (swapped by the merge). Removed: Respite's three kettle coffees and Central Kitchen's coffee fluid
// recipes (the swap would make Rustic's coffee from the kettle and pour it into Respite's coffee fluid), and the
// berry / bean / sack recipes the swap turns into loops or duplicates of Rustic's (beans -> beans, bag -> 9 beans,
// berries -> red dye next to Rustic's beans -> yellow dye).
ServerEvents.recipes(e => {
	if (!global.CD) return

	var i = global.item
	var R = global.NCH

	global.CD.recipes(() => {
		var FR_COFFEES = ['coffee', 'long_coffee', 'strong_coffee']
		FR_COFFEES.forEach(c => {
			R.remove({ id: 'farmersrespite:brewing/' + c }, e)
			R.remove({ id: 'create_central_kitchen:filling/' + c }, e)
			R.remove({ id: 'create_central_kitchen:emptying/' + c }, e)
		})
		R.remove({ id: 'create_central_kitchen:mixing/coffee' }, e)
		R.remove({ id: 'create_central_kitchen:milling/coffee_berries' }, e)
		R.remove({ id: 'farmersrespite:cutting/coffee_berries' }, e)
		R.remove({ id: 'farmersrespite:coffee_beans' }, e)
		R.remove({ id: 'farmersrespite:coffee_beans_sack' }, e)
		R.remove({ id: 'minecraft:red_dye' }, e)
		// The kettle type has no KubeJS schema, so the swap can't edit it: gambler's tea is rebuilt on Rustic's beans.
		R.remove({ id: 'farmersrespite:brewing/gamblers_tea' }, e)
		R.frBrewing([i.coffee_beans, i.glow_berries], i.gamblers_tea, e, 'gamblers_tea')
	})
})
