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
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var DIR = 'kubejs/generation_scripts/worldgen/'
	var CONFIG_PATH = DIR + 'structure_config.json'
	var TAGS_PATH = DIR + 'structure_tags.json'
	var GROUPS_PATH = DIR + 'structure_groups.json'
	var REPORT_PATH = DIR + 'structure_tag_report.md'
	var GROUP_SET_PREFIX = 'creativeengineering:group/'
	var TAG = '[structure-tuning] '

	// Codec limits (RandomSpreadStructurePlacement / ConcentricRingsStructurePlacement / ExclusionZone, 1.19.2).
	var MAX_SPACING = 4096
	var MAX_COUNT = 4095
	var MAX_EXCLUSION = 16
	var DEFAULT_WEIGHT = 100

	var GROUP_PLACEMENT_KEYS = ['spacing', 'separation', 'salt', 'spread_type', 'frequency',
		'frequency_reduction_method', 'locate_offset', 'exclusion_zone']
	var MEMBER_KEYS = ['weight', 'frequency', 'enabled', 'note']
	var REDUCTION_METHODS = ['default', 'legacy_type_1', 'legacy_type_2', 'legacy_type_3']

	// KubeJS's class filter blocks java.io, so the markdown report goes through patcherMod.
	var WorldgenFiles = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles')

	// CEConfig [worldgen] switches; the defaults when the jar predates WorldgenConfig.
	function worldgenSettings() {
		var s = { groups: true, density: 1 }
		try {
			var C = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig')
			s.groups = C.structureGroupsEnabled() == true
			s.density = Number(C.structureDensity())
		} catch (err) {
			console.warn(TAG + 'no patcherMod WorldgenConfig (older jar?): structure groups on, density 1')
		}
		if (!(s.density > 0) || !isFinite(s.density)) s.density = 1
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
		Object.keys(config.meta_structures || {}).forEach(function (id) { if (!setOf[id]) allIds.push(id) })

		var resolved = resolveTags(tagDefs, allIds)
		var warnings = resolved.warnings.slice()
		if (resolved.errors.length) {
			// Groups off: the tags only feed the report, so their errors don't stop the mod defaults.
			if (settings.groups) return { errors: resolved.errors, title: 'structure_tags.json has errors' }
			resolved.errors.forEach(function (e) { warnings.push('structure_tags.json: ' + e) })
		}
		var tagMembers = resolved.members
		var errors = []
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

		var knownSets = {}
		Object.keys(sets).forEach(function (id) { knownSets[id] = true })
		groupNames.forEach(function (g) { knownSets[GROUP_SET_PREFIX + g] = true })

		var files = {}
		var frequencies = {}     // set id -> { structure id: frequency < 1 }
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
				return assign({ structure: id, from: setOf[id] || '(meta structure)', blocked: blocked[id] }, r.settings)
			})
			var live = members.filter(function (m) { return m.enabled && m.frequency > 0 })
			var scaled = scalePlacement(p, density)   // after the members: their auto defaults use the real grid
			if (scaled.capped) capped.push(setId)
			var plan = { name: g, id: setId, note: entry.note, placement: scaled.placement, members: members, live: live }
			groupPlans.push(plan)
			if (!live.length) return
			files[resourcePath(setId)] = toJson({
				placement: outputPlacement(scaled.placement),
				structures: live.map(function (m) { return { structure: m.structure, weight: m.weight } }),
			})
			live.forEach(function (m) {
				if (m.frequency < 1) (frequencies[setId] = frequencies[setId] || {})[m.structure] = m.frequency
			})
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
		if (density !== 1) warnings.push('DEBUG structure density x' + density + ' (CEConfig [worldgen.debug] structureDensity): every set\'s rate is scaled; '
			+ (capped.length ? capped.length + ' set(s) hit their densest grid and stay below x' + density + ': ' + capped.join(', ') : 'all exact')
			+ '. Set it back to 1 for play.')

		var autoCount = 0
		groupPlans.forEach(function (gp) { gp.members.forEach(function (m) { if (m.auto) autoCount++ }) })
		if (autoCount) warnings.push(autoCount + ' group member(s) have no entry in structure_groups.json and use a rough default (marked "auto" in the report): run python3 tools/gen_structure_groups.py')

		var freqCount = 0
		Object.keys(frequencies).forEach(function (s) { freqCount += Object.keys(frequencies[s]).length })
		return {
			files: files,
			frequencies: frequencies,
			report: buildReport(groupPlans, setPlans, tagDefs, tagMembers, allIds, warnings, freqCount),
			warnings: warnings,
			summary: (settings.groups ? '' : 'GROUPS OFF, ') + (density !== 1 ? 'DEBUG density x' + density + ', ' : '')
				+ groupPlans.length + ' groups, ' + Object.keys(files).length + ' structure_sets written ('
				+ setPlans.filter(function (sp) { return sp.emptied && sp.moved.length }).length + ' emptied into groups), '
				+ Object.keys(sets).length + ' original sets; ' + freqCount + ' structure frequencies below 1',
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

	function buildReport(groupPlans, setPlans, tagDefs, tagMembers, allIds, warnings, freqCount) {
		var lines = []
		var out = function (s) { lines.push(s) }
		reportHeader(lines)

		if (warnings.length) {
			out('## Warnings')
			out('')
			warnings.forEach(function (w) { out('- ' + w) })
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
			out('| Structure | Weight | Frequency | Alone: 1 per | Was in | Note |')
			out('|---|---|---|---|---|---|')
			gp.members.forEach(function (m) {
				var notes = []
				if (m.auto) notes.push('**auto** (not in structure_groups.json yet)')
				if (!m.enabled) notes.push('disabled')
				if (m.blocked) notes.push('**blocked:** ' + m.blocked)
				out('| ' + code(m.structure) + ' | ' + m.weight + ' | ' + m.frequency + ' | '
					+ (m.enabled ? aloneSpacing(p, m.frequency) : '-') + ' | ' + code(m.from) + ' | ' + notes.join('; ') + ' |')
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

	// patcherMod's frequency roller; null when the jar is older than this script
	function frequencyStage() {
		try {
			return Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.StructureFrequencies')
		} catch (err) {
			return null
		}
	}

	ServerEvents.highPriorityData(function (event) {
		var result
		try {
			result = plan()
		} catch (err) {
			result = { errors: ['structure tuning failed: ' + err], title: 'Structure tuning failed' }
		}
		var stage = frequencyStage()
		if (!result.errors && !stage) {
			result = { errors: ['patcherMod has no worldgen.StructureFrequencies: mods/ holds an older creativeengineering jar. '
				+ 'Groups without their per-structure frequencies would spawn every member at full rate, so nothing is applied. '
				+ 'Copy the new jar from development/patcherMod/build/libs into mods/.'], title: 'patcherMod jar too old' }
		}
		if (result.errors) {
			result.errors.forEach(function (e) { console.error(TAG + e) })
			console.error(TAG + 'nothing applied, every structure uses its mod/vanilla default')
			if (stage) stage.stage('{}')
			writeReport(buildErrorReport(result.title || 'Errors', result.errors))
			return
		}
		Object.keys(result.files).forEach(function (path) { event.add(path, result.files[path]) })
		stage.stage(toJson(result.frequencies))
		result.warnings.forEach(function (w) { console.warn(TAG + w) })
		console.info(TAG + result.summary)
		writeReport(result.report)
	})
})()
