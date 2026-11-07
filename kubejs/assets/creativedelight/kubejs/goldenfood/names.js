// requires: creativedelight
// Gilded food names (food.md 3.16, golden_food.md 3.7; TODO food/types-and-tuning #23, owner 2026-11-02). The
// enchanted golden foods are called "Gilded" in language; their ids stay `enchanted_golden_*`. Covers every
// item_dict id with an `enchanted_golden_` path (Golden Food's 24, vanilla's apple, Immersive Weathering's moss clump),
// their generated cans (`canned_enchanted_golden_*`) and their sequenced assembly incomplete items
// (`kubejs:incomplete_enchanted_golden_*`). The name is "Gilded" plus the rest of the id in title case, as the old
// names were ("Enchanted Golden Cooked Beef" -> "Gilded Cooked Beef").
ClientEvents.lang('en_us', e => {
	if (!global.CD || !global.item) return
	var title = s => s.split('_').map(w => w.charAt(0).toUpperCase() + w.substring(1)).join(' ')
	var seen = {}
	Object.keys(global.item).forEach(key => {
		var id = global.item[key]
		if (typeof id !== 'string' || id.charAt(0) === '#' || seen[id]) return
		var m = /^([a-z0-9_.-]+):(canned_)?enchanted_golden_([a-z0-9_]+)$/.exec(id)
		if (!m) return
		seen[id] = true
		var name = 'Gilded ' + title(m[3])
		if (m[2]) {
			e.renameItem(id, 'Canned ' + name)
		} else {
			e.renameItem(id, name)
			e.renameItem('kubejs:incomplete_enchanted_golden_' + m[3], 'Incomplete ' + name)
		}
	})
})
