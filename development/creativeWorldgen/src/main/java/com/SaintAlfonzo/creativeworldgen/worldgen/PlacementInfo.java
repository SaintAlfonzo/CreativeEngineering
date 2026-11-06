package com.SaintAlfonzo.creativeworldgen.worldgen;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Optional;

/**
 * The protected StructurePlacement fields for /dumpstructuregen (kubejs/server_scripts/_lib/dump_structuregen.js):
 * Rhino can't call protected methods, so the dump used to log locate_offset, frequency_reduction_method, frequency and
 * salt as errors on every set. Read here by SRG field name, with a Mojang-name fallback for dev.
 */
public final class PlacementInfo {

	private static final Field LOCATE_OFFSET = field("f_227022_", "locateOffset");
	private static final Field REDUCTION = field("f_227023_", "frequencyReductionMethod");
	private static final Field FREQUENCY = field("f_227024_", "frequency");
	private static final Field SALT = field("f_227025_", "salt");
	private static final Field EXCLUSION = field("f_227026_", "exclusionZone");

	private PlacementInfo() {}

	public static int[] locateOffset(StructurePlacement placement) {
		Vec3i v = (Vec3i) get(LOCATE_OFFSET, placement);
		return new int[] { v.getX(), v.getY(), v.getZ() };
	}

	/** The datapack name: default, legacy_type_1, legacy_type_2, legacy_type_3. */
	public static String frequencyReductionMethod(StructurePlacement placement) {
		return ((Enum<?>) get(REDUCTION, placement)).name().toLowerCase(Locale.ROOT);
	}

	public static float frequency(StructurePlacement placement) {
		return (Float) get(FREQUENCY, placement);
	}

	public static int salt(StructurePlacement placement) {
		return (Integer) get(SALT, placement);
	}

	/** "other_set chunk_count", or null when the placement has no exclusion zone. */
	public static String exclusionZone(StructurePlacement placement) {
		Optional<?> zone = (Optional<?>) get(EXCLUSION, placement);
		if (zone.isEmpty()) return null;
		StructurePlacement.ExclusionZone z = (StructurePlacement.ExclusionZone) zone.get();
		String other = z.otherSet().unwrapKey().map(k -> k.location().toString()).orElse("?");
		return other + " " + z.chunkCount();
	}

	private static Object get(Field f, Object target) {
		try {
			return f.get(target);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("placement reflection", e);
		}
	}

	private static Field field(String srg, String mojmap) {
		try {
			return ObfuscationReflectionHelper.findField(StructurePlacement.class, srg);
		} catch (RuntimeException | LinkageError e) {
			try {
				Field f = StructurePlacement.class.getDeclaredField(mojmap);
				f.setAccessible(true);
				return f;
			} catch (ReflectiveOperationException e2) {
				throw new IllegalStateException("no field " + srg + "/" + mojmap + " on StructurePlacement", e2);
			}
		}
	}
}
