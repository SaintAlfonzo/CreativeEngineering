// Creative Compat startup script (loaded from the Compat jar by the startup loader); moved from
// kubejs/startup_scripts/func/item_physics.js on 2026-11-01. Defines global.itemPhysics.
// ─────────────────────────────────────────────────────────────────────────────
//  Item Physics — fire / lava burning split + float control
// ─────────────────────────────────────────────────────────────────────────────
//
//  Backed by Creative Compat (creativecompat-<version>.jar in mods/); this script ships in that jar:
//    com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit
//    + mixin/ItemPhysicServerMixin
//
//  DEFAULT (config/itemphysic.json): every dropped item SINKS and is DESTROYED by
//  both open fire and lava.
//
//  ── The normal way to change behaviour: item tags ───────────────────────────
//  Membership updates on /reload (no restart). Populate them in
//  kubejs/data/creativecompat/kubejs/tags/item_physics_tags.js (or any datapack):
//
//    creativecompat:physics/floats     → floats in water
//    creativecompat:physics/sinks      → force-sink (wins over floats)
//    creativecompat:physics/fireproof  → survives open fire, still burns in lava
//    creativecompat:physics/lavaproof  → immune to BOTH fire and lava (wins over fireproof)
//
//  Creative Compat ships starter contents for these tags (netherite, boats, …).
//
//  ── This file: the programmatic API, for id-based one-offs ──────────────────
//  Startup-only — edit + restart to apply (a /reload will not re-run this).
//  global.itemPhysics.<fn>(...ids and/or '#tags'):
//
//    .float(...)  .sink(...)        add / remove float rules
//    .unburnable(...)               immune to fire AND lava   (same as the lavaproof tag)
//    .burnable(...)                 back to default
//    .fireOnly(...) .lavaOnly(...)  finer split — burns in only one of fire/lava
//
//  ── Burn-transform: leave something behind when fire/lava destroys a dropped item ──
//    .burnReplace(idOrTag, resultId [, chance=1] [, perItem=true])
//    .burnExplode(idOrTag [, power=3] [, chance=1] [, shrapnel=false])   power 4 ≈ TNT, no block damage
//    .burnExclude(...idOrTag)                             never transform (checked first)
//    .burnClear(idOrTag)
//  Backed by Creative Compat ItemBurnTransform + the discard() redirect in ItemPhysicServerMixin.
//  Bucket membership (#creativecompat:burn/*) is reload-able in the tags file.
// ─────────────────────────────────────────────────────────────────────────────

console.log('Item Physics (fire/lava split) starting')

;(function () {
	let SPLIT = null
	let BURN = null
	try {
		SPLIT = Java.loadClass('com.SaintAlfonzo.creativecompat.itemphysic.ItemPhysicSplit')
	} catch (err) {
		console.warn('[item_physics] Creative Compat ItemPhysicSplit not on classpath — API disabled: ' + err)
	}
	try {
		BURN = Java.loadClass('com.SaintAlfonzo.creativecompat.itemphysic.ItemBurnTransform')
	} catch (err) {
		console.warn('[item_physics] Creative Compat ItemBurnTransform not on classpath — burn-transform API disabled: ' + err)
	}

	// flatten an `arguments` object (strings and/or arrays of strings), warn on blanks
	var each = (args, fn) => {
		for (let n = 0; n < args.length; n++) {
			var a = args[n]
			if (Array.isArray(a)) { a.forEach(v => push(v, fn)); continue }
			push(a, fn)
		}
	}
	var push = (v, fn) => {
		if (v == null || !String(v).length) { console.warn('[item_physics] ignored empty/undefined rule arg'); return }
		fn(String(v))
	}
	var isTag = s => s.charAt(0) === '#'
	var bare  = s => (isTag(s) ? s.substring(1) : s)

	var api = {
		float() { if (!SPLIT) return; each(arguments, s => isTag(s) ? SPLIT.floatsTag(bare(s)) : SPLIT.floats(s)) },
		sink()  { if (!SPLIT) return; each(arguments, s => isTag(s) ? SPLIT.sinksTag(bare(s))  : SPLIT.sinks(s))  },

		unburnable() { this._burn('never', arguments) },
		burnable()   { this._burn('burn',  arguments) },
		fireOnly()   { this._burn('fire',  arguments) },
		lavaOnly()   { this._burn('lava',  arguments) },

		_burn(mode, args) {
			if (!SPLIT) return
			each(args, s => isTag(s) ? SPLIT.setBurnTag(bare(s), mode) : SPLIT.setBurn(s, mode))
		},

		// ── burn-transform: what a dropped item leaves behind when fire/lava destroys it ──
		// idOrTag: 'mod:item' or '#some:tag' (id wins over tag; tags matched in reg. order)
		//
		//   burnReplace(idOrTag, resultId [, chance=1] [, perItem=true])
		//     Drop resultId instead of vanishing. perItem=true rolls `chance` once for each
		//     item in the stack (a stack of 8 → up to 8 results); false rolls once total.
		//   burnExplode(idOrTag [, power=3] [, chance=1] [, shrapnel=false])
		//     Non-block-breaking explosion at the item (still hurts entities). power 4 ≈ TNT.
		//     shrapnel=true also fires a CEConfig.Shrapnel burst (shared with primed-TNT-entity
		//     detonations) from the item's position.
		burnReplace(idOrTag, resultId, chance, perItem) {
			if (!BURN || !idOrTag || !resultId) return
			BURN.replace(String(idOrTag), String(resultId),
				chance == null ? 1.0 : chance, perItem !== false)
		},
		burnExplode(idOrTag, power, chance, shrapnel) {
			if (!BURN || !idOrTag) return
			BURN.explode(String(idOrTag), power == null ? 3.0 : power, chance == null ? 1.0 : chance, !!shrapnel)
		},
		burnClear(idOrTag) { if (BURN && idOrTag) BURN.clear(String(idOrTag)) },

		//   burnExclude(...idOrTag)  — never transform these, even if a rule matches.
		//   Use it for the end products (dirty glass/pane/shard, ash) so re-dropping one
		//   in fire/lava just burns it away instead of re-running its own rule.
		burnExclude() {
			if (!BURN) return
			each(arguments, s => BURN.exclude(s))
		},

		reset() {
			if (SPLIT) SPLIT.reset()
			if (BURN)  BURN.reset()
		},
		summary() {
			return (SPLIT ? String(SPLIT.summary()) : '(split disabled)') +
				' | ' + (BURN ? String(BURN.summary()) : '(burn-transform disabled)')
		}
	}

	global.itemPhysics = api

	// ═════════════════════════════════════════════════════════════════════════
	//  Programmatic rules — cleared and re-applied on every launch.
	//  Prefer the item tags (creativecompat/kubejs/tags/item_physics_tags.js) for anything
	//  that isn't a hard-to-tag one-off.
	// ═════════════════════════════════════════════════════════════════════════
	api.reset()

	// (float / burn split — nothing here yet; everything lives in the tags)

	// ── Burn-transform rules ────────────────────────────────────────────────
	//  Fires when fire/lava would destroy the dropped item — in FIRE only if the item
	//  isn't in the physics/fireproof tag, otherwise LAVA only. Bucket membership
	//  (#forge:glass, #quark:shards, #creativecompat:burn/*) is reload-able in
	//  creativecompat/kubejs/tags/item_physics_tags.js; only the params below need a restart.

	// exclusions FIRST — the end products never re-transform when re-dropped
	api.burnExclude('quark:dirty_glass', 'quark:dirty_glass_pane', 'quark:dirty_shard', 'supplementaries:ash')

	api.burnReplace('#forge:glass',       'quark:dirty_glass',      1.0, true)   // glass block  -> dirty glass  (tinted_glass is in #forge:glass)
	api.burnReplace('#forge:glass_panes', 'quark:dirty_glass_pane', 1.0, true)   // glass pane   -> dirty glass pane
	api.burnReplace('#quark:shards',      'quark:dirty_shard',      1.0, true)   // glass shard  -> dirty shard
	api.burnReplace('#creativecompat:burn/to_ash', 'supplementaries:ash', 0.25, true) // wood -> ~1-in-4 ash per plank/log

	api.burnExplode('minecraft:tnt',          2.0, 1.0, true)   // shrapnel — see CEConfig.Shrapnel
	api.burnExplode('minecraft:tnt_minecart', 2.0, 1.0, true)
	api.burnExplode('minecraft:gunpowder',    1.0, 1.0)
	api.burnExplode('#creativecompat:burn/explosive', 1.0, 1.0)             // spare bucket for anything else

	console.log('[item_physics] ' + api.summary())
})()
