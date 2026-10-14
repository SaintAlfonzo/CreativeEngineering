// Mob spawn tuning: per-biome spawn weights, additions and removals.
//
// Edit kubejs/generation_scripts/worldgen/mob_spawns.json, then rejoin the world (biome modifiers are read at
// world load, not on /reload). Spawns change in existing worlds too, for mobs spawning from then on.
// Reference + tests: feature_writeup/mob_spawns.md; resolved tables: feature_writeup/spawns_by_biome.md
// (python3 tools/gen_spawn_table.py applies the same rules offline to the last /dumpspawns).
//
// Per rule in the config:
//   - biome tag creativeengineering:mob_spawns/<id> = the biomes it matches (ServerEvents.tags, via global.tags);
//   - every rule goes into ONE biome modifier, creativeengineering:forge/biome_modifier/mob_spawns.json, of
//     patcherMod's type creativeengineering:tune_spawns (worldgen/SpawnTuning), which runs the rules in file order
//     after every mod's modifiers (Forge phase AFTER_EVERYTHING). One modifier keeps the order deterministic;
//   - disable_modifiers: the mods' own biome modifiers, each replaced by forge:none.
// Per spawn condition (conditions list: light_only, water_only; patcherMod worldgen/SpawnConditions):
//   - biome tag creativeengineering:spawn_conditions/<id> when it names biomes (where/add/except, or 'rules': the
//     biomes those rules match); none = everywhere;
//   - written into the same modifier, which hands them to the spawn checks.
// spawn_ground: block tag -> entries added to it (ServerEvents.tags('block')), e.g. every dirt into
// #minecraft:animals_spawnable_on.
// Written in ServerEvents.highPriorityData (KubeJS's top-priority virtual datapack, above the mod jars).
//
// An older patcherMod jar without SpawnTuning would make the modifier type unknown, and an unknown type stops the
// world loading, so nothing is written then. A modifier id of a mod that isn't loaded is skipped.
//
// Selectors match tools/gen_spawn_table.py: 'where' clauses OR'ed, traits in a clause AND'ed, '!trait' negated,
// traits from wild_crops.json's biome table; then 'add' (ids, '#tags' as nested tag references) and 'except' (ids).
//
// The tags handler can't check biome ids (KubeJS runs it as a pre-pass without a registry, see wild_crops.js),
// so ServerEvents.loaded compares the live tags with the rules and logs SpawnTuning's summary of what changed.
//
// CEConfig [worldgen.systems] mobSpawns = false: nothing here runs, every mob spawns as its mod ships it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/mob_spawns.json'
	var TRAITS_PATH = 'kubejs/generation_scripts/worldgen/wild_crops.json'
	var TAG_PREFIX = 'creativeengineering:mob_spawns/'
	var COND_PREFIX = 'creativeengineering:spawn_conditions/'
	var CONDITIONS = { light_only: true, water_only: true }
	var MODIFIER_PATH = 'creativeengineering:forge/biome_modifier/mob_spawns.json'
	var TUNING_CLASS = 'com.SaintAlfonzo.creativeengineering.worldgen.SpawnTuning'
	var ACTIONS = { set: true, scale: true, remove: true }
	var CATEGORIES = { monster: true, creature: true, ambient: true, water_creature: true, water_ambient: true,
		underground_water_creature: true, axolotls: true, misc: true }
	var LOG = '[mob-spawns] '

	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	function enabled() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig').mobSpawnsEnabled() == true
		} catch (err) {
			return true   // jar without the switch: SpawnTuning is missing too, see tuningClass()
		}
	}

	function tuningClass() {
		try {
			return Java.loadClass(TUNING_CLASS)
		} catch (err) {
			return null
		}
	}

	function readJson(path) {
		return JSON.parse(String(JsonIO.readString(path)))
	}

	function readConfig() {
		if (!enabled()) {
			console.info(LOG + 'off in CEConfig [worldgen.systems] mobSpawns: every mob spawns as its mod ships it')
			return null
		}
		try {
			var cfg = readJson(CONFIG_PATH)
			cfg.rules = cfg.rules || []
			cfg.conditions = cfg.conditions || []
			cfg.spawn_ground = cfg.spawn_ground || {}
			cfg.traits = traitSets(readJson(TRAITS_PATH).biomes)
			return cfg
		} catch (err) {
			console.error(LOG + 'could not read ' + CONFIG_PATH + ' or ' + TRAITS_PATH + ', nothing changed: ' + err)
			return null
		}
	}

	function traitSets(biomes) {
		var out = {}
		Object.keys(biomes).forEach(function (b) {
			var set = {}
			String(biomes[b]).trim().split(/\s+/).forEach(function (t) { if (t) set[t] = true })
			out[b] = set
		})
		return out
	}

	function matches(traits, clause) {
		var toks = String(clause).trim().split(/\s+/)
		for (var k = 0; k < toks.length; k++) {
			var t = toks[k]
			if (!t) continue
			var want = t.charAt(0) !== '!'
			var name = want ? t : t.substring(1)
			if (!!traits[name] !== want) return false
		}
		return true
	}

	// rule -> { ids: sorted biome ids, tags: ['#ns:tag', ...] }
	function resolve(traits, rule) {
		var out = {}
		var tags = []
		Object.keys(traits).forEach(function (b) {
			var where = rule.where || []
			for (var k = 0; k < where.length; k++) {
				if (matches(traits[b], where[k])) { out[b] = true; return }
			}
		})
		;(rule.add || []).forEach(function (b) {
			var s = String(b)
			if (s.charAt(0) === '#') { if (tags.indexOf(s) < 0) tags.push(s) }
			else out[s] = true
		})
		;(rule.except || []).forEach(function (b) { delete out[String(b)] })
		return { ids: Object.keys(out).sort(), tags: tags }
	}

	function namespace(id) {
		var s = String(id)
		var c = s.indexOf(':')
		return c < 0 ? 'minecraft' : s.substring(0, c)
	}

	function modLoaded(id) {
		var ns = namespace(id)
		return ns === 'minecraft' || ns === 'creativeengineering' || Platform.isLoaded(ns)
	}

	// problems that make a rule unusable -> null when fine
	function invalid(rule, seen) {
		if (!rule.id || !/^[a-z0-9_]+$/.test(String(rule.id))) return 'id must be lowercase a-z 0-9 _'
		if (seen[rule.id]) return 'duplicate id'
		if (!rule.mob || String(rule.mob).indexOf(':') < 0) return 'mob must be an entity id'
		if (!ACTIONS[rule.action]) return 'action must be set, scale or remove'
		if (rule.action === 'set' && !(typeof rule.weight === 'number' && rule.weight >= 0)) return 'set needs a weight >= 0'
		if (rule.action === 'scale' && !(typeof rule.factor === 'number' && rule.factor >= 0)) return 'scale needs a factor >= 0'
		if (rule.category != null && !CATEGORIES[rule.category]) return 'unknown category ' + rule.category
		if (rule.min != null && !(rule.min >= 1)) return 'min must be 1 or more'
		if (rule.max != null && !(rule.max >= (rule.min != null ? rule.min : 1))) return 'max must be >= min'
		return null
	}

	// typos in selectors match nothing silently; flag tokens no biome carries and tags in 'except'
	function checkRule(rule, traits, vocab) {
		;(rule.where || []).forEach(function (clause) {
			String(clause).trim().split(/\s+/).forEach(function (t) {
				var name = t.charAt(0) === '!' ? t.substring(1) : t
				if (name && !vocab[name]) console.warn(LOG + rule.id + ': trait "' + name + '" is on no biome (typo?)')
			})
		})
		;(rule.except || []).forEach(function (b) {
			if (String(b).charAt(0) === '#') console.warn(LOG + rule.id + ': except takes biome ids, not tags (' + b + ' ignored)')
		})
	}

	// the usable rules, with their biome selection; logs the rest
	function usableRules(cfg, quiet) {
		var vocab = {}
		Object.keys(cfg.traits).forEach(function (b) { for (var t in cfg.traits[b]) vocab[t] = true })
		var seen = {}
		var out = []
		cfg.rules.forEach(function (rule, n) {
			var bad = invalid(rule, seen)
			if (bad) {
				if (!quiet) console.error(LOG + 'rule ' + (n + 1) + ' (' + (rule.id || 'no id') + '): ' + bad + ', skipped')
				return
			}
			seen[rule.id] = true
			if (!modLoaded(rule.mob)) {
				if (!quiet) console.warn(LOG + rule.id + ': mod not loaded, skipped ' + rule.mob)
				return
			}
			var sel = resolve(cfg.traits, rule)
			if (!sel.ids.length && !sel.tags.length) {
				if (!quiet) console.warn(LOG + rule.id + ': matches no biome, skipped')
				return
			}
			if (!quiet) checkRule(rule, cfg.traits, vocab)
			out.push({ rule: rule, sel: sel })
		})
		return out
	}

	function invalidCondition(c, seen, ruleIds) {
		if (!c.id || !/^[a-z0-9_]+$/.test(String(c.id))) return 'id must be lowercase a-z 0-9 _'
		if (seen[c.id]) return 'duplicate id'
		if (!CONDITIONS[c.condition]) return 'condition must be light_only or water_only'
		if (!Array.isArray(c.mobs) || !c.mobs.length) return 'mobs must be a list of entity ids'
		for (var k = 0; k < c.mobs.length; k++) {
			if (String(c.mobs[k]).indexOf(':') < 0) return 'mob ' + c.mobs[k] + ' is not an entity id'
		}
		var refs = c.rules || []
		for (var j = 0; j < refs.length; j++) {
			if (!ruleIds[refs[j]]) return 'rules: no usable rule ' + refs[j]
		}
		return null
	}

	function hasSelector(c) {
		return !!((c.where && c.where.length) || (c.add && c.add.length) || (c.rules && c.rules.length))
	}

	// the usable conditions: { cond, mobs: loaded mob ids, sel: null (everywhere) or { ids, tags } }; logs the rest
	function usableConditions(cfg, rules, quiet) {
		var ruleSel = {}
		rules.forEach(function (u) { ruleSel[u.rule.id] = u.sel })
		var seen = {}
		var out = []
		cfg.conditions.forEach(function (c, n) {
			var bad = invalidCondition(c, seen, ruleSel)
			if (bad) {
				if (!quiet) console.error(LOG + 'condition ' + (n + 1) + ' (' + (c.id || 'no id') + '): ' + bad + ', skipped')
				return
			}
			seen[c.id] = true
			var mobs = c.mobs.map(String).filter(function (m) {
				if (modLoaded(m)) return true
				if (!quiet) console.warn(LOG + c.id + ': mod not loaded, left out ' + m)
				return false
			})
			if (!mobs.length) return
			var sel = null
			if (hasSelector(c)) {
				var own = resolve(cfg.traits, c)
				var ids = {}
				var tags = own.tags.slice()
				own.ids.forEach(function (b) { ids[b] = true })
				;(c.rules || []).forEach(function (r) {
					ruleSel[r].ids.forEach(function (b) { ids[b] = true })
					ruleSel[r].tags.forEach(function (t) { if (tags.indexOf(t) < 0) tags.push(t) })
				})
				;(c.except || []).forEach(function (b) { delete ids[String(b)] })
				sel = { ids: Object.keys(ids).sort(), tags: tags }
				if (!sel.ids.length && !sel.tags.length) {
					if (!quiet) console.warn(LOG + c.id + ': matches no biome, skipped')
					return
				}
			}
			out.push({ cond: c, mobs: mobs, sel: sel })
		})
		return out
	}

	// JSON with JS-style numbers (JSON.stringify is Gson-backed here and writes 8 as "8.0")
	function toJson(v) {
		if (v === null) return 'null'
		if (Array.isArray(v)) return '[' + v.map(toJson).join(',') + ']'
		switch (typeof v) {
			case 'number':
				if (!isFinite(v)) throw new Error('cannot write non-finite number ' + v)
				return String(v)
			case 'boolean':
				return String(v)
			case 'string':
				return JSON.stringify(v)
			case 'object':
				return '{' + Object.keys(v).filter(function (k) { return v[k] !== undefined })
					.map(function (k) { return JSON.stringify(k) + ':' + toJson(v[k]) }).join(',') + '}'
		}
		throw new Error('cannot write ' + typeof v + ' as JSON')
	}

	function modifierPath(id) {
		var s = String(id)
		return namespace(s) + ':forge/biome_modifier/' + s.substring(s.indexOf(':') + 1) + '.json'
	}

	ServerEvents.tags('worldgen/biome', function (event) {
		var cfg = readConfig()
		if (!cfg || !tuningClass()) return
		var rules = usableRules(cfg, true)
		rules.forEach(function (u) {
			var entries = u.sel.ids.concat(u.sel.tags)
			if (entries.length) global.tags.add(event, TAG_PREFIX + u.rule.id, entries)
		})
		usableConditions(cfg, rules, true).forEach(function (u) {
			if (u.sel) global.tags.add(event, COND_PREFIX + u.cond.id, u.sel.ids.concat(u.sel.tags))
		})
	})

	// Ground blocks for spawn rules. Plain block tags, but patcherMod's walk-value mixins (same jar as SpawnTuning)
	// must read them too, or night spawns on the new blocks fail; so an old jar gets none.
	ServerEvents.tags('block', function (event) {
		var cfg = readConfig()
		if (!cfg || !tuningClass()) return
		Object.keys(cfg.spawn_ground).forEach(function (tag) {
			if (tag.charAt(0) === '_') return
			var entries = (cfg.spawn_ground[tag] || []).map(String).filter(function (e) {
				return modLoaded(e.charAt(0) === '#' ? e.substring(1) : e)
			})
			if (entries.length) global.tags.add(event, tag, entries)
		})
	})

	ServerEvents.highPriorityData(function (event) {
		var cfg = readConfig()
		if (!cfg) return
		if (!tuningClass()) {
			if (cfg.rules.length || cfg.conditions.length) console.error(LOG + 'patcherMod has no ' + TUNING_CLASS
				+ ' (old jar): ' + cfg.rules.length + ' rules and ' + cfg.conditions.length
				+ ' conditions NOT applied. Install patcherMod 1.0.5 from development/patcherMod.')
			return
		}
		var rules = usableRules(cfg, false)
		var disabled = {}
		var out = rules.map(function (u) {
			var r = u.rule
			;(r.disable_modifiers || []).forEach(function (id) {
				if (modLoaded(id)) disabled[id] = true
			})
			return {
				id: r.id,
				biomes: '#' + TAG_PREFIX + r.id,
				mob: String(r.mob),
				action: r.action,
				category: r.category,
				weight: r.action === 'set' ? r.weight : undefined,
				factor: r.action === 'scale' ? r.factor : undefined,
				min: r.action === 'set' ? r.min : undefined,
				max: r.action === 'set' ? r.max : undefined,
			}
		})
		var conds = usableConditions(cfg, rules, false).map(function (u) {
			return {
				id: u.cond.id,
				condition: u.cond.condition,
				mobs: u.mobs,
				biomes: u.sel ? '#' + COND_PREFIX + u.cond.id : undefined,
			}
		})
		if (out.length || conds.length) {
			event.add(MODIFIER_PATH, toJson({ type: 'creativeengineering:tune_spawns', rules: out, conditions: conds }))
		}
		Object.keys(disabled).forEach(function (id) {
			event.add(modifierPath(id), toJson({ type: 'forge:none' }))
		})
		console.info(LOG + out.length + ' of ' + cfg.rules.length + ' rules and ' + conds.length + ' of '
			+ cfg.conditions.length + ' conditions in ' + MODIFIER_PATH + ', ' + Object.keys(disabled).length + ' mod modifiers off')
	})

	function idSet(iterable, toId) {
		var out = {}
		var it = iterable.iterator()
		while (it.hasNext()) {
			var id = toId(it.next())
			if (id) out[id] = true
		}
		return out
	}

	function holderId(holder) {
		var key = holder.unwrapKey()
		return key.isPresent() ? String(key.get().location().toString()) : null
	}

	function tagMembers(reg, id) {
		return idSet(reg.getTagOrEmpty($TagKey.create($Registry.BIOME_REGISTRY, new $ResourceLocation(id))), holderId)
	}

	ServerEvents.loaded(function (event) {
		var cfg = readConfig()
		if (!cfg) return
		var tuning = tuningClass()
		if (!tuning) return
		var reg
		var known
		try {
			reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
			known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
		} catch (err) {
			console.error(LOG + 'could not read the biome registry, mob spawn rules unverified: ' + err)
			return
		}
		var pairs = 0
		var problems = []
		usableRules(cfg, true).forEach(function (u) {
			var members
			try {
				members = tagMembers(reg, TAG_PREFIX + u.rule.id)
			} catch (err) {
				problems.push('could not read #' + TAG_PREFIX + u.rule.id + ': ' + err)
				return
			}
			u.sel.ids.forEach(function (b) {
				if (!known[b]) problems.push(u.rule.id + ': no such biome ' + b)
				else if (!members[b]) problems.push(u.rule.id + ': ' + b + ' is not in #' + TAG_PREFIX + u.rule.id)
				else pairs++
			})
		})
		usableConditions(cfg, usableRules(cfg, true), true).forEach(function (u) {
			if (!u.sel) return
			var members
			try {
				members = tagMembers(reg, COND_PREFIX + u.cond.id)
			} catch (err) {
				problems.push('could not read #' + COND_PREFIX + u.cond.id + ': ' + err)
				return
			}
			u.sel.ids.forEach(function (b) {
				if (!known[b]) problems.push(u.cond.id + ': no such biome ' + b)
				else if (!members[b]) problems.push(u.cond.id + ': ' + b + ' is not in #' + COND_PREFIX + u.cond.id)
			})
		})
		problems.forEach(function (p) { console.error(LOG + p) })
		console.info(LOG + pairs + ' rule/biome pairs checked' + (problems.length ? ', ' + problems.length + ' problems' : '')
			+ '; ' + String(tuning.summary()))
	})
})()
