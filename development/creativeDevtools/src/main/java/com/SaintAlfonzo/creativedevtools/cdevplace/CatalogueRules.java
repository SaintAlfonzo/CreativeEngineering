package com.SaintAlfonzo.creativedevtools.cdevplace;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Structure Catalogue membership and family rules (feature_writeup/plan_structure_catalogue.md 3 and 4), from the
 * hand-edited {@code structure_world.json}: turns structure ids, template ids and pack tags into ordered columns.
 * No game classes, so it runs in the plain Java test (tools/test_worldgen_tools.sh).
 */
final class CatalogueRules {

	enum Kind { WHOLE, PIECE }

	/** Where one item belongs. {@code skip} non-null: not placed at all. */
	record Home(String category, String subfamily, String function, String biome, String skip) {}

	/** {@code template} non-null: place that template rather than generating the structure. */
	record Entry(String id, Kind kind, String template, boolean dry) {}

	/** {@code path}: category, subfamily, function, biome; unused levels are null. One column runs along +Z. */
	record Column(boolean unmapped, List<String> path, List<Entry> items) {}

	record Plan(List<Column> mapped, List<Column> unmapped, List<String> markers, List<String> entityPieces,
			List<String> noPieces, List<String> replaced) {

		List<Column> columns() {
			List<Column> all = new ArrayList<>(mapped);
			all.addAll(unmapped);
			return all;
		}

		int count(List<Column> columns, Kind kind) {
			return (int) columns.stream().flatMap(c -> c.items().stream()).filter(e -> e.kind() == kind).count();
		}

		int total() {
			return columns().stream().mapToInt(c -> c.items().size()).sum();
		}
	}

	static final String UNMAPPED = "Unmapped";
	private static final String ANY = "Any";
	private static final int LEVELS = 4;

	/** Category, subfamily, function, biome. */
	final int[] levelGaps;
	final int variationGap, originX, originZ;

	private final List<String> categoryOrder, subfamilyOrder, functionOrder;
	private final Pattern skipStructures, replacedNotPath, pieceIdMatch, skyId, dryId;
	private final String replacedType, skyCategory, skySubfamilyPrefix, skyBiome, pieceCategory;
	private final Set<String> replacedIds, dryCategories, drySubfamilies, landIgnore, zombieMerged;
	private final List<PieceMatch> pieceMatch = new ArrayList<>();
	private final List<WholeRule> wholeRules = new ArrayList<>();
	private final List<PieceRule> pieceRules = new ArrayList<>();
	private final List<BiomeRule> pieceBiomeRules = new ArrayList<>();
	private final List<String> wholeWords, pieceWords;
	private final Map<String, String> wordAliases = new LinkedHashMap<>();
	private final String landPrefix, zombieMark, zombieSuffix;
	private final JsonObject overrides;

	private record Cond(Set<String> types, List<String> typePrefixes, Pattern path) {
		boolean test(String idPath, List<String> idTypes) {
			if (types.isEmpty() && typePrefixes.isEmpty() && path == null) return true;
			if (path != null && path.matcher(idPath).find()) return true;
			for (String t : idTypes) {
				if (types.contains(t)) return true;
				for (String p : typePrefixes) if (t.startsWith(p)) return true;
			}
			return false;
		}
	}

	private record Choice(Cond when, String value) {}

	private record WholeRule(Cond when, String category, List<Choice> subfamily, Map<String, String> firstType, List<Choice> function) {}

	private record PieceRule(Pattern path, Pattern leaf, Pattern notLeaf, String skip, String subfamily, String function) {}

	private record PieceMatch(Pattern id, String pieces) {}

	private record BiomeRule(Pattern id, String prefix) {}

	CatalogueRules(JsonObject o) {
		JsonObject gaps = o.getAsJsonObject("gaps");
		levelGaps = new int[]{gaps.get("category").getAsInt(), gaps.get("subfamily").getAsInt(), gaps.get("function").getAsInt(), gaps.get("biome").getAsInt()};
		variationGap = gaps.get("variation").getAsInt();
		originX = o.getAsJsonObject("origin").get("x").getAsInt();
		originZ = o.getAsJsonObject("origin").get("z").getAsInt();

		JsonObject order = o.getAsJsonObject("order");
		categoryOrder = strings(order.get("categories"));
		subfamilyOrder = strings(order.get("subfamilies"));
		functionOrder = strings(order.get("functions"));

		skipStructures = Pattern.compile(o.get("skipStructures").getAsString());
		JsonObject replaced = o.getAsJsonObject("replacedByPieces");
		replacedType = replaced.get("type").getAsString();
		replacedNotPath = Pattern.compile(replaced.get("notPath").getAsString());
		replacedIds = new LinkedHashSet<>(strings(replaced.get("ids")));
		for (JsonElement e : o.getAsJsonArray("pieceMatch")) {
			JsonObject m = e.getAsJsonObject();
			pieceMatch.add(new PieceMatch(Pattern.compile(m.get("id").getAsString()), m.get("pieces").getAsString()));
		}

		for (JsonElement e : o.getAsJsonArray("whole")) {
			JsonObject r = e.getAsJsonObject();
			Map<String, String> firstType = null;
			if (r.has("subfamilyFromFirstType")) {
				firstType = new LinkedHashMap<>();
				for (Map.Entry<String, JsonElement> f : r.getAsJsonObject("subfamilyFromFirstType").entrySet()) firstType.put(f.getKey(), f.getValue().getAsString());
			}
			wholeRules.add(new WholeRule(cond(r.get("when")), r.get("category").getAsString(), choices(r.get("subfamily")), firstType, choices(r.get("function"))));
		}

		JsonObject wb = o.getAsJsonObject("wholeBiome");
		wholeWords = strings(wb.get("words"));
		for (Map.Entry<String, JsonElement> a : wb.getAsJsonObject("aliases").entrySet()) wordAliases.put(a.getKey(), a.getValue().getAsString());
		landPrefix = wb.get("landTagPrefix").getAsString();
		landIgnore = new LinkedHashSet<>(strings(wb.get("landTagIgnore")));

		JsonObject pieces = o.getAsJsonObject("pieces");
		pieceIdMatch = Pattern.compile(pieces.get("idMatch").getAsString());
		pieceCategory = pieces.get("category").getAsString();
		JsonObject sky = pieces.getAsJsonObject("sky");
		skyId = Pattern.compile(sky.get("id").getAsString());
		skyCategory = sky.get("category").getAsString();
		skySubfamilyPrefix = sky.get("subfamilyPrefix").getAsString();
		skyBiome = sky.get("biome").getAsString();
		for (JsonElement e : pieces.getAsJsonArray("rules")) {
			JsonObject r = e.getAsJsonObject();
			pieceRules.add(new PieceRule(regex(r, "path"), regex(r, "leaf"), regex(r, "notLeaf"), text(r, "skip"), text(r, "subfamily"), text(r, "function")));
		}
		JsonObject pb = pieces.getAsJsonObject("biome");
		for (JsonElement e : pb.getAsJsonArray("rules")) {
			JsonObject r = e.getAsJsonObject();
			pieceBiomeRules.add(new BiomeRule(Pattern.compile(r.get("id").getAsString()), r.has("prefix") ? r.get("prefix").getAsString() : ""));
		}
		pieceWords = strings(pb.get("words"));
		zombieMark = pb.get("zombie").getAsString();
		zombieSuffix = pb.get("zombieSuffix").getAsString();
		zombieMerged = new LinkedHashSet<>(strings(pb.get("zombieMergedIn")));

		JsonObject dry = o.getAsJsonObject("drying");
		dryCategories = new LinkedHashSet<>(strings(dry.get("categories")));
		drySubfamilies = new LinkedHashSet<>(strings(dry.get("subfamilies")));
		dryId = Pattern.compile(dry.get("id").getAsString());

		overrides = o.has("overrides") ? o.getAsJsonObject("overrides") : new JsonObject();
	}

	static CatalogueRules parse(String text) {
		return new CatalogueRules(parseLenient(text));
	}

	static JsonObject parseLenient(String text) {
		JsonReader reader = new JsonReader(new StringReader(text));
		reader.setLenient(true);
		return JsonParser.parseReader(reader).getAsJsonObject();
	}

	private static List<String> strings(JsonElement e) {
		List<String> out = new ArrayList<>();
		if (e != null && e.isJsonArray()) for (JsonElement x : e.getAsJsonArray()) out.add(x.getAsString());
		return out;
	}

	private static Pattern regex(JsonObject o, String key) {
		return o.has(key) ? Pattern.compile(o.get(key).getAsString()) : null;
	}

	private static String text(JsonObject o, String key) {
		return o.has(key) ? o.get(key).getAsString() : null;
	}

	private static Cond cond(JsonElement e) {
		if (e == null) return new Cond(Set.of(), List.of(), null);
		JsonObject o = e.getAsJsonObject();
		return new Cond(new LinkedHashSet<>(strings(o.get("type"))), strings(o.get("typePrefix")), regex(o, "path"));
	}

	/** A plain string is a single always-true choice. */
	private static List<Choice> choices(JsonElement e) {
		List<Choice> out = new ArrayList<>();
		if (e == null) return out;
		if (e.isJsonPrimitive()) {
			out.add(new Choice(cond(null), e.getAsString()));
			return out;
		}
		JsonArray a = e.getAsJsonArray();
		for (JsonElement c : a) out.add(new Choice(cond(c.getAsJsonObject().get("when")), c.getAsJsonObject().get("value").getAsString()));
		return out;
	}

	// ── resolving one id ─────────────────────────────────────────────────────

	private static String pathOf(String id) {
		return id.substring(id.indexOf(':') + 1);
	}

	private static String namespaceOf(String id) {
		int colon = id.indexOf(':');
		return colon < 0 ? "minecraft" : id.substring(0, colon);
	}

	private static String leafOf(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/** Short names of the id's {@code type/*} tags, in tag-file order. */
	static List<String> typesOf(String id, Map<String, Set<String>> tags) {
		List<String> out = new ArrayList<>();
		for (Map.Entry<String, Set<String>> e : tags.entrySet())
			if (e.getKey().startsWith("type/") && e.getValue().contains(id)) out.add(e.getKey().substring(5));
		return out;
	}

	/** The home of a whole structure that has at least one type tag. */
	Home whole(String id, List<String> types, Map<String, Set<String>> tags) {
		String path = pathOf(id);
		for (WholeRule r : wholeRules) {
			if (!r.when().test(path, types)) continue;
			String subfamily;
			if (r.firstType() != null) {
				String first = types.get(0);
				subfamily = r.firstType().getOrDefault(first, title(first));
			} else {
				subfamily = choose(r.subfamily(), path, types);
			}
			return new Home(r.category(), subfamily, choose(r.function(), path, types), wholeBiome(id, tags), null);
		}
		throw new IllegalStateException("no whole-structure rule matched " + id + " (the rule file needs a last rule with no 'when')");
	}

	private static String choose(List<Choice> choices, String path, List<String> types) {
		for (Choice c : choices) if (c.when().test(path, types)) return c.value();
		return null;
	}

	private String wholeBiome(String id, Map<String, Set<String>> tags) {
		String leaf = leafOf(pathOf(id));
		for (String word : wholeWords) {
			if (Pattern.compile("(^|_)" + Pattern.quote(word) + "(_|$)").matcher(leaf).find())
				return wordAliases.containsKey(word) ? wordAliases.get(word) : title(word);
		}
		for (Map.Entry<String, Set<String>> e : tags.entrySet()) {
			String name = e.getKey();
			if (name.startsWith(landPrefix) && !landIgnore.contains(name) && e.getValue().contains(id))
				return title(name.substring(landPrefix.length()));
		}
		return ANY;
	}

	boolean isPiece(String templateId) {
		return pieceIdMatch.matcher(templateId).find();
	}

	/** The home of a village or sky village piece. */
	Home piece(String id) {
		String path = pathOf(id), leaf = leafOf(path);
		for (PieceRule r : pieceRules) {
			if (r.path() != null && !r.path().matcher(path).find()) continue;
			if (r.leaf() != null && !r.leaf().matcher(leaf).find()) continue;
			if (r.notLeaf() != null && r.notLeaf().matcher(leaf).find()) continue;
			if (r.skip() != null) return new Home(null, null, null, null, r.skip());
			if (skyId.matcher(id).find()) return new Home(skyCategory, skySubfamilyPrefix + r.subfamily(), r.function(), skyBiome, null);
			return new Home(pieceCategory, r.subfamily(), r.function(), pieceBiome(id, r.function()), null);
		}
		throw new IllegalStateException("no piece rule matched " + id + " (the rule file needs a last rule that matches anything)");
	}

	private String pieceBiome(String id, String function) {
		String suffix = id.contains(zombieMark) && !zombieMerged.contains(function) ? zombieSuffix : "";
		for (BiomeRule r : pieceBiomeRules) {
			Matcher m = r.id().matcher(id);
			if (m.find()) return title(r.prefix() + m.group(1)) + suffix;
		}
		String path = pathOf(id);
		for (String word : pieceWords) if (path.contains(word)) return title(word) + suffix;
		return ANY;
	}

	/** Python's str.title() over the name with underscores as spaces: "flower_forest" is "Flower Forest". */
	static String title(String s) {
		StringBuilder out = new StringBuilder(s.length());
		boolean afterLetter = false;
		for (char c : s.replace('_', ' ').toCharArray()) {
			out.append(afterLetter ? Character.toLowerCase(c) : Character.toUpperCase(c));
			afterLetter = Character.isLetter(c);
		}
		return out.toString();
	}

	private boolean isDry(String id, Home h) {
		JsonObject ov = override(id);
		if (ov != null && ov.has("dry")) return ov.get("dry").getAsBoolean();
		return (h != null && (dryCategories.contains(h.category()) || drySubfamilies.contains(h.subfamily()))) || dryId.matcher(id).find();
	}

	private JsonObject override(String id) {
		return overrides.has(id) ? overrides.getAsJsonObject(id) : null;
	}

	private String overrideTemplate(String id) {
		JsonObject ov = override(id);
		return ov != null && ov.has("template") ? ov.get("template").getAsString() : null;
	}

	private Home applyOverride(String id, Home h) {
		JsonObject ov = override(id);
		if (ov == null) return h;
		return new Home(ov.has("category") ? ov.get("category").getAsString() : h.category(),
				ov.has("subfamily") ? ov.get("subfamily").getAsString() : h.subfamily(),
				ov.has("function") ? ov.get("function").getAsString() : h.function(),
				ov.has("biome") ? ov.get("biome").getAsString() : h.biome(), h.skip());
	}

	/** True when this structure's place is taken by its individual pieces. */
	private boolean isReplaced(String id, List<String> types) {
		return replacedIds.contains(id) || (types.contains(replacedType) && !replacedNotPath.matcher(pathOf(id)).find());
	}

	private boolean hasPieces(String id, Collection<String> pieces) {
		for (PieceMatch m : pieceMatch) {
			Matcher match = m.id().matcher(id);
			if (!match.find()) continue;
			String regex = match.groupCount() > 0 ? m.pieces().replace("$1", Pattern.quote(match.group(1))) : m.pieces();
			Pattern p = Pattern.compile(regex);
			return pieces.stream().anyMatch(t -> p.matcher(t).find());
		}
		return false;
	}

	// ── the plan ─────────────────────────────────────────────────────────────

	Plan build(Collection<String> structures, Collection<String> templates, Map<String, Set<String>> tags) {
		Map<List<String>, List<Entry>> mapped = new LinkedHashMap<>();
		Map<List<String>, List<Entry>> unmapped = new LinkedHashMap<>();
		List<String> markers = new ArrayList<>(), entities = new ArrayList<>(), noPieces = new ArrayList<>(), replaced = new ArrayList<>(), pieces = new ArrayList<>();

		for (String t : new TreeSet<>(templates)) {
			if (!isPiece(t)) continue;
			Home h = piece(t);
			if (h.skip() != null) { entities.add(t); continue; }
			pieces.add(t);
			mapped.computeIfAbsent(path(applyOverride(t, h)), k -> new ArrayList<>()).add(new Entry(t, Kind.PIECE, null, false));
		}

		for (String id : new TreeSet<>(structures)) {
			if (skipStructures.matcher(id).find()) { markers.add(id); continue; }
			List<String> types = typesOf(id, tags);
			if (isReplaced(id, types)) {
				if (hasPieces(id, pieces)) { replaced.add(id); continue; }
				noPieces.add(id);
			}
			if (types.isEmpty()) {
				List<String> p = Arrays.asList(UNMAPPED, null, null, namespaceOf(id));
				unmapped.computeIfAbsent(p, k -> new ArrayList<>()).add(new Entry(id, Kind.WHOLE, overrideTemplate(id), isDry(id, null)));
				continue;
			}
			Home h = applyOverride(id, whole(id, types, tags));
			mapped.computeIfAbsent(path(h), k -> new ArrayList<>()).add(new Entry(id, Kind.WHOLE, overrideTemplate(id), isDry(id, h)));
		}
		return new Plan(columns(mapped, false), columns(unmapped, true), markers, entities, noPieces, replaced);
	}

	private static List<String> path(Home h) {
		return Arrays.asList(h.category(), h.subfamily(), h.function(), h.biome());
	}

	private List<Column> columns(Map<List<String>, List<Entry>> groups, boolean unmapped) {
		Comparator<Entry> byId = (a, b) -> natural(a.id(), b.id());
		List<Column> out = new ArrayList<>();
		for (Map.Entry<List<String>, List<Entry>> e : groups.entrySet()) {
			List<Entry> items = new ArrayList<>(e.getValue());
			items.sort(byId);
			out.add(new Column(unmapped, e.getKey(), items));
		}
		out.sort((a, b) -> comparePaths(a.path(), b.path()));
		return out;
	}

	int comparePaths(List<String> a, List<String> b) {
		for (int level = 0; level < LEVELS; level++) {
			int c = compareLevel(level, a.get(level), b.get(level));
			if (c != 0) return c;
		}
		return 0;
	}

	private int compareLevel(int level, String a, String b) {
		if (a == null || b == null) return a == null ? (b == null ? 0 : -1) : 1;
		if (a.equals(b)) return 0;
		if (level == LEVELS - 1) {
			if (a.equals(ANY) != b.equals(ANY)) return a.equals(ANY) ? 1 : -1;
			return natural(a, b);
		}
		List<String> order = level == 0 ? categoryOrder : level == 1 ? subfamilyOrder : functionOrder;
		int ia = order.indexOf(a), ib = order.indexOf(b);
		if (ia >= 0 && ib >= 0) return Integer.compare(ia, ib);
		if (ia >= 0 || ib >= 0) return ia >= 0 ? -1 : 1;
		return natural(a, b);
	}

	/** Natural order: digit runs compare as numbers, so {@code _2} sorts before {@code _10}. */
	static int natural(String a, String b) {
		int i = 0, j = 0;
		while (i < a.length() && j < b.length()) {
			char ca = a.charAt(i), cb = b.charAt(j);
			if (Character.isDigit(ca) && Character.isDigit(cb)) {
				int si = i, sj = j;
				while (i < a.length() && Character.isDigit(a.charAt(i))) i++;
				while (j < b.length() && Character.isDigit(b.charAt(j))) j++;
				int c = new java.math.BigInteger(a.substring(si, i)).compareTo(new java.math.BigInteger(b.substring(sj, j)));
				if (c != 0) return c;
			} else {
				if (ca != cb) return Character.compare(ca, cb);
				i++;
				j++;
			}
		}
		return Integer.compare(a.length() - i, b.length() - j);
	}

	// ── structure_tags.json over a list of ids (also /cdevplace's filters) ────

	/** Each tag's member ids, in the file's order. Unknown ids are just skipped. */
	static Map<String, Set<String>> resolveTags(JsonObject tags, List<String> all) {
		Map<String, List<String>> defs = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> e : tags.entrySet()) {
			JsonElement v = e.getValue().isJsonObject() ? e.getValue().getAsJsonObject().get("values") : e.getValue();
			defs.put(e.getKey(), strings(v));
		}
		Map<String, Set<String>> done = new TreeMap<>();
		for (String name : defs.keySet()) resolve(name, defs, all, done, new LinkedHashSet<>());
		Map<String, Set<String>> ordered = new LinkedHashMap<>();
		for (String name : defs.keySet()) if (done.containsKey(name)) ordered.put(name, done.get(name));
		return ordered;
	}

	private static Set<String> resolve(String name, Map<String, List<String>> defs, List<String> all, Map<String, Set<String>> done, Set<String> resolving) {
		if (done.containsKey(name)) return done.get(name);
		if (!defs.containsKey(name) || !resolving.add(name)) return Set.of();
		Set<String> inc = new LinkedHashSet<>(), exc = new LinkedHashSet<>();
		for (String raw : defs.get(name)) {
			boolean neg = raw.startsWith("!");
			String v = neg ? raw.substring(1) : raw;
			Collection<String> ids;
			if (v.startsWith("#")) ids = resolve(v.substring(1), defs, all, done, resolving);
			else if (v.contains("*")) { Pattern p = glob(v); ids = all.stream().filter(id -> p.matcher(id).matches()).toList(); }
			else ids = all.contains(v) ? List.of(v) : List.of();
			(neg ? exc : inc).addAll(ids);
		}
		resolving.remove(name);
		inc.removeAll(exc);
		done.put(name, inc);
		return inc;
	}

	static Pattern glob(String g) {
		StringBuilder sb = new StringBuilder("^");
		String[] parts = g.split("\\*", -1);
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) sb.append(".*");
			sb.append(Pattern.quote(parts[i]));
		}
		return Pattern.compile(sb.append('$').toString());
	}
}
