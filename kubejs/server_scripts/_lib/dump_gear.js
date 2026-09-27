// /dumpgear [filter ...]   ->  writes kubejs/exported/gear/
//
//   armor.json  / armor.csv        one row per armor piece  (every value worth tuning)
//   armor_sets.json / armor_sets.csv   one row per material, totals for a full 4-piece set
//   tools.json  / tools.csv        one row per tool/weapon
//
//   filter tokens (space- or comma-separated). Default (no token) = every armor + tool in the registry.
//     armor                every ArmorItem
//     tools                every TieredItem / DiggerItem (swords, pickaxes, axes, shovels, hoes, ...)
//     <namespace>          every gear item from that mod            e.g.  immersiveengineering
//     <namespace:path>     one exact item id                       e.g.  minecraft:netherite_sword
//     #<namespace:path>    every item in that item tag             e.g.  #forge:armors
//
//   Also callable from other scripts:
//     dumpGear(['immersiveengineering', 'create', 'minecraft:netherite_chestplate'])

ServerEvents.commandRegistry(event => {
	var { commands: Commands, arguments: Arguments } = event

	var run = (ctx, filter) => {
		var r = dumpGear(filter)
		ctx.source.sendSuccess(
			Text.green(`Dumped ${r.armor} armor pieces (${r.sets} sets) + ${r.tools} tools to kubejs/exported/gear/`), false)
		return 1
	}

	event.register(
		Commands.literal('dumpgear')
			.requires(src => src.hasPermission(2))
			.executes(ctx => run(ctx, []))
			.then(
				Commands.argument('filter', Arguments.GREEDY_STRING.create(event))
					.executes(ctx => run(ctx, Arguments.GREEDY_STRING.getResult(ctx, 'filter')
						.split(/[,\s]+/).map(s => s.trim()).filter(Boolean)))
			)
	)
})

function dumpGear(filter) {
	const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	const ArmorItem     = Java.loadClass('net.minecraft.world.item.ArmorItem')
	const TieredItem    = Java.loadClass('net.minecraft.world.item.TieredItem')
	const DiggerItem    = Java.loadClass('net.minecraft.world.item.DiggerItem')
	const SwordItem     = Java.loadClass('net.minecraft.world.item.SwordItem')
	const PickaxeItem   = Java.loadClass('net.minecraft.world.item.PickaxeItem')
	const AxeItem       = Java.loadClass('net.minecraft.world.item.AxeItem')
	const ShovelItem    = Java.loadClass('net.minecraft.world.item.ShovelItem')
	const HoeItem       = Java.loadClass('net.minecraft.world.item.HoeItem')
	const EquipmentSlot = Java.loadClass('net.minecraft.world.entity.EquipmentSlot')

	let TierSorting = null
	try { TierSorting = Java.loadClass('net.minecraftforge.common.TierSortingRegistry') } catch (err) {}

	// ── parse filter ─────────────────────────────────────────────────────────
	const tokens        = (filter || []).map(t => String(t))
	const noFilter      = tokens.length === 0
	const wantAllArmor  = noFilter || tokens.indexOf('armor') !== -1
	const wantAllTools  = noFilter || tokens.indexOf('tools') !== -1
	const modFilters    = tokens.filter(t => t !== 'armor' && t !== 'tools' && t.charAt(0) !== '#' && t.indexOf(':') === -1)
	const idFilters     = tokens.filter(t => t.charAt(0) !== '#' && t.indexOf(':') !== -1)
	const tagFilters    = tokens.filter(t => t.charAt(0) === '#').map(t => t.substring(1))

	const explicitIds = {}
	idFilters.forEach(id => { explicitIds[id] = true })
	tagFilters.forEach(tag => {
		let resolved = 0
		try {
			Ingredient.of('#' + tag).getItemIds().forEach(id => { explicitIds[String(id)] = true; resolved++ })
		} catch (err) {
			try {
				Ingredient.of('#' + tag).itemIds.forEach(id => { explicitIds[String(id)] = true; resolved++ })
			} catch (err2) { console.warn(`[dumpgear] could not resolve tag #${tag}: ${err2}`) }
		}
		if (resolved) console.log(`[dumpgear] tag #${tag} -> ${resolved} items`)
	})

	// ── helpers ──────────────────────────────────────────────────────────────
	const round = (n, d) => {
		var p = Math.pow(10, d || 2)
		return Math.round(n * p) / p
	}

	const idOf = obj => {
		try { return ForgeRegistries.ITEMS.getKey(obj).toString() } catch (err) { return null }
	}

	const ingredientIds = ing => {
		var out = []
		try {
			ing.getItems().forEach(stack => {
				var rid = idOf(stack.getItem())
				if (rid && out.indexOf(rid) === -1) out.push(rid)
			})
		} catch (err) {}
		return out
	}

	// every attribute modifier this stack grants in a given slot
	const modifiersForSlot = (stack, slot) => {
		var out = []
		let map
		try { map = stack.getAttributeModifiers(slot) } catch (err) { return out }
		if (!map) return out
		try {
			map.entries().forEach(e => {
				var attr = e.getKey()
				var mod  = e.getValue()
				let attrId = 'unknown'
				try { attrId = ForgeRegistries.ATTRIBUTES.getKey(attr).toString() }
				catch (err2) { try { attrId = attr.getDescriptionId() } catch (err3) {} }
				let op = 'ADDITION'
				try { op = String(mod.getOperation()) } catch (err2) {}
				out.push({ attribute: attrId, amount: round(mod.getAmount(), 4), operation: op })
			})
		} catch (err) {}
		return out
	}

	// pull the additive value of one attribute (matched on its last id segment, e.g. "armor",
	// "armor_toughness", "attack_damage") out of a modifier list
	const additive = (mods, key) => {
		let sum = 0
		mods.forEach(m => {
			var seg = m.attribute.substring(m.attribute.lastIndexOf('.') + 1)
			if (m.operation === 'ADDITION' && seg === key) sum += m.amount
		})
		return sum
	}

	// non-empty modifier sets across every slot (catches modded off-hand / armor bonuses on tools etc.)
	const SLOTS = EquipmentSlot.values()
	var allSlotModifiers = stack => {
		var out = {}
		for (let i = 0; i < SLOTS.length; i++) {
			var mods = modifiersForSlot(stack, SLOTS[i])
			if (mods.length) out[SLOTS[i].getName()] = mods
		}
		return out
	}

	// ── scan registry ────────────────────────────────────────────────────────
	const armorRows = []
	const toolRows  = []

	const keys = []
	ForgeRegistries.ITEMS.getKeys().forEach(rl => keys.push(rl))
	keys.sort((a, b) => (String(a) < String(b) ? -1 : String(a) > String(b) ? 1 : 0))

	keys.forEach(rl => {
		var id = String(rl)
		if (id === 'minecraft:air') return

		var item = ForgeRegistries.ITEMS.getValue(rl)
		if (!item) return

		var isArmor = item instanceof ArmorItem
		var isTool  = (item instanceof TieredItem) || (item instanceof DiggerItem)
		if (!isArmor && !isTool) return

		var ns = id.substring(0, id.indexOf(':'))

		let selected = false
		if (explicitIds[id]) selected = true
		if (isArmor && wantAllArmor) selected = true
		if (isTool && wantAllTools) selected = true
		if (modFilters.indexOf(ns) !== -1) selected = true
		if (!selected) return

		var stack = Item.of(id)

		let durability = 0
		try { durability = stack.getMaxDamage() } catch (err) {}

		let rarity = 'COMMON'
		try { rarity = String(stack.getRarity()) } catch (err) {}

		let fireResistant = false
		try { fireResistant = item.isFireResistant() } catch (err) {}

		let enchantValue = 0
		try { enchantValue = item.getEnchantmentValue(stack) } catch (err) {
			try { enchantValue = item.getEnchantmentValue() } catch (err2) {}
		}

		// ── ARMOR ────────────────────────────────────────────────────────────
		if (isArmor) {
			// derive the worn slot from whichever slot actually carries the armor modifiers
			// (mapping-independent; falls back to the Type / Forge accessors)
			var slotMods = allSlotModifiers(stack)
			var modSlots = Object.keys(slotMods)

			let type = null
			try { type = item.getType() } catch (err) {}

			let slotName = modSlots.length === 1 ? modSlots[0] : null
			if (!slotName) { try { if (type) slotName = type.getSlot().getName() } catch (err) {} }
			if (!slotName) { try { var s = item.getEquipmentSlot(stack); if (s) slotName = s.getName() } catch (err) {} }
			if (!slotName && modSlots.length) slotName = modSlots[0]
			if (!slotName) slotName = 'unknown'

			let piece = slotName
			try { if (type) piece = type.getName() } catch (err) {}

			let mat = null
			try { mat = item.getMaterial() } catch (err) {}

			let material = 'unknown'
			try { if (mat) material = String(mat.getName()) } catch (err) {}

			let defense = 0
			try { defense = item.getDefense() } catch (err) {}

			let toughness = 0
			try { toughness = item.getToughness() } catch (err) {}

			let knockbackRes = 0
			try { if (mat) knockbackRes = mat.getKnockbackResistance() } catch (err) {}

			let equipSound = ''
			try { if (mat) equipSound = mat.getEquipSound().getLocation().toString() } catch (err) {}

			let repair = []
			try { if (mat) repair = ingredientIds(mat.getRepairIngredient()) } catch (err) {}

			// what the game actually applies while worn
			var wornMods    = slotMods[slotName] || []
			var armorApplied     = additive(wornMods, 'armor') || defense
			var toughnessApplied = additive(wornMods, 'armor_toughness') || toughness
			var knockbackApplied = additive(wornMods, 'knockback_resistance') || knockbackRes

			armorRows.push({
				id: id,
				mod: ns,
				material: material,
				piece: piece,
				slot: slotName,
				armor: round(armorApplied, 3),
				toughness: round(toughnessApplied, 3),
				knockbackResistance: round(knockbackApplied, 3),
				durability: durability,
				enchantability: enchantValue,
				repairWith: repair.join(' | '),
				equipSound: equipSound,
				fireResistant: fireResistant,
				rarity: rarity,
				attributeModifiers: slotMods
			})
			return
		}

		// ── TOOLS / WEAPONS ─────────────────────────────────────────────────
		let toolClass = 'other'
		if (item instanceof SwordItem)        toolClass = 'sword'
		else if (item instanceof PickaxeItem) toolClass = 'pickaxe'
		else if (item instanceof AxeItem)     toolClass = 'axe'
		else if (item instanceof ShovelItem)  toolClass = 'shovel'
		else if (item instanceof HoeItem)     toolClass = 'hoe'
		else if (item instanceof DiggerItem)  toolClass = 'digger'

		let tier = null
		try { tier = item.getTier() } catch (err) {}

		let material = 'unknown'
		if (tier) {
			try {
				var sortName = TierSorting ? TierSorting.getName(tier) : null
				if (sortName) material = String(sortName)
			} catch (err) {}
			if (material === 'unknown') {
				try {
					material = tier.getClass().isEnum() ? String(tier).toLowerCase() : tier.getClass().getSimpleName()
				} catch (err) {}
			}
		}

		let tierUses = 0, tierSpeed = 0, tierAttackBonus = 0, tierLevel = -1, tierEnchant = 0
		let tierRepair = []
		if (tier) {
			try { tierUses        = tier.getUses() } catch (err) {}
			try { tierSpeed       = tier.getSpeed() } catch (err) {}
			try { tierAttackBonus = tier.getAttackDamageBonus() } catch (err) {}
			try { tierLevel       = tier.getLevel() } catch (err) {}
			try { tierEnchant     = tier.getEnchantmentValue() } catch (err) {}
			try { tierRepair      = ingredientIds(tier.getRepairIngredient()) } catch (err) {}
		}

		// what the game actually shows in the tooltip
		var mainMods    = modifiersForSlot(stack, EquipmentSlot.MAINHAND)
		var attackDamage = round(1 + additive(mainMods, 'attack_damage'), 3)   // +1 player base
		var attackSpeed  = round(4 + additive(mainMods, 'attack_speed'), 3)    // 4.0 player base
		var dps          = round(attackDamage * attackSpeed, 3)

		toolRows.push({
			id: id,
			mod: ns,
			class: toolClass,
			material: material,
			attackDamage: attackDamage,
			attackSpeed: attackSpeed,
			dps: dps,
			durability: durability,
			miningSpeed: round(tierSpeed, 3),
			harvestLevel: tierLevel,
			enchantability: enchantValue,
			repairWith: (tierRepair.length ? tierRepair : []).join(' | '),
			fireResistant: fireResistant,
			rarity: rarity,
			tier: {
				name: material,
				uses: tierUses,
				miningSpeed: round(tierSpeed, 3),
				attackDamageBonus: round(tierAttackBonus, 3),
				harvestLevel: tierLevel,
				enchantability: tierEnchant
			},
			attributeModifiers: allSlotModifiers(stack)
		})
	})

	// ── armor set totals (per material, for a full 4-piece set) ──────────────
	var setMap = {}
	armorRows.forEach(r => {
		var s = setMap[r.material] = setMap[r.material] || {
			material: r.material, mod: r.mod, pieces: 0,
			setArmor: 0, setToughness: 0, setKnockbackResistance: 0, setDurability: 0,
			byPiece: {}
		}
		s.pieces++
		s.setArmor                += r.armor
		s.setToughness            += r.toughness
		s.setKnockbackResistance  += r.knockbackResistance
		s.setDurability           += r.durability
		s.byPiece[r.piece] = { armor: r.armor, durability: r.durability }
	})
	var setRows = Object.keys(setMap).sort().map(k => {
		var s = setMap[k]
		s.setArmor               = round(s.setArmor, 3)
		s.setToughness           = round(s.setToughness, 3)
		s.setKnockbackResistance = round(s.setKnockbackResistance, 3)
		return s
	})

	// ── sort ────────────────────────────────────────────────────────────────
	armorRows.sort((a, b) => cmp(a.material, b.material) || cmp(slotOrder(a.slot), slotOrder(b.slot)) || cmp(a.id, b.id))
	toolRows.sort((a, b) => cmp(a.material, b.material) || cmp(a.class, b.class) || cmp(a.id, b.id))

	// ── write ───────────────────────────────────────────────────────────────
	// JSON first: this also creates kubejs/exported/gear/ (needed by the CSV writer)
	writeExportJson('kubejs/exported/gear/armor.json',      { count: armorRows.length, items: armorRows })
	writeExportJson('kubejs/exported/gear/armor_sets.json', { count: setRows.length,   sets: setRows })
	writeExportJson('kubejs/exported/gear/tools.json',      { count: toolRows.length,  items: toolRows })

	writeCsv('armor.csv', armorRows,
		['id', 'mod', 'material', 'piece', 'slot', 'armor', 'toughness', 'knockbackResistance',
		 'durability', 'enchantability', 'repairWith', 'equipSound', 'fireResistant', 'rarity'])

	writeCsv('armor_sets.csv', setRows,
		['material', 'mod', 'pieces', 'setArmor', 'setToughness', 'setKnockbackResistance', 'setDurability'])

	writeCsv('tools.csv', toolRows,
		['id', 'mod', 'class', 'material', 'attackDamage', 'attackSpeed', 'dps', 'durability',
		 'miningSpeed', 'harvestLevel', 'enchantability', 'repairWith', 'fireResistant', 'rarity'])

	console.log(`[dumpgear] ${armorRows.length} armor pieces / ${setRows.length} sets / ${toolRows.length} tools -> kubejs/exported/gear/`)
	return { armor: armorRows.length, sets: setRows.length, tools: toolRows.length }

	// ── local fns ───────────────────────────────────────────────────────────
	function cmp(a, b) { return a < b ? -1 : a > b ? 1 : 0 }
	function slotOrder(s) { return ({ head: 0, chest: 1, legs: 2, feet: 3 })[s] != null ? ({ head: 0, chest: 1, legs: 2, feet: 3 })[s] : 9 }

	// CsvIO (from the kubejs_csv mod) writes a JsonArray of flat objects; it does NOT quote
	// values, so flatten every field to a comma-free string first. Falls back to a JSON
	// sidecar if the CSV mod isn't present.
	function writeCsv(name, rows, cols) {
		var flat = rows.map(r => {
			var o = {}
			cols.forEach(c => {
				let v = r[c]
				v = (v === null || v === undefined) ? '' : String(v)
				o[c] = v.replace(/[\r\n]+/g, ' ').replace(/,/g, ';')
			})
			return o
		})
		try {
			CsvIO.write('kubejs/exported/gear/' + name, flat)
		} catch (err) {
			console.warn(`[dumpgear] CsvIO unavailable (${err}); writing ${name}.json instead`)
			writeExportJson('kubejs/exported/gear/' + name.replace(/\.csv$/, '') + '_flat.json', { rows: flat })
		}
	}
}
