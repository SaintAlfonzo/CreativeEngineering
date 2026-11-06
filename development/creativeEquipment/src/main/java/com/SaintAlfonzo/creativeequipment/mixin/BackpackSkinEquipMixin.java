package com.SaintAlfonzo.creativeequipment.mixin;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeequipment.CEQConfig;
import com.SaintAlfonzo.creativeequipment.backpackskin.BackpackSkins;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** With the equip switch off a skinned backpack has no equipment slot: no chest slot, shift-click or dispenser equip. */
@Mixin(value = BackpackItem.class, remap = false)
public abstract class BackpackSkinEquipMixin {

    @Inject(method = "getEquipmentSlot", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void creativeengineering$noSkinEquip(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (!CEQConfig.BACKPACK_SKINS.allowEquip.get() && BackpackSkins.getSkin(stack) != null) cir.setReturnValue(null);
    }
}
