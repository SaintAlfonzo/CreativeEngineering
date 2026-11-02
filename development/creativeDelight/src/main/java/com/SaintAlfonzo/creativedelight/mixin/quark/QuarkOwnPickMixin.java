package com.SaintAlfonzo.creativedelight.mixin.quark;

import com.SaintAlfonzo.creativedelight.crops.CropParity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.api.event.SimpleHarvestEvent.ActionType;
import vazkii.quark.content.tweaks.module.SimpleHarvestModule;

/**
 * Quark's harvesting search takes every modded {@code CropBlock}, and its right-click event runs before the block's
 * own {@code use}: a ripe cucumber was broken for its loot and replanted at stage 0 instead of picked back to stage 5.
 * Crops in {@link CropParity#OWN_PICK_CROPS} get Quark's berry bush treatment instead: no action for an empty hand or
 * a single-block hoe (Quark lets the click through to the block), a click on each in a hoe area.
 */
@Mixin(value = SimpleHarvestModule.class, remap = false)
public abstract class QuarkOwnPickMixin {

    @Inject(method = "getAction", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private static void creativeengineering$ownPick(BlockState state, boolean doRightClick, CallbackInfoReturnable<ActionType> cir) {
        if (CropParity.isOwnPickCrop(state)) {
            cir.setReturnValue(doRightClick ? ActionType.CLICK : ActionType.NONE);
        }
    }
}
