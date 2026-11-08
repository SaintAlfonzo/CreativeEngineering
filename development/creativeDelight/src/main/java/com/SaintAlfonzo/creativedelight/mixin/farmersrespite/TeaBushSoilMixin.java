package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import com.SaintAlfonzo.creativedelight.crops.CropParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.SmallTeaBushBlock;
import umpaz.farmersrespite.common.block.TeaBushBlock;

/**
 * Farmer's Respite's small and full tea bushes override {@code mayPlaceOn} ({@code m_6266_}) with
 * {@code state.is(BlockTags.DIRT)}, which refuses Farmland and Rich Farmland (crops.md G12). A refused soil is
 * accepted when {@link CropParity#isFarmlandSoil} takes it, as for tobacco. Placement and survival
 * ({@code canSurvive} of the lower half) both go through this method. Untilled dirt and the other dirt-tag
 * blocks keep working as before.
 */
@Mixin(value = {SmallTeaBushBlock.class, TeaBushBlock.class}, remap = false)
public abstract class TeaBushSoilMixin {

    @Inject(method = "m_6266_", at = @At("RETURN"), cancellable = true, require = 1, remap = false)
    private void creativeengineering$farmlandSoils(BlockState soil, BlockGetter level, BlockPos pos,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && CropParity.isFarmlandSoil(soil)) cir.setReturnValue(true);
    }
}
