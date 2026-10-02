// Can tags: #creativeengineering:cans/servings/<n> for every can in
// kubejs/generation_scripts/food/can_servings.json (item_dict key -> servings), chained up to
// #creativeengineering:cans/servings and #creativeengineering:cans.
// patcherMod food.CanServings reads the servings count from the tag path: eating or crafting takes a
// serving, the bar shows what's left, the last one rolls empty/crushed/nothing (CEConfig [canning]).
// Recipes use cans through global.NCH shaped/shapeless with the `cans` argument.
// Reference: feature_writeup/food.md

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	var path = 'kubejs/generation_scripts/food/can_servings.json'
	var data = null
	try { data = JSON.parse(String(JsonIO.readString(path))) } catch (err) {
		console.error('[can servings] could not read ' + path + ': ' + err)
		return
	}

	var byCount = {}
	var missing = []
	Object.keys(data.servings).forEach(key => {
		var n = parseInt(data.servings[key])
		if (!(n >= 1)) { console.warn('[can servings] ' + key + ': servings must be a whole number >= 1, got ' + data.servings[key]); return }
		var id = i[key]
		if (typeof id !== 'string' || id.charAt(0) === '#') { missing.push(key); return }
		if (!byCount[n]) byCount[n] = []
		byCount[n].push(id)
	})

	var total = 0
	Object.keys(byCount).forEach(n => {
		t.addTag(e, i.t_can_servings + '/' + n, byCount[n])
		total += byCount[n].length
	})

	if (missing.length) console.warn('[can servings] not item ids in item_dict: ' + missing.join(', '))
	console.log('[can servings] ' + total + ' cans: ' + Object.keys(byCount).map(n => byCount[n].length + ' x ' + n).join(', '))
})

// Canning ingredient groups (server_scripts/cooking/canning.js): varieties with the same value canned into one can.
// vinery's own #vinery:red_grape / #vinery:white_grape mix the small grapes with jungle grapes (5, 6), which can at a
// different ratio, so they get their own tags.
ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item

	t.add(e, i.any_grape, [
		i.red_grape, i.white_grape,
		i.savanna_grapes_red, i.savanna_grapes_white,
		i.taiga_grapes_red, i.taiga_grapes_white,
	])
	t.add(e, i.any_jungle_grape, [i.jungle_grapes_red, i.jungle_grapes_white])
})
