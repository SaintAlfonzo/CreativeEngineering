package com.SaintAlfonzo.creativeengineering.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Biome frequency tuning, solved at world load. kubejs/server_scripts/worldgen/biome_overrides.js
 * calls {@link #run()} from ServerEvents.highPriorityData and adds every returned file to KubeJS's
 * top-priority virtual datapack, so editing biome_config.json and creating a world is the whole
 * workflow. Port of the former node script gen_biome_overrides.js (kubejs/archive/worldgen/).
 *
 * For every multi_noise dimension with an edited biome it produces a full dimension override
 * (the whole biome list is one document, there is no additive form for a biome_source).
 *
 * How a biome's frequency is changed: the game picks, per quart-block, the parameter entry with
 * the lowest   fitness = sum over 6 climate axes of (distance outside the entry's range)^2
 *                        + offset^2
 * (Climate.ParameterPoint#fitness, 1.19.2). Adding a penalty P to offset^2 of every entry of one
 * biome makes it lose exactly the samples where it was winning by less than P, and a negative P
 * lets it win samples where it was losing by less than |P|. One scalar per tuned biome; the
 * solver picks each one against the /dumpclimate sample set so the biome's share of its layer
 * lands on multiplier x original share. Negative penalties are realised by adding the same
 * constant to every entry in the dimension (argmin-invariant), so every written offset is a real
 * non-negative number.
 *
 * multiplier 0 drops the biome's entries outright; replace_with relabels them. Neither needs
 * samples. Every other multiplier does. target_pct (overrides the multiplier) aims at an absolute share
 * of the biome's layer instead, so it also works for a biome the samples never hit (a multiplier of
 * nothing is nothing): its entries then win the samples they lose by the smallest margins. A never-hit
 * biome has no layer of its own; target_layer names it (default: the first layer, "surface").
 *
 * Samples: climate/<dim>.json.gz from /dumpclimate; when climate/<dim>.caves.json.gz from
 * /dumpclimatecaves exists, its points replace the main dump's underground layer (deeper, denser cave
 * coverage), so the two can be dumped separately.
 *
 * Stats (layer / share_pct / result_pct / note, per dimension samples + model_agreement_pct) are
 * written back into biome_config.json, only when they changed. Results are cached on the input
 * files' contents, so a /reload with nothing edited costs a hash, not a solve.
 */
public final class BiomeLayoutTuner {

	static final String WORLDGEN_DIR = "kubejs/generation_scripts/worldgen/";
	static final String CONFIG = WORLDGEN_DIR + "biome_config.json";
	// /dumpclimate samples: compact JSON, gzipped (about 7% of the old pretty-printed size), shipped with
	// the pack next to the config they feed. Before 1.0.3 they lived in exported/ as plain JSON; that
	// copy is still read when there is no .gz, so an older dump keeps working.
	static final String SAMPLE_DIR = WORLDGEN_DIR + "climate/";
	static final String LEGACY_SAMPLE_DIR = "kubejs/exported/worldgen/climate/";

	private static final String[] AXES = {"temperature", "humidity", "continentalness", "erosion", "depth", "weirdness"};
	private static final double Q = 10000;                 // Climate.quantizeCoord scale
	private static final double PMAX = 0.6 * 0.6 * Q * Q;  // largest penalty either way (an offset of 0.6)
	private static final double WINDOW = 3 * PMAX;         // biomes further than this from a sample's best can never win it
	// Candidates per sample and solver rounds: CEConfig [worldgen.biome_solver] (read per solve, part of the cache key).
	private static int maxCandidates = 24;
	private static int rounds = 20;

	private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
	private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();

	private static String cacheKey = null;
	private static Result cached = null;
	/** dimension id -> sample file used by the current run, relative to WORLDGEN_DIR (for the "samples" stats). */
	private static final Map<String, String> sampleSource = new HashMap<>();

	private BiomeLayoutTuner() {}

	/** What a run produced. Getters only (no public fields): Rhino mishandles field+method pairs. */
	public static final class Result {
		private final Map<String, String> files = new LinkedHashMap<>();
		private final List<String> messages = new ArrayList<>();
		private final List<String> errors = new ArrayList<>();
		private boolean fromCache = false;

		/** resource path ("minecraft:dimension/overworld.json") -> JSON text. */
		public Map<String, String> getFiles() { return files; }
		public List<String> getMessages() { return messages; }
		public List<String> getErrors() { return errors; }
		public boolean isFromCache() { return fromCache; }
	}

	public static synchronized Result run() {
		Result r = new Result();
		if (!WorldgenConfig.biomeLayoutEnabled()) {
			r.messages.add("off in CEConfig [worldgen.systems] biomeLayout: new worlds get the mod/vanilla biome layout");
			return r;
		}
		maxCandidates = WorldgenConfig.biomeMaxCandidates();
		rounds = WorldgenConfig.biomeRounds();
		try {
			String cfgText = WorldgenFiles.readText(CONFIG);
			if (cfgText == null) {
				r.messages.add("no " + CONFIG + ", nothing to tune");
				return r;
			}
			JsonObject config = JsonParser.parseString(cfgText).getAsJsonObject();
			JsonObject dims = config.getAsJsonObject("dimensions");

			Map<String, String> baselineText = new HashMap<>();
			Map<String, String> sampleText = new HashMap<>();
			for (String dimId : dims.keySet()) {
				String rel = WORLDGEN_DIR + dims.getAsJsonObject(dimId).get("baseline").getAsString();
				String text = WorldgenFiles.readText(rel);
				if (text == null) throw new IllegalStateException(dimId + ": baseline " + rel + " is missing (re-run tools/gen_biome_config.py)");
				baselineText.put(dimId, text);
				sampleText.put(dimId, readSamples(dimId));
			}

			String key = fingerprint(cfgText, dims.keySet(), baselineText, sampleText);
			if (key.equals(cacheKey) && cached != null) {
				cached.fromCache = true;
				return cached;
			}

			Set<String> knownBiomes = new LinkedHashSet<>();
			for (String dimId : dims.keySet()) knownBiomes.addAll(dims.getAsJsonObject(dimId).getAsJsonObject("biomes").keySet());

			for (String dimId : dims.keySet()) {
				JsonObject dim = dims.getAsJsonObject(dimId);
				DimResult dr = processDimension(dimId, dim, knownBiomes, r.errors, baselineText.get(dimId), sampleText.get(dimId));
				if (dr == null) {
					r.messages.add("skipped " + dimId + " (see errors; the mod/vanilla layout is used)");
				} else if (dr.edited) {
					JsonObject doc = dr.baseline.deepCopy();
					doc.getAsJsonObject("generator").getAsJsonObject("biome_source").add("biomes", dr.newEntries);
					r.files.put(resourcePath(dimId), COMPACT.toJson(doc));
					r.messages.add("override " + dimId + " (" + dr.summary + ")");
				} else {
					r.messages.add(dimId + ": untouched (" + dr.summary + ")");
				}
			}

			for (String dimId : dims.keySet()) {
				JsonObject biomes = dims.getAsJsonObject(dimId).getAsJsonObject("biomes");
				for (String b : biomes.keySet()) {
					JsonElement note = biomes.getAsJsonObject(b).get("note");
					if (note != null && !note.isJsonNull()) r.messages.add("note " + dimId + " " + b + ": " + note.getAsString());
				}
			}

			String newCfgText = PRETTY.toJson(config) + "\n";
			if (WorldgenFiles.writeText(CONFIG, newCfgText)) r.messages.add("updated stats in " + CONFIG);

			cacheKey = fingerprint(newCfgText, dims.keySet(), baselineText, sampleText);
			cached = r;
		} catch (Exception e) {
			// Never hand back a half-built layout: no files means the mod/vanilla layout is used.
			r.files.clear();
			r.errors.add("biome tuning failed: " + e);
		}
		return r;
	}

	// ── dimension ─────────────────────────────────────────────────────────────

	private static final class DimResult {
		boolean edited;
		JsonArray newEntries;
		JsonObject baseline;
		String summary;
	}

	private static DimResult processDimension(String dimId, JsonObject dim, Set<String> knownBiomes, List<String> errors,
			String baselineText, String sampleText) {
		JsonObject baseline = JsonParser.parseString(baselineText).getAsJsonObject();
		JsonArray origArr = baseline.getAsJsonObject("generator").getAsJsonObject("biome_source").getAsJsonArray("biomes");
		List<JsonObject> origEntries = new ArrayList<>();
		for (JsonElement el : origArr) origEntries.add(el.getAsJsonObject());
		JsonObject cfg = dim.getAsJsonObject("biomes");

		// --- validate edits ---
		int errorsBefore = errors.size();
		List<String> tuned = new ArrayList<>(), removed = new ArrayList<>(), replaced = new ArrayList<>();
		for (String b : cfg.keySet()) {
			JsonObject c = cfg.getAsJsonObject(b);
			c.remove("note");
			String rw = str(c.get("replace_with"));
			if (rw != null) {
				if (!knownBiomes.contains(rw)) {
					errors.add(dimId + ": " + b + ".replace_with \"" + rw + "\" is not a biome any scanned dimension uses (typo? an unknown id makes world creation fail)");
				} else if (!rw.equals(b)) replaced.add(b);
				continue;
			}
			Double tp = num(c.get("target_pct"));
			if (c.has("target_pct") && !c.get("target_pct").isJsonNull()) {
				if (tp == null || !(tp >= 0) || tp > 100) errors.add(dimId + ": " + b + ".target_pct must be a number from 0 to 100");
				else tuned.add(b);
				continue;
			}
			Double m = num(c.get("multiplier"));
			if (m == null || !(m >= 0) || m.isInfinite()) {
				errors.add(dimId + ": " + b + ".multiplier must be a number >= 0");
				continue;
			}
			if (m == 0) removed.add(b);
			else if (m != 1) tuned.add(b);
		}
		if (removed.size() == cfg.size()) errors.add(dimId + ": every biome is removed -- the dimension needs at least one");
		if (errors.size() > errorsBefore) return null; // never write a half-valid layout
		boolean edited = tuned.size() + removed.size() + replaced.size() > 0;

		// --- edited entry list (structure only; offsets come later) ---
		List<JsonObject> newEntries = new ArrayList<>();
		for (JsonObject e : origEntries) {
			String biome = e.get("biome").getAsString();
			JsonObject c = cfg.has(biome) ? cfg.getAsJsonObject(biome) : null;
			String rw = c == null ? null : str(c.get("replace_with"));
			if (rw == null && c != null && num(c.get("target_pct")) == null) {
				Double m = num(c.get("multiplier"));
				if (m != null && m == 0) continue;
			}
			JsonObject ne = new JsonObject();
			ne.addProperty("biome", rw != null ? rw : biome);
			ne.add("parameters", e.getAsJsonObject("parameters").deepCopy());
			newEntries.add(ne);
		}

		DimResult out = new DimResult();
		out.edited = edited;
		out.baseline = baseline;
		out.newEntries = new JsonArray();
		newEntries.forEach(out.newEntries::add);

		// --- samples ---
		if (sampleText == null) {
			dim.add("samples", JsonNull.INSTANCE);
			dim.add("model_agreement_pct", JsonNull.INSTANCE);
			if (!tuned.isEmpty()) {
				errors.add(dimId + ": " + String.join(", ", tuned) + " need a climate sample -- run /dumpclimate in-game, then rejoin (or /reload before creating a world)");
				return null;
			}
			out.summary = removed.size() + " removed, " + replaced.size() + " replaced (no samples, no stats)";
			return out;
		}
		JsonObject dump = JsonParser.parseString(sampleText).getAsJsonObject();
		JsonArray pointArr = dump.getAsJsonArray("points");
		List<String> layers = strings(dump.getAsJsonArray("layers"));
		List<String> dumpBiomes = strings(dump.getAsJsonArray("biomes"));
		double[][] points = new double[pointArr.size()][];
		for (int j = 0; j < points.length; j++) {
			JsonArray p = pointArr.get(j).getAsJsonArray();
			double[] s = new double[p.size()];
			for (int k = 0; k < s.length; k++) s[k] = p.get(k).getAsDouble();
			points[j] = s;
		}

		// biome index space covers original + any replace_with targets
		LinkedHashSet<String> idSet = new LinkedHashSet<>();
		origEntries.forEach(e -> idSet.add(e.get("biome").getAsString()));
		newEntries.forEach(e -> idSet.add(e.get("biome").getAsString()));
		List<String> biomeIds = new ArrayList<>(idSet);
		Map<String, Integer> bIdx = new HashMap<>();
		for (int i = 0; i < biomeIds.size(); i++) bIdx.put(biomeIds.get(i), i);
		int B = biomeIds.size();

		Quant qOrig = quantize(origEntries);
		int[] origLabels = labels(origEntries, bIdx);
		Quant qNew = quantize(newEntries);
		int[] newLabels = labels(newEntries, bIdx);

		// --- original model: winner per sample, per-layer counts, agreement with the live world ---
		int nL = layers.size();
		double[] layerTotals = new double[nL];
		double[][] origCounts = new double[nL][B];
		double[] fitO = new double[qOrig.n];
		int agree = 0;
		for (double[] s : points) {
			fitnessAll(qOrig, s, fitO);
			int w = argminBiome(fitO, origLabels, qOrig.n);
			int L = (int) s[0];
			layerTotals[L]++;
			origCounts[L][w]++;
			if (biomeIds.get(w).equals(at(dumpBiomes, (int) s[7]))) agree++;
		}

		// Which layer a biome is measured on. With both a surface and an underground layer, a biome with an entry
		// whose depth range holds 0 is a surface biome, else a cave biome (vanilla and Terralith give surface biomes
		// entries at depth 0 and 1, cave biomes only depth > 0). The surface ones also win deep samples through
		// their depth-1 entries, sometimes with a bigger share than on the surface (warm river, the skylands with
		// /dumpclimatecaves), and cave biomes win a few shallow surface samples: "biggest share" would put both on
		// the wrong layer and scale the wrong share. Other dimensions: the layer where it holds the biggest share.
		int surfaceL = layers.indexOf("surface"), underL = layers.indexOf("underground");
		boolean[] depthZero = new boolean[B], known = new boolean[B];
		for (int i = 0; i < qOrig.n; i++) {
			int b = origLabels[i];
			known[b] = true;
			if (qOrig.mins[i * 6 + 4] <= 0 && qOrig.maxs[i * 6 + 4] >= 0) depthZero[b] = true;
		}
		int[] layerOf = new int[B];
		for (int b = 0; b < B; b++) {
			if (surfaceL >= 0 && underL >= 0 && known[b]) {
				layerOf[b] = depthZero[b] ? surfaceL : underL;
				continue;
			}
			double best = -1;
			for (int L = 0; L < nL; L++) {
				double share = layerTotals[L] != 0 ? origCounts[L][b] / layerTotals[L] : 0;
				if (share > best) { best = share; layerOf[b] = L; }
			}
		}
		for (String b : cfg.keySet()) {   // target_layer for a target the samples never hit
			Integer i = bIdx.get(b);
			String tl = str(cfg.getAsJsonObject(b).get("target_layer"));
			if (i != null && tl != null && layers.contains(tl) && origCounts[layerOf[i]][i] == 0) layerOf[i] = layers.indexOf(tl);
		}

		// --- candidate lists on the edited layout: per sample, best base fitness per biome within WINDOW ---
		double[] fitN = new double[qNew.n];
		double[] perBiome = new double[B];
		int[][] candB = new int[points.length][];
		double[][] candF = new double[points.length][];
		for (int j = 0; j < points.length; j++) {
			fitnessAll(qNew, points[j], fitN);
			java.util.Arrays.fill(perBiome, Double.POSITIVE_INFINITY);
			for (int i = 0; i < qNew.n; i++) if (fitN[i] < perBiome[newLabels[i]]) perBiome[newLabels[i]] = fitN[i];
			double best = Double.POSITIVE_INFINITY;
			for (int b = 0; b < B; b++) if (perBiome[b] < best) best = perBiome[b];
			List<Integer> list = new ArrayList<>();
			for (int b = 0; b < B; b++) if (perBiome[b] <= best + WINDOW) list.add(b);
			final double[] pb = perBiome.clone();
			list.sort((x, y) -> Double.compare(pb[x], pb[y])); // stable, like Array.prototype.sort
			int n = Math.min(maxCandidates, list.size());
			candB[j] = new int[n];
			candF[j] = new double[n];
			for (int t = 0; t < n; t++) { candB[j][t] = list.get(t); candF[j][t] = pb[list.get(t)]; }
		}

		// --- solve penalties ---
		double[] pen = new double[B];
		int[] tunedIdx = tuned.stream().mapToInt(bIdx::get).toArray();
		Map<Integer, Long> target = new LinkedHashMap<>();
		for (int b : tunedIdx) {
			JsonObject c = cfg.getAsJsonObject(biomeIds.get(b));
			Double tp = num(c.get("target_pct"));
			target.put(b, tp != null
					? Math.round(tp / 100 * layerTotals[layerOf[b]])
					: Math.round(num(c.get("multiplier")) * origCounts[layerOf[b]][b]));
		}
		for (int round = 0; round < rounds && tunedIdx.length > 0; round++) {
			double moved = 0;
			for (int b : tunedIdx) {
				int L = layerOf[b];
				List<Double> marginList = new ArrayList<>();
				for (int j = 0; j < points.length; j++) {
					if ((int) points[j][0] != L) continue;
					boolean hasB = false;
					double fb = 0, other = Double.POSITIVE_INFINITY;
					for (int t = 0; t < candB[j].length; t++) {
						int c = candB[j][t];
						double f = candF[j][t];
						if (c == b) { fb = f; hasB = true; }
						else if (f + pen[c] < other) other = f + pen[c];
					}
					if (hasB) marginList.add(other - fb);
				}
				double[] margins = new double[marginList.size()];
				for (int t = 0; t < margins.length; t++) margins[t] = marginList.get(t);
				java.util.Arrays.sort(margins);
				reverse(margins); // descending
				double clampedP = Math.max(-PMAX, Math.min(PMAX, penaltyFor(margins, target.get(b))));
				moved = Math.max(moved, Math.abs(clampedP - pen[b]));
				pen[b] = clampedP;
			}
			if (moved < 1) break;
		}

		// --- penalties -> offsets, as exact quantized integers ---
		// The game re-quantizes every offset as (long)(float * 10000), so write oq/10000 plus half a
		// unit: that always truncates back to exactly oq, whatever float rounding does.
		double minTotal = Double.POSITIVE_INFINITY;
		for (int i = 0; i < qNew.n; i++) minTotal = Math.min(minTotal, qNew.off2[i] + pen[newLabels[i]]);
		double C = Math.max(0, -minTotal);
		for (int i = 0; i < newEntries.size(); i++) {
			double add = pen[newLabels[i]] + C;
			if (add == 0) continue;
			long oq = Math.round(Math.sqrt(qNew.off2[i] + add));
			double offset = Math.round((oq + 0.5) * 1000) / 1e7;
			newEntries.get(i).getAsJsonObject("parameters").addProperty("offset", offset);
			if (quant(offset) != oq) throw new IllegalStateException("offset round-trip failed for " + newEntries.get(i).get("biome").getAsString() + ": " + oq);
		}

		// --- result shares: brute-force the entries exactly as they'll be written ---
		Quant qOut = quantize(newEntries);
		double[] fitR = new double[qOut.n];
		double[][] resCounts = new double[nL][B];
		int agreeNew = 0;
		for (double[] s : points) {
			fitnessAll(qOut, s, fitR);
			int who = argminBiome(fitR, newLabels, qOut.n);
			resCounts[(int) s[0]][who]++;
			if (biomeIds.get(who).equals(at(dumpBiomes, (int) s[7]))) agreeNew++;
		}

		// --- write stats back into the config ---
		for (String b : cfg.keySet()) {
			Integer iObj = bIdx.get(b);
			if (iObj == null) continue; // config lists a biome the baseline no longer has (re-run the scanner)
			int i = iObj, L = layerOf[i];
			JsonObject c = cfg.getAsJsonObject(b);
			c.addProperty("layer", layers.get(L));
			JsonElement resultPct = pct(resCounts[L][i], layerTotals[L]);
			c.add("share_pct", pct(origCounts[L][i], layerTotals[L]));
			c.add("result_pct", resultPct);
			if (origCounts[L][i] == 0 && tuned.contains(b) && num(c.get("target_pct")) == null) {
				c.addProperty("note", "never hit in the sample set -- a multiplier has nothing to scale; dump more columns (/dumpclimate 20000)");
			} else if (target.containsKey(i) && Math.abs(resCounts[L][i] - target.get(i)) > Math.max(2, 0.05 * target.get(i))) {
				c.addProperty("note", "aimed for " + jsStr(pct(target.get(i), layerTotals[L])) + "%, got " + jsStr(resultPct) + "% -- "
						+ "either out of the penalty range (offset 0.6 either way) or its climate space is all-or-nothing (overlapping boxes)");
			}
		}
		JsonObject samples = new JsonObject();
		samples.addProperty("file", sampleSource.get(dimId));
		samples.add("points", jsNum(points.length));
		if (dump.has("seed")) samples.add("seed", dump.get("seed"));
		if (dump.has("generated")) samples.add("generated", dump.get("generated"));
		JsonObject layerPoints = new JsonObject();
		for (int L = 0; L < nL; L++) layerPoints.add(layers.get(L), jsNum(layerTotals[L]));
		samples.add("layer_points", layerPoints);
		dim.add("samples", samples);
		dim.add("model_agreement_pct", pct(agree, points.length));

		String liveMatch = agreeNew > agree
				? "live world matches the TUNED layout (" + jsStr(pct(agreeNew, points.length)) + "%)"
				: "live world matches the baseline (" + jsStr(pct(agree, points.length)) + "%)";
		out.summary = tuned.size() + " tuned, " + removed.size() + " removed, " + replaced.size() + " replaced; " + liveMatch;
		return out;
	}

	// ── climate maths ─────────────────────────────────────────────────────────

	private static final class Quant {
		int n;
		double[] mins, maxs, off2;
	}

	/** Climate.quantizeCoord: (long)(coord * 10000.0F), float maths, truncating cast. */
	static long quant(double v) {
		return (long) ((float) v * 10000.0F);
	}

	private static Quant quantize(List<JsonObject> entries) {
		Quant q = new Quant();
		q.n = entries.size();
		q.mins = new double[q.n * 6];
		q.maxs = new double[q.n * 6];
		q.off2 = new double[q.n];
		for (int i = 0; i < q.n; i++) {
			JsonObject params = entries.get(i).getAsJsonObject("parameters");
			for (int k = 0; k < 6; k++) {
				JsonElement v = params.get(AXES[k]);
				double lo, hi;
				if (v.isJsonArray()) {
					lo = v.getAsJsonArray().get(0).getAsDouble();
					hi = v.getAsJsonArray().get(1).getAsDouble();
				} else {
					lo = hi = v.getAsDouble();
				}
				q.mins[i * 6 + k] = quant(lo);
				q.maxs[i * 6 + k] = quant(hi);
			}
			JsonElement off = params.get("offset");
			double o = off == null || off.isJsonNull() ? 0 : quant(off.getAsDouble());
			q.off2[i] = o * o;
		}
		return q;
	}

	/** Base fitness (without the per-biome penalty) of every entry for one sample. */
	private static void fitnessAll(Quant q, double[] s, double[] out) {
		for (int i = 0; i < q.n; i++) {
			double f = q.off2[i];
			for (int k = 0; k < 6; k++) {
				double t = s[k + 1];
				double max = q.maxs[i * 6 + k], min = q.mins[i * 6 + k];
				double d = t > max ? t - max : (min > t ? min - t : 0);
				f += d * d;
			}
			out[i] = f;
		}
	}

	private static int argminBiome(double[] fit, int[] labels, int n) {
		double best = Double.POSITIVE_INFINITY;
		int who = -1;
		for (int i = 0; i < n; i++) if (fit[i] < best) { best = fit[i]; who = i; }
		return labels[who];
	}

	/**
	 * margins sorted descending: b wins a sample iff its penalty < that sample's margin. Pick the
	 * penalty that lets b win as close to k samples as possible, always strictly between two
	 * distinct margins -- overlapping entries give whole plateaus of identical margins (inside both
	 * boxes only the offsets differ), and a penalty ON a plateau would be a tie the game breaks
	 * arbitrarily.
	 */
	private static double penaltyFor(double[] margins, long kL) {
		int n = margins.length;
		if (n == 0) return 0;
		if (kL <= 0) return margins[0] + 2;
		if (kL >= n) return margins[n - 1] - 2;
		int k = (int) kL;
		int lo = k, hi = k; // plateau around the cut: margins[lo..hi] all equal margins[k]
		while (lo > 0 && margins[lo - 1] == margins[k]) lo--;
		while (hi < n - 1 && margins[hi + 1] == margins[k]) hi++;
		if (lo == k) return (margins[k - 1] + margins[k]) / 2; // clean cut, no plateau
		// win none of the plateau (count lo) or all of it (count hi+1)
		if (k - lo <= hi + 1 - k) return lo > 0 ? (margins[lo - 1] + margins[k]) / 2 : margins[k] + 2;
		return hi < n - 1 ? (margins[k] + margins[hi + 1]) / 2 : margins[k] - 2;
	}

	// ── helpers ───────────────────────────────────────────────────────────────

	static String resourcePath(String dimId) {
		int c = dimId.indexOf(':');
		return dimId.substring(0, c) + ":dimension/" + dimId.substring(c + 1) + ".json";
	}

	/**
	 * The dimension's climate samples (gzipped, else the pre-1.0.3 plain copy in exported/), or null. With a
	 * caves dump next to the gzipped file, its points replace the underground layer (see the class comment).
	 */
	static String readSamples(String dimId) throws IOException {
		String gz = sampleFile(dimId) + ".gz";
		String text = WorldgenFiles.readGzipText(SAMPLE_DIR + gz);
		if (text != null) {
			String cavesGz = cavesFile(dimId) + ".gz";
			String caves = WorldgenFiles.readGzipText(SAMPLE_DIR + cavesGz);
			if (caves == null) {
				sampleSource.put(dimId, "climate/" + gz);
				return text;
			}
			sampleSource.put(dimId, "climate/" + gz + " + climate/" + cavesGz);
			return mergeCaves(text, caves);
		}
		sampleSource.put(dimId, "../../exported/worldgen/climate/" + sampleFile(dimId));
		return WorldgenFiles.readText(LEGACY_SAMPLE_DIR + sampleFile(dimId));
	}

	static String cavesFile(String dimId) {
		return sampleFile(dimId).replaceFirst("\\.json$", ".caves.json");
	}

	/**
	 * Main dump + caves dump: the main dump's points minus its underground layer, plus every caves point in
	 * the underground layer. Layer and live-biome indexes of the caves points are remapped to the main lists.
	 * Point layout (both files): [layer, temperature, humidity, continentalness, erosion, depth, weirdness, live_biome].
	 */
	static String mergeCaves(String mainText, String cavesText) {
		JsonObject main = JsonParser.parseString(mainText).getAsJsonObject();
		JsonObject caves = JsonParser.parseString(cavesText).getAsJsonObject();
		JsonArray layers = main.getAsJsonArray("layers");
		JsonArray biomes = main.getAsJsonArray("biomes");
		List<String> layerNames = strings(layers), biomeNames = strings(biomes);
		int under = layerNames.indexOf("underground");
		if (under < 0) {
			layers.add("underground");
			under = layerNames.size();
		}
		List<String> caveBiomes = strings(caves.getAsJsonArray("biomes"));
		int[] remap = new int[caveBiomes.size()];
		for (int i = 0; i < remap.length; i++) {
			int j = biomeNames.indexOf(caveBiomes.get(i));
			if (j < 0) {
				biomes.add(caveBiomes.get(i));
				biomeNames.add(caveBiomes.get(i));
				j = biomeNames.size() - 1;
			}
			remap[i] = j;
		}
		JsonArray out = new JsonArray();
		for (JsonElement p : main.getAsJsonArray("points")) if (p.getAsJsonArray().get(0).getAsInt() != under) out.add(p);
		for (JsonElement p : caves.getAsJsonArray("points")) {
			JsonArray a = p.getAsJsonArray().deepCopy();
			a.set(0, new JsonPrimitive(under));
			int lb = a.get(7).getAsInt();
			if (lb >= 0 && lb < remap.length) a.set(7, new JsonPrimitive(remap[lb]));
			out.add(a);
		}
		main.add("points", out);
		if (caves.has("generated")) main.addProperty("caves_generated", caves.get("generated").getAsString());
		return COMPACT.toJson(main);
	}

	static String sampleFile(String dimId) {
		int c = dimId.indexOf(':');
		return dimId.substring(0, c) + "__" + dimId.substring(c + 1).replace("/", "__") + ".json";
	}

	private static int[] labels(List<JsonObject> entries, Map<String, Integer> bIdx) {
		int[] out = new int[entries.size()];
		for (int i = 0; i < out.length; i++) out[i] = bIdx.get(entries.get(i).get("biome").getAsString());
		return out;
	}

	private static List<String> strings(JsonArray arr) {
		List<String> out = new ArrayList<>();
		if (arr != null) for (JsonElement e : arr) out.add(e.isJsonNull() ? null : e.getAsString());
		return out;
	}

	private static String at(List<String> list, int i) {
		return i >= 0 && i < list.size() ? list.get(i) : null;
	}

	private static void reverse(double[] a) {
		for (int i = 0, j = a.length - 1; i < j; i++, j--) { double t = a[i]; a[i] = a[j]; a[j] = t; }
	}

	/** A non-empty string value, else null (JSON null / missing / "" all mean "no replacement"). */
	private static String str(JsonElement e) {
		if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) return null;
		String s = e.getAsString();
		return s.isEmpty() ? null : s;
	}

	private static Double num(JsonElement e) {
		if (e == null || !e.isJsonPrimitive()) return null;
		try {
			return e.getAsDouble();
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static JsonElement pct(double count, double total) {
		return total != 0 ? jsNum(Math.round(count / total * 1e5) / 1e3) : JsonNull.INSTANCE;
	}

	/** A number written the way JSON.stringify writes it: whole values without ".0". */
	private static JsonPrimitive jsNum(double d) {
		if (d == Math.rint(d) && Math.abs(d) < 1e15) return new JsonPrimitive((long) d);
		return new JsonPrimitive(d);
	}

	private static String jsStr(JsonElement e) {
		return e.isJsonNull() ? "null" : e.getAsJsonPrimitive().toString();
	}

	private static String fingerprint(String cfgText, Set<String> dimIds, Map<String, String> baselines, Map<String, String> samples) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(("solver " + maxCandidates + " " + rounds + "\0").getBytes(StandardCharsets.UTF_8));
		md.update(cfgText.getBytes(StandardCharsets.UTF_8));
		for (String d : dimIds) {
			md.update(("\0" + d + "\0").getBytes(StandardCharsets.UTF_8));
			md.update(baselines.get(d).getBytes(StandardCharsets.UTF_8));
			String s = samples.get(d);
			md.update((s == null ? "\0none" : s).getBytes(StandardCharsets.UTF_8));
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : md.digest()) sb.append(String.format("%02x", b));
		return sb.toString();
	}
}
