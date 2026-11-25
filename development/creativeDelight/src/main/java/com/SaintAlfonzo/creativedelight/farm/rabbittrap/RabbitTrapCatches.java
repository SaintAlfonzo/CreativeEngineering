// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.creativedelight.farm.rabbittrap;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unattended catch tables by biome (farming_for_blockheads.md 2.3.2): data files {@code data/<ns>/rabbit_trap/*.json}
 * of the form {@code {"biomes": {"<biome>": {"<entity type>": weight}}}}, merged in load order (a later file replaces
 * a biome's whole table). Creative Delight ships {@code farmingforblockheads:rabbit_trap/biomes}, written by
 * {@code tools/rabbit_trap_catches.py} from the spawn dump. A biome missing from the tables catches nothing. Types not in
 * the {@link RabbitTrap#CAPTURABLE} tag or not registered are never rolled.
 */
public final class RabbitTrapCatches extends SimpleJsonResourceReloadListener {
	public static final RabbitTrapCatches INSTANCE = new RabbitTrapCatches();
	private static final Gson GSON = new GsonBuilder().create();

	private Map<ResourceLocation, Map<ResourceLocation, Integer>> tables = Map.of();

	private RabbitTrapCatches() {
		super(GSON, "rabbit_trap");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
		Map<ResourceLocation, Map<ResourceLocation, Integer>> out = new HashMap<>();
		files.forEach((file, json) -> {
			try {
				JsonObject biomes = json.getAsJsonObject().getAsJsonObject("biomes");
				if (biomes == null) return;
				biomes.entrySet().forEach(biome -> {
					Map<ResourceLocation, Integer> table = new LinkedHashMap<>();
					biome.getValue().getAsJsonObject().entrySet().forEach(e -> {
						int weight = e.getValue().getAsInt();
						if (weight > 0) table.put(new ResourceLocation(e.getKey()), weight);
					});
					out.put(new ResourceLocation(biome.getKey()), table);
				});
			} catch (RuntimeException ex) {
				CreativeDelight.LOGGER.error("[creativedelight] rabbit trap catch table {} skipped: {}", file, ex.toString());
			}
		});
		tables = out;
		CreativeDelight.LOGGER.debug("[creativedelight] rabbit trap catch tables: {} biomes", out.size());
	}

	/** A weighted pick from the table of the biome at {@code pos}, or null when it has no catchable type. */
	@Nullable
	public EntityType<?> roll(ServerLevel level, BlockPos pos, RandomSource random) {
		ResourceLocation biome = level.getBiome(pos).unwrapKey().map(k -> k.location()).orElse(null);
		Map<ResourceLocation, Integer> table = biome == null ? null : tables.get(biome);
		if (table == null || table.isEmpty()) return null;
		Map<EntityType<?>, Integer> usable = new LinkedHashMap<>();
		int total = 0;
		for (Map.Entry<ResourceLocation, Integer> e : table.entrySet()) {
			EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(e.getKey());
			if (type == null || !ForgeRegistries.ENTITY_TYPES.containsKey(e.getKey()) || !type.is(RabbitTrap.CAPTURABLE)) continue;
			usable.put(type, e.getValue());
			total += e.getValue();
		}
		if (total <= 0) return null;
		int pick = random.nextInt(total);
		for (Map.Entry<EntityType<?>, Integer> e : usable.entrySet()) {
			pick -= e.getValue();
			if (pick < 0) return e.getKey();
		}
		return null;
	}
}
