package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.food.PlacedFoodSol;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bites from placed pies and cakes count for Spice of Life ({@link PlacedFoodSol}, TODO food/types-and-tuning #5).
 * Remembers the clicked block's state and the held item at HEAD, then compares at RETURN. Server side only; one block
 * lookup per right-click on a block. SRG name with {@code remap = false}: m_7179_ useItemOn.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModePlacedFoodMixin {

    @Unique private BlockState ce$stateBefore;
    @Unique private ItemStack ce$heldBefore = ItemStack.EMPTY;

    @Inject(method = "m_7179_", at = @At("HEAD"), remap = false)
    private void ce$rememberState(ServerPlayer player, Level level, ItemStack held, InteractionHand hand,
                                  BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ce$stateBefore = level.getBlockState(hit.getBlockPos());
        ce$heldBefore = held.copy();
    }

    @Inject(method = "m_7179_", at = @At("RETURN"), remap = false)
    private void ce$placedFoodBite(ServerPlayer player, Level level, ItemStack held, InteractionHand hand,
                                   BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlockState before = ce$stateBefore;
        ce$stateBefore = null;
        if (before == null) return;
        PlacedFoodSol.afterUse(player, level, hit.getBlockPos(), before, ce$heldBefore, cir.getReturnValue());
        ce$heldBefore = ItemStack.EMPTY;
    }
}
