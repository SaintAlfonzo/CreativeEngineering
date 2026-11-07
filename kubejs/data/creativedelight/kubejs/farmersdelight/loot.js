// requires: creativedelight
// requires: farmersdelight
// requires: lootjs
// Farmer's Delight 1.3.4 loot modifiers on the repacked 1.2.4 (feature_writeup/farmers_delight.md 3), as LootJS
// rows: 1.3.4's own JSON uses 1.20's any_of condition and its replace_item modifier, neither in 1.2.4.

LootJS.modifiers(event => {
	var i = global.item

	// Onions from zombies, husks and zombie villagers, and from pillagers, evokers and vindicators: 2% plus 1% per
	// Looting level, player kills only (add_onion_to_zombies / add_onion_to_illagers)
	event.addEntityLootModifier('minecraft:zombie', 'minecraft:husk', 'minecraft:zombie_villager',
			'minecraft:pillager', 'minecraft:evoker', 'minecraft:vindicator')
		.killedByPlayer()
		.randomChanceWithLooting(0.02, 0.01)
		.addLoot(i.onion_farmersdelight)
		.logName('CE/fd_onion_drops')

	// A pumpkin broken with a knife (no Silk Touch) gives 4 Pumpkin Slices instead (scavenging_pumpkin)
	event.addBlockLootModifier(i.pumpkin)
		.matchMainHand(i.any_knife)
		.not(n => n.matchMainHand(ItemFilter.hasEnchantment('minecraft:silk_touch')))
		.removeLoot(i.pumpkin)
		.addLoot(Item.of(i.pumpkin_slice, 4))
		.logName('CE/fd_pumpkin_knife')
})
