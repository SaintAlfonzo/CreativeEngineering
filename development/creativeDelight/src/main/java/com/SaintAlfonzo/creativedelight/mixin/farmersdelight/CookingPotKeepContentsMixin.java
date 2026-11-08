package com.SaintAlfonzo.creativedelight.mixin.farmersdelight;

import com.SaintAlfonzo.creativedelight.cooking.CookwareContents;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

/**
 * Farmer's Delight cooking pot keeps its contents on a marked break ({@link CookwareContents}, kitchen.md 2.12):
 * {@code writeMeal} (the loot copy_meal function) writes all nine slots, the meal's container, the name and the cook
 * progress; {@code getDroppableInventory} leaves those slots out, so only the remainder buffer drops.
 */
@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotKeepContentsMixin {

    @Shadow @Final private ItemStackHandler inventory;
    @Shadow private int cookTime;
    @Shadow private int cookTimeTotal;
    @Shadow private ItemStack mealContainerStack;
    @Shadow private Component customName;

    @Inject(method = "writeMeal", at = @At("RETURN"))
    private void ce$writeAll(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if (!CookwareContents.keeping((BlockEntity) (Object) this) || ce$empty()) return;
        CompoundTag out = cir.getReturnValue();
        out.put("Inventory", inventory.serializeNBT());
        out.put("Container", mealContainerStack.serializeNBT());
        out.putInt("CookTime", cookTime);
        out.putInt("CookTimeTotal", cookTimeTotal);
        if (customName != null) out.putString("CustomName", Component.Serializer.toJson(customName));
    }

    @Inject(method = "getDroppableInventory", at = @At("RETURN"))
    private void ce$keepSlots(CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        if (!CookwareContents.keeping((BlockEntity) (Object) this)) return;
        NonNullList<ItemStack> drops = cir.getReturnValue();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack kept = inventory.getStackInSlot(slot);
            if (!kept.isEmpty()) drops.removeIf(s -> s == kept);
        }
    }

    @Unique
    private boolean ce$empty() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) return false;
        }
        return true;
    }
}
