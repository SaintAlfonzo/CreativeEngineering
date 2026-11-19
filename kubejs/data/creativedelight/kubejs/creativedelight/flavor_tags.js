// requires: creativedelight
// Food family tags and the flavorable tags built from them (flavors were seasonings until food/seasoning #25).
//
// #creativedelight:food_family/<path>: every member of each food family, edible items and whole food blocks (block
// items; plan_bugfix_1114.md 7.4) (feature_writeup/food_families.md),
// from kubejs/generation_scripts/food/food_families.json (written by python3 tools/food_families.py, or /cdevgen run
// food_families, after /dumpall). A subfamily also lands in its parents: snack, fruit/whole -> fruit -> food_family.
//
// #<ns>:flavorable/<path>, one per pack flavor (global.flavor, Creative Delight's
// kubejs_startup/creativedelight/flavors.js): the foods that flavor may go on, in this order:
//   1. families: every member of each listed family and its subfamilies (expanded to item ids here)
//   2. add:      single items or tags, after the families
//   3. remove:   single items, last. An item that only comes in through a tag in `add` can't be removed (nested tag).
// The flavor craft (Creative Delight food/FlavorRecipe) refuses a food outside the tag, and flavor.js makes one
// craft per flavor with the tag as its food. Salt keeps its own #salt:can_be_salted.

ServerEvents.tags('item', e => {
	if (!global.CD) return
	var t = global.tags
	var path = 'kubejs/generation_scripts/food/food_families.json'
	var families = {}
	try { families = JSON.parse(String(global.genText(path))).families } catch (err) {
		console.error('[food families] could not read ' + path + ': ' + err + '; family tags and family flavors empty')
	}

	Object.keys(families).forEach(f => {
		if (families[f].length) t.addTag(e, 'creativedelight:food_family/' + f, families[f])
	})

	// a family's members plus all of its subfamilies'
	var members = f => {
		var out = []
		Object.keys(families).forEach(g => {
			if (g === f || g.indexOf(f + '/') === 0) out = out.concat(families[g])
		})
		return out
	}

	Object.keys(global.flavor || {}).forEach(key => {
		var s = global.flavor[key]
		if (s.needs && !global.CD.on(s.needs)) return
		var colon = s.id.indexOf(':')
		var tag = s.id.substring(0, colon) + ':flavorable/' + s.id.substring(colon + 1)
		var ids = {}
		;(s.families || []).forEach(f => {
			var m = members(f)
			if (!m.length) console.warn(`[flavor] ${key}: food family ${f} has no members`)
			m.forEach(id => { ids[id] = true })
		})
		var entries = Object.keys(ids).concat(s.add || [])
		if (entries.length) t.add(e, tag, entries)
		if ((s.remove || []).length) t.remove(e, tag, s.remove)
	})
})
