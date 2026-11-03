package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CreativeWorldgen;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Biome modifier type {@code creativeengineering:tune_spawns}: the mob spawn tuning rules from
 * kubejs/generation_scripts/worldgen/mob_spawns.json, written as one modifier by
 * kubejs/server_scripts/worldgen/mob_spawns.js (feature_writeup/mob_spawns.md section 3).
 *
 * <p>Runs in {@link BiomeModifier.Phase#AFTER_EVERYTHING}, so it sees every mod's add_spawns / remove_spawns
 * (and mod-specific modifier types) first. The rules run in file order, per biome:
 * <ul>
 *   <li>{@code set}: the mob spawns at {@code weight} (and {@code min}/{@code max} when given). Where it already
 *       spawns, its entries collapse into one, in its first entry's category and list position; where it doesn't,
 *       one entry is appended to {@code category} (default: the mob's own category), group 1-1 unless given.
 *       Weight 0 removes it.</li>
 *   <li>{@code scale}: every existing entry's weight times {@code factor}, rounded, at least 1. Factor 0 removes
 *       it. Never adds.</li>
 *   <li>{@code remove}: every entry of the mob, in every category.</li>
 * </ul>
 * The mob is kept as an id and resolved when the rule runs, so an id of a mod that isn't loaded logs a warning
 * instead of stopping the world from loading.
 *
 * <p>Every biome a rule changed is recorded (spawners before the first rule, then one line per change) for
 * /dumpspawns; {@link #summary()} is the one-line load report mob_spawns.js logs. Biome modifiers apply at every
 * server start, so changes reach existing worlds too.
 *
 * <p>{@code brown_chance} (minecraft:mooshroom rules only) is not a biome change either: the biomes the rule matches
 * get that chance of a brown mooshroom, read by {@link com.SaintAlfonzo.creativeworldgen.mooshroom.MooshroomFamily}
 * on natural and chunk-generation spawns. When several rules give a biome a chance, the last one wins.
 *
 * <p>The optional {@code conditions} list (light_only, water_only) is not a biome change: it is handed to
 * {@link SpawnConditions}, which the spawn checks read.
 */
public record SpawnTuning(List<Rule> rules, List<SpawnConditions.Condition> conditions) implements BiomeModifier {

	private static final Logger LOGGER = LogUtils.getLogger();

	public enum Action {
		SET, SCALE, REMOVE;

		static final Codec<Action> CODEC = Codec.STRING.comapFlatMap(s -> {
			try {
				return DataResult.success(Action.valueOf(s.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException e) {
				return DataResult.error("tune_spawns: unknown action '" + s + "' (set, scale, remove)");
			}
		}, a -> a.name().toLowerCase(Locale.ROOT));
	}

	public record Rule(String id, HolderSet<Biome> biomes, ResourceLocation mob, Action action,
					   Optional<MobCategory> category, int weight, double factor,
					   Optional<Integer> min, Optional<Integer> max, Optional<Double> brownChance) {
		static final Codec<Rule> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("id").forGetter(Rule::id),
				Biome.LIST_CODEC.fieldOf("biomes").forGetter(Rule::biomes),
				ResourceLocation.CODEC.fieldOf("mob").forGetter(Rule::mob),
				Action.CODEC.fieldOf("action").forGetter(Rule::action),
				MobCategory.CODEC.optionalFieldOf("category").forGetter(Rule::category),
				Codec.INT.optionalFieldOf("weight", 0).forGetter(Rule::weight),
				Codec.DOUBLE.optionalFieldOf("factor", 1.0).forGetter(Rule::factor),
				Codec.INT.optionalFieldOf("min").forGetter(Rule::min),
				Codec.INT.optionalFieldOf("max").forGetter(Rule::max),
				Codec.DOUBLE.optionalFieldOf("brown_chance").forGetter(Rule::brownChance)
		).apply(i, Rule::new));
	}

	public static final Codec<SpawnTuning> CODEC = RecordCodecBuilder.create(i -> i.group(
			Rule.CODEC.listOf().optionalFieldOf("rules", List.of()).forGetter(SpawnTuning::rules),
			SpawnConditions.Condition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(SpawnTuning::conditions)
	).apply(i, SpawnTuning::new));

	/** Holder class, so headless tests can load SpawnTuning without Forge's registries. */
	private static final class Registration {
		static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
				DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, CreativeWorldgen.NS);
		static final RegistryObject<Codec<SpawnTuning>> TUNE_SPAWNS = SERIALIZERS.register("tune_spawns", () -> CODEC);
	}

	public static void register(IEventBus modBus) {
		Registration.SERIALIZERS.register(modBus);
	}

	// ── change record, read by /dumpspawns ───────────────────────────────────

	/** One spawner entry, as recorded. */
	public record Entry(String category, String mob, int weight, int min, int max) {}

	/** What the rules did to one biome: its spawners before the first rule ran, then one line per change. */
	public record BiomeRecord(List<Entry> before, List<String> changes) {}

	private static final Map<String, BiomeRecord> RECORDS = new ConcurrentHashMap<>();
	private static final Map<String, Integer> MATCHED = new ConcurrentHashMap<>();
	private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
	private static volatile List<String> ruleIds = List.of();
	private static final Map<String, Double> BROWN_CHANCE = new ConcurrentHashMap<>();
	private static final ResourceLocation MOOSHROOM = new ResourceLocation("minecraft", "mooshroom");

	/** Chance (0-1) that a naturally spawned mooshroom in this biome is brown; 0 when no rule gives one. */
	public static double brownChance(String biomeId) {
		return BROWN_CHANCE.getOrDefault(biomeId, 0.0);
	}

	/** Biome id -> record, for the biomes a rule changed, sorted by id. */
	public static Map<String, BiomeRecord> records() {
		return Collections.unmodifiableMap(new TreeMap<>(RECORDS));
	}

	/** Rule ids of the modifier that last ran, in file order. Empty when no tuning modifier ran. */
	public static List<String> ruleIds() {
		return ruleIds;
	}

	/** Rule id -> biomes it matched (changed or not). */
	public static Map<String, Integer> matched() {
		return Collections.unmodifiableMap(new TreeMap<>(MATCHED));
	}

	/** One line for the load log. */
	public static String summary() {
		List<String> ids = ruleIds;
		String conds = SpawnConditions.ids().isEmpty() ? "" : "; conditions: " + String.join(", ", SpawnConditions.ids());
		if (ids.isEmpty()) return conds.isEmpty()
				? "no tune_spawns modifier ran (no rules, switched off, or the world isn't loaded yet)"
				: "0 rules" + conds;
		int changes = 0;
		for (BiomeRecord r : RECORDS.values()) changes += r.changes().size();
		List<String> idle = new ArrayList<>();
		for (String id : ids) if (!MATCHED.containsKey(id)) idle.add(id);
		return ids.size() + " rules, " + RECORDS.size() + " biomes changed, " + changes + " changes"
				+ (idle.isEmpty() ? "" : "; rules that matched no biome: " + String.join(", ", idle)) + conds;
	}

	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		RECORDS.clear();
		MATCHED.clear();
		WARNED.clear();
		ruleIds = List.of();
		BROWN_CHANCE.clear();
		SpawnConditions.clear();
	}

	// ── the modifier ─────────────────────────────────────────────────────────

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase != Phase.AFTER_EVERYTHING) return;
		SpawnConditions.install(conditions);
		List<String> ids = new ArrayList<>(rules.size());
		for (Rule r : rules) ids.add(r.id());
		ruleIds = List.copyOf(ids);

		String biomeId = biome.unwrapKey().map(k -> k.location().toString()).orElse("?");
		MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
		List<Entry> before = null;
		List<String> changes = new ArrayList<>();
		for (Rule rule : rules) {
			if (!rule.biomes().contains(biome)) continue;
			MATCHED.merge(rule.id(), 1, Integer::sum);
			if (rule.brownChance().isPresent()) {
				if (MOOSHROOM.equals(rule.mob())) BROWN_CHANCE.put(biomeId, Math.max(0.0, Math.min(1.0, rule.brownChance().get())));
				else if (WARNED.add(rule.id() + "#brown")) LOGGER.warn("[mob-spawns] rule {}: brown_chance only works on minecraft:mooshroom, ignored", rule.id());
			}
			EntityType<?> type = resolve(rule);
			if (type == null) continue;
			if (before == null) before = snapshot(spawns);
			String change = apply(spawns, rule, type);
			if (change != null) changes.add(rule.id() + ": " + change);
		}
		if (!changes.isEmpty()) RECORDS.put(biomeId, new BiomeRecord(before, List.copyOf(changes)));
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return Registration.TUNE_SPAWNS.get();
	}

	/** One rule on one biome's spawners; returns the change line, or null when nothing changed. */
	static String apply(MobSpawnSettingsBuilder spawns, Rule rule, EntityType<?> type) {
		return switch (rule.action()) {
			case SET -> set(spawns, rule, type);
			case SCALE -> scale(spawns, rule, type);
			case REMOVE -> remove(spawns, type);
		};
	}

	// The vanilla registry (Forge registers modded entity types into it too): works in headless tests.
	private static EntityType<?> resolve(Rule rule) {
		Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOptional(rule.mob());
		if (type.isPresent()) return type.get();
		if (WARNED.add(rule.id())) LOGGER.warn("[mob-spawns] rule {}: no entity type {}, skipped", rule.id(), rule.mob());
		return null;
	}

	private static String name(EntityType<?> type) {
		return Registry.ENTITY_TYPE.getKey(type).toString();
	}

	static List<Entry> snapshot(MobSpawnSettingsBuilder spawns) {
		List<Entry> out = new ArrayList<>();
		for (MobCategory cat : MobCategory.values()) {
			for (MobSpawnSettings.SpawnerData d : spawns.getSpawner(cat)) {
				out.add(new Entry(cat.getName(), name(d.type), d.getWeight().asInt(), d.minCount, d.maxCount));
			}
		}
		return out;
	}

	private static String describe(MobSpawnSettings.SpawnerData d) {
		return d.getWeight().asInt() + " (" + d.minCount + "-" + d.maxCount + ")";
	}

	private static String set(MobSpawnSettingsBuilder spawns, Rule rule, EntityType<?> type) {
		if (rule.weight() <= 0) return remove(spawns, type);
		MobCategory home = null;
		int at = -1;
		MobSpawnSettings.SpawnerData first = null;
		List<String> old = new ArrayList<>();
		for (MobCategory cat : MobCategory.values()) {
			List<MobSpawnSettings.SpawnerData> list = spawns.getSpawner(cat);
			for (int i = 0; i < list.size(); i++) {
				MobSpawnSettings.SpawnerData d = list.get(i);
				if (d.type != type) continue;
				if (first == null) { first = d; home = cat; at = i; }
				old.add(describe(d));
			}
		}
		int min = rule.min().orElse(first != null ? first.minCount : 1);
		int max = Math.max(min, rule.max().orElse(first != null ? first.maxCount : min));
		MobSpawnSettings.SpawnerData data = new MobSpawnSettings.SpawnerData(type, rule.weight(), min, max);
		if (first == null) {
			MobCategory cat = rule.category().orElse(type.getCategory());
			spawns.getSpawner(cat).add(data);
			return "added " + describe(data) + " to " + cat.getName();
		}
		for (MobCategory cat : MobCategory.values()) spawns.getSpawner(cat).removeIf(d -> d.type == type);
		List<MobSpawnSettings.SpawnerData> list = spawns.getSpawner(home);
		list.add(Math.min(at, list.size()), data);
		String now = describe(data);
		if (old.size() == 1 && old.get(0).equals(now)) return null;
		return "set " + String.join(" + ", old) + " -> " + now;
	}

	private static String scale(MobSpawnSettingsBuilder spawns, Rule rule, EntityType<?> type) {
		if (rule.factor() <= 0) return remove(spawns, type);
		List<String> parts = new ArrayList<>();
		for (MobCategory cat : MobCategory.values()) {
			List<MobSpawnSettings.SpawnerData> list = spawns.getSpawner(cat);
			for (int i = 0; i < list.size(); i++) {
				MobSpawnSettings.SpawnerData d = list.get(i);
				if (d.type != type) continue;
				int w = d.getWeight().asInt();
				int nw = Math.max(1, (int) Math.round(w * rule.factor()));
				if (nw == w) continue;
				list.set(i, new MobSpawnSettings.SpawnerData(type, nw, d.minCount, d.maxCount));
				parts.add(w + " -> " + nw);
			}
		}
		return parts.isEmpty() ? null : "scaled x" + rule.factor() + ": " + String.join(", ", parts);
	}

	private static String remove(MobSpawnSettingsBuilder spawns, EntityType<?> type) {
		Map<String, List<String>> gone = new LinkedHashMap<>();
		for (MobCategory cat : MobCategory.values()) {
			for (MobSpawnSettings.SpawnerData d : spawns.getSpawner(cat)) {
				if (d.type == type) gone.computeIfAbsent(cat.getName(), k -> new ArrayList<>()).add(describe(d));
			}
			spawns.getSpawner(cat).removeIf(d -> d.type == type);
		}
		if (gone.isEmpty()) return null;
		List<String> parts = new ArrayList<>();
		gone.forEach((cat, ws) -> parts.add(cat + " " + String.join(" + ", ws)));
		return "removed " + String.join(", ", parts);
	}
}
