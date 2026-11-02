// Structure spawn rates: structure groups + standalone sets, applied at world load.
//
// Edit kubejs/generation_scripts/worldgen/structure_groups.json (every rate) or the group/* tags in
// structure_tags.json (who is in which group), then rejoin the world. Worldgen registries are read
// when a world loads, so /reload doesn't apply these to a running world (it does refresh the
// report), and only chunks generated afterwards change. After moving structures between groups or
// updating mods, run `python3 tools/gen_structure_groups.py` to give new members fitted values.
//
// THE MODEL
//   Group: one structure_set, creativeengineering:group/<name>, holding every structure in the tag
//   group/<name>. All members share its grid, so no two ever start closer than its separation.
//   Per member: weight (who wins a slot where several fit the biome) and frequency (chance the
//   winner really spawns; else the slot stays empty, no fall-through to another member).
//   Standalone: every structure in no group keeps its own structure_set, tuned the same way.
//   A structure moved into a group is removed from its original set; a set left with no live
//   member is written with no structures and frequency 0, so it can't generate or be searched.
//
// BIOME RULES (biome_rules in structure_groups.json): a rule names biomes and a structure tag; only the
// tag's structures may start in those biomes (water: ships, wrecks, ocean ruins...), every other structure
// loses those biomes from its own list. Handed to patcherMod (worldgen/StructureBiomeRules), which also
// covers structures in no config yet; its log line lists what each rule kept out. Independent of the groups.
// A rule's optional surface_check tag: those structures (dungeons with a staircase to the surface) also may not start
// where the surface above them is one of the rule's biomes or under water (patcherMod checks the noise surface).
//
// BIOME FITS (biome_fits.json): a fit is a biome list and the structure tag of the same name (land/oak). A structure
// in the tag spawns in its fit's biomes plus every parent fit's (land/oak: land/oak + land), never a sibling's, in
// place of its mod's own list. Each fit is also added as the real biome tag and structure tag creativeengineering:<fit>
// (ServerEvents.tags below). Handed to patcherMod (worldgen/StructureBiomeFits), which answers Structure#biomes().
//
// META STRUCTURES (meta_structures.json): our structures built from other structures and features (a village with
// peaceful towers, a driftwood flotilla). Each definition is added as data/<ns>/worldgen/structure/<path>.json, but only
// when patcherMod has worldgen/MetaStructure (its structure type) and [worldgen.systems] metaStructures is on; otherwise
// it stays out of every group set (an unknown structure type, or a set naming a missing structure, fails the world load).
// They live only in groups. The meta/* tags of structure_tags.json are also added as real structure tags
// creativeengineering:meta/<name> (the anchors and parts the definitions name), from ServerEvents.tags below.
//
// PLACEMENT PRIORITY ("priority" in structure_groups.json): a rank per group (and per tag, for the fortified towns); a
// structure whose pieces would overlap a higher-ranked structure's never starts. A meta structure's "companions" are
// structures started in chunks of their own around it (villages around a fortified town). priority.peaceful: a radius
// (chunks) around big settlements, villages and remote camps/houses where no hostile structure may start. Staged to
// patcherMod (worldgen/PlacementPriority).
//
// REPLACEMENTS: a group member with "replaces": "<member id>" and "share": 0..1 never wins a slot by its own weight; it
// takes that share of the slots the named member wins and keeps (the vanilla swamp hut in YUNG's witch hut slots), so the
// two together spawn exactly as often as the named member alone did. A meta structure without an anchor may be a
// replacement too: its parts then build in the named member's slot (meta/meteorite_desert_pyramid in YUNG's temple slots). Staged to patcherMod (worldgen/MetaSlots). With
// [worldgen.systems] structureReplacements off, replacements are left out of their group set and the named member keeps
// every slot.
//
// BIOME FREQUENCY (a group's "biome_frequency", 2026-10-14): {"#biome tag" | "biome id": factor 0..1, ..., "default": factor}.
// Every member's frequency is multiplied by the factor of the biome at its generation point (the first rule the biome
// matches, else default); patcherMod rolls it in generation and in /locate's prediction alike. The vehicles use it to be
// twice as common in #creativeengineering:harsh biomes. Biome tags of our own (harsh) are biome_fits.json "biome_tags",
// added as real tags creativeengineering:<name> below.
//
// frequency < 1 is not a vanilla feature: the table is handed to patcherMod
// (worldgen/StructureFrequencies), which rolls it when a structure is about to be placed.
// Without that class nothing is applied at all (a group without its frequencies would spawn
// every member at full rate).
//
// ServerEvents.highPriorityData runs inside KubeJS's world-load resource wrap, before the game
// parses worldgen: every structure_set that ends up different from its mod/vanilla default is
// added to KubeJS's top-priority virtual datapack (above kubejs/data, Terralith, Nullscape).
//
// Any config error applies NOTHING, so the game falls back to every mod's own defaults, and the
// errors land in the log + the report. structure_tag_report.md (same folder as the configs) is
// rewritten on every load.
//
// CEConfig [worldgen] (config/creativeengineering-common.toml, read through patcherMod's WorldgenConfig):
//   systems.structureGroups = false  -> groups and standalone edits are ignored: every set as its mod ships it.
//   debug.structureDensity = x       -> every set's rate times x (see scalePlacement), grouped or not.
//   debug.testOverrides = true       -> structure_test_overrides.json loosens soft rules for scratch tests (see
//                                       applyTestOverrides): frequency / weight / share per structure, group spacing.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var DIR = 'kubejs/generation_scripts/worldgen/'
	var CONFIG_PATH = DIR + 'structure_config.json'
	var TAGS_PATH = DIR + 'structure_tags.json'
	var GROUPS_PATH = DIR + 'structure_groups.json'
	var META_PATH = DIR + 'meta_structures.json'
	var FITS_PATH = DIR + 'biome_fits.json'
	var TEST_PATH = DIR + 'structure_test_overrides.json'
	var REPORT_PATH = DIR + 'structure_tag_report.md'
	var GROUP_SET_PREFIX = 'creativeengineering:group/'
	var TAG = '[structure-tuning] '

	// Codec limits (RandomSpreadStructurePlacement / ConcentricRingsStructurePlacement / ExclusionZone, 1.19.2).
	var MAX_SPACING = 4096
	var MAX_COUNT = 4095
	var MAX_EXCLUSION = 16
	var PEACE_MAX_RADIUS = 16   // PlacementPriority.MAX_RADIUS
	var DEFAULT_WEIGHT = 100

	var GROUP_PLACEMENT_KEYS = ['spacing', 'separation', 'salt', 'spread_type', 'frequency',
		'frequency_reduction_method', 'locate_offset', 'exclusion_zone']
	var MEMBER_KEYS = ['weight', 'frequency', 'enabled', 'note', 'tuned', 'fit_base', 'fit_keep', 'replaces', 'share']   // fit_base, fit_keep, tuned: gen_structure_groups.py bookkeeping (--fit-rates, --strip-weights), not read here
	// structure_test_overrides.json: soft rules only (structure_tuning.md 7.4.8)
	var TEST_MEMBER_KEYS = ['frequency', 'weight', 'share']
	var TEST_GROUP_KEYS = ['spacing', 'separation']
	var TEST_RULE = 'test overrides loosen soft rules only (frequency, weight, share, spacing, separation; structure_tuning.md 7.4.8); '
		+ 'biomes, biome rules, fits, surface checks, peaceful radius, priority and earth/water fill are correctness rules and stay'
	var REDUCTION_METHODS = ['default', 'legacy_type_1', 'legacy_type_2', 'legacy_type_3']
	var META_TYPE = 'creativeengineering:meta'
	var META_KEYS = ['note', 'tuned', 'type', 'biomes', 'step', 'terrain_adaptation', 'spawn_overrides', 'anchor', 'share', 'parts', 'companions']
	var COMPANION_KEYS = ['structures', 'count', 'distance']
	var META_PART_KEYS = ['structures', 'features', 'count', 'chance', 'distance', 'spacing', 'margin', 'tries', 'water_depth', 'radius', 'overlap', 'cluster']
	var META_MAX_DISTANCE = 112   // MetaStructure.REACH chunks
	var META_TAG_PREFIX = 'meta/'
	var TYPE_TAG_PREFIX = 'type/'
	var WAY_SIGN_GROUP = 'settlements'
	var WAY_SIGN_DESTINATIONS = 'supplementaries:way_sign_destinations'

	// KubeJS's class filter blocks java.io, so the markdown report goes through patcherMod.
	var WorldgenFiles = Java.loadClass('com.SaintAlfonzo.creativeengineering.util.WorldgenFiles')
	// patcherMod's worldgen side: switches, and the stages the plan is handed to
	var WorldgenConfig = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig')
	var StructureFrequencies = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.StructureFrequencies')
	var StructureBiomeRules = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.StructureBiomeRules')
	var StructureBiomeFits = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.StructureBiomeFits')
	var MetaSlots = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.MetaSlots')
	var PlacementPriority = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.PlacementPriority')

	// CEConfig [worldgen] switches
	function worldgenSettings() {
		var s = { groups: WorldgenConfig.structureGroupsEnabled() == true, density: Number(WorldgenConfig.structureDensity()), test: false }
		if (!(s.density > 0) || !isFinite(s.density)) s.density = 1
		try { s.test = WorldgenConfig.testOverrides() == true } catch (err) { s.test = false }   // older patcherMod jar: no switch
		return s
	}

	// ── small helpers (Rhino: no Object.assign / Set / spread / ** here) ──────

	function assign(target) {
		for (var a = 1; a < arguments.length; a++) {
			var src = arguments[a]
			if (!src) continue
			Object.keys(src).forEach(function (k) { target[k] = src[k] })
		}
		return target
	}

	// JSON text with JS number formatting. KubeJS's Rhino builds JSON.stringify on Gson, which
	// writes every number as a Java double (spacing 128 -> "128.0", salt -> "1.11823992E8"); the
	// game happens to parse those, but written files should say what they mean. String(n) is
	// JS-correct in this Rhino. JSON.stringify is still fine for strings and for comparing two
	// values inside this script (both sides get the same formatting).
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

	function readJson(path) {
		var text
		try {
			text = JsonIO.readString(path)
		} catch (err) {
			return null
		}
		return text == null ? null : JSON.parse(String(text))
	}

	function resourcePath(id) {
		var c = id.indexOf(':')
		return id.substring(0, c) + ':worldgen/structure_set/' + id.substring(c + 1) + '.json'
	}

	function structurePath(id) {
		var c = id.indexOf(':')
		return id.substring(0, c) + ':worldgen/structure/' + id.substring(c + 1) + '.json'
	}

	function isInt(v) {
		return typeof v === 'number' && isFinite(v) && Math.floor(v) === v
	}

	function code(id) {
		return '`' + id + '`'
	}

	function median(values) {
		if (!values.length) return DEFAULT_WEIGHT
		var s = values.slice().sort(function (a, b) { return a - b })
		return s[Math.floor(s.length / 2)]
	}

	function round4(x) {
		return Math.round(x * 1e4) / 1e4
	}

	// ── tags ─────────────────────────────────────────────────────────────────

	function globToRegex(glob) {
		var esc = glob.split('*').map(function (part) { return part.replace(/[.+?^${}()|[\]\\]/g, '\\$&') })
		return new RegExp('^' + esc.join('.*') + '$')
	}

	// -> { members: {tag: {id: true}}, errors: [], warnings: [] }
	function resolveTags(tagDefs, allIds) {
		var idSet = {}
		allIds.forEach(function (id) { idSet[id] = true })
		var members = {}
		var resolving = {}
		var errors = []
		var warnings = []

		function resolve(name, from) {
			if (members[name]) return members[name]
			var def = tagDefs[name]
			if (!def) {
				errors.push(from + ': unknown tag "#' + name + '"')
				return {}
			}
			if (resolving[name]) {
				errors.push(from + ': tag cycle through "#' + name + '"')
				return {}
			}
			resolving[name] = true
			var include = {}
			var exclude = {}
			;(def.values || []).forEach(function (raw) {
				var negate = raw.charAt(0) === '!'
				var v = negate ? raw.substring(1) : raw
				var ids
				if (v.charAt(0) === '#') {
					ids = Object.keys(resolve(v.substring(1), name))
				} else if (v.indexOf('*') >= 0) {
					var re = globToRegex(v)
					ids = allIds.filter(function (id) { return re.test(id) })
					if (!ids.length) warnings.push(name + ': "' + raw + '" matches no structure')
				} else if (idSet[v]) {
					ids = [v]
				} else {
					errors.push(name + ': unknown structure "' + v + '"')
					ids = []
				}
				ids.forEach(function (id) { (negate ? exclude : include)[id] = true })
			})
			delete resolving[name]
			var result = {}
			Object.keys(include).forEach(function (id) { if (!exclude[id]) result[id] = true })
			members[name] = result
			return result
		}

		Object.keys(tagDefs).forEach(function (name) {
			if ('multiplier' in tagDefs[name]) warnings.push(name + ': "multiplier" is ignored (rates live in structure_groups.json since 2026-09-27)')
			resolve(name, name)
		})
		return { members: members, errors: errors, warnings: warnings }
	}

	// ── validation ───────────────────────────────────────────────────────────

	function validatePlacement(where, p, knownSets) {
		var errs = []
		if ('spacing' in p || 'separation' in p) {
			if (!isInt(p.spacing) || p.spacing < 1 || p.spacing > MAX_SPACING) errs.push(where + ': spacing must be a whole number 1..' + MAX_SPACING + ' (got ' + p.spacing + ')')
			else if (!isInt(p.separation) || p.separation < 0 || p.separation >= p.spacing) errs.push(where + ': separation must be a whole number from 0 to spacing - 1 (got ' + p.separation + ', spacing ' + p.spacing + ')')
		}
		if ('salt' in p && (!isInt(p.salt) || p.salt < 0)) errs.push(where + ': salt must be a whole number >= 0')
		if ('frequency' in p && !(typeof p.frequency === 'number' && p.frequency >= 0 && p.frequency <= 1)) errs.push(where + ': frequency ' + p.frequency + ' outside 0..1')
		if ('count' in p && !(isInt(p.count) && p.count >= 1 && p.count <= MAX_COUNT)) errs.push(where + ': count ' + p.count + ' outside 1..' + MAX_COUNT)
		if ('spread_type' in p && ['linear', 'triangular'].indexOf(p.spread_type) < 0) errs.push(where + ': spread_type must be "linear" or "triangular"')
		if ('frequency_reduction_method' in p && REDUCTION_METHODS.indexOf(p.frequency_reduction_method) < 0) errs.push(where + ': frequency_reduction_method must be one of ' + REDUCTION_METHODS.join(', '))
		if ('locate_offset' in p && !(Array.isArray(p.locate_offset) && p.locate_offset.length === 3 && p.locate_offset.every(isInt))) errs.push(where + ': locate_offset must be [x, y, z] whole numbers')
		if ('exclusion_zone' in p) {
			var ez = p.exclusion_zone
			if (!ez || typeof ez !== 'object') errs.push(where + ': exclusion_zone must be {"other_set": id, "chunk_count": n}')
			else {
				if (!knownSets[ez.other_set]) errs.push(where + ': exclusion_zone.other_set "' + ez.other_set + '" is not a structure set (a set id from structure_config.json, or ' + GROUP_SET_PREFIX + '<group>)')
				if (!isInt(ez.chunk_count) || ez.chunk_count < 1 || ez.chunk_count > MAX_EXCLUSION) errs.push(where + ': exclusion_zone.chunk_count must be 1..' + MAX_EXCLUSION)
			}
		}
		return errs
	}

	// -> { settings, errs }
	function memberSettings(where, raw, defaults) {
		var errs = []
		var s = assign({}, defaults)
		if (raw === undefined) return { settings: s, errs: errs }
		if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return { settings: s, errs: [where + ': must be an object like {"weight": 10, "frequency": 0.5}'] }
		Object.keys(raw).forEach(function (k) {
			if (MEMBER_KEYS.indexOf(k) < 0) errs.push(where + ': unknown field "' + k + '" (allowed: ' + MEMBER_KEYS.join(', ') + ')')
		})
		if ('weight' in raw) {
			if (!isInt(raw.weight) || raw.weight < 1) errs.push(where + ': weight must be a whole number >= 1 (got ' + raw.weight + ')')
			else s.weight = raw.weight
		}
		if ('frequency' in raw) {
			if (!(typeof raw.frequency === 'number' && raw.frequency >= 0 && raw.frequency <= 1)) errs.push(where + ': frequency must be 0..1 (got ' + raw.frequency + ')')
			else s.frequency = raw.frequency
		}
		if ('enabled' in raw) {
			if (typeof raw.enabled !== 'boolean') errs.push(where + ': enabled must be true or false')
			else s.enabled = raw.enabled
		}
		if (('replaces' in raw) !== ('share' in raw)) errs.push(where + ': "replaces" and "share" go together')
		else if ('replaces' in raw) {
			if (typeof raw.replaces !== 'string' || !/^[a-z0-9_.-]+:[a-z0-9_.\/-]+$/.test(raw.replaces)) errs.push(where + ': replaces must be a structure id')
			else if (!(typeof raw.share === 'number' && raw.share >= 0 && raw.share <= 1)) errs.push(where + ': share must be 0..1 (got ' + raw.share + ')')
			else { s.replaces = raw.replaces; s.share = raw.share }
		}
		s.auto = false
		return { settings: s, errs: errs }
	}

	// ── output shape ─────────────────────────────────────────────────────────

	// The JSON "placement" object: type first, defaults dropped.
	function outputPlacement(p) {
		var out = { type: p.type }
		Object.keys(p).forEach(function (k) {
			if (k === 'type') return
			if (k === 'frequency' && p[k] === 1) return
			if (k === 'locate_offset' && Array.isArray(p[k]) && p[k].every(function (v) { return v === 0 })) return
			out[k] = p[k]
		})
		return out
	}

	function gridDensity(p) {
		if (typeof p.spacing !== 'number') return null
		return ('frequency' in p ? p.frequency : 1) / (p.spacing * p.spacing)
	}

	// debug.structureDensity: the set's expected rate x m -> { placement, capped }.
	//   m < 1: frequency x m (exact; spacing, separation and gaps unchanged).
	//   m > 1, grids: spacing and separation / sqrt(m), rounded down, and the frequency absorbs the rounding
	//          (exact), unless the spacing hits separation + 1 (capped: as dense as that grid gets).
	//   m > 1, concentric rings: count x m.
	// legacy_type_1 rounds 1/frequency to a whole number, so a changed frequency switches it to default.
	function scalePlacement(p, m) {
		if (m === 1) return { placement: p, capped: false }
		var q = assign({}, p)
		var f = 'frequency' in q ? q.frequency : 1
		var capped = false
		if (m < 1) {
			q.frequency = f * m
		} else if (isInt(q.spacing)) {
			var root = Math.sqrt(m)
			var sep = Math.floor(q.separation / root)
			var spacing = Math.max(sep + 1, Math.floor(q.spacing / root))
			var nf = f * m * (spacing * spacing) / (q.spacing * q.spacing)
			if (nf > 1 + 1e-9) capped = true
			q.spacing = spacing
			q.separation = sep
			q.frequency = Math.min(1, nf)
		} else if (isInt(q.count)) {
			q.count = Math.round(q.count * m)
			if (q.count > MAX_COUNT) { q.count = MAX_COUNT; capped = true }
		} else {
			capped = true
		}
		if ('frequency' in q) q.frequency = Math.max(1e-6, Math.round(q.frequency * 1e6) / 1e6)
		if (q.frequency !== f && q.frequency_reduction_method === 'legacy_type_1') q.frequency_reduction_method = 'default'
		return { placement: q, capped: capped }
	}

	// "one per N x N chunks" where the structure is the only member that fits
	function aloneSpacing(p, frequency) {
		var d = gridDensity(p)
		if (d === null || !(d * frequency > 0)) return '-'
		return String(Math.round(Math.sqrt(1 / (d * frequency))))
	}

	// ── planning ─────────────────────────────────────────────────────────────

	// biome_rules -> { name: { biomes: [...], allow: [structure ids], note } }; problems go to errors
	function planBiomeRules(rulesDoc, tagDefs, tagMembers, errors) {
		var out = {}
		var biomeRe = /^#?[a-z0-9_.-]+:[a-z0-9_.\/-]+$/
		Object.keys(rulesDoc).forEach(function (name) {
			var r = rulesDoc[name]
			var where = 'structure_groups.json biome_rules.' + name
			if (!r || !Array.isArray(r.biomes) || !r.biomes.length || !r.biomes.every(function (b) { return typeof b === 'string' && biomeRe.test(b) })) {
				errors.push(where + '.biomes must be a non-empty list of biome ids or #biome_tags')
				return
			}
			if (typeof r.allow !== 'string' || r.allow.charAt(0) !== '#' || !tagMembers[r.allow.substring(1)]) {
				errors.push(where + '.allow must be "#<tag>" naming a tag in structure_tags.json (got ' + JSON.stringify(r.allow) + ')')
				return
			}
			var surface = []
			if ('surface_check' in r) {
				if (typeof r.surface_check !== 'string' || r.surface_check.charAt(0) !== '#' || !tagMembers[r.surface_check.substring(1)]) {
					errors.push(where + '.surface_check must be "#<tag>" naming a tag in structure_tags.json (got ' + JSON.stringify(r.surface_check) + ')')
					return
				}
				surface = Object.keys(tagMembers[r.surface_check.substring(1)]).sort()
			}
			out[name] = { biomes: r.biomes.slice(), allow: Object.keys(tagMembers[r.allow.substring(1)]).sort(), tag: r.allow, note: r.note,
				surface: surface, surfaceTag: r.surface_check }
		})
		return out
	}

	// ── biome fits ───────────────────────────────────────────────────────────

	var FIT_NAME = /^[a-z0-9_.-]+(\/[a-z0-9_.-]+)*$/
	var FIT_KEYS = ['note', 'biomes', 'inherit']

	function readFits() {
		var doc = readJson(FITS_PATH)
		return (doc && doc.fits) || {}
	}

	// The fits a structure in fit tag `name` takes, nearest first: the fit, then (inherit) its nearest parent fit's chain.
	function fitChain(fits, name) {
		var chain = [name]
		if (fits[name].inherit === false) return chain
		var parts = name.split('/')
		for (var n = parts.length - 1; n > 0; n--) {
			var parent = parts.slice(0, n).join('/')
			if (fits[parent]) return chain.concat(fitChain(fits, parent))
		}
		return chain
	}

	// biome_fits.json -> { fits: {name: {biomes, inherit, note, chain, structures}}, structures: {id: [fit names]} }.
	// Problems go to errors (any applies no fit); a structure tag that looks like a fit but isn't one is a warning.
	function planBiomeFits(fitsDoc, tagDefs, tagMembers, errors, warnings) {
		var biomeRe = /^#?[a-z0-9_.-]+:[a-z0-9_.\/-]+$/
		var fits = {}
		Object.keys(fitsDoc).forEach(function (name) {
			var f = fitsDoc[name]
			var where = 'biome_fits.json fits.' + name
			if (!FIT_NAME.test(name)) return errors.push(where + ': a fit name is a tag path like land/oak')
			if (!f || typeof f !== 'object') return errors.push(where + ' must be an object')
			Object.keys(f).forEach(function (k) { if (FIT_KEYS.indexOf(k) < 0) errors.push(where + ': unknown field "' + k + '"') })
			if (!Array.isArray(f.biomes) || !f.biomes.every(function (b) { return typeof b === 'string' && biomeRe.test(b) }))
				return errors.push(where + '.biomes must be a list of biome ids or #biome_tags')
			if ('inherit' in f && typeof f.inherit !== 'boolean') return errors.push(where + '.inherit must be true or false')
			if (!tagDefs[name]) return errors.push(where + ': structure_tags.json has no tag "' + name + '" (its structures; may be empty)')
			if (!f.biomes.length && Object.keys(tagMembers[name] || {}).length) warnings.push(where + ' lists no biome: its structures only spawn in its parent fits\' biomes, or never')
			fits[name] = { biomes: f.biomes.slice(), inherit: f.inherit !== false, note: f.note }
		})
		var roots = {}
		Object.keys(fits).forEach(function (name) { roots[name.split('/')[0]] = true })
		Object.keys(tagDefs).forEach(function (t) {
			if (!fits[t] && t.indexOf('/') > 0 && roots[t.split('/')[0]] && FIT_NAME.test(t))
				warnings.push('structure_tags.json ' + t + ' looks like a biome fit, but biome_fits.json has no fit "' + t + '": ignored')
		})
		var structures = {}
		Object.keys(fits).forEach(function (name) {
			var chain = fitChain(fits, name)
			fits[name].chain = chain
			fits[name].structures = Object.keys(tagMembers[name] || {}).sort()
			fits[name].structures.forEach(function (id) {
				var list = structures[id] || (structures[id] = [])
				chain.forEach(function (c) { if (list.indexOf(c) < 0) list.push(c) })
			})
		})
		return { fits: fits, structures: structures }
	}

	// What StructureBiomeFits.stage takes: every fit's listed biomes + each structure's fit names.
	function fitStage(biomeFits) {
		var fits = {}
		Object.keys(biomeFits.fits).forEach(function (name) { fits[name] = biomeFits.fits[name].biomes })
		return { fits: fits, structures: biomeFits.structures }
	}

	// ── meta structures ──────────────────────────────────────────────────────

	// {ok} when the meta structure switch is on, else {ok: false, why}
	function metaSupport() {
		if (WorldgenConfig.metaStructuresEnabled() != true) return { ok: false, why: 'switched off ([worldgen.systems] metaStructures)' }
		return { ok: true }
	}

	// {ok} when the replacement switch is on, else {ok: false, why}
	function replacementSupport() {
		if (WorldgenConfig.structureReplacementsEnabled() != true) return { ok: false, why: 'switched off ([worldgen.systems] structureReplacements)' }
		return { ok: true }
	}

	// structure_groups.json "priority" -> {ranks: {id: n}, companions: {meta: {...}}, groups, overrides}
	function planPriority(doc, groupNames, tagMembers, owner, metaPlans, errors, warnings) {
		var pr = doc.priority || {}
		var out = { ranks: {}, companions: {}, groups: {}, overrides: {}, shaped: [], shapeGroups: [],
			radius: {}, hostile: [], peaceGroups: {}, peaceOverrides: {}, hostileKeys: [] }
		var groupRanks = pr.groups || {}
		Object.keys(pr).forEach(function (k) {
			if (['_readme', 'groups', 'structures', 'shape', 'peaceful'].indexOf(k) < 0) errors.push('priority: unknown field "' + k + '"')
		})
		Object.keys(groupRanks).forEach(function (g) {
			if (!isInt(groupRanks[g])) return errors.push('priority.groups.' + g + ' must be a whole number')
			if (groupNames.indexOf(g) < 0) return warnings.push('priority.groups.' + g + ': no such group (ignored)')
			out.groups[g] = groupRanks[g]
			Object.keys(tagMembers['group/' + g] || {}).forEach(function (id) { if (owner[id] === g) out.ranks[id] = groupRanks[g] })
		})
		Object.keys(pr.structures || {}).forEach(function (k) {
			var r = pr.structures[k]
			if (!isInt(r)) return errors.push('priority.structures.' + k + ' must be a whole number')
			var ids = k.charAt(0) === '#' ? Object.keys(tagMembers[k.substring(1)] || {}) : [k]
			if (k.charAt(0) === '#' && !tagMembers[k.substring(1)]) return errors.push('priority.structures: no tag ' + k + ' in structure_tags.json')
			out.overrides[k] = r
			ids.forEach(function (id) { if (owner[id]) out.ranks[id] = r })
		})
		if ('shape' in pr) {
			if (!Array.isArray(pr.shape)) errors.push('priority.shape must be a list of group names')
			else pr.shape.forEach(function (g) {
				if (groupNames.indexOf(g) < 0) return warnings.push('priority.shape: no group ' + g + ' (ignored)')
				out.shapeGroups.push(g)
				Object.keys(tagMembers['group/' + g] || {}).forEach(function (id) { if (owner[id] === g && out.shaped.indexOf(id) < 0) out.shaped.push(id) })
			})
			out.shaped.sort()
		}
		if ('peaceful' in pr) planPeaceful(pr.peaceful, out, groupNames, tagMembers, owner, errors, warnings)
		metaPlans.forEach(function (mp) {
			if (mp.off || !mp.def || !mp.def.companions || !owner[mp.id]) return
			var g = owner[mp.id]
			var cp = mp.def.companions
			var ids = []
			entryList(cp.structures).forEach(function (e) {
				var t = e.indexOf('#creativeengineering:') === 0 ? e.substring('#creativeengineering:'.length) : null
				if (t) Object.keys(tagMembers[t] || {}).forEach(function (a) { ids.push(a) })
				else if (e.charAt(0) !== '#') ids.push(e)
			})
			ids = ids.filter(function (id, i) { return ids.indexOf(id) === i && owner[id] === g }).sort()
			if (!ids.length) return warnings.push(mp.id + ' companions: none of its companion structures is in group/' + g + ', so none spawn')
			if (!isInt(groupRanks[g])) return warnings.push(mp.id + ' companions: group/' + g + ' has no priority rank, so none spawn')
			var count = isInt(cp.count) ? [cp.count, cp.count] : cp.count
			out.companions[mp.id] = { structures: ids, count: count, distance: [Math.max(1, Math.round(cp.distance[0] / 16)), Math.round(cp.distance[1] / 16)],
				rank: groupRanks[g] - 1 }
		})
		return out
	}

	// priority.peaceful -> out.radius {id: chunks} (groups, then structures over them) and out.hostile [ids]
	function planPeaceful(pc, out, groupNames, tagMembers, owner, errors, warnings) {
		if (!pc || typeof pc !== 'object' || Array.isArray(pc)) return errors.push('priority.peaceful must be an object')
		Object.keys(pc).forEach(function (k) {
			if (['_readme', 'groups', 'structures', 'hostile'].indexOf(k) < 0) errors.push('priority.peaceful: unknown field "' + k + '"')
		})
		function radiusOk(where, n) {
			if (isInt(n) && n >= 1 && n <= PEACE_MAX_RADIUS) return true
			errors.push(where + ' must be a whole number of chunks from 1 to ' + PEACE_MAX_RADIUS)
			return false
		}
		// "#tag", "group name" or an id -> ids
		function members(where, k) {
			if (k.charAt(0) === '#') {
				if (!tagMembers[k.substring(1)]) { errors.push(where + ': no tag ' + k + ' in structure_tags.json'); return [] }
				return Object.keys(tagMembers[k.substring(1)])
			}
			if (groupNames.indexOf(k) >= 0) return Object.keys(tagMembers['group/' + k] || {}).filter(function (id) { return owner[id] === k })
			if (k.indexOf(':') < 0) { warnings.push(where + ': no group ' + k + ' (ignored)'); return [] }
			return [k]
		}
		var groups = pc.groups || {}
		Object.keys(groups).forEach(function (g) {
			if (!radiusOk('priority.peaceful.groups.' + g, groups[g])) return
			if (groupNames.indexOf(g) < 0) return warnings.push('priority.peaceful.groups.' + g + ': no such group (ignored)')
			out.peaceGroups[g] = groups[g]
			members('priority.peaceful.groups', g).forEach(function (id) { out.radius[id] = groups[g] })
		})
		var structs = pc.structures || {}
		Object.keys(structs).forEach(function (k) {
			if (!radiusOk('priority.peaceful.structures.' + k, structs[k])) return
			out.peaceOverrides[k] = structs[k]
			members('priority.peaceful.structures', k).forEach(function (id) { out.radius[id] = structs[k] })
		})
		if ('hostile' in pc) {
			if (!Array.isArray(pc.hostile)) return errors.push('priority.peaceful.hostile must be a list of group names, "#tag"s or ids')
			pc.hostile.forEach(function (k) {
				out.hostileKeys.push(k)
				members('priority.peaceful.hostile', k).forEach(function (id) {
					if (id in out.radius) return warnings.push('priority.peaceful: ' + id + ' is both peaceful and hostile (kept peaceful)')
					if (out.hostile.indexOf(id) < 0) out.hostile.push(id)
				})
			})
			out.hostile.sort()
		}
		if (Object.keys(out.radius).length && !out.hostile.length) warnings.push('priority.peaceful: radii but no hostile structures, so nothing is kept away')
	}

	// A group's biome_frequency -> the ordered rules object StructureFrequencies.stageBiomeFactors takes, or null (errors)
	function planBiomeFrequency(where, raw, errors) {
		var ruleRe = /^#?[a-z0-9_.-]+:[a-z0-9_.\/-]+$/
		if (!raw || typeof raw !== 'object' || Array.isArray(raw) || !Object.keys(raw).length) {
			errors.push(where + ' must be an object of "#biome_tag" / "biome id" / "default" -> factor 0..1')
			return null
		}
		var out = {}
		var ok = true
		Object.keys(raw).forEach(function (k) {
			var v = raw[k]
			if (k !== 'default' && !ruleRe.test(k)) { errors.push(where + ': "' + k + '" is not a biome id, #biome_tag or "default"'); ok = false }
			else if (typeof v !== 'number' || !(v >= 0 && v <= 1)) { errors.push(where + '.' + k + ' must be a number 0..1'); ok = false }
			else out[k] = v
		})
		return ok ? out : null
	}

	function isRange(v, max) {
		if (isInt(v)) return v >= 0 && v <= max
		return Array.isArray(v) && v.length === 2 && isInt(v[0]) && isInt(v[1]) && v[0] >= 0 && v[0] <= v[1] && v[1] <= max
	}

	function entryList(v) {
		if (typeof v === 'string') return [v]
		if (Array.isArray(v) && v.length && v.every(function (e) { return typeof e === 'string' })) return v
		return null
	}

	// Problems with one definition (it is then left out, the rest still apply). Checks what the Java codec would
	// reject, so a typo can't fail the world load, plus #creativeengineering:meta/* tags that structure_tags.json lacks.
	function metaProblems(id, def, tagDefs) {
		var errs = []
		if (!def || typeof def !== 'object' || Array.isArray(def)) return ['must be an object']
		Object.keys(def).forEach(function (k) { if (META_KEYS.indexOf(k) < 0) errs.push('unknown field "' + k + '"') })
		if (def.type !== META_TYPE) errs.push('type must be "' + META_TYPE + '"')
		if (id.indexOf('creativeengineering:') !== 0) errs.push('the id must be in the creativeengineering namespace')
		;['biomes', 'step', 'spawn_overrides'].forEach(function (k) { if (!(k in def)) errs.push('needs "' + k + '"') })
		function checkEntries(where, v) {
			var list = entryList(v)
			if (!list) return errs.push(where + ' must be "#tag", an id, or a list of them')
			list.forEach(function (e) {
				var tagPrefix = '#creativeengineering:' + META_TAG_PREFIX
				if (e.indexOf(tagPrefix) === 0 && !tagDefs[e.substring('#creativeengineering:'.length)]) {
					errs.push(where + ': ' + e + ' has no ' + e.substring('#creativeengineering:'.length) + ' tag in structure_tags.json')
				}
			})
		}
		if ('anchor' in def) checkEntries('anchor', def.anchor)
		if ('anchor' in def && !('share' in def)) errs.push('needs "share" (0..1, the share of its anchors\' slots it takes) because it has an anchor')
		if ('share' in def && !('anchor' in def)) errs.push('"share" needs an "anchor" (a meta without one is an ordinary group member)')
		if ('share' in def && !(typeof def.share === 'number' && def.share >= 0 && def.share <= 1)) errs.push('share must be 0..1')
		if ('companions' in def) {
			var cp = def.companions
			if (!cp || typeof cp !== 'object' || Array.isArray(cp)) errs.push('companions must be an object')
			else {
				Object.keys(cp).forEach(function (k) { if (COMPANION_KEYS.indexOf(k) < 0) errs.push('companions: unknown field "' + k + '"') })
				checkEntries('companions.structures', cp.structures)
				if (!isRange(cp.count, 8)) errs.push('companions.count must be n or [min, max] (at most 8)')
				if (!(Array.isArray(cp.distance) && isRange(cp.distance, 512) && cp.distance[0] >= 16)) errs.push('companions.distance must be [min, max] blocks, 16..512')
				if (!('anchor' in def)) errs.push('companions need an anchor (they surround the slot the anchor won)')
			}
		}
		if (!Array.isArray(def.parts) || (!def.parts.length && !('companions' in def))) errs.push('needs a non-empty "parts" list (empty is allowed with "companions")')
		else def.parts.forEach(function (part, n) {
			var where = 'parts[' + n + ']'
			if (!part || typeof part !== 'object') return errs.push(where + ' must be an object')
			Object.keys(part).forEach(function (k) { if (META_PART_KEYS.indexOf(k) < 0) errs.push(where + ': unknown field "' + k + '"') })
			if (('structures' in part) === ('features' in part)) errs.push(where + ' needs exactly one of "structures" and "features"')
			if ('structures' in part) checkEntries(where + '.structures', part.structures)
			if ('features' in part) checkEntries(where + '.features', part.features)
			if ('count' in part && !isRange(part.count, 4096)) errs.push(where + '.count must be n or [min, max]')
			if ('distance' in part && !(Array.isArray(part.distance) && isRange(part.distance, META_MAX_DISTANCE))) errs.push(where + '.distance must be [min, max] with max <= ' + META_MAX_DISTANCE)
			if ('chance' in part && !(typeof part.chance === 'number' && part.chance >= 0 && part.chance <= 1)) errs.push(where + '.chance must be 0..1')
			if ('overlap' in part && typeof part.overlap !== 'boolean') errs.push(where + '.overlap must be true or false')
			if ('cluster' in part && !(isInt(part.cluster) && part.cluster >= 0 && part.cluster <= META_MAX_DISTANCE)) errs.push(where + '.cluster must be a whole number 0..' + META_MAX_DISTANCE)
			if ('cluster' in part && 'features' in part) errs.push(where + ': cluster is for structure parts')
			;[['spacing', 0, 256], ['margin', 0, 64], ['tries', 1, 64], ['water_depth', 0, 64], ['radius', 0, 16]].forEach(function (f) {
				if (f[0] in part && !(isInt(part[f[0]]) && part[f[0]] >= f[1] && part[f[0]] <= f[2])) errs.push(where + '.' + f[0] + ' must be a whole number ' + f[1] + '..' + f[2])
			})
		})
		return errs
	}

	// the structure json the game reads: the definition without our note
	function metaJson(def) {
		var out = {}
		Object.keys(def).forEach(function (k) { if (k !== 'note' && k !== 'tuned' && k !== 'companions') out[k] = def[k] })
		return toJson(out)
	}

	// every structure id the tags may name: the baseline's set members, then our meta structures
	function structureIds(config, metaDefs) {
		var ids = []
		var seen = {}
		Object.keys(config.structure_sets).forEach(function (id) {
			config.structure_sets[id].structures.forEach(function (s) {
				if (!seen[s.structure]) { seen[s.structure] = true; ids.push(s.structure) }
			})
		})
		Object.keys(config.meta_structures || {}).concat(Object.keys(metaDefs)).forEach(function (id) {
			if (!seen[id]) { seen[id] = true; ids.push(id) }
		})
		return ids
	}

	// CEConfig [worldgen.debug] testOverrides: structure_test_overrides.json changes the raw configs in memory before
	// they are planned, so every check below still runs. Soft rules only (TEST_RULE): any other key refuses the whole
	// file (nothing from it applied, the world loads as without it). Keys: "_readme"; "groups": {group: {spacing,
	// separation}}; "<structure id>" or "#<structure tag>": {frequency 0..1, weight >= 1, share 0..1 (metas with an
	// anchor, replacements)}. Tags first, then ids, so an id overrides its tag. -> { applied: [text], refused }
	function applyTestOverrides(doc, metaDefs, tagMembers, setOf, warnings) {
		var raw = readJson(TEST_PATH)
		var out = { applied: [], refused: false }
		if (!raw) { warnings.push('TEST OVERRIDES switched on but ' + TEST_PATH + ' is missing or empty: nothing loosened'); return out }
		var errs = []
		var groups = doc.groups || {}
		var standalone = doc.standalone || {}
		var groupOf = {}
		Object.keys(tagMembers).forEach(function (t) {
			if (t.indexOf('group/') === 0) Object.keys(tagMembers[t]).forEach(function (id) { groupOf[id] = t.substring(6) })
		})
		var todo = []   // [order, id, values, key]
		Object.keys(raw).forEach(function (k) {
			if (k === '_readme' || k === 'groups') return
			var v = raw[k]
			if (!v || typeof v !== 'object' || Array.isArray(v)) return errs.push(k + ': must be an object like {"frequency": 1}')
			Object.keys(v).forEach(function (f) { if (TEST_MEMBER_KEYS.indexOf(f) < 0) errs.push(k + ': "' + f + '" refused (' + TEST_RULE + ')') })
			if ('frequency' in v && !(typeof v.frequency === 'number' && v.frequency >= 0 && v.frequency <= 1)) errs.push(k + ': frequency must be 0..1')
			if ('weight' in v && !(isInt(v.weight) && v.weight >= 1)) errs.push(k + ': weight must be a whole number >= 1')
			if ('share' in v && !(typeof v.share === 'number' && v.share >= 0 && v.share <= 1)) errs.push(k + ': share must be 0..1')
			var ids
			if (k.charAt(0) === '#') {
				if (!tagMembers[k.substring(1)]) return errs.push(k + ': no tag ' + k.substring(1) + ' in structure_tags.json')
				ids = Object.keys(tagMembers[k.substring(1)]).sort()
			} else if (/^[a-z0-9_.-]+:[a-z0-9_.\/-]+$/.test(k)) ids = [k]
			else return errs.push('"' + k + '": not a structure id, "#tag", "groups" or "_readme" (' + TEST_RULE + ')')
			ids.forEach(function (id) { todo.push([k.charAt(0) === '#' ? 0 : 1, id, v, k]) })
		})
		var rg = raw.groups || {}
		if (typeof rg !== 'object' || Array.isArray(rg)) errs.push('groups: must be an object {group: {spacing, separation}}')
		else Object.keys(rg).forEach(function (g) {
			var v = rg[g]
			if (!groups[g] || !groups[g].placement) return errs.push('groups.' + g + ': no such group in structure_groups.json')
			if (!v || typeof v !== 'object' || Array.isArray(v)) return errs.push('groups.' + g + ': must be an object like {"spacing": 20}')
			Object.keys(v).forEach(function (f) { if (TEST_GROUP_KEYS.indexOf(f) < 0) errs.push('groups.' + g + ': "' + f + '" refused (' + TEST_RULE + ')') })
			var sp = 'spacing' in v ? v.spacing : groups[g].placement.spacing
			var se = 'separation' in v ? v.separation : groups[g].placement.separation
			if (!(isInt(sp) && sp >= 1 && sp <= MAX_SPACING && isInt(se) && se >= 0 && se < sp)) errs.push('groups.' + g + ': needs whole numbers with 0 <= separation < spacing <= ' + MAX_SPACING + ' (got ' + sp + ' / ' + se + ')')
		})
		// where each id lives: a member entry of its group, or of its standalone set; a meta's share in meta_structures.json
		function entryOf(id) {
			var g = groupOf[id]
			if (g && groups[g]) {
				var st = groups[g].structures = groups[g].structures || {}
				return { where: 'group/' + g, e: st[id] = st[id] || {} }
			}
			if (!g && setOf[id] && standalone[setOf[id]] !== undefined) {
				var st2 = standalone[setOf[id]].structures = standalone[setOf[id]].structures || {}
				return { where: setOf[id], e: st2[id] = st2[id] || {} }
			}
			if (!g && setOf[id]) {
				standalone[setOf[id]] = { structures: {} }
				doc.standalone = standalone
				return { where: setOf[id], e: standalone[setOf[id]].structures[id] = {} }
			}
			return null
		}
		todo.forEach(function (t) {
			var id = t[1], v = t[2]
			if ('share' in v) {
				var isMeta = metaDefs[id] && 'anchor' in metaDefs[id]
				var g = groupOf[id]
				var isRepl = g && groups[g] && groups[g].structures && groups[g].structures[id] && 'replaces' in groups[g].structures[id]
				if (!isMeta && !isRepl && t[0] === 1) errs.push(t[3] + ': share only works on a meta structure with an anchor or a replacement')
			}
			if (('frequency' in v || 'weight' in v) && !groupOf[id] && !setOf[id] && t[0] === 1) errs.push(t[3] + ': ' + id + ' is in no group and no structure set')
		})
		if (errs.length) {
			out.refused = true
			errs.forEach(function (e) { warnings.push('TEST OVERRIDES REFUSED, nothing from ' + TEST_PATH + ' applied: ' + e) })
			return out
		}
		Object.keys(rg).sort().forEach(function (g) {
			var p = groups[g].placement
			TEST_GROUP_KEYS.forEach(function (f) {
				if (f in rg[g] && p[f] !== rg[g][f]) { out.applied.push('group/' + g + ' ' + f + ' ' + p[f] + ' -> ' + rg[g][f]); p[f] = rg[g][f] }
			})
		})
		todo.sort(function (a, b) { return a[0] - b[0] || (a[1] < b[1] ? -1 : a[1] > b[1] ? 1 : 0) })
		todo.forEach(function (t) {
			var id = t[1], v = t[2], from = t[0] ? '' : ' (' + t[3] + ')'
			if ('share' in v) {
				if (metaDefs[id] && 'anchor' in metaDefs[id]) {
					if (metaDefs[id].share !== v.share) out.applied.push(id + ' share ' + metaDefs[id].share + ' -> ' + v.share + from)
					metaDefs[id].share = v.share
				} else {
					var g = groupOf[id]
					var r = g && groups[g] && groups[g].structures && groups[g].structures[id]
					if (r && 'replaces' in r) {
						if (r.share !== v.share) out.applied.push(id + ' share ' + r.share + ' -> ' + v.share + from)
						r.share = v.share
					}
				}
			}
			if (!('frequency' in v) && !('weight' in v)) return
			var at = entryOf(id)
			if (!at) return
			;['frequency', 'weight'].forEach(function (f) {
				if (!(f in v)) return
				var old = f in at.e ? at.e[f] : '(default)'
				if (old !== v[f]) out.applied.push(id + ' ' + f + ' ' + old + ' -> ' + v[f] + ' in ' + at.where + from)
				at.e[f] = v[f]
			})
		})
		return out
	}

	function plan() {
		var settings = worldgenSettings()
		var density = settings.density
		var capped = []
		var config = readJson(CONFIG_PATH)
		if (!config) return { errors: ['could not read ' + CONFIG_PATH + ' (run python3 tools/gen_structure_config.py)'] }
		var sets = config.structure_sets
		var tagsDoc = readJson(TAGS_PATH)
		var tagDefs = tagsDoc ? tagsDoc.tags : {}
		var doc = readJson(GROUPS_PATH)
		if (!doc) return { errors: ['could not read ' + GROUPS_PATH + ' (run python3 tools/gen_structure_groups.py)'] }
		var groupsDoc = settings.groups ? doc.groups || {} : {}
		var standaloneDoc = settings.groups ? doc.standalone || {} : {}
		var metaDoc = readJson(META_PATH)
		var metaDefs = (metaDoc && metaDoc.structures) || {}

		var allIds = []
		var setOf = {}
		var baseWeight = {}
		var blocked = {}
		Object.keys(sets).forEach(function (id) {
			sets[id].structures.forEach(function (s) {
				allIds.push(s.structure)
				if (!setOf[s.structure]) setOf[s.structure] = id
				baseWeight[s.structure] = s.weight
				if (s.blocked) blocked[s.structure] = s.blocked
			})
		})
		// meta structures: structures of our own with no set of their own (they only live in groups)
		Object.keys(config.meta_structures || {}).concat(Object.keys(metaDefs)).forEach(function (id) {
			if (!setOf[id] && allIds.indexOf(id) < 0) allIds.push(id)
		})

		var resolved = resolveTags(tagDefs, allIds)
		var warnings = resolved.warnings.slice()
		if (resolved.errors.length) {
			// Groups off: the tags only feed the report, so their errors don't stop the mod defaults.
			if (settings.groups) return { errors: resolved.errors, title: 'structure_tags.json has errors' }
			resolved.errors.forEach(function (e) { warnings.push('structure_tags.json: ' + e) })
		}
		var tagMembers = resolved.members
		var errors = []
		var test = settings.test ? applyTestOverrides(doc, metaDefs, tagMembers, setOf, warnings) : null

		// which meta structures can be built: staged as structure files, the rest kept out of every set
		var support = metaSupport()
		var metaPlans = []
		var metaOff = {}       // id -> why it can't generate
		var metaFiles = {}
		Object.keys(metaDefs).forEach(function (id) {
			var problems = metaProblems(id, metaDefs[id], tagDefs)
			problems.forEach(function (e) { warnings.push('meta_structures.json ' + id + ': ' + e + ' (left out)') })
			if (problems.length) metaOff[id] = 'errors in meta_structures.json'
			else if (!support.ok) metaOff[id] = support.why
			else metaFiles[structurePath(id)] = metaJson(metaDefs[id])
			metaPlans.push({ id: id, def: metaDefs[id], off: metaOff[id], problems: problems })
		})
		Object.keys(config.meta_structures || {}).forEach(function (id) {
			if (!metaDefs[id]) { metaOff[id] = 'not in meta_structures.json'; warnings.push(id + ' is listed in structure_config.json but has no definition in meta_structures.json: rerun python3 tools/gen_structure_config.py') }
		})
		if (!support.ok && metaPlans.length) warnings.push('meta structures not built: ' + support.why + '; their group slots go to the other members')
		var biomeRules = planBiomeRules(doc.biome_rules || {}, tagDefs, tagMembers, errors)
		var fitsDoc = readJson(FITS_PATH)
		if (!fitsDoc) warnings.push('could not read ' + FITS_PATH + ': no biome fits')
		var biomeFits = planBiomeFits((fitsDoc && fitsDoc.fits) || {}, tagDefs, tagMembers, errors, warnings)
		if (!settings.groups) warnings.push('structure groups are OFF (CEConfig [worldgen.systems] structureGroups): every structure set is its mod/vanilla default'
			+ (density !== 1 ? ' times the debug density' : ''))

		// group membership
		var groupNames = !settings.groups ? [] : Object.keys(tagDefs).filter(function (t) { return t.indexOf('group/') === 0 })
			.map(function (t) { return t.substring('group/'.length) })
		var owner = {}
		groupNames.forEach(function (g) {
			if (!groupsDoc[g]) errors.push('tag group/' + g + ' has no entry under "groups" in structure_groups.json (run python3 tools/gen_structure_groups.py)')
			Object.keys(tagMembers['group/' + g]).forEach(function (id) {
				if (owner[id]) errors.push(id + ' is in both group/' + owner[id] + ' and group/' + g + ' (a structure can be in one group)')
				else owner[id] = g
			})
		})
		Object.keys(groupsDoc).forEach(function (g) {
			if (groupNames.indexOf(g) < 0) errors.push('structure_groups.json group "' + g + '" has no tag group/' + g + ' in structure_tags.json')
		})
		if (settings.groups) allIds.forEach(function (id) {
			if (!setOf[id] && !owner[id]) warnings.push(id + ' (meta structure) is in no group, so it never generates')
		})
		// a meta with an anchor only takes slots its anchors win in its own group (patcherMod MetaSlots)
		var shareSum = {}
		if (settings.groups) metaPlans.forEach(function (mp) {
			if (mp.off || !('anchor' in mp.def) || !owner[mp.id]) return
			var anchors = []
			entryList(mp.def.anchor).forEach(function (e) {
				var t = e.indexOf('#creativeengineering:') === 0 ? e.substring('#creativeengineering:'.length) : null
				if (t) Object.keys(tagMembers[t] || {}).forEach(function (a) { anchors.push(a) })
				else if (e.charAt(0) !== '#') anchors.push(e)
			})
			var inGroup = anchors.filter(function (a) { return owner[a] === owner[mp.id] })
			if (!inGroup.length) warnings.push(mp.id + ' (meta structure): none of its anchors is in group/' + owner[mp.id] + ', so it never generates')
			inGroup.forEach(function (a) { shareSum[a] = (shareSum[a] || 0) + mp.def.share })
		})
		// replacements: checked here, their members left out of the group sets below when they can't be staged
		var replSupport = replacementSupport()
		var replacements = {}   // replacement id -> { replaces, share }
		var replOff = {}        // replacement id -> why it isn't staged
		if (settings.groups) groupNames.forEach(function (g) {
			var listed = (groupsDoc[g] && groupsDoc[g].structures) || {}
			Object.keys(listed).sort().forEach(function (id) {
				var r = listed[id]
				if (!r || typeof r !== 'object' || !('replaces' in r) || owner[id] !== g) return
				var where = 'groups.' + g + '.structures.' + id
				if (r.replaces === id) errors.push(where + ': replaces itself')
				else if (owner[r.replaces] !== g) errors.push(where + ': replaces ' + r.replaces + ', which is not in group/' + g + ' (a replacement takes slots in its own group)')
				else if ((metaDefs[id] && 'anchor' in metaDefs[id]) || metaDefs[r.replaces]) errors.push(where + ': a meta structure with an anchor can\'t replace, and no meta can be replaced (use "anchor" + "share" in meta_structures.json)')
				else if (listed[r.replaces] && typeof listed[r.replaces] === 'object' && 'replaces' in listed[r.replaces]) errors.push(where + ': replaces ' + r.replaces + ', which is a replacement itself')
				else if (typeof r.share === 'number' && r.enabled !== false) {
					if (!replSupport.ok) { replOff[id] = replSupport.why; return }
					replacements[id] = { replaces: r.replaces, share: r.share }
					shareSum[r.replaces] = (shareSum[r.replaces] || 0) + r.share
				}
			})
		})
		if (Object.keys(replOff).length) warnings.push('replacements not staged: ' + replSupport.why + '; ' + Object.keys(replOff).sort().join(', ')
			+ ' left out of their groups, the structures they replace keep every slot')
		Object.keys(shareSum).sort().forEach(function (a) {
			if (shareSum[a] > 1 + 1e-9) warnings.push(a + ': the meta structures and replacements it anchors take ' + Math.round(shareSum[a] * 100) + '% of its slots; the game cuts that to 100% in id order')
		})

		var priority = planPriority(doc, groupNames, tagMembers, owner, metaPlans, errors, warnings)

		var knownSets = {}
		Object.keys(sets).forEach(function (id) { knownSets[id] = true })
		groupNames.forEach(function (g) { knownSets[GROUP_SET_PREFIX + g] = true })

		var files = {}
		var frequencies = {}     // set id -> { structure id: frequency < 1 }
		var biomeFrequencies = {}   // set id -> { "#tag" | "biome" | "default": factor }, groups with biome_frequency
		var groupPlans = []
		var setPlans = []

		// groups
		groupNames.forEach(function (g) {
			var entry = groupsDoc[g]
			if (!entry) return
			var setId = GROUP_SET_PREFIX + g
			var raw = entry.placement || {}
			Object.keys(raw).forEach(function (k) {
				if (GROUP_PLACEMENT_KEYS.indexOf(k) < 0) errors.push('groups.' + g + '.placement: unknown field "' + k + '" (allowed: ' + GROUP_PLACEMENT_KEYS.join(', ') + ')')
			})
			if (!('spacing' in raw) || !('separation' in raw) || !('salt' in raw)) errors.push('groups.' + g + '.placement needs spacing, separation and salt')
			var p = assign({ type: 'minecraft:random_spread' }, raw)
			errors.push.apply(errors, validatePlacement('groups.' + g + '.placement', p, knownSets))

			var biomeFrequency = 'biome_frequency' in entry ? planBiomeFrequency('groups.' + g + '.biome_frequency', entry.biome_frequency, errors) : null
			var listed = entry.structures || {}
			var ids = Object.keys(tagMembers['group/' + g]).sort()
			Object.keys(listed).forEach(function (id) {
				if (owner[id] !== g) errors.push('groups.' + g + '.structures lists ' + id + ', which is not in group/' + g + (owner[id] ? ' (it is in group/' + owner[id] + ')' : ''))
			})
			var listedWeights = ids.filter(function (id) { return listed[id] && isInt(listed[id].weight) }).map(function (id) { return listed[id].weight })
			var autoWeight = median(listedWeights)
			var members = ids.map(function (id) {
				var auto = !(id in listed)
				var defaults = { weight: autoWeight, frequency: 1, enabled: true, auto: auto }
				if (auto) {
					// rough default until gen_structure_groups.py fits one: the rate its own set gave it
					var own = setOf[id] ? assign({}, sets[setOf[id]].original) : null
					var ownD = own ? gridDensity(own) : null
					var groupD = gridDensity(p)
					if (ownD !== null && groupD) defaults.frequency = round4(Math.min(1, ownD / groupD))
				}
				var r = memberSettings('groups.' + g + '.structures.' + id, listed[id], defaults)
				errors.push.apply(errors, r.errs)
				r.settings.auto = auto
				return assign({ structure: id, from: setOf[id] || '(meta structure)', blocked: blocked[id], metaOff: metaOff[id], replOff: replOff[id] }, r.settings)
			})
			var live = members.filter(function (m) { return m.enabled && m.frequency > 0 && !m.metaOff && !m.replOff })
			var scaled = scalePlacement(p, density)   // after the members: their auto defaults use the real grid
			if (scaled.capped) capped.push(setId)
			var plan = { name: g, id: setId, note: entry.note, placement: scaled.placement, members: members, live: live, biomeFrequency: biomeFrequency }
			groupPlans.push(plan)
			if (!live.length) return
			files[resourcePath(setId)] = toJson({
				placement: outputPlacement(scaled.placement),
				structures: live.map(function (m) { return { structure: m.structure, weight: m.weight } }),
			})
			live.forEach(function (m) {
				if (m.frequency < 1) (frequencies[setId] = frequencies[setId] || {})[m.structure] = m.frequency
			})
			if (biomeFrequency) biomeFrequencies[setId] = biomeFrequency
		})

		// every original set: members that moved to a group leave it; the rest follow "standalone"
		Object.keys(standaloneDoc).forEach(function (id) {
			if (!sets[id]) errors.push('standalone: "' + id + '" is not a structure set in structure_config.json')
		})
		Object.keys(sets).forEach(function (id) {
			var entry = sets[id]
			var sa = standaloneDoc[id] || {}
			var rest = entry.structures.filter(function (s) { return !owner[s.structure] })
			var moved = entry.structures.filter(function (s) { return owner[s.structure] })
				.map(function (s) { return { structure: s.structure, group: owner[s.structure] } })
			var p = assign({}, entry.original, sa.placement || {})
			if (rest.length) errors.push.apply(errors, validatePlacement('standalone.' + id + '.placement', p, knownSets))
			var listed = sa.structures || {}
			Object.keys(listed).forEach(function (sid) {
				if (!entry.structures.some(function (s) { return s.structure === sid })) errors.push('standalone.' + id + '.structures lists ' + sid + ', which is not in that set')
				else if (owner[sid]) warnings.push('standalone.' + id + '.structures still lists ' + sid + ', which is in group/' + owner[sid] + ' now: ignored (python3 tools/gen_structure_groups.py removes it)')
			})
			var members = rest.map(function (s) {
				var r = memberSettings('standalone.' + id + '.structures.' + s.structure, listed[s.structure],
					{ weight: s.weight, frequency: 1, enabled: true })
				errors.push.apply(errors, r.errs)
				return assign({ structure: s.structure, blocked: s.blocked }, r.settings)
			})
			var live = members.filter(function (m) { return m.enabled && m.frequency > 0 })
			var out
			if (live.length) {
				var scaled = scalePlacement(p, density)
				if (scaled.capped) capped.push(id)
				p = scaled.placement
				out = { placement: outputPlacement(p), structures: live.map(function (m) { return { structure: m.structure, weight: m.weight } }) }
			} else {
				out = { placement: outputPlacement(assign({}, p, { frequency: 0 })), structures: [] }
			}
			var original = {
				placement: outputPlacement(entry.original),
				structures: entry.structures.map(function (s) { return { structure: s.structure, weight: s.weight } }),
			}
			var changed = JSON.stringify(out) !== JSON.stringify(original)
			if (changed) files[resourcePath(id)] = toJson(out)
			live.forEach(function (m) {
				if (m.frequency < 1) (frequencies[id] = frequencies[id] || {})[m.structure] = m.frequency
			})
			setPlans.push({ id: id, entry: entry, placement: p, members: members, live: live, moved: moved, changed: changed,
				emptied: !live.length, handEdited: Object.keys(sa.placement || {}).length > 0 })
		})

		// exclusion zones that point at a set which can no longer generate
		var dead = {}
		setPlans.forEach(function (sp) { if (sp.emptied) dead[sp.id] = sp.moved.length ? 'its structures moved to groups' : 'every member is disabled' })
		groupPlans.forEach(function (gp) { if (!gp.live.length) dead[gp.id] = 'the group has no live member' })
		function checkExclusion(where, p) {
			var ez = p.exclusion_zone
			if (ez && typeof ez === 'object' && dead[ez.other_set]) warnings.push(where + ': exclusion_zone points at ' + ez.other_set + ', which never generates (' + dead[ez.other_set] + '), so it excludes nothing. Point it at the group that took them.')
		}
		groupPlans.forEach(function (gp) { checkExclusion('groups.' + gp.name, gp.placement) })
		setPlans.forEach(function (sp) { if (!sp.emptied) checkExclusion('standalone.' + sp.id, sp.placement) })

		if (errors.length) return { errors: errors, title: 'structure_groups.json / structure_tags.json have errors' }
		if (test && test.applied.length) warnings.push('TEST OVERRIDES ACTIVE (CEConfig [worldgen.debug] testOverrides, ' + TEST_PATH + '): '
			+ test.applied.join('; ') + '. Turn the switch off for play.')
		if (density !== 1) warnings.push('DEBUG structure density x' + density + ' (CEConfig [worldgen.debug] structureDensity): every set\'s rate is scaled; '
			+ (capped.length ? capped.length + ' set(s) hit their densest grid and stay below x' + density + ': ' + capped.join(', ') : 'all exact')
			+ '. Set it back to 1 for play.')

		var autoCount = 0
		groupPlans.forEach(function (gp) { gp.members.forEach(function (m) { if (m.auto) autoCount++ }) })
		if (autoCount) warnings.push(autoCount + ' group member(s) have no entry in structure_groups.json and use a rough default (marked "auto" in the report): run python3 tools/gen_structure_groups.py')

		var freqCount = 0
		Object.keys(frequencies).forEach(function (s) { freqCount += Object.keys(frequencies[s]).length })
		var metaCount = Object.keys(metaFiles).length
		return {
			files: files,
			metaFiles: metaFiles,
			frequencies: frequencies,
			biomeFrequencies: biomeFrequencies,
			replacements: replacements,
			priority: priority,
			biomeRules: biomeRules,
			biomeFits: biomeFits,
			report: buildReport(groupPlans, setPlans, biomeRules, biomeFits, tagDefs, tagMembers, allIds, warnings, freqCount, metaPlans, owner, priority, test),
			warnings: warnings,
			summary: (settings.groups ? '' : 'GROUPS OFF, ') + (test && test.applied.length ? 'TEST OVERRIDES (' + test.applied.length + '), ' : '') + (density !== 1 ? 'DEBUG density x' + density + ', ' : '')
				+ groupPlans.length + ' groups, ' + Object.keys(files).length + ' structure_sets written ('
				+ setPlans.filter(function (sp) { return sp.emptied && sp.moved.length }).length + ' emptied into groups), '
				+ Object.keys(sets).length + ' original sets; ' + freqCount + ' structure frequencies below 1; '
				+ Object.keys(biomeRules).length + ' biome rule(s); ' + Object.keys(biomeFits.fits).length + ' biome fit(s) for '
				+ Object.keys(biomeFits.structures).length + ' structure(s); ' + metaCount + ' meta structure(s)'
				+ (metaPlans.length > metaCount ? ' (' + (metaPlans.length - metaCount) + ' left out)' : '')
				+ '; ' + Object.keys(replacements).length + ' replacement(s)'
				+ '; ' + Object.keys(priority.ranks).length + ' ranked structure(s), ' + Object.keys(priority.companions).length + ' companion rule(s)'
				+ (Object.keys(replOff).length ? ' (' + Object.keys(replOff).length + ' left out)' : ''),
		}
	}

	// ── report ───────────────────────────────────────────────────────────────

	function reportHeader(lines) {
		lines.push('# Structure tuning report')
		lines.push('')
		lines.push('Written ' + new Date().toISOString().slice(0, 19).replace('T', ' ') + ' UTC at world load by '
			+ '`kubejs/server_scripts/worldgen/structure_overrides.js`. Do not edit, it is rewritten every load. '
			+ 'Edit `structure_groups.json` (rates) or the `group/*` tags in `structure_tags.json` (membership), then rejoin the world. '
			+ 'Expected rates per biome: `structure_group_estimates.md` (`python3 tools/gen_structure_groups.py --check`).')
		lines.push('')
	}

	function buildErrorReport(title, errors) {
		var lines = []
		reportHeader(lines)
		lines.push('## ' + title)
		lines.push('')
		lines.push('Nothing was applied: every structure uses its mod/vanilla default until these are fixed.')
		lines.push('')
		errors.forEach(function (e) { lines.push('- ' + e) })
		lines.push('')
		return lines.join('\n')
	}

	function placementText(p) {
		var keys = ['spacing', 'separation', 'frequency', 'count', 'distance', 'spread', 'spread_type', 'frequency_reduction_method', 'salt']
		var parts = keys.filter(function (k) { return k in p }).map(function (k) { return k + ' ' + p[k] })
		if (p.exclusion_zone) parts.push('exclusion_zone ' + p.exclusion_zone.other_set + ' r' + p.exclusion_zone.chunk_count)
		return parts.join(', ')
	}

	function buildReport(groupPlans, setPlans, biomeRules, biomeFits, tagDefs, tagMembers, allIds, warnings, freqCount, metaPlans, owner, priority, test) {
		var lines = []
		var out = function (s) { lines.push(s) }
		reportHeader(lines)
		var metaShare = {}   // meta id -> share, for the built metas with an anchor
		metaPlans.forEach(function (mp) { if (!mp.off && mp.def && 'anchor' in mp.def) metaShare[mp.id] = mp.def.share })

		if (warnings.length) {
			out('## Warnings')
			out('')
			warnings.forEach(function (w) { out('- ' + w) })
			out('')
		}

		if (test) {
			out('## Test overrides')
			out('')
			out('CEConfig `[worldgen.debug] testOverrides` is on: `' + TEST_PATH + '` loosened these soft rules for testing. '
				+ 'Rates in this report include them. Turn the switch off for play.')
			out('')
			if (!test.applied.length) out('None applied' + (test.refused ? ': the file was refused (see Warnings).' : '.'))
			test.applied.forEach(function (a) { out('- ' + a) })
			out('')
		}

		out('## Biome rules')
		out('')
		out('Only the allowed structures may start in a rule\'s biomes; every other structure loses those biomes from its own list '
			+ '(patcherMod StructureBiomeRules, CEConfig [worldgen.systems] structureBiomeRules). The game log line '
			+ '`[structure-biome-rules]` lists the structures each rule kept out.')
		out('')
		if (!Object.keys(biomeRules).length) out('None.')
		Object.keys(biomeRules).forEach(function (name) {
			var r = biomeRules[name]
			out('### `' + name + '`')
			out('')
			if (r.note) out(r.note)
			if (r.note) out('')
			out('Biomes: ' + r.biomes.map(code).join(', ') + '. Allowed (`' + r.tag + '`, ' + r.allow.length + '): ' + r.allow.map(code).join(', '))
			if (r.surface.length) out('Surface check (`' + r.surfaceTag + '`): ' + r.surface.map(code).join(', ')
				+ ' may not start where the surface above them is in these biomes or under water.')
			out('')
		})

		out('## Biome fits')
		out('')
		out('A structure in a fit\'s structure tag spawns in that fit\'s biomes plus its parents\' instead of its own list '
			+ '(`biome_fits.json`, patcherMod StructureBiomeFits, CEConfig [worldgen.systems] structureBiomeFits). Each fit is also the real '
			+ 'biome tag and structure tag `creativeengineering:<fit>`. The game log line `[structure-biome-fits]` gives each structure\'s '
			+ 'old and new biome count.')
		out('')
		if (!Object.keys(biomeFits.fits).length) out('None.')
		else {
			out('| Fit | Spawns in | Biomes listed | Structures |')
			out('|---|---|---|---|')
			Object.keys(biomeFits.fits).forEach(function (name) {
				var f = biomeFits.fits[name]
				out('| `' + name + '` | ' + f.chain.map(code).join(' + ') + ' | ' + f.biomes.length + ' | '
					+ (f.structures.length ? f.structures.map(code).join(', ') : 'none') + ' |')
			})
		}
		out('')

		out('## Placement priority')
		out('')
		out('A structure whose pieces would overlap a higher-ranked structure\'s never starts (patcherMod PlacementPriority, CEConfig '
			+ '[worldgen.systems] placementPriority). Same rank: no check (a group\'s grid keeps its own members apart).')
		out('')
		out('| Group | Rank |')
		out('|---|---:|')
		Object.keys(priority.groups).sort(function (a, b) { return priority.groups[b] - priority.groups[a] }).forEach(function (g) { out('| `' + g + '` | ' + priority.groups[g] + ' |') })
		out('')
		Object.keys(priority.overrides).forEach(function (k) { out('- `' + k + '`: rank ' + priority.overrides[k]) })
		if (priority.shapeGroups.length) out('- shaped (jigsaw members grow around higher-ranked structures instead of being dropped): '
			+ priority.shapeGroups.map(function (g) { return '`' + g + '`' }).join(', ') + ' (' + priority.shaped.length + ' structures)')
		Object.keys(priority.companions).forEach(function (m) {
			var cp = priority.companions[m]
			out('- companions of `' + m + '`: ' + cp.count[0] + ' to ' + cp.count[1] + ' of ' + cp.structures.length + ' structures, '
				+ cp.distance[0] + ' to ' + cp.distance[1] + ' chunks out, rank ' + cp.rank)
		})
		out('')
		if (Object.keys(priority.radius).length) {
			out('### Peaceful radius')
			out('')
			out('No hostile structure starts with a piece within this many chunks (square, XZ) of a peaceful structure\'s pieces, '
				+ 'whatever the ranks; peaceful structures ignore hostile ones in their own rank check.')
			out('')
			out('| Peaceful | Radius (chunks) |')
			out('|---|---:|')
			Object.keys(priority.peaceOverrides).forEach(function (k) { out('| `' + k + '` | ' + priority.peaceOverrides[k] + ' |') })
			Object.keys(priority.peaceGroups).sort(function (a, b) { return priority.peaceGroups[b] - priority.peaceGroups[a] })
				.forEach(function (g) { out('| `' + g + '` | ' + priority.peaceGroups[g] + ' |') })
			out('')
			out('- hostile: ' + priority.hostileKeys.map(function (k) { return '`' + k + '`' }).join(', ') + ' (' + priority.hostile.length + ' structures)')
			out('')
		}

		out('## Groups')
		out('')
		out('Members of a group share one grid: at most one structure per grid slot, and no two members start closer '
			+ 'than separation + 1 chunks. "Alone: 1 per N" = one per N x N chunks where it is the only member that fits the biome '
			+ '(sharing a biome splits the slots by weight). ' + freqCount + ' frequencies below 1 are rolled by patcherMod.')
		out('')
		groupPlans.forEach(function (gp) {
			var p = gp.placement
			out('### `' + gp.name + '` -> `' + gp.id + '`')
			out('')
			if (gp.note) out(gp.note)
			out('')
			out('Grid: ' + placementText(p) + '. Members never start closer than ' + (p.separation + 1) + ' chunks ('
				+ ((p.separation + 1) * 16) + ' blocks). ' + gp.live.length + ' of ' + gp.members.length + ' members live.'
				+ (gp.live.length ? '' : ' **No live member: no set written.**'))
			out('')
			if (gp.biomeFrequency) {
				out('Biome frequency: every member\'s frequency x ' + Object.keys(gp.biomeFrequency).map(function (k) {
					return (k === 'default' ? 'anywhere else' : code(k)) + ' ' + gp.biomeFrequency[k]
				}).join(', ') + ' (the biome at its generation point; the first that matches).')
				out('')
			}
			out('| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |')
			out('|---|---|---|---|---|---|')
			gp.members.forEach(function (m) {
				var notes = []
				if (m.auto) notes.push('**auto** (not in structure_groups.json yet)')
				if (!m.enabled) notes.push('disabled')
				if (m.blocked) notes.push('**blocked:** ' + m.blocked)
				if (m.metaOff) notes.push('**meta structure not built:** ' + m.metaOff)
				else if (metaShare[m.structure] !== undefined) notes.push('meta structure: takes ' + Math.round(metaShare[m.structure] * 100) + '% of the slots its anchors win here; this weight is never used')
				if (m.replOff) notes.push('**replacement left out:** ' + m.replOff)
				else if (m.replaces && m.enabled) notes.push('replaces ' + code(m.replaces) + ': takes ' + Math.round(m.share * 100) + '% of the slots it wins and keeps; this weight is never used')
				out('| ' + code(m.structure) + ' | ' + m.weight + ' | ' + m.frequency + ' | '
					+ (m.enabled ? aloneSpacing(p, m.frequency) : '-') + ' | ' + code(m.from) + ' | ' + notes.join('; ') + ' |')
			})
			out('')
		})

		out('## Meta structures')
		out('')
		out('Our structures built from other structures and features (`meta_structures.json`, patcherMod MetaStructure). '
			+ 'They generate only through their group. `#creativeengineering:meta/*` = the `meta/*` tags below.')
		out('')
		if (!metaPlans.length) out('None defined.')
		metaPlans.forEach(function (mp) {
			out('### ' + code(mp.id))
			out('')
			if (mp.def && mp.def.note) out(mp.def.note)
			if (mp.def && mp.def.note) out('')
			out('Status: ' + (mp.off ? '**not built** (' + mp.off + ')' : 'built') + '. Group: '
				+ (owner[mp.id] ? '`' + owner[mp.id] + '`' : '**none, so it never generates**') + '.')
			out('')
			if (mp.problems.length) mp.problems.forEach(function (e) { out('- error: ' + e) })
			function members(entries) {
				return (entryList(entries) || []).map(function (e) {
					var t = e.indexOf('#creativeengineering:') === 0 ? e.substring('#creativeengineering:'.length) : null
					return t && tagMembers[t] ? code(e) + ' (' + Object.keys(tagMembers[t]).sort().map(code).join(', ') + ')' : code(e)
				}).join(', ')
			}
			if (mp.def && 'anchor' in mp.def) out('- anchor: ' + members(mp.def.anchor) + '; takes ' + Math.round((mp.def.share || 0) * 100)
				+ '% of the slots these win in its group (share ' + mp.def.share + ')')
			;((mp.def && mp.def.parts) || []).forEach(function (part, n) {
				var what = 'structures' in part ? members(part.structures) : 'features ' + members(part.features)
				var bits = ['count ' + toJson('count' in part ? part.count : 1), 'distance ' + toJson(part.distance || [0, 64])]
				;['chance', 'spacing', 'margin', 'tries', 'water_depth', 'radius', 'overlap', 'cluster'].forEach(function (k) { if (k in part) bits.push(k + ' ' + part[k]) })
				out('- part ' + (n + 1) + ': ' + what + '; ' + bits.join(', '))
			})
			out('')
		})

		out('## Standalone sets written')
		out('')
		var written = setPlans.filter(function (sp) { return sp.changed && !(sp.emptied && sp.moved.length) })
		if (!written.length) out('None. Every standalone set matches its mod/vanilla default.')
		written.forEach(function (sp) {
			out('### `' + sp.id + '`')
			out('')
			out('Source: ' + sp.entry.source + '. Placement: ' + placementText(sp.placement) + (sp.handEdited ? ' (edited)' : '') + '.'
				+ (sp.emptied ? ' **Every member disabled: written with no structures.**' : ''))
			if (sp.moved.length) out('Moved to groups: ' + sp.moved.map(function (s) { return code(s.structure) + ' (' + s.group + ')' }).join(', ') + '.')
			out('')
			sp.members.forEach(function (m) {
				var bits = ['weight ' + m.weight, 'frequency ' + m.frequency]
				if (!m.enabled) bits.push('disabled')
				if (m.enabled) bits.push('alone: 1 per ' + aloneSpacing(sp.placement, m.frequency))
				if (m.blocked) bits.push('**blocked:** ' + m.blocked)
				out('- ' + code(m.structure) + ': ' + bits.join(', '))
			})
			out('')
		})

		out('## Sets emptied into groups')
		out('')
		out('Written with no structures and frequency 0: they can neither generate nor show up in /locate or map searches.')
		out('')
		setPlans.filter(function (sp) { return sp.emptied && sp.moved.length }).forEach(function (sp) {
			out('- ' + code(sp.id) + ': ' + sp.moved.map(function (s) { return code(s.structure) + ' -> ' + s.group }).join(', '))
		})
		out('')

		out('## Coverage')
		out('')
		function inFamily(prefixes, id) {
			return Object.keys(tagMembers).some(function (t) {
				return prefixes.some(function (p) { return t.indexOf(p) === 0 }) && tagMembers[t][id]
			})
		}
		var noType = allIds.filter(function (id) { return !inFamily(['type/', 'dungeon/'], id) })
		var noMod = allIds.filter(function (id) { return !inFamily(['mod/'], id) })
		var standaloneIds = allIds.filter(function (id) { return !inFamily(['group/'], id) })
		out('- Structures with no `type/*` or `dungeon/*` tag: ' + (noType.length ? noType.map(code).join(', ') : 'none'))
		out('- Structures with no `mod/*` tag: ' + (noMod.length ? noMod.map(code).join(', ') : 'none'))
		var blockedIds = []
		setPlans.forEach(function (sp) { sp.entry.structures.forEach(function (s) { if (s.blocked) blockedIds.push(code(s.structure)) }) })
		out('- Structures another mod stops in code (tuning them has no effect, see "blocked" in structure_config.json): ' + (blockedIds.length ? blockedIds.join(', ') : 'none'))
		out('- Structures in no group (' + standaloneIds.length + ', standalone): ' + standaloneIds.map(code).join(', '))
		out('')

		out('## Tag members')
		out('')
		Object.keys(tagDefs).forEach(function (t) {
			var ids = Object.keys(tagMembers[t]).sort()
			out('- **`' + t + '`** ' + ids.length + ': ' + ids.map(code).join(', '))
		})
		out('')
		return lines.join('\n')
	}

	function writeReport(text) {
		try {
			WorldgenFiles.writeText(REPORT_PATH, text + '\n')
		} catch (err) {
			console.warn(TAG + 'could not write ' + REPORT_PATH + ': ' + err)
		}
	}

	ServerEvents.highPriorityData(function (event) {
		var result
		try {
			result = plan()
		} catch (err) {
			result = { errors: ['structure tuning failed: ' + err], title: 'Structure tuning failed' }
		}
		if (result.errors) {
			result.errors.forEach(function (e) { console.error(TAG + e) })
			console.error(TAG + 'nothing applied, every structure uses its mod/vanilla default')
			StructureFrequencies.stage('{}')
			StructureFrequencies.stageBiomeFactors('{}')
			PlacementPriority.stage('{}')
			MetaSlots.stageReplacements('{}')
			StructureBiomeRules.stage('{}')
			StructureBiomeFits.stage('{}')
			writeReport(buildErrorReport(result.title || 'Errors', result.errors))
			return
		}
		Object.keys(result.files).forEach(function (path) { event.add(path, result.files[path]) })
		Object.keys(result.metaFiles).forEach(function (path) { event.add(path, result.metaFiles[path]) })
		StructureFrequencies.stage(toJson(result.frequencies))
		StructureFrequencies.stageBiomeFactors(toJson(result.biomeFrequencies))
		MetaSlots.stageReplacements(toJson(result.replacements))
		PlacementPriority.stage(toJson({ ranks: result.priority.ranks, shaped: result.priority.shaped, companions: result.priority.companions,
			peace: { radius: result.priority.radius, hostile: result.priority.hostile } }))
		var rules = {}
		Object.keys(result.biomeRules).forEach(function (name) {
			rules[name] = { biomes: result.biomeRules[name].biomes, allow: result.biomeRules[name].allow }
			if (result.biomeRules[name].surface.length) rules[name].surface_check = result.biomeRules[name].surface
		})
		StructureBiomeRules.stage(toJson(rules))
		StructureBiomeFits.stage(toJson(fitStage(result.biomeFits)))
		result.warnings.forEach(function (w) { console.warn(TAG + w) })
		console.info(TAG + result.summary)
		writeReport(result.report)
	})

	// The meta/* tags of structure_tags.json as real structure tags creativeengineering:meta/<name>: the anchors
	// and parts meta_structures.json names (patcherMod MetaStructure reads them when it generates). The type/* tags
	// too (creativeengineering:type/<name>): earth_fill.js prunes #type/village, and [earth_fill] overrides may name
	// them. Also every biome fit's structure tag as creativeengineering:<fit> (for /locate and other datapacks; the fit
	// itself is staged).
	ServerEvents.tags('worldgen/structure', function (e) {
		var config = readJson(CONFIG_PATH)
		var tagsDoc = readJson(TAGS_PATH)
		if (!config || !tagsDoc) return console.error(TAG + 'meta tags: could not read ' + CONFIG_PATH + ' / ' + TAGS_PATH)
		var metaDoc = readJson(META_PATH)
		var resolved = resolveTags(tagsDoc.tags, structureIds(config, (metaDoc && metaDoc.structures) || {}))
		resolved.errors.forEach(function (err) { console.error(TAG + 'meta tags: structure_tags.json: ' + err) })
		Object.keys(tagsDoc.tags).filter(function (t) { return t.indexOf(META_TAG_PREFIX) === 0 || t.indexOf(TYPE_TAG_PREFIX) === 0 }).forEach(function (t) {
			var ids = Object.keys(resolved.members[t] || {}).sort()
			if (ids.length) global.tags.add(e, 'creativeengineering:' + t, ids)
			else console.warn(TAG + 'meta tags: ' + t + ' has no members')
		})
		var fits = readFits()
		Object.keys(fits).filter(function (t) { return tagsDoc.tags[t] }).forEach(function (t) {
			var ids = Object.keys(resolved.members[t] || {}).sort()
			if (ids.length) global.tags.add(e, 'creativeengineering:' + t, ids)
		})
		// Way signs point at the nearest #supplementaries:way_sign_destinations: every settlement the group holds, so
		// meta/village_towers (stored under the meta's id, not the village's) and Terralith's fortified villages count too.
		var settlements = Object.keys(resolved.members['group/' + WAY_SIGN_GROUP] || {}).sort()
		if (settlements.length) global.tags.add(e, WAY_SIGN_DESTINATIONS, settlements)
		else console.warn(TAG + 'way sign destinations: group/' + WAY_SIGN_GROUP + ' has no members')
	})

	// Every biome fit as the real biome tag creativeengineering:<fit>; StructureBiomeFits resolves the fit through it.
	// Also biome_fits.json "biome_tags" (biome tags that are not fits, e.g. harsh for biome_frequency) as
	// creativeengineering:<name>; they may name fit tags (#creativeengineering:land/desert).
	// Unknown biome ids are reported by StructureBiomeFits at world load (this handler runs as a pre-pass, see _biome_rules.js).
	ServerEvents.tags('worldgen/biome', function (e) {
		var fits = readFits()
		Object.keys(fits).forEach(function (name) {
			var biomes = fits[name] && fits[name].biomes
			if (FIT_NAME.test(name) && Array.isArray(biomes) && biomes.length) global.tags.add(e, 'creativeengineering:' + name, biomes)
		})
		var doc = readJson(FITS_PATH)
		var extra = (doc && doc.biome_tags) || {}
		Object.keys(extra).forEach(function (name) {
			var biomes = extra[name] && extra[name].biomes
			if (FIT_NAME.test(name) && !fits[name] && Array.isArray(biomes) && biomes.length) global.tags.add(e, 'creativeengineering:' + name, biomes)
			else console.warn(TAG + 'biome_fits.json biome_tags.' + name + ': needs a tag path name (not a fit) and a list of biomes')
		})
	})
})()
