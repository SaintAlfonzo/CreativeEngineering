package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Drops structure placements that can never place anything (frequency <= 0) from structure
 * searches. Used by ChunkGeneratorLocateMixin; see there for why.
 */
public final class DeadPlacementFilter {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Field FREQUENCY = field(StructurePlacement.class, "f_227024_", "frequency");
	private static boolean logged;

	private DeadPlacementFilter() {}

	/** SRG name in the game; the Mojmap name when run headless against the mapped jar (tests). */
	private static Field field(Class<?> c, String srg, String mojmap) {
		try {
			return ObfuscationReflectionHelper.findField(c, srg);
		} catch (RuntimeException | LinkageError e) {
			try {
				Field f = c.getDeclaredField(mojmap);
				f.setAccessible(true);
				return f;
			} catch (NoSuchFieldException e2) {
				throw new IllegalStateException("DeadPlacementFilter: no field " + srg + "/" + mojmap + " on " + c.getName(), e);
			}
		}
	}

	public static boolean isDead(StructurePlacement placement) {
		try {
			return FREQUENCY.getFloat(placement) <= 0.0F;
		} catch (IllegalAccessException e) {
			return false;
		}
	}

	/** Returns {@code placements} unchanged when nothing is dead, otherwise a filtered copy. */
	public static List<StructurePlacement> filter(List<StructurePlacement> placements) {
		if (!WorldgenConfig.deadPlacementFilterEnabled()) return placements;
		List<StructurePlacement> live = null;
		for (int i = 0; i < placements.size(); i++) {
			StructurePlacement p = placements.get(i);
			if (isDead(p)) {
				if (live == null) live = new ArrayList<>(placements.subList(0, i));
			} else if (live != null) {
				live.add(p);
			}
		}
		if (live == null) return placements;
		if (!logged) {
			logged = true;
			LOGGER.info("[locate] skipping frequency-0 structure placements in map/locate searches (logged once)");
		}
		return live;
	}
}
