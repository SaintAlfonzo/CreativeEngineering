package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.CropParity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.tweaks.module.HoeHarvestingModule;

/**
 * Quark's hoe area break (breaking a plant with a hoe breaks the plants around it) only takes
 * Forge {@code IPlantable}s or replaceable plant materials. Tobacco is neither (material PLANT,
 * not replaceable), so it was skipped. Plants in {@link CropParity#FARMLAND_CROPS} now count, both
 * as the broken block that starts the area break and as the blocks around it.
 */
@Mixin(value = HoeHarvestingModule.class, remap = false)
public abstract class QuarkHoeHarvestMixin {

    @Inject(method = "canHarvest", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$farmlandCrops(Player player, LevelAccessor world, BlockPos pos, BlockState state,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (CropParity.isFarmlandCrop(state)) {
            cir.setReturnValue(true);
        }
    }
}
