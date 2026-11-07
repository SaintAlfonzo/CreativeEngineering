// requires: creativedelight
// Can tags: #creativeengineering:cans/servings/<n> for every can in the <mod>/can_servings.js startup member
// scripts of the CD jar (item_dict key -> servings; record kubejs/generation_scripts/food/can_servings.json, written
// by tools/gen_can_types.py; framework #7 2026-11-01, read by instance path before), chained up to
// #creativeengineering:cans/servings and #creativeengineering:cans.
// patcherMod food.CanServings reads the servings count from the tag path: eating or crafting takes a
// serving, the bar shows what's left, the last one rolls empty/crushed/nothing (CEConfig [canning]).
// Recipes use cans through global.NCH shaped/shapeless with the `cans` argument.
// Reference: feature_writeup/food.md
// Moved from the pack's server_scripts/tags/cantags.js 2026-10-25, rows unchanged; the JSON stays in the pack folder.

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	var data = global.CD.genMerge('can_servings')
	if (!data.servings) {
		console.error('[can servings] no can_servings member scripts loaded (CD jar startup scripts), run tools/gen_can_types.py --members')
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
