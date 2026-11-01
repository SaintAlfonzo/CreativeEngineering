package com.SaintAlfonzo.creativedevtools.dump;

import com.SaintAlfonzo.creativeengineering.worldgen.SpawnConditions;
import com.SaintAlfonzo.creativeengineering.worldgen.SpawnTuning;
import com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * /dumpspawns (kubejs/server_scripts/_lib/dump_spawns.js): every biome's mob spawners as the running game has
 * them, after every biome modifier (the mods', then the pack's tune_spawns), plus each mob's spawn placement.
 * Writes kubejs/exported/spawns.json, read by tools/gen_spawn_table.py (feature_writeup/mob_spawns.md).
 *
 * <p>Java rather than KubeJS: Rhino resolves {@code WeightedRandomList.unwrap()} to its own wrapper method, so a
 * script reads every spawn list as empty.
 *
 * <p>Spawn placement predicates are code. The dump names the class that registered each one (vanilla:
 * {@code SpawnPlacements}; {@code SpawnPlacementRegisterEvent} when a mod changed it through Forge's event), the
 * mob's class chain, any {@code checkSpawnRules}/{@code checkSpawnObstruction} override, and static methods on the
 * mob's own classes that look like spawn rules (mod classes only: vanilla method names are obfuscated at runtime;
 * the generator names vanilla's rules from tools/spawn_rules_vanilla.json instead).
 */
public final class SpawnDump {

	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String OUT = "kubejs/exported/spawns.json";

	private SpawnDump() {}

	/** Writes {@link #OUT}; returns a one-line summary for chat. */
	public static String write(MinecraftServer server) throws Exception {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("generated", Instant.now().toString());
		root.put("categories", categories());
		Map<String, Object> biomes = biomes(server);
		root.put("biomes", biomes);
		Map<String, Object> entities = entities(server, biomes);
		root.put("entities", entities);
		root.put("tuning", tuning());
		String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root) + "\n";
		WorldgenFiles.writeText(OUT, json);
		String line = biomes.size() + " biomes, " + entities.size() + " mobs -> " + OUT;
		LOGGER.info("[dumpspawns] {}", line);
		return line;
	}

	private static Map<String, Object> categories() {
		Map<String, Object> out = new LinkedHashMap<>();
		for (MobCategory c : MobCategory.values()) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("max_per_chunk", c.getMaxInstancesPerChunk());
			m.put("friendly", c.isFriendly());
			m.put("persistent", c.isPersistent());
			m.put("despawn_distance", c.getDespawnDistance());
			m.put("no_despawn_distance", c.getNoDespawnDistance());
			out.put(c.getName(), m);
		}
		return out;
	}

	private static Map<String, Object> biomes(MinecraftServer server) {
		Registry<Biome> reg = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		Map<String, Set<String>> dims = new TreeMap<>();
		for (ServerLevel level : server.getAllLevels()) {
			String dim = level.dimension().location().toString();
			try {
				for (Holder<Biome> h : level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
					h.unwrapKey().ifPresent(k -> dims.computeIfAbsent(k.location().toString(), x -> new TreeSet<>()).add(dim));
				}
			} catch (RuntimeException e) {
				LOGGER.warn("[dumpspawns] no biome source for {}: {}", dim, e.toString());
			}
		}
		Map<String, Object> out = new TreeMap<>();
		for (ResourceKey<Biome> key : reg.registryKeySet()) {
			Holder<Biome> holder = reg.getHolderOrThrow(key);
			MobSpawnSettings ms = holder.value().getMobSettings();
			Map<String, Object> b = new LinkedHashMap<>();
			String id = key.location().toString();
			b.put("dimensions", new ArrayList<>(dims.getOrDefault(id, Set.of())));
			b.put("creature_probability", ms.getCreatureProbability());
			Map<String, Object> spawners = new LinkedHashMap<>();
			for (MobCategory c : MobCategory.values()) {
				List<Object> list = new ArrayList<>();
				for (MobSpawnSettings.SpawnerData d : ms.getMobs(c).unwrap()) {
					Map<String, Object> e = new LinkedHashMap<>();
					e.put("type", entityId(d.type));
					e.put("weight", d.getWeight().asInt());
					e.put("min", d.minCount);
					e.put("max", d.maxCount);
					list.add(e);
				}
				if (!list.isEmpty()) spawners.put(c.getName(), list);
			}
			b.put("spawners", spawners);
			Map<String, Object> costs = new TreeMap<>();
			for (EntityType<?> t : ms.getEntityTypes()) {
				MobSpawnSettings.MobSpawnCost cost = ms.getMobSpawnCost(t);
				if (cost == null) continue;
				Map<String, Object> c = new LinkedHashMap<>();
				c.put("energy_budget", cost.getEnergyBudget());
				c.put("charge", cost.getCharge());
				costs.put(entityId(t), c);
			}
			if (!costs.isEmpty()) b.put("costs", costs);
			List<String> tags = new ArrayList<>();
			holder.tags().forEach(t -> tags.add(t.location().toString()));
			tags.sort(null);
			b.put("tags", tags);
			out.put(id, b);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> entities(MinecraftServer server, Map<String, Object> biomes) {
		// how many biomes list each mob, and in which categories
		Map<String, Integer> spawnsIn = new TreeMap<>();
		Map<String, Set<String>> spawnCats = new TreeMap<>();
		for (Object bo : biomes.values()) {
			Map<String, Object> spawners = (Map<String, Object>) ((Map<String, Object>) bo).get("spawners");
			Set<String> seen = new TreeSet<>();
			spawners.forEach((cat, list) -> {
				for (Object eo : (List<Object>) list) {
					String t = (String) ((Map<String, Object>) eo).get("type");
					spawnCats.computeIfAbsent(t, x -> new TreeSet<>()).add(cat);
					seen.add(t);
				}
			});
			seen.forEach(t -> spawnsIn.merge(t, 1, Integer::sum));
		}

		Map<EntityType<?>, String> eggs = new LinkedHashMap<>();
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (!(item instanceof SpawnEggItem egg)) continue;
			try {
				EntityType<?> t = egg.getType(null);
				if (t != null) eggs.putIfAbsent(t, String.valueOf(ForgeRegistries.ITEMS.getKey(item)));
			} catch (Throwable e) {
				LOGGER.debug("[dumpspawns] spawn egg {}: {}", ForgeRegistries.ITEMS.getKey(item), e.toString());
			}
		}

		Map<EntityType<?>, Object> placements = placementData();
		ServerLevel level = server.overworld();
		Method rulesBase = findMobMethod("m_5545_", LevelAccessor.class, MobSpawnType.class);
		Method obstructionBase = findMobMethod("m_6914_", LevelReader.class);

		Map<String, Object> out = new TreeMap<>();
		for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
			String id = entityId(type);
			boolean natural = spawnsIn.containsKey(id);
			Object data = placements.get(type);
			boolean egg = eggs.containsKey(type);
			if (!natural && data == null && !egg) continue;
			Map<String, Object> e = new LinkedHashMap<>();
			e.put("category", type.getCategory().getName());
			e.put("spawns_in_biomes", spawnsIn.getOrDefault(id, 0));
			if (spawnCats.containsKey(id)) e.put("spawner_categories", new ArrayList<>(spawnCats.get(id)));
			e.put("spawn_egg", eggs.get(type));
			if (data != null) {
				e.put("placement", String.valueOf(SpawnPlacements.getPlacementType(type)));
				e.put("heightmap", String.valueOf(SpawnPlacements.getHeightmapType(type)));
				e.put("predicate_source", predicateSource(data));
			}
			describeClass(type, level, rulesBase, obstructionBase, e);
			out.put(id, e);
		}
		return out;
	}

	/** SpawnPlacements.DATA_BY_TYPE, by reflection (SRG names; ObfuscationReflectionHelper maps them in dev). */
	@SuppressWarnings("unchecked")
	private static Map<EntityType<?>, Object> placementData() {
		try {
			Field f = ObfuscationReflectionHelper.findField(SpawnPlacements.class, "f_21750_");
			return new LinkedHashMap<>((Map<EntityType<?>, Object>) f.get(null));
		} catch (Throwable e) {
			LOGGER.warn("[dumpspawns] could not read SpawnPlacements data: {}", e.toString());
			return Map.of();
		}
	}

	/** The class that created the placement predicate: the part of the lambda class name before $$Lambda. */
	private static String predicateSource(Object data) {
		try {
			Field f = ObfuscationReflectionHelper.findField(data.getClass(), "f_21769_");
			Object p = f.get(data);
			if (p == null) return null;
			String n = p.getClass().getName();
			int cut = n.indexOf("$$Lambda");
			return cut >= 0 ? n.substring(0, cut) : n;
		} catch (Throwable e) {
			return "? (" + e.getClass().getSimpleName() + ")";
		}
	}

	private static Method findMobMethod(String srg, Class<?>... params) {
		try {
			return ObfuscationReflectionHelper.findMethod(Mob.class, srg, params);
		} catch (Throwable e) {
			LOGGER.warn("[dumpspawns] Mob.{} not found: {}", srg, e.toString());
			return null;
		}
	}

	/**
	 * The mob's class chain and spawn-rule hooks. Needs an instance (EntityType doesn't keep its class), so the
	 * mob is created in the overworld and never added to it.
	 */
	private static void describeClass(EntityType<?> type, ServerLevel level, Method rulesBase, Method obstructionBase,
									  Map<String, Object> e) {
		Entity ent;
		try {
			ent = type.create(level);
		} catch (Throwable t) {
			e.put("class_error", t.getClass().getSimpleName() + ": " + t.getMessage());
			return;
		}
		if (ent == null) return;
		Class<?> cls = ent.getClass();
		e.put("class", cls.getName());
		List<String> chain = new ArrayList<>();
		for (Class<?> c = cls.getSuperclass(); c != null && c != Entity.class && c != Object.class; c = c.getSuperclass()) {
			chain.add(c.getName());
		}
		e.put("superclasses", chain);
		if (ent instanceof Mob) {
			e.put("spawn_rules_override", overrideOwner(cls, rulesBase));
			e.put("obstruction_override", overrideOwner(cls, obstructionBase));
		}
		List<String> rules = new ArrayList<>();
		for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
			if (c.getName().startsWith("net.minecraft.")) break;
			for (Method m : c.getDeclaredMethods()) {
				if (looksLikeSpawnRule(m)) rules.add(c.getName() + "." + m.getName());
			}
		}
		rules.sort(null);
		if (!rules.isEmpty()) e.put("rule_methods", rules);
		ent.discard();
	}

	/** The class that declares the method the mob actually runs, when that isn't Mob itself; null otherwise. */
	private static String overrideOwner(Class<?> cls, Method base) {
		if (base == null) return null;
		try {
			Class<?> owner = cls.getMethod(base.getName(), base.getParameterTypes()).getDeclaringClass();
			return owner == Mob.class ? null : owner.getName();
		} catch (Throwable t) {
			return null;
		}
	}

	/** static boolean x(EntityType, <level>, MobSpawnType, BlockPos, RandomSource), the SpawnPredicate shape. */
	private static boolean looksLikeSpawnRule(Method m) {
		if (!Modifier.isStatic(m.getModifiers()) || m.getReturnType() != boolean.class) return false;
		Class<?>[] p = m.getParameterTypes();
		return p.length == 5 && p[0] == EntityType.class && p[2] == MobSpawnType.class;
	}

	private static Map<String, Object> tuning() {
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("rules", SpawnTuning.ruleIds());
		out.put("matched", SpawnTuning.matched());
		out.put("conditions", SpawnConditions.ids());
		Map<String, Object> biomes = new TreeMap<>();
		SpawnTuning.records().forEach((biome, rec) -> {
			Map<String, Object> r = new LinkedHashMap<>();
			List<Object> before = new ArrayList<>();
			for (SpawnTuning.Entry en : rec.before()) {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("category", en.category());
				m.put("type", en.mob());
				m.put("weight", en.weight());
				m.put("min", en.min());
				m.put("max", en.max());
				before.add(m);
			}
			r.put("before", before);
			r.put("changes", rec.changes());
			biomes.put(biome, r);
		});
		out.put("biomes", biomes);
		return out;
	}

	private static String entityId(EntityType<?> t) {
		ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(t);
		return id == null ? "?" : id.toString();
	}
}
