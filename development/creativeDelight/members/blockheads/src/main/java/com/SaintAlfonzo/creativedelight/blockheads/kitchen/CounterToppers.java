package com.SaintAlfonzo.creativedelight.blockheads.kitchen;

import com.SaintAlfonzo.creativedelight.blockheads.BAConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Small blocks on a Cooking for Blockheads counter (TODO cooking/kitchen #8, #20): Farmer's Delight's cutting board,
 * Display Delight's plates and plated foods (and the pack's own dishes, which are Display Delight block classes), Brewin' and Chewin's coaster ({@code #creativeengineering:counter_toppers}).
 * The counter and corner ({@code #creativeengineering:counter_supports}) are 15 px tall with no full top face, so these
 * refused to stay on them, and would float 1 px above the worktop. {@code BlockStateCounterTopperMixin} lets them stay
 * ({@code canSurvive}), breaks them (and every Display Delight block) when the block below goes ({@code updateShape}),
 * and shifts their model ({@code getOffset}); {@code CuttingBoardRendererOffsetMixin} and
 * {@code CoasterRendererOffsetMixin} shift what their block entity renderers draw. The shift is
 * {@code [kitchen] cuttingBoardOffset*} ({@code blockheads_additions-common.toml}) for the board and {@code plateOffset*} for the rest. The hitbox is not moved.
 */
public final class CounterToppers {

    public static final TagKey<Block> SUPPORTS =
            BlockTags.create(new ResourceLocation("creativeengineering", "counter_supports"));
    public static final TagKey<Block> TOPPERS =
            BlockTags.create(new ResourceLocation("creativeengineering", "counter_toppers"));
    private static final ResourceLocation BOARD_ID = new ResourceLocation("farmersdelight", "cutting_board");
    private static final String DISPLAY_DELIGHT = "displaydelight";
    /** Display Delight's (and its port's) block classes; patcherMod's own dish blocks are of these too. */
    private static final String DISPLAY_DELIGHT_PACKAGE = "com.jkvin114.displaydelight.";

    private static final Object NO_BOARD = new Object();

    private static volatile Object board;
    /** Bumped on every tag (re)load; the per-state topper cache in the mixin is valid for one generation. Starts at 1. */
    private static volatile int generation = 1;

    private CounterToppers() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> generation++);
    }

    public static int generation() {
        return generation;
    }

    /**
     * FD's cutting board block, compared by reference (looked up once; no FD class is referenced). Without FD it is a
     * sentinel no block equals: the registry's getValue would answer air for a missing id.
     */
    public static Object board() {
        Object b = board;
        if (b == null) {
            b = ForgeRegistries.BLOCKS.containsKey(BOARD_ID) ? ForgeRegistries.BLOCKS.getValue(BOARD_ID) : NO_BOARD;
            board = b;
        }
        return b;
    }

    /**
     * Whether {@code state} breaks when the block below changes and it can no longer survive: every topper, and every
     * Display Delight block (its plates, plated foods, bowls and drinks check survival only when placed). The board and
     * the coaster already do this in their own {@code updateShape}.
     */
    public static boolean popsUnsupported(BlockState state) {
        if (state.is(TOPPERS)) return true;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && (DISPLAY_DELIGHT.equals(id.getNamespace())
                || state.getBlock().getClass().getName().startsWith(DISPLAY_DELIGHT_PACKAGE));
    }

    /** Whether a topper at {@code pos} sits on a support (and so may stay there). */
    public static boolean onSupport(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(SUPPORTS);
    }

    /** Offset in blocks for a topper {@code block} at {@code pos}: the configured shift when it sits on a support, else zero. */
    public static Vec3 offset(Block block, BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(SUPPORTS)) {
            return Vec3.ZERO;
        }
                boolean isBoard = block == board();
        double x = (isBoard ? BAConfig.CUTTING_BOARD_OFFSET_X : BAConfig.PLATE_OFFSET_X).get() / 16.0;
        double y = (isBoard ? BAConfig.CUTTING_BOARD_OFFSET_Y : BAConfig.PLATE_OFFSET_Y).get() / 16.0;
        double z = (isBoard ? BAConfig.CUTTING_BOARD_OFFSET_Z : BAConfig.PLATE_OFFSET_Z).get() / 16.0;
        // Config frame = a counter facing north (+z toward its back); turn it clockwise with the counter.
        return switch (facing(below)) {
            case SOUTH -> new Vec3(-x, y, -z);
            case EAST -> new Vec3(-z, y, x);
            case WEST -> new Vec3(z, y, -x);
            default -> new Vec3(x, y, z);
        };
    }

    private static Direction facing(BlockState state) {
        Property<?> p = state.getBlock().getStateDefinition().getProperty("facing");
        return p instanceof DirectionProperty d ? state.getValue(d) : Direction.NORTH;
    }
}
