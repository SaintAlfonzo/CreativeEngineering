package com.SaintAlfonzo.creativeengineering.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

/**
 * Geodes under oceans generate lower, so fewer break through the sea floor and leave amethyst or prismarine shells
 * sticking out of the seabed. Covers vanilla's GeodeFeature: the amethyst geode and every Geode Plus geode (all are
 * built on it; Geode Plus' own OceanGeodeFeature is registered but unused). mixin/GeodeFeatureOceanDepthMixin.
 * Geodes whose inner layer is in [worldgen.ocean_geodes] untouched (Geode Plus' prismarine geode) keep their height.
 *
 * A geode counts as under an ocean when the biome at the sea floor of its column (OCEAN_FLOOR_WG) is in
 * #minecraft:is_ocean. Such a geode is moved down until its top (origin + its config's maxGenOffset, the bound of
 * GeodeFeature's placement box) is {@code clearance} blocks under {@code lowestFloorY}, the deepest sea floor the pack
 * generates, then up to {@code spread} blocks further so moved geodes don't share one layer. Geodes already that deep
 * stay. A share {@code stayShare} keeps its height regardless. Both rolls hash the world seed and the geode's position,
 * so they don't consume the feature's random and are the same every time. CEConfig [worldgen.ocean_geodes], switch
 * [worldgen.systems] oceanGeodes.
 */
public final class OceanGeodes {

	private static final int SALT = "ocean-geodes".hashCode();
	private static final int SPREAD_SALT = "ocean-geodes-spread".hashCode();
	/** Set while the moved geode places, so its own place() call doesn't move it again. */
	private static final ThreadLocal<Boolean> PLACING = new ThreadLocal<>();

	private OceanGeodes() {}

	/**
	 * Feature#place HEAD for a geode: the result of placing it lower, or null to go on as usual (not under an ocean,
	 * a geode that stays, the system off, or the moved geode's own call).
	 */
	public static Boolean placeLowered(FeaturePlaceContext<GeodeConfiguration> ctx, Predicate<FeaturePlaceContext<GeodeConfiguration>> place) {
		if (PLACING.get() != null || !WorldgenConfig.oceanGeodesEnabled() || untouched(ctx.config())) return null;
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		int reach = ctx.config().maxGenOffset;
		int highest = WorldgenConfig.oceanGeodeLowestFloorY() - WorldgenConfig.oceanGeodeClearance() - reach;
		if (origin.getY() <= highest) return null;
		int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
		if (!level.getBiome(new BlockPos(origin.getX(), floor, origin.getZ())).is(BiomeTags.IS_OCEAN)) return null;
		if (StructureFrequencies.roll(level.getSeed(), origin.getX(), origin.getZ(), SALT + origin.getY()) < WorldgenConfig.oceanGeodeStayShare()) return null;
		int spread = (int) (StructureFrequencies.roll(level.getSeed(), origin.getX(), origin.getZ(), SPREAD_SALT + origin.getY())
				* (WorldgenConfig.oceanGeodeSpread() + 1));
		BlockPos lowered = origin.atY(highest - spread);
		if (lowered.getY() - reach <= level.getMinBuildHeight()) return null;
		PLACING.set(Boolean.TRUE);
		try {
			return place.test(new FeaturePlaceContext<>(ctx.topFeature(), level, ctx.chunkGenerator(), ctx.random(), lowered, ctx.config()));
		} finally {
			PLACING.remove();
		}
	}

	/**
	 * The geode's inner layer or alternate inner layer block is on the untouched list. Geode Plus' prismarine
	 * geode has plain prismarine inside and its budding prismarine in the alternate layer (amethyst's pattern).
	 */
	private static boolean untouched(GeodeConfiguration config) {
		java.util.List<? extends String> ids = WorldgenConfig.oceanGeodeUntouched();
		if (ids.isEmpty()) return false;
		return listed(ids, config.geodeBlockSettings.innerLayerProvider.getState(RandomSource.create(0), BlockPos.ZERO))
				|| listed(ids, config.geodeBlockSettings.alternateInnerLayerProvider.getState(RandomSource.create(0), BlockPos.ZERO));
	}

	private static boolean listed(java.util.List<? extends String> ids, BlockState state) {
		ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
		return id != null && ids.contains(id.toString());
	}
}
