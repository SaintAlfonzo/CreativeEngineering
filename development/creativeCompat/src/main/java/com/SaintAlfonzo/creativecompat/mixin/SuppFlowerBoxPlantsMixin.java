package com.SaintAlfonzo.creativecompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.SaintAlfonzo.creativecompat.compat.PlantBoxModels;

import net.mehvahdjukaar.supplementaries.common.block.tiles.FlowerBoxBlockTile;
import net.minecraft.world.item.ItemStack;

/**
 * Supplementaries' flower box refuses {@code #creativecompat:not_in_flower_boxes} (azalea and flowering azalea,
 * which it takes through its special model list). Boxes already holding one keep it and still draw it.
 * flower_pots.md 3.2.
 */
@Mixin(value = FlowerBoxBlockTile.class, remap = false)
public abstract class SuppFlowerBoxPlantsMixin {

    @Inject(method = "m_7013_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$notInBoxes(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(PlantBoxModels.NOT_IN_BOXES)) cir.setReturnValue(false);
    }
}
