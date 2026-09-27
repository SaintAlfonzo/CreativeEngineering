// ARCHIVED 2026-09-26: replaced by kubejs/server_scripts/worldgen/ (applied at world load, no manual run).
// Kept for reference only; running this would write kubejs/data files that double-apply.
// Run manually with `node gen_structure_overrides.js` (from this folder) after editing
// structure_config.json or structure_tags.json. Writes one datapack override per structure_set
// that ends up different from its mod/vanilla default, to
// kubejs/data/<namespace>/worldgen/structure_set/<path>.json, and removes overrides that are no
// longer needed. Worldgen registries load when a world loads (not on /reload), and only chunks
// generated afterwards are affected.
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
// structure_tag_report.md is rewritten on every run: tag members, multiplier chains, what was
// written, splits and coverage gaps.

const fs = require('fs');
const path = require('path');

const HERE = __dirname;
const CONFIG_PATH = path.join(HERE, 'structure_config.json');
const TAGS_PATH = path.join(HERE, 'structure_tags.json');
const REPORT_PATH = path.join(HERE, 'structure_tag_report.md');
const MANIFEST_PATH = path.join(HERE, '.generated_structure_sets.json');
const DATA_ROOT = path.join(HERE, '..', '..', 'data'); // kubejs/data
const INSTANCE_ROOT = path.join(HERE, '..', '..', '..');
const SPLIT_PREFIX = 'creativeengineering:split/';

// Codec limits (RandomSpreadStructurePlacement / ConcentricRingsStructurePlacement, 1.19.2).
const MAX_SPACING = 4096;
const MAX_COUNT = 4095;

function setFilePath(id) {
	const [ns, p] = id.split(':');
	return path.join(DATA_ROOT, ns, 'worldgen', 'structure_set', `${p}.json`);
}

function rel(file) {
	return path.relative(INSTANCE_ROOT, file);
}

function round6(x) {
	return Math.round(x * 1e6) / 1e6;
}

function fmtMult(m) {
	return `x${Number(m.toFixed(4))}`;
}

// ── tags ──────────────────────────────────────────────────────────────────────

function globToRegex(glob) {
	const esc = glob.split('*').map(part => part.replace(/[.+?^${}()|[\]\\]/g, '\\$&'));
	return new RegExp('^' + esc.join('.*') + '$');
}

// -> { members: {tag: Set<id>}, errors: [], warnings: [] }
function resolveTags(tagDefs, allIds) {
	const idSet = new Set(allIds);
	const members = {};
	const resolving = new Set();
	const errors = [];
	const warnings = [];

	function resolve(name, from) {
		if (members[name]) return members[name];
		const def = tagDefs[name];
		if (!def) {
			errors.push(`${from}: unknown tag "#${name}"`);
			return new Set();
		}
		if (resolving.has(name)) {
			errors.push(`${from}: tag cycle through "#${name}"`);
			return new Set();
		}
		resolving.add(name);
		const include = new Set();
		const exclude = new Set();
		(def.values || []).forEach(raw => {
			const negate = raw.startsWith('!');
			const v = negate ? raw.slice(1) : raw;
			let ids;
			if (v.startsWith('#')) {
				ids = [...resolve(v.slice(1), name)];
			} else if (v.includes('*')) {
				const re = globToRegex(v);
				ids = allIds.filter(id => re.test(id));
				if (!ids.length) warnings.push(`${name}: "${raw}" matches no structure`);
			} else if (idSet.has(v)) {
				ids = [v];
			} else {
				errors.push(`${name}: unknown structure "${v}"`);
				ids = [];
			}
			ids.forEach(id => (negate ? exclude : include).add(id));
		});
		resolving.delete(name);
		members[name] = new Set([...include].filter(id => !exclude.has(id)));
		return members[name];
	}

	Object.keys(tagDefs).forEach(name => {
		const m = tagDefs[name].multiplier;
		if (typeof m !== 'number' || !isFinite(m) || m < 0) {
			errors.push(`${name}: multiplier must be a number >= 0 (got ${JSON.stringify(m)})`);
		}
		resolve(name, name);
	});
	return { members, errors, warnings };
}

// ── placement math ────────────────────────────────────────────────────────────

// Scale a placement so its structure count per area becomes m times the base's.
//   Grid placements (anything with "spacing": vanilla random_spread and the YUNG/MES subclasses
//   of it): one attempt per spacing x spacing cell, so density ~ frequency / spacing^2.
//   spacing' = floor(spacing / sqrt(m)) is never denser than asked; frequency then takes the
//   remaining factor exactly (it can only drop, so it stays <= 1 unless spacing hit 1).
//   Rings (strongholds): count scales.
//   Anything else: frequency only (can't go above 1).
function scalePlacement(base, m) {
	const p = JSON.parse(JSON.stringify(base));
	const notes = [];
	if (m === 1) return { p, achieved: 1, notes };

	const f0 = ('frequency' in p) ? p.frequency : 1;
	if (f0 === 0) {
		notes.push('base frequency is 0 (set is disabled), multiplier ignored');
		return { p, achieved: 1, notes };
	}

	let achieved;
	if (typeof p.spacing === 'number') {
		const s0 = p.spacing;
		const sep0 = p.separation || 0;
		const s = Math.max(1, Math.min(MAX_SPACING, Math.floor(s0 / Math.sqrt(m) + 1e-9)));
		const sep = Math.max(0, Math.min(s - 1, Math.round(sep0 * s / s0)));
		const f = round6(Math.min(1, f0 * m * (s / s0) ** 2));
		p.spacing = s;
		p.separation = sep;
		p.frequency = f;
		achieved = (s0 / s) ** 2 * f / f0;
	} else if (typeof p.count === 'number') {
		const c = Math.max(1, Math.min(MAX_COUNT, Math.round(p.count * m)));
		achieved = c / p.count;
		p.count = c;
	} else {
		p.frequency = round6(Math.min(1, f0 * m));
		achieved = p.frequency / f0;
	}

	// legacy_type_1 (vanilla outposts) rolls nextInt((int)(1/frequency)) == 0, which only hits
	// 1/n exactly; "default" rolls nextFloat() < frequency.
	if (p.frequency !== f0 && p.frequency_reduction_method === 'legacy_type_1') {
		p.frequency_reduction_method = 'default';
		notes.push('frequency_reduction_method legacy_type_1 -> default (legacy only rounds to 1/n)');
	}
	if (Math.abs(achieved - m) > m * 0.01) {
		notes.push(`limit reached: asked ${fmtMult(m)}, got ${fmtMult(achieved)}`);
	}
	return { p, achieved, notes };
}

// The JSON "placement" object: type first, defaults dropped.
function outputPlacement(p) {
	const out = { type: p.type };
	Object.keys(p).forEach(k => {
		if (k === 'type') return;
		if (k === 'frequency' && p[k] === 1) return;
		if (k === 'locate_offset' && Array.isArray(p[k]) && p[k].every(v => v === 0)) return;
		out[k] = p[k];
	});
	return out;
}

function validatePlacement(id, p) {
	const errs = [];
	if (typeof p.spacing === 'number') {
		if (!(p.spacing > (p.separation || 0))) errs.push(`${id}: spacing ${p.spacing} must be > separation ${p.separation}`);
		if (p.spacing > MAX_SPACING) errs.push(`${id}: spacing ${p.spacing} > ${MAX_SPACING}`);
	}
	if ('frequency' in p && !(p.frequency >= 0 && p.frequency <= 1)) errs.push(`${id}: frequency ${p.frequency} outside 0..1`);
	if (typeof p.count === 'number' && !(p.count >= 1 && p.count <= MAX_COUNT)) errs.push(`${id}: count ${p.count} outside 1..${MAX_COUNT}`);
	if (typeof p.salt === 'number' && p.salt < 0) errs.push(`${id}: salt ${p.salt} must be >= 0`);
	return errs;
}

// ── planning ──────────────────────────────────────────────────────────────────

function planSet(id, entry, multOf) {
	const base = Object.assign({}, entry.original, entry.current);
	const members = entry.structures.map(s => {
		const mult = multOf(s.structure);
		return { structure: s.structure, weight: s.weight, enabled: s.enabled, m: mult.m, chain: mult.chain };
	});
	const live = members.filter(s => s.enabled && s.m > 0);
	const outputs = [];

	if (!live.length) {
		const p = Object.assign({}, base, { frequency: 0 });
		outputs.push({
			id, primary: true, m: 0, achieved: 0, notes: ['every member disabled or at multiplier 0 -> frequency 0'],
			placement: p,
			json: { placement: outputPlacement(p), structures: entry.structures.map(s => ({ structure: s.structure, weight: s.weight })) },
			members,
		});
	} else {
		const groups = [];
		live.forEach(s => {
			let g = groups.find(x => x.m === s.m);
			if (!g) groups.push(g = { m: s.m, members: [], weight: 0 });
			g.members.push(s);
			g.weight += s.weight;
		});
		// Heaviest group keeps the original id (stable: first-listed wins ties).
		const ordered = groups.map((g, i) => ({ g, i })).sort((a, b) => b.g.weight - a.g.weight || a.i - b.i).map(x => x.g);
		ordered.forEach((g, k) => {
			const scaled = scalePlacement(base, g.m);
			const p = scaled.p;
			let outId = id;
			if (k > 0) {
				outId = `${SPLIT_PREFIX}${id.replace(':', '/')}_${k}`;
				p.salt = ((base.salt || 0) + k * 1000003) % 2147483647;
			}
			outputs.push({
				id: outId, primary: k === 0, m: g.m, achieved: scaled.achieved, notes: scaled.notes,
				placement: p,
				json: { placement: outputPlacement(p), structures: g.members.map(s => ({ structure: s.structure, weight: s.weight })) },
				members: k === 0 ? members.filter(s => !(s.enabled && s.m > 0) || g.members.includes(s)) : g.members,
			});
		});
	}

	const originalJson = {
		placement: outputPlacement(entry.original),
		structures: entry.structures.map(s => ({ structure: s.structure, weight: s.weight })),
	};
	const primary = outputs[0];
	primary.unchanged = JSON.stringify(primary.json) === JSON.stringify(originalJson);
	return { id, entry, base, members, outputs, split: outputs.length > 1 };
}

// ── report ────────────────────────────────────────────────────────────────────

function placementSummary(from, to) {
	const keys = ['spacing', 'separation', 'frequency', 'count', 'frequency_reduction_method'];
	return keys.filter(k => k in from || k in to).map(k => {
		const a = (k in from) ? from[k] : (k === 'frequency' ? 1 : '-');
		const b = (k in to) ? to[k] : (k === 'frequency' ? 1 : '-');
		return a === b ? `${k} ${a}` : `${k} ${a} -> **${b}**`;
	}).join(', ');
}

function chainText(s) {
	if (!s.enabled) return 'disabled (enabled: false)';
	if (!s.chain.length) return 'x1';
	const parts = s.chain.map(([tag, m]) => `${tag} ${fmtMult(m)}`);
	return parts.join(' · ') + (s.chain.length > 1 ? ` = ${fmtMult(s.m)}` : '');
}

function buildReport(tagDefs, tagMembers, plans, allIds, splitWarnings, warnings) {
	const lines = [];
	const out = s => lines.push(s);
	out('# Structure tag report');
	out('');
	out(`Generated ${new Date().toISOString().slice(0, 19).replace('T', ' ')} by \`node gen_structure_overrides.js\`. Do not edit, it is rewritten every run. Edit \`structure_tags.json\` / \`structure_config.json\` instead.`);
	out('');

	out('## Active multipliers');
	out('');
	const active = Object.keys(tagDefs).filter(t => tagDefs[t].multiplier !== 1);
	if (!active.length) {
		out('None. Every tag is at 1, so tags change nothing yet.');
	} else {
		out('| Tag | Multiplier | Structures |');
		out('|---|---|---|');
		active.forEach(t => out(`| \`${t}\` | ${fmtMult(tagDefs[t].multiplier)} | ${tagMembers[t].size} |`));
	}
	out('');

	out('## Structure sets written');
	out('');
	const changed = plans.filter(pl => pl.split || !pl.outputs[0].unchanged);
	if (!changed.length) out('None. Every set matches its mod/vanilla default.');
	changed.forEach(pl => {
		out(`### \`${pl.id}\`${pl.split ? ' (split)' : ''}`);
		out('');
		out(`Source: ${pl.entry.source}.`);
		const handEdited = JSON.stringify(outputPlacement(pl.base)) !== JSON.stringify(outputPlacement(pl.entry.original));
		if (handEdited) out(`Hand edits in "current": ${placementSummary(pl.entry.original, pl.base)}.`);
		out('');
		pl.outputs.forEach(o => {
			if (o.primary && o.unchanged) return;
			out(`- ${o.primary ? 'Written as' : 'Split off to'} \`${o.id}\`: target ${fmtMult(o.m)}, got ${fmtMult(o.achieved)}. ${placementSummary(pl.entry.original, o.placement)}`);
			o.notes.forEach(n => out(`  - note: ${n}`));
			o.members.forEach(s => out(`  - \`${s.structure}\`: ${chainText(s)}`));
		});
		out('');
	});

	if (splitWarnings.length) {
		out('## Splits to check');
		out('');
		out('A split set\'s groups no longer share one grid. Exact when their biomes don\'t overlap; where they do, the split-off group gets somewhat more common than its multiplier says.');
		out('');
		splitWarnings.forEach(w => out(`- ${w}`));
		out('');
	}

	if (warnings.length) {
		out('## Warnings');
		out('');
		warnings.forEach(w => out(`- ${w}`));
		out('');
	}

	out('## Coverage');
	out('');
	const inFamily = prefixes => id => Object.keys(tagMembers).some(t => prefixes.some(p => t.startsWith(p)) && tagMembers[t].has(id));
	const noType = allIds.filter(id => !inFamily(['type/', 'dungeon/'])(id));
	const noMod = allIds.filter(id => !inFamily(['mod/'])(id));
	out(`- Structures with no \`type/*\` or \`dungeon/*\` tag: ${noType.length ? noType.map(i => `\`${i}\``).join(', ') : 'none'}`);
	out(`- Structures with no \`mod/*\` tag: ${noMod.length ? noMod.map(i => `\`${i}\``).join(', ') : 'none'}`);
	out('');

	out('## Tag members');
	out('');
	Object.keys(tagDefs).forEach(t => {
		const ids = [...tagMembers[t]].sort();
		out(`- **\`${t}\`** ${fmtMult(tagDefs[t].multiplier)}, ${ids.length}: ${ids.map(i => `\`${i}\``).join(', ')}`);
	});
	out('');
	return lines.join('\n');
}

// ── main ──────────────────────────────────────────────────────────────────────

function run() {
	const config = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));
	const sets = config.structure_sets;
	const tagDefs = fs.existsSync(TAGS_PATH) ? JSON.parse(fs.readFileSync(TAGS_PATH, 'utf8')).tags : {};

	const allIds = [];
	Object.keys(sets).forEach(id => sets[id].structures.forEach(s => allIds.push(s.structure)));

	const { members: tagMembers, errors, warnings } = resolveTags(tagDefs, allIds);
	if (errors.length) {
		console.error('structure_tags.json has errors, nothing written:');
		errors.forEach(e => console.error('  ' + e));
		process.exit(1);
	}

	const activeTags = Object.keys(tagDefs).filter(t => tagDefs[t].multiplier !== 1);
	const multCache = {};
	const multOf = sid => {
		if (!multCache[sid]) {
			const chain = activeTags.filter(t => tagMembers[t].has(sid)).map(t => [t, tagDefs[t].multiplier]);
			multCache[sid] = { m: chain.reduce((acc, [, m]) => acc * m, 1), chain };
		}
		return multCache[sid];
	};

	const plans = Object.keys(sets).map(id => planSet(id, sets[id], multOf));

	// Files to write this run.
	const files = new Map(); // abs path -> json
	const invalid = [];
	const splitWarnings = [];
	plans.forEach(pl => {
		pl.outputs.forEach(o => {
			if (o.primary && o.unchanged && !pl.split) return;
			invalid.push(...validatePlacement(o.id, o.placement));
			files.set(setFilePath(o.id), o.json);
		});
		if (pl.split) {
			const groups = pl.outputs.map(o => `${o.id} (${fmtMult(o.m)}: ${o.json.structures.map(s => s.structure).join(', ')})`);
			let w = `\`${pl.id}\` split into ${groups.join(' | ')}`;
			const referrers = Object.keys(sets).filter(other => ['exclusion_zone', 'enhanced_exclusion_zone']
				.some(k => (sets[other].original[k] || {}).other_set === pl.id));
			if (referrers.length) w += `. Exclusion zones in ${referrers.join(', ')} point at \`${pl.id}\` and won't see the split-off groups`;
			splitWarnings.push(w);
		}
	});
	if (invalid.length) {
		console.error('Refusing to write invalid placements:');
		invalid.forEach(e => console.error('  ' + e));
		process.exit(1);
	}

	// Remove stale files: anything the last run wrote, plus the default override path of every
	// tracked set (covers files written before the manifest existed).
	const previous = fs.existsSync(MANIFEST_PATH) ? JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf8')) : [];
	const candidates = new Set(previous.map(f => path.join(INSTANCE_ROOT, f)));
	Object.keys(sets).forEach(id => candidates.add(setFilePath(id)));
	let removed = 0;
	candidates.forEach(f => {
		if (!files.has(f) && fs.existsSync(f)) {
			fs.unlinkSync(f);
			console.log(`removed: ${rel(f)}`);
			removed++;
			// Prune directories this left empty (split sets nest several levels deep), up to and
			// including the namespace's worldgen/structure_set folder.
			for (let dir = path.dirname(f); path.basename(dir) !== 'worldgen' && fs.readdirSync(dir).length === 0; dir = path.dirname(dir)) {
				fs.rmdirSync(dir);
			}
		}
	});

	files.forEach((json, f) => {
		fs.mkdirSync(path.dirname(f), { recursive: true });
		fs.writeFileSync(f, JSON.stringify(json, null, 2) + '\n');
		console.log(`wrote: ${rel(f)}`);
	});
	fs.writeFileSync(MANIFEST_PATH, JSON.stringify([...files.keys()].map(rel).sort(), null, 2) + '\n');

	fs.writeFileSync(REPORT_PATH, buildReport(tagDefs, tagMembers, plans, allIds, splitWarnings, warnings) + '\n');

	const splits = plans.filter(p => p.split).length;
	console.log(`\n${files.size} written (${splits} split sets), ${removed} removed, ${plans.length} sets total`);
	console.log(`${activeTags.length} active tag multipliers: ${activeTags.map(t => `${t} ${fmtMult(tagDefs[t].multiplier)}`).join(', ') || 'none'}`);
	warnings.forEach(w => console.log('warning: ' + w));
	console.log(`report: ${rel(REPORT_PATH)}`);
}

run();
