// requires: creativecompat
// Creative Compat: tags of the ported tier-1 mods (feature_writeup/feature_porting.md 1; recipes in
// other/feature_ports.js); moved from kubejs/server_scripts/tags/feature_port_tags.js on 2026-11-01.
// AE2 QoL Recipes: #ae2:can_remove_color (the bucket in the middle of its fluix "undye" recipes) = #forge:buckets/water,
// and #forge:buckets/water = the water bucket.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	t.add(e, 'forge:buckets/water', i.water_bucket)
	t.add(e, 'ae2:can_remove_color', '#forge:buckets/water')
})
