// requires: creativedelight
// requires: corn_delight
// requires: lootjs
// Corn Delight loot rows (feature_writeup/corn_delight.md 2.4). Non-member file.

LootJS.modifiers(event => {
	if (!global.CD) return
	var i = global.item
	var b = global.blocks

	// Mature corn broken with a knife (no Silk Touch): 25% chance of a Corncob on top of the normal drops. 1.2.11 gives
	// Straw here; the pack gives the cob, and corn still gives no straw. Same form as the pumpkin knife rule
	// (farmersdelight/loot.js).
	event.addBlockLootModifier(b.corn_crop)
		.matchBlockState(Block.getBlock(b.corn_crop), { age: '7' })
		.matchMainHand(i.any_knife)
		.not(n => n.matchMainHand(ItemFilter.hasEnchantment('minecraft:silk_touch')))
		.randomChance(0.25)
		.addLoot(i.corncob)
		.logName('CE/corn_knife_corncob')
})
