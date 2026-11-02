// Loot meta tags: item groups the chest-loot rules (loot/07_rules.js) tune as one unit.
// A '#tag' rule reads the tag's members from this same reload (loot/95_tag_capture.js), so
// anything added here or nested in later is picked up by the next /reload.

var t = global.tags
var i = global.item
var b = global.blocks

ServerEvents.tags('item', e => {
	// every chalk stick (nested #chalk:chalks, so new colours follow) + the chalk box
	t.add(e, i.t_loot_chalk, [i.t_chalks, i.chalk_box])

	// golden food: golden apple / carrot + Golden Food's plain golden foods (the enchanted ones and
	// the notch apple stay out: they're the rare tier)
	t.add(e, i.t_loot_golden_food, [i.golden_apple, i.golden_carrot])
	t.addMatching(e, i.t_loot_golden_food, 'goldenfood:golden_*')
})

// Oceanic Delight fishing net tags (block, coral item and biome): Creative Delight
// (kubejs/data/creativedelight/kubejs/oceanic_delight/fishing_net_tags.js, moved 2026-11-01).
