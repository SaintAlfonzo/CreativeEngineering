// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativecommon.perf;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * The dimension limit on Create: Gravity's per-entity work ({@code [perf] gravityLimitDimensions} and
 * {@code gravityDimensions}; mixin {@code CreateGravityDimensionMixin}). Common API: Creative Compat's low gravity for
 * non-living entities ({@code creativecompat.gravity.LowGravity}) asks the same limit, so both stop at the same
 * dimensions (plan_wave2_publish.md W2-4). Plain Minecraft classes only, so it loads without Create: Gravity. Both sides
 * read their own copy of Common's config; the pack ships the same file to both.
 */
public final class GravityDimensions {

	private static List<? extends String> source;
	private static Set<ResourceLocation> dimensions = Set.of();

	private GravityDimensions() {}

	/** False when the dimension limit is on and this level is not in perf.gravityDimensions. */
	public static boolean allowed(Level level) {
		if (!CommonConfig.PERF.gravityLimitDimensions.get()) return true;
		List<? extends String> list = CommonConfig.PERF.gravityDimensions.get();
		if (list != source) {
			Set<ResourceLocation> set = new HashSet<>();
			for (String id : list) {
				ResourceLocation rl = ResourceLocation.tryParse(id);
				if (rl != null) set.add(rl);
			}
			dimensions = set;
			source = list;
		}
		return dimensions.contains(level.dimension().location());
	}
}
