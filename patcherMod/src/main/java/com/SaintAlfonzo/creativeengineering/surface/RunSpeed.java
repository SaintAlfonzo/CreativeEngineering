package com.SaintAlfonzo.creativeengineering.surface;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CEConfig.SpeedFormat;

import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The walk speed a block really gives, for display (the WTHIT line, {@link WthitRunSpeed}). It uses
 * the same rules as movement: BlockRunner's own Block-to-speed map, then {@link StickyFacing} and
 * {@link CamoSpeed}; a block's own vanilla speed factor where BlockRunner has none (soul sand); and
 * {@link StuckInBlock} for blocks that slow you while you're inside them (cobwebs, leaf piles).
 *
 * <p>Which block counts follows BlockRunner, which reads the block 0.2 below your feet: a block whose
 * collision top is under 0.2 (carpet, 1-2 snow layers) shows the block beneath it, and a block with no
 * collision (plants) shows nothing unless it slows you from inside.
 *
 * <p>Cheap enough to call on every overlay refresh: one or two collision shapes, one map lookup for
 * the speed, a block-entity read for frames only, and the tier from a cached Block-to-tier map that is
 * cleared whenever tags reload ({@link #clearTierCache}).
 */
public final class RunSpeed {

    private RunSpeed() {}

    /** Where the number comes from. {@link #NONE} is an "untyped" block: nothing changes its speed. */
    public enum Source { TIER, OVERRIDE, NATIVE, IN_BLOCK, NONE }

    /** Extra context shown after the tier name. */
    public enum Note { FROM_CAMO, STICKY_FACE_NOT_UP, BLOCK_BELOW }

    /**
     * {@code tier} is the surface tier the speed comes from ({@code null} = none, e.g. a one-off json
     * override or a frame whose camo has no tier).
     */
    public record Info(double factor, Source source, @Nullable SurfaceTier tier, Set<Note> notes) {

        Info(double factor, Source source, @Nullable SurfaceTier tier) {
            this(factor, source, tier, Set.of());
        }

        public boolean typed() {
            return source != Source.NONE;
        }

        Info with(Note note) {
            EnumSet<Note> all = EnumSet.of(note);
            all.addAll(notes);
            return new Info(factor, source, tier, all);
        }
    }

    private static final Info UNTYPED = new Info(1.0, Source.NONE, null);

    /** BlockRunner (via vanilla getOnPos) reads the block 0.2F below your feet. */
    private static final double REGISTER_HEIGHT = 0.2F;

    private static final float DEFAULT_FRICTION = 0.6f;

    private static final double EPSILON = 1.0E-6;

    /** The tier tags, built on first use (a TagKey needs the block registry). */
    private static final class Tags {
        static final Map<SurfaceTier, TagKey<Block>> BY_TIER = new EnumMap<>(SurfaceTier.class);
        static {
            for (SurfaceTier t : SurfaceTier.values()) {
                BY_TIER.put(t, BlockTags.create(new ResourceLocation("blockrunner", "surface/" + t.key())));
            }
        }
    }

    /** Block -> its surface tier (empty = none). Filled on first look, cleared on tag reload. */
    private static final Map<Block, Optional<SurfaceTier>> TIER_OF = new ConcurrentHashMap<>();

    /**
     * The speed for walking on (or through) this block, or {@code null} when it has none to show: you
     * can't stand on it and it doesn't slow you from inside (grass, flowers, open doors).
     */
    @Nullable
    public static Info at(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        double top = standTop(level, pos, state, player);
        boolean standable = top >= REGISTER_HEIGHT;
        double stuck = StuckInBlock.horizontal(state, player, standable);
        boolean inBlock = !Double.isNaN(stuck);

        BlockPos floorPos = pos;
        BlockState floor = state;
        boolean onFloor = standable;
        Info surface;
        if (standable) {
            surface = surface(level, pos, state, player);
        } else if (inBlock || isThinLayer(state, top)) {
            floorPos = pos.below();
            floor = level.getBlockState(floorPos);
            onFloor = standTop(level, floorPos, floor, player) >= REGISTER_HEIGHT;
            surface = onFloor ? surface(level, floorPos, floor, player).with(Note.BLOCK_BELOW) : UNTYPED;
        } else {
            return null;
        }

        if (!inBlock) {
            return surface;
        }
        float friction = onFloor ? floor.getFriction(level, floorPos, player) : DEFAULT_FRICTION;
        return new Info(surface.factor() * StuckInBlock.walkRatio(stuck, friction), Source.IN_BLOCK, null);
    }

    /** What standing on top of this (standable) block gives. */
    private static Info surface(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        BlockState camo = CamoSpeed.speedCamo(level, pos, state);
        if (camo != null) {
            // frames are in the camouflage tier, so always typed, even when the camo has no tier
            return new Info(factor(camo.getBlock()), Source.TIER, tierOf(camo.getBlock()), Set.of(Note.FROM_CAMO));
        }
        if (StickyFacing.cancels(state)) {
            return new Info(1.0, Source.TIER, tierOf(state.getBlock()), Set.of(Note.STICKY_FACE_NOT_UP));
        }
        Block block = state.getBlock();
        if (BlockSpeedManager.INSTANCE.hasBlockSpeed(block)) {
            SurfaceTier tier = tierOf(block);
            return new Info(factor(block), tier != null ? Source.TIER : Source.OVERRIDE, tier);
        }
        float speedFactor = block.getSpeedFactor();
        if (!isNormal(speedFactor)) {
            return new Info(nativeRatio(speedFactor, state.getFriction(level, pos, player)), Source.NATIVE, null);
        }
        return UNTYPED;
    }

    /**
     * A vanilla speed factor {@code s} scales your momentum after every move, so steady walking is
     * {@code a / (1 - 0.91 f s)} against {@code a / (1 - 0.91 f)} normally (soul sand 0.4 -> 0.58x).
     */
    static double nativeRatio(double speedFactor, float friction) {
        double keep = 0.91 * friction;
        return (1.0 - keep) / (1.0 - keep * speedFactor);
    }

    /**
     * A solid layer too thin to count (carpet, 2 snow layers; {@code top} is under 0.2), or a single
     * snow layer, which has no collision at all but still lies on the block beneath.
     */
    private static boolean isThinLayer(BlockState state, double top) {
        return top > 0.0 || state.getBlock() instanceof SnowLayerBlock;
    }

    /** Top of the collision shape for someone standing on it, or -1 when you'd pass through. */
    private static double standTop(Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        VoxelShape shape = state.getCollisionShape(level, pos, new Standing(pos, player));
        return shape.isEmpty() ? -1.0 : shape.max(Direction.Axis.Y);
    }

    /**
     * Collision context for the player standing on top of {@code pos}, whatever their real position:
     * scaffolding then reports its solid top, and powder snow is solid only in leather boots.
     */
    private static final class Standing extends EntityCollisionContext {
        private static final Predicate<FluidState> NO_FLUID = fluid -> false;

        Standing(BlockPos pos, @Nullable Player player) {
            super(false, pos.getY() + 1.0,
                    player != null ? player.getMainHandItem() : ItemStack.EMPTY,
                    player != null ? player::canStandOnFluid : NO_FLUID,
                    player);
        }
    }

    @Nullable
    public static SurfaceTier tierOf(Block block) {
        return TIER_OF.computeIfAbsent(block, RunSpeed::findTier).orElse(null);
    }

    public static void clearTierCache() {
        TIER_OF.clear();
    }

    public static boolean isNormal(double factor) {
        return Math.abs(factor - 1.0) < EPSILON;
    }

    /** {@code 1.50×} or {@code +50%}; green when faster than normal, red when slower. */
    public static Component format(double factor, SpeedFormat format) {
        String text;
        if (format == SpeedFormat.PERCENT) {
            long percent = Math.round((factor - 1.0) * 100.0);
            text = (percent > 0 ? "+" : "") + percent + "%";
        } else {
            text = String.format(Locale.ROOT, "%.2f×", factor);
        }
        ChatFormatting color = isNormal(factor) ? ChatFormatting.WHITE
                : factor > 1.0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        return Component.literal(text).withStyle(color);
    }

    private static double factor(Block block) {
        return BlockSpeedManager.INSTANCE.getSpeedFactor(block);
    }

    private static Optional<SurfaceTier> findTier(Block block) {
        BlockState state = block.defaultBlockState();
        for (Map.Entry<SurfaceTier, TagKey<Block>> e : Tags.BY_TIER.entrySet()) {
            if (state.is(e.getValue())) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }
}
