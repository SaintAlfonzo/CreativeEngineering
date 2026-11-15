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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Wild hops (plan_bugfix_1114 3.1; modelled on Vinery's JungleGrapeFeature): from the placement origin, looks for one tree
 * trunk (a log in #minecraft:logs with air beside it) within 6 blocks and hangs a vine of the configured state down one face
 * of it, 2 to 5 blocks long, ending above the ground. One tree per placement; nothing when no trunk is found. The vine's
 * {@code age} (if any) is random 1 to 3. The block is named by id and looked up when placing, so the configured feature parses
 * without it: the hops vine exists only with Vinery loaded, and with it absent the feature places nothing. It must be a
 * VineBlock, so the face properties exist.
 */
public class TrunkVineFeature extends Feature<TrunkVineFeature.Config> {
    /** {@code {"block": "hearthandharvest:hops_vine"}} */
    public record Config(ResourceLocation block) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = ResourceLocation.CODEC.fieldOf("block").xmap(Config::new, Config::block).codec();
    }

    private static final int RADIUS = 6;
    private static final int COLUMNS = 24;
    private static final int UP = 10;
    private static final int DOWN = 6;

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
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int c = 0; c < COLUMNS; c++) {
            int x = origin.getX() + random.nextInt(RADIUS * 2 + 1) - RADIUS;
            int z = origin.getZ() + random.nextInt(RADIUS * 2 + 1) - RADIUS;
            // lowest log of the column near the origin height: the trunk base
            int base = Integer.MIN_VALUE;
            for (int y = origin.getY() - DOWN; y <= origin.getY() + UP; y++) {
                pos.set(x, y, z);
                if (level.getBlockState(pos).is(BlockTags.LOGS) && !level.getBlockState(pos.below()).is(BlockTags.LOGS)) {
                    base = y;
                    break;
                }
            }
            if (base == Integer.MIN_VALUE) {
                continue;
            }
            int top = base;
            while (top < base + UP && level.getBlockState(pos.set(x, top + 1, z)).is(BlockTags.LOGS)) {
                top++;
            }
            if (top - base < 2) {
                continue;
            }
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            for (int turn = 0; turn < 4; turn++, side = side.getClockWise()) {
                if (hang(level, random, vine, x, z, base, top, side)) {
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
