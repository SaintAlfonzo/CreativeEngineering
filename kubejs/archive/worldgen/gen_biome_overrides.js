// ARCHIVED 2026-09-26: replaced by kubejs/server_scripts/worldgen/ (applied at world load, no manual run).
// Kept for reference only; running this would write kubejs/data files that double-apply.
// Run manually with `node gen_biome_overrides.js` (from this folder) after editing
// biome_config.json. For every multi_noise dimension with an edited biome it writes a full
// dimension override to kubejs/data/<ns>/dimension/<path>.json (the whole biome list has to be
// replaced as one document -- there is no additive form for a biome_source). Dimensions with
// nothing edited get their override removed again, falling back to the mod/vanilla layout.
//
// How a biome's frequency is changed: the game picks, per quart-block, the parameter entry with
// the lowest   fitness = sum over 6 climate axes of (distance outside the entry's range)^2
//                        + offset^2
// (Climate.ParameterPoint#fitness, bytecode-checked against 1.19.2). So adding a penalty P to
// offset^2 of every entry of one biome makes it lose exactly the samples where it was winning by
// less than P, and a negative P lets it win samples where it was losing by less than |P|. One
// scalar per tuned biome; the solver picks each one against the /dumpclimate sample set so the
// biome's share of its layer lands on multiplier x original share. Negative penalties are
// realised by adding the same constant to every entry in the dimension (argmin-invariant), so
// every written offset is a real non-negative number.
//
// multiplier 0 drops the biome's entries outright; replace_with relabels them. Neither needs
// samples. Every other multiplier does -- run /dumpclimate in any world with this modpack first.

const fs = require('fs');
const path = require('path');

const HERE = __dirname;
const CONFIG_PATH = path.join(HERE, 'biome_config.json');
const KUBEJS = path.join(HERE, '..', '..');
const DATA_ROOT = path.join(KUBEJS, 'data');
const SAMPLE_DIR = path.join(KUBEJS, 'exported', 'worldgen', 'climate');

const AXES = ['temperature', 'humidity', 'continentalness', 'erosion', 'depth', 'weirdness'];
const Q = 10000;              // Climate.quantizeCoord scale
const PMAX = 0.6 * 0.6 * Q * Q; // largest penalty either way, in quantized^2 units (an offset of 0.6)
const WINDOW = 3 * PMAX;      // biomes further than this from a sample's best can never win it
const MAX_CANDIDATES = 24;
const ROUNDS = 20;

// Climate.quantizeCoord: (long)(coord * 10000.0F), float maths, truncating cast.
const quant = v => Math.trunc(Math.fround(Math.fround(v) * Math.fround(Q)));

function fileFor(dimId, dir, ext) {
	const [ns, p] = dimId.split(':');
	return path.join(dir, `${ns}__${p.replace(/\//g, '__')}${ext}`);
}

function overridePath(dimId) {
	const [ns, p] = dimId.split(':');
	return path.join(DATA_ROOT, ns, 'dimension', `${p}.json`);
}

// entries -> flat Float64Arrays: mins/maxs [n*6], off2 [n] (offset squared, quantized)
function quantizeEntries(entries) {
	const n = entries.length;
	const mins = new Float64Array(n * 6), maxs = new Float64Array(n * 6), off2 = new Float64Array(n);
	entries.forEach((e, i) => {
		AXES.forEach((a, k) => {
			const v = e.parameters[a];
			const [lo, hi] = Array.isArray(v) ? v : [v, v];
			mins[i * 6 + k] = quant(lo);
			maxs[i * 6 + k] = quant(hi);
		});
		const o = quant(e.parameters.offset || 0);
		off2[i] = o * o;
	});
	return { n, mins, maxs, off2 };
}

// Base fitness (without the per-biome penalty) of every entry for one sample.
function fitnessAll(q, s, out) {
	for (let i = 0; i < q.n; i++) {
		let f = q.off2[i];
		for (let k = 0; k < 6; k++) {
			const t = s[k + 1];
			const d = t > q.maxs[i * 6 + k] ? t - q.maxs[i * 6 + k] : (q.mins[i * 6 + k] > t ? q.mins[i * 6 + k] - t : 0);
			f += d * d;
		}
		out[i] = f;
	}
}

function argminBiome(fit, labels, n) {
	let best = Infinity, who = -1;
	for (let i = 0; i < n; i++) if (fit[i] < best) { best = fit[i]; who = i; }
	return labels[who];
}

function pct(count, total) {
	return total ? Math.round(count / total * 1e5) / 1e3 : null;
}

// margins sorted descending: b wins a sample iff its penalty < that sample's margin. Pick the
// penalty that lets b win as close to k samples as possible, always strictly between two distinct
// margins -- overlapping entries give whole plateaus of identical margins (inside both boxes only
// the offsets differ), and a penalty ON a plateau would be a tie the game breaks arbitrarily.
function penaltyFor(margins, k) {
	const n = margins.length;
	if (!n) return 0;
	if (k <= 0) return margins[0] + 2;
	if (k >= n) return margins[n - 1] - 2;
	let lo = k, hi = k; // plateau around the cut: margins[lo..hi] all equal margins[k]
	while (lo > 0 && margins[lo - 1] === margins[k]) lo--;
	while (hi < n - 1 && margins[hi + 1] === margins[k]) hi++;
	if (lo === k) return (margins[k - 1] + margins[k]) / 2; // clean cut, no plateau
	// win none of the plateau (count lo) or all of it (count hi+1)
	if (k - lo <= hi + 1 - k) return (lo > 0 ? (margins[lo - 1] + margins[k]) / 2 : margins[k] + 2);
	return hi < n - 1 ? (margins[k] + margins[hi + 1]) / 2 : margins[k] - 2;
}

function processDimension(dimId, dim, knownBiomes, errors) {
	const baseline = JSON.parse(fs.readFileSync(path.join(HERE, dim.baseline), 'utf8'));
	const origEntries = baseline.generator.biome_source.biomes;
	const cfg = dim.biomes;

	// --- validate edits ---
	const errorsBefore = errors.length;
	const tuned = [], removed = [], replaced = [];
	Object.keys(cfg).forEach(b => {
		const c = cfg[b];
		delete c.note;
		if (c.replace_with) {
			if (!knownBiomes.has(c.replace_with)) {
				errors.push(`${dimId}: ${b}.replace_with "${c.replace_with}" is not a biome any scanned dimension uses (typo? an unknown id makes world creation fail)`);
			} else if (c.replace_with !== b) replaced.push(b);
			return;
		}
		const m = Number(c.multiplier);
		if (!(m >= 0) || !isFinite(m)) { errors.push(`${dimId}: ${b}.multiplier must be a number >= 0`); return; }
		if (m === 0) removed.push(b);
		else if (m !== 1) tuned.push(b);
	});
	if (removed.length === Object.keys(cfg).length) errors.push(`${dimId}: every biome is removed -- the dimension needs at least one`);
	if (errors.length > errorsBefore) return null; // never write a half-valid layout
	const edited = tuned.length + removed.length + replaced.length > 0;

	// --- edited entry list (structure only; offsets come later) ---
	const newEntries = [];
	origEntries.forEach(e => {
		const c = cfg[e.biome] || {};
		if (!c.replace_with && Number(c.multiplier) === 0) return;
		newEntries.push({ biome: c.replace_with || e.biome, parameters: Object.assign({}, e.parameters) });
	});

	// --- samples ---
	const samplePath = fileFor(dimId, SAMPLE_DIR, '.json');
	const haveSamples = fs.existsSync(samplePath);
	if (!haveSamples) {
		dim.samples = null;
		dim.model_agreement_pct = null;
		if (tuned.length) {
			errors.push(`${dimId}: ${tuned.join(', ')} need a climate sample -- run /dumpclimate in-game, then re-run this`);
			return null;
		}
		return { edited, newEntries, baseline, summary: `${removed.length} removed, ${replaced.length} replaced (no samples, no stats)` };
	}
	const dump = JSON.parse(fs.readFileSync(samplePath, 'utf8'));
	const points = dump.points;
	const layers = dump.layers;

	// biome index space covers original + any replace_with targets
	const biomeIds = Array.from(new Set(origEntries.map(e => e.biome).concat(newEntries.map(e => e.biome))));
	const bIdx = new Map(biomeIds.map((b, i) => [b, i]));
	const B = biomeIds.length;

	const qOrig = quantizeEntries(origEntries);
	const origLabels = Int32Array.from(origEntries, e => bIdx.get(e.biome));
	const qNew = quantizeEntries(newEntries);
	const newLabels = Int32Array.from(newEntries, e => bIdx.get(e.biome));

	// --- original model: winner per sample, per-layer counts, agreement with the live world ---
	const layerTotals = new Array(layers.length).fill(0);
	const origCounts = layers.map(() => new Float64Array(B));
	const fitO = new Float64Array(qOrig.n);
	let agree = 0;
	const origWinner = new Int32Array(points.length);
	points.forEach((s, j) => {
		fitnessAll(qOrig, s, fitO);
		const w = argminBiome(fitO, origLabels, qOrig.n);
		origWinner[j] = w;
		layerTotals[s[0]]++;
		origCounts[s[0]][w]++;
		if (biomeIds[w] === dump.biomes[s[7]]) agree++;
	});

	// each biome is measured on the layer where it holds the biggest share
	const layerOf = new Int32Array(B);
	for (let b = 0; b < B; b++) {
		let best = -1;
		layers.forEach((_, L) => {
			const share = layerTotals[L] ? origCounts[L][b] / layerTotals[L] : 0;
			if (share > best) { best = share; layerOf[b] = L; }
		});
	}

	// --- candidate lists on the edited layout: per sample, best base fitness per biome within WINDOW ---
	const fitN = new Float64Array(qNew.n);
	const perBiome = new Float64Array(B);
	const cand = points.map(s => {
		fitnessAll(qNew, s, fitN);
		perBiome.fill(Infinity);
		for (let i = 0; i < qNew.n; i++) if (fitN[i] < perBiome[newLabels[i]]) perBiome[newLabels[i]] = fitN[i];
		let best = Infinity;
		for (let b = 0; b < B; b++) if (perBiome[b] < best) best = perBiome[b];
		const list = [];
		for (let b = 0; b < B; b++) if (perBiome[b] <= best + WINDOW) list.push([b, perBiome[b]]);
		list.sort((x, y) => x[1] - y[1]);
		return list.slice(0, MAX_CANDIDATES);
	});

	// --- solve penalties ---
	const pen = new Float64Array(B);
	const tunedIdx = tuned.map(b => bIdx.get(b));
	const target = new Map(tunedIdx.map(b => [b, Math.round(Number(cfg[biomeIds[b]].multiplier) * origCounts[layerOf[b]][b])]));
	for (let round = 0; round < ROUNDS && tunedIdx.length; round++) {
		let moved = 0;
		tunedIdx.forEach(b => {
			const L = layerOf[b];
			const margins = [];
			points.forEach((s, j) => {
				if (s[0] !== L) return;
				let fb = null, other = Infinity;
				for (const [c, f] of cand[j]) {
					if (c === b) fb = f;
					else if (f + pen[c] < other) other = f + pen[c];
				}
				if (fb !== null) margins.push(other - fb);
			});
			margins.sort((x, y) => y - x);
			const clampedP = Math.max(-PMAX, Math.min(PMAX, penaltyFor(margins, target.get(b))));
			moved = Math.max(moved, Math.abs(clampedP - pen[b]));
			pen[b] = clampedP;
		});
		if (moved < 1) break;
	}

	// --- penalties -> offsets, as exact quantized integers ---
	// The game re-quantizes every offset as (long)(float * 10000), so write oq/10000 plus half a
	// unit: that always truncates back to exactly oq, whatever float rounding does.
	let minTotal = Infinity;
	for (let i = 0; i < qNew.n; i++) minTotal = Math.min(minTotal, qNew.off2[i] + pen[newLabels[i]]);
	const C = Math.max(0, -minTotal);
	newEntries.forEach((e, i) => {
		const add = pen[newLabels[i]] + C;
		if (add === 0) return;
		const oq = Math.round(Math.sqrt(qNew.off2[i] + add));
		e.parameters.offset = Math.round((oq + 0.5) * 1000) / 1e7;
		if (quant(e.parameters.offset) !== oq) throw new Error(`offset round-trip failed for ${e.biome}: ${oq}`);
	});

	// --- result shares: brute-force the entries exactly as they'll be written ---
	const qOut = quantizeEntries(newEntries);
	const fitR = new Float64Array(qOut.n);
	const resCounts = layers.map(() => new Float64Array(B));
	let agreeNew = 0;
	points.forEach(s => {
		fitnessAll(qOut, s, fitR);
		const who = argminBiome(fitR, newLabels, qOut.n);
		resCounts[s[0]][who]++;
		if (biomeIds[who] === dump.biomes[s[7]]) agreeNew++;
	});

	// --- write stats back into the config ---
	Object.keys(cfg).forEach(b => {
		const i = bIdx.get(b), L = layerOf[i];
		cfg[b].layer = layers[L];
		cfg[b].share_pct = pct(origCounts[L][i], layerTotals[L]);
		cfg[b].result_pct = pct(resCounts[L][i], layerTotals[L]);
		if (origCounts[L][i] === 0 && tuned.includes(b)) {
			cfg[b].note = 'never hit in the sample set -- a multiplier has nothing to scale; dump more columns (/dumpclimate 20000)';
		} else if (target.has(i) && Math.abs(resCounts[L][i] - target.get(i)) > Math.max(2, 0.05 * target.get(i))) {
			cfg[b].note = `aimed for ${pct(target.get(i), layerTotals[L])}%, got ${cfg[b].result_pct}% -- `
				+ 'either out of the penalty range (offset 0.6 either way) or its climate space is all-or-nothing (overlapping boxes)';
		}
	});
	dim.samples = {
		file: path.relative(HERE, samplePath),
		points: points.length,
		seed: dump.seed,
		generated: dump.generated,
		layer_points: Object.fromEntries(layers.map((l, L) => [l, layerTotals[L]])),
	};
	dim.model_agreement_pct = pct(agree, points.length);

	const liveMatch = agreeNew > agree
		? `live world matches the TUNED layout (${pct(agreeNew, points.length)}%)`
		: `live world matches the baseline (${pct(agree, points.length)}%)`;
	return {
		edited, newEntries, baseline,
		summary: `${tuned.length} tuned, ${removed.length} removed, ${replaced.length} replaced; ${liveMatch}`,
	};
}

function run() {
	const config = JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));
	const dims = config.dimensions;
	const knownBiomes = new Set();
	Object.values(dims).forEach(d => Object.keys(d.biomes).forEach(b => knownBiomes.add(b)));

	const errors = [];
	Object.keys(dims).forEach(dimId => {
		const r = processDimension(dimId, dims[dimId], knownBiomes, errors);
		if (!r) { console.log(`  skipped ${dimId} (see errors below; any existing override left as it was)`); return; }
		const out = overridePath(dimId);
		if (r.edited) {
			const doc = JSON.parse(JSON.stringify(r.baseline));
			doc.generator.biome_source.biomes = r.newEntries;
			fs.mkdirSync(path.dirname(out), { recursive: true });
			fs.writeFileSync(out, JSON.stringify(doc, null, 1) + '\n');
			console.log(`  wrote   ${path.relative(KUBEJS, out)}  (${r.summary})`);
		} else {
			if (fs.existsSync(out)) {
				fs.unlinkSync(out);
				console.log(`  removed ${path.relative(KUBEJS, out)}  (nothing edited any more)`);
			} else {
				console.log(`  ${dimId}: untouched (${r.summary})`);
			}
		}
	});

	fs.writeFileSync(CONFIG_PATH, JSON.stringify(config, null, 2) + '\n');
	Object.keys(dims).forEach(dimId => Object.entries(dims[dimId].biomes).forEach(([b, c]) => {
		if (c.note) console.log(`  note    ${dimId} ${b}: ${c.note}`);
	}));
	if (errors.length) {
		errors.forEach(e => console.error('  ERROR   ' + e));
		process.exitCode = 1;
	}
	console.log('Only worlds created after this take the new layout -- existing saves keep theirs (level.dat).');
}

run();
