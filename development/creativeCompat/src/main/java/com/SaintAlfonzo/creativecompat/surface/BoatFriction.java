package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boat-only friction per block (R3, plan_small_fixes_1116.md), read from {@code [boat_friction] blocks} ("block id=friction")
 * and rebuilt when that list changes (config reload included). Used by {@code mixin/BoatIceFrictionMixin}.
 */
public final class BoatFriction {
	private static final Logger LOG = LogManager.getLogger("creativecompat/boat-friction");
	private static List<? extends String> source;
	private static Map<Block, Float> map = Map.of();

	private BoatFriction() {
	}

	/** The boat friction for this block, or null for the block's own. */
	public static Float of(BlockState state) {
		if (!CompatConfig.SPEC.isLoaded() || !CompatConfig.BOAT_FRICTION.enabled.get()) return null;
		List<? extends String> now = CompatConfig.BOAT_FRICTION.blocks.get();
		if (now != source) rebuild(now);
		return map.get(state.getBlock());
	}

	private static synchronized void rebuild(List<? extends String> list) {
		Map<Block, Float> out = new IdentityHashMap<>();
		for (String row : list) {
			int eq = row.indexOf('=');
			ResourceLocation id = eq > 0 ? ResourceLocation.tryParse(row.substring(0, eq).trim()) : null;
			Block block = id != null && ForgeRegistries.BLOCKS.containsKey(id) ? ForgeRegistries.BLOCKS.getValue(id) : null;
			try {
				if (block == null) throw new NumberFormatException();
				out.put(block, Float.parseFloat(row.substring(eq + 1).trim()));
			} catch (NumberFormatException e) {
				LOG.warn("[boat_friction] skipped \"{}\" (want block id=friction, a known block)", row);
			}
		}
		map = out;
		source = list;
	}
}
