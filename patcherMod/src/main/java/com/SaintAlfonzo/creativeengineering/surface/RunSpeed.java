package com.SaintAlfonzo.creativeengineering.surface;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CEConfig.SpeedFormat;

import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The walk speed a block really gives, for display (the WTHIT line, {@link WthitRunSpeed}). It uses
 * the same rules as movement: BlockRunner's own Block-to-speed map, then {@link StickyFacing} and
 * {@link CamoSpeed}. Cheap enough to call on every overlay refresh: one map lookup for the speed, a
 * block-entity read for frames only, and the tier from a cached Block-to-tier map that is cleared
 * whenever tags reload ({@link #clearTierCache}).
 */
public final class RunSpeed {

    private RunSpeed() {}

    /** Why the speed differs from the block's own tier, if it does. */
    public enum Note { NONE, FROM_CAMO, STICKY_FACE_NOT_UP }

    /** {@code tier} is the tier the speed comes from ({@code null} = none, e.g. a one-off json override). */
    public record Info(double factor, @Nullable SurfaceTier tier, Note note) {}

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

    public static Info at(Level level, BlockPos pos, BlockState state) {
        BlockState camo = CamoSpeed.speedCamo(level, pos, state);
        if (camo != null) {
            return new Info(factor(camo.getBlock()), tierOf(camo.getBlock()), Note.FROM_CAMO);
        }
        if (StickyFacing.cancels(state)) {
            return new Info(1.0, tierOf(state.getBlock()), Note.STICKY_FACE_NOT_UP);
        }
        return new Info(factor(state.getBlock()), tierOf(state.getBlock()), Note.NONE);
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
            text = String.format(Locale.ROOT, "%.2f\u00d7", factor);
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
