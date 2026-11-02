// requires: miners_delight
// requires: lootjs
// Squid drops for the squid chain (TODO dishes/ocean #3): a squid drops one Miner's Delight squid item and a glow squid
// one glow squid item, every kill, knife or not; the ink and the calamari come from cutting it (miners_delight/recipes.js).
// LootJS runs after every mod's loot modifiers, so this also takes out what they add: Rustic's calamari, Miner's knife
// squid item, and the tentacles of Miner's, Ocean's and Oceanic Delight (those items are removed, dishes/ocean #4).
// Exactly one (TODO dishes/ocean #9, 2026-11-02): every other drop goes, whatever added it, so looting, a knife or
// another mod's modifier can't add a second squid or anything else; the rule no longer waits on Rustic Delight.

LootJS.modifiers(event => {
	if (!global.CD) return
	var i = global.item

	;[
		['minecraft:squid', i.squid],
		['minecraft:glow_squid', i.glow_squid],
	].forEach(s => {
		event.addEntityLootModifier(s[0])
			.removeLoot(Ingredient.all)
			.addLoot(s[1])
			.logName('CE/squid_chain/' + s[0].split(':')[1])
	})
})

// Cave carrot crate split out of the loose cave carrot's entry, like the Farmer's Delight crates (pre-roll loot
// framework, server_scripts/loot/05_tuning.js). The cave carrots and copper carrots of abandoned mineshafts come from
// Miner's own chest loot modifier (D8), which stays. miners_delight.md I5.
;(function () {
	if (!global.lootTuning) return
	var i = global.item
	global.lootTuning.pack(i.cave_carrot_crate, i.cave_carrot)
})()
