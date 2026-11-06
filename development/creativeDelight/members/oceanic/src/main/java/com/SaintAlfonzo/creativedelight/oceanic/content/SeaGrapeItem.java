package com.SaintAlfonzo.creativedelight.oceanic.content;

import com.SaintAlfonzo.creativedelight.oceanic.registry.OceanicBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sea Grape (ocean_delight.md 5.6): a snack (Instant Breath), and the seed of the crop. Used on a block it plants a sea
 * grape plant in the still water above it, where one could stand and nothing grows yet; otherwise it is eaten.
 */
public class SeaGrapeItem extends OceanicFood {
    public SeaGrapeItem(Builder builder) {
        super(builder);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos at = context.getClickedPos().above();
        BlockState there = level.getBlockState(at);
        BlockState plant = OceanicBlocks.SEA_GRAPE_PLANT.get().defaultBlockState().setValue(WaterloggedPlantBlock.WATERLOGGED, true);
        boolean taken = there.is(OceanicBlocks.SEA_GRAPE_PLANT.get()) || there.is(Blocks.KELP_PLANT) || there.is(Blocks.KELP)
                || (there.hasProperty(WaterloggedPlantBlock.WATERLOGGED) && there.getValue(WaterloggedPlantBlock.WATERLOGGED));
        if (taken || !plant.canSurvive(level, at)) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            level.setBlock(at, plant, 3);
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            level.playSound(null, at, SoundEvents.WET_GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
