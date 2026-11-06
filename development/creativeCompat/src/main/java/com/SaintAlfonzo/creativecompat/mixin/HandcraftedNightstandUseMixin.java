package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.NightstandStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Right-clicking a Handcrafted 2.0.6 nightstand (Every Compat's too, a subclass) opens its chest storage
 * ({@link NightstandStorage}); the storage drops when the block is broken. Handcrafted's own {@code use} (a sheet
 * placed on top, taken off with sneak) runs first and is kept: the chest opens only when it answers PASS, and not for
 * a block item on the top face, so lamps and candles can still be placed on it. Neither NightstandBlock nor
 * SimpleEntityBlock declares onRemove, so it is added. SRG name with {@code remap = false}: m_6227_ use.
 */
@Pseudo
@Mixin(targets = "earth.terrarium.handcrafted.common.block.table.nightstand.NightstandBlock", remap = false)
public abstract class HandcraftedNightstandUseMixin extends BaseEntityBlock {

    protected HandcraftedNightstandUseMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "m_6227_", at = @At("RETURN"), cancellable = true)
    private void ce$openStorage(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.PASS) return;
        if (hit.getDirection() == Direction.UP && player.getItemInHand(hand).getItem() instanceof BlockItem) return;
        if (!(level.getBlockEntity(pos) instanceof NightstandStorage.Holder holder)) return;
        if (!level.isClientSide) {
            player.openMenu(NightstandStorage.menu(holder.ce$nightstandStorage(), state.getBlock().getName()));
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NightstandStorage.Holder holder) {
            Containers.dropContents(level, pos, holder.ce$nightstandStorage());
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
