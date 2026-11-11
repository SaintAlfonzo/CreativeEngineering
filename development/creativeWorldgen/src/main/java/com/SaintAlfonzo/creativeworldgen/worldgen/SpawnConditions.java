package com.SaintAlfonzo.creativeworldgen.worldgen;

import com.SaintAlfonzo.creativeworldgen.CWGConfig;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Spawn conditions from the {@code conditions} list of kubejs/generation_scripts/worldgen/mob_spawns.json
 * (feature_writeup/mob_spawns.md section 3.1). They ride in the same {@code creativeworldgen:tune_spawns}
 * biome modifier as the rules; {@link SpawnTuning#modify} installs them, so they follow the CWGConfig
 * {@code mobSpawns} switch and reload with the world.
 *
 * <p>Checked from {@code SpawnPlacementsMixin} at the head of {@code SpawnPlacements.checkSpawnRules}, for natural
 * spawns and chunk-generation herds only (spawn eggs, spawners and commands are untouched):
 * <ul>
 *   <li>{@code light_only}: the mob's spawn rule becomes "light above 8" (raw sky + block light, the same test as
 *       vanilla animals), on any block its placement type allows. Replaces ground-block requirements. The mob's own
 *       {@code checkSpawnRules} (Alex's Mobs' 1-in-N roll) still runs afterwards.</li>
 *   <li>{@code water_only}: the spawn position must be on the water surface: the top motion-blocking block of the
 *       column is water and the position is at or above it. The mob's own rule still runs too.</li>
 * </ul>
 * A condition without {@code biomes} applies everywhere; with them, only in those biomes.
 */
public final class SpawnConditions {

	private static final Logger LOGGER = LogUtils.getLogger();

	private SpawnConditions() {}

	public enum Kind {
		LIGHT_ONLY, WATER_ONLY;

		static final Codec<Kind> CODEC = Codec.STRING.comapFlatMap(s -> {
			try {
				return DataResult.success(Kind.valueOf(s.toUpperCase(Locale.ROOT)));
			} catch (IllegalArgumentException e) {
				return DataResult.error("tune_spawns: unknown condition '" + s + "' (light_only, water_only)");
			}
		}, k -> k.name().toLowerCase(Locale.ROOT));
	}

	public record Condition(String id, Kind kind, List<ResourceLocation> mobs, Optional<HolderSet<Biome>> biomes) {
		public static final Codec<Condition> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.STRING.fieldOf("id").forGetter(Condition::id),
				Kind.CODEC.fieldOf("condition").forGetter(Condition::kind),
				ResourceLocation.CODEC.listOf().fieldOf("mobs").forGetter(Condition::mobs),
				Biome.LIST_CODEC.optionalFieldOf("biomes").forGetter(Condition::biomes)
		).apply(i, Condition::new));
	}

	private record Active(Condition condition, Set<EntityType<?>> types) {}

	private static volatile List<Active> active = List.of();
	private static volatile List<Condition> installed = List.of();

	/** Called for every biome the modifier visits; resolves the mob ids once per new list. */
	static void install(List<Condition> conditions) {
		if (conditions == installed) return;
		List<Active> out = new ArrayList<>();
		for (Condition c : conditions) {
			Set<EntityType<?>> types = new HashSet<>();
			for (ResourceLocation id : c.mobs()) {
				Optional<EntityType<?>> type = Registry.ENTITY_TYPE.getOptional(id);
				if (type.isPresent()) types.add(type.get());
				else LOGGER.warn("[mob-spawns] condition {}: no entity type {}, skipped", c.id(), id);
			}
			if (!types.isEmpty()) out.add(new Active(c, Set.copyOf(types)));
		}
		active = List.copyOf(out);
		installed = conditions;
	}

	static void clear() {
		active = List.of();
		installed = List.of();
	}

	/** Condition ids in force, in file order (for /dumpspawns). */
	public static List<String> ids() {
		List<String> out = new ArrayList<>();
		for (Active a : active) out.add(a.condition().id());
		return out;
	}

	/**
	 * The conditions' verdict for one spawn check: {@code null} = no condition applies, run the mob's own rule;
	 * {@code FALSE} = refuse; {@code TRUE} = allow without the mob's own rule (light_only passed).
	 */
	public static Boolean check(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos) {
		List<Active> list = active;
		if (list.isEmpty() || (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION)) return null;
		Boolean verdict = null;
		for (Active a : list) {
			if (!a.types().contains(type)) continue;
			Optional<HolderSet<Biome>> biomes = a.condition().biomes();
			if (biomes.isPresent() && !biomes.get().contains(level.getBiome(pos))) continue;
			switch (a.condition().kind()) {
				case WATER_ONLY -> {
					if (!onWaterSurface(level, pos)) return Boolean.FALSE;
				}
				case LIGHT_ONLY -> {
					if (level.getRawBrightness(pos, 0) <= 8) return Boolean.FALSE;
					verdict = Boolean.TRUE;
				}
			}
		}
		return verdict;
	}

	private static boolean onWaterSurface(ServerLevelAccessor level, BlockPos pos) {
		int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 1;
		return pos.getY() >= top && level.getFluidState(new BlockPos(pos.getX(), top, pos.getZ())).is(FluidTags.WATER);
	}
}
