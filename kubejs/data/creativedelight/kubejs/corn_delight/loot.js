// requires: creativedelight
// requires: corn_delight
// Corn Delight loot rows (feature_writeup/corn_delight.md 2.4). Non-member file.

// A rule of Creative Loot's drops engine (loot/09_drops.js), queued with global.LOOT.rules.
global.LOOT.rules(function (L) {
	if (!global.CD) return
	var i = global.item
	var b = global.blocks

	// Mature corn broken with a knife (no Silk Touch): 25% chance of a Corncob on top of the normal drops. 1.2.11 gives
	// Straw here; the pack gives the cob, and corn still gives no straw. Same knife and Silk Touch test as the pumpkin
	// rule (farmersdelight/loot.js).
	L.drop('corn_delight:blocks/corn_crop', i.corncob, { chance: 0.25, knife: true, silkTouch: false, state: { age: '7' },
		stateBlock: b.corn_crop })
})
