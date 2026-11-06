package com.SaintAlfonzo.creativedelight.enders.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** The End plants' ground: the two soil tags (filled by the pack's scripts) and their growth tier. */
public final class EndSoil {
    public static final TagKey<Block> SLOW = BlockTags.create(new ResourceLocation("creativedelight", "end_plant_soil_slow"));
    public static final TagKey<Block> FAST = BlockTags.create(new ResourceLocation("creativedelight", "end_plant_soil_fast"));

    private EndSoil() {
    }

    public static boolean isSoil(BlockState state) {
        return state.is(FAST) || state.is(SLOW);
    }

    public static boolean isFast(BlockState state) {
        return state.is(FAST);
    }
}
