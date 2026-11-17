// requires: creativedelight
// requires: delightful
// Delightful 3.8.1 loot modifiers on the installed 3.4.1 (feature_writeup/delightful.md 2.5), as Creative Loot drop rules: 3.8.1's
// own JSON uses Farmer's Delight 1.3's replace_item modifier, not in the repacked 1.2.4.

// Rules of Creative Loot's drops engine (loot/09_drops.js), queued with global.LOOT.rules: the block tables get a
// guarded pool, so a knife without Silk Touch gives the slices and the block itself does not drop.
global.LOOT.rules(function (L) {
	var i = global.item

	// A Cantaloupe broken with a knife (no Silk Touch) gives 4 Cantaloupe Slices instead (scavenging_cantaloupe)
	L.swapDrop('delightful:blocks/cantaloupe', i.cantaloupe, { item: i.cantaloupe_slice, count: 4 }, { knife: true, silkTouch: false })

	// A Mini Melon broken with a knife (no Silk Touch) gives 4 Melon Slices instead (scavenging_mini_melon)
	L.swapDrop('delightful:blocks/mini_melon', i.mini_melon, { item: i.melon, count: 4 }, { knife: true, silkTouch: false })
})
