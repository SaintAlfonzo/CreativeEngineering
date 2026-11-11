package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Load-only aliases for the ids this jar had under {@code creativeengineering:} until 2026-11-10 (namespace_move.md
 * step 6, D-DIM). Forge registries (the biome modifier codecs) are remapped by the swap list in
 * data/creativeworldgen/swaps; this class covers what Forge cannot remap:
 * <ul>
 * <li>Dimension, dimension type and noise settings keys ({@link #keyAlias}, called from
 * mixin.ResourceKeyLegacyDimensionMixin): every saved dimension string (level.dat's dimension list, player
 * Dimension and SpawnDimension, other mods' saved data, portal entities) is read through {@code ResourceKey.create},
 * so an old id resolves to the moved dimension wherever it comes from. Only the listed paths, never the whole namespace.</li>
 * <li>Structure starts, structure references and piece types in chunk NBT ({@link #upgradeStructures}, called from
 * mixin.ChunkSerializerLegacyStructuresMixin and mixin.StructureCheckLegacyStructuresMixin): vanilla reads them with
 * {@code Registry.get(ResourceLocation)}, which has no alias, and drops unknown ones.</li>
 * <li>Structure ids and placed features held inside our own meta pieces ({@link #moved}, MetaStructure.Index and
 * MetaFeaturePiece).</li>
 * </ul>
 * An old id is renamed only when it is not registered and its creativeworldgen: twin is. The folders of the moved
 * dimensions are moved by {@link DimensionMove}. Never remove: a world last saved before the move must keep loading.
 */
public final class LegacyIds {
	private static final Logger LOG = LogManager.getLogger("creativeworldgen/legacy-ids");

	/** The namespace these ids had until 2026-11-10. */
	public static final String LEGACY_NS = "creativeengineering";
	/** CreativeWorldgen.NS, repeated so this class (loaded at bootstrap by the ResourceKey mixin) does not load the mod class. */
	public static final String NS = "creativeworldgen";

	/** data/creativeworldgen/dimension: the dimensions (and their level stems) that moved. */
	public static final Set<String> DIMENSIONS = Set.of("autumnsky", "desert", "flowergrove", "flowerhills", "flowerridge", "hub",
			"shield", "springsky", "summersky", "wintersky", "yellowstone");
	static final Set<String> DIMENSION_TYPES = Set.of("hub_type");
	static final Set<String> NOISE_SETTINGS = Set.of("y208void", "y32plus", "y40plus");

	private static final AtomicBoolean LOGGED_KEY = new AtomicBoolean();
	private static final AtomicBoolean LOGGED_CHUNK = new AtomicBoolean();

	private LegacyIds() {}

	/**
	 * The moved location for an old dimension, level stem, dimension type or noise settings key, else null.
	 * {@code registry} is the registry key's location (minecraft:dimension holds both levels and level stems).
	 */
	public static ResourceLocation keyAlias(ResourceLocation registry, ResourceLocation location) {
		if (!LEGACY_NS.equals(location.getNamespace()) || !"minecraft".equals(registry.getNamespace())) return null;
		Set<String> moved = switch (registry.getPath()) {
			case "dimension" -> DIMENSIONS;
			case "dimension_type" -> DIMENSION_TYPES;
			case "worldgen/noise_settings" -> NOISE_SETTINGS;
			default -> null;
		};
		if (moved == null || !moved.contains(location.getPath())) return null;
		if (LOGGED_KEY.compareAndSet(false, true)) {
			LOG.info("[legacy-ids] old key {} {} read as {}:{} (further renames not logged)", registry, location, NS, location.getPath());
		}
		return new ResourceLocation(NS, location.getPath());
	}

	/** The creativeworldgen: twin of an old id when only the twin is registered in {@code registry}, else null. */
	public static ResourceLocation moved(String id, Registry<?> registry) {
		ResourceLocation rl = id == null || id.isEmpty() ? null : ResourceLocation.tryParse(id);
		if (rl == null || !LEGACY_NS.equals(rl.getNamespace())) return null;
		ResourceLocation to = new ResourceLocation(NS, rl.getPath());
		return registry.containsKey(to) && !registry.containsKey(rl) ? to : null;
	}

	/**
	 * Renames old structure ids in a chunk's {@code structures} compound in place: the {@code starts} and
	 * {@code References} keys, each start's {@code id} and its pieces' {@code id}. Idempotent.
	 */
	public static void upgradeStructures(CompoundTag structures, Registry<?> structureRegistry) {
		if (structures == null || structures.isEmpty()) return;
		int renamed = 0;
		if (structures.contains("starts", Tag.TAG_COMPOUND)) {
			CompoundTag starts = structures.getCompound("starts");
			renamed += renameKeys(starts, structureRegistry);
			for (String key : starts.getAllKeys()) {
				CompoundTag start = starts.getCompound(key);
				ResourceLocation id = moved(start.getString("id"), structureRegistry);
				if (id != null) {
					start.putString("id", id.toString());
					renamed++;
				}
				for (Tag t : start.getList("Children", Tag.TAG_COMPOUND)) {
					CompoundTag piece = (CompoundTag) t;
					ResourceLocation type = moved(piece.getString("id"), Registry.STRUCTURE_PIECE);
					if (type != null) {
						piece.putString("id", type.toString());
						renamed++;
					}
				}
			}
		}
		if (structures.contains("References", Tag.TAG_COMPOUND)) renamed += renameKeys(structures.getCompound("References"), structureRegistry);
		if (renamed > 0 && LOGGED_CHUNK.compareAndSet(false, true)) {
			LOG.info("[legacy-ids] chunk structure data under {}: read as {}: (further chunks not logged)", LEGACY_NS, NS);
		}
	}

	private static int renameKeys(CompoundTag tag, Registry<?> registry) {
		int renamed = 0;
		for (String key : new ArrayList<>(tag.getAllKeys())) {
			ResourceLocation to = moved(key, registry);
			if (to == null) continue;
			Tag value = tag.get(key);
			tag.remove(key);
			if (!tag.contains(to.toString()) && value != null) tag.put(to.toString(), value);
			renamed++;
		}
		return renamed;
	}

	/**
	 * Config lists written before the move ([worldgen.ground_match] families, [worldgen.earth_fill] blocks and
	 * overrides, [worldgen.water_fill] overrides) name this jar's tags as creativeengineering:; read them as
	 * creativeworldgen:. Every creativeengineering: tag these lists can name is Worldgen's.
	 */
	public static java.util.List<? extends String> configIds(java.util.List<? extends String> entries) {
		if (entries.stream().noneMatch(e -> e.contains(LEGACY_NS + ":"))) return entries;
		return entries.stream().map(e -> e.replace(LEGACY_NS + ":", NS + ":")).toList();
	}

	/**
	 * A random seed string as it was before the move: creativeworldgen: ids read as creativeengineering:, so salts and
	 * rolls hashed from ids (structure frequencies, meta shares, companions, the distant seabed) stay the same and old
	 * worlds go on generating as before.
	 */
	public static String seed(String text) {
		return text == null ? null : text.replace(NS + ":", LEGACY_NS + ":");
	}
}
