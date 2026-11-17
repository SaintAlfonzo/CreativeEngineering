// The stats tuneArmor (tune_armor.js) and tuneTools (tune_tools.js) share: durability, stack size, fireproof and
// rarity. Each tuner calls it first and gets back its guarded setter for the stats of its own kind.
//   const set = global.CEQ.tuneBase('tuneArmor', id, item, spec)
//   if (spec.armor != null) set('armor', () => item.setArmorProtection(spec.armor))
;(function () {
	const Rarity = Java.loadClass('net.minecraft.world.item.Rarity')

	global.CEQ.tuneBase = function (tag, id, item, s) {
		const set = (label, fn) => {
			try { fn() }
			catch (err) { console.warn('[' + tag + '] ' + id + ': skipped ' + label + ' — ' + err) }
		}

		if (s.durability != null) set('durability', () => item.setMaxDamage(Math.round(s.durability)))
		if (s.stackSize  != null) set('stackSize',  () => item.setMaxStackSize(Math.round(s.stackSize)))
		if (s.fireproof  != null) set('fireproof',  () => item.setFireResistant(s.fireproof === true))
		if (s.rarity     != null) set('rarity',     () => item.setRarity(Rarity.valueOf(String(s.rarity).toUpperCase())))
		return set
	}
})()
