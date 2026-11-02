// Food type tags: every food in exactly one #creativeengineering:food_type/<type>, plus
// #creativeengineering:canned_source for foods that already have a Canned Goods can.
//
// The assignments live in kubejs/generation_scripts/food/food_types.json (type -> item_dict keys). Move a food by
// moving its key there, then python3 tools/food_catalog.py --members. python3 tools/food_catalog.py puts new foods
// into a type (from a /dumpfood export), gives them item_dict keys and checks the file; it never moves a food you
// placed. The game reads the <mod>/food_types.js member scripts the tool writes from the JSON, one per item namespace
// (`// requires: <mod>`, framework #7 2026-11-01; this file read the JSON by instance path before).
//
// A nested type also lands in its parents: raw/meat/sliced -> raw/meat -> raw -> food_type, so
// #creativeengineering:food_type/raw is every raw food and #creativeengineering:food_type every typed one.
// Recipes reference the tags through the i.t_food_* keys (generated block at the end of item_dict.js).
// Reference: feature_writeup/food.md
// Moved from the pack's server_scripts/tags/foodtypetags.js 2026-10-25, rows unchanged. Removed items in
// food_types.json lose these tags again in _core/removed_item_tags.js, which runs after every CD tag script.

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var i = global.item
	var data = global.CD.genMerge('food_types')
	if (!data.types) {
		console.error('[food types] no food_types member scripts loaded, run python3 tools/food_catalog.py --members')
		return
	}
	if (!data.canned) data.canned = {}

	var isItemId = v => typeof v === 'string' && v.charAt(0) !== '#'
	var seen = {}
	var missing = []
	var total = 0

	Object.keys(data.types).forEach(type => {
		var tagKey = 't_food_' + type.replace(/\//g, '_')
		var tag = i[tagKey]
		if (!tag) {
			console.warn(`[food types] no item_dict key i.${tagKey} - run python3 tools/food_catalog.py, then restart`)
			tag = '#creativeengineering:food_type/' + type
		}
		var ids = []
		data.types[type].forEach(key => {
			if (seen[key]) console.warn(`[food types] ${key} is in both ${seen[key]} and ${type}; the tags get both`)
			seen[key] = type
			if (isItemId(i[key])) ids.push(i[key])
			else missing.push(key)
		})
		t.addTag(e, tag, ids)
		total += ids.length
	})

	var sources = []
	Object.keys(data.canned).forEach(can => {
		data.canned[can].forEach(key => {
			if (!isItemId(i[key])) { missing.push(key); return }
			if (sources.indexOf(i[key]) < 0) sources.push(i[key])
		})
	})
	t.add(e, i.t_canned_source, sources)

	if (missing.length) {
		console.warn(`[food types] ${missing.length} key(s) are not item ids in item_dict (new keys need a restart): `
			+ missing.slice(0, 25).join(', ') + (missing.length > 25 ? ' ...' : ''))
	}
	console.log(`[food types] ${total} foods in ${Object.keys(data.types).length} types, ${sources.length} canned sources`)
})
