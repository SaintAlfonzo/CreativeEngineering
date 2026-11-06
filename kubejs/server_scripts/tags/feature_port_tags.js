// Pack tags of ported features. The AE2 QoL tier-1 port tags moved to Creative Compat on 2026-11-01
// (creativecompat/kubejs/tags/feature_port_tags.js).

// Bottled Air (patcherMod item.BottledAir, [bottled_air]): the "fire type" items that evaporate the filled bottle when
// held in the other hand (the mod's list: campfires, fire charge, lava bucket, magma block).
ServerEvents.tags('item', e => {
	var t = global.tags
	var i = global.item
	t.add(e, 'creativeengineering:bottled_air/fire_sources', [i.campfire, i.soul_campfire, i.fire_charge, i.lava_bucket, i.magma_block])
})
