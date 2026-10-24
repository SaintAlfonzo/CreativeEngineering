package com.sammy.minersdelight.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

/** Silverfish Eggs (a food) also infest the stone they are used on. */
public class SilverfishEggsItem extends Item {
    public SilverfishEggsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (!InfestedBlock.isCompatibleHostBlock(state)) {
            return super.useOn(context);
        }
        if (player != null) {
            player.swing(context.getHand());
        }
        if (!level.isClientSide) {
            BlockState infested = InfestedBlock.infestedStateByHost(state);
            level.setBlock(pos, infested, Block.UPDATE_ALL);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(infested));
            if (player == null || !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
