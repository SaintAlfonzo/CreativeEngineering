// requires: delightful
// requires: lootjs
// Delightful 3.8.1 loot modifiers on the installed 3.4.1 (feature_writeup/delightful.md 2.5), as LootJS rows: 3.8.1's
// own JSON uses Farmer's Delight 1.3's replace_item modifier, not in the repacked 1.2.4.

LootJS.modifiers(event => {
	var i = global.item

	// A Cantaloupe broken with a knife (no Silk Touch) gives 4 Cantaloupe Slices instead (scavenging_cantaloupe)
	event.addBlockLootModifier(i.cantaloupe)
		.matchMainHand(i.any_knife)
		.not(n => n.matchMainHand(ItemFilter.hasEnchantment('minecraft:silk_touch')))
		.removeLoot(i.cantaloupe)
		.addLoot(Item.of(i.cantaloupe_slice, 4))
		.logName('CE/delightful_cantaloupe_knife')

	// A Mini Melon broken with a knife (no Silk Touch) gives 4 Melon Slices instead (scavenging_mini_melon)
	event.addBlockLootModifier(i.mini_melon)
		.matchMainHand(i.any_knife)
		.not(n => n.matchMainHand(ItemFilter.hasEnchantment('minecraft:silk_touch')))
		.removeLoot(i.mini_melon)
		.addLoot(Item.of(i.melon, 4))
		.logName('CE/delightful_mini_melon_knife')
})
