package com.SaintAlfonzo.solraspberrypie.ladder;

import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Reads serverconfig/solraspberrypie/ladder.json once at server start (3), seeding it from
 * defaultconfigs/solraspberrypie/ladder.json or the built-in default, and replacing an unedited copy when that default
 * changed (see {@link #load}). Applies the validation table (3.3):
 * unregistered effects and attributes are skipped (one warning listing them), unusable unlocks dropped, levels clamped,
 * refresh-unsafe effects refused, a broken file falls back to the built-in default with a loud error. Optional fields
 * (raspberry_pie.md 3.3): {@code "toggle": "on"|"off"} on a pool entry or a flat rung makes that effect a toggle (on or off
 * by default), {@code "tagline": "<lang key>"} on a flat or unlock rung names its hover tagline, {@code "replaces": [ids]} on a
 * pool entry names pool effects that effect replaces once unlocked (food-tuning #49).
 */
public final class LadderLoader {
	private static final String BUILT_IN = "/solraspberrypie/default_ladder.json";
	/** V10: a refresh would refill, clamp or re-fire these. Instant effects are refused too. */
	private static final Set<MobEffect> REFRESH_UNSAFE = Set.of(MobEffects.ABSORPTION, MobEffects.HEALTH_BOOST, MobEffects.SATURATION);

	/** What {@link #reset} did: the ladder now loaded, where the fresh copy came from, the old copy's file name. */
	public record Reset(Ladder ladder, String source, String backup) {}

	private static Path worldFile(MinecraftServer server) {
		return server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve(SolRaspberryPie.MOD_ID).resolve("ladder.json");
	}

	/** Log names: never an absolute path (PR-RP-22). */
	private static final String WORLD_NAME = "<world>/serverconfig/" + SolRaspberryPie.MOD_ID + "/ladder.json";
	private static final String SEED_NAME = "defaultconfigs/" + SolRaspberryPie.MOD_ID + "/ladder.json";

	/**
	 * {@code /solrp ladder reset} (PR-RP-18): moves the world's ladder copy to {@code ladder.json.YYYY-MM-DD_HHMM.bak}
	 * beside it, then {@link #load} seeds a fresh copy from {@code defaultconfigs} (or the jar default) and reads it.
	 */
	public static Reset reset(MinecraftServer server) throws IOException {
		Path worldFile = worldFile(server);
		String backup = Files.exists(worldFile) ? backup(worldFile) : "none";
		return new Reset(load(server), seedSource(), backup);
	}

	/** Moves the world's copy to a dated .bak beside it; returns the backup's file name. */
	private static String backup(Path worldFile) throws IOException {
		String stamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
		Path bak = worldFile.resolveSibling("ladder.json." + stamp + ".bak");
		for (int i = 2; Files.exists(bak); i++) bak = worldFile.resolveSibling("ladder.json." + stamp + "-" + i + ".bak");
		Files.move(worldFile, bak);
		return bak.getFileName().toString();
	}

	private static Path seedFile() {
		return FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve(SolRaspberryPie.MOD_ID).resolve("ladder.json");
	}

	private static String seedSource() {
		return Files.exists(seedFile()) ? SEED_NAME : "the built-in default ladder";
	}

	/** The ladder a new world gets: defaultconfigs, else the jar default. */
	private static byte[] seedBytes() throws IOException {
		if (Files.exists(seedFile())) return Files.readAllBytes(seedFile());
		try (InputStream in = LadderLoader.class.getResourceAsStream(BUILT_IN)) {
			if (in == null) throw new IOException("built-in default ladder missing from the jar");
			return in.readAllBytes();
		}
	}

	private static String sha1(byte[] data) {
		try {
			return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-1").digest(data));
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Seeds or updates the world's copy, then reads it. Beside the copy, {@code ladder.json.seeded} holds the SHA-1 of the
	 * ladder as it was seeded. At load, a copy that differs from the current default is replaced (old copy kept as a
	 * dated .bak, one log line) when it still matches that hash, i.e. nobody edited it; an edited copy, or one seeded
	 * before the hash existed, is kept with a warning ({@code /solrp ladder reset} replaces it). A copy equal to the
	 * default gets its hash written, so it is tracked from then on (PR-RP-18, owner 2026-11-23).
	 */
	public static Ladder load(MinecraftServer server) {
		Path worldFile = worldFile(server);
		Path hashFile = worldFile.resolveSibling("ladder.json.seeded");
		try {
			byte[] fresh = seedBytes();
			String freshHash = sha1(fresh);
			if (!Files.exists(worldFile)) {
				Files.createDirectories(worldFile.getParent());
				Files.write(worldFile, fresh);
				Files.writeString(hashFile, freshHash, StandardCharsets.UTF_8);
				SolRaspberryPie.LOG.info("Seeded {} from {}", WORLD_NAME, seedSource());
			} else {
				String current = sha1(Files.readAllBytes(worldFile));
				String seeded = Files.exists(hashFile) ? Files.readString(hashFile, StandardCharsets.UTF_8).trim() : null;
				if (current.equals(freshHash)) {
					if (!freshHash.equals(seeded)) Files.writeString(hashFile, freshHash, StandardCharsets.UTF_8);
				} else if (current.equals(seeded)) {
					String bak = backup(worldFile);
					Files.write(worldFile, fresh);
					Files.writeString(hashFile, freshHash, StandardCharsets.UTF_8);
					SolRaspberryPie.LOG.info("Updated {} from {} (the copy was unedited; old copy saved as {})", WORLD_NAME, seedSource(), bak);
				} else {
					SolRaspberryPie.LOG.warn("{} differs from {} and was edited or predates edit tracking; kept. /solrp ladder reset replaces it",
							WORLD_NAME, seedSource());
				}
			}
		} catch (Exception e) {
			SolRaspberryPie.LOG.error("Could not seed or update {}: {}", WORLD_NAME, e.toString());
		}
		try (Reader r = Files.newBufferedReader(worldFile, StandardCharsets.UTF_8)) {
			Ladder ladder = parse(JsonParser.parseReader(r).getAsJsonObject(), WORLD_NAME);
			SolRaspberryPie.LOG.info("Diet ladder: {} rungs, {} slots, {} pool effects", ladder.rungs.size(), ladder.slotCount(), ladder.pool.size());
			return ladder;
		} catch (Exception e) {
			// V13 (Q-H): fall back to the built-in ladder, loudly.
			SolRaspberryPie.LOG.error("================================================================");
			SolRaspberryPie.LOG.error("DIET LADDER FILE IS BROKEN OR MISSING: {}", WORLD_NAME);
			SolRaspberryPie.LOG.error("  {}", e.toString());
			SolRaspberryPie.LOG.error("  Using the built-in default ladder until the file is fixed and the server restarted.");
			SolRaspberryPie.LOG.error("================================================================");
			return builtIn();
		}
	}

	public static Ladder builtIn() {
		try (InputStream in = LadderLoader.class.getResourceAsStream(BUILT_IN);
			 Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return parse(JsonParser.parseReader(r).getAsJsonObject(), "built-in default");
		} catch (Exception e) {
			SolRaspberryPie.LOG.error("Built-in ladder unreadable: {}", e.toString());
			return new Ladder(List.of(), Map.of());
		}
	}

	static Ladder parse(JsonObject root, String source) {
		return parse(root, source, new ArrayList<>());
	}

	/**
	 * As {@link #parse(JsonObject, String)}, and adds every warning it logs to {@code warnings} (the skipped-entry
	 * summary first); the headless default-ladder test (tools/test_rp_ladder.sh) asserts the built-in ladder adds none.
	 */
	static Ladder parse(JsonObject root, String source, List<String> warnings) {
		List<String> skipped = new ArrayList<>();
		int firstOwn = warnings.size();

		Map<ResourceLocation, Integer> pool = new LinkedHashMap<>();
		Map<ResourceLocation, Boolean> toggles = new LinkedHashMap<>();
		Map<ResourceLocation, Boolean> flatToggles = new LinkedHashMap<>();
		Map<ResourceLocation, List<ResourceLocation>> replacesRaw = new LinkedHashMap<>();
		for (JsonElement el : arr(root, "pool")) {
			JsonObject o = el.getAsJsonObject();
			ResourceLocation id = new ResourceLocation(o.get("effect").getAsString());
			int cap = o.has("max_level") ? Math.max(1, o.get("max_level").getAsInt()) : 1;
			MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
			if (!ForgeRegistries.MOB_EFFECTS.containsKey(id) || effect == null) {
				skipped.add("pool " + id);
				continue;
			}
			if (refreshUnsafe(effect)) {
				warnings.add("V10: " + id + " cannot be refreshed safely; removed from the pool");
				continue;
			}
			pool.put(id, cap);
			Boolean t = toggle(o, "pool " + id, warnings);
			if (t != null) toggles.put(id, t);
			if (o.has("replaces")) {
				List<ResourceLocation> list = new ArrayList<>();
				JsonElement rv = o.get("replaces");
				for (JsonElement x : rv.isJsonArray() ? rv.getAsJsonArray() : single(rv)) list.add(new ResourceLocation(x.getAsString()));
				replacesRaw.put(id, list);
			}
		}
		// "replaces": only pool effects count; an unregistered or unpooled target is dropped quietly when its mod is
		// absent (A6), a self reference is refused.
		Map<ResourceLocation, List<ResourceLocation>> replaces = new LinkedHashMap<>();
		replacesRaw.forEach((id, list) -> {
			List<ResourceLocation> keep = new ArrayList<>();
			for (ResourceLocation r : list) {
				if (r.equals(id)) warnings.add("replaces: " + id + " names itself; ignored");
				else if (pool.containsKey(r)) keep.add(r);
				else if (ForgeRegistries.MOB_EFFECTS.containsKey(r)) warnings.add("replaces: " + id + " names " + r + ", which is not in the slot pool; ignored");
			}
			if (!keep.isEmpty()) replaces.put(id, keep);
		});

		List<Rung> rungs = new ArrayList<>();
		for (JsonElement el : arr(root, "rungs")) {
			JsonObject o = el.getAsJsonObject();
			double t = o.get("threshold").getAsDouble();
			String type = o.get("type").getAsString().toLowerCase(Locale.ROOT);
			switch (type) {
				case "health" -> rungs.add(new Rung(Rung.Kind.HEALTH, t, null, o.get("amount").getAsDouble(), 0));
				case "attribute" -> {
					ResourceLocation id = new ResourceLocation(o.get("attribute").getAsString());
					if (!ForgeRegistries.ATTRIBUTES.containsKey(id)) {
						skipped.add("attribute " + id + " at " + t);
						continue;
					}
					rungs.add(new Rung(Rung.Kind.ATTRIBUTE, t, id, o.get("amount").getAsDouble(), 0));
				}
				case "slot" -> rungs.add(new Rung(Rung.Kind.SLOT, t, null, 0, 0));
				case "flat", "unlock" -> {
					ResourceLocation id = new ResourceLocation(o.get("effect").getAsString());
					int level = o.has("level") ? Math.max(1, o.get("level").getAsInt()) : 1;
					MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(id);
					if (!ForgeRegistries.MOB_EFFECTS.containsKey(id) || effect == null) {
						skipped.add(type + " " + id + " at " + t);
						continue;
					}
					if (type.equals("flat")) {
						// Satiated (minecraft:saturation) is the one instant effect allowed as a flat rung (owner): it ticks every
						// tick while active, so a refresh only extends it (raspberry_pie.md 3.3, 4).
						if (refreshUnsafe(effect) && effect != MobEffects.SATURATION) {
							warnings.add("V10: flat " + id + " at " + t + " cannot be refreshed safely; skipped");
							continue;
						}
						rungs.add(new Rung(Rung.Kind.FLAT, t, id, 0, level, tagline(o)));
						Boolean tg = toggle(o, "flat " + id + " at " + t, warnings);
						if (tg != null) {
							Boolean before = flatToggles.put(id, tg);
							if (before != null && !before.equals(tg)) {
								warnings.add("toggle: flat " + id + " is 'on' in one rung and 'off' in another; the later rung wins");
							}
						}
					} else {
						Integer cap = pool.get(id);
						if (cap == null) {
							warnings.add("V3: unlock " + id + " at " + t + " is not in the slot pool; skipped");
							continue;
						}
						if (level > cap) {
							warnings.add("V5: unlock " + id + " level " + level + " at " + t + " is above the pool cap " + cap + "; clamped");
							level = cap;
						}
						rungs.add(new Rung(Rung.Kind.UNLOCK, t, id, 0, level, tagline(o)));
					}
				}
				default -> warnings.add("unknown rung type '" + type + "' at " + t + "; skipped");
			}
		}

		flatToggles.forEach((id, on) -> {
			Boolean pooled = toggles.put(id, on);
			if (pooled != null && !pooled.equals(on)) {
				warnings.add("toggle: " + id + " is '" + (pooled ? "on" : "off") + "' in the pool and '" + (on ? "on" : "off")
						+ "' as a flat rung; the flat rung's default is used");
			}
		});
		Ladder ladder = new Ladder(rungs, pool, toggles, replaces);

		// V4: pool effects nobody can unlock.
		for (ResourceLocation id : pool.keySet()) {
			if (ladder.rungs.stream().noneMatch(r -> r.kind == Rung.Kind.UNLOCK && id.equals(r.id))) {
				warnings.add("V4: pool effect " + id + " has no unlock rung and can never be picked");
			}
		}
		// V6: a higher level unlocked before a lower one (the higher one implies the lower ones, so it still works).
		Map<ResourceLocation, Map<Integer, Double>> firstAt = new LinkedHashMap<>();
		for (Rung r : ladder.rungs) {
			if (r.kind == Rung.Kind.UNLOCK) firstAt.computeIfAbsent(r.id, k -> new LinkedHashMap<>()).putIfAbsent(r.level, r.threshold);
		}
		firstAt.forEach((id, levels) -> levels.forEach((lvl, at) -> levels.forEach((lvl2, at2) -> {
			if (lvl2 < lvl && at2 > at) {
				warnings.add("V6: " + id + " level " + lvl + " unlocks at " + at + " before level " + lvl2 + " at " + at2
						+ "; the higher unlock also grants the lower levels");
			}
		})));
		// V7: slot milestones on the same threshold.
		Set<Double> slotThresholds = new HashSet<>();
		for (Rung s : ladder.slots) {
			if (!slotThresholds.add(s.threshold)) warnings.add("V7: two slot milestones at " + s.threshold + "; numbered in file order");
		}
		// V14: an unlock that can never be slotted, because a flat rung of the same effect is reached no later than the
		// first slot (a flat bonus hides the effect from the picker, V11).
		if (!ladder.slots.isEmpty()) {
			double firstSlot = ladder.slots.get(0).threshold;
			for (Rung u : ladder.rungs) {
				if (u.kind != Rung.Kind.UNLOCK) continue;
				for (Rung f : ladder.rungs) {
					if (f.kind == Rung.Kind.FLAT && u.id.equals(f.id) && f.threshold <= firstSlot) {
						warnings.add("V14: unlock " + u.id + " at " + u.threshold + " can never be slotted: the flat rung at " + f.threshold
								+ " grants it before the first slot at " + firstSlot);
						break;
					}
				}
			}
		}
		// V12: unreachable thresholds.
		double reach = maxReachableDiversity();
		if (reach > 0) {
			for (Rung r : ladder.rungs) {
				if (r.threshold > reach) warnings.add("V12: " + r + " is above the highest reachable diversity (about " + String.format(Locale.ROOT, "%.1f", reach) + ")");
			}
		}

		if (!skipped.isEmpty()) {
			String line = "skipped " + skipped.size() + " entries naming effects or attributes that are not registered: " + String.join(", ", skipped);
			SolRaspberryPie.LOG.warn("Diet ladder ({}): {}", source, line);
			warnings.add(firstOwn, line);
		}
		for (String w : warnings.subList(skipped.isEmpty() ? firstOwn : firstOwn + 1, warnings.size())) {
			SolRaspberryPie.LOG.warn("Diet ladder ({}): {}", source, w);
		}
		return ladder;
	}

	/** The optional "toggle" field: "on" or "off" (the default state), null when absent. */
	private static Boolean toggle(JsonObject o, String what, List<String> warnings) {
		if (!o.has("toggle")) return null;
		String v = o.get("toggle").getAsString().toLowerCase(Locale.ROOT);
		if (v.equals("on")) return Boolean.TRUE;
		if (v.equals("off")) return Boolean.FALSE;
		warnings.add("toggle: " + what + " has toggle '" + v + "' (expected \"on\" or \"off\"); read as on");
		return Boolean.TRUE;
	}

	private static JsonArray single(JsonElement e) {
		JsonArray a = new JsonArray();
		a.add(e);
		return a;
	}

	private static String tagline(JsonObject o) {
		return o.has("tagline") ? o.get("tagline").getAsString() : null;
	}

	public static boolean refreshUnsafe(MobEffect effect) {
		return effect.isInstantenous() || REFRESH_UNSAFE.contains(effect);
	}

	/** Upper bound: the most complex food in every position of the decay window. */
	private static double maxReachableDiversity() {
		try {
			Item best = null;
			double bestC = -1;
			for (Item item : ForgeRegistries.ITEMS) {
				if (!item.isEdible()) continue;
				double c = FoodList.getComplexity(new FoodInstance(item));
				if (c > bestC) {
					bestC = c;
					best = item;
				}
			}
			if (best == null) return 0;
			FoodInstance food = new FoodInstance(best);
			double sum = 0;
			int size = SOLSweetPotatoConfig.size();
			for (int k = 0; k < size; k++) sum += FoodList.calculateDiversityContribution(food, k);
			return sum;
		} catch (Throwable t) {
			return 0;
		}
	}

	private static JsonArray arr(JsonObject root, String key) {
		return root.has(key) ? root.getAsJsonArray(key) : new JsonArray();
	}

	private LadderLoader() {}
}
