package com.SaintAlfonzo.creativedelight.hearth.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Wild hops (plan_bugfix_1114 3.1; modelled on Vinery's JungleGrapeFeature): around the placement origin, finds the tree
 * trunks and hangs a vine of the configured state down one face of one of them, 2 to 5 blocks long, ending above the
 * ground. One tree per placement; nothing when no trunk is found. The vine's {@code age} (if any) is random 1 to 3. The
 * block is named by id and looked up when placing, so the configured feature parses without it: the hops vine exists
 * only with Vinery loaded, and with it absent the feature places nothing. It must be a VineBlock, so the face properties
 * exist.
 * <p>
 * Trunk search (plan_post_scratch_1115 C3, C1 findings): every column of the 17 x 17 square (radius 8) around the
 * origin. A trunk is a column whose top block ignoring leaves ({@code MOTION_BLOCKING_NO_LEAVES}, live during features,
 * so trees placed earlier in the chunk count) is in {@code #minecraft:logs}, walked down to its lowest log; kept when it
 * is at least 3 logs high and has an empty block beside it somewhere along its height. One kept trunk is picked at
 * random (the next one is tried if no vine fits). Was 24 random columns within 6 in a y window of -6 to +10 around the
 * origin, which found a trunk about 1 time in 25 (SQ-HOP-06).
 */
public class TrunkVineFeature extends Feature<TrunkVineFeature.Config> {
    /** {@code {"block": "hearthandharvest:hops_vine"}} */
    public record Config(ResourceLocation block) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = ResourceLocation.CODEC.fieldOf("block").xmap(Config::new, Config::block).codec();
    }

    private static final int RADIUS = 8;
    private static final int MIN_LOGS = 3;

    public TrunkVineFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Block block = ForgeRegistries.BLOCKS.getValue(context.config().block());
        if (!(block instanceof VineBlock)) {
            return false;
        }
        BlockState vine = block.defaultBlockState();
        List<int[]> trunks = trunks(level, origin);
        while (!trunks.isEmpty()) {
            int[] t = trunks.remove(random.nextInt(trunks.size()));
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            for (int turn = 0; turn < 4; turn++, side = side.getClockWise()) {
                if (hang(level, random, vine, t[0], t[1], t[2], t[3], side)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {x, z, base, top} of every trunk in the square around the origin (see the class comment). */
    private static List<int[]> trunks(WorldGenLevel level, BlockPos origin) {
        List<int[]> out = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int min = level.getMinBuildHeight();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                if (!level.getBlockState(pos.set(x, top, z)).is(BlockTags.LOGS)) {
                    continue;
                }
                int base = top;
                while (base - 1 >= min && level.getBlockState(pos.set(x, base - 1, z)).is(BlockTags.LOGS)) {
                    base--;
                }
                if (top - base + 1 >= MIN_LOGS && freeSide(level, pos, x, z, base, top)) {
                    out.add(new int[]{x, z, base, top});
                }
            }
        }
        return out;
    }

    /** An empty block beside the trunk above its base, where a vine could hang. */
    private static boolean freeSide(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int base, int top) {
        for (int y = base + 1; y <= top; y++) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (level.isEmptyBlock(pos.set(x, y, z).move(side))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Vine blocks on the {@code side} face of the trunk column, from a random height down; true if any was placed. */
    private static boolean hang(WorldGenLevel level, RandomSource random, BlockState vine, int x, int z, int base, int top, Direction side) {
        BooleanProperty face = VineBlock.getPropertyForFace(side.getOpposite());
        int start = base + 1 + random.nextInt(Math.max(1, top - base));
        int length = 2 + random.nextInt(4);
        boolean placed = false;
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int y = start; y > start - length && y > base; y--) {
            at.set(x, y, z).move(side);
            if (!level.isEmptyBlock(at)) {
                break;
            }
            BlockState state = withAge(vine.setValue(face, true), random);
            if (!state.canSurvive(level, at)) {
                break;
            }
            level.setBlock(at, state, 2);
            placed = true;
        }
        return placed;
    }

    private static BlockState withAge(BlockState state, RandomSource random) {
        for (Property<?> property : state.getProperties()) {
            if (property == BlockStateProperties.AGE_3 && property instanceof IntegerProperty age) {
                return state.setValue(age, 1 + random.nextInt(3));
            }
        }
        return state;
    }
}
