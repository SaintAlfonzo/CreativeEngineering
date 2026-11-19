package com.SaintAlfonzo.creativeloot.corpse;

import com.SaintAlfonzo.creativecompat.corpse.CorpseDecay;
import com.SaintAlfonzo.creativecompat.corpse.CorpseState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;
import java.util.UUID;

/**
 * Ancient corpses and Visceral Heap remains (feature_writeup/plan_corpses.md 3.2 and 3.4; TODO `corpses #2`, `#5`,
 * `#7`). One placed feature per chunk, added by {@link AncientCorpseModifier} to overworld biomes and the remains
 * biomes, only when the Corpse mod is loaded. Each chunk rolls independently:
 * <ul>
 * <li><b>cave</b> (overworld): {@code chunk_chance}, then a target height drawn from {@code y_curve} (below
 * {@code max_y}), then up to {@code attempts} columns scanned {@code search} blocks around it for a spot;</li>
 * <li><b>ocean</b> (overworld, the chunk's center biome in {@code #minecraft:is_ocean}): {@code base_chunk_chance}
 * times the temperature factor (cold / warm lists, else neutral) and the depth factor ({@code #minecraft:is_deep_ocean});
 * the spot is the ocean floor heightmap, under water;</li>
 * <li><b>remains</b> (a {@code remains.biomes} biome at the spot, any dimension, any height).</li>
 * </ul>
 * A spot is a 3x3 ({@code footprint}) floor of full blocks at one height with {@code headroom} blocks of air (water
 * for ocean) above all of it (ruling 11). The corpse is built from NBT (no Corpse classes needed here): an empty Death
 * with the rolled armor (ruling 7), Compat's {@code Ancient} or {@code Remains} state, and in Forge persistent data the
 * unrolled loot table ({@link #LOOT_KEY}, rolled on first open, ruling 5) plus Compat's sealed mark, or for remains
 * {@link #REMAINS_KEY} (broken into {@code remains.drops}).
 */
public class AncientCorpseFeature extends Feature<NoneFeatureConfiguration> {
	/** Forge persistent data: the chest loot table rolled into the corpse when first opened. */
	public static final String LOOT_KEY = "creativeloot:corpse_loot";
	/** Forge persistent data (boolean): Visceral Heap remains, broken by a hit into {@code remains.drops}. */
	public static final String REMAINS_KEY = "creativeloot:corpse_remains";

	public AncientCorpseFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	enum Kind { CAVE, OCEAN, REMAINS }

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		AncientCorpseConfig c = AncientCorpseConfig.get();
		if (!c.enabled) return false;
		WorldGenLevel level = ctx.level();
		RandomSource r = ctx.random();
		BlockPos o = ctx.origin();
		int x0 = o.getX() & ~15, z0 = o.getZ() & ~15;
		boolean placed = false;

		if (c.remains.enabled() && !c.remains.biomes().isEmpty() && r.nextDouble() < c.remains.chunkChance()) {
			placed |= tryRemains(level, c, r, x0, z0);
		}
		if (level.getLevel().dimension() != Level.OVERWORLD) return placed;

		int sea = level.getLevel().getSeaLevel();
		Holder<Biome> center = level.getBiome(new BlockPos(x0 + 8, sea - 1, z0 + 8));
		if (center.is(BiomeTags.IS_OCEAN)) {
			double chance = c.ocean.baseChunkChance() * c.ocean.temperatureFactor().getOrDefault(temperature(c, center), 1.0)
					* c.ocean.depthFactor().getOrDefault(center.is(BiomeTags.IS_DEEP_OCEAN) ? "deep" : "shallow", 1.0);
			if (r.nextDouble() < chance) placed |= tryOcean(level, c, r, x0, z0, sea);
		}
		if (r.nextDouble() < c.cave.chunkChance()) placed |= tryCave(level, c, r, x0, z0);
		return placed;
	}

	static String temperature(AncientCorpseConfig c, Holder<Biome> b) {
		for (String id : c.ocean.coldBiomes()) if (b.is(new ResourceLocation(id))) return "cold";
		for (String id : c.ocean.warmBiomes()) if (b.is(new ResourceLocation(id))) return "warm";
		return "neutral";
	}

	private boolean tryCave(WorldGenLevel level, AncientCorpseConfig c, RandomSource r, int x0, int z0) {
		int maxY = Math.min(c.cave.maxY(), level.getLevel().getSeaLevel() - 1);
		int target = HeightCurve.sampleY(c.cave.curveY(), c.cave.curveWeight(), r, level.getMinBuildHeight() + 1, maxY);
		if (target == Integer.MIN_VALUE) return false;
		for (int i = 0; i < c.cave.attempts(); i++) {
			int x = x0 + r.nextInt(16), z = z0 + r.nextInt(16);
			int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
			int top = Math.min(maxY, target + c.cave.search());
			int bottom = Math.max(level.getMinBuildHeight() + 1, target - c.cave.search());
			for (int y = top; y >= bottom; y--) {
				if (y + c.headroom >= surface) continue;
				BlockPos pos = new BlockPos(x, y, z);
				if (fits(level, pos, c, false) && !inRemainsBiome(level, pos, c)) {
					spawn(level, pos, c, r, Kind.CAVE);
					return true;
				}
			}
		}
		return false;
	}

	private boolean tryOcean(WorldGenLevel level, AncientCorpseConfig c, RandomSource r, int x0, int z0, int sea) {
		for (int i = 0; i < c.ocean.attempts(); i++) {
			int x = x0 + r.nextInt(16), z = z0 + r.nextInt(16);
			int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
			if (y + c.headroom > sea) continue;
			BlockPos pos = new BlockPos(x, y, z);
			if (!level.getBiome(pos).is(BiomeTags.IS_OCEAN)) continue;
			if (fits(level, pos, c, true)) {
				spawn(level, pos, c, r, Kind.OCEAN);
				return true;
			}
		}
		return false;
	}

	private boolean tryRemains(WorldGenLevel level, AncientCorpseConfig c, RandomSource r, int x0, int z0) {
		int min = level.getMinBuildHeight() + 1;
		int max = level.getMinBuildHeight() + level.getLevel().getLogicalHeight() - c.headroom - 1;
		if (max <= min) return false;
		for (int i = 0; i < c.remains.attempts(); i++) {
			int x = x0 + r.nextInt(16), z = z0 + r.nextInt(16);
			int start = min + r.nextInt(max - min + 1);
			if (!inRemainsBiome(level, new BlockPos(x, start, z), c)) continue;
			for (int y = start; y >= Math.max(min, start - c.remains.search()); y--) {
				BlockPos pos = new BlockPos(x, y, z);
				if (fits(level, pos, c, false) && inRemainsBiome(level, pos, c)) {
					spawn(level, pos, c, r, Kind.REMAINS);
					return true;
				}
			}
		}
		return false;
	}

	static boolean inRemainsBiome(WorldGenLevel level, BlockPos pos, AncientCorpseConfig c) {
		Holder<Biome> b = level.getBiome(pos);
		for (String id : c.remains.biomes()) if (b.is(new ResourceLocation(id))) return true;
		return false;
	}

	/** 3x3 floor of full blocks at y-1 and {@code headroom} blocks of air (or water) above each (ruling 11). */
	static boolean fits(WorldGenLevel level, BlockPos pos, AncientCorpseConfig c, boolean water) {
		int half = c.footprint / 2;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int dx = -half; dx <= half; dx++) {
			for (int dz = -half; dz <= half; dz++) {
				m.set(pos.getX() + dx, pos.getY() - 1, pos.getZ() + dz);
				BlockState floor = level.getBlockState(m);
				if (!floor.getFluidState().isEmpty() || !floor.isCollisionShapeFullBlock(level, m)) return false;
				for (int h = 0; h < c.headroom; h++) {
					m.set(pos.getX() + dx, pos.getY() + h, pos.getZ() + dz);
					BlockState s = level.getBlockState(m);
					if (water ? !s.is(Blocks.WATER) : !s.isAir()) return false;
				}
			}
		}
		return true;
	}

	private void spawn(WorldGenLevel level, BlockPos pos, AncientCorpseConfig c, RandomSource r, Kind kind) {
		ServerLevel server = level.getLevel();
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "corpse:corpse");

		CompoundTag death = new CompoundTag();
		death.putUUID("Id", new UUID(r.nextLong(), r.nextLong()));
		death.putUUID("PlayerUuid", new UUID(0L, 0L));
		death.putString("PlayerName", "");
		ListTag armor = new ListTag();
		if (kind != Kind.REMAINS) {
			ItemStack[] rolled = AncientArmor.roll(c.armor, r);
			for (int i = 0; i < rolled.length; i++) {
				if (rolled[i].isEmpty()) continue;
				CompoundTag st = rolled[i].save(new CompoundTag());
				st.putInt("Slot", i);
				armor.add(st);
			}
		}
		death.put("ArmorInventory", armor);
		death.putString("Dimension", server.dimension().location().toString());
		death.putDouble("PosX", pos.getX() + 0.5);
		death.putDouble("PosY", pos.getY());
		death.putDouble("PosZ", pos.getZ() + 0.5);
		tag.put("Death", death);

		CompoundTag state = new CompoundTag();
		state.putBoolean(kind == Kind.REMAINS ? "Remains" : "Ancient", true);
		state.putByte("Stage", (byte) CorpseDecay.SKELETON);
		tag.put(CorpseDecay.TAG, state);

		CompoundTag forge = new CompoundTag();
		switch (kind) {
			case CAVE -> forge.putString(LOOT_KEY, c.cave.lootTable());
			case OCEAN -> forge.putString(LOOT_KEY, c.ocean.lootTable());
			case REMAINS -> forge.putBoolean(REMAINS_KEY, true);
		}
		if (kind != Kind.REMAINS) forge.putBoolean(CorpseState.SEALED, true);
		tag.put("ForgeData", forge);
		// the Corpse mod floats corpses up through water; ocean floor ones stay put
		if (kind == Kind.OCEAN) tag.putBoolean("NoGravity", true);

		Entity e = EntityType.loadEntityRecursive(tag, server, ent -> {
			ent.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, r.nextFloat() * 360.0F, 0.0F);
			return ent;
		});
		if (e != null) level.addFreshEntity(e);
	}
}
