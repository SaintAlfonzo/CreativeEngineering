// requires: creativedelight
// requires: salt
// requires: morejs
// Salt's butcher trade, mirroring the Barbeque's Delight seasoning powders (barbequesdelight/trades.js): level 1,
// 12 emeralds for 64 salt, 4 uses, 4 XP, price multiplier 0.05 (salt.md 6 S9, TODO food/seasoning #12, 2026-11-03).
// Salt's own four trades (sell 18 or 8 salt for an emerald at level 1, buy 14 for 5 or 8 for 3 emeralds at level 3)
// are off: config/salt-common.toml ButcherSaltTradesEnabled = false.
MoreJSEvents.villagerTrades(e => {
	if (!global.CD) return
	var i = global.item
	var SALT = global.CD.on('vintagedelight') ? i.salt_dust : i.salt_item
	e.addTrade('minecraft:butcher', 1, [Item.of(i.emerald, 12)], Item.of(SALT, 64)).maxUses(4).villagerExperience(4).priceMultiplier(0.05)
})
