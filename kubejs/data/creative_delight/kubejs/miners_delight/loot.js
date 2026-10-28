// requires: miners_delight
// requires: lootjs
// Squid drops for the squid chain (TODO dishes/ocean #3): a squid drops one Miner's Delight squid item and a glow squid
// one glow squid item, every kill, knife or not; the ink and the calamari come from cutting it (miners_delight/recipes.js).
// LootJS runs after every mod's loot modifiers, so this also takes out what they add: Rustic's calamari, Miner's knife
// squid item, and the tentacles of Miner's, Ocean's and Oceanic Delight (those items stay until dishes/ocean #4).

LootJS.modifiers(event => {
	if (!global.CD || !global.CD.on('rusticdelight')) return
	var i = global.item
	var TENTACLES = [i.tentacles, i.oceansdelight_tentacles, i.squid_tentacles, i.glow_squid_tentacles]
		.filter(id => typeof id === 'string' && Item.exists(id))

	;[
		['minecraft:squid', i.squid, [i.ink_sac, i.calamari]],
		['minecraft:glow_squid', i.glow_squid, [i.glow_ink, i.calamari, i.glowing_calamari]],
	].forEach(s => {
		event.addEntityLootModifier(s[0])
			.removeLoot(s[2].concat(TENTACLES, [s[1]]))
			.addLoot(s[1])
			.logName('CE/squid_chain/' + s[0].split(':')[1])
	})
})
