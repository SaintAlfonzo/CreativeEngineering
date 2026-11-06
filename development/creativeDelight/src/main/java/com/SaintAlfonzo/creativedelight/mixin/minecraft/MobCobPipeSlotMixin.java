package com.SaintAlfonzo.creativedelight.mixin.minecraft;

import com.SaintAlfonzo.creativedelight.compat.CornDelightCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Cob Pipe (a KubeJS item, so no getEquipmentSlot override) belongs in the head slot, as 1.2.11's Equipable item:
 * the armor slot accepts it, shift click and dispensers put it there (feature_writeup/corn_delight.md 2.3).
 */
@Mixin(LivingEntity.class)
public abstract class MobCobPipeSlotMixin {

    // m_147233_ = LivingEntity.getEquipmentSlotForItem(ItemStack), static (no refmap: SRG name, remap = false). Targeted
    // Mob until 2026-11-01: never applied in patcherMod (no defaultRequire), crashed the server under CD (defaultRequire 1)
    @Inject(method = "m_147233_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$cobPipeHead(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (CornDelightCompat.isCobPipe(stack)) cir.setReturnValue(EquipmentSlot.HEAD);
    }
}
