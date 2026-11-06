package com.SaintAlfonzo.creativedelight.mixin.the_dirty_stuff;

import com.SaintAlfonzo.creativedelight.crops.CropParity;
import net.mcreator.thedirtystuff.procedures.TobaccoSeedsBlockValidPlacementConditionProcedure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Dirty Stuff's tobacco ({@code the_dirty_stuff:tobacco_seeds}, a plain MCreator block) only
 * survives on {@code block below == Blocks.FARMLAND}, an identity check that refuses Rich Farmland
 * and Fertile Soil. TobaccoSeedsBlock.canSurvive is this procedure's only caller, so replacing its
 * answer covers placement, survival and neighbour updates. Soils come from
 * {@link CropParity#FARMLAND_SOILS} (vanilla farmland always counts).
 */
@Mixin(value = TobaccoSeedsBlockValidPlacementConditionProcedure.class, remap = false)
public abstract class TobaccoPlacementMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$farmlandSoils(LevelAccessor world, double x, double y, double z,
                                                          CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CropParity.isFarmlandSoil(world.getBlockState(new BlockPos(x, y - 1.0, z))));
    }
}
