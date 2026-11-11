package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.SaintAlfonzo.creativecommon.util.WorldgenFiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Spawn boxes from kubejs/generation_scripts/worldgen/spawn_boxes.json: vanilla-style structure spawn overrides
 * (the pillager outpost's), given to structures by id or tag, with named mob pools. Read at every world load.
 *
 * <p>Hooked at the head of ChunkGenerator#getMobsAt (ChunkGeneratorSpawnBoxesMixin), which every natural spawn
 * attempt and every later mob of its group asks: for a position inside a boxed structure, its pool replaces the
 * biome's list (and the structure's own spawn_overrides). Nothing found: vanilla's lookup runs as before.
 *
 * <p>META STRUCTURES: a meta is one structure whose own spawn_overrides cover all its parts alike, so on its own a
 * part inside a meta lost its box and its spawn_overrides (the game looks both up by the start's structure). Here each
 * part (the anchor too) answers for its own pieces: a part with a box in this file uses the meta's pool when the meta
 * has a box (camps inside a camp meta spawn the camp-meta mix), else its own; for any other category, and for a part
 * without a box, the part's own spawn_overrides apply (a monument's guardians, a witch hut's cats, Philip's Ruins'
 * zombies). The box or override type applies to the part's own pieces ("full" = the box around the part's pieces, not
 * the whole meta). This part lookup runs with the box switch off too (overrides only); outside every part, vanilla's
 * lookup runs (the meta's own spawn_overrides, normally none).
 *
 * <p>A box can require a piece (requires_piece, template ids): Unwrecked Ships' ship is boxed only when it carries a
 * pillager crew. no_drops strips items from box mobs' drops (the evoker's totem: a box respawns forever); box mobs
 * are marked at their natural spawn when they stand in a box (LivingSpawnEvent.SpecialSpawn, persistent data).
 *
 * <p>Switch: CWGConfig [worldgen.systems] spawnBoxes. Overrides are read when mobs spawn, so boxes reach old chunks too.
 * Feature_writeup/structure_tuning.md "Spawners and spawn boxes".
 */
public final class StructureSpawnBoxes {

	private static final Logger LOG = LogManager.getLogger("creativeworldgen/spawn-boxes");
	static final String CONFIG = BiomeLayoutTuner.WORLDGEN_DIR + "spawn_boxes.json";
	static final String BOX_MOB = "creativeengineering:box_spawn";

	/** One box entry as written: which structures, which pool, where in them. */
	record Box(String id, List<String> structures, List<String> exclude, MobCategory category, boolean piece,
			WeightedRandomList<MobSpawnSettings.SpawnerData> spawns, Set<ResourceLocation> requiresPiece) {}

	record Config(List<Box> boxes, Map<EntityType<?>, Set<Item>> noDrops) {
		static final Config EMPTY = new Config(List.of(), Map.of());
	}

	private static volatile Config config = Config.EMPTY;
	private static final Map<Registry<Structure>, Map<Structure, Box>> RESOLVED = Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<StructureStart, Boolean> REQUIRED_SEEN = Collections.synchronizedMap(new WeakHashMap<>());

	private StructureSpawnBoxes() {}

	// ── config ───────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		load();
	}

	static void load() {
		RESOLVED.clear();
		REQUIRED_SEEN.clear();
		try {
			String text = WorldgenFiles.readText(CONFIG);
			config = text == null ? Config.EMPTY : parse(PoolTuner.parseLenient(text));
		} catch (Exception e) {
			LOG.error("[spawn-boxes] could not read {}, no spawn boxes this session", CONFIG, e);
			config = Config.EMPTY;
		}
		LOG.info("[spawn-boxes] {} box(es){}", config.boxes().size(), WorldgenConfig.spawnBoxesEnabled() ? "" : " (off in CWGConfig [worldgen.systems])");
	}

	static Config parse(JsonObject doc) {
		Map<String, WeightedRandomList<MobSpawnSettings.SpawnerData>> pools = new LinkedHashMap<>();
		JsonObject poolsJson = doc.has("pools") ? doc.getAsJsonObject("pools") : new JsonObject();
		for (Map.Entry<String, JsonElement> e : poolsJson.entrySet()) {
			if (e.getKey().startsWith("_")) continue;
			List<MobSpawnSettings.SpawnerData> list = new ArrayList<>();
			for (JsonElement el : e.getValue().getAsJsonArray()) {
				JsonObject o = el.getAsJsonObject();
				ResourceLocation id = new ResourceLocation(o.get("type").getAsString());
				var type = Registry.ENTITY_TYPE.getOptional(id);
				if (type.isEmpty()) {
					LOG.warn("[spawn-boxes] pool {}: no entity type {}, skipped", e.getKey(), id);
					continue;
				}
				int min = o.has("minCount") ? o.get("minCount").getAsInt() : 1;
				int max = Math.max(min, o.has("maxCount") ? o.get("maxCount").getAsInt() : min);
				list.add(new MobSpawnSettings.SpawnerData(type.get(), o.get("weight").getAsInt(), min, max));
			}
			pools.put(e.getKey(), WeightedRandomList.create(list));
		}
		List<Box> boxes = new ArrayList<>();
		for (JsonElement el : doc.has("boxes") ? doc.getAsJsonArray("boxes") : new JsonArray()) {
			JsonObject o = el.getAsJsonObject();
			String id = o.get("id").getAsString();
			WeightedRandomList<MobSpawnSettings.SpawnerData> spawns = pools.get(o.get("pool").getAsString());
			if (spawns == null) {
				LOG.warn("[spawn-boxes] box {}: no pool {}, skipped", id, o.get("pool").getAsString());
				continue;
			}
			MobCategory cat = MobCategory.byName(o.has("category") ? o.get("category").getAsString() : "monster");
			String type = o.has("bounding_box") ? o.get("bounding_box").getAsString() : "piece";
			Set<ResourceLocation> required = new java.util.HashSet<>();
			for (String s : strings(o.get("requires_piece"))) required.add(new ResourceLocation(s));
			boxes.add(new Box(id, strings(o.get("structures")), strings(o.get("exclude")), cat, !"full".equals(type), spawns, Set.copyOf(required)));
		}
		Map<EntityType<?>, Set<Item>> noDrops = new IdentityHashMap<>();
		JsonObject nd = doc.has("no_drops") ? doc.getAsJsonObject("no_drops") : new JsonObject();
		for (Map.Entry<String, JsonElement> e : nd.entrySet()) {
			if (e.getKey().startsWith("_")) continue;
			var type = Registry.ENTITY_TYPE.getOptional(new ResourceLocation(e.getKey()));
			if (type.isEmpty()) continue;
			Set<Item> items = Collections.newSetFromMap(new IdentityHashMap<>());
			for (String s : strings(e.getValue())) {
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
				if (item != null) items.add(item);
			}
			noDrops.put(type.get(), items);
		}
		return new Config(List.copyOf(boxes), noDrops);
	}

	private static List<String> strings(JsonElement el) {
		List<String> out = new ArrayList<>();
		if (el == null) return out;
		if (el.isJsonArray()) for (JsonElement e : el.getAsJsonArray()) out.add(e.getAsString());
		else out.add(el.getAsString());
		return out;
	}

	/** Structure -> its box (the first box that names it), per structure registry (one per world load). */
	private static Map<Structure, Box> resolved(RegistryAccess access) {
		Registry<Structure> reg = access.registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Map<Structure, Box> cached = RESOLVED.get(reg);
		if (cached != null) return cached;
		Map<Structure, Box> out = new IdentityHashMap<>();
		int named = 0;
		for (Box box : config.boxes()) {
			Set<Structure> excluded = matching(reg, box.exclude());
			for (Structure s : matching(reg, box.structures())) {
				if (excluded.contains(s)) continue;
				Box was = out.putIfAbsent(s, box);
				if (was != null) LOG.warn("[spawn-boxes] {} is in box {} and in box {}; the first wins", reg.getKey(s), was.id(), box.id());
				else named++;
			}
		}
		LOG.info("[spawn-boxes] {} structure(s) boxed", named);
		RESOLVED.put(reg, out);
		return out;
	}

	/** "#tag", "namespace:*" or an id: the structures it names (unknown ones are skipped quietly: an absent mod). */
	private static Set<Structure> matching(Registry<Structure> reg, List<String> entries) {
		Set<Structure> out = Collections.newSetFromMap(new IdentityHashMap<>());
		for (String entry : entries) {
			if (entry.startsWith("#")) {
				for (Holder<Structure> h : reg.getTagOrEmpty(TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(entry.substring(1))))) out.add(h.value());
			} else if (entry.endsWith(":*")) {
				String ns = entry.substring(0, entry.length() - 2);
				for (Map.Entry<net.minecraft.resources.ResourceKey<Structure>, Structure> e : reg.entrySet()) {
					if (e.getKey().location().getNamespace().equals(ns)) out.add(e.getValue());
				}
			} else {
				Structure s = reg.get(new ResourceLocation(entry));
				if (s != null) out.add(s);
			}
		}
		return out;
	}

	// ── the lookup ───────────────────────────────────────────────────────────

	/** What answered a spawn lookup: the pool, the box id (or "spawn_overrides" of a meta part), the structure and the meta part (null: not in a meta). */
	public record Hit(WeightedRandomList<MobSpawnSettings.SpawnerData> spawns, String source, Structure structure, Structure part) {}

	/** ChunkGenerator#getMobsAt HEAD: the box pool at pos, or null to let vanilla's lookup run. */
	public static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(StructureManager structures, MobCategory category, BlockPos pos) {
		Hit hit = lookup(structures, category, pos);
		return hit == null ? null : hit.spawns();
	}

	/** The box (or meta part override) answering at pos, or null when vanilla's lookup runs. Also used by /cwgspawnbox. */
	public static Hit lookup(StructureManager structures, MobCategory category, BlockPos pos) {
		Config cfg = config;
		boolean boxed = !cfg.boxes().isEmpty() && WorldgenConfig.spawnBoxesEnabled();
		Map<Structure, LongSet> all = structures.getAllStructuresAt(pos);
		if (all.isEmpty()) return null;
		Map<Structure, Box> boxes = boxed ? resolved(structures.registryAccess()) : Map.of();
		for (Map.Entry<Structure, LongSet> e : all.entrySet()) {
			Structure s = e.getKey();
			boolean meta = s instanceof MetaStructure;
			Box own = boxes.get(s);
			if (!meta && (own == null || own.category() != category)) continue;
			List<Hit> found = new ArrayList<>(1);
			structures.fillStartsForStructure(s, e.getValue(), start -> {
				if (!found.isEmpty() || !start.getBoundingBox().isInside(pos)) return;
				Hit r = meta ? inMeta(start, own, boxes, category, pos)
						: covers(own.piece(), start.getPieces(), pos) && hasRequired(own, start, start.getPieces()) ? new Hit(own.spawns(), own.id(), s, null) : null;
				if (r != null) found.add(r);
			});
			if (!found.isEmpty()) return found.get(0);
		}
		return null;
	}

	/** The part of a meta start whose pieces hold pos: its box (with the meta's pool if the meta has one) or its own overrides (null: none answers). */
	private static Hit inMeta(StructureStart start, Box metaBox, Map<Structure, Box> boxes, MobCategory category, BlockPos pos) {
		List<MetaStructure.PartPieces> parts = MetaStructure.parts(start);
		if (parts == null) return null;
		for (MetaStructure.PartPieces part : parts) {
			Structure member = part.structure().value();
			Box own = boxes.get(member);
			Box pool = own == null ? null : metaBox != null ? metaBox : own;
			if (pool != null && pool.category() == category) {
				if (covers(own.piece(), part.pieces(), pos) && hasRequired(own, null, part.pieces())) {
					return new Hit(pool.spawns(), pool == own ? own.id() : metaBox.id() + " (part box " + own.id() + ")", start.getStructure(), member);
				}
			} else {
				StructureSpawnOverride o = member.spawnOverrides().get(category);
				if (o != null && covers(o.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE, part.pieces(), pos)) {
					return new Hit(o.spawns(), "spawn_overrides", start.getStructure(), member);
				}
			}
		}
		return null;
	}

	// ── spawn height in a boxed column ───────────────────────────────────────

	/**
	 * NaturalSpawner#getRandomPosWithin RETURN (NaturalSpawnerBoxHeightMixin): vanilla picks the attempt's height
	 * uniformly from the world bottom to the column top, so over a 22-high tower nearly every attempt lands in the
	 * ground or caves below it and the box pool is almost never asked on the tower's floors. In a column that a box of
	 * this category covers, half the attempts instead take a height between the box's bottom and the column top. The
	 * other half, every column without a box and every category without one keep vanilla's height. Only the attempt's
	 * start height moves: caps, light and placement rules, the 24-block player rule and the box lookup run as before.
	 */
	public static BlockPos boxHeight(ServerLevel level, MobCategory category, net.minecraft.world.level.chunk.LevelChunk chunk, BlockPos pos) {
		Config cfg = config;
		if (cfg.boxes().isEmpty() || !hasCategory(cfg, category) || chunk.getAllReferences().isEmpty()
				|| !WorldgenConfig.spawnBoxesEnabled() || level.random.nextBoolean()) return pos;
		StructureManager sm = level.structureManager();
		Map<Structure, Box> boxes = resolved(sm.registryAccess());
		int x = pos.getX(), z = pos.getZ();
		int[] span = null;
		for (Map.Entry<Structure, LongSet> e : sm.getAllStructuresAt(pos).entrySet()) {
			Structure s = e.getKey();
			boolean meta = s instanceof MetaStructure;
			Box own = boxes.get(s);
			if (!meta && (own == null || own.category() != category)) continue;
			int[][] found = new int[1][];
			sm.fillStartsForStructure(s, e.getValue(), start -> {
				if (found[0] != null || !inColumn(start.getBoundingBox(), x, z)) return;
				found[0] = meta ? metaSpan(start, own, boxes, category, x, z)
						: hasRequired(own, start, start.getPieces()) ? span(own.piece(), start.getPieces(), x, z) : null;
			});
			if (found[0] != null) {
				span = found[0];
				break;
			}
		}
		if (span == null) return pos;
		int lo = Math.max(span[0], level.getMinBuildHeight() + 1);
		int hi = Math.min(span[1], chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z) + 1);
		if (lo > hi) return pos;
		return new BlockPos(x, net.minecraft.util.Mth.randomBetweenInclusive(level.random, lo, hi), z);
	}

	private static boolean hasCategory(Config cfg, MobCategory category) {
		for (Box b : cfg.boxes()) if (b.category() == category) return true;
		return false;
	}

	private static boolean inColumn(BoundingBox box, int x, int z) {
		return x >= box.minX() && x <= box.maxX() && z >= box.minZ() && z <= box.maxZ();
	}

	/** The box part of a meta start whose pieces cover the column (same choice of pool as inMeta): its y span, or null. */
	private static int[] metaSpan(StructureStart start, Box metaBox, Map<Structure, Box> boxes, MobCategory category, int x, int z) {
		List<MetaStructure.PartPieces> parts = MetaStructure.parts(start);
		if (parts == null) return null;
		for (MetaStructure.PartPieces part : parts) {
			Box own = boxes.get(part.structure().value());
			Box pool = own == null ? null : metaBox != null ? metaBox : own;
			if (pool == null || pool.category() != category || !hasRequired(own, null, part.pieces())) continue;
			int[] span = span(own.piece(), part.pieces(), x, z);
			if (span != null) return span;
		}
		return null;
	}

	/** The y span of the box over the column: "piece" = the pieces whose boxes cover it, "full" = the box around all pieces. */
	private static int[] span(boolean piece, List<StructurePiece> pieces, int x, int z) {
		if (pieces.isEmpty()) return null;
		if (!piece) {
			BoundingBox box = StructurePiece.createBoundingBox(pieces.stream());
			return inColumn(box, x, z) ? new int[] {box.minY(), box.maxY()} : null;
		}
		int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
		for (StructurePiece p : pieces) {
			BoundingBox b = p.getBoundingBox();
			if (!inColumn(b, x, z)) continue;
			lo = Math.min(lo, b.minY());
			hi = Math.max(hi, b.maxY());
		}
		return lo > hi ? null : new int[] {lo, hi};
	}

	// ── /cwgspawnbox: what answers a natural spawn here (scratch tests; feature_writeup/structure_tuning.md) ──

	@SubscribeEvent
	public static void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
		event.getDispatcher().register(net.minecraft.commands.Commands.literal("cwgspawnbox").requires(src -> src.hasPermission(2))
				.executes(ctx -> probe(ctx.getSource(), new BlockPos(ctx.getSource().getPosition()), MobCategory.MONSTER))
				.then(net.minecraft.commands.Commands.argument("pos", net.minecraft.commands.arguments.coordinates.BlockPosArgument.blockPos())
						.executes(ctx -> probe(ctx.getSource(), net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(ctx, "pos"), MobCategory.MONSTER))
						.then(net.minecraft.commands.Commands.argument("category", com.mojang.brigadier.arguments.StringArgumentType.word())
								.executes(ctx -> {
									String name = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "category");
									MobCategory cat = MobCategory.byName(name);
									if (cat == null) {
										ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("[spawn-boxes] no mob category " + name));
										return 0;
									}
									return probe(ctx.getSource(), net.minecraft.commands.arguments.coordinates.BlockPosArgument.getLoadedBlockPos(ctx, "pos"), cat);
								}))));
	}

	/**
	 * One line to chat and the log: the box (or part override) that answers a natural spawn of the category at pos with
	 * its pool, or "vanilla" and the list vanilla would use (the structures' own spawn_overrides, else the biome's).
	 */
	static int probe(net.minecraft.commands.CommandSourceStack source, BlockPos pos, MobCategory category) {
		ServerLevel level = source.getLevel();
		StructureManager sm = level.structureManager();
		Registry<Structure> reg = sm.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
		Hit hit = lookup(sm, category, pos);
		String where = "[spawn-boxes] " + category.getName() + " at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + ": ";
		String line;
		if (hit != null) {
			line = where + "box " + hit.source() + " of " + reg.getKey(hit.structure())
					+ (hit.part() != null ? ", part " + reg.getKey(hit.part()) : "") + " -> " + poolText(hit.spawns());
		} else {
			List<String> here = new ArrayList<>();
			for (Structure s : sm.getAllStructuresAt(pos).keySet()) here.add(String.valueOf(reg.getKey(s)));
			WeightedRandomList<MobSpawnSettings.SpawnerData> vanilla = level.getChunkSource().getGenerator()
					.getMobsAt(level.getBiome(pos), sm, category, pos);
			line = where + "no box (" + (WorldgenConfig.spawnBoxesEnabled() ? "" : "switch off, ")
					+ "structures referenced here: " + (here.isEmpty() ? "none" : String.join(", ", here)) + ") -> vanilla " + poolText(vanilla);
		}
		LOG.info(line);
		source.sendSuccess(net.minecraft.network.chat.Component.literal(line), false);
		return hit != null ? 1 : 0;
	}

	private static String poolText(WeightedRandomList<MobSpawnSettings.SpawnerData> pool) {
		List<String> out = new ArrayList<>();
		for (MobSpawnSettings.SpawnerData d : pool.unwrap()) {
			out.add(Registry.ENTITY_TYPE.getKey(d.type) + " " + d.getWeight().asInt() + " (" + d.minCount + "-" + d.maxCount + ")");
		}
		return out.isEmpty() ? "[]" : "[" + String.join(", ", out) + "]";
	}

	/** "piece": a piece's box holds pos; "full": the box around all the pieces does. */
	private static boolean covers(boolean piece, List<StructurePiece> pieces, BlockPos pos) {
		if (pieces.isEmpty()) return false;
		if (piece) {
			for (StructurePiece p : pieces) if (p.getBoundingBox().isInside(pos)) return true;
			return false;
		}
		BoundingBox box = StructurePiece.createBoundingBox(pieces.stream());
		return box.isInside(pos);
	}

	/** requires_piece: one of the pieces was placed from a listed template. Cached per whole start (key null: not cached). */
	private static boolean hasRequired(Box box, StructureStart start, List<StructurePiece> pieces) {
		if (box.requiresPiece().isEmpty()) return true;
		if (start != null) {
			Boolean seen = REQUIRED_SEEN.get(start);
			if (seen != null) return seen;
		}
		boolean hit = false;
		for (StructurePiece p : pieces) {
			ResourceLocation t = templateOf(p);
			if (t != null && box.requiresPiece().contains(t)) { hit = true; break; }
		}
		if (start != null) REQUIRED_SEEN.put(start, hit);
		return hit;
	}

	private static ResourceLocation templateOf(StructurePiece piece) {
		if (piece instanceof PoolElementStructurePiece pool && pool.getElement() instanceof SinglePoolElement single) {
			try {
				if (EarthFill.SINGLE_TEMPLATE.get(single) instanceof Either<?, ?> either) {
					return either.left().map(o -> o instanceof ResourceLocation rl ? rl : null).orElse(null);
				}
			} catch (IllegalAccessException ignored) {
			}
		}
		return null;
	}

	// ── box mobs' drops ──────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
		if (event.getSpawnReason() != MobSpawnType.NATURAL || !config.noDrops().containsKey(event.getEntity().getType())) return;
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
		if (mobsAt(level.structureManager(), event.getEntity().getType().getCategory(), pos) != null) {
			event.getEntity().getPersistentData().putBoolean(BOX_MOB, true);
		}
	}

	@SubscribeEvent
	public static void onDrops(LivingDropsEvent event) {
		Set<Item> items = config.noDrops().get(event.getEntity().getType());
		if (items == null || items.isEmpty() || !event.getEntity().getPersistentData().getBoolean(BOX_MOB)) return;
		event.getDrops().removeIf((ItemEntity drop) -> items.contains(drop.getItem().getItem()));
	}
}
