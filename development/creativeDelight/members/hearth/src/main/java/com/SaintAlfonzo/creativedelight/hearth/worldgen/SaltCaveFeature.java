package com.SaintAlfonzo.creativedelight.hearth.worldgen;

import com.SaintAlfonzo.creativedelight.hearth.HHConfig;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Hearth and Harvest's salt cave with The Salt's blocks (hearth_and_harvest.md 3.2, D13, N24-11): the same 33-block
 * chambers and tunnels, the shell raw rock salt block with calcite and tuff, the rim of the pools raw rock salt block,
 * the drips The Salt's buds and cluster. Own code, written from the 1.4.1 behaviour.
 */
public class SaltCaveFeature extends Feature<NoneFeatureConfiguration> {
    private static final int RADIUS = 16;
    private static final int SIZE = RADIUS * 2 + 1;
    private static final byte OUTSIDE = 0;
    private static final byte SHELL = 1;
    private static final byte INSIDE = 2;
    private static final int BEDROCK_MARGIN = 6;
    private static final double SHELL_THICKNESS = 1.5;
    private static final double MIN_SOLID_SHELL = 0.7;
    private static final float POOL_CHANCE = 0.012F;
    private static final float CEILING_DRIP_CHANCE = 0.06F;
    private static final float FLOOR_DRIP_CHANCE = 0.05F;
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private static final ResourceLocation ROCK_ID = new ResourceLocation("salt", "raw_rock_salt_block");
    private static final ResourceLocation CLUSTER_ID = new ResourceLocation("salt", "salt_cluster");
    private static final ResourceLocation LARGE_ID = new ResourceLocation("salt", "large_salt_bud");
    private static final ResourceLocation MEDIUM_ID = new ResourceLocation("salt", "medium_salt_bud");
    private static final ResourceLocation SMALL_ID = new ResourceLocation("salt", "small_salt_bud");

    public SaltCaveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Nullable
    private static BlockState state(ResourceLocation id) {
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        return block == null || block == Blocks.AIR ? null : block.defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!HHConfig.SALT_CAVES_ENABLED.get()) {
            return false;
        }
        RandomSource random = ctx.random();
        if (random.nextInt(HHConfig.SALT_CAVE_RARITY.get()) != 0) {
            return false;
        }
        BlockState rock = state(ROCK_ID);
        if (rock == null) {
            return false;
        }
        WorldGenLevel level = ctx.level();
        BlockPos origin = pickOrigin(level, ctx.origin(), random);
        if (origin == null) {
            return false;
        }
        byte[] grid = buildGrid(Layout.create(random));
        if (!canPlace(level, origin, grid)) {
            return false;
        }
        carve(level, origin, grid, random, rock);
        placePools(level, origin, grid, random, rock);
        decorate(level, origin, grid, random);
        return true;
    }

    @Nullable
    private static BlockPos pickOrigin(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int minY = Math.max(HHConfig.SALT_CAVE_MIN_Y.get(), level.getMinBuildHeight() + BEDROCK_MARGIN + RADIUS);
        int maxY = Math.min(HHConfig.SALT_CAVE_MAX_Y.get(), level.getMaxBuildHeight() - RADIUS - 1);
        if (minY > maxY) {
            return null;
        }
        if (origin.getY() >= minY && origin.getY() <= maxY) {
            return origin;
        }
        return new BlockPos(origin.getX(), minY + random.nextInt(maxY - minY + 1), origin.getZ());
    }

    private static byte[] buildGrid(Layout layout) {
        byte[] grid = new byte[SIZE * SIZE * SIZE];
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    boolean edge = Math.abs(dx) == RADIUS || Math.abs(dy) == RADIUS || Math.abs(dz) == RADIUS;
                    grid[index(dx, dy, dz)] = edge ? OUTSIDE : layout.classify(dx, dy, dz);
                }
            }
        }
        return grid;
    }

    private static boolean canPlace(WorldGenLevel level, BlockPos origin, byte[] grid) {
        MutableBlockPos pos = new MutableBlockPos();
        MutableBlockPos neighbor = new MutableBlockPos();
        int shellCells = 0;
        int solidShellCells = 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    byte cell = grid[index(dx, dy, dz)];
                    if (cell == OUTSIDE) {
                        continue;
                    }
                    pos.setWithOffset(origin, dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.getFluidState().isEmpty() || state.is(BlockTags.FEATURES_CANNOT_REPLACE) || state.getDestroySpeed(level, pos) < 0.0F) {
                        return false;
                    }
                    if (cell == SHELL) {
                        shellCells++;
                        if (isHostRock(state) || state.is(Tags.Blocks.ORES)) {
                            solidShellCells++;
                        } else if (state.isAir()) {
                            for (Direction direction : Direction.values()) {
                                neighbor.setWithOffset(pos, direction);
                                if (!level.getBlockState(neighbor).getFluidState().isEmpty()) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return shellCells > 0 && solidShellCells >= shellCells * MIN_SOLID_SHELL;
    }

    private static void carve(WorldGenLevel level, BlockPos origin, byte[] grid, RandomSource random, BlockState rock) {
        MutableBlockPos pos = new MutableBlockPos();
        boolean deep = origin.getY() < 0;
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    byte cell = grid[index(dx, dy, dz)];
                    if (cell == OUTSIDE) {
                        continue;
                    }
                    pos.setWithOffset(origin, dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (cell == INSIDE) {
                        if (!state.isAir()) {
                            level.setBlock(pos, Blocks.CAVE_AIR.defaultBlockState(), 2);
                        }
                    } else if (isHostRock(state)) {
                        level.setBlock(pos, pickWallBlock(random, deep, rock), 2);
                    }
                }
            }
        }
    }

    private static void placePools(WorldGenLevel level, BlockPos origin, byte[] grid, RandomSource random, BlockState rock) {
        MutableBlockPos pos = new MutableBlockPos();
        int limit = RADIUS - 4;
        for (int dx = -limit; dx <= limit; dx++) {
            for (int dy = -RADIUS + 1; dy <= RADIUS; dy++) {
                for (int dz = -limit; dz <= limit; dz++) {
                    if (grid[index(dx, dy, dz)] == INSIDE && grid[index(dx, dy - 1, dz)] != INSIDE && random.nextFloat() < POOL_CHANCE) {
                        pos.setWithOffset(origin, dx, dy - 1, dz);
                        if (isSolid(level, pos)) {
                            tryPool(level, pos.immutable(), 1 + random.nextInt(2), rock);
                        }
                    }
                }
            }
        }
    }

    private static void tryPool(WorldGenLevel level, BlockPos center, int radius, BlockState rock) {
        Set<BlockPos> water = new HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius + 1) {
                    BlockPos pos = center.offset(dx, 0, dz);
                    if (isSolid(level, pos) && level.getBlockState(pos.above()).isAir()) {
                        water.add(pos);
                    }
                }
            }
        }
        boolean changed = true;
        while (changed && !water.isEmpty()) {
            changed = water.removeIf(pos -> !isContained(level, pos, water));
        }
        if (water.size() < 2) {
            return;
        }
        for (BlockPos pos : water) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
        }
        for (BlockPos pos : water) {
            for (Direction direction : HORIZONTAL) {
                BlockPos rim = pos.relative(direction);
                if (!water.contains(rim)) {
                    BlockState state = level.getBlockState(rim);
                    if (isHostRock(state) || isCaveMineral(state)) {
                        level.setBlock(rim, rock, 2);
                    }
                }
            }
        }
    }

    private static boolean isContained(WorldGenLevel level, BlockPos pos, Set<BlockPos> water) {
        if (!isSolid(level, pos.below())) {
            return false;
        }
        for (Direction direction : HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (!water.contains(side) && !isSolid(level, side)) {
                return false;
            }
        }
        return true;
    }

    private static void decorate(WorldGenLevel level, BlockPos origin, byte[] grid, RandomSource random) {
        MutableBlockPos pos = new MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = RADIUS; dy >= -RADIUS; dy--) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (grid[index(dx, dy, dz)] != INSIDE) {
                        continue;
                    }
                    pos.setWithOffset(origin, dx, dy, dz);
                    if (!level.getBlockState(pos).isAir()) {
                        continue;
                    }
                    BlockPos above = pos.above();
                    BlockPos below = pos.below();
                    if (level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN) && random.nextFloat() < CEILING_DRIP_CHANCE) {
                        placeFormation(level, pos.immutable(), Direction.DOWN, random);
                    } else if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && random.nextFloat() < FLOOR_DRIP_CHANCE) {
                        placeFormation(level, pos.immutable(), Direction.UP, random);
                    }
                }
            }
        }
    }

    /**
     * 1.4.1's stalactite shapes as buds: a column becomes a cluster top and bottom, a two-block drip one cluster, the
     * small drips a bud of random size. A bud's facing is the tip's direction, as the amethyst block's.
     */
    private static void placeFormation(WorldGenLevel level, BlockPos pos, Direction tip, RandomSource random) {
        int gap = airRun(level, pos, tip, 4);
        if (tip == Direction.DOWN && gap == 3 && isSturdy(level, pos.below(3), Direction.UP) && random.nextFloat() < 0.35F) {
            setBud(level, pos, CLUSTER_ID, Direction.DOWN);
            setBud(level, pos.below(2), CLUSTER_ID, Direction.UP);
        } else if (gap >= 2 && random.nextFloat() < 0.4F) {
            setBud(level, pos, CLUSTER_ID, tip);
        } else {
            int roll = random.nextInt(3);
            setBud(level, pos, roll == 0 ? LARGE_ID : roll == 1 ? MEDIUM_ID : SMALL_ID, tip);
        }
    }

    private static void setBud(WorldGenLevel level, BlockPos pos, ResourceLocation id, Direction facing) {
        BlockState bud = state(id);
        if (bud != null && bud.hasProperty(BlockStateProperties.FACING)) {
            level.setBlock(pos, bud.setValue(BlockStateProperties.FACING, facing), 2);
        }
    }

    private static int airRun(WorldGenLevel level, BlockPos start, Direction direction, int max) {
        int run = 0;
        MutableBlockPos pos = start.mutable();
        while (run < max && level.getBlockState(pos).isAir()) {
            run++;
            pos.move(direction);
        }
        return run;
    }

    /** 1.4.1 rolled salt 70 % (its licked stages among them), then calcite and tuff; deep caves favour tuff. */
    private static BlockState pickWallBlock(RandomSource random, boolean deep, BlockState rock) {
        int roll = random.nextInt(100);
        if (roll < 70) {
            return rock;
        }
        if (roll < 88) {
            return deep ? Blocks.TUFF.defaultBlockState() : Blocks.CALCITE.defaultBlockState();
        }
        return deep ? Blocks.CALCITE.defaultBlockState() : Blocks.TUFF.defaultBlockState();
    }

    private static boolean isHostRock(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    private static boolean isCaveMineral(BlockState state) {
        return state.is(Blocks.CALCITE) || state.is(Blocks.TUFF) || ROCK_ID.equals(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
    }

    private static boolean isSolid(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && state.isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isSturdy(WorldGenLevel level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, face);
    }

    private static int index(int dx, int dy, int dz) {
        return ((dx + RADIUS) * SIZE + dy + RADIUS) * SIZE + dz + RADIUS;
    }

    private record Chamber(double x, double y, double z, double radiusH, double radiusV) {
        double distance(double px, double py, double pz, double grow) {
            double h = radiusH + grow;
            double v = (py < y ? radiusV * 0.55D : radiusV) + grow;
            double ox = px - x;
            double oy = py - y;
            double oz = pz - z;
            return Math.sqrt((ox * ox + oz * oz) / (h * h) + oy * oy / (v * v));
        }
    }

    private record Tunnel(double ax, double ay, double az, double bx, double by, double bz, double radius) {
        double distance(double px, double py, double pz) {
            double sx = bx - ax;
            double sy = by - ay;
            double sz = bz - az;
            double lengthSqr = sx * sx + sy * sy + sz * sz;
            double t = lengthSqr == 0.0D ? 0.0D : ((px - ax) * sx + (py - ay) * sy + (pz - az) * sz) / lengthSqr;
            t = Math.max(0.0D, Math.min(1.0D, t));
            double cx = px - (ax + sx * t);
            double cy = py - (ay + sy * t);
            double cz = pz - (az + sz * t);
            return Math.sqrt(cx * cx + cy * cy + cz * cz);
        }
    }

    private record Layout(List<Chamber> chambers, List<Tunnel> tunnels, SimplexNoise noise) {
        static Layout create(RandomSource random) {
            List<Chamber> chambers = new ArrayList<>();
            List<Tunnel> tunnels = new ArrayList<>();
            Chamber main = new Chamber(0.0D, 0.0D, 0.0D, 8 + random.nextInt(3), 6 + random.nextInt(2));
            chambers.add(main);
            int lobes = 2 + random.nextInt(3);
            double startAngle = random.nextDouble() * Math.PI * 2.0D;
            for (int i = 0; i < lobes; i++) {
                double radiusH = 4 + random.nextInt(2);
                double angle = startAngle + (Math.PI * 2.0D) / lobes * i + (random.nextDouble() - 0.5D) * 0.8D;
                double distance = Math.min(maxDistance(radiusH), main.radiusH() - 1.0D + random.nextInt(3));
                chambers.add(new Chamber(Math.cos(angle) * distance, random.nextInt(3) - 1, Math.sin(angle) * distance, radiusH, 4.0D));
            }
            if (random.nextBoolean()) {
                boolean upper = random.nextBoolean();
                double radiusH = 5 + random.nextInt(2);
                double radiusV = 3 + random.nextInt(2);
                double x = random.nextInt(9) - 4;
                double z = random.nextInt(9) - 4;
                double y = upper ? main.radiusV() + 1.0D : -(main.radiusV() * 0.55D + radiusV + 1.0D);
                chambers.add(new Chamber(x, y, z, radiusH, radiusV));
                tunnels.add(new Tunnel(0.0D, upper ? 1.0D : -1.0D, 0.0D, x, y, z, 2.0D + random.nextDouble() * 0.5D));
            }
            return new Layout(chambers, tunnels, new SimplexNoise(random));
        }

        private static double maxDistance(double radiusH) {
            return 15.0D - (radiusH + SHELL_THICKNESS) * 1.14D;
        }

        byte classify(int dx, int dy, int dz) {
            double wobble = 1.0D + noise.getValue(dx * 0.18D, dy * 0.25D, dz * 0.18D) * 0.14D;
            boolean shell = false;
            for (Chamber chamber : chambers) {
                if (chamber.distance(dx, dy, dz, 0.0D) <= wobble) {
                    return INSIDE;
                }
                if (chamber.distance(dx, dy, dz, SHELL_THICKNESS) <= wobble) {
                    shell = true;
                }
            }
            for (Tunnel tunnel : tunnels) {
                double distance = tunnel.distance(dx, dy, dz);
                if (distance <= tunnel.radius() * wobble) {
                    return INSIDE;
                }
                if (distance <= (tunnel.radius() + SHELL_THICKNESS) * wobble) {
                    shell = true;
                }
            }
            return shell ? SHELL : OUTSIDE;
        }
    }
}
