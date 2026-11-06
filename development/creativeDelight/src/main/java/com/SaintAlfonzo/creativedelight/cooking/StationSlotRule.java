package com.SaintAlfonzo.creativedelight.cooking;

import net.minecraft.world.item.ItemStack;

/**
 * Which items a station's inventory slots take (feature_writeup/remainder_buffer.md 3.8): containers only in the
 * container slot, never in an ingredient slot. Set on another mod's {@code ItemStackHandler} through
 * {@link Holder} (mixin.forge.ItemStackHandlerSlotRuleMixin) and checked by its {@code isItemValid}, which the menu
 * slots, shift-click and every hopper or pipe insert go through. Internal moves ({@code setStackInSlot}) skip it.
 */
@FunctionalInterface
public interface StationSlotRule {

    /** false = {@code stack} may not go into {@code slot}. */
    boolean allows(int slot, ItemStack stack);

    /** Mixed into {@code net.minecraftforge.items.ItemStackHandler}. */
    interface Holder {
        void ce$setSlotRule(StationSlotRule rule);
    }

    /** Sets {@code rule} on {@code handler} (a no-op if the mixin did not apply). */
    static void apply(Object handler, StationSlotRule rule) {
        if (handler instanceof Holder holder) holder.ce$setSlotRule(rule);
    }
}
