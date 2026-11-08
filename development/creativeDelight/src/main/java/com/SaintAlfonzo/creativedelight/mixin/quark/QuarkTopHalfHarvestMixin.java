package com.SaintAlfonzo.creativedelight.mixin.quark;

import com.SaintAlfonzo.creativedelight.crops.CropParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.tweaks.module.SimpleHarvestModule;

/**
 * Corn's top half is the same block as the lower half with {@code upper=true}, but Quark's harvest search only maps
 * the lower half's ripe state, so a right-click on the top half did nothing (crops.md G13). Quark's per-block
 * {@code handle} is pointed at the lower half when the clicked block is the top half of a
 * {@link CropParity#HARVEST_FROM_BOTTOM} crop. Every path goes through {@code handle}: the empty hand, a 1x1 hoe, the
 * area hoe loop ({@code QuarkHoeAreaHarvestMixin}) and the server side of Quark's harvest packet. The replant
 * resets the lower half and the top breaks on its own (survival check), dropping its loot as before.
 */
@Mixin(value = SimpleHarvestModule.class, remap = false)
public abstract class QuarkTopHalfHarvestMixin {

    @Shadow
    private static boolean handle(Player player, InteractionHand hand, BlockPos pos, boolean doRightClick, boolean isHoe) {
        throw new AssertionError();
    }

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void creativeengineering$topHalfToLower(Player player, InteractionHand hand, BlockPos pos,
                                                           boolean doRightClick, boolean isHoe,
                                                           CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        BlockPos below = pos.below();
        if (CropParity.isTopHalf(player.level.getBlockState(pos), player.level.getBlockState(below))) {
            cir.setReturnValue(handle(player, hand, below, doRightClick, isHoe));
        }
    }
}
