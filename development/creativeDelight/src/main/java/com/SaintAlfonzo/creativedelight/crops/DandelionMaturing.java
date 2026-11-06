package com.SaintAlfonzo.creativedelight.crops;

import com.SaintAlfonzo.creativedelight.CDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Plain Dandelion to Veggies Delight's Mature Dandelion, driven by the random ticks of the soil under it (Rich Soil,
 * Rich Soil Farmland, Fertile Soil); odds in {@link CDConfig.DandelionMaturing}. Called from
 * {@code mixin/DandelionSoilMixin}. The dandelion block itself never ticks, so nothing is paid by other flowers.
 */
public final class DandelionMaturing {

    private static final ResourceLocation MATURE_ID = new ResourceLocation("veggiesdelight", "mature_dandelion");

    private DandelionMaturing() {}

    /** @param richSoil true for Farmer's Delight soils, false for Fertile Soil */
    public static void soilTick(ServerLevel level, BlockPos soilPos, RandomSource random, boolean richSoil) {
        int chance = richSoil ? CDConfig.DANDELION_MATURING.richSoilTicks.get()
                : CDConfig.DANDELION_MATURING.fertileSoilTicks.get();
        if (chance <= 0) return;
        BlockPos above = soilPos.above();
        if (!level.getBlockState(above).is(Blocks.DANDELION) || random.nextInt(chance) != 0) return;
        Block mature = ForgeRegistries.BLOCKS.getValue(MATURE_ID);
        if (mature == null || mature == Blocks.AIR) return;
        BlockState state = mature.defaultBlockState();
        level.setBlock(above, state, Block.UPDATE_ALL);
    }
}
