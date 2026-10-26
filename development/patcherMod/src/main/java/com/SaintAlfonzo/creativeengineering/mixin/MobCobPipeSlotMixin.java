package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.CornDelightCompat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Cob Pipe (a KubeJS item, so no getEquipmentSlot override) belongs in the head slot, as 1.2.11's Equipable item:
 * the armor slot accepts it, shift click and dispensers put it there (feature_writeup/corn_delight.md 2.3).
 */
@Mixin(Mob.class)
public abstract class MobCobPipeSlotMixin {

    // m_147233_ = Mob.getEquipmentSlotForItem(ItemStack), static (no refmap: SRG name, remap = false)
    @Inject(method = "m_147233_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void creativeengineering$cobPipeHead(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (CornDelightCompat.isCobPipe(stack)) cir.setReturnValue(EquipmentSlot.HEAD);
    }
}
