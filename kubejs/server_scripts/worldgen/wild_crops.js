// Wild crops: which biomes each wild crop and fruit tree generates in.
//
// Edit kubejs/generation_scripts/worldgen/wild_crops.json, then rejoin the world (biome modifiers are read
// at world load, not on /reload). Only chunks generated afterwards change. Reference + tests:
// feature_writeup/crops.md section 2.8; resolved lists: feature_writeup/crops_by_biome.md
// (python3 tools/gen_crop_worldgen_table.py evaluates the same rules offline).
//
// Per crop key in the config:
//   - biome tag creativeengineering:wild_crops/<key> = the biomes its rule matches (ServerEvents.tags);
//   - each of the mods' own biome modifiers for that crop is replaced by forge:none, and one
//     creativeengineering:wild_crops/<key> forge:add_features modifier adds the crop's features to the tag
//     (ServerEvents.highPriorityData: KubeJS's top-priority virtual datapack, above the mod jars);
//   - remove_features (vanilla patches that Terralith's biome files list) go into one forge:remove_features
//     modifier over #minecraft:is_overworld. Forge removes after it adds, which is why those crops add the
//     pack's copies (creativeengineering:wild/*) instead of the original ids.
// A feature or modifier of a mod that isn't loaded is skipped: an unknown feature id stops the world loading.
//
// Rule matching is the same as tools/gen_crop_worldgen_table.py: clauses OR'ed, the traits in a clause
// AND'ed, '!trait' negated, then add / except; biomes marked ocean or cave never match.
//
// The tags handler can't check biome ids: KubeJS runs it once at script load as a "pre" pass (PreTagEventJS:
// add() only queues the action, totalAdded never moves, no registry yet) and replays the queued adds on the real
// tag load without calling this script again. So ServerEvents.loaded compares the live tags with the rules and
// lists overworld biomes the config doesn't classify.
//
// CEConfig [worldgen.systems] wildCrops = false: nothing here runs, every crop as its mod ships it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/wild_crops.json'
	var TAG_PREFIX = 'creativeengineering:wild_crops/'
	var MODIFIER_PREFIX = 'creativeengineering:forge/biome_modifier/wild_crops/'
	var NEVER = { ocean: true, cave: true }
	var LOG = '[wild-crops] '

	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	function enabled() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig').wildCropsEnabled() == true
		} catch (err) {
			return true   // older jar: no switch
		}
	}

	function readConfig() {
		if (!enabled()) {
			console.info(LOG + 'off in CEConfig [worldgen.systems] wildCrops: every crop spawns where its mod puts it')
			return null
		}
		try {
			return JSON.parse(String(JsonIO.readString(CONFIG_PATH)))
		} catch (err) {
			console.error(LOG + 'could not read ' + CONFIG_PATH + ', nothing changed: ' + err)
			return null
		}
	}

	function traitSets(cfg) {
		var out = {}
		Object.keys(cfg.biomes).forEach(function (b) {
			var set = {}
			String(cfg.biomes[b]).trim().split(/\s+/).forEach(function (t) { if (t) set[t] = true })
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

	// crop entry -> sorted biome ids
	function resolve(traits, crop) {
		var out = {}
		Object.keys(traits).forEach(function (b) {
			var tr = traits[b]
			for (var n in NEVER) if (tr[n]) return
			var where = crop.where || []
			for (var k = 0; k < where.length; k++) {
				if (matches(tr, where[k])) { out[b] = true; return }
			}
		})
		;(crop.add || []).forEach(function (b) { out[b] = true })
		;(crop.except || []).forEach(function (b) { delete out[b] })
		return Object.keys(out).sort()
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

	// typos in rules match nothing silently; flag tokens no biome carries
	function checkRules(cfg, traits) {
		var vocab = {}
		Object.keys(traits).forEach(function (b) { for (var t in traits[b]) vocab[t] = true })
		Object.keys(cfg.crops).forEach(function (key) {
			(cfg.crops[key].where || []).forEach(function (clause) {
				String(clause).trim().split(/\s+/).forEach(function (t) {
					var name = t.charAt(0) === '!' ? t.substring(1) : t
					if (name && !vocab[name]) console.warn(LOG + key + ': trait "' + name + '" is on no biome (typo?)')
				})
			})
		})
	}

	function modifierPath(id) {
		var s = String(id)
		return namespace(s) + ':forge/biome_modifier/' + s.substring(s.indexOf(':') + 1) + '.json'
	}

	ServerEvents.tags('worldgen/biome', function (event) {
		var cfg = readConfig()
		if (!cfg) return
		var traits = traitSets(cfg)
		Object.keys(cfg.crops).forEach(function (key) {
			var biomes = resolve(traits, cfg.crops[key])
			if (biomes.length) global.tags.add(event, TAG_PREFIX + key, biomes)
		})
	})

	ServerEvents.highPriorityData(function (event) {
		var cfg = readConfig()
		if (!cfg) return
		var traits = traitSets(cfg)
		checkRules(cfg, traits)
		var disabled = {}
		var removed = {}
		var added = 0
		var skipped = []
		Object.keys(cfg.crops).forEach(function (key) {
			var crop = cfg.crops[key]
			;(crop.disable_modifiers || []).forEach(function (id) {
				if (modLoaded(id)) disabled[id] = true
			})
			;(crop.remove_features || []).forEach(function (id) {
				if (modLoaded(id)) removed[id] = true
				else skipped.push(key + ': ' + id)
			})
			var features = (crop.features || []).filter(function (id) {
				if (modLoaded(id)) return true
				skipped.push(key + ': ' + id)
				return false
			})
			if (!features.length || !resolve(traits, crop).length) return
			event.add(MODIFIER_PREFIX + key + '.json', JSON.stringify({
				type: 'forge:add_features',
				biomes: '#' + TAG_PREFIX + key,
				features: features,
				step: 'vegetal_decoration',
			}))
			added++
		})
		Object.keys(disabled).forEach(function (id) {
			event.add(modifierPath(id), JSON.stringify({ type: 'forge:none' }))
		})
		var removeIds = Object.keys(removed).sort()
		if (removeIds.length) {
			event.add(MODIFIER_PREFIX + '_remove_vanilla_patches.json', JSON.stringify({
				type: 'forge:remove_features',
				biomes: '#minecraft:is_overworld',
				features: removeIds,
				steps: ['vegetal_decoration'],
			}))
		}
		skipped.forEach(function (s) { console.warn(LOG + 'mod not loaded, skipped ' + s) })
		console.info(LOG + added + ' crop modifiers added, ' + Object.keys(disabled).length + ' mod modifiers off, '
			+ removeIds.length + ' biome-file patches removed')
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
		var traits = traitSets(cfg)
		var reg
		var known
		var overworld
		try {
			reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
			known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
			overworld = tagMembers(reg, 'minecraft:is_overworld')
		} catch (err) {
			console.error(LOG + 'could not read the biome registry, wild crops unverified: ' + err)
			return
		}
		var pairs = 0
		var problems = []
		Object.keys(cfg.crops).forEach(function (key) {
			var members
			try {
				members = tagMembers(reg, TAG_PREFIX + key)
			} catch (err) {
				problems.push('could not read #' + TAG_PREFIX + key + ': ' + err)
				return
			}
			resolve(traits, cfg.crops[key]).forEach(function (b) {
				if (!known[b]) problems.push(key + ': no such biome ' + b)
				else if (!members[b]) problems.push(key + ': ' + b + ' is not in #' + TAG_PREFIX + key)
				else pairs++
			})
		})
		var unclassified = Object.keys(overworld).filter(function (b) { return !traits[b] }).sort()
		problems.forEach(function (p) { console.error(LOG + p) })
		if (unclassified.length) {
			console.warn(LOG + unclassified.length + ' overworld biomes are not in ' + CONFIG_PATH + ' and get no wild crops: '
				+ unclassified.join(', '))
		}
		console.info(LOG + pairs + ' crop/biome pairs in ' + Object.keys(cfg.crops).length + ' crop tags'
			+ (problems.length ? ', ' + problems.length + ' problems' : ''))
	})
})()
