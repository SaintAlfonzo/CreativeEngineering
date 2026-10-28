// Wild flowers: which biomes flowers generate in, how often, which Botania colors, and how many Flower Patch
// blocks they come with.
//
// Edit kubejs/generation_scripts/worldgen/wild_flowers.json, then rejoin the world (biome modifiers and features
// are read at world load, not on /reload). Only chunks generated afterwards change. Reference + tests:
// feature_writeup/flowers.md; resolved numbers per biome: feature_writeup/flowers_by_biome.md
// (python3 tools/gen_wild_flowers.py evaluates the same rules offline). Biome traits are the ones in
// biome_traits.json; a clause means the same thing as in the wild crop rules.
//
// What it writes (ServerEvents.highPriorityData: KubeJS's top-priority virtual datapack, above the mod jars and
// kubejs/data):
//   - botania: forge:none over Botania's own modifier; biomes with the same palette and rate share one
//     simple_random_selector over Botania's per-color patches, placed at that rate, added through the biome tag
//     creativeengineering:wild_flowers/botania/<n>.
//   - flowers: per entry and rate tier, one flower patch feature (weighted blocks, Flower Patch blocks mixed in),
//     added through the tag creativeengineering:wild_flowers/<key>/<tier>. disable_modifiers -> forge:none.
//   - existing: the flower features biome files list (snapshot in flower_features.json, made by the generator):
//     placed features rewritten at their new rate, configured features rewritten with Flower Patch blocks, and
//     a forge:remove_features per feature over the biomes in its "remove" list.
// A tier is the placement a rate becomes: c<N> = N patches per chunk, r<N> = one patch in N chunks.
// Anything that names a mod which isn't loaded is skipped: an unknown id stops the world loading.
//
// The tags handler can't check biome ids (KubeJS runs it as a pre-pass without a registry, see
// _biome_rules.js), so ServerEvents.loaded compares the live tags with the rules.
//
// CEConfig [worldgen.systems] wildFlowers = false: nothing here runs, every flower as its mod ships it.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var CONFIG_PATH = 'kubejs/generation_scripts/worldgen/wild_flowers.json'
	var SNAPSHOT_PATH = 'kubejs/generation_scripts/worldgen/flower_features.json'
	var TRAITS_PATH = 'kubejs/generation_scripts/worldgen/biome_traits.json'
	var NS = 'creativeengineering:'
	var PREFIX = 'wild_flowers/'
	var NEVER = { ocean: true, cave: true }
	var LOG = '[wild-flowers] '
	var PROVIDERS = {
		'minecraft:simple_state_provider': true, 'minecraft:weighted_state_provider': true,
		'minecraft:noise_threshold_provider': true, 'minecraft:noise_provider': true, 'minecraft:dual_noise_provider': true,
	}
	var AIR_ONLY = [{ type: 'minecraft:block_predicate_filter',
		predicate: { type: 'minecraft:matching_blocks', blocks: ['minecraft:air'] } }]

	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	function enabled() {
		return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig').wildFlowersEnabled() == true
	}

	function readJson(path) {
		return JSON.parse(String(JsonIO.readString(path)))
	}

	// { cfg, snap, traits } or null
	function load() {
		if (!enabled()) {
			console.info(LOG + 'off in CEConfig [worldgen.systems] wildFlowers: every flower spawns where its mod puts it')
			return null
		}
		var out = {}
		try {
			out.cfg = readJson(CONFIG_PATH)
			out.snap = readJson(SNAPSHOT_PATH)
			out.traits = traitSets(readJson(TRAITS_PATH).biomes)
		} catch (err) {
			console.error(LOG + 'could not read ' + CONFIG_PATH + ', ' + SNAPSHOT_PATH + ' or ' + TRAITS_PATH + ', nothing changed: ' + err)
			return null
		}
		var follows = out.snap.follows || {}
		Object.keys(out.cfg.flowers || {}).forEach(function (key) {
			var entry = out.cfg.flowers[key]
			if (!entry.follows) return
			entry._follows = follows[key] || []
			if (!follows[key]) console.warn(LOG + key + ': follows ' + entry.follows + ' but ' + SNAPSHOT_PATH + ' has no biome list for it (rerun tools/gen_wild_flowers.py)')
		})
		return out
	}

	// ── rules (same as tools/gen_wild_flowers.py) ──────────────────────────────

	function traitSets(biomes) {
		var out = {}
		Object.keys(biomes).forEach(function (b) {
			var set = {}
			String(biomes[b]).trim().split(/\s+/).forEach(function (t) { if (t) set[t] = true })
			out[b] = set
		})
		return out
	}

	function clauseMatches(traits, clause) {
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

	// a selector is a biome id (has ':') or a trait clause
	function selectorMatches(sel, biome, traits) {
		return String(sel).indexOf(':') >= 0 ? String(sel) === biome : clauseMatches(traits, sel)
	}

	function isNever(tr) {
		for (var n in NEVER) if (tr[n]) return true
		return false
	}

	// where/add/except, as the wild crop rules. An entry with 'follows' takes the biome list the generator resolved for it
	// (snapshot 'follows': every biome where that flower generates after the pack's rules), stored in load().
	function resolve(traits, entry) {
		if (entry.follows) return (entry._follows || []).slice()
		var out = {}
		Object.keys(traits).forEach(function (b) {
			var tr = traits[b]
			if (isNever(tr)) return
			var where = entry.where || []
			for (var k = 0; k < where.length; k++) {
				if (clauseMatches(tr, where[k])) { out[b] = true; return }
			}
		})
		;(entry.add || []).forEach(function (b) { out[b] = true })
		;(entry.except || []).forEach(function (b) { delete out[b] })
		return Object.keys(out).sort()
	}

	function parseRate(r) {
		if (typeof r === 'number') return r
		var s = String(r).trim()
		var slash = s.indexOf('/')
		return slash >= 0 ? Number(s.substring(0, slash)) / Number(s.substring(slash + 1)) : Number(s)
	}

	function tierKey(rate) {
		if (!(rate > 0)) return null
		if (rate >= 1) return 'c' + Math.round(rate)
		return 'r' + Math.max(1, Math.round(1 / rate))
	}

	function rateFor(biome, traits, entry, density) {
		var r = parseRate(entry.rate == null ? 0 : entry.rate)
		var rates = entry.rates || []
		for (var k = 0; k < rates.length; k++) {
			if (selectorMatches(rates[k][0], biome, traits)) { r = parseRate(rates[k][1]); break }
		}
		return r * density
	}

	function num(v, dflt) {
		return v == null ? dflt : Number(v)
	}

	function normalizePalette(p) {
		var out = []
		String(p || '').trim().split(/\s+/).forEach(function (tok) {
			if (!tok) return
			var star = tok.indexOf('*')
			var name = star >= 0 ? tok.substring(0, star) : tok
			var n = star >= 0 ? Number(tok.substring(star + 1)) : 1
			for (var k = 0; k < n; k++) out.push(name)
		})
		return out.sort()
	}

	// [{ palette: [colors], tier, biomes }] in the generator's order
	function botaniaPlan(cfg, traits) {
		var bot = cfg.botania || {}
		var pals = bot.palettes || {}
		var density = num(cfg.density, 1) * num(bot.density, 1)
		var groups = {}
		Object.keys(traits).sort().forEach(function (b) {
			var tr = traits[b]
			if (isNever(tr)) return
			var pal = normalizePalette(pals.hasOwnProperty(b) ? pals[b] : pals._default)
			if (!pal.length) return
			var tier = tierKey(rateFor(b, tr, bot, density))
			if (!tier) return
			var key = pal.join(' ') + '|' + tier
			if (!groups[key]) groups[key] = { palette: pal, tier: tier, biomes: [] }
			groups[key].biomes.push(b)
		})
		return Object.keys(groups).sort().map(function (k) { return groups[k] })
	}

	// { tier: [biomes] } for one flowers entry
	function entryPlan(cfg, traits, key) {
		var entry = cfg.flowers[key]
		var density = num(cfg.density, 1) * num(entry.density, 1)
		var tiers = {}
		resolve(traits, entry).forEach(function (b) {
			if (!traits[b]) return
			var tier = tierKey(rateFor(b, traits[b], entry, density))
			if (!tier) return
			if (!tiers[tier]) tiers[tier] = []
			tiers[tier].push(b)
		})
		return tiers
	}

	function removalBiomes(spec, traits) {
		var sels = spec.remove || []
		return Object.keys(traits).sort().filter(function (b) {
			for (var k = 0; k < sels.length; k++) if (selectorMatches(sels[k], b, traits[b])) return true
			return false
		})
	}

	function safeName(id) {
		return String(id).replace(':', '/')
	}

	// every biome tag this script fills: tag id -> biomes
	function tagPlan(d) {
		var out = {}
		botaniaPlan(d.cfg, d.traits).forEach(function (g, n) { out[NS + PREFIX + 'botania/' + n] = g.biomes })
		Object.keys(d.cfg.flowers || {}).forEach(function (key) {
			var tiers = entryPlan(d.cfg, d.traits, key)
			Object.keys(tiers).forEach(function (t) { out[NS + PREFIX + key + '/' + t] = tiers[t] })
		})
		Object.keys(d.cfg.existing || {}).forEach(function (f) {
			if (f.charAt(0) === '_') return
			var bs = removalBiomes(d.cfg.existing[f], d.traits)
			if (bs.length && num(d.cfg.existing[f].scale, 1) > 0) out[NS + PREFIX + 'remove/' + safeName(f)] = bs
		})
		return out
	}

	// ── Flower Patch conversion (same as the generator) ────────────────────────

	function gcd(a, b) { return b ? gcd(b, a % b) : a }

	// [{ size: null | '2'..., weight }]
	function patchWeights(share, sizes) {
		if (!(share > 0)) return [{ size: null, weight: 1 }]
		var keys = Object.keys(sizes).sort(function (a, b) { return Number(a) - Number(b) })
		var total = 0
		keys.forEach(function (k) { total += Number(sizes[k]) })
		var out = []
		if (share < 1) out.push({ size: null, weight: Math.round(100 * (1 - share)) })
		keys.forEach(function (k) { out.push({ size: k, weight: Math.round(100 * Math.min(share, 1) * Number(sizes[k]) / total) }) })
		out = out.filter(function (e) { return e.weight > 0 })
		var d = 0
		out.forEach(function (e) { d = gcd(d, e.weight) })
		d = d || 1
		return out.map(function (e) { return { size: e.size, weight: e.weight / d } })
	}

	function sumWeights(ws) {
		var s = 0
		ws.forEach(function (e) { s += e.weight })
		return s
	}

	function convertible(fp, state) {
		return state != null && typeof state === 'object' && fp.blocks.hasOwnProperty(state.Name) && !state.Properties
	}

	function patchState(fp, state, size) {
		if (size == null) return state
		var tgt = fp.blocks[state.Name]
		var props = {}
		if (typeof tgt === 'object') {
			props[tgt.property || 'flowers'] = String(size)
			return { Name: tgt.block, Properties: props }
		}
		props.flowers = String(size)
		return { Name: tgt, Properties: props }
	}

	function hasConvertible(fp, list) {
		for (var k = 0; k < list.length; k++) if (convertible(fp, list[k])) return true
		return false
	}

	// uniform-pick lists: weights become repeats; every other entry repeats by the total, so bands keep their size
	function repeatList(fp, list, ws) {
		var out = []
		var total = sumWeights(ws)
		list.forEach(function (s) {
			if (convertible(fp, s)) {
				ws.forEach(function (e) { for (var k = 0; k < e.weight; k++) out.push(patchState(fp, s, e.size)) })
			} else {
				for (var k = 0; k < total; k++) out.push(s)
			}
		})
		return out
	}

	function copy(o) {
		var out = {}
		Object.keys(o).forEach(function (k) { out[k] = o[k] })
		return out
	}

	// provider -> { p, ok }
	function convertProvider(p, fp, share) {
		var ws = patchWeights(share, fp.sizes)
		var q
		if (p.type === 'minecraft:simple_state_provider' && convertible(fp, p.state)) {
			return { ok: true, p: { type: 'minecraft:weighted_state_provider', entries: ws.map(function (e) {
				return { data: patchState(fp, p.state, e.size), weight: e.weight }
			}) } }
		}
		if (p.type === 'minecraft:weighted_state_provider') {
			if (!hasConvertible(fp, p.entries.map(function (e) { return e.data }))) return { ok: false, p: p }
			var entries = []
			p.entries.forEach(function (e) {
				if (convertible(fp, e.data)) {
					ws.forEach(function (w) { entries.push({ data: patchState(fp, e.data, w.size), weight: e.weight * w.weight }) })
				} else {
					entries.push({ data: e.data, weight: e.weight * sumWeights(ws) })
				}
			})
			q = copy(p)
			q.entries = entries
			return { ok: true, p: q }
		}
		if (p.type === 'minecraft:noise_threshold_provider') {
			var changed = false
			q = copy(p)
			;['low_states', 'high_states'].forEach(function (field) {
				var list = p[field] || []
				if (hasConvertible(fp, list)) {
					q[field] = repeatList(fp, list, ws)
					changed = true
				}
			})
			return { ok: changed, p: q }
		}
		if (p.type === 'minecraft:noise_provider') {
			if (!hasConvertible(fp, p.states || [])) return { ok: false, p: p }
			q = copy(p)
			q.states = repeatList(fp, p.states, ws)
			return { ok: true, p: q }
		}
		return { ok: false, p: p }
	}

	// deep copy with every convertible provider converted; stats.converted counts them
	function convertJson(v, fp, share, stats) {
		if (Array.isArray(v)) return v.map(function (x) { return convertJson(x, fp, share, stats) })
		if (v == null || typeof v !== 'object') return v
		if (PROVIDERS[v.type]) {
			var r = convertProvider(v, fp, share)
			if (r.ok) stats.converted++
			return r.p
		}
		var out = {}
		Object.keys(v).forEach(function (k) { out[k] = convertJson(v[k], fp, share, stats) })
		return out
	}

	// placement list with its rate times s (s > 0)
	function scalePlacement(pl, s) {
		pl = pl.map(copy)
		var ri = -1
		for (var k = 0; k < pl.length; k++) if (pl[k].type === 'minecraft:rarity_filter') { ri = k; break }
		if (ri >= 0) {
			var n = pl[ri].chance / s
			if (n >= 1) {
				pl[ri].chance = Math.max(1, Math.round(n))
				return pl
			}
			pl.splice(ri, 1)
			s = 1 / n
		}
		if (Math.abs(s - 1) < 1e-9) return pl
		if (s < 1) return [{ type: 'minecraft:rarity_filter', chance: Math.max(1, Math.round(1 / s)) }].concat(pl)
		return [{ type: 'minecraft:count', count: Math.round(s) }].concat(pl)
	}

	// ── writing ───────────────────────────────────────────────────────────────

	// JSON.stringify here is Gson-backed and writes 128 as 128.0; numbers via String() instead
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

	function namespace(id) {
		var s = String(id)
		var c = s.indexOf(':')
		return c < 0 ? 'minecraft' : s.substring(0, c)
	}

	function modLoaded(id) {
		var ns = namespace(id)
		return ns === 'minecraft' || ns === 'creativeengineering' || Platform.isLoaded(ns)
	}

	function dataPath(id, kind) {
		var s = String(id)
		return namespace(s) + ':' + kind + '/' + s.substring(s.indexOf(':') + 1) + '.json'
	}

	function placementFor(tier, heightmap) {
		var n = Number(tier.substring(1))
		var out = []
		if (tier.charAt(0) === 'c') out.push({ type: 'minecraft:count', count: n })
		else if (n > 1) out.push({ type: 'minecraft:rarity_filter', chance: n })
		return out.concat([
			{ type: 'minecraft:in_square' },
			{ type: 'minecraft:heightmap', heightmap: heightmap },
			{ type: 'minecraft:biome' },
		])
	}

	function blockLoaded(id) {
		return modLoaded(id)
	}

	// the configured feature for a flowers entry, or null when none of its blocks' mods is loaded
	function entryFeature(entry, fp, defaultShare, tallBlocks) {
		var share = num(entry.flower_patch, defaultShare)
		var ws = patchWeights(share, fp.sizes)
		var total = sumWeights(ws)
		var entries = []
		var tall = false
		Object.keys(entry.blocks || {}).forEach(function (b) {
			if (!blockLoaded(b)) return
			var w = Math.max(1, Math.round(Number(entry.blocks[b])))
			if (tallBlocks.indexOf(b) >= 0) tall = true
			var state = { Name: b }
			if (convertible(fp, state) && blockLoaded(typeof fp.blocks[b] === 'object' ? fp.blocks[b].block : fp.blocks[b])) {
				ws.forEach(function (e) { entries.push({ data: patchState(fp, state, e.size), weight: w * e.weight }) })
			} else {
				entries.push({ data: state, weight: w * total })
			}
		})
		if (!entries.length) return null
		var patch = entry.patch || {}
		return {
			// minecraft:flower also lets bone meal on grass grow it (when it is the biome's first flower feature)
			type: tall ? 'minecraft:random_patch' : 'minecraft:flower',
			config: {
				tries: num(patch.tries, 48),
				xz_spread: num(patch.xz_spread, 6),
				y_spread: num(patch.y_spread, 3),
				feature: {
					feature: { type: 'minecraft:simple_block', config: { to_place: { type: 'minecraft:weighted_state_provider', entries: entries } } },
					placement: AIR_ONLY,
				},
			},
		}
	}

	// typos in rules match nothing silently; flag tokens no biome carries
	function checkRules(d) {
		var vocab = {}
		Object.keys(d.traits).forEach(function (b) { for (var t in d.traits[b]) vocab[t] = true })
		function check(where, sel) {
			if (String(sel).indexOf(':') >= 0) {
				if (!d.traits[sel]) console.warn(LOG + where + ': ' + sel + ' is not in ' + TRAITS_PATH + ' "biomes"')
				return
			}
			String(sel).trim().split(/\s+/).forEach(function (t) {
				var name = t.charAt(0) === '!' ? t.substring(1) : t
				if (name && !vocab[name]) console.warn(LOG + where + ': trait "' + name + '" is on no biome (typo?)')
			})
		}
		var bot = d.cfg.botania || {}
		;(bot.rates || []).forEach(function (r) { check('botania.rates', r[0]) })
		Object.keys(d.cfg.flowers || {}).forEach(function (key) {
			var e = d.cfg.flowers[key]
			;(e.where || []).forEach(function (c) { check(key, c) })
			;(e.rates || []).forEach(function (r) { check(key + '.rates', r[0]) })
		})
		Object.keys(d.cfg.existing || {}).forEach(function (f) {
			if (f.charAt(0) === '_') return
			if (!d.snap.placed[f]) console.warn(LOG + 'existing: ' + f + ' is not in ' + SNAPSHOT_PATH + ' (rerun tools/gen_wild_flowers.py?)')
			;(d.cfg.existing[f].remove || []).forEach(function (s) { check('existing ' + f + '.remove', s) })
		})
	}

	ServerEvents.tags('worldgen/biome', function (event) {
		var d = load()
		if (!d) return
		var plan = tagPlan(d)
		Object.keys(plan).forEach(function (tag) {
			if (plan[tag].length) global.tags.add(event, tag, plan[tag])
		})
	})

	ServerEvents.highPriorityData(function (event) {
		var d = load()
		if (!d) return
		checkRules(d)
		var cfg = d.cfg
		var fp = cfg.flower_patch || { blocks: {}, sizes: {} }
		var heightmap = cfg.heightmap || 'MOTION_BLOCKING_NO_LEAVES'
		var files = 0
		var counts = { botania: 0, flowers: 0, scaled: 0, patched: 0, removed: 0, off: 0 }
		var skipped = []
		var disabled = {}

		function put(id, kind, obj) {
			event.add(dataPath(id, kind), toJson(obj))
			files++
		}

		function addFeature(name, configured, tier, tag) {
			var id = NS + PREFIX + name
			put(id, 'worldgen/configured_feature', configured)
			put(id, 'worldgen/placed_feature', { feature: id, placement: placementFor(tier, heightmap) })
			put(NS + PREFIX + name, 'forge/biome_modifier', {
				type: 'forge:add_features', biomes: '#' + tag, features: id, step: 'vegetal_decoration',
			})
		}

		// botania
		var bot = cfg.botania || {}
		var pattern = bot.patch_feature || 'botania:{color}_mystical_flower_patch'
		if (modLoaded(pattern)) {
			;(bot.disable_modifiers || []).forEach(function (m) { if (modLoaded(m)) disabled[m] = true })
			botaniaPlan(cfg, d.traits).forEach(function (g, n) {
				addFeature('botania/' + n, {
					type: 'minecraft:simple_random_selector',
					config: { features: g.palette.map(function (c) { return { feature: pattern.replace('{color}', c), placement: [] } }) },
				}, g.tier, NS + PREFIX + 'botania/' + n)
				counts.botania++
			})
		} else if (bot.palettes) {
			skipped.push('botania (not loaded)')
		}

		// flowers
		Object.keys(cfg.flowers || {}).forEach(function (key) {
			var entry = cfg.flowers[key]
			var feature = entryFeature(entry, fp, num(fp.share, 0), cfg.tall_flowers || [])
			if (!feature) {
				skipped.push(key + ' (no block of a loaded mod)')
				return
			}
			;(entry.disable_modifiers || []).forEach(function (m) { if (modLoaded(m)) disabled[m] = true })
			var tiers = entryPlan(cfg, d.traits, key)
			Object.keys(tiers).forEach(function (t) {
				addFeature(key + '/' + t, feature, t, NS + PREFIX + key + '/' + t)
				counts.flowers++
			})
		})

		// existing features from the biome files
		var existing = cfg.existing || {}
		var shares = {}
		var dflt = num(fp.existing_share, 0)
		Object.keys(d.snap.configured_of).forEach(function (f) {
			var share = num((existing[f] || {}).flower_patch, dflt)
			d.snap.configured_of[f].forEach(function (c) { shares[c] = Math.max(shares[c] || 0, share) })
		})
		Object.keys(d.snap.configured).sort().forEach(function (c) {
			if (!modLoaded(c) || !(shares[c] > 0)) return
			var stats = { converted: 0 }
			var out = convertJson(d.snap.configured[c], fp, shares[c], stats)
			if (!stats.converted) return
			put(c, 'worldgen/configured_feature', out)
			counts.patched++
		})
		Object.keys(d.snap.placed).sort().forEach(function (f) {
			if (!modLoaded(f)) return
			var spec = existing[f] || {}
			var scale = num(spec.scale, 1)
			var placed = d.snap.placed[f]
			var changed = false
			var out = copy(placed)
			var share = num(spec.flower_patch, dflt)
			if (typeof placed.feature === 'object' && share > 0) {   // inline configured feature
				var stats = { converted: 0 }
				out.feature = convertJson(placed.feature, fp, share, stats)
				if (stats.converted) { changed = true; counts.patched++ }
			}
			if (scale > 0 && Math.abs(scale - 1) > 1e-9) {
				out.placement = scalePlacement(placed.placement || [], scale)
				changed = true
				counts.scaled++
			}
			if (scale > 0 && changed) put(f, 'worldgen/placed_feature', out)
			var removeTag = null
			if (!(scale > 0)) removeTag = '#minecraft:is_overworld'
			else if (removalBiomes(spec, d.traits).length) removeTag = '#' + NS + PREFIX + 'remove/' + safeName(f)
			if (removeTag) {
				put(NS + PREFIX + 'remove/' + safeName(f), 'forge/biome_modifier', {
					type: 'forge:remove_features', biomes: removeTag, features: f, steps: ['vegetal_decoration'],
				})
				counts.removed++
			}
		})

		Object.keys(disabled).sort().forEach(function (id) {
			put(id, 'forge/biome_modifier', { type: 'forge:none' })
			counts.off++
		})
		skipped.forEach(function (s) { console.warn(LOG + 'mod not loaded, skipped ' + s) })
		console.info(LOG + counts.botania + ' Botania palette features, ' + counts.flowers + ' flower features, '
			+ counts.scaled + ' existing features rescaled, ' + counts.patched + ' with Flower Patch blocks, '
			+ counts.removed + ' removals, ' + counts.off + ' mod modifiers off (' + files + ' files)')
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
		var d = load()
		if (!d) return
		var reg
		var known
		try {
			reg = event.server.registryAccess().registryOrThrow($Registry.BIOME_REGISTRY)
			known = idSet(reg.keySet(), function (rl) { return String(rl.toString()) })
		} catch (err) {
			console.error(LOG + 'could not read the biome registry, wild flowers unverified: ' + err)
			return
		}
		var plan = tagPlan(d)
		var pairs = 0
		var problems = []
		Object.keys(plan).forEach(function (tag) {
			var members
			try {
				members = idSet(reg.getTagOrEmpty($TagKey.create($Registry.BIOME_REGISTRY, new $ResourceLocation(tag))), holderId)
			} catch (err) {
				problems.push('could not read #' + tag + ': ' + err)
				return
			}
			plan[tag].forEach(function (b) {
				if (!known[b]) problems.push(tag + ': no such biome ' + b)
				else if (!members[b]) problems.push(b + ' is not in #' + tag)
				else pairs++
			})
		})
		problems.forEach(function (p) { console.error(LOG + p) })
		console.info(LOG + pairs + ' flower/biome pairs in ' + Object.keys(plan).length + ' tags'
			+ (problems.length ? ', ' + problems.length + ' problems' : ''))
	})
})()
