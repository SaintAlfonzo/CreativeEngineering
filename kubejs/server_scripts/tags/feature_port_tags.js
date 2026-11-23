// requires: creativecompat
// Pack script: moved out of Creative Compat (Compat publishes compatibility fixes only; publish_readiness.md
// D1, PR-CP-01 to -03). Recipe ids keep the creativecompat namespace (forPack) so nothing already saved changes.
// AE2 QoL Recipes: #ae2:can_remove_color (the bucket in the middle of its fluix "undye" recipes) =
// #forge:buckets/water, and #forge:buckets/water = the water bucket.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	t.add(e, 'forge:buckets/water', i.water_bucket)
	t.add(e, 'ae2:can_remove_color', '#forge:buckets/water')
})
