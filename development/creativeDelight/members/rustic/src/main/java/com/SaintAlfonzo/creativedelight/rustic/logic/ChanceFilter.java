package com.SaintAlfonzo.creativedelight.rustic.logic;

import com.SaintAlfonzo.creativedelight.rustic.registry.RDLoot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Map;

/**
 * Placement modifier {@code rusticdelight:configurable_rarity_filter}: passes one chunk in N, N named by {@code option}.
 * The port has no worldgen config: N is upstream 1.7.0's default for each option, except wild cotton, which keeps the
 * pack's 1.3.0 value of 96 (1.7.0: 32), and the giant pepper patch, which is 64 (1.7.0: 16, owner 2026-10-25: fairly rare). Where the pack places each patch is set by
 * Creative Delight's kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json.
 */
public final class ChanceFilter extends PlacementFilter {
    private static final Map<String, Integer> DEFAULTS = Map.of("wild_cotton_chance", 96, "wild_bell_peppers_chance", 15,
            "wild_coffee_chance", 15, "bell_pepper_block_patch_chance", 64);
    public static final Codec<ChanceFilter> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("option").forGetter(f -> f.option)).apply(inst, ChanceFilter::new));

    private final String option;

    private ChanceFilter(String option) {
        this.option = option;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        int chance = DEFAULTS.getOrDefault(option, 0);
        return chance > 0 && random.nextFloat() < 1.0F / chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return RDLoot.CHANCE_FILTER.get();
    }
}
