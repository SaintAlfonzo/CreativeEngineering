package net.satisfy.foundation.banner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Everything that makes a mod's banner its own.
 *
 * @param effect            effect given to every player within {@code radius} blocks while the banner stands
 * @param durationTicks     effect length given at each refresh
 * @param refreshBelowTicks a player whose effect from the banner has this much or more left is skipped
 * @param showEffect        the item tooltip names the effect and radius
 */
public record BannerSettings(Supplier<BlockEntityType<CompletionistBannerEntity>> type, Supplier<Block> wallBanner, ResourceLocation texture, String tooltipPrefix, Supplier<MobEffect> effect, IntSupplier radius, IntSupplier amplifier,
                             IntSupplier durationTicks, IntSupplier refreshBelowTicks, BooleanSupplier showEffect) {
    public static final int DEFAULT_RADIUS = 8;
}
