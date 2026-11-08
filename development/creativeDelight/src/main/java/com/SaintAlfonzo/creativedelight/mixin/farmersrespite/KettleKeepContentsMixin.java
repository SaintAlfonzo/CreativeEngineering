package com.SaintAlfonzo.creativedelight.mixin.farmersrespite;

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
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;

/**
 * Farmer's Respite kettle keeps its contents on a marked break ({@link CookwareContents}, kitchen.md 2.12):
 * {@code writeMeal} (the loot copy_meal function) writes all five slots, the drink container, the name and the brew
 * progress; {@code getDroppableInventory} leaves those slots out, so only the remainder buffer and the
 * tank boxes drop. The tank itself rides along already (KettleTankMixin).
 */
@Mixin(value = KettleBlockEntity.class, remap = false)
public abstract class KettleKeepContentsMixin {

    @Shadow @Final private ItemStackHandler inventory;
    @Shadow private int brewTime;
    @Shadow private int brewTimeTotal;
    @Shadow private ItemStack drinkContainerStack;
    @Shadow private Component customName;

    @Inject(method = "writeMeal", at = @At("RETURN"))
    private void ce$writeAll(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        if (!CookwareContents.keeping((BlockEntity) (Object) this) || ce$empty()) return;
        CompoundTag out = cir.getReturnValue();
        out.put("Inventory", inventory.serializeNBT());
        out.put("Container", drinkContainerStack.serializeNBT());
        out.putInt("BrewTime", brewTime);
        out.putInt("BrewTimeTotal", brewTimeTotal);
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
