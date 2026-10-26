// priority: 1
// Shared machinery of the biome-rule worldgen systems: mushroom_spawns.js (mushroom_spawns.json) and Creative Delight's
// wild crops (kubejs/data/creative_delight/kubejs/_worldgen/wild_crops.js, shipped in the CD jar). Both read the biome
// words of biome_traits.json, match rules with the same clause rules, and write the same three kinds of data.
// global.biomeRules.system(opts) returns the three handlers ({ tags, data, loaded }) a system script hands to ServerEvents.
//
// Per rule key in the config:
//   - biome tag <tagPrefix><key> = the biomes its rule matches (ServerEvents.tags);
//   - each of the mods' own biome modifiers in disable_modifiers is replaced by forge:none, and one
//     <modifierPrefix><key> forge:add_features modifier adds the rule's features to the tag at the rule's step
//     (ServerEvents.highPriorityData: KubeJS's top-priority virtual datapack, above the mod jars);
//   - remove_features (vanilla patches that Terralith's biome files list) go into one forge:remove_features
//     modifier over #minecraft:is_overworld. Forge removes after it adds, which is why those rules add the
//     copies (creative_delight:wild/*) instead of the original ids.
// A feature or modifier of a mod that isn't loaded is skipped: an unknown feature id stops the world loading.
//
// Rule matching is the same as tools/gen_crop_worldgen_table.py: clauses OR'ed, the traits in a clause AND'ed,
// '!trait' negated, then add / except; biomes carrying a trait in opts.never(rule) never match.
//
// The tags handler can't check biome ids: KubeJS runs it once at script load as a "pre" pass (PreTagEventJS:
// add() only queues the action, totalAdded never moves, no registry yet) and replays the queued adds on the real
// tag load without calling the script again. So the loaded handler compares the live tags with the rules.
//
// opts: name (log prefix word), configPath, biomesPath (optional: read "biomes" from this file instead),
//   readConfig() (optional: returns the config's JSON text, or null for none; configPath then only names it in logs),
//   rulesKey ('crops' / 'rules'), tagPrefix, modifierPrefix, enabled() (CEConfig switch), offMessage,
//   never(rule) -> { trait: true }, step(rule) -> decoration step, ruleWord (log noun), reportUnclassified.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

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

	// rule entry -> sorted biome ids
	function resolve(traits, rule, never) {
		var out = {}
		Object.keys(traits).forEach(function (b) {
			var tr = traits[b]
			for (var n in never) if (tr[n]) return
			var where = rule.where || []
			for (var k = 0; k < where.length; k++) {
				if (matches(tr, where[k])) { out[b] = true; return }
			}
		})
		;(rule.add || []).forEach(function (b) { out[b] = true })
		;(rule.except || []).forEach(function (b) { delete out[b] })
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

	function modifierPath(id) {
		var s = String(id)
		return namespace(s) + ':forge/biome_modifier/' + s.substring(s.indexOf(':') + 1) + '.json'
	}

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

	function system(opts) {
		var LOG = '[' + opts.name + '] '
		var word = opts.ruleWord || 'rule'

		function readConfig() {
			if (!opts.enabled()) {
				console.info(LOG + opts.offMessage)
				return null
			}
			var cfg
			try {
				cfg = JSON.parse(String(opts.readConfig ? opts.readConfig() : JsonIO.readString(opts.configPath)))
				if (!cfg) throw 'no such file'
				if (opts.biomesPath) cfg.biomes = JSON.parse(String(JsonIO.readString(opts.biomesPath))).biomes
			} catch (err) {
				console.error(LOG + 'could not read ' + opts.configPath + (opts.biomesPath ? ' or ' + opts.biomesPath : '')
					+ ', nothing changed: ' + err)
				return null
			}
			return { cfg: cfg, rules: cfg[opts.rulesKey] || {}, traits: traitSets(cfg.biomes) }
		}

		function biomesOf(c, key) {
			return resolve(c.traits, c.rules[key], opts.never(c.rules[key]))
		}

		// typos in rules match nothing silently; flag tokens no biome carries
		function checkRules(c) {
			var vocab = {}
			Object.keys(c.traits).forEach(function (b) { for (var t in c.traits[b]) vocab[t] = true })
			Object.keys(c.rules).forEach(function (key) {
				(c.rules[key].where || []).forEach(function (clause) {
					String(clause).trim().split(/\s+/).forEach(function (t) {
						var name = t.charAt(0) === '!' ? t.substring(1) : t
						if (name && !vocab[name]) console.warn(LOG + key + ': trait "' + name + '" is on no biome (typo?)')
					})
				})
			})
		}

		function tags(event) {
			var c = readConfig()
			if (!c) return
			Object.keys(c.rules).forEach(function (key) {
				var biomes = biomesOf(c, key)
				if (biomes.length) global.tags.add(event, opts.tagPrefix + key, biomes)
			})
		}

		function data(event) {
			var c = readConfig()
			if (!c) return
			checkRules(c)
			var disabled = {}
			var removed = {}
			var added = 0
			var skipped = []
			Object.keys(c.rules).forEach(function (key) {
				var rule = c.rules[key]
				// "requires": a mod id the rule's features need beyond their own namespace (a feature only one build of
				// that mod has, e.g. Rustic Delight 1.7.0's giant peppers, registered by Creative Delight's port)
				if (rule.requires && !Platform.isLoaded(rule.requires)) {
					skipped.push(key + ': needs ' + rule.requires)
					return
				}
				;(rule.disable_modifiers || []).forEach(function (id) {
					if (modLoaded(id)) disabled[id] = true
				})
				;(rule.remove_features || []).forEach(function (id) {
					if (modLoaded(id)) removed[id] = true
					else skipped.push(key + ': ' + id)
				})
				var features = (rule.features || []).filter(function (id) {
					if (modLoaded(id)) return true
					skipped.push(key + ': ' + id)
					return false
				})
				if (!features.length || !biomesOf(c, key).length) return
				event.add(opts.modifierPrefix + key + '.json', JSON.stringify({
					type: 'forge:add_features',
					biomes: '#' + opts.tagPrefix + key,
					features: features,
					step: opts.step(rule),
				}))
				added++
			})
			Object.keys(disabled).forEach(function (id) {
				event.add(modifierPath(id), JSON.stringify({ type: 'forge:none' }))
			})
			var removeIds = Object.keys(removed).sort()
			if (removeIds.length) {
				event.add(opts.modifierPrefix + '_remove_vanilla_patches.json', JSON.stringify({
					type: 'forge:remove_features',
					biomes: '#minecraft:is_overworld',
					features: removeIds,
					steps: ['vegetal_decoration'],
				}))
			}
			skipped.forEach(function (s) { console.warn(LOG + 'mod not loaded, skipped ' + s) })
			console.info(LOG + added + ' ' + word + ' modifiers added, ' + Object.keys(disabled).length + ' mod modifiers off, '
				+ removeIds.length + ' biome-file patches removed')
		}

		function loaded(event) {
			var c = readConfig()
			if (!c) return
			var $Registry = Java.loadClass('net.minecraft.core.Registry')
			var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
			var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
			function tagMembers(reg, id) {
				return idSet(reg.getTagOrEmpty($TagKey.create($Registry.BIOME_REGISTRY, new $ResourceLocation(id))), holderId)
			}
			var reg
			var known
			var overworld
			try {
				reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
				known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
				overworld = tagMembers(reg, 'minecraft:is_overworld')
			} catch (err) {
				console.error(LOG + 'could not read the biome registry, ' + opts.name + ' unverified: ' + err)
				return
			}
			var pairs = 0
			var problems = []
			Object.keys(c.rules).forEach(function (key) {
				var members
				try {
					members = tagMembers(reg, opts.tagPrefix + key)
				} catch (err) {
					problems.push('could not read #' + opts.tagPrefix + key + ': ' + err)
					return
				}
				biomesOf(c, key).forEach(function (b) {
					if (!known[b]) problems.push(key + ': no such biome ' + b)
					else if (!members[b]) problems.push(key + ': ' + b + ' is not in #' + opts.tagPrefix + key)
					else pairs++
				})
			})
			problems.forEach(function (p) { console.error(LOG + p) })
			if (opts.reportUnclassified) {
				var unclassified = Object.keys(overworld).filter(function (b) { return !c.traits[b] }).sort()
				if (unclassified.length) {
					console.warn(LOG + unclassified.length + ' overworld biomes are not in ' + (opts.biomesPath || opts.configPath)
						+ ' and match no rule: ' + unclassified.join(', '))
				}
			}
			console.info(LOG + pairs + ' ' + word + '/biome pairs in ' + Object.keys(c.rules).length + ' ' + word + ' tags'
				+ (problems.length ? ', ' + problems.length + ' problems' : ''))
		}

		return { tags: tags, data: data, loaded: loaded }
	}

	global.biomeRules = { system: system, resolve: resolve, traitSets: traitSets, matches: matches }
})()
