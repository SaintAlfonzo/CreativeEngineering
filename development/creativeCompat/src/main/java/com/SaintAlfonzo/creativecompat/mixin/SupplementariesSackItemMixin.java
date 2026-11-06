package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.SackBundle;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bundle-style take-out for sacks ({@link SackBundle}): right-click a filled sack in a slot with an empty
 * cursor and its selected stack goes to the cursor. Supplementaries does nothing there (the click would pick
 * the sack up); an empty sack still gets picked up. Runs on both sides, like the vanilla bundle.
 */
@Mixin(value = SackItem.class, remap = false)
public abstract class SupplementariesSackItemMixin {

    @Inject(method = "m_142305_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$takeStack(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action,
                                               Player player, SlotAccess access,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (action != ClickAction.SECONDARY || !incoming.isEmpty() || !slot.allowModification(player)) {
            return;
        }
        ItemStack taken = SackBundle.takeSelected(stack, player);
        if (!taken.isEmpty()) {
            access.set(taken);
            cir.setReturnValue(true);
        }
    }
}
