// priority: -100
//   ^ load after _lib/item_dict.js (priority 0). KubeJS startup-script order is
//   directory-walk order, not alphabetical, and func/ walks before _lib/ — so
//   without this, `global.item` is undefined when the boilerplate below reads i.*.
// ─────────────────────────────────────────────────────────────────────────────
//  global.tuneArmor — per-item overrides for armor stats
// ─────────────────────────────────────────────────────────────────────────────
//
//  Sibling of func/tune_tools.js. Runs on ItemEvents.modification (a STARTUP
//  event) — edit + restart to apply, a /reload will NOT re-run it.
//
//  global.tuneArmor({
//    'minecraft:netherite_chestplate': {
//      armor:               8,      // armor points while worn (SET — replaces the base value, not added on top)
//      toughness:           3,      // armor toughness
//      knockbackResistance: 0.1,    // 0..1  (0.1 ≈ ignore 10% of knockback)
//      durability:          592,    // max uses
//      rarity:              'epic', // common | uncommon | rare | epic (name colour)
//      fireproof:           true,   // survives fire/lava as a dropped item
//      stackSize:           1,
//    },
//    'a:helmet, a:boots': { armor: 3 },   // comma-separated ids share a spec
//  })
//
//  Notes / limits:
//   • armor / toughness / knockbackResistance go through KubeJS's setArmorProtection/
//     Toughness/KnockbackResistance, which reuse the vanilla per-slot modifier UUID →
//     they REPLACE the piece's built-in value rather than stacking with it.
//     ArmorItem (+ subclasses) only.
//   • enchantability / repairWith are NOT reachable on armor from KubeJS (armor
//     reads them off its ArmorMaterial, which has no kjs$ accessor). Passing them
//     logs a [tuneArmor] warning and is otherwise ignored. To change them you need
//     a patcherMod mixin — echo (18) and felsic (21) are already done there in
//     ArmorMaterialEnchantabilityMixin (keyed by ArmorMaterial name); add a row for more.
//   • Anything not applicable to an item is skipped with a warning; the rest of
//     the spec still applies.
//   • Call it as many times as you like; calls stack.
//   • Calls are UNPREFIXED (item.setArmorProtection, not item.kjs$setArmorProtection).
//     This KubeJS build's ItemMixin carries @RemapPrefixForJS("kjs$"), which strips
//     the prefix for scripts — the kjs$-prefixed name throws "Cannot find function"
//     and silently no-ops every stat (verified in this pack's jar).
// ─────────────────────────────────────────────────────────────────────────────

console.log('tuneArmor starting')

var i = global.item;

;(function () {
	const Rarity = Java.loadClass('net.minecraft.world.item.Rarity')

	function applySpec(id, item, s) {
		const set = (label, fn) => {
			try { fn() }
			catch (err) { console.warn('[tuneArmor] ' + id + ': skipped ' + label + ' — ' + err) }
		}

		if (s.durability != null) set('durability', () => item.setMaxDamage(Math.round(s.durability)))
		if (s.stackSize  != null) set('stackSize',  () => item.setMaxStackSize(Math.round(s.stackSize)))
		if (s.fireproof  != null) set('fireproof',  () => item.setFireResistant(s.fireproof === true))
		if (s.rarity     != null) set('rarity',     () => item.setRarity(Rarity.valueOf(String(s.rarity).toUpperCase())))

		if (s.armor               != null) set('armor',               () => item.setArmorProtection(s.armor))
		if (s.toughness           != null) set('toughness',           () => item.setArmorToughness(s.toughness))
		if (s.knockbackResistance != null) set('knockbackResistance', () => item.setArmorKnockbackResistance(s.knockbackResistance))

		if (s.enchantability != null) console.warn('[tuneArmor] ' + id + ': enchantability is not tunable on armor from KubeJS — ignored')
		if (s.repairWith     != null) console.warn('[tuneArmor] ' + id + ': repairWith is not tunable on armor from KubeJS — ignored')
	}

	global.tuneArmor = function (specs) {
		if (!specs) return
		ItemEvents.modification(event => {
			Object.keys(specs).forEach(key => {
				const spec = specs[key]
				String(key).split(',').map(x => x.trim()).filter(Boolean).forEach(id => {
					event.modify(id, item => applySpec(id, item, spec))
				})
			})
		})
	}

	// ═══════════════════════════ your overrides ═══════════════════════════
	// Boilerplate below = every Deep Dark (echo), Felsic and Dragon armor piece,
	// each pre-filled with its CURRENT in-game values from the last `/dumpgear`
	// (kubejs/exported/gear/armor.csv). Edit freely, then restart. `enchantability`
	// is shown as a trailing comment only — see the note at the top of the file.
	const armorSpecs = {}
	const tune = (id, spec) => {
		if (!id) { console.warn('[tuneArmor] boilerplate: undefined item id — skipped'); return }
		armorSpecs[id] = spec
	}

	// ───────────────────────────── Deep Dark · echo ─────────────────────────────
	// ench forced to 18 by patcherMod ArmorMaterialEnchantabilityMixin (mat "echo_armor")
	tune(i.echo_helmet,     { armor: 3.5,  toughness: 3.5,   knockbackResistance: 0.2, durability: 429,  rarity: 'epic', fireproof: true }) // create_deep_dark:echo_armor_helmet
	tune(i.echo_chestplate, { armor: 8.5,  toughness: 3.5,   knockbackResistance: 0.2, durability: 624,  rarity: 'epic', fireproof: true }) // create_deep_dark:echo_armor_chestplate
	tune(i.echo_leggings,   { armor: 6.5,  toughness: 3.5,   knockbackResistance: 0.2, durability: 585,  rarity: 'epic', fireproof: true }) // create_deep_dark:echo_armor_leggings
	tune(i.echo_boots,      { armor: 3.5,  toughness: 3.5,   knockbackResistance: 0.2, durability: 507,  rarity: 'epic', fireproof: true }) // create_deep_dark:echo_armor_boots

	// ─────────────────────────────────── Felsic ────────────────────────────────
	// ench forced to 21 by patcherMod ArmorMaterialEnchantabilityMixin (mat "felsic")
	tune(i.felsic_helmet,     { armor: 4, toughness: 4, knockbackResistance: 0.25, durability: 572,  rarity: 'epic', fireproof: true })
	tune(i.felsic_chestplate, { armor: 9, toughness: 4, knockbackResistance: 0.25, durability: 832,  rarity: 'epic', fireproof: true })
	tune(i.felsic_leggings,   { armor: 7, toughness: 4, knockbackResistance: 0.25, durability: 780,  rarity: 'epic', fireproof: true })
	tune(i.felsic_boots,      { armor: 4, toughness: 4, knockbackResistance: 0.25, durability: 676,  rarity: 'epic', fireproof: true })

	// ─────────────────────────────────── Dragon ────────────────────────────────
	tune(i.dragon_helmet,              { armor: 7,  toughness: 4.5, knockbackResistance: 0.3, durability: 1300, rarity: 'epic', fireproof: true }) // ench 25
	tune(i.dragon_chestplate,          { armor: 12, toughness: 4.5, knockbackResistance: 0.3, durability: 1750, rarity: 'epic', fireproof: true }) // ench 25
	tune(i.upgraded_dragon_chestplate, { armor: 12, toughness: 4.5, knockbackResistance: 0.3, durability: 1750, rarity: 'epic', fireproof: true }) // dragonloot:upgraded_dragon_chestplate · ench 25
	tune(i.dragon_leggings,            { armor: 10,  toughness: 4.5, knockbackResistance: 0.3, durability: 1600, rarity: 'epic', fireproof: true }) // ench 25
	tune(i.dragon_boots,               { armor: 7,  toughness: 4.5, knockbackResistance: 0.3, durability: 1400, rarity: 'epic', fireproof: true }) // ench 25

	// dragon_horse_armor is not an ArmorItem (HorseArmorItem) — kjs$setArmor* won't
	// take. Tune its protection via a datapack attribute or patcherMod if needed.

	global.tuneArmor(armorSpecs)
})()

console.log('tuneArmor finished')
