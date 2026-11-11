package com.SaintAlfonzo.creativedelight.compat.cookingforblockheads.kitchen;

import com.SaintAlfonzo.creativedelight.CreativeDelight;
import com.SaintAlfonzo.creativedelight.CDConfig;

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
 * Display Delight's plates and plated foods (and the pack's own dishes, which are Display Delight block classes), Brewin' and Chewin's coaster ({@code #creativedelight:counter_toppers}).
 * The counter and corner ({@code #creativedelight:counter_supports}) are 15 px tall with no full top face, so these
 * refused to stay on them, and would float 1 px above the worktop. {@code BlockStateCounterTopperMixin} lets them stay
 * ({@code canSurvive}), breaks them (and every Display Delight block) when the block below goes ({@code updateShape}),
 * and shifts their model ({@code getOffset}); {@code CuttingBoardRendererOffsetMixin} and
 * {@code CoasterRendererOffsetMixin} shift what their block entity renderers draw. The shift is
 * {@code cuttingBoardOffset*} ({@code creativedelight-common.toml [cookingforblockheads]}) for the board and {@code plateOffset*} for the rest. The hitbox is not moved.
 */
public final class CounterToppers {

    public static final TagKey<Block> SUPPORTS =
            BlockTags.create(new ResourceLocation(CreativeDelight.MOD_ID, "counter_supports"));
    public static final TagKey<Block> TOPPERS =
            BlockTags.create(new ResourceLocation(CreativeDelight.MOD_ID, "counter_toppers"));
    private static final String BOARD_CLASS = "vectorwing.farmersdelight.common.block.CuttingBoardBlock";
    private static final String DISPLAY_DELIGHT = "displaydelight";
    /** Display Delight's (and its port's) block classes; patcherMod's own dish blocks are of these too. */
    private static final String DISPLAY_DELIGHT_PACKAGE = "com.jkvin114.displaydelight.";

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
     * Whether {@code block} is a Farmer's Delight cutting board: FD's own and the CD stone and wood boards, which all use
     * FD's block class (compared by name, so no FD class is referenced).
     */
    public static boolean isBoard(Block block) {
        return BOARD_CLASS.equals(block.getClass().getName());
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
        boolean isBoard = isBoard(block);
        double x = (isBoard ? CDConfig.CFB.cuttingBoardOffsetX : CDConfig.CFB.plateOffsetX).get() / 16.0;
        double y = (isBoard ? CDConfig.CFB.cuttingBoardOffsetY : CDConfig.CFB.plateOffsetY).get() / 16.0;
        double z = (isBoard ? CDConfig.CFB.cuttingBoardOffsetZ : CDConfig.CFB.plateOffsetZ).get() / 16.0;
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
