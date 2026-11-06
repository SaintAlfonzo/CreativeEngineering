package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.farmersrespite.common.block.entity.inventory.KettleItemHandler;

/**
 * Farmer's Respite kettle, automation inserts (remainder_buffer.md 3.8): every face fills both the ingredient slots and the
 * container slot, and the inventory's slot rule (KettleRemainderMixin) sends a container only to the container slot and
 * anything else only to the ingredient slots. Upstream split it by face (top = ingredients, sides = containers).
 * Extraction is unchanged: the top pulls ingredients, the sides and bottom the output and the leftover slots.
 */
@Mixin(value = KettleItemHandler.class, remap = false)
public abstract class KettleItemHandlerMixin {

    private static final int INGREDIENT_SLOTS = 2;
    private static final int CONTAINER_SLOT = 3;

    @Shadow
    @Final
    private IItemHandler itemHandler;

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void ce$everyFaceRoutes(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(slot < INGREDIENT_SLOTS || slot == CONTAINER_SLOT ? itemHandler.insertItem(slot, stack, simulate) : stack);
    }
}
