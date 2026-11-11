package com.SaintAlfonzo.creativeworldgen.worldgen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * D-DIM (namespace_move.md section 4): the pack's own dimensions moved from creativeengineering: to creativeworldgen:
 * on 2026-11-10. A dimension's chunks, entities, POI and per-dimension data live in
 * {@code <world>/dimensions/<namespace>/<path>}, so an old world's folders are renamed to the new namespace when the
 * world is opened (mixin.LevelStorageAccessDimensionMoveMixin: the end of the LevelStorageAccess constructor, after the
 * session lock and before level.dat or any level is read). Only the dimensions in {@link LegacyIds#DIMENSIONS}; a
 * folder whose new twin already exists is left alone and logged (never merged). The dimension ids saved in level.dat,
 * player files and other mods' data are read through {@link LegacyIds#keyAlias}.
 */
public final class DimensionMove {
	private static final Logger LOG = LogManager.getLogger("creativeworldgen/dimension-move");

	private DimensionMove() {}

	public static void moveFolders(Path levelRoot) {
		Path dimensions = levelRoot.resolve("dimensions");
		Path oldDir = dimensions.resolve(LegacyIds.LEGACY_NS);
		if (!Files.isDirectory(oldDir)) return;
		Path newDir = dimensions.resolve(LegacyIds.NS);
		for (String dim : LegacyIds.DIMENSIONS) {
			Path from = oldDir.resolve(dim);
			if (!Files.isDirectory(from)) continue;
			Path to = newDir.resolve(dim);
			if (Files.exists(to)) {
				LOG.warn("[dimension-move] {} and {} both exist: left both, only the second is loaded; move the old chunks by hand if needed",
						levelRoot.relativize(from), levelRoot.relativize(to));
				continue;
			}
			try {
				Files.createDirectories(newDir);
				Files.move(from, to);
				LOG.info("[dimension-move] {} -> {}", levelRoot.relativize(from), levelRoot.relativize(to));
			} catch (IOException e) {
				LOG.error("[dimension-move] could not move {} to {}: the dimension starts empty under its new id", from, to, e);
			}
		}
		try (Stream<Path> left = Files.list(oldDir)) {
			if (left.findAny().isEmpty()) Files.delete(oldDir);
		} catch (IOException e) {
			LOG.warn("[dimension-move] could not remove the empty folder {}", oldDir, e);
		}
	}
}
