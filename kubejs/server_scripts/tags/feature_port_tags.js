// Tags of the ported tier-1 mods (feature_writeup/feature_porting.md 1; recipes in other/feature_ports.js).
// AE2 QoL Recipes: #ae2:can_remove_color (the bucket in the middle of its fluix "undye" recipes) = #forge:buckets/water,
// and #forge:buckets/water = the water bucket.

ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	t.add(e, 'forge:buckets/water', i.water_bucket)
	t.add(e, 'ae2:can_remove_color', '#forge:buckets/water')
})

// Bottled Air (patcherMod item.BottledAir, [bottled_air]): the "fire type" items that evaporate the filled bottle when
// held in the other hand (the mod's list: campfires, fire charge, lava bucket, magma block).
ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	t.add(e, 'creativeengineering:bottled_air/fire_sources', [i.campfire, i.soul_campfire, i.fire_charge, i.lava_bucket, i.magma_block])
})
