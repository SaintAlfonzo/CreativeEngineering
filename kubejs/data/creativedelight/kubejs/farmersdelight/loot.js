// requires: creativedelight
// requires: farmersdelight
// Farmer's Delight 1.3.4 loot modifiers on the repacked 1.2.4 (feature_writeup/farmers_delight.md 3), as Creative
// Loot drop rules: 1.3.4's own JSON uses 1.20's any_of condition and its replace_item modifier, neither in 1.2.4.

// Rules of Creative Loot's drops engine (loot/09_drops.js), queued with global.LOOT.rules.
global.LOOT.rules(function (L) {
	var i = global.item

	// Onions from zombies, husks and zombie villagers, and from pillagers, evokers and vindicators: 2% plus 1% per
	// Looting level, player kills only (add_onion_to_zombies / add_onion_to_illagers)
	L.drop(['minecraft:entities/zombie', 'minecraft:entities/husk', 'minecraft:entities/zombie_villager',
			'minecraft:entities/pillager', 'minecraft:entities/evoker', 'minecraft:entities/vindicator'],
		i.onion_farmersdelight, { chance: 0.02, looting: 0.01, killedByPlayer: true })

	// A pumpkin broken with a knife (no Silk Touch) gives 8 Pumpkin Slices instead (scavenging_pumpkin; the knife cut, TODO fruit #3)
	L.swapDrop('minecraft:blocks/pumpkin', i.pumpkin, { item: i.pumpkin_slice, count: 8 }, { knife: true, silkTouch: false })
})
