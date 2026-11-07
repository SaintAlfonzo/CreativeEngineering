// requires: creativedelight
// requires: barbequesdelight
// requires: morejs
// Barbeque's Delight butcher trades (food/seasoning #18, feature_writeup/barbeques_delight.md O38): the six seasonings
// as upstream sold them (BBQGeneralEventHandlers: butcher level 1, 12 emeralds, 4 uses, 4 XP, price multiplier 0.05).
// Since the seasoning fold (food/seasoning #11, O31) a seasoning is single use, so one trade gives the old item's uses:
// 64 of a powder, 16 of a sauce. Upstream's six level-1 listings flood the novice pool (bug B2, kept by owner decision).
MoreJSEvents.villagerTrades(e => {
	if (!global.CD || !global.CD.on('barbequesdelight')) return
	var i = global.item
	var butcher = 'minecraft:butcher'
	var sold = [
		[i.cumin_powder, 64], [i.pepper_powder, 64], [i.chili_powder, 64],
		[i.honey_mustard_sauce, 16], [i.buffalo_sauce, 16], [i.barbeque_sauce, 16],
	]
	sold.forEach(s => {
		e.addTrade(butcher, 1, [Item.of(i.emerald, 12)], Item.of(s[0], s[1])).maxUses(4).villagerExperience(4).priceMultiplier(0.05)
	})
})
