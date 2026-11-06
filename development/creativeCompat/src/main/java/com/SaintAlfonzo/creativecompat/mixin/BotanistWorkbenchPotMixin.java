package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.BotanistPot;
import com.SaintAlfonzo.creativecompat.compat.BotanistPotBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Beautify 1.4.3's botanist workbench becomes a flower pot ({@link BotanistPot}): a block entity for the plant, the
 * vanilla pot's click rules, and the plant dropped when the block goes. BotanistWorkbench declares none of these
 * methods, so they are added rather than replaced.
 */
@Pseudo
@Mixin(targets = "com.github.Pandarix.beautify.common.block.BotanistWorkbench", remap = false)
public abstract class BotanistWorkbenchPotMixin extends HorizontalDirectionalBlock implements EntityBlock {

    protected BotanistWorkbenchPotMixin(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BotanistPotBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return BotanistPot.use(level, pos, player, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) BotanistPot.dropPlant(level, pos);
        super.onRemove(state, level, pos, newState, moved);
    }
}
