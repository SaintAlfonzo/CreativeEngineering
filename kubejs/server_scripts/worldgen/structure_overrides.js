// Structure spawn frequency / removal, applied at world load.
//
// Edit kubejs/generation_scripts/worldgen/structure_config.json ("current" placement values,
// per-structure "enabled") or structure_tags.json (category multipliers), then rejoin the world.
// Worldgen registries are read when a world loads, so /reload doesn't apply these to a running
// world (it does refresh the report), and only chunks generated afterwards change. Refresh the
// baseline after mod updates with `python3 tools/gen_structure_config.py` (keeps your edits).
//
// ServerEvents.highPriorityData runs inside KubeJS's world-load resource wrap, before the game
// parses worldgen: every structure_set that ends up different from its mod/vanilla default is
// added to KubeJS's top-priority virtual datapack (above kubejs/data, Terralith, Nullscape).
//
// Per structure_set, in order:
//   1. base = "current" (hand edits) over "original" (every raw placement field is kept:
//      exclusion zones, spread types, MES/YUNG-specific fields).
//   2. Each structure's multiplier = product of every structure_tags.json tag containing it.
//   3. Members with enabled:false or multiplier 0 are dropped. If none are left, the whole set
//      gets "frequency": 0 (schema-safe for any placement type).
//   4. Remaining members are grouped by multiplier. One group keeps the set's id; any others
//      move to new sets (creativeengineering:split/<ns>/<path>_<n>) with the same placement and
//      a different salt, so each group can have its own grid density.
//   5. Each group's placement is scaled by its multiplier (see scalePlacement).
//
// Any config error (unknown tag/structure, bad multiplier, invalid placement) applies NOTHING, so
// the game falls back to every mod's own defaults, and the errors land in the log + the report.
// structure_tag_report.md (same folder as the configs) is rewritten on every load: tag members,
// multiplier chains, what was applied, splits and coverage gaps.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var DIR = 'kubejs/generation_scripts/worldgen/'
	var CONFIG_PATH = DIR + 'structure_config.json'
	var TAGS_PATH = DIR + 'structure_tags.json'
	var REPORT_PATH = DIR + 'structure_tag_report.md'
	var SPLIT_PREFIX = 'creativeengineering:split/'
	var TAG = '[structure-tuning] '

	// Codec limits (RandomSpreadStructurePlacement / ConcentricRingsStructurePlacement, 1.19.2).
	var MAX_SPACING = 4096
	var MAX_COUNT = 4095

	// KubeJS's class filter blocks java.io, so the markdown report goes through patcherMod.
	var WorldgenFiles = Java.loadClass('com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles')

	// ── small helpers (Rhino: no Object.assign / Set / spread / ** here) ──────

	function assign(target) {
		for (var a = 1; a < arguments.length; a++) {
			var src = arguments[a]
			if (!src) continue
			Object.keys(src).forEach(function (k) { target[k] = src[k] })
		}
		return target
	}

	function clone(v) {
		return JSON.parse(JSON.stringify(v))
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

	function round6(x) {
		return Math.round(x * 1e6) / 1e6
	}

	function fmtMult(m) {
		return 'x' + Number(m.toFixed(4))
	}

	function sortedKeys(obj) {
		return Object.keys(obj).sort()
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
			var m = tagDefs[name].multiplier
			if (typeof m !== 'number' || !isFinite(m) || m < 0) {
				errors.push(name + ': multiplier must be a number >= 0 (got ' + (typeof m === 'number' ? String(m) : JSON.stringify(m)) + ')')
			}
			resolve(name, name)
		})
		return { members: members, errors: errors, warnings: warnings }
	}

	// ── placement math ───────────────────────────────────────────────────────

	// Scale a placement so its structure count per area becomes m times the base's.
	//   Grid placements (anything with "spacing": vanilla random_spread and the YUNG/MES subclasses
	//   of it): one attempt per spacing x spacing cell, so density ~ frequency / spacing^2.
	//   spacing' = floor(spacing / sqrt(m)) is never denser than asked; frequency then takes the
	//   remaining factor exactly (it can only drop, so it stays <= 1 unless spacing hit 1).
	//   Rings (strongholds): count scales.
	//   Anything else: frequency only (can't go above 1).
	function scalePlacement(base, m) {
		var p = clone(base)
		var notes = []
		if (m === 1) return { p: p, achieved: 1, notes: notes }

		var f0 = ('frequency' in p) ? p.frequency : 1
		if (f0 === 0) {
			notes.push('base frequency is 0 (set is disabled), multiplier ignored')
			return { p: p, achieved: 1, notes: notes }
		}

		var achieved
		if (typeof p.spacing === 'number') {
			var s0 = p.spacing
			var sep0 = p.separation || 0
			var s = Math.max(1, Math.min(MAX_SPACING, Math.floor(s0 / Math.sqrt(m) + 1e-9)))
			var sep = Math.max(0, Math.min(s - 1, Math.round(sep0 * s / s0)))
			var f = round6(Math.min(1, f0 * m * Math.pow(s / s0, 2)))
			p.spacing = s
			p.separation = sep
			p.frequency = f
			achieved = Math.pow(s0 / s, 2) * f / f0
		} else if (typeof p.count === 'number') {
			var c = Math.max(1, Math.min(MAX_COUNT, Math.round(p.count * m)))
			achieved = c / p.count
			p.count = c
		} else {
			p.frequency = round6(Math.min(1, f0 * m))
			achieved = p.frequency / f0
		}

		// legacy_type_1 (vanilla outposts) rolls nextInt((int)(1/frequency)) == 0, which only hits
		// 1/n exactly; "default" rolls nextFloat() < frequency.
		if (p.frequency !== f0 && p.frequency_reduction_method === 'legacy_type_1') {
			p.frequency_reduction_method = 'default'
			notes.push('frequency_reduction_method legacy_type_1 -> default (legacy only rounds to 1/n)')
		}
		if (Math.abs(achieved - m) > m * 0.01) {
			notes.push('limit reached: asked ' + fmtMult(m) + ', got ' + fmtMult(achieved))
		}
		return { p: p, achieved: achieved, notes: notes }
	}

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

	function validatePlacement(id, p) {
		var errs = []
		if (typeof p.spacing === 'number') {
			if (!(p.spacing > (p.separation || 0))) errs.push(id + ': spacing ' + p.spacing + ' must be > separation ' + p.separation)
			if (p.spacing > MAX_SPACING) errs.push(id + ': spacing ' + p.spacing + ' > ' + MAX_SPACING)
		}
		if ('frequency' in p && !(p.frequency >= 0 && p.frequency <= 1)) errs.push(id + ': frequency ' + p.frequency + ' outside 0..1')
		if (typeof p.count === 'number' && !(p.count >= 1 && p.count <= MAX_COUNT)) errs.push(id + ': count ' + p.count + ' outside 1..' + MAX_COUNT)
		if (typeof p.salt === 'number' && p.salt < 0) errs.push(id + ': salt ' + p.salt + ' must be >= 0')
		return errs
	}

	// ── planning ─────────────────────────────────────────────────────────────

	function structureList(members) {
		return members.map(function (s) { return { structure: s.structure, weight: s.weight } })
	}

	function planSet(id, entry, multOf) {
		var base = assign({}, entry.original, entry.current)
		var members = entry.structures.map(function (s) {
			var mult = multOf(s.structure)
			return { structure: s.structure, weight: s.weight, enabled: s.enabled, m: mult.m, chain: mult.chain, blocked: s.blocked }
		})
		var live = members.filter(function (s) { return s.enabled && s.m > 0 })
		var outputs = []

		if (!live.length) {
			var p0 = assign({}, base, { frequency: 0 })
			outputs.push({
				id: id, primary: true, m: 0, achieved: 0, notes: ['every member disabled or at multiplier 0 -> frequency 0'],
				placement: p0,
				json: { placement: outputPlacement(p0), structures: structureList(entry.structures) },
				members: members,
			})
		} else {
			var groups = []
			live.forEach(function (s) {
				var g = null
				for (var i = 0; i < groups.length; i++) if (groups[i].m === s.m) { g = groups[i]; break }
				if (!g) groups.push(g = { m: s.m, members: [], weight: 0 })
				g.members.push(s)
				g.weight += s.weight
			})
			// Heaviest group keeps the original id (stable: first-listed wins ties).
			var ordered = groups.map(function (g, i) { return { g: g, i: i } })
				.sort(function (a, b) { return b.g.weight - a.g.weight || a.i - b.i })
				.map(function (x) { return x.g })
			ordered.forEach(function (g, k) {
				var scaled = scalePlacement(base, g.m)
				var p = scaled.p
				var outId = id
				if (k > 0) {
					outId = SPLIT_PREFIX + id.replace(':', '/') + '_' + k
					p.salt = ((base.salt || 0) + k * 1000003) % 2147483647
				}
				outputs.push({
					id: outId, primary: k === 0, m: g.m, achieved: scaled.achieved, notes: scaled.notes,
					placement: p,
					json: { placement: outputPlacement(p), structures: structureList(g.members) },
					members: k === 0
						? members.filter(function (s) { return !(s.enabled && s.m > 0) || g.members.indexOf(s) >= 0 })
						: g.members,
				})
			})
		}

		var originalJson = {
			placement: outputPlacement(entry.original),
			structures: structureList(entry.structures),
		}
		var primary = outputs[0]
		primary.unchanged = JSON.stringify(primary.json) === JSON.stringify(originalJson)
		return { id: id, entry: entry, base: base, members: members, outputs: outputs, split: outputs.length > 1 }
	}

	// ── report ───────────────────────────────────────────────────────────────

	function placementSummary(from, to) {
		var keys = ['spacing', 'separation', 'frequency', 'count', 'frequency_reduction_method']
		return keys.filter(function (k) { return (k in from) || (k in to) }).map(function (k) {
			var a = (k in from) ? from[k] : (k === 'frequency' ? 1 : '-')
			var b = (k in to) ? to[k] : (k === 'frequency' ? 1 : '-')
			return a === b ? k + ' ' + a : k + ' ' + a + ' -> **' + b + '**'
		}).join(', ')
	}

	function chainText(s) {
		if (!s.enabled) return 'disabled (enabled: false)'
		if (!s.chain.length) return 'x1'
		var parts = s.chain.map(function (c) { return c[0] + ' ' + fmtMult(c[1]) })
		return parts.join(' · ') + (s.chain.length > 1 ? ' = ' + fmtMult(s.m) : '')
	}

	function code(id) {
		return '`' + id + '`'
	}

	function reportHeader(lines) {
		lines.push('# Structure tag report')
		lines.push('')
		lines.push('Written ' + new Date().toISOString().slice(0, 19).replace('T', ' ') + ' UTC at world load by '
			+ '`kubejs/server_scripts/worldgen/structure_overrides.js`. Do not edit, it is rewritten every load. '
			+ 'Edit `structure_tags.json` / `structure_config.json` instead, then rejoin the world.')
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

	function buildReport(tagDefs, tagMembers, plans, allIds, splitWarnings, warnings) {
		var lines = []
		var out = function (s) { lines.push(s) }
		reportHeader(lines)

		out('## Active multipliers')
		out('')
		var active = Object.keys(tagDefs).filter(function (t) { return tagDefs[t].multiplier !== 1 })
		if (!active.length) {
			out('None. Every tag is at 1, so tags change nothing yet.')
		} else {
			out('| Tag | Multiplier | Structures |')
			out('|---|---|---|')
			active.forEach(function (t) { out('| `' + t + '` | ' + fmtMult(tagDefs[t].multiplier) + ' | ' + Object.keys(tagMembers[t]).length + ' |') })
		}
		out('')

		out('## Structure sets written')
		out('')
		var changed = plans.filter(function (pl) { return pl.split || !pl.outputs[0].unchanged })
		if (!changed.length) out('None. Every set matches its mod/vanilla default.')
		changed.forEach(function (pl) {
			out('### `' + pl.id + '`' + (pl.split ? ' (split)' : ''))
			out('')
			out('Source: ' + pl.entry.source + '.')
			var handEdited = JSON.stringify(outputPlacement(pl.base)) !== JSON.stringify(outputPlacement(pl.entry.original))
			if (handEdited) out('Hand edits in "current": ' + placementSummary(pl.entry.original, pl.base) + '.')
			out('')
			pl.outputs.forEach(function (o) {
				if (o.primary && o.unchanged) return
				out('- ' + (o.primary ? 'Written as' : 'Split off to') + ' `' + o.id + '`: target ' + fmtMult(o.m) + ', got '
					+ fmtMult(o.achieved) + '. ' + placementSummary(pl.entry.original, o.placement))
				o.notes.forEach(function (n) { out('  - note: ' + n) })
				o.members.forEach(function (s) { out('  - `' + s.structure + '`: ' + chainText(s) + (s.blocked ? ' (**blocked:** ' + s.blocked + ')' : '')) })
			})
			out('')
		})

		if (splitWarnings.length) {
			out('## Splits to check')
			out('')
			out('A split set\'s groups no longer share one grid. Exact when their biomes don\'t overlap; where they do, the split-off group gets somewhat more common than its multiplier says.')
			out('')
			splitWarnings.forEach(function (w) { out('- ' + w) })
			out('')
		}

		if (warnings.length) {
			out('## Warnings')
			out('')
			warnings.forEach(function (w) { out('- ' + w) })
			out('')
		}

		out('## Coverage')
		out('')
		function inFamily(prefixes, id) {
			return Object.keys(tagMembers).some(function (t) {
				return prefixes.some(function (p) { return t.indexOf(p) === 0 }) && tagMembers[t][id]
			})
		}
		var noType = allIds.filter(function (id) { return !inFamily(['type/', 'dungeon/'], id) })
		var noMod = allIds.filter(function (id) { return !inFamily(['mod/'], id) })
		out('- Structures with no `type/*` or `dungeon/*` tag: ' + (noType.length ? noType.map(code).join(', ') : 'none'))
		out('- Structures with no `mod/*` tag: ' + (noMod.length ? noMod.map(code).join(', ') : 'none'))
		// "blocked" notes come from tools/gen_structure_config.py (another mod stops these in code)
		var blocked = []
		plans.forEach(function (pl) { pl.entry.structures.forEach(function (s) { if (s.blocked) blocked.push(code(s.structure)) }) })
		out('- Structures another mod stops in code (tuning them has no effect, see "blocked" in structure_config.json): ' + (blocked.length ? blocked.join(', ') : 'none'))
		out('')

		out('## Tag members')
		out('')
		Object.keys(tagDefs).forEach(function (t) {
			var ids = sortedKeys(tagMembers[t])
			out('- **`' + t + '`** ' + fmtMult(tagDefs[t].multiplier) + ', ' + ids.length + ': ' + ids.map(code).join(', '))
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

	// ── main ─────────────────────────────────────────────────────────────────

	// -> { files: {resourcePath: jsonText}, summary } or { errors: [...] }
	function plan() {
		var config = readJson(CONFIG_PATH)
		if (!config) return { errors: ['could not read ' + CONFIG_PATH + ' (run python3 tools/gen_structure_config.py)'] }
		var sets = config.structure_sets
		var tagsDoc = readJson(TAGS_PATH)
		var tagDefs = tagsDoc ? tagsDoc.tags : {}

		var allIds = []
		Object.keys(sets).forEach(function (id) { sets[id].structures.forEach(function (s) { allIds.push(s.structure) }) })

		var resolved = resolveTags(tagDefs, allIds)
		var tagMembers = resolved.members
		if (resolved.errors.length) return { errors: resolved.errors, title: 'structure_tags.json has errors' }

		var activeTags = Object.keys(tagDefs).filter(function (t) { return tagDefs[t].multiplier !== 1 })
		var multCache = {}
		function multOf(sid) {
			if (!multCache[sid]) {
				var chain = activeTags.filter(function (t) { return tagMembers[t][sid] }).map(function (t) { return [t, tagDefs[t].multiplier] })
				multCache[sid] = { m: chain.reduce(function (acc, c) { return acc * c[1] }, 1), chain: chain }
			}
			return multCache[sid]
		}

		var plans = Object.keys(sets).map(function (id) { return planSet(id, sets[id], multOf) })

		var files = {}
		var invalid = []
		var splitWarnings = []
		plans.forEach(function (pl) {
			pl.outputs.forEach(function (o) {
				if (o.primary && o.unchanged && !pl.split) return
				invalid.push.apply(invalid, validatePlacement(o.id, o.placement))
				files[resourcePath(o.id)] = toJson(o.json)
			})
			if (pl.split) {
				var groups = pl.outputs.map(function (o) {
					return o.id + ' (' + fmtMult(o.m) + ': ' + o.json.structures.map(function (s) { return s.structure }).join(', ') + ')'
				})
				var w = '`' + pl.id + '` split into ' + groups.join(' | ')
				var referrers = Object.keys(sets).filter(function (other) {
					return ['exclusion_zone', 'enhanced_exclusion_zone'].some(function (k) {
						return (sets[other].original[k] || {}).other_set === pl.id
					})
				})
				if (referrers.length) w += '. Exclusion zones in ' + referrers.join(', ') + ' point at `' + pl.id + '` and won\'t see the split-off groups'
				splitWarnings.push(w)
			}
		})
		if (invalid.length) return { errors: invalid, title: 'Invalid placements' }

		var splits = plans.filter(function (p) { return p.split }).length
		return {
			files: files,
			report: buildReport(tagDefs, tagMembers, plans, allIds, splitWarnings, resolved.warnings),
			warnings: resolved.warnings,
			summary: Object.keys(files).length + ' structure_sets overridden (' + splits + ' split sets), ' + plans.length + ' sets total; '
				+ activeTags.length + ' active tag multipliers: '
				+ (activeTags.map(function (t) { return t + ' ' + fmtMult(tagDefs[t].multiplier) }).join(', ') || 'none'),
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
			writeReport(buildErrorReport(result.title || 'Errors', result.errors))
			return
		}
		Object.keys(result.files).forEach(function (path) { event.add(path, result.files[path]) })
		result.warnings.forEach(function (w) { console.warn(TAG + w) })
		console.info(TAG + result.summary)
		writeReport(result.report)
	})
})()
