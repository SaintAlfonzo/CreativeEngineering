// priority: -100
//   ^ load after _lib/item_dict.js (priority 0). KubeJS startup-script order is
//   directory-walk order, not alphabetical, and func/ walks before _lib/ — so
//   without this, `global.item` is undefined when the boilerplate below reads i.*.
// ─────────────────────────────────────────────────────────────────────────────
//  global.tuneTools — per-item overrides for tool/weapon stats
// ─────────────────────────────────────────────────────────────────────────────
//
//  Runs on ItemEvents.modification (a STARTUP event) — edit + restart to apply,
//  a /reload will NOT re-run it.
//
//  global.tuneTools({
//    // key: one id, or several comma-separated ids that share a spec
//    'minecraft:diamond_pickaxe': {
//      damage:         5,       // attack damage shown in the tooltip
//      attackSpeed:    1.1,     // attack speed shown in the tooltip (4.0 = bare fist; lower = slower)
//      miningSpeed:    10,      // block-breaking multiplier (diamond 8, netherite 9) — DIGGERS only
//      durability:     3000,    // max uses
//      enchantability: 18,      // higher = better enchants per level — TIERED items only
//      miningLevel:    4,       // 0 wood/gold · 1 stone · 2 iron · 3 diamond · 4 netherite — TIERED only
//      repairWith:     'minecraft:netherite_ingot',   // anvil repair material — TIERED only
//      rarity:         'epic',  // common | uncommon | rare | epic (name colour)
//      fireproof:      true,    // survives fire/lava as a dropped item
//      stackSize:      1,
//    },
//    'minecraft:iron_sword, minecraft:iron_axe': { durability: 400, damage: 7 },
//  })
//
//  Notes / limits:
//   • `damage` / `attackSpeed` are the TOOLTIP numbers (this func subtracts the
//     player's 1.0 / 4.0 baseline for you). Weapons want attackSpeed < 4.
//   • `miningSpeed` only works on pickaxe/axe/shovel/hoe (DiggerItem). Swords are fixed.
//   • `enchantability` / `miningLevel` / `repairWith` only work on tiered tools
//     (anything with a material — vanilla + most modded). Not bows/shears/flint&steel/
//     elytra/shield — those aren't tiered and KubeJS can't retag their enchantability.
//   • Anything not applicable to a given item is skipped with a [tuneTools] warning,
//     the rest of the spec still applies.
//   • Call it as many times as you like (e.g. one call per mod); they stack.
//   • Calls are UNPREFIXED (item.setMaxDamage, not item.kjs$setMaxDamage). This
//     KubeJS build's ItemMixin carries @RemapPrefixForJS("kjs$"), which strips the
//     prefix for scripts — calling the kjs$-prefixed name throws "Cannot find
//     function" and silently no-ops every stat (verified in this pack's jar).
// ─────────────────────────────────────────────────────────────────────────────

console.log('tuneTools starting')

const R = global.NCH
var i = global.item;

;(function () {
	const Rarity = Java.loadClass('net.minecraft.world.item.Rarity')

	function applySpec(id, item, s) {
		const set = (label, fn) => {
			try { fn() }
			catch (err) { console.warn('[tuneTools] ' + id + ': skipped ' + label + ' — ' + err) }
		}

		if (s.durability     != null) set('durability',  () => item.setMaxDamage(Math.round(s.durability)))
		if (s.stackSize      != null) set('stackSize',   () => item.setMaxStackSize(Math.round(s.stackSize)))
		if (s.fireproof      != null) set('fireproof',   () => item.setFireResistant(s.fireproof === true))
		if (s.rarity         != null) set('rarity',      () => item.setRarity(Rarity.valueOf(String(s.rarity).toUpperCase())))

		// tooltip damage = 1.0 (player base) + modifier ; tooltip speed = 4.0 + modifier
		if (s.damage         != null) set('damage',      () => item.setAttackDamage(s.damage - 1.0))
		if (s.attackSpeed    != null) set('attackSpeed', () => item.setAttackSpeed(s.attackSpeed - 4.0))

		if (s.miningSpeed    != null) set('miningSpeed', () => item.setDigSpeed(s.miningSpeed))

		if (s.enchantability != null || s.miningLevel != null || s.repairWith != null) {
			set('tier', () => item.setTier(t => {
				if (s.enchantability != null) t.setEnchantmentValue(Math.round(s.enchantability))
				if (s.miningLevel    != null) t.setLevel(Math.round(s.miningLevel))
				if (s.repairWith     != null) t.setRepairIngredient(Ingredient.of(s.repairWith))
			}))
		}
	}

	global.tuneTools = function (specs) {
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
	// Boilerplate below = every Deep Dark (echo), Felsic and Dragon melee/digging
	// tool, each pre-filled with its CURRENT in-game values from the last
	// `/dumpgear` (kubejs/exported/gear/tools.csv). Edit any number, delete any
	// line to leave that stat at the mod default, then restart.
	//
	//   damage / attackSpeed = tooltip numbers (bare fist = 1.0 dmg / 4.0 speed)
	//   miningSpeed           = diggers only (swords have none — omitted there)
	//   miningLevel           = 0 wood/gold · 1 stone · 2 iron · 3 diamond · 4 netherite
	//   enchantability        = higher → better enchants per level
	//
	// Rhino here has no computed-key syntax, so specs are added via tune(id, {…})
	// with global.item keys rather than a big object literal.
	const toolSpecs = {}
	const tune = (id, spec) => {
		if (!id) { console.warn('[tuneTools] boilerplate: undefined item id — skipped'); return }
		toolSpecs[id] = spec
	}

	// ───────────────────────────── Deep Dark · echo ─────────────────────────────
	tune(i.echo_sword,   { damage: 9,  attackSpeed: 1.65,               durability: 2124, enchantability: 18, miningLevel: 4, rarity: 'epic', fireproof: true }) // create_deep_dark:echo_sword
	tune(i.echo_pickaxe, { damage: 6,   attackSpeed: 1.2, miningSpeed: 10, durability: 2124, enchantability: 18, miningLevel: 4, rarity: 'epic', fireproof: true }) // creativeengineering:echo_pickaxe
	tune(i.echo_axe,     { damage: 12,  attackSpeed: 1, miningSpeed: 10, durability: 2124, enchantability: 18, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.echo_shovel,  { damage: 7, attackSpeed: 1.2, miningSpeed: 10, durability: 2124, enchantability: 18, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.echo_hoe,     { damage: 1,   attackSpeed: 4.5, miningSpeed: 10, durability: 2124, enchantability: 18, miningLevel: 4, rarity: 'epic', fireproof: true })

	// ─────────────────────────────────── Felsic ────────────────────────────────
	tune(i.felsic_sword,   { damage: 10,   attackSpeed: 1.7,                durability: 2502, enchantability: 21, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.felsic_pickaxe, { damage: 7,   attackSpeed: 1.2, miningSpeed: 11, durability: 2501, enchantability: 21, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.felsic_axe,     { damage: 14,  attackSpeed: 1.1, miningSpeed: 11, durability: 2501, enchantability: 21, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.felsic_shovel,  { damage: 7.5, attackSpeed: 1.4, miningSpeed: 11, durability: 2501, enchantability: 21, miningLevel: 4, rarity: 'epic', fireproof: true })
	tune(i.felsic_hoe,     { damage: 1,   attackSpeed: 5.0, miningSpeed: 11, durability: 2501, enchantability: 21, miningLevel: 4, rarity: 'epic', fireproof: true })

	// ─────────────────────────────────── Dragon ────────────────────────────────
	tune(i.dragon_sword,   { damage: 11,  attackSpeed: 1.75,                durability: 3350, enchantability: 25, miningLevel: 5, rarity: 'epic', fireproof: true })
	tune(i.dragon_pickaxe, { damage: 8,   attackSpeed: 1.2, miningSpeed: 12, durability: 3350, enchantability: 25, miningLevel: 5, rarity: 'epic', fireproof: true })
	tune(i.dragon_axe,     { damage: 16,  attackSpeed: 1.2, miningSpeed: 12, durability: 3350, enchantability: 25, miningLevel: 5, rarity: 'epic', fireproof: true })
	tune(i.dragon_shovel,  { damage: 8, attackSpeed: 1.6, miningSpeed: 12, durability: 3350, enchantability: 25, miningLevel: 5, rarity: 'epic', fireproof: true })
	tune(i.dragon_hoe,     { damage: 1,   attackSpeed: 5.5, miningSpeed: 12, durability: 3350, enchantability: 25, miningLevel: 5, rarity: 'epic', fireproof: true })

	// Ranged/thrown dragon gear (dragon_bow / dragon_crossbow / dragon_trident) and
	// dragon_horse_armor aren't in /dumpgear. Bows/crossbows aren't tiered items, so
	// only durability / rarity / fireproof / stackSize apply there; the trident also
	// takes damage / attackSpeed. Look the current numbers up in JEI and add e.g.:
	//   tune(i.dragon_trident,  { damage: X, attackSpeed: Y, durability: Z, rarity: 'common', fireproof: true })
	//   tune(i.dragon_bow,      { durability: Z, rarity: 'common', fireproof: true })
	//   tune(i.dragon_crossbow, { durability: Z, rarity: 'common', fireproof: true })

	global.tuneTools(toolSpecs)
})()

console.log('tuneTools finished')
