package com.SaintAlfonzo.creativedelight.hearth.item;

import com.SaintAlfonzo.creativedelight.hearth.block.HHBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/** Edible, and planted on soil as a stem (N23-16, B7 fixed). */
public class SunflowerSeedItem extends ConsumableItem {
    public SunflowerSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().above();
        BlockState stem = HHBlocks.SUNFLOWER_STEM.get().defaultBlockState();
        if (context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos) || !stem.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, stem, 11);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
