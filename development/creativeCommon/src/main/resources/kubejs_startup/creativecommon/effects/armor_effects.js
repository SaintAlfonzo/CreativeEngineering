// priority: -100
// Creative Common since 2026-11-06 (plan_cb_polish.md 2: the effect frameworks live in Common, the rows with the jar
// that owns the gear: Creative Equipment's kubejs_startup/creativeequipment/gear_effects.js). Was Creative Brewing's,
// before that the pack's startup_scripts/func/armor_effects.js. Common rebuild + restart.
//   ^ load after _lib/item_dict.js (priority 0). KubeJS startup-script order is
//   directory-walk order, not alphabetical, and func/ walks before _lib/ — so
//   without this, `global.item` is undefined when the set bonuses below read i.*
//   (same gotcha as tune_tools.js / tune_armor.js — see [[tune-tools-func]]).
// ─────────────────────────────────────────────────────────────────────────────
//  Armor Effects — generic "grant potion effects while worn" framework, styled
//  after Create: Deep Dark's echo-armor set-bonus tooltip (see patcherMod's
//  EchoArmorTooltipMixin / EchoArmorEffectProcedureMixin — CDD's own version is
//  hardcoded Java against ITS OWN items; this is the from-scratch KubeJS
//  equivalent for OUR OWN and/or vanilla/modded armor).
//
//  This file only holds the DATA + registration API (a startup func, per the
//  centralize-recipes-tags-items convention). The two consumers live where their
//  event actually runs:
//    Common client effects/armor_effects_tooltip.js  — ItemEvents.tooltip, builds the lines
//    Common server effects/armor_effects.js    — PlayerEvents.tick, applies the effects
//
//  global.armorEffects.piece(itemId, effects)
//    Effects apply whenever THIS ONE item is worn (any armor slot it fits),
//    independent of what else is worn. Tooltip header: "§7When worn:"
//
//  global.armorEffects.set(itemIds, effects)
//    Effects apply ONLY when EVERY item in itemIds is worn simultaneously (a full
//    set — itemIds should be one item per slot, e.g. [helmet, chest, legs, boots]).
//    Tooltip is added to EVERY item in itemIds, header "§7With full set on:" — same
//    wording CDD's own echo-armor set bonus uses (see EchoArmorTooltipMixin).
//
//  global.armorEffects.mount(itemId, effects)
//    Same shape/semantics as piece(), but for equipment worn by a non-player mob via
//    ITS OWN inventory slot rather than a LivingEntity armor slot — e.g. horse armor
//    (HorseArmorItem, worn by AbstractHorse in the vanilla saddle/armor Container, not
//    EquipmentSlot.HEAD/CHEST/LEGS/FEET; see tune_armor.js's own comment on
//    dragon_horse_armor not being an ArmorItem at all). piece()/set() are read by
//    Common server effects/armor_effects.js's PlayerEvents.tick, which only ever looks
//    at player.getItemBySlot(HEAD/CHEST/LEGS/FEET) — it would never see a mount's
//    armor slot, so mount() effects are applied by a separate LevelEvents.tick handler
//    in that same file that walks AbstractHorse entities instead. Tooltip rendering is
//    identical to piece() (Common client effects/armor_effects_tooltip.js just also loops
//    MOUNTS), since the tooltip doesn't care who ends up wearing the item.
//
//  effects: array of up to 7 entries (MAX_EFFECTS) —
//    { effect: 'namespace:id', amplifier?: 0, name?: 'Display Name', color?: 'a' }
//    - effect:    a MobEffect id (vanilla or modded). Resolved LAZILY on first actual
//                 use (tooltip render / tick), never at registration time — KubeJS
//                 startup scripts run before other mods' MobEffect registries are
//                 populated (see potions.js's own header comment on this exact
//                 problem; potion_area.js's mobEffect() helper uses the same lazy
//                 pattern), so an eager lookup here would silently fail for any
//                 modded effect.
//    - amplifier: 0 = no roman numeral shown (vanilla convention), 1 = "II", etc.
//    - name:      display name for the tooltip line. There's no client-safe way to
//                 resolve an arbitrary MobEffect's translated name at the point
//                 this framework needs it, so this isn't auto-derived — omit it and
//                 a naive Title Case of the effect id's path is used instead, but an
//                 explicit name is recommended (matches vanilla's actual wording).
//    - color:     single legacy formatting code letter for the name (e.g. 'a' green,
//                 'c' red, 'b' aqua — same codes as EchoArmorTooltipMixin's
//                 "§aResistance II" / echo_tools.js's "§cWeakness II"). Defaults to
//                 'a' (green) for BENEFICIAL/NEUTRAL effects and 'c' (red) for
//                 HARMFUL ones (MobEffect#getCategory()).
//
//  Applied every tick while worn as a short, continuously-refreshed effect — the
//  same "(infinite)" idiom CDD's own echo-armor bonus uses. Max 7 effects per
//  piece()/set() call (extras are dropped with a console warning).
//
//  Tooltip lines are pre-formatted raw strings ("§7- §aNight Vision §7(infinite)",
//  identical style to EchoArmorTooltipMixin/echo_tools.js), built by
//  _buildTooltipLines() below and fed straight into ItemEvents.tooltip's e.add(id,
//  [...]) (which accepts raw strings directly, not just creativecommon client _lib/tooltips.js's
//  own {text,color} object convention — that convention can't do the two-tone
//  "gray dash + colored name + gray duration" single line CDD uses, so this
//  framework intentionally uses raw strings instead, same as echo_tools.js).
// ─────────────────────────────────────────────────────────────────────────────

console.log('Armor Effects framework loading')

;(function () {
	const $ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	const $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
	const $MobEffectCategory = Java.loadClass('net.minecraft.world.effect.MobEffectCategory')

	const MAX_EFFECTS = 7
	// index 0 = level I (amplifier 0 - never shown, vanilla convention), so ROMAN[amplifier]
	// directly gives the displayed numeral for amplifier >= 1.
	const ROMAN = ['I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X']

	// itemId -> effects[]                         (from .piece())
	const PIECES = {}
	// [{ ids: [...], effects: [...] }, ...]        (from .set())
	const SETS = []
	// itemId -> effects[]                         (from .mount())
	const MOUNTS = {}

	// effect id -> resolved MobEffect (or null if confirmed missing) — populated lazily,
	// never at registration time. See header comment on why.
	const RESOLVED = {}

	function resolveEffect(id) {
		if (id in RESOLVED) return RESOLVED[id]
		var me = null
		try { me = $ForgeRegistries.MOB_EFFECTS.getValue(new $ResourceLocation(id)) } catch (err) {}
		if (!me) console.warn('[armorEffects] effect "' + id + '" is not registered (bad id, or that mod isn\'t installed) - skipped')
		RESOLVED[id] = me
		return me
	}

	function titleCase(path) {
		return String(path).split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')
	}

	function amplifierSuffix(amp) {
		amp = amp || 0
		if (amp <= 0) return ''
		return ' ' + (ROMAN[amp] || String(amp + 1))
	}

	// Resolves a spec's display name + color code, without requiring the caller to
	// have specified either. Safe to call any time (lazy Java lookups only).
	function describe(spec) {
		var name = spec.name || titleCase(String(spec.effect).split(':').pop())
		var color = spec.color
		if (!color) {
			color = 'a' // default (green) if the effect can't be resolved yet (e.g. tooltip built before world load)
			var me = resolveEffect(spec.effect)
			if (me) {
				try { color = (me.getCategory() === $MobEffectCategory.HARMFUL) ? 'c' : 'a' } catch (err) {}
			}
		}
		return { name: name + amplifierSuffix(spec.amplifier), color: color }
	}

	// Builds the full raw-string tooltip block for one header + its effects list,
	// e.g. ['§7When worn:', '§7- §aNight Vision §7(infinite)', ...]. Used by
	// Common client effects/armor_effects_tooltip.js — kept here so the exact formatting
	// (matching EchoArmorTooltipMixin) lives in one place.
	function buildTooltipLines(header, effects) {
		var lines = ['§7' + header + ':']
		effects.forEach(spec => {
			var d = describe(spec)
			lines.push('§7- §' + d.color + d.name + ' §7(infinite)')
		})
		return lines
	}

	function clampEffects(context, effects) {
		effects = effects || []
		if (effects.length > MAX_EFFECTS) {
			console.warn('[armorEffects] ' + context + ': ' + effects.length + ' effects given, only the first ' + MAX_EFFECTS + ' are used')
			effects = effects.slice(0, MAX_EFFECTS)
		}
		return effects
	}

	global.armorEffects = {
		MAX_EFFECTS: MAX_EFFECTS,
		PIECES: PIECES, // read by Common client effects/armor_effects_tooltip.js + Common server effects/armor_effects.js
		SETS: SETS,
		MOUNTS: MOUNTS, // read by Common client effects/armor_effects_tooltip.js + Common server effects/armor_effects.js

		piece(itemId, effects) {
			if (!itemId) { console.warn('[armorEffects] piece() called without an item id'); return }
			PIECES[itemId] = clampEffects('piece ' + itemId, effects)
		},

		set(itemIds, effects) {
			if (!itemIds || !itemIds.length) { console.warn('[armorEffects] set() called without item ids'); return }
			SETS.push({ ids: itemIds.slice(), effects: clampEffects('set [' + itemIds.join(', ') + ']', effects) })
		},

		mount(itemId, effects) {
			if (!itemId) { console.warn('[armorEffects] mount() called without an item id'); return }
			MOUNTS[itemId] = clampEffects('mount ' + itemId, effects)
		},

		// internals, exposed for the tooltip/tick consumers - not meant to be called directly
		_resolveEffect: resolveEffect,
		_describe: describe,
		_buildTooltipLines: buildTooltipLines,
	}
})()
