package com.SaintAlfonzo.creativedelight.mixin.forge;

import com.SaintAlfonzo.creativedelight.cooking.StationSlotRule;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A {@link StationSlotRule} on any ItemStackHandler, for stations whose inventory is another mod's anonymous handler
 * (Farmer's Delight cooking pot, Farmer's Respite kettle; remainder_buffer.md 3.8). Handlers without a rule are
 * untouched (one null check). Subclasses that override isItemValid without calling super are not covered; the
 * stations set here do not override it.
 */
@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class ItemStackHandlerSlotRuleMixin implements StationSlotRule.Holder {

    @Unique
    private StationSlotRule ce$slotRule;

    @Override
    public void ce$setSlotRule(StationSlotRule rule) {
        ce$slotRule = rule;
    }

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void ce$slotRule(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        StationSlotRule rule = ce$slotRule;
        if (rule != null && !rule.allows(slot, stack)) {
            cir.setReturnValue(false);
        }
    }
}
