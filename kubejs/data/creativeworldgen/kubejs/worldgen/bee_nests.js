// requires: creativeworldgen
// Bee nest tuning: per-biome nest chance on generated trees, plus extra bee-nest trees.
//
// Edit kubejs/generation_scripts/worldgen/bee_nests.json, then rejoin the world (biome modifiers and worldgen
// features are read at world load, not on /reload). Only chunks generated afterwards change.
// Reference + tests: feature_writeup/bee_nests.md; per-biome before/after: feature_writeup/bee_nests_by_biome.md
// (python3 tools/gen_bee_nests.py applies the same rules offline, and reads the last /dumpbeenests).
//
// Per rule in the config:
//   - biome tag creativeworldgen:bee_nests/<id> = the biomes it matches (ServerEvents.tags, via global.tags);
//   - chance / scale go into ONE biome modifier, creativeworldgen:forge/biome_modifier/bee_nests.json, of
//     patcherMod's type creativeworldgen:bee_nests (worldgen/BeeNests): the rules fold in file order per biome,
//     and every tree generated there gets the result (TreeFeatureBeeNestsMixin). One modifier keeps the order;
//   - trees: a placed feature creativeworldgen:bee_nests/<id> (the configured tree at the rate, placed like a
//     vanilla tree: on dry ground where the sapling would survive) and a forge:add_features modifier
//     creativeworldgen:forge/biome_modifier/bee_nests/<id>.json, step vegetal_decoration.
// Written in ServerEvents.highPriorityData (KubeJS's top-priority virtual datapack, above the mod jars).
//
// A tree feature of a mod that isn't loaded is skipped (an unknown feature id stops the world loading).
//
// Selectors match tools/gen_bee_nests.py and the wild crop rules: 'where' clauses OR'ed, traits in a clause AND'ed,
// '!trait' negated, traits from biome_traits.json, biomes marked ocean or cave never match a clause;
// then 'add' (ids, '#tags' as nested tag references) and 'except' (ids).
//
// ServerEvents.loaded compares the live tags with the rules and logs BeeNests' summary.
//
// creativeworldgen-common.toml [worldgen.systems] beeNests = false: nothing here runs, nests as the mods ship them.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/bee_nests.json'
	var TRAITS_PATH = 'kubejs/generation_scripts/worldgen/biome_traits.json'
	var TAG_PREFIX = 'creativeworldgen:bee_nests/'
	var MODIFIER_PATH = 'creativeworldgen:forge/biome_modifier/bee_nests.json'
	var NESTS_CLASS = 'com.SaintAlfonzo.creativeworldgen.worldgen.BeeNests'
	var NEVER = ['ocean', 'cave']
	var DEFAULT_SAPLING = 'minecraft:oak_sapling'
	var LOG = '[bee-nests] '

	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	var $BeeNests = Java.loadClass(NESTS_CLASS)

	function enabled() {
		return Java.loadClass('com.SaintAlfonzo.creativeworldgen.worldgen.WorldgenConfig').beeNestsEnabled() == true
	}

	function readJson(path) {
		return JSON.parse(String(global.genText(path)))
	}

	function readConfig() {
		if (!enabled()) {
			console.info(LOG + 'off in creativeworldgen-common.toml [worldgen.systems] beeNests: nests as the mods ship them')
			return null
		}
		try {
			var cfg = readJson(CONFIG_PATH)
			cfg.rules = cfg.rules || []
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
			for (var n = 0; n < NEVER.length; n++) if (traits[b][NEVER[n]]) return
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
		return ns === 'minecraft' || ns === 'creativeworldgen' || Platform.isLoaded(ns)
	}

	// '1/16' | '3' | 0.5 -> trees per chunk
	function parseRate(r) {
		if (typeof r === 'number') return r
		var s = String(r).trim()
		var slash = s.indexOf('/')
		return slash < 0 ? Number(s) : Number(s.substring(0, slash)) / Number(s.substring(slash + 1))
	}

	function isNumber(v, lo, hi) {
		return typeof v === 'number' && isFinite(v) && v >= lo && v <= hi
	}

	// problems that make a rule unusable -> null when fine
	function invalid(rule, seen) {
		if (!rule.id || !/^[a-z0-9_]+$/.test(String(rule.id))) return 'id must be lowercase a-z 0-9 _'
		if (seen[rule.id]) return 'duplicate id'
		if (rule.chance == null && rule.scale == null && rule.trees == null) return 'needs chance, scale or trees'
		if (rule.chance != null && !isNumber(rule.chance, 0, 1)) return 'chance must be a number from 0 to 1'
		if (rule.scale != null && !isNumber(rule.scale, 0, 1000)) return 'scale must be a number from 0 to 1000'
		if (rule.trees != null) {
			var t = rule.trees
			if (!t.feature || String(t.feature).indexOf(':') < 0) return 'trees.feature must be a configured feature id'
			var r = parseRate(t.rate)
			if (!(isFinite(r) && r > 0 && r <= 64)) return 'trees.rate must be above 0 and at most 64 per chunk'
			if (t.sapling != null && String(t.sapling).indexOf(':') < 0) return 'trees.sapling must be a block id'
		}
		return null
	}

	// typos in selectors match nothing silently; flag tokens no biome carries and tags in 'except'
	function checkRule(rule, vocab) {
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

	// the usable rules, with their biome selection and whether their trees can be written; logs the rest
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
			if (!quiet) checkRule(rule, vocab)
			var sel = resolve(cfg.traits, rule)
			if (!sel.ids.length && !sel.tags.length) {
				if (!quiet) console.warn(LOG + rule.id + ': matches no biome, skipped')
				return
			}
			var trees = rule.trees != null
			if (trees && !(modLoaded(rule.trees.feature) && modLoaded(rule.trees.sapling || DEFAULT_SAPLING))) {
				if (!quiet) console.warn(LOG + rule.id + ': mod not loaded, trees skipped (' + rule.trees.feature + ')')
				trees = false
			}
			out.push({ rule: rule, sel: sel, trees: trees })
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

	// Math.round, as the generator's js_round
	function treePlacement(rule) {
		var rate = parseRate(rule.trees.rate)
		var first = rate >= 1
			? { type: 'minecraft:count', count: Math.round(rate) }
			: { type: 'minecraft:rarity_filter', chance: Math.max(1, Math.round(1 / rate)) }
		return [
			first,
			{ type: 'minecraft:in_square' },
			{ type: 'minecraft:surface_water_depth_filter', max_water_depth: 0 },
			{ type: 'minecraft:heightmap', heightmap: 'OCEAN_FLOOR' },
			{ type: 'minecraft:biome' },
			{ type: 'minecraft:block_predicate_filter', predicate: { type: 'minecraft:would_survive',
				state: { Name: String(rule.trees.sapling || DEFAULT_SAPLING), Properties: { stage: '0' } } } },
		]
	}

	ServerEvents.tags('worldgen/biome', function (event) {
		var cfg = readConfig()
		if (!cfg) return
		usableRules(cfg, true).forEach(function (u) {
			var entries = u.sel.ids.concat(u.sel.tags)
			if (entries.length) global.tags.add(event, TAG_PREFIX + u.rule.id, entries)
		})
	})

	ServerEvents.highPriorityData(function (event) {
		var cfg = readConfig()
		if (!cfg) return
		var rules = usableRules(cfg, false)
		var tuned = rules.filter(function (u) { return u.rule.chance != null || u.rule.scale != null })
		var out = tuned.map(function (u) {
			return { id: u.rule.id, biomes: '#' + TAG_PREFIX + u.rule.id, chance: u.rule.chance, scale: u.rule.scale }
		})
		if (out.length) event.add(MODIFIER_PATH, toJson({ type: 'creativeworldgen:bee_nests', rules: out }))
		var trees = 0
		rules.forEach(function (u) {
			if (!u.trees) return
			var id = TAG_PREFIX + u.rule.id
			event.add('creativeworldgen:worldgen/placed_feature/bee_nests/' + u.rule.id + '.json',
				toJson({ feature: String(u.rule.trees.feature), placement: treePlacement(u.rule) }))
			event.add('creativeworldgen:forge/biome_modifier/bee_nests/' + u.rule.id + '.json',
				toJson({ type: 'forge:add_features', biomes: '#' + id, features: id, step: 'vegetal_decoration' }))
			trees++
		})
		console.info(LOG + out.length + ' chance/scale rules in ' + MODIFIER_PATH + ', ' + trees + ' bee-tree features, '
			+ cfg.rules.length + ' rules in the config')
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

	ServerEvents.loaded(function (event) {
		var cfg = readConfig()
		if (!cfg) return
		var reg
		var known
		try {
			reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
			known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
		} catch (err) {
			console.error(LOG + 'could not read the biome registry, bee nest rules unverified: ' + err)
			return
		}
		var pairs = 0
		var problems = []
		usableRules(cfg, true).forEach(function (u) {
			var members
			try {
				members = idSet(reg.getTagOrEmpty($TagKey.create($Registry.BIOME_REGISTRY,
					new $ResourceLocation(TAG_PREFIX + u.rule.id))), holderId)
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
		problems.forEach(function (p) { console.error(LOG + p) })
		console.info(LOG + pairs + ' rule/biome pairs checked' + (problems.length ? ', ' + problems.length + ' problems' : '')
			+ '; ' + String($BeeNests.summary()))
	})
})()
